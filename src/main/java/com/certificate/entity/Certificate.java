package com.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
     * 证书编号（唯一）
     */
    private String certNo;

    /**
     * 证书类型ID
     */
    private Long typeId;

    /**
     * 发证机构ID
     */
    private Long orgId;

    /**
     * 持有人用户ID
     */
    private Long userId;

    /**
     * 持有人姓名
     */
    private String holderName;

    /**
     * 持有人身份证号
     */
    private String holderIdCard;

    /**
     * 证书标题
     */
    private String title;

    /**
     * 证书内容（JSON格式，包含证书属性和值）
     */
    private String content;

    /**
     * 发证日期
     */
    private Date issueDate;

    /**
     * 有效期开始日期
     */
    private Date validFromDate;

    /**
     * 有效期结束日期（null表示永久有效）
     */
    private Date validToDate;

    /**
     * 区块链交易哈希（上链后的哈希值）
     */
    private String blockchainTxHash;

    /**
     * 证书状态：0-待上链，1-已上链，2-已撤销
     */
    private Integer status;

    /**
     * 证书哈希值（用于验证）
     */
    private String hash;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}