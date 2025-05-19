package com.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("certificate")
public class Certificate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 证书编号
     */
    @TableField("certificate_no")
    private String certNo;

    /**
     * 证书类型ID
     */
    @TableField("certificate_type_id")
    private Long typeId;

    /**
     * 发证机构ID
     */
    @TableField("org_id")
    private Long orgId;

    /**
     * 持有人用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 持有人姓名 - 使用user表关联
     */
    @TableField(exist = false)
    private String holderName;

    /**
     * 持有人身份证号 - 使用user表关联
     */
    @TableField(exist = false)
    private String holderIdCard;

    /**
     * 证书标题
     */
    @TableField("name")
    private String title;

    /**
     * 证书内容（JSON格式，包含证书属性和值）
     */
    @TableField(exist = false)
    private String content;

    /**
     * 发证日期
     */
    @TableField("issue_date")
    private Date issueDate;

    /**
     * 有效期开始日期
     */
    @TableField("issue_date")
    private Date validFromDate;

    /**
     * 有效期结束日期（null表示永久有效）
     */
    @TableField("expire_date")
    private Date validToDate;

    /**
     * 区块链交易哈希（上链后的哈希值）
     */
    @TableField("tx_hash")
    private String blockchainTxHash;

    /**
     * 证书状态：0-待审核，1-已审核，2-已上链，3-已撤销
     */
    @TableField("status")
    private Integer status;

    /**
     * 证书哈希值（用于验证）
     */
    @TableField(exist = false)
    private String hash;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 模板ID
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 上链时间
     */
    @TableField("chain_time")
    private Date chainTime;
}