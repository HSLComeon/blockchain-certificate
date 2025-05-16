package com.certificate.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.common.constant.Constants;
import com.certificate.entity.Certificate;
import com.certificate.entity.CertificateType;
import com.certificate.entity.Organization;
import com.certificate.mapper.CertificateMapper;
import com.certificate.service.BlockchainService;
import com.certificate.service.CertificateService;
import com.certificate.service.CertificateTypeService;
import com.certificate.service.OrganizationService;
import com.certificate.util.QRCodeUtil;
import com.certificate.vo.certificate.*;
import com.google.zxing.WriterException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CertificateServiceImpl extends ServiceImpl<CertificateMapper, Certificate> implements CertificateService {

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private CertificateTypeService certificateTypeService;

    @Autowired
    private OrganizationService organizationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CertificateVO createCertificate(CertificateCreateVO createVO, Long orgId) {
        // 检查证书类型是否存在
        CertificateType certificateType = certificateTypeService.getById(createVO.getTypeId());
        if (certificateType == null) {
            throw new RuntimeException("证书类型不存在");
        }

        // 获取机构信息
        Organization organization = organizationService.getById(orgId);
        if (organization == null) {
            throw new RuntimeException("机构不存在");
        }

        // 生成证书编号（格式：前缀+8位随机数）
        String certNo = generateCertNo(orgId);

        // 创建证书对象
        Certificate certificate = new Certificate();
        BeanUtils.copyProperties(createVO, certificate);
        certificate.setCertNo(certNo);
        certificate.setOrgId(orgId);
        certificate.setContent(JSON.toJSONString(createVO.getContent()));
        certificate.setStatus(Constants.CertificateStatus.PENDING); // 待上链

        // 生成证书哈希
        String hash = generateCertificateHash(certificate);
        certificate.setHash(hash);

        certificate.setCreateTime(new Date());
        certificate.setUpdateTime(new Date());

        // 保存证书
        save(certificate);

        // 返回证书VO
        CertificateVO certificateVO = convertToVO(certificate);
        certificateVO.setType(certificateType);
        certificateVO.setOrganization(organization);

        return certificateVO;
    }

    @Override
    public IPage<CertificateVO> getCertificateList(Long orgId, Integer status, String keyword, Integer pageNum, Integer pageSize) {
        Page<Certificate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();

        // 根据机构ID筛选
        if (orgId != null) {
            wrapper.eq(Certificate::getOrgId, orgId);
        }

        // 根据状态筛选
        if (status != null) {
            wrapper.eq(Certificate::getStatus, status);
        }

        // 根据关键字搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(Certificate::getTitle, keyword)
                    .or()
                    .like(Certificate::getCertNo, keyword)
                    .or()
                    .like(Certificate::getHolderName, keyword)
                    .or()
                    .like(Certificate::getHolderIdCard, keyword)
            );
        }

        // 按创建时间降序排序
        wrapper.orderByDesc(Certificate::getCreateTime);

        // 查询证书列表
        IPage<Certificate> certificatePage = page(page, wrapper);

        // 转换为VO
        IPage<CertificateVO> certificateVOPage = certificatePage.convert(this::convertToVO);

        // 填充类型和机构信息
        if (certificateVOPage.getRecords().size() > 0) {
            // 获取证书类型ID列表
            List<Long> typeIds = certificateVOPage.getRecords().stream()
                    .map(CertificateVO::getTypeId)
                    .distinct()
                    .collect(Collectors.toList());

            // 获取机构ID列表
            List<Long> orgIds = certificateVOPage.getRecords().stream()
                    .map(CertificateVO::getOrgId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询证书类型
            List<CertificateType> types = certificateTypeService.listByIds(typeIds);
            Map<Long, CertificateType> typeMap = types.stream()
                    .collect(Collectors.toMap(CertificateType::getId, type -> type));

            // 批量查询机构
            List<Organization> orgs = organizationService.listByIds(orgIds);
            Map<Long, Organization> orgMap = orgs.stream()
                    .collect(Collectors.toMap(Organization::getId, org -> org));

            // 填充类型和机构信息
            for (CertificateVO certificateVO : certificateVOPage.getRecords()) {
                certificateVO.setType(typeMap.get(certificateVO.getTypeId()));
                certificateVO.setOrganization(orgMap.get(certificateVO.getOrgId()));
            }
        }

        return certificateVOPage;
    }

    @Override
    public CertificateVO getCertificateDetail(Long id) {
        Certificate certificate = getById(id);
        if (certificate == null) {
            throw new RuntimeException("证书不存在");
        }

        // 转换为VO
        CertificateVO certificateVO = convertToVO(certificate);

        // 填充类型和机构信息
        CertificateType certificateType = certificateTypeService.getById(certificate.getTypeId());
        Organization organization = organizationService.getById(certificate.getOrgId());

        certificateVO.setType(certificateType);
        certificateVO.setOrganization(organization);

        return certificateVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CertificateVO uploadToBlockchain(Long id) {
        Certificate certificate = getById(id);
        if (certificate == null) {
            throw new RuntimeException("证书不存在");
        }

        // 检查证书状态
        if (certificate.getStatus() != Constants.CertificateStatus.PENDING) {
            throw new RuntimeException("证书状态不允许上链");
        }

        // 调用区块链服务上链
        String txHash = blockchainService.uploadToBlockchain(certificate);

        // 更新证书状态
        certificate.setBlockchainTxHash(txHash);
        certificate.setStatus(Constants.CertificateStatus.ON_CHAIN);
        certificate.setUpdateTime(new Date());
        updateById(certificate);

        // 返回更新后的证书VO
        return getCertificateDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUploadToBlockchain(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Long id : ids) {
            try {
                uploadToBlockchain(id);
                successCount++;
            } catch (Exception e) {
                System.err.println("证书 " + id + " 上链失败: " + e.getMessage());
            }
        }

        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revokeCertificate(CertificateRevokeVO revokeVO) {
        Certificate certificate = getById(revokeVO.getId());
        if (certificate == null) {
            throw new RuntimeException("证书不存在");
        }

        // 检查证书状态
        if (certificate.getStatus() != Constants.CertificateStatus.ON_CHAIN) {
            throw new RuntimeException("证书状态不允许撤销");
        }

        // 调用区块链服务撤销证书
        String txHash = blockchainService.revokeCertificate(certificate);

        // 更新证书状态
        certificate.setStatus(Constants.CertificateStatus.REVOKED);
        certificate.setUpdateTime(new Date());
        return updateById(certificate);
    }

    @Override
    public CertificateVerifyResultVO verifyCertificate(CertificateVerifyVO verifyVO) {
        // 根据证书编号查询证书
        Certificate certificate = baseMapper.getByCertNo(verifyVO.getCertNo());
        if (certificate == null) {
            return CertificateVerifyResultVO.failure("证书不存在");
        }

        // 检查证书状态
        if (certificate.getStatus() == Constants.CertificateStatus.REVOKED) {
            return CertificateVerifyResultVO.failure("证书已被撤销");
        }

        // 验证哈希
        if (!certificate.getHash().equals(verifyVO.getHash())) {
            return CertificateVerifyResultVO.failure("证书哈希不匹配");
        }

        // 如果证书已上链，调用区块链验证
        if (certificate.getStatus() == Constants.CertificateStatus.ON_CHAIN) {
            boolean verifyResult = blockchainService.verifyCertificate(verifyVO.getCertNo(), verifyVO.getHash());
            if (!verifyResult) {
                return CertificateVerifyResultVO.failure("区块链验证失败");
            }
        }

        // 获取完整证书信息
        CertificateVO certificateVO = getCertificateDetail(certificate.getId());
        return CertificateVerifyResultVO.success(certificateVO);
    }

    @Override
    public CertificateVO getCertificateByCertNo(String certNo) {
        Certificate certificate = baseMapper.getByCertNo(certNo);
        if (certificate == null) {
            throw new RuntimeException("证书不存在");
        }

        return getCertificateDetail(certificate.getId());
    }

    @Override
    public String generateQRCode(Long id) {
        Certificate certificate = getById(id);
        if (certificate == null) {
            throw new RuntimeException("证书不存在");
        }

        // 构建QR码内容（证书编号和哈希）
        Map<String, String> qrContent = new HashMap<>();
        qrContent.put("certNo", certificate.getCertNo());
        qrContent.put("hash", certificate.getHash());

        String content = JSON.toJSONString(qrContent);

        try {
            // 生成QR码
            return QRCodeUtil.generateQRCodeBase64(content, 300, 300);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("生成二维码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int countCertificates(Long orgId) {
        if (orgId != null) {
            return baseMapper.countByOrgId(orgId);
        } else {
            return (int) count();
        }
    }

    /**
     * 生成证书编号
     */
    private String generateCertNo(Long orgId) {
        // 格式：ORG + 机构ID前缀 + 时间戳 + 3位随机数
        String prefix = "CERT" + String.format("%04d", orgId % 10000);
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        String random = String.format("%03d", new Random().nextInt(1000));

        return prefix + timestamp + random;
    }

    /**
     * 生成证书哈希
     */
    private String generateCertificateHash(Certificate certificate) {
        // 简单实现：使用证书内容的MD5
        String content = certificate.getCertNo() + certificate.getHolderName() +
                certificate.getHolderIdCard() + certificate.getContent() +
                certificate.getIssueDate() + certificate.getOrgId();

        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(content.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; i++) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("生成证书哈希失败", e);
        }
    }

    /**
     * 转换为VO
     */
    private CertificateVO convertToVO(Certificate certificate) {
        CertificateVO vo = new CertificateVO();
        BeanUtils.copyProperties(certificate, vo);

        // 处理内容JSON
        try {
            Map<String, Object> contentMap = JSON.parseObject(certificate.getContent(), Map.class);
            vo.setContent(contentMap);
        } catch (Exception e) {
            vo.setContent(new HashMap<>());
        }

        // 设置状态文本
        switch (certificate.getStatus()) {
            case Constants.CertificateStatus.PENDING:
                vo.setStatusText("待上链");
                break;
            case Constants.CertificateStatus.ON_CHAIN:
                vo.setStatusText("已上链");
                break;
            case Constants.CertificateStatus.REVOKED:
                vo.setStatusText("已撤销");
                break;
            default:
                vo.setStatusText("未知");
                break;
        }

        return vo;
    }
}