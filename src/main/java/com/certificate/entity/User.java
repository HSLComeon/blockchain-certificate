package com.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String idCard;
    private String phone;
    private String email;
    private Integer role;  // 1:系统管理员 2:机构管理员 3:普通用户
    private Integer status; // 0:禁用 1:启用
    private Long institutionId;
    private Date createTime;
    private Date updateTime;
}