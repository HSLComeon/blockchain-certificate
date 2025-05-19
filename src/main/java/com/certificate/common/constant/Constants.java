// blockchain-certificate/src/main/java/com/certificate/common/constant/Constants.java
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

    /**
     * 机构状态
     */
    public interface OrganizationStatus {
        /**
         * 待审核
         */
        int PENDING = 0;

        /**
         * 已通过
         */
        int APPROVED = 1;

        /**
         * 已拒绝
         */
        int REJECTED = 2;

        /**
         * 已禁用
         */
        int DISABLED = 3;
    }

    /**
     * 证书类型状态
     */
    public interface CertificateTypeStatus {
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
     * 证书状态
     */
    public static class CertificateStatus {
        /**
         * 待上链
         */
        public static final int PENDING = 0;

        /**
         * 已上链
         */
        public static final int ON_CHAIN = 1;

        /**
         * 已撤销
         */
        public static final int REVOKED = 2;
    }

    /**
     * 申请状态常量
     */
    public static class ApplicationStatus {
        /**
         * 待审核
         */
        public static final int PENDING = 0;

        /**
         * 已批准
         */
        public static final int APPROVED = 1;

        /**
         * 已拒绝
         */
        public static final int REJECTED = 2;

        /**
         * 已取消
         */
        public static final int CANCELED = 3;
    }

    /**
     * 注销状态常量
     */
    public static class RevocationStatus {
        /**
         * 待审核
         */
        public static final int PENDING = 0;

        /**
         * 已批准
         */
        public static final int APPROVED = 1;

        /**
         * 已拒绝
         */
        public static final int REJECTED = 2;

        /**
         * 已取消
         */
        public static final int CANCELED = 3;
    }
}