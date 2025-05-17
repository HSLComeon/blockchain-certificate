// blockchain-certificate/src/main/java/com/certificate/service/impl/CertificateApplicationServiceImpl.java
package com.certificate.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.entity.CertificateApplication;
import com.certificate.entity.CertificateType;
import com.certificate.entity.Organization;
import com.certificate.entity.User;
import com.certificate.mapper.CertificateApplicationMapper;
import com.certificate.service.CertificateApplicationService;
import com.certificate.service.CertificateTypeService;
import com.certificate.service.OrganizationService;
import com.certificate.service.UserService;
import com.certificate.vo.certificate.CertificateApplicationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;

@Service
public class CertificateApplicationServiceImpl extends ServiceImpl<CertificateApplicationMapper, CertificateApplication>
        implements CertificateApplicationService {

    @Autowired
    private UserService userService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private CertificateTypeService certificateTypeService;

    @Override
    public IPage<CertificateApplicationVO> getApplicationList(Integer status, String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<CertificateApplication> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(CertificateApplication::getStatus, status);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(CertificateApplication::getApplicationNo, keyword)
                    .or().like(CertificateApplication::getApplicationData, keyword));
        }
        wrapper.orderByDesc(CertificateApplication::getApplyTime);
        Page<CertificateApplication> page = new Page<>(pageNum, pageSize);
        IPage<CertificateApplication> result = page(page, wrapper);
        return result.convert(this::convertToVO);
    }

    @Override
    public boolean reviewApplication(Long id, Integer status, String rejectReason, Long reviewerId) {
        CertificateApplication application = getById(id);
        if (application == null) throw new RuntimeException("申请不存在");
        application.setStatus(status);
        application.setReviewTime(new Date());
        application.setReviewerId(reviewerId);
        application.setRejectReason(rejectReason);
        // 审核通过后可在此处自动生成证书并上链
        return updateById(application);
    }

    private CertificateApplicationVO convertToVO(CertificateApplication application) {
        CertificateApplicationVO vo = new CertificateApplicationVO();
        BeanUtils.copyProperties(application, vo);
        User user = userService.getById(application.getUserId());
        if (user != null) vo.setUserName(user.getUsername());
        Organization org = organizationService.getById(application.getOrgId());
        if (org != null) vo.setOrgName(org.getOrgName());
        CertificateType type = certificateTypeService.getById(application.getCertificateTypeId());
        if (type != null) vo.setCertificateTypeName(type.getName());
        vo.setStatusText(getStatusText(application.getStatus()));
        try {
            vo.setApplicationData(JSON.parseObject(application.getApplicationData(), HashMap.class));
        } catch (Exception e) {
            vo.setApplicationData(new HashMap<>());
        }
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