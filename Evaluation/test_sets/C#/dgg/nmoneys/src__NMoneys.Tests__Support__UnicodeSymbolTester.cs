using System;
using NMoneys.Support;
using NUnit.Framework;

namespace NMoneys.Tests.Support
{
	[TestFixture]
	public class UnicodeSymbolTester
	{
		#region FromTokenizedCodePoints

		[TestCase("65 66 67", "ABC", new[] { 65, 66, 67 })]
		[TestCase("97 98", "ab", new[] { 97, 98 })]
		[TestCase("36", "$", new[] { 36 })]
		[TestCase("8364", "€", new[] { 8364 })]
		[TestCase("36", "$", new[] { 36 })]
		[TestCase("8364 32 36", "€ $", new[] { 8364, 32, 36 })]
		public void FromTokenizedCodePoints_ValidTokenizedPoints_TranslatedToSymbolAndCodePoints(
			string tokenizedCodePoints,
			string symbol,
			int[] codePoints)
		{
			UnicodeSymbol subject = UnicodeSymbol.FromTokenizedCodePoints(tokenizedCodePoints);

			Assert.That(subject.TokenizedCodePoints, Is.EqualTo(tokenizedCodePoints));
			Assert.That(subject.Symbol, Is.EqualTo(symbol));
			Assert.That(subject.CodePoints, Is.EqualTo(codePoints));
		}

		[TestCase("65,66")]
		[TestCase("65-66")]
		[TestCase("65:66")]
		public void FromTokenizedCodePoints_WrongTokenizer_Exception(string wrongTokenizer)
		{
			Assert.That(() => UnicodeSymbol.FromTokenizedCodePoints(wrongTokenizer), Throws.InstanceOf<FormatException>());
		}

		[TestCase("a b", typeof(FormatException))]
		[TestCase("A", typeof(FormatException))]
		[TestCase("-4 -3", typeof(ArgumentOutOfRangeException))]
		[TestCase(null, typeof(ArgumentNullException))]
		public void FromTokenizedCodePoints_NotACodePointChain_Exception(string notAChainOfCodePoints, Type expectedException)
		{
			Assert.That(()=> UnicodeSymbol.FromTokenizedCodePoints(notAChainOfCodePoints), Throws.InstanceOf(expectedException));
		}

		[Test]
		public void FromTokenizedCodePoints_Empty_EmptySymbol()
		{
			UnicodeSymbol empty = UnicodeSymbol.FromTokenizedCodePoints(string.Empty);

			Assert.That(empty.Symbol, Is.Empty);
			Assert.That(empty.CodePoints, Is.Empty);
			Assert.That(empty.TokenizedCodePoints, Is.Empty);
		}

		#endregion

		#region FromSymbol

		[TestCase("ABC", "65 66 67", new[] { 65, 66, 67 })]
		[TestCase("ab", "97 98", new[] { 97, 98 })]
		[TestCase("$", "36", new[] { 36 })]
		[TestCase("€", "8364", new[] { 8364 })]
		[TestCase("€ $", "8364 32 36", new[] { 8364, 32, 36 })]
		public void FromSymbol_TranslatedCodePoints(
			string symbol,
			string tokenizedCodePoints,
			int[] codePoints)
		{
			UnicodeSymbol subject = UnicodeSymbol.FromSymbol(symbol);

			Assert.That(subject.Symbol, Is.EqualTo(symbol));
			Assert.That(subject.TokenizedCodePoints, Is.EqualTo(tokenizedCodePoints));
			Assert.That(subject.CodePoints, Is.EqualTo(codePoints));
		}

		[TestCase(null, typeof(ArgumentNullException))]
		[TestCase("", typeof(ArgumentException))]
		public void FromSymbol_NotASymbol_Exception(string notASymbol, Type expectedException)
		{
			Assert.That(()=> UnicodeSymbol.FromSymbol(notASymbol), Throws.InstanceOf(expectedException));
		}

		#endregion
	}
}
