// blockchain-certificate/src/main/java/com/certificate/service/impl/BlockchainApplicationServiceImpl.java
package com.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.common.constant.Constants;
import com.certificate.entity.Admin;
import com.certificate.entity.Certificate;
import com.certificate.entity.CertificateBlockchainApplication;
import com.certificate.entity.Organization;
import com.certificate.mapper.CertificateBlockchainApplicationMapper;
import com.certificate.service.AdminService;
import com.certificate.service.BlockchainApplicationService;
import com.certificate.service.BlockchainService;
import com.certificate.service.CertificateService;
import com.certificate.service.OrganizationService;
import com.certificate.util.SnowflakeIdGenerator;
import com.certificate.vo.blockchain.BlockchainApplicationVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BlockchainApplicationServiceImpl extends ServiceImpl<CertificateBlockchainApplicationMapper, CertificateBlockchainApplication> implements BlockchainApplicationService {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private AdminService adminService;

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createApplication(CertificateBlockchainApplication application) {
        // 检查证书是否存在
        Certificate certificate = certificateService.getById(application.getCertificateId());
        if (certificate == null) {
            throw new RuntimeException("证书不存在");
        }

        // 检查证书状态是否允许上链 (0:待上链, 1:已上链, 2:已撤销)
        if (certificate.getStatus() == Constants.CertificateStatus.ON_CHAIN) {
            throw new RuntimeException("证书已上链，无需再次申请");
        }
        if (certificate.getStatus() == Constants.CertificateStatus.REVOKED) {
            throw new RuntimeException("证书已撤销，不能申请上链");
        }

        // 检查是否已经有待审核的上链申请
        LambdaQueryWrapper<CertificateBlockchainApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CertificateBlockchainApplication::getCertificateId, application.getCertificateId())
                .eq(CertificateBlockchainApplication::getStatus, 0);
        if (this.count(queryWrapper) > 0) {
            throw new RuntimeException("该证书已有待审核的上链申请");
        }

        // 生成申请编号
        application.setApplicationNo("BC" + idGenerator.nextId());
        application.setStatus(0); // 0-待审核
        application.setApplyTime(LocalDateTime.now());

        return this.save(application);
    }

    @Override
    public IPage<BlockchainApplicationVO> getApplicationList(Integer pageNum, Integer pageSize, Integer status, String keyword) {
        // 分页查询申请记录
        Page<CertificateBlockchainApplication> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CertificateBlockchainApplication> queryWrapper = new LambdaQueryWrapper<>();

        // 添加状态筛选条件
        if (status != null) {
            queryWrapper.eq(CertificateBlockchainApplication::getStatus, status);
        }

        // 添加关键字搜索条件
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(CertificateBlockchainApplication::getApplicationNo, keyword);
        }

        // 按申请时间降序排序
        queryWrapper.orderByDesc(CertificateBlockchainApplication::getApplyTime);

        // 执行查询
        Page<CertificateBlockchainApplication> resultPage = this.page(page, queryWrapper);

        // 转换为VO
        return convertToVOPage(resultPage);
    }

    @Override
    public IPage<BlockchainApplicationVO> getOrgApplicationList(Integer pageNum, Integer pageSize, Long orgId, Integer status, String keyword) {
        // 分页查询机构的申请记录
        Page<CertificateBlockchainApplication> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CertificateBlockchainApplication> queryWrapper = new LambdaQueryWrapper<>();

        // 添加机构ID筛选条件
        queryWrapper.eq(CertificateBlockchainApplication::getOrgId, orgId);

        // 添加状态筛选条件
        if (status != null) {
            queryWrapper.eq(CertificateBlockchainApplication::getStatus, status);
        }

        // 添加关键字搜索条件
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(CertificateBlockchainApplication::getApplicationNo, keyword);
        }

        // 按申请时间降序排序
        queryWrapper.orderByDesc(CertificateBlockchainApplication::getApplyTime);

        // 执行查询
        Page<CertificateBlockchainApplication> resultPage = this.page(page, queryWrapper);

        // 转换为VO
        return convertToVOPage(resultPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewApplication(Long id, Integer status, String rejectReason, Long reviewerId) {
        // 获取申请记录
        CertificateBlockchainApplication application = this.getById(id);
        if (application == null) {
            throw new RuntimeException("申请记录不存在");
        }

        // 检查状态是否为待审核
        if (application.getStatus() != 0) {
            throw new RuntimeException("申请已审核，不能重复审核");
        }

        // 更新申请状态
        application.setStatus(status); // 1-通过，2-拒绝
        application.setReviewTime(LocalDateTime.now());
        application.setReviewerId(reviewerId);

        if (status == 2) {
            // 拒绝申请
            if (StringUtils.isBlank(rejectReason)) {
                throw new RuntimeException("拒绝原因不能为空");
            }
            application.setRejectReason(rejectReason);
            return this.updateById(application);
        } else if (status == 1) {
            // 通过申请，执行上链操作
            Certificate certificate = certificateService.getById(application.getCertificateId());
            if (certificate == null) {
                throw new RuntimeException("证书不存在");
            }

            // 调用区块链服务上链
            try {
                String txHash = blockchainService.uploadToBlockchain(certificate);
                application.setTxHash(txHash);

                // 更新证书状态为已上链
                certificate.setStatus(Constants.CertificateStatus.ON_CHAIN); // 已上链
                certificate.setBlockchainTxHash(txHash);
                certificate.setChainTime(new Date());
                certificate.setUpdateTime(new Date());
                certificateService.updateById(certificate);

                return this.updateById(application);
            } catch (Exception e) {
                log.error("证书上链失败", e);
                throw new RuntimeException("证书上链失败: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("无效的审核状态");
        }
    }

    @Override
    public boolean cancelApplication(Long id) {
        // 获取申请记录
        CertificateBlockchainApplication application = this.getById(id);
        if (application == null) {
            throw new RuntimeException("申请记录不存在");
        }

        // 检查状态是否为待审核
        if (application.getStatus() != 0) {
            throw new RuntimeException("申请已审核，不能取消");
        }

        // 更新状态为已取消
        application.setStatus(3); // 3-已取消
        return this.updateById(application);
    }

    @Override
    public BlockchainApplicationVO getApplicationDetail(Long id) {
        // 获取申请记录
        CertificateBlockchainApplication application = this.getById(id);
        if (application == null) {
            return null;
        }

        // 转换为VO
        return convertToVO(application);
    }

    // 将实体转换为VO
    private BlockchainApplicationVO convertToVO(CertificateBlockchainApplication application) {
        BlockchainApplicationVO vo = new BlockchainApplicationVO();
        BeanUtils.copyProperties(application, vo);

        // 设置状态名称
        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(0, "待审核");
        statusMap.put(1, "已通过");
        statusMap.put(2, "已拒绝");
        statusMap.put(3, "已取消");
        vo.setStatusName(statusMap.getOrDefault(application.getStatus(), "未知"));

        // 设置证书信息
        Certificate certificate = certificateService.getById(application.getCertificateId());
        if (certificate != null) {
            vo.setCertificateNo(certificate.getCertificateNo());
        }

        // 设置机构名称
        Organization org = organizationService.getById(application.getOrgId());
        if (org != null) {
            vo.setOrgName(org.getOrgName());
        }

        // 设置审核人信息
        if (application.getReviewerId() != null) {
            Admin admin = adminService.getById(application.getReviewerId());
            if (admin != null) {
                vo.setReviewerName(admin.getUsername());
            }
        }

        return vo;
    }

    // 将分页结果转换为VO分页
    private IPage<BlockchainApplicationVO> convertToVOPage(Page<CertificateBlockchainApplication> page) {
        Page<BlockchainApplicationVO> voPage = new Page<>();
        voPage.setCurrent(page.getCurrent());
        voPage.setSize(page.getSize());
        voPage.setTotal(page.getTotal());

        List<BlockchainApplicationVO> records = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(records);

        return voPage;
    }
}