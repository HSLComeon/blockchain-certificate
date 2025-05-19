// blockchain-certificate/src/main/java/com/certificate/service/BlockchainApplicationService.java
package com.certificate.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.certificate.entity.CertificateBlockchainApplication;
import com.certificate.vo.blockchain.BlockchainApplicationVO;

public interface BlockchainApplicationService extends IService<CertificateBlockchainApplication> {

    /**
     * 创建上链申请
     * @param application 申请信息
     * @return 是否成功
     */
    boolean createApplication(CertificateBlockchainApplication application);

    /**
     * 获取上链申请列表（管理员端）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param status 状态
     * @param keyword 关键字
     * @return 分页结果
     */
    IPage<BlockchainApplicationVO> getApplicationList(Integer pageNum, Integer pageSize, Integer status, String keyword);

    /**
     * 获取机构的上链申请列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param orgId 机构ID
     * @param status 状态
     * @param keyword 关键字
     * @return 分页结果
     */
    IPage<BlockchainApplicationVO> getOrgApplicationList(Integer pageNum, Integer pageSize, Long orgId, Integer status, String keyword);

    /**
     * 审核上链申请
     * @param id 申请ID
     * @param status 审核状态(1-通过,2-拒绝)
     * @param rejectReason 拒绝原因
     * @param reviewerId 审核人ID
     * @return 是否成功
     */
    boolean reviewApplication(Long id, Integer status, String rejectReason, Long reviewerId);

    /**
     * 取消上链申请
     * @param id 申请ID
     * @return 是否成功
     */
    boolean cancelApplication(Long id);

    /**
     * 获取申请详情
     * @param id 申请ID
     * @return 申请详情
     */
    BlockchainApplicationVO getApplicationDetail(Long id);
}