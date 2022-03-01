using System;

namespace Wintellect.Sterling.Test.Helpers
{
    public class TestCompositeKeyClass
    {
        public TestCompositeKeyClass()
        {
        }

        public TestCompositeKeyClass(int key1, string key2, Guid key3, DateTime key4)
        {
            Key1 = key1;
            Key2 = key2;
            Key3 = key3;
            Key4 = key4;
        }

        public int Key1 { get; set; }
        public string Key2 { get; set; }
        public Guid Key3 { get; set; }
        public DateTime Key4 { get; set; }

        public override bool Equals(object obj)
        {
            var other = obj as TestCompositeKeyClass;
            if (other == null)
            {
                return false;
            }

            return other.Key1.Equals(Key1) && other.Key2.Equals(Key2)
                   && other.Key3.Equals(Key3) && other.Key4.Equals(Key4);
        }

        public override int GetHashCode()
        {
            return string.Format("{0}{1}{2}{3}", Key1, Key2, Key3, Key4).GetHashCode();
        }

    }
}