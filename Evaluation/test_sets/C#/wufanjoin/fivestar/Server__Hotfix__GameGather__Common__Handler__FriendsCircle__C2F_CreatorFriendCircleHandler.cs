using System;
using System.Collections.Generic;
using System.Text;
using ETModel;

namespace ETHotfix
{
    /// <summary>
    /// 创建亲友圈
    /// </summary>
    [MessageHandler(AppType.FriendsCircle)]
    public class C2F_CreatorFriendCircleHandler : AMRpcHandler<C2F_CreatorFriendCircle, F2C_CreatorFriendCircle>
    {
        protected override async void Run(Session session, C2F_CreatorFriendCircle message, Action<F2C_CreatorFriendCircle> reply)
        {
            F2C_CreatorFriendCircle response = new F2C_CreatorFriendCircle();
            try
            {
                response.FriendsCircle=await FriendsCircleComponent.Ins.CreatorFriendsCircle(message.UserId, message.Name, message.Announcement,message.WanFaCofigs, message.ToyGameId, response);
                reply(response);
            }
            catch (Exception e)
            {
                ReplyError(response, e, reply);
            }
        }
    }
}
