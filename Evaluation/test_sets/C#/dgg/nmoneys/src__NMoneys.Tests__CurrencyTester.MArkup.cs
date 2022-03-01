using NMoneys.Tests.CustomConstraints;
using NUnit.Framework;
using Testing.Commons;

namespace NMoneys.Tests
{
	[TestFixture]
	public partial class CurrencyTester
	{
		[Test, TestCaseSource(nameof(HtmlEntitySpec))]
		public void SomeCurrencies_HaveAnHtmlEntity(string isoSymbol, string entityName, string entityNumber)
		{
			Currency withHtmlEntity = Currency.Get(isoSymbol);
			Assert.That(withHtmlEntity.Entity, Must.Be.EntityWith(entityName, entityNumber));
		}

		[Test]
		public void MostCurrencies_DoNotHaveAnHtmlEntity()
		{
			Assert.That(Currency.Dkk.Entity, Is.SameAs(CharacterReference.Empty));
			Assert.That(Currency.Nok.Entity, Is.SameAs(CharacterReference.Empty));
		}

#pragma warning disable 169
		private static object[] HtmlEntitySpec =
		{
			new object[]{"ANG", "&fnof;", "&#402;" },
			new object[]{"AWG", "&fnof;", "&#402;" },
			
			new object[]{"GBP", "&pound;", "&#163;" },
			new object[]{"SHP", "&pound;", "&#163;" },
			new object[]{"FKP", "&pound;", "&#163;" },

			new object[]{"JPY", "&yen;", "&#165;" },
			new object[]{"CNY", "&yen;", "&#165;" },

			new object[]{"EUR", "&euro;", "&#8364;" },
			new object[]{"CHE", "&euro;", "&#8364;" },

			new object[]{"XXX", "&curren;", "&#164;" },
			new object[]{"XBA", "&curren;", "&#164;" },
			new object[]{"XBB", "&curren;", "&#164;" },
			new object[]{"XBC", "&curren;", "&#164;" },
			new object[]{"XBD", "&curren;", "&#164;" },
			new object[]{"XDR", "&curren;", "&#164;" },
			new object[]{"XPT", "&curren;", "&#164;" },
			new object[]{"XTS", "&curren;", "&#164;" },
		};
	}
}