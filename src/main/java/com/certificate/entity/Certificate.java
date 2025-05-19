package com.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("certificate")
public class Certificate {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("certificate_no")
    private String certificateNo;

    private Long certificateTypeId;

    private Long orgId;

    private Long userId;

    private String name;  // 证书标题

    private Date issueDate;

    private Date validFromDate;

    private Date expireDate;

    private String txHash;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private Long templateId;

    private Date chainTime;

    // 这些字段在数据库中不存在，标记为非数据库字段
    @TableField(exist = false)
    private String content;

    @TableField(exist = false)
    private String hash;

    @TableField(exist = false)
    private String holderName;

    @TableField(exist = false)
    private String holderIdCard;

    // 非数据库字段，但在VO中使用
    @TableField(exist = false)
    private String blockchainTxHash;

    @TableField(exist = false)
    private String statusText;

    @TableField(exist = false)
    private String validToDate;
}