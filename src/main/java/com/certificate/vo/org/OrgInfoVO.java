package com.certificate.vo.org;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class OrgInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String orgName;

    private String orgCode;

    private String address;

    private String contactPerson;

    private String contactPhone;

    private String email;

    private String description;

    private Integer status;

    private Date createTime;

    private String token;

    private Integer certificates;
    private Integer users;
    private Integer templates;
    private String lastActive;
    // 新增：最近发放的证书
    private List<Map<String, Object>> recentCertificates; // 新增字段
}