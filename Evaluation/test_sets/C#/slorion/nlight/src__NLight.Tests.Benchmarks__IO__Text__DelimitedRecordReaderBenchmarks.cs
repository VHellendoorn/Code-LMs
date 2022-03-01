// Author(s): Sébastien Lorion

using NLight.IO.Text;
using System.Data;
using System.IO;
using System.Text.RegularExpressions;
using DS = DataStreams.Csv;
using LW = LumenWorks.Framework.IO.Csv;
using CH = CsvHelper;

namespace NLight.Tests.Benchmarks.IO.Text
{
	public static class DelimitedRecordReaderBenchmarks
	{
		public static void ReadAll(DelimitedRecordReaderBenchmarkArguments args)
		{
			using (var reader = new DelimitedRecordReader(new StreamReader(args.Path, args.Encoding, true, args.BufferSize), args.BufferSize))
			{
				reader.AdvancedEscapingEnabled = args.AdvancedEscapingEnabled;
				reader.DoubleQuoteEscapingEnabled = args.DoubleQuoteEscapingEnabled;
				reader.SkipEmptyLines = args.SkipEmptyLines;
				reader.TrimWhiteSpaces = args.TrimWhiteSpaces;

				if (args.FieldIndex > -1)
				{
					reader.DynamicColumnCount = false;

					for (int i = 0; i < args.FieldIndex + 1; i++)
						reader.Columns.Add(new DelimitedRecordColumn(reader.GetDefaultColumnName(i)));
				}

				string s;

				while (reader.Read() != ReadResult.EndOfFile)
				{
					for (int i = 0; i < reader.Columns.Count - 1; i++)
						s = reader[i];
				}
			}
		}

		public static void ReadAll_LumenWorks(DelimitedRecordReaderBenchmarkArguments args)
		{
			using (var reader = new LW.CsvReader(new StreamReader(args.Path, args.Encoding, true, args.BufferSize), false, LW.CsvReader.DefaultDelimiter, LW.CsvReader.DefaultQuote, LW.CsvReader.DefaultEscape, LW.CsvReader.DefaultComment, args.TrimWhiteSpaces ? LW.ValueTrimmingOptions.All : LW.ValueTrimmingOptions.None, args.BufferSize))
			{
				reader.SkipEmptyLines = args.SkipEmptyLines;

				string s;

				if (args.FieldIndex < 0)
				{
					while (reader.ReadNextRecord())
					{
						for (int i = 0; i < reader.FieldCount; i++)
							s = reader[i];
					}
				}
				else
				{
					while (reader.ReadNextRecord())
					{
						for (int i = 0; i < args.FieldIndex + 1; i++)
							s = reader[i];
					}
				}
			}
		}

		public static void ReadAll_DataStreams(DelimitedRecordReaderBenchmarkArguments args)
		{
			using (var reader = new DS.CsvReader(new StreamReader(args.Path, args.Encoding, true, args.BufferSize)))
			{
				reader.Settings.CaptureRawRecord = false;
				reader.Settings.CaseSensitive = false;
				reader.Settings.SafetySwitch = false;
				reader.Settings.UseComments = true;

				if (args.AdvancedEscapingEnabled)
					reader.Settings.EscapeMode = DS.EscapeMode.Backslash;
				else
					reader.Settings.EscapeMode = DS.EscapeMode.Doubled;

				reader.Settings.SkipEmptyRecords = args.SkipEmptyLines;
				reader.Settings.TrimWhitespace = args.TrimWhiteSpaces;

				string s;

				if (args.FieldIndex < 0)
				{
					while (reader.ReadRecord())
					{
						for (int i = 0; i < reader.ColumnCount; i++)
							s = reader[i];
					}
				}
				else
				{
					while (reader.ReadRecord())
					{
						for (int i = 0; i < args.FieldIndex + 1; i++)
							s = reader[i];
					}
				}
			}
		}

		public static void ReadAll_CsvHelper(DelimitedRecordReaderBenchmarkArguments args)
		{
			var config = new CH.Configuration.Configuration
			{
				BufferSize = args.BufferSize,
				AllowComments = true,
				IgnoreBlankLines = args.SkipEmptyLines,
				HasHeaderRecord = false,
				DetectColumnCountChanges = true,
				TrimOptions = args.TrimWhiteSpaces ? CH.Configuration.TrimOptions.Trim | CH.Configuration.TrimOptions.InsideQuotes : CH.Configuration.TrimOptions.None
			};

			using (var reader = new CH.CsvReader(new StreamReader(args.Path, args.Encoding, true, args.BufferSize), config))
			{
				string s;

				if (args.FieldIndex < 0)
				{
					while (reader.Read())
					{
						var record = reader.Context.Record;
						for (int i = 0; i < record.Length; i++)
							s = record[i];
					}
				}
				else
				{
					while (reader.Read())
					{
						for (int i = 0; i < args.FieldIndex + 1; i++)
							s = reader[i];
					}
				}
			}
		}

		public static void ReadAll_Regex(DelimitedRecordReaderBenchmarkArguments args)
		{
			// regex from Jeffrey Friedl's Mastering Regular Expressions 2nd edition, p. 271
			// does NOT handle trimming and multiline fields
			Regex regex = new Regex(@"
				\G(^|,)
				""(?<field> (?> [^""]*) (?> """" [^""]* )* )""
				| (?<field> [^"",]* )",
				RegexOptions.Compiled | RegexOptions.ExplicitCapture | RegexOptions.IgnorePatternWhitespace);

			int fieldGroupIndex = regex.GroupNumberFromName("field");

			using (var sr = new StreamReader(args.Path, args.Encoding, true, args.BufferSize))
			{
				string s;

				if (args.FieldIndex < 0)
				{
					while ((s = sr.ReadLine()) != null)
					{
						MatchCollection mc = regex.Matches(s);

						for (int i = 0; i < mc.Count; i += 2)
							s = mc[i].Groups[fieldGroupIndex].Value;
					}
				}
				else
				{
					while ((s = sr.ReadLine()) != null)
					{
						MatchCollection mc = regex.Matches(s);

						for (int i = 0; i < args.FieldIndex + 1; i++)
							s = mc[i * 2].Groups[fieldGroupIndex].Value;
					}
				}
			}
		}
	}
}