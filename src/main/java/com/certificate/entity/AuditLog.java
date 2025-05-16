package com.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("audit_log")
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 日志编号
     */
    private String logNo;

    /**
     * 日志类型：login-登录日志，operation-操作日志，system-系统日志，blockchain-区块链日志
     */
    private String type;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 操作时间
     */
    private Date operationTime;

    /**
     * 操作状态：success-成功，failed-失败
     */
    private String status;

    /**
     * 详细内容
     */
    private String content;

    private Date createTime;
}