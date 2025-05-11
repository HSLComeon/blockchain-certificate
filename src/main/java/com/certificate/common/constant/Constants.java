package com.certificate.common.constant;

public class Constants {

    /**
     * 用户状态
     */
    public interface UserStatus {
        /**
         * 禁用
         */
        int DISABLED = 0;

        /**
         * 启用
         */
        int ENABLED = 1;
    }

    /**
     * 登录类型
     */
    public interface LoginType {
        /**
         * 用户
         */
        String USER = "user";

        /**
         * 机构
         */
        String ORG = "org";

        /**
         * 管理员
         */
        String ADMIN = "admin";
    }
}