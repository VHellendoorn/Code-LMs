// Decompiled with JetBrains decompiler
// Type: HalconDotNet.HDevOpMultiWindowImpl
// Assembly: hdevenginedotnetxl, Version=17.12.0.0, Culture=neutral, PublicKeyToken=4973bed59ddbf2b8
// MVID: 1BC5D9BA-5A99-483F-ACA6-A4C6BCF4A886
// Assembly location: C:\Program Files\MVTec\HALCON-17.12-Progress\bin\dotnet35\hdevenginedotnetxl.dll

namespace HalconDotNet
{
    /// <summary>Convenience implementation of HDevOperators managing multiple windows</summary>
    public class HDevOpMultiWindowImpl : HDevOpFixedWindowImpl
    {
        /// <summary>Window IDs of all open windows</summary>
        protected HTuple windowIDs;
        /// <summary>Window IDs of all designated external windows</summary>
        protected HTuple fixedIDs;
        /// <summary>Flags indicating whether a fixed window is "dev-open"</summary>
        protected HTuple fixedUsed;

        private static HTuple TupleFind2(HTuple t1, HTuple t2)
        {
            HTuple htuple = t1.TupleFind(t2);
            if (htuple.Length > 0 && htuple[0].I == -1)
                htuple = new HTuple();
            return htuple;
        }

        /// <summary>Creates an instance initially managing zero open windows</summary>
        /// <param name="windowIDs">List of IDs for existing HALCON windows</param>
        public HDevOpMultiWindowImpl(params HTuple[] windowIDs)
          : base((HTuple)0)
        {
            this.fixedIDs = new HTuple().TupleConcat(windowIDs);
            this.fixedUsed = HTuple.TupleGenConst((HTuple)this.fixedIDs.Length, (HTuple)0);
            this.windowIDs = new HTuple();
            HTuple windowID;
            this.DevOpenWindow((HTuple)0, (HTuple)0, (HTuple)512, (HTuple)512, (HTuple)"black", out windowID);
        }

        /// <summary>Creates an instance initially managing zero open windows</summary>
        /// <param name="windows">List of existing HALCON windows</param>
        public HDevOpMultiWindowImpl(params HWindow[] windows)
          : this(HTool.ConcatArray((HTool[])windows))
        {
        }

        /// <summary>Creates an instance with no fixed windows</summary>
        public HDevOpMultiWindowImpl()
          : this(new HTuple[0])
        {
        }

        /// <summary>Opens a window, appends window ID and sets active window</summary>
        public override void DevOpenWindow(
          HTuple row,
          HTuple column,
          HTuple width,
          HTuple height,
          HTuple background,
          out HTuple windowID)
        {
            HTuple htuple = HDevOpMultiWindowImpl.TupleFind2(this.fixedUsed, (HTuple)0);
            if (htuple.Length > 0)
            {
                this.activeID = (HTuple)this.fixedIDs[htuple[0].I];
                this.fixedUsed[htuple[0].I] = (HTupleElements)1;
            }
            else
            {
                HOperatorSet.SetWindowAttr((HTuple)"background_color", background);
                HOperatorSet.OpenWindow(row, column, width, height, (HTuple)0, (HTuple)"visible", (HTuple)"", out this.activeID);
            }
            this.windowIDs[this.windowIDs.Length] = (HTupleElements)this.activeID;
            windowID = this.activeID.Clone();
        }

        /// <summary>Closes active window, removes it from list and activates another window</summary>
        public override void DevCloseWindow()
        {
            if (this.activeID.I == 0)
                return;
            HTuple htuple1 = HDevOpMultiWindowImpl.TupleFind2(this.fixedIDs, this.activeID);
            if (htuple1.Length > 0)
                this.fixedUsed[htuple1[0].I] = (HTupleElements)0;
            else
                HOperatorSet.CloseWindow(this.activeID);
            HTuple htuple2 = HDevOpMultiWindowImpl.TupleFind2(this.windowIDs, this.activeID);
            if (htuple2.Length > 0)
                this.windowIDs = this.windowIDs.TupleRemove((HTuple)htuple2[0]);
            if (this.windowIDs.Length > 0)
                this.activeID = (HTuple)this.windowIDs[this.windowIDs.Length - 1];
            else
                this.activeID = (HTuple)0;
        }

        /// <summary>Sets the active window ID</summary>
        public override void DevSetWindow(HTuple windowHandle)
        {
            this.activeID = windowHandle.Clone();
        }

        /// <summary>Sets the active window ID</summary>
        public override void DevGetWindow(out HTuple windowHandle)
        {
            windowHandle = this.activeID.Clone();
        }

        /// <summary>No action for fixed windows, adapt size otherwise</summary>
        public override void DevSetWindowExtents(
          HTuple row,
          HTuple column,
          HTuple width,
          HTuple height)
        {
            if (HDevOpMultiWindowImpl.TupleFind2(this.fixedIDs, this.activeID).Length != 0)
                return;
            HOperatorSet.SetWindowExtents(this.activeID, row, column, width, height);
        }
    }
}
