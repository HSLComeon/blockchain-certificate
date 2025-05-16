package com.certificate.vo.log;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class LogVO implements Serializable {
    private Long id;
    private String logNo;
    private String type;
    private String description;
    private String operator;
    private String ipAddress;
    private Date operationTime;
    private String status;
    private String content;
}