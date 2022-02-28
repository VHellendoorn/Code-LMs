using System;
using DG.Tweening;
using ETModel;
using UnityEngine;
using UnityEngine.UI;

namespace ETHotfix
{
    public class FriendCircleTopView : BaseView
    {
        #region 脚本工具生成的代码
        private Text mNameAndIdText;
        private Text mOriginatorText;
        private Text mNumberText;
        private Text mJewelNumText;
        private Button mAddJewelBtn;
        private Button mMoreBtn;
        private GameObject mMoreFunctionGo;
        private Button mCutBtn;
        private Button mJoinBtn;
        private Button mCreateBtn;
        private Button mOutBtn;
        private Button mReturnBtn;
        public override void Init(GameObject go)
        {
            base.Init(go);
            mNameAndIdText = rc.Get<GameObject>("NameAndIdText").GetComponent<Text>();
            mOriginatorText = rc.Get<GameObject>("OriginatorText").GetComponent<Text>();
            mNumberText = rc.Get<GameObject>("NumberText").GetComponent<Text>();
            mJewelNumText = rc.Get<GameObject>("JewelNumText").GetComponent<Text>();
            mAddJewelBtn = rc.Get<GameObject>("AddJewelBtn").GetComponent<Button>();
            mMoreBtn = rc.Get<GameObject>("MoreBtn").GetComponent<Button>();
            mMoreFunctionGo = rc.Get<GameObject>("MoreFunctionGo");
            mCutBtn = rc.Get<GameObject>("CutBtn").GetComponent<Button>();
            mJoinBtn = rc.Get<GameObject>("JoinBtn").GetComponent<Button>();
            mCreateBtn = rc.Get<GameObject>("CreateBtn").GetComponent<Button>();
            mOutBtn = rc.Get<GameObject>("OutBtn").GetComponent<Button>();
            mReturnBtn = rc.Get<GameObject>("ReturnBtn").GetComponent<Button>();
            InitPanel();
        }
        #endregion
        public void InitPanel()
        {
            mAddJewelBtn.Add(AddJewelBtnEvent);
            mMoreBtn.Add(MoreBtnEvent,false);
            mReturnBtn.Add(ReturnBtnEvent);

            mCutBtn.Add(CutBtnEvent);
            mJoinBtn.Add(JoinBtnEvent);
            mCreateBtn.Add(CreateBtnEvent);
            mOutBtn.Add(OutBtnEvent);
        }
        public void ShowFriendCircleInfo()
        {
            Show();
            mNameAndIdText.text = FrienCircleComponet.Ins.CuurSelectFriendsCircle.Name +
                                    $"(ID:{FrienCircleComponet.Ins.CuurSelectFriendsCircle.FriendsCircleId})";
            mOriginatorText.text = FrienCircleComponet.Ins.CreateUser.Name;
            mNumberText.text = FrienCircleComponet.Ins.CuurSelectFriendsCircle.TotalNumber.ToString();
            mJewelNumText.text = FrienCircleComponet.Ins.CreateUser.Jewel.ToString();
            mAddJewelBtn.gameObject.SetActive(FrienCircleComponet.Ins.CuurSelectFriendsCircle.ManageUserIds.Contains(UserComponent.Ins.pUserId));
        }


        public void CutBtnEvent()
        {
            UIComponent.GetUiView<JoinFriendCiclePanelComponent>().ShowPanel(JoinFrienPanelShowType.Cut);
        }
        public void JoinBtnEvent()
        {
            UIComponent.GetUiView<JoinFriendCiclePanelComponent>().ShowPanel(JoinFrienPanelShowType.Join);
        }
        public void CreateBtnEvent()
        {
            UIComponent.GetUiView<CreateFriendCiclePanelComponent>().Show();
        }
        public void OutBtnEvent()
        {
            UIComponent.GetUiView<PopUpHintPanelComponent>().ShowOptionWindow("是否退出该亲友圈？", OutFriendCircleAction);
        }

        private async void OutFriendCircleAction(bool isConirm)
        {
            if (isConirm)
            {
                F2C_OutFriendsCircle friendsCircle =
                    (F2C_OutFriendsCircle)await SessionComponent.Instance.Call(new C2F_OutFriendsCircle()
                    {
                        FriendsCrircleId = FrienCircleComponet.Ins.CuurSelectFriendsCircle.FriendsCircleId
                    });
                if (!string.IsNullOrEmpty(friendsCircle.Message))
                {
                    UIComponent.GetUiView<NormalHintPanelComponent>().ShowHintPanel(friendsCircle.Message);
                }
                else
                {
                    FrienCircleComponet.Ins.OutFrienCircle(FrienCircleComponet.Ins.CuurSelectFriendsCircle
                        .FriendsCircleId);
                }
            }
        }
        public void MoreBtnEvent()
        {
            mMoreFunctionGo.transform.DOLocalMoveY(40,0.5f);
        }
        public void ReturnBtnEvent()
        {
            mMoreFunctionGo.transform.DOLocalMoveY(360, 0.5f);
        }
        public void AddJewelBtnEvent()
        {
            UIComponent.GetUiView<ShopPanelComponent>().ShowGoodsList(GoodsId.Jewel, UIType.FriendCirclePanel);
        }
    }
}
