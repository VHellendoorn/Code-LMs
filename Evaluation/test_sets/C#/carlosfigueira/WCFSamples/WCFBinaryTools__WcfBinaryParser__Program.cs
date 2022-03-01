using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Reflection;
using BinaryFormatParser;
using System.Xml;

namespace WcfBinaryParser
{
    public class Program
    {
        private static void PrintUsage()
        {
            Console.WriteLine("Usage: {0} [options] <file.bin>", Path.GetFileName(Assembly.GetEntryAssembly().CodeBase));
            Console.WriteLine("   <file.bin> - the file with the data on the binary format");
            Console.WriteLine("Options:");
            Console.WriteLine("   -noDict - Do not try to read dictionary strings");
            Console.WriteLine("   -static <fileName> - use static dictionary from the strings in the given file. Default: WCF static dictionary");
            Console.WriteLine("   -session <fileName> - use dynamic dictionary from the strings in the given file. Default: no dynamic dictionary");
        }

        private static bool TryParseArgs(string[] args, out string binaryFileName, out IXmlDictionary xmlDictionary, out XmlBinaryReaderSession readerSession)
        {
            binaryFileName = null;
            xmlDictionary = null;
            readerSession = null;
            bool useDictionaries = true;
            string staticDictionaryFile = null;
            string dynamicDictionaryFile = null;
            int offset = 0;
            while (offset < args.Length)
            {
                if (args[offset] == "-static")
                {
                    if (offset + 1 >= args.Length)
                    {
                        return false;
                    }

                    staticDictionaryFile = args[offset + 1];
                    offset += 2;
                }
                if (args[offset] == "-session")
                {
                    if (offset + 1 >= args.Length)
                    {
                        return false;
                    }

                    dynamicDictionaryFile = args[offset + 1];
                    offset += 2;
                }
                else if (args[offset] == "-noDict")
                {
                    offset++;
                    useDictionaries = false;
                }
                else
                {
                    binaryFileName = args[offset];
                    offset++;
                    if (offset != args.Length)
                    {
                        return false;
                    }
                }
            }

            if (useDictionaries)
            {
                if (staticDictionaryFile == null)
                {
                    xmlDictionary = WcfDictionary.GetStaticDictionary();
                }
                else
                {
                    XmlDictionary temp = new XmlDictionary();
                    xmlDictionary = temp;
                    foreach (string entry in File.ReadAllLines(staticDictionaryFile))
                    {
                        temp.Add(entry);
                    }
                }

                readerSession = new XmlBinaryReaderSession();
                if (dynamicDictionaryFile == null)
                {
                    for (int i = 0; i < 200; i++)
                    {
                        readerSession.Add(i, "session_" + i);
                    }
                }
                else
                {
                    string[] lines = File.ReadAllLines(dynamicDictionaryFile);
                    for (int i = 0; i < lines.Length; i++)
                    {
                        readerSession.Add(i, lines[i]);
                    }
                }
            }
            else
            {
                readerSession = null;
                xmlDictionary = null;
            }

            return true;
        }

        public static void Main(string[] args)
        {
            XmlBinaryReaderSession readerSession;
            IXmlDictionary xmlDictionary;
            string binaryFileName;
            if (!TryParseArgs(args, out binaryFileName, out xmlDictionary, out readerSession))
            {
                PrintUsage();
                return;
            }

            byte[] binaryDoc = File.ReadAllBytes(binaryFileName);
            XmlBinaryParser parser = new XmlBinaryParser(binaryDoc);
            Console.WriteLine(parser.RootNode.ToString(xmlDictionary, readerSession));
        }
    }
}
