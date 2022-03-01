using System;
using ETModel;
using UnityEngine;
using UnityEngine.UI;

namespace ETHotfix
{
    public class ActivityView : BaseView
    {
        #region 脚本工具生成的代码
        private Toggle mShareHaveAwardToggle;
        private Toggle mSignInActivityToggle;
        private Toggle mTurntableActivityToggle;
        private GameObject mActivityViewPointGo;
        public override void Init(GameObject go)
        {
            base.Init(go);
            mShareHaveAwardToggle = rc.Get<GameObject>("ShareHaveAwardToggle").GetComponent<Toggle>();
            mSignInActivityToggle = rc.Get<GameObject>("SignInActivityToggle").GetComponent<Toggle>();
            mTurntableActivityToggle = rc.Get<GameObject>("TurntableActivityToggle").GetComponent<Toggle>();
            mActivityViewPointGo = rc.Get<GameObject>("ActivityViewPointGo");
            InitPanel();
        }
        #endregion

        private ShareHaveAwardView shareHaveAwardView;
        private ShareHaveAwardView _ShareHaveAwardView
        {
            get
            {
                if (shareHaveAwardView == null)
                {
                    GameObject shareAwardPrefab = ResourcesComponent.Ins.GetResoure(UIType.ActivityAndAnnouncementPanel, "ShareHaveAwardView") as GameObject;
                    shareHaveAwardView = GameObject.Instantiate(shareAwardPrefab, mActivityViewPointGo.transform).AddItem<ShareHaveAwardView>();
                }
                return shareHaveAwardView;
            }
        }

        private SignInActivityView signInActivityView;

        private SignInActivityView _SignInActivityView
        {
            get
            {
                if (signInActivityView == null)
                {
                    GameObject signPrefab = ResourcesComponent.Ins.GetResoure(UIType.ActivityAndAnnouncementPanel, "SignInActivityView") as GameObject;
                    signInActivityView = GameObject.Instantiate(signPrefab, mActivityViewPointGo.transform).AddItem<SignInActivityView>();
                }
                return signInActivityView;
            }
        }

        private TurntableActivityView turntableActivityView;

        private TurntableActivityView _TurntableActivityView
        {
            get
            {
                if (turntableActivityView == null)
                {
                    GameObject turntabPrefab = ResourcesComponent.Ins.GetResoure(UIType.ActivityAndAnnouncementPanel, "TurntableActivityView") as GameObject;
                    turntableActivityView = GameObject.Instantiate(turntabPrefab, mActivityViewPointGo.transform).AddItem<TurntableActivityView>();
                }
                return turntableActivityView;
            }
        }
        public void InitPanel()
        {
            mShareHaveAwardToggle.Add(ShareHaveAwardToggleEvent);
            mSignInActivityToggle.Add(SignInActivityToggleEvent);
            mTurntableActivityToggle.Add(TurntableActivityToggleEvent);
            mSignInActivityToggle.isOn = true;
        }

        public void HideAllActivityView()
        {
            shareHaveAwardView?.Hide();
            signInActivityView?.Hide();
            turntableActivityView?.Hide();
        }
        public void ShareHaveAwardToggleEvent(bool isOn)
        {
            if (isOn)
            {
                HideAllActivityView();
                _ShareHaveAwardView.Show();
            }
        }

        public void SignInActivityToggleEvent(bool isOn)
        {
            if (isOn)
            {
                HideAllActivityView();
                _SignInActivityView.Show();
            }
        }
        public void TurntableActivityToggleEvent(bool isOn)
        {
            if (isOn)
            {
                HideAllActivityView();
                _TurntableActivityView.Show();
            }
        }
    }
}
