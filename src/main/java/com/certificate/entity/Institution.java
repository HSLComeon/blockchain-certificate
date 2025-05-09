package com.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("institution")
public class Institution {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 登录用户名
    private String username;

    // 登录密码
    private String password;

    // 机构名称
    private String institutionName;

    // 机构编码
    private String code;

    // 机构地址
    private String address;

    // 联系人
    private String contact;

    // 联系电话
    private String phone;

    // 机构描述
    private String description;

    // 状态 0:待审核 1:已通过 2:已拒绝
    private Integer status;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;
}