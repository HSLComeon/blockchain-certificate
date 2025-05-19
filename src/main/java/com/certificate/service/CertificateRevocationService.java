// blockchain-certificate/src/main/java/com/certificate/service/CertificateRevocationService.java
package com.certificate.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.certificate.entity.CertificateRevocation;
import com.certificate.vo.certificate.CertificateRevocationVO;

import java.util.List;
import java.util.Map;

public interface CertificateRevocationService extends IService<CertificateRevocation> {
    /**
     * 获取注销申请列表
     * @param status 状态（可选）
     * @param keyword 关键字（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<CertificateRevocationVO> getRevocationList(Integer status, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 审核注销申请
     * @param id 申请ID
     * @param status 状态
     * @param rejectReason 拒绝原因
     * @param reviewerId 审核人ID
     * @return 是否成功
     */
    boolean reviewRevocation(Long id, Integer status, String rejectReason, Long reviewerId);

    /**
     * 创建注销申请
     * @param revocation 注销申请实体
     * @return 是否成功
     */
    boolean createRevocation(CertificateRevocation revocation);

    /**
     * 取消注销申请
     * @param id 申请ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean cancelRevocation(Long id, Long userId);

    /**
     * 获取机构注销申请列表
     * @param orgId 机构ID
     * @param status 状态（可选）
     * @param keyword 关键字（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<CertificateRevocationVO> getOrgRevocationList(Long orgId, Integer status, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 根据机构ID统计注销申请数量
     * @param orgId 机构ID
     * @return 注销申请数量
     */
    int countByOrgId(Long orgId);

    /**
     * 根据机构ID和状态统计注销申请数量
     * @param orgId 机构ID
     * @param status 状态
     * @return 注销申请数量
     */
    int countByOrgIdAndStatus(Long orgId, Integer status);

    /**
     * 获取机构最近的证书注销申请
     * @param orgId 机构ID
     * @param limit 数量限制
     * @return 注销申请列表
     */
    List<Map<String, Object>> getRecentOrgRevocations(Long orgId, int limit);
}