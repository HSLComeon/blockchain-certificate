// blockchain-certificate/src/main/java/com/certificate/service/CertificateApplicationService.java
package com.certificate.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.certificate.entity.CertificateApplication;
import com.certificate.vo.certificate.CertificateApplicationVO;

import java.util.List;
import java.util.Map;

public interface CertificateApplicationService extends IService<CertificateApplication> {
    /**
     * 获取申请列表
     * @param status 状态（可选）
     * @param keyword 关键字（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<CertificateApplicationVO> getApplicationList(Integer status, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 审核申请
     * @param id 申请ID
     * @param status 状态
     * @param rejectReason 拒绝原因
     * @param reviewerId 审核人ID
     * @return 是否成功
     */
    boolean reviewApplication(Long id, Integer status, String rejectReason, Long reviewerId);

    /**
     * 创建申请
     * @param application 申请实体
     * @return 是否成功
     */
    boolean createApplication(CertificateApplication application);

    /**
     * 取消申请
     * @param id 申请ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean cancelApplication(Long id, Long userId);

    /**
     * 获取机构申请列表
     * @param orgId 机构ID
     * @param status 状态（可选）
     * @param keyword 关键字（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<CertificateApplicationVO> getOrgApplicationList(Long orgId, Integer status, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 根据机构ID统计申请数量
     * @param orgId 机构ID
     * @return 申请数量
     */
    int countByOrgId(Long orgId);

    /**
     * 根据机构ID和状态统计申请数量
     * @param orgId 机构ID
     * @param status 状态
     * @return 申请数量
     */
    int countByOrgIdAndStatus(Long orgId, Integer status);

    /**
     * 获取机构最近的证书申请
     * @param orgId 机构ID
     * @param limit 数量限制
     * @return 申请列表
     */
    List<Map<String, Object>> getRecentOrgApplications(Long orgId, int limit);
}