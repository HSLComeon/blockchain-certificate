package com.certificate.vo.log;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class LogQueryVO implements Serializable {
    private String type;
    private String operator;
    private Date startDate;
    private Date endDate;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}