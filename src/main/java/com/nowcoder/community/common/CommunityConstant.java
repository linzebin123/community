package com.nowcoder.community.common;

public class CommunityConstant {
    /**
     * 激活成功
     */
    public static final int ACTIVATION_SUCCESS=0;
    /**
     * 重复激活
     */
    public static final int ACTIVATION_REPEAT=1;
    /**
     * 激活失败
     */
    public static final int ACTIVATION_FAIL=2;

    /**
     * 默认状态登陆凭证存留时间
     */
    public static final int DEFAULT_EXPIRED_SECONDS=3600*12;
    /**
     * 记住状态下正路凭证存留时间
     */
    public static final int REMEMBER_EXPIRED_SECONDS=3600*24*100;

    /**
     * 实体类型：帖子
     */
    public static final int ENTITY_TYPE_POST=1;

    /**
     * 实体类型：评论
     */
    public static final int ENTITY_TYPE_COMMENT=2;

    /**
     * 实体类型：用户
     */
    public static final int ENTITY_TYPE_USER=3;

    /**
     * 主题：评论
     */
    public static final String TOPIC_COMMENT="comment";

    /**
     * 主题：点赞
     */
    public static final String TOPIC_LIKE="like";

    /**
     * 主题：关注
     */
    public static final String TOPIC_FOLLOW="follow";

    /**
     * 系统用户id
     */
    public static final int SYSTEM_USER_ID=1;
}
