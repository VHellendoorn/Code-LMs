using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Windows.Forms;
using static V2RayGCon.Misc.UI;

namespace V2RayGCon.Test
{
    [TestClass]
    public class UITest
    {

        public UITest()
        {

        }

        [TestMethod]
        public void UpdateControlOnDemandTest()
        {
            TextBox box = new TextBox();
            box.Text = "abc";
            var result = UpdateControlOnDemand(box, "def");
            Assert.AreEqual("def", box.Text);
            Assert.AreEqual(true, result);

            CheckBox cbox = new CheckBox();
            cbox.Checked = true;
            result = UpdateControlOnDemand(cbox, false);
            Assert.AreEqual(false, cbox.Checked);
            Assert.AreEqual(true, result);

            Assert.ThrowsException<ArgumentException>(() =>
            {
                UpdateControlOnDemand(box, 123);
            });
        }
    }
}
