using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HalconDotNet
{
    public class HData
    {
        internal HTuple tuple;

        internal HData()
        {
            this.tuple = new HTuple();
        }

        internal HData(HTuple t)
        {
            this.tuple = t;
        }

        internal HData(HData data)
        {
            this.tuple = data.tuple;
        }

        internal static HTuple ConcatArray(HData[] data)
        {
            HTuple htuple = new HTuple();
            for (int index = 0; index < data.Length; ++index)
                htuple = htuple.TupleConcat(data[index].tuple);
            return htuple;
        }

        internal void UnpinTuple()
        {
            this.tuple.UnpinTuple();
        }

        internal void Store(IntPtr proc, int parIndex)
        {
            this.tuple.Store(proc, parIndex);
        }

        internal int Load(IntPtr proc, int parIndex, int err)
        {
            return this.tuple.Load(proc, parIndex, err);
        }

        internal int Load(IntPtr proc, int parIndex, HTupleType type, int err)
        {
            return this.tuple.Load(proc, parIndex, type, err);
        }

        /// <summary>Provides access to the internally used tuple data</summary>
        public HTuple RawData
        {
            get
            {
                return this.tuple;
            }
            set
            {
                this.tuple = new HTuple(value);
            }
        }

        public static implicit operator HTuple(HData data)
        {
            return data.tuple;
        }

        /// <summary>Provides access to the value at the specified index</summary>
        public HTupleElements this[int index]
        {
            get
            {
                return this.tuple[index];
            }
            set
            {
                this.tuple[index] = value;
            }
        }
    }
}
