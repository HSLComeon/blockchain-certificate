// blockchain-certificate/src/main/java/com/certificate/service/CertificateApplicationService.java
package com.certificate.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.certificate.entity.CertificateApplication;
import com.certificate.vo.certificate.CertificateApplicationVO;

public interface CertificateApplicationService extends IService<CertificateApplication> {
    IPage<CertificateApplicationVO> getApplicationList(Integer status, String keyword, Integer pageNum, Integer pageSize);
    boolean reviewApplication(Long id, Integer status, String rejectReason, Long reviewerId);
}