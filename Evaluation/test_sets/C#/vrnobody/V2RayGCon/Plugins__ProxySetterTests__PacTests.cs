using Microsoft.VisualStudio.TestTools.UnitTesting;
using ProxySetter.Model.Data;
using System.Collections.Generic;
using static VgcApis.Misc.Utils;

namespace ProxySetterTests
{
    [TestClass]
    public class PacTests
    {
        [DataTestMethod]
        [DataRow("172.316.254.1", false)]
        [DataRow("0.254.255.0", true)]
        [DataRow("192.168.1.15_1", false)]
        [DataRow("127.0.0.1", true)]
        [DataRow("0.0.0.", false)]
        [DataRow("0.0.0.0", true)]
        public void IsIPTest(string address, bool expect)
        {
            Assert.AreEqual(expect, ProxySetter.Misc.Utils.IsIP(address));
        }

        [DataTestMethod]
        [DataRow("172.316.254.1", 2906455553)] // becareful not a valid ip
        [DataRow("0.254.255.0", 16711424)]
        [DataRow("127.0.0.1", 2130706433)]
        [DataRow("0.0.0.0", 0)]
        public void IP2Int32Test(string address, long expect)
        {
            Assert.AreEqual(expect, ProxySetter.Misc.Utils.IP2Long(address));
        }

        [DataTestMethod]
        [DataRow("11,22 2,5 3,4 7,8 1,2 6,6", "1,8 11,22")]
        [DataRow("11,22 2,5 3,4 7,8 1,2", "1,5 7,8 11,22")]
        [DataRow("1,2 3,4 1,1 1,2", "1,4")]
        public void CompactRangeArrayListTest(string org, string expect)
        {
            long[] rangeParser(string rangeArray)
            {
                var v = rangeArray.Split(',');
                return new long[] {
                    (long)Str2Int(v[0]),
                    (long)Str2Int(v[1]),
                };
            }

            List<long[]> listParser(string listString)
            {
                var r = new List<long[]>();
                foreach (var item in listString.Split(' '))
                {
                    r.Add(rangeParser(item));
                }
                return r;
            }

            var o = listParser(org);
            var e = listParser(expect);
            var result = ProxySetter.Misc.Utils.CompactCidrList(ref o);

            for (int i = 0; i < result.Count; i++)
            {
                if (result[i][0] != e[i][0] || result[i][1] != e[i][1])
                {
                    Assert.Fail();
                }
            }
        }

        [DataTestMethod]
        [DataRow(null, ",,,,")]
        [DataRow(
           "type=blacklist&ip=8.7.6.5&port=4321&proto=http",
           "blacklist,http,8.7.6.5,4321,")]
        //// url = "type,proto,ip,port,debug"
        public void GetProxyParamsFromUrlTest(string url, string expect)
        {
            var proxyParams = ProxySetter.Misc.Utils.GetQureryParamsFrom(
                url == null ? null : (
                "http://localhost:3000/pac/?&"
                + url
                + "&key="
                + RandomHex(8)));

            var expParts = expect.Split(',');

            if (
                (proxyParams.type ?? "") != expParts[0]
                || (proxyParams.proto ?? "") != expParts[1]
                || (proxyParams.ip ?? "") != expParts[2]
                || (proxyParams.port ?? "") != expParts[3]
                || (proxyParams.debug ?? "") != expParts[4])
            {
                Assert.Fail();
            }
        }

        [DataTestMethod]
        [DataRow(1, 1, 3, 3, Enum.Overlaps.None)]
        [DataRow(1, 3, 2, 3, Enum.Overlaps.Right)]
        [DataRow(1, 3, 0, 2, Enum.Overlaps.Left)]
        [DataRow(-1, 1, 2, 10, Enum.Overlaps.None)]
        [DataRow(3, 4, -1, 1, Enum.Overlaps.None)]
        [DataRow(1, 1, 1, 1, Enum.Overlaps.All)]
        [DataRow(-10, -1, -5, -3, Enum.Overlaps.Middle)]
        [DataRow(1, 4, 3, 3, Enum.Overlaps.Middle)]
        [DataRow(1, 2, 3, 4, Enum.Overlaps.None)]
        public void OverlapsTest(long aStart, long aEnd, long bStart, long bEnd, Enum.Overlaps expect)
        {
            var a = new long[] { aStart, aEnd };
            var b = new long[] { bStart, bEnd };
            var result = ProxySetter.Misc.Utils.Overlaps(a, b);
            Assert.AreEqual(expect, result);
        }

        [DataTestMethod]
        [DataRow((1L << 32) - 1, "255.255.255.255")]
        [DataRow(1, "0.0.0.1")]
        [DataRow(0, "0.0.0.0")]
        [DataRow(-1, "0.0.0.0")]
        [DataRow(1L << 32, "0.0.0.0")]
        [DataRow(255 + 123 * 256 + 12 * 256 * 256 + 192L * 256 * 256 * 256, "192.12.123.255")]
        public void Long2IpTest(long number, string expect)
        {
            var ip = ProxySetter.Misc.Utils.Long2Ip(number);
            Assert.AreEqual(expect, ip);
        }

        [DataTestMethod]
        [DataRow("0.0.0.0/32", "0.0.0.0,0.0.0.0")]
        [DataRow("0.0.0.0/-1", "0.0.0.0,255.255.255.255")]
        [DataRow("0.0.0.0/0", "0.0.0.0,255.255.255.255")]
        [DataRow("0.0.0.0/1", "0.0.0.0,127.255.255.255")]
        [DataRow("0.0.0.0/2", "0.0.0.0,63.255.255.255")]
        public void CidrRangeArrayTest(string cidr, string expect)
        {
            var c = ProxySetter.Misc.Utils.Cidr2RangeArray(cidr);
            var a = ProxySetter.Misc.Utils.Long2Ip(c[0]);
            var b = ProxySetter.Misc.Utils.Long2Ip(c[1]);
            Assert.AreEqual(expect, a + "," + b);
        }
    }
}
