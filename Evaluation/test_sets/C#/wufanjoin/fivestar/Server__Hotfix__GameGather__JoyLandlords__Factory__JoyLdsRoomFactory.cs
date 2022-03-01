using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using ETModel;

namespace ETHotfix
{
  public static class JoyLdsRoomFactory
    {
        public  static async Task<JoyLdsRoom> Create(M2S_StartGame m2SStartGame)
        {
            JoyLdsRoom joyLdsRoom = ComponentFactory.Create<JoyLdsRoom>();
            joyLdsRoom.BesansLowest = m2SStartGame.RoomConfig.BesansLowest;
            joyLdsRoom.BaseMultiple = m2SStartGame.RoomConfig.BaseScore;
            foreach (var matchPlayer in m2SStartGame.MatchPlayerInfos)
            {
                JoyLdsPlayer joyLdsPlayer =await JoyLdsPlayerFactory.Create(matchPlayer, joyLdsRoom);
                joyLdsRoom.pJoyLdsPlayerDic.Add(joyLdsPlayer.pSeatIndex, joyLdsPlayer); 
            }
            return joyLdsRoom;
        }
    }
}
