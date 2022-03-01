
using System.Runtime.InteropServices;

namespace ETModel
{
    public class IOSSdk : IBaseSdk
    {

        [DllImport("__Internal")]
        public static extern void IOSWeChatLogin();

        [DllImport("__Internal")]
        public static extern void IOSWeChatShareImage(string imagePath, string title, string desc, int shadeType);

        [DllImport("__Internal")]
        public static extern void IOSWeChatShareUrl(string url, string title, string description, int shareType);

        [DllImport("__Internal")]
        public static extern void IOSWeChatPay(string prepayId, string nonceStr);

        [DllImport("__Internal")]
        public static extern int IOSGetLocation();

        [DllImport("__Internal")]
        public static extern int IOSGetBatteryElectric();

        [DllImport("__Internal")]
        public static extern int IOSCopyClipBoard(string content);

        [DllImport("__Internal")]
        public static extern void InstallationIpa(string url);

        public void WeChatLogin()
        {
            IOSWeChatLogin();
        }

        public void WeChatShareImage(string path, string title, string desc, int scene)
        {
            IOSWeChatShareImage(path, title, desc, (int)scene);
        }

        public void WeChatShareUrl(string url, string title, string description, int scene)
        {

            IOSWeChatShareUrl(url, title, description, (int)scene);

        }



        public int GetBatteryElectric()
        {
            return IOSGetBatteryElectric();
        }



        public void CopyClipBoard(string info)
        {
            IOSCopyClipBoard(info);
        }

        public void GetLocation()
        {
            IOSGetLocation();
        }

        public void Alipay(string info)
        {
            throw new System.NotImplementedException();
        }

        public void WeChatpay(string prepayId, string nonceStr)
        {
            IOSWeChatPay( prepayId, nonceStr);
        }

        public void OpenApp(string packageName, string appName, string versionUrl)
        {
            throw new System.NotImplementedException();
        }


        public void InstallApk(string fileFullPath)
        {
            InstallationIpa(fileFullPath);
        }

    }
}