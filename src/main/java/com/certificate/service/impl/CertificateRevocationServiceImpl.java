// blockchain-certificate/src/main/java/com/certificate/service/impl/CertificateRevocationServiceImpl.java
package com.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.common.constant.Constants;
import com.certificate.entity.Certificate;
import com.certificate.entity.CertificateRevocation;
import com.certificate.entity.User;
import com.certificate.mapper.CertificateRevocationMapper;
import com.certificate.service.BlockchainService;
import com.certificate.service.CertificateRevocationService;
import com.certificate.service.CertificateService;
import com.certificate.service.UserService;
import com.certificate.vo.certificate.CertificateRevocationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class CertificateRevocationServiceImpl extends ServiceImpl<CertificateRevocationMapper, CertificateRevocation>
        implements CertificateRevocationService {

    @Autowired
    private UserService userService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private BlockchainService blockchainService;

    @Override
    public IPage<CertificateRevocationVO> getRevocationList(Integer status, String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<CertificateRevocation> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(CertificateRevocation::getStatus, status);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(CertificateRevocation::getRevocationNo, keyword)
                    .or().like(CertificateRevocation::getReason, keyword));
        }
        wrapper.orderByDesc(CertificateRevocation::getApplyTime);
        Page<CertificateRevocation> page = new Page<>(pageNum, pageSize);
        IPage<CertificateRevocation> result = page(page, wrapper);
        return result.convert(this::convertToVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewRevocation(Long id, Integer status, String rejectReason, Long reviewerId) {
        CertificateRevocation revocation = getById(id);
        if (revocation == null) throw new RuntimeException("注销申请不存在");

        // 先检查证书状态
        Certificate certificate = certificateService.getById(revocation.getCertificateId());
        if (certificate == null) throw new RuntimeException("证书不存在");

        if (certificate.getStatus() == Constants.CertificateStatus.REVOKED) {
            revocation.setStatus(Constants.RevocationStatus.APPROVED); // 自动批准
            revocation.setReviewTime(new Date());
            revocation.setReviewerId(reviewerId);
            revocation.setRevocationTime(new Date());
            return updateById(revocation);
        }

        revocation.setStatus(status);
        revocation.setReviewTime(new Date());
        revocation.setReviewerId(reviewerId);
        revocation.setRejectReason(rejectReason);

        // 审核通过后更新证书状态
        if (status == Constants.RevocationStatus.APPROVED) {
            // 如果证书已经上链，则调用区块链服务撤销
            if (certificate.getStatus() == Constants.CertificateStatus.ON_CHAIN) {
                try {
                    String txHash = blockchainService.revokeCertificate(certificate);
                    revocation.setTxHash(txHash);
                    certificate.setTxHash(txHash);  // 更新证书交易哈希
                } catch (Exception e) {
                    log.error("证书撤销上链失败: {}", e.getMessage());
                    // 即使上链失败，我们仍然会更新证书状态为已撤销
                }
            }

            // 更新证书状态为已撤销
            certificate.setStatus(Constants.CertificateStatus.REVOKED);
            certificate.setUpdateTime(new Date());
            certificateService.updateById(certificate);

            // 记录实际注销时间
            revocation.setRevocationTime(new Date());
        }

        return updateById(revocation);
    }

    private CertificateRevocationVO convertToVO(CertificateRevocation revocation) {
        CertificateRevocationVO vo = new CertificateRevocationVO();
        BeanUtils.copyProperties(revocation, vo);
        User user = userService.getById(revocation.getUserId());
        if (user != null) vo.setUserName(user.getUsername());
        vo.setStatusText(getStatusText(revocation.getStatus()));
        return vo;
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待审核";
            case 1: return "已批准";
            case 2: return "已拒绝";
            default: return "未知";
        }
    }

    @Override
    public boolean createRevocation(CertificateRevocation revocation) {
        revocation.setRevocationNo(generateRevocationNo());
        revocation.setStatus(Constants.RevocationStatus.PENDING);
        revocation.setApplyTime(new Date());
        return save(revocation);
    }

    @Override
    public boolean cancelRevocation(Long id, Long userId) {
        CertificateRevocation revocation = getById(id);
        if (revocation == null) {
            throw new RuntimeException("注销申请不存在");
        }

        if (!revocation.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此申请");
        }

        if (revocation.getStatus() != Constants.RevocationStatus.PENDING) {
            throw new RuntimeException("只能取消待审核的申请");
        }

        revocation.setStatus(Constants.RevocationStatus.CANCELED);

        return updateById(revocation);
    }

    @Override
    public IPage<CertificateRevocationVO> getOrgRevocationList(Long orgId, Integer status, String keyword, Integer pageNum, Integer pageSize) {
        // 获取机构的所有证书ID
        List<Long> certificateIds = certificateService.getCertificateIdsByOrgId(orgId);

        if (certificateIds.isEmpty()) {
            // 如果机构没有证书，则返回空结果
            return new Page<>(pageNum, pageSize);
        }

        LambdaQueryWrapper<CertificateRevocation> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CertificateRevocation::getCertificateId, certificateIds);

        if (status != null) {
            wrapper.eq(CertificateRevocation::getStatus, status);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(CertificateRevocation::getRevocationNo, keyword)
                    .or().like(CertificateRevocation::getReason, keyword));
        }

        wrapper.orderByDesc(CertificateRevocation::getApplyTime);
        Page<CertificateRevocation> page = new Page<>(pageNum, pageSize);
        IPage<CertificateRevocation> result = page(page, wrapper);

        return result.convert(this::convertToVO);
    }

    @Override
    public int countByOrgId(Long orgId) {
        // 获取机构的所有证书ID
        List<Long> certificateIds = certificateService.getCertificateIdsByOrgId(orgId);
        if (certificateIds.isEmpty()) {
            return 0;
        }

        LambdaQueryWrapper<CertificateRevocation> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CertificateRevocation::getCertificateId, certificateIds);
        return (int) count(wrapper);
    }

    @Override
    public int countByOrgIdAndStatus(Long orgId, Integer status) {
        // 获取机构的所有证书ID
        List<Long> certificateIds = certificateService.getCertificateIdsByOrgId(orgId);
        if (certificateIds.isEmpty()) {
            return 0;
        }

        LambdaQueryWrapper<CertificateRevocation> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CertificateRevocation::getCertificateId, certificateIds)
                .eq(CertificateRevocation::getStatus, status);
        return (int) count(wrapper);
    }

    @Override
    public List<Map<String, Object>> getRecentOrgRevocations(Long orgId, int limit) {
        // 获取机构的所有证书ID
        List<Long> certificateIds = certificateService.getCertificateIdsByOrgId(orgId);
        List<Map<String, Object>> result = new ArrayList<>();

        if (!certificateIds.isEmpty()) {
            LambdaQueryWrapper<CertificateRevocation> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(CertificateRevocation::getCertificateId, certificateIds)
                    .orderByDesc(CertificateRevocation::getApplyTime)
                    .last("LIMIT " + limit);

            List<CertificateRevocation> revocations = list(wrapper);

            for (CertificateRevocation rev : revocations) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rev.getId());
                map.put("revocationNo", rev.getRevocationNo());
                map.put("certificateId", rev.getCertificateId());
                map.put("status", rev.getStatus());
                map.put("statusText", getStatusText(rev.getStatus()));
                map.put("applyTime", rev.getApplyTime());

                // 获取用户信息
                User user = userService.getById(rev.getUserId());
                map.put("userName", user != null ? user.getUsername() : "未知用户");

                // 获取证书信息（简化处理，仅获取certNo作为标识）
                Certificate certificate = certificateService.getById(rev.getCertificateId());
                map.put("certNo", certificate != null ? certificate.getCertificateNo() : "未知证书");

                result.add(map);
            }
        }

        return result;
    }

    @Override
    public boolean hasActiveRevocation(Long certificateId) {
        LambdaQueryWrapper<CertificateRevocation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CertificateRevocation::getCertificateId, certificateId)
                .eq(CertificateRevocation::getStatus, Constants.RevocationStatus.PENDING);
        return count(wrapper) > 0;
    }

    // 辅助方法：生成注销申请编号
    private String generateRevocationNo() {
        return "REV" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

    // CertificateRevocationServiceImpl.java
    @Override
    public CertificateRevocationVO getRevocationDetailVO(Long id) {
        CertificateRevocation revocation = getById(id);
        if (revocation == null) return null;
        return convertToVO(revocation);
    }
}