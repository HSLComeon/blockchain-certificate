package com.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("admin")
public class Admin {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private Integer role;  // 1:系统管理员 2:机构管理员
    private Integer status; // 0:待审核 1:已通过 2:已拒绝
    private Long institutionId;
    private Date createTime;
    private Date updateTime;
}