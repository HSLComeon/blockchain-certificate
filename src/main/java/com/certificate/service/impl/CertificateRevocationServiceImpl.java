// blockchain-certificate/src/main/java/com/certificate/service/impl/CertificateRevocationServiceImpl.java
package com.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.entity.CertificateRevocation;
import com.certificate.entity.User;
import com.certificate.mapper.CertificateRevocationMapper;
import com.certificate.service.CertificateRevocationService;
import com.certificate.service.UserService;
import com.certificate.vo.certificate.CertificateRevocationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CertificateRevocationServiceImpl extends ServiceImpl<CertificateRevocationMapper, CertificateRevocation>
        implements CertificateRevocationService {

    @Autowired
    private UserService userService;

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
    public boolean reviewRevocation(Long id, Integer status, String rejectReason, Long reviewerId) {
        CertificateRevocation revocation = getById(id);
        if (revocation == null) throw new RuntimeException("注销申请不存在");
        revocation.setStatus(status);
        revocation.setReviewTime(new Date());
        revocation.setReviewerId(reviewerId);
        revocation.setRejectReason(rejectReason);
        // 审核通过后可在此处自动撤销证书并上链
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
}