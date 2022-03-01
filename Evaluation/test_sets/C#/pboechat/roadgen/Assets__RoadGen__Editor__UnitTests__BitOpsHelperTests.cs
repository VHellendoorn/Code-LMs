using NUnit.Framework;
using RoadGen;

[TestFixture]
public class BitOpsHelperTests
{
    public void test_bfind()
    {
        // 11 = 1011, so the third flipped bit is in index 3
        Assert.AreEqual(3, BitOpsHelper.BFind(11, 3));
        // 3 = 11, so there's no third flipped bit and BFind returns 32
        Assert.AreEqual(32, BitOpsHelper.BFind(3, 3));
        // searching for the 0th flipped bit always causes BFind to return 32
        Assert.AreEqual(32, BitOpsHelper.BFind(1, 0));
    }

}
