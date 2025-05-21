// blockchain-certificate/src/main/java/com/certificate/service/CertificateRevocationService.java
package com.certificate.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.certificate.entity.CertificateRevocation;
import com.certificate.vo.certificate.CertificateRevocationVO;

import java.util.List;
import java.util.Map;

public interface CertificateRevocationService extends IService<CertificateRevocation> {

    IPage<CertificateRevocationVO> getRevocationList(Integer status, String keyword, Integer pageNum, Integer pageSize);

    boolean reviewRevocation(Long id, Integer status, String rejectReason, Long reviewerId);

    boolean createRevocation(CertificateRevocation revocation);

    boolean cancelRevocation(Long id, Long userId);

    IPage<CertificateRevocationVO> getOrgRevocationList(Long orgId, Integer status, String keyword, Integer pageNum, Integer pageSize);

    int countByOrgId(Long orgId);

    int countByOrgIdAndStatus(Long orgId, Integer status);

    List<Map<String, Object>> getRecentOrgRevocations(Long orgId, int limit);

    /**
     * 检查证书是否有活跃的注销申请
     * @param certificateId 证书ID
     * @return 是否有活跃的注销申请
     */
    boolean hasActiveRevocation(Long certificateId);

    // CertificateRevocationService.java
    CertificateRevocationVO getRevocationDetailVO(Long id);
}