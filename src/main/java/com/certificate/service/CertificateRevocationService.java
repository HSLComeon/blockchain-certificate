// blockchain-certificate/src/main/java/com/certificate/service/CertificateRevocationService.java
package com.certificate.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.certificate.entity.CertificateRevocation;
import com.certificate.vo.certificate.CertificateRevocationVO;

public interface CertificateRevocationService extends IService<CertificateRevocation> {
    IPage<CertificateRevocationVO> getRevocationList(Integer status, String keyword, Integer pageNum, Integer pageSize);
    boolean reviewRevocation(Long id, Integer status, String rejectReason, Long reviewerId);
}