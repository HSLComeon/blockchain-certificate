// blockchain-certificate/src/main/java/com/certificate/service/CertificateService.java
package com.certificate.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.certificate.entity.Certificate;
import com.certificate.vo.certificate.*;

import java.util.List;

public interface CertificateService extends IService<Certificate> {

    /**
     * 创建证书
     * @param createVO 创建VO
     * @param orgId 机构ID
     * @return 证书VO
     */
    CertificateVO createCertificate(CertificateCreateVO createVO, Long orgId);

    /**
     * 获取证书列表
     * @param orgId 机构ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键字（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<CertificateVO> getCertificateList(Long orgId, Integer status, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 获取证书详情
     * @param id 证书ID
     * @return 证书VO
     */
    CertificateVO getCertificateDetail(Long id);

    /**
     * 将证书上链
     * @param id 证书ID
     * @return 证书VO
     */
    CertificateVO uploadToBlockchain(Long id);

    /**
     * 批量上链
     * @param ids 证书ID列表
     * @return 成功上链的数量
     */
    int batchUploadToBlockchain(List<Long> ids);

    /**
     * 撤销证书
     * @param revokeVO 撤销VO
     * @return 是否成功
     */
    boolean revokeCertificate(CertificateRevokeVO revokeVO);

    /**
     * 验证证书
     * @param verifyVO 验证VO
     * @return 验证结果
     */
    CertificateVerifyResultVO verifyCertificate(CertificateVerifyVO verifyVO);

    /**
     * 根据证书编号查询证书
     * @param certNo 证书编号
     * @return 证书VO
     */
    CertificateVO getCertificateByCertNo(String certNo);

    /**
     * 生成二维码（包含证书编号和哈希）
     * @param id 证书ID
     * @return Base64编码的二维码图片
     */
    String generateQRCode(Long id);

    /**
     * 统计证书数量
     * @param orgId 机构ID（可选）
     * @return 证书数量
     */
    int countCertificates(Long orgId);

    /**
     * 根据机构ID统计证书数量
     * @param orgId 机构ID
     * @return 证书数量
     */
    int countByOrgId(Long orgId);

    /**
     * 根据机构ID和状态统计证书数量
     * @param orgId 机构ID
     * @param status 状态
     * @return 证书数量
     */
    int countByOrgIdAndStatus(Long orgId, Integer status);

    /**
     * 根据机构ID获取该机构的所有证书ID
     * @param orgId 机构ID
     * @return 证书ID列表
     */
    List<Long> getCertificateIdsByOrgId(Long orgId);
}