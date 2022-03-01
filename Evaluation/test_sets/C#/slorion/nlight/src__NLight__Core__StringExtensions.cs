// Author(s): Sébastien Lorion

using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;

namespace NLight.Core
{
	public static class StringExtensions
	{
		public static string Truncate(this string source, int maxLength)
		{
			if (source == null) throw new ArgumentNullException(nameof(source));

			return source.Length <= maxLength ? source : source.Substring(0, maxLength);
		}

		public static bool EqualsIgnoreCase(this string source, string other, bool useOrdinalComparison = true)
		{
			if (source == null) throw new ArgumentNullException(nameof(source));

			return string.Equals(source, other, useOrdinalComparison ? StringComparison.OrdinalIgnoreCase : StringComparison.CurrentCultureIgnoreCase);
		}

		public static bool Contains(this string source, string value, StringComparison comparisonType)
		{
			if (source == null) throw new ArgumentNullException(nameof(source));

			// .NET string functions consider an empty string as always contained into another string, so only null values are rejected
			if (value == null) throw new ArgumentNullException(nameof(value));

			return source.IndexOf(value, comparisonType) > -1;
		}

		public static bool Contains(this string source, string other, CompareOptions options, CultureInfo culture = null)
			=> IndexOf(source, other, options, culture) > -1;

		public static int IndexOf(this string source, string other, CompareOptions options, CultureInfo culture = null)
			=> (culture ?? CultureInfo.CurrentCulture).CompareInfo.IndexOf(source, other, options);

		public static int LastIndexOf(this string source, string other, CompareOptions options, CultureInfo culture = null)
			=> (culture ?? CultureInfo.CurrentCulture).CompareInfo.LastIndexOf(source, other, options);

		public static bool EqualsTo(this string source, string other, CompareOptions options, CultureInfo culture = null)
			=> CompareTo(source, other, options, culture) == 0;

		public static int CompareTo(this string source, string other, CompareOptions options, CultureInfo culture = null)
			=> (culture ?? CultureInfo.CurrentCulture).CompareInfo.Compare(source, other, options);

		public static string RemoveDiacriticsAndRecompose(this string source, bool fullCompatibilityDecomposition = false, Func<char, char> customConvert = null)
		{
			return new string(RemoveDiacritics(source, fullCompatibilityDecomposition, customConvert).ToArray())
				.Normalize(NormalizationForm.FormC);
		}

		// based on http://stackoverflow.com/a/3769995/852829
		public static IEnumerable<char> RemoveDiacritics(this string source, bool fullCompatibilityDecomposition = false, Func<char, char> customConvert = null)
		{
			if (source == null) throw new ArgumentNullException(nameof(source));

			if (source.Length <= 0)
				return source;
			else
			{
				var output = source
					.Normalize(fullCompatibilityDecomposition ? NormalizationForm.FormKD : NormalizationForm.FormD)
					.Where(
						c =>
						{
							switch (CharUnicodeInfo.GetUnicodeCategory(c))
							{
								case UnicodeCategory.NonSpacingMark:
								case UnicodeCategory.SpacingCombiningMark:
								case UnicodeCategory.EnclosingMark:
									return false;
								default:
									return true;
							}
						});

				if (customConvert != null)
					output = output.Select(c => customConvert(c));

				return output;
			}
		}
	}
}