package com.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@TableName("certificate_type")
public class CertificateType implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 证书类型名称
     */
    private String name;

    /**
     * 证书类型描述
     */
    private String description;

    /**
     * 标识颜色
     */
    private String color;

    /**
     * 证书属性，JSON格式保存
     */
    private String attributes;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 使用次数
     */
    private Integer usageCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 解析后的属性列表，非数据库字段
     */
    // 方案1：直接删除
// @TableField(exist = false)
// private List<String> attributeList;

// 方案2：如果业务需要用到但不想返回给前端
    @TableField(exist = false)
    @JsonIgnore
    private List<String> attributeList;
}