package com.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("organization")
public class Organization implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String orgName;

    private String orgCode;

    private String address;

    private String contactPerson;

    private String contactPhone;

    private String email;

    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    private Date createTime;

    private Date updateTime;
}