// blockchain-certificate/src/main/java/com/certificate/service/impl/CertificateApplicationServiceImpl.java
package com.certificate.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.common.constant.Constants;
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

import java.util.*;

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
    // 在CertificateApplicationServiceImpl.java文件中添加以下方法

    @Override
    public boolean createApplication(CertificateApplication application) {
        application.setApplicationNo(generateApplicationNo());
        application.setStatus(Constants.ApplicationStatus.PENDING);
        application.setApplyTime(new Date());
        return save(application);
    }

    // 修改后的CertificateApplicationServiceImpl中的cancelApplication方法
    @Override
    public boolean cancelApplication(Long id, Long userId) {
        CertificateApplication application = getById(id);
        if (application == null) {
            throw new RuntimeException("申请不存在");
        }

        if (!application.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此申请");
        }

        if (application.getStatus() != Constants.ApplicationStatus.PENDING) {
            throw new RuntimeException("只能取消待审核的申请");
        }

        application.setStatus(Constants.ApplicationStatus.CANCELED);
        // 移除对不存在的updateTime字段的设置

        return updateById(application);
    }

    @Override
    public IPage<CertificateApplicationVO> getOrgApplicationList(Long orgId, Integer status, String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<CertificateApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CertificateApplication::getOrgId, orgId);

        if (status != null) {
            wrapper.eq(CertificateApplication::getStatus, status);
        }

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
    public int countByOrgId(Long orgId) {
        LambdaQueryWrapper<CertificateApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CertificateApplication::getOrgId, orgId);
        return (int) count(wrapper);
    }

    @Override
    public int countByOrgIdAndStatus(Long orgId, Integer status) {
        LambdaQueryWrapper<CertificateApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CertificateApplication::getOrgId, orgId)
                .eq(CertificateApplication::getStatus, status);
        return (int) count(wrapper);
    }

    @Override
    public List<Map<String, Object>> getRecentOrgApplications(Long orgId, int limit) {
        LambdaQueryWrapper<CertificateApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CertificateApplication::getOrgId, orgId)
                .orderByDesc(CertificateApplication::getApplyTime)
                .last("LIMIT " + limit);

        List<CertificateApplication> applications = list(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (CertificateApplication app : applications) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", app.getId());
            map.put("applicationNo", app.getApplicationNo());
            map.put("status", app.getStatus());
            map.put("statusText", getStatusText(app.getStatus()));
            map.put("applyTime", app.getApplyTime());

            // 获取用户信息
            User user = userService.getById(app.getUserId());
            map.put("userName", user != null ? user.getUsername() : "未知用户");

            // 获取证书类型信息
            CertificateType type = certificateTypeService.getById(app.getCertificateTypeId());
            map.put("certificateTypeName", type != null ? type.getName() : "未知类型");

            result.add(map);
        }

        return result;
    }

    // 辅助方法：生成申请编号
    private String generateApplicationNo() {
        return "APP" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }
}