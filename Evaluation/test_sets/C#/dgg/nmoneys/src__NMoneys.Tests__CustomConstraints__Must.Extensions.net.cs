using System;
using NMoneys.Serialization;
using NUnit.Framework.Constraints;
using Testing.Commons;
using Testing.Commons.NUnit.Constraints;
using Testing.Commons.Serialization;

namespace NMoneys.Tests.CustomConstraints
{
	internal static partial class MustExtensions
	{
		public static Constraint BinarySerializable<T>(this Must.BeEntryPoint entryPoint, Constraint constraintOverDeserialized)
		{
			return new SerializationConstraint<T>(new BinaryRoundtripSerializer<T>(), constraintOverDeserialized);
		}

		public static Constraint XmlSerializable<T>(this Must.BeEntryPoint entryPoint)
		{
			return new SerializationConstraint<T>(new XmlRoundtripSerializer<T>(), NUnit.Framework.Is.Not.Null);
		}

		public static Constraint XmlDeserializableInto<T>(this Must.BeEntryPoint entryPoint, T to)
		{
			return new DeserializationConstraint<T>(new XmlDeserializer(), NUnit.Framework.Is.EqualTo(to));
		}

		public static Constraint DataContractSerializable<T>(this Must.BeEntryPoint entryPoint)
		{
			return new SerializationConstraint<T>(new DataContractRoundtripSerializer<T>(), NUnit.Framework.Is.Not.Null);
		}

		public static Constraint DataContractDeserializableInto<T>(this Must.BeEntryPoint entryPoint, T to)
		{
			return new DeserializationConstraint<T>(new DataContractDeserializer(), NUnit.Framework.Is.EqualTo(to));
		}

		public static Constraint DataContractJsonSerializable<T>(this Must.BeEntryPoint entryPoint)
		{
			return new SerializationConstraint<T>(new DataContractJsonRoundtripSerializer<T>(dataContractSurrogate: new DataContractSurrogate()), NUnit.Framework.Is.Not.Null);
		}

		public static Constraint DataContractJsonDeserializableInto<T>(this Must.BeEntryPoint entryPoint, T to)
		{
			return new DeserializationConstraint<T>(new DataContractJsonDeserializer(dataContractSurrogate: new DataContractSurrogate()), NUnit.Framework.Is.EqualTo(to));
		}
	}
}
