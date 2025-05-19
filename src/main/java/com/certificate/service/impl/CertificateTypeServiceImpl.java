package com.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.entity.CertificateType;
import com.certificate.mapper.CertificateTypeMapper;
import com.certificate.service.CertificateTypeService;
import com.certificate.vo.certificateType.CertificateTypeCreateVO;
import com.certificate.vo.certificateType.CertificateTypeUpdateVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class CertificateTypeServiceImpl extends ServiceImpl<CertificateTypeMapper, CertificateType> implements CertificateTypeService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public IPage<CertificateType> getCertificateTypeList(Integer pageNum, Integer pageSize, String keyword) {
        LambdaQueryWrapper<CertificateType> queryWrapper = new LambdaQueryWrapper<>();

        // 添加关键字搜索条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(CertificateType::getName, keyword)
                    .or()
                    .like(CertificateType::getDescription, keyword);
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(CertificateType::getCreateTime);

        // 分页查询
        Page<CertificateType> page = new Page<>(pageNum, pageSize);
        IPage<CertificateType> pageResult = page(page, queryWrapper);

        // 处理查询结果，将JSON字符串属性转为List
        for (CertificateType certificateType : pageResult.getRecords()) {
            parseAttributes(certificateType);
        }

        return pageResult;
    }

    @Override
    public CertificateType createCertificateType(CertificateTypeCreateVO createVO) {
        CertificateType certificateType = new CertificateType();
        BeanUtils.copyProperties(createVO, certificateType);

        // 将属性列表转为JSON字符串
        try {
            certificateType.setAttributes(objectMapper.writeValueAsString(createVO.getAttributes()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("属性转换失败", e);
        }

        // 设置初始状态和使用次数
        certificateType.setStatus(1); // 启用
        certificateType.setUsageCount(0);

        // 设置时间
        Date now = new Date();
        certificateType.setCreateTime(now);
        certificateType.setUpdateTime(now);

        // 保存到数据库
        save(certificateType);

        // 设置属性列表
        certificateType.setAttributeList(createVO.getAttributes());

        return certificateType;
    }

    @Override
    public boolean updateCertificateType(CertificateTypeUpdateVO updateVO) {
        // 查询是否存在
        CertificateType certificateType = getById(updateVO.getId());
        if (certificateType == null) {
            throw new RuntimeException("证书类型不存在");
        }

        // 更新属性
        certificateType.setName(updateVO.getName());
        certificateType.setDescription(updateVO.getDescription());
        certificateType.setColor(updateVO.getColor());

        // 将属性列表转为JSON字符串
        try {
            certificateType.setAttributes(objectMapper.writeValueAsString(updateVO.getAttributes()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("属性转换失败", e);
        }

        // 更新时间
        certificateType.setUpdateTime(new Date());

        // 更新数据库
        return updateById(certificateType);
    }

    @Override
    public boolean deleteCertificateType(Long id) {
        // 可以在这里添加业务逻辑，例如检查该类型是否被使用

        return removeById(id);
    }

    /**
     * 解析证书类型的属性字符串为List
     */
    private void parseAttributes(CertificateType certificateType) {
        if (StringUtils.hasText(certificateType.getAttributes())) {
            try {
                certificateType.setAttributeList(objectMapper.readValue(certificateType.getAttributes(), List.class));
            } catch (JsonProcessingException e) {
                certificateType.setAttributeList(List.of());
                log.error("解析证书属性失败", e);
            }
        } else {
            certificateType.setAttributeList(List.of());
        }
    }

    // blockchain-certificate/src/main/java/com/certificate/service/impl/CertificateTypeServiceImpl.java
// 在 CertificateTypeServiceImpl 类中添加以下方法

    @Override
    public List<CertificateType> getEnabledTypes() {
        LambdaQueryWrapper<CertificateType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CertificateType::getStatus, 1); // 1表示启用状态
        wrapper.orderByDesc(CertificateType::getCreateTime);

        List<CertificateType> types = list(wrapper);

        // 处理属性列表
        for (CertificateType type : types) {
            parseAttributes(type);
        }

        return types;
    }
}