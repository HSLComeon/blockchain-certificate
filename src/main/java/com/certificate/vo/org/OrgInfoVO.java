package com.certificate.vo.org;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

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
}