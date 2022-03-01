package com.bilibili.widget.danmu.live.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by czp on 17-5-24.
 */
public class SendGiftEntity extends JSONEntity {
    public SendGiftEntityData data;

    public class SendGiftEntityData {
        public String giftName;
        public Integer num;
        public String uname;
        public Integer rcost;
        public Integer uid;
        /**
         * The 'top_list' can be a JSONArray or Boolean.
         */
        //top_list 可能是 JSONArray 也可能是 布尔类型, 为 JSONArray 时内部元素不明
        public Object top_list;
        /**
         * Get Unix timestamp, it is not Java timestamp!
         */
        public Long timestamp;  //Unix时间戳
        public Integer giftId;
        public Integer giftType;
        public String action;
        /**
         * The key of this field in JSON is 'super'. This word conflict with Java keyword, use superI instead.
         */
        //json中是super, 与java关键字冲突
        @JSONField(name = "super")
        public Integer superI;
        public Integer price;
        public String rnd;
        public Integer newMedal;
        public Integer newTitle;
        /**
         * The 'medal' can be a Integer or a JSONObject.
         * <p>When it is a JSONObject, It contains data below:
         * <pre>
         *  "medal": {
         *      "medalId": 10,
         *      "medalName": "猛男",
         *      "level": 1
         *  }
         * </pre>
         * Get data manual:
         * <pre>
         *     JSONObject medal = (JSONObject)sendGiftEntityData.data.medal;
         *     Integer medalId = medal.getInteger("medalId");
         *     String medalName = medal.getString("medalName");
         *     Integer level = medal.getInteger("level");
         * </pre>
         */
        //medal 可能是数字也可能是 JSONObject. 为 JSONObject 时, 包含三个字段 medalId(Integer), medalName(String), level(Integer)
        public Object medal;
        public String title;
        public String beatId;
        public String gold;
        public Integer silver;
        public JSONArray notice_msg;
        public SendGiftEntityDataCapsule capsule;

        public class SendGiftEntityDataCapsule {
            public SendGiftEntityDataCapsuleColor normal;
            public SendGiftEntityDataCapsuleColor colorful;

            public class SendGiftEntityDataCapsuleColor {
                public Integer coin;
                public Integer change;
                public SendGiftEntityDataCapsuleColorfulProgress progress;

                public class SendGiftEntityDataCapsuleColorfulProgress {
                    public Integer now;
                    public Integer max;
                }
            }
        }
    }
}
