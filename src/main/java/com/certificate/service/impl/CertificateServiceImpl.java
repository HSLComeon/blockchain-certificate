package com.certificate.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.common.constant.Constants;
import com.certificate.entity.Certificate;
import com.certificate.entity.CertificateType;
import com.certificate.entity.Organization;
import com.certificate.entity.User;
import com.certificate.mapper.CertificateMapper;
import com.certificate.service.BlockchainService;
import com.certificate.service.CertificateService;
import com.certificate.service.CertificateTypeService;
import com.certificate.service.OrganizationService;
import com.certificate.service.UserService;
import com.certificate.util.QRCodeUtil;
import com.certificate.vo.certificate.*;
import com.google.zxing.WriterException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CertificateServiceImpl extends ServiceImpl<CertificateMapper, Certificate> implements CertificateService {

    private static final Logger log = LoggerFactory.getLogger(CertificateServiceImpl.class);

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private CertificateTypeService certificateTypeService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService; // 添加UserService依赖

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
        certificate.setCertificateNo(certNo);
        certificate.setName(createVO.getTitle());  // 使用title作为name
        certificate.setCertificateTypeId(createVO.getTypeId());
        certificate.setOrgId(orgId);
        certificate.setUserId(createVO.getUserId() != null ? createVO.getUserId() : 0L);
        certificate.setIssueDate(createVO.getIssueDate());
        certificate.setValidFromDate(createVO.getValidFromDate());
        certificate.setExpireDate(createVO.getValidToDate());
        certificate.setStatus(Constants.CertificateStatus.PENDING); // 待上链
        certificate.setCreateTime(new Date());
        certificate.setUpdateTime(new Date());

        // 将持有人信息和内容存储在临时字段中，不写入数据库
        // 这些信息可以在转换为VO时使用
        certificate.setHolderName(createVO.getHolderName());
        certificate.setHolderIdCard(createVO.getHolderIdCard());
        certificate.setContent(JSON.toJSONString(createVO.getContent()));

        // 保存证书
        save(certificate);

        // 返回证书VO
        CertificateVO certificateVO = convertToVO(certificate);
        certificateVO.setType(certificateType);
        certificateVO.setOrganization(organization);
        // 保存持有人信息到VO中
        certificateVO.setHolderName(createVO.getHolderName());
        certificateVO.setHolderIdCard(createVO.getHolderIdCard());
        // 保存内容到VO中
        certificateVO.setContent(createVO.getContent());

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
                    .like(Certificate::getName, keyword)
                    .or()
                    .like(Certificate::getCertificateNo, keyword)
            );
        }

        // 按创建时间降序排序
        wrapper.orderByDesc(Certificate::getCreateTime);

        // 查询证书列表
        IPage<Certificate> certificatePage = page(page, wrapper);

        // 转换为VO并填充其他信息
        return certificatePage.convert(certificate -> {
            CertificateVO vo = new CertificateVO();
            BeanUtils.copyProperties(certificate, vo);

            // 设置正确的字段映射
            vo.setTitle(certificate.getName());
            vo.setBlockchainTxHash(certificate.getTxHash());
            vo.setTypeId(certificate.getCertificateTypeId());
            vo.setValidToDate(certificate.getExpireDate());
            vo.setCertNo(certificate.getCertificateNo());

            // 设置证书状态文本
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
                    vo.setStatusText("未知状态");
            }

            // 如果用户ID不为0，则查询用户信息并填充持有人信息
            if (certificate.getUserId() != null && certificate.getUserId() > 0) {
                User user = userService.getById(certificate.getUserId());
                if (user != null) {
                    vo.setHolderName(user.getName());
                    vo.setHolderIdCard(user.getIdCard());
                }
            }

            return vo;
        });
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
        CertificateType certificateType = certificateTypeService.getById(certificate.getCertificateTypeId());
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
        certificate.setTxHash(txHash);
        certificate.setStatus(Constants.CertificateStatus.ON_CHAIN);
        certificate.setUpdateTime(new Date());
        updateById(certificate);

        // 返回更新后的证书
        return getCertificateDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUploadToBlockchain(List<Long> ids) {
        int successCount = 0;

        for (Long id : ids) {
            try {
                uploadToBlockchain(id);
                successCount++;
            } catch (Exception e) {
                // 记录错误但继续处理下一个
                log.error("证书上链失败, id: {}, error: {}", id, e.getMessage());
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
            throw new RuntimeException("只有已上链的证书可以撤销");
        }

        // 更新证书状态
        certificate.setStatus(Constants.CertificateStatus.REVOKED);
        certificate.setUpdateTime(new Date());

        // 如果是通过区块链撤销，需要调用区块链服务
        try {
            // 实际项目中可能需要调用区块链服务进行撤销操作
            // blockchainService.revokeCertificate(certificate, revokeVO.getReason());
        } catch (Exception e) {
            throw new RuntimeException("撤销证书时调用区块链服务失败: " + e.getMessage());
        }

        // 更新数据库
        return updateById(certificate);
    }

    @Override
    public CertificateVerifyResultVO verifyCertificate(CertificateVerifyVO verifyVO) {
        // 实际项目中需要调用区块链服务验证证书
        // 这里简化处理，仅检查证书是否存在且已上链

        return new CertificateVerifyResultVO(); // 简化实现
    }

    @Override
    public CertificateVO getCertificateByCertNo(String certNo) {
        Certificate certificate = getOne(new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getCertificateNo, certNo)); // 修改这里，使用getCertificateNo
        return certificate != null ? getCertificateDetail(certificate.getId()) : null;
    }

    @Override
    public String generateQRCode(Long id) {
        try {
            Certificate certificate = getById(id);
            if (certificate == null) {
                throw new RuntimeException("证书不存在");
            }

            // 生成二维码内容
            Map<String, Object> qrContent = new HashMap<>();
            qrContent.put("id", certificate.getId());
            qrContent.put("certNo", certificate.getCertificateNo()); // 修改这里，使用getCertificateNo
            qrContent.put("title", certificate.getName());
            qrContent.put("hash", certificate.getHash());

            String qrCodeContent = JSON.toJSONString(qrContent);

            // 生成二维码图片
            return QRCodeUtil.generateQRCodeBase64(qrCodeContent, 200, 200);
        } catch (WriterException | IOException e) {
            log.error("生成二维码失败: {}", e.getMessage());
            throw new RuntimeException("生成二维码失败");
        }
    }

    @Override
    public int countCertificates(Long orgId) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        if (orgId != null) {
            wrapper.eq(Certificate::getOrgId, orgId);
        }
        return (int) count(wrapper);
    }

    @Override
    public int countCertificatesByStatus(Long orgId, Integer status) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        if (orgId != null) {
            wrapper.eq(Certificate::getOrgId, orgId);
        }
        wrapper.eq(Certificate::getStatus, status);
        return (int) count(wrapper);
    }

    private String generateCertNo(Long orgId) {
        // 生成证书编号（格式：CERT + 12位随机数）
        String prefix = "CERT";
        String randomNum = String.valueOf(System.currentTimeMillis() % 1000000000000L);

        // 补齐12位
        randomNum = "000000000000".substring(randomNum.length()) + randomNum;

        return prefix + randomNum;
    }

    private String generateCertificateHash(Certificate certificate) {
        // 实际项目中应该使用更复杂的哈希算法
        // 这里简化处理
        StringBuilder sb = new StringBuilder();
        sb.append(certificate.getCertificateNo()) // 修改这里，使用getCertificateNo
                .append(certificate.getName())
                .append(certificate.getCertificateTypeId())
                .append(certificate.getOrgId())
                .append(certificate.getIssueDate());

        // 可以使用SHA-256等算法生成哈希
        // 这里简化处理，返回UUID作为哈希
        return UUID.randomUUID().toString().replace("-", "");
    }

    private CertificateVO convertToVO(Certificate certificate) {
        if (certificate == null) {
            return null;
        }

        CertificateVO vo = new CertificateVO();
        BeanUtils.copyProperties(certificate, vo);

        // 设置正确的字段映射
        vo.setTitle(certificate.getName());
        vo.setBlockchainTxHash(certificate.getTxHash());
        vo.setTypeId(certificate.getCertificateTypeId());
        vo.setValidToDate(certificate.getExpireDate());
        vo.setCertNo(certificate.getCertificateNo());

        // 设置证书状态文本
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
                vo.setStatusText("未知状态");
        }

        // 如果用户ID不为0，则查询用户信息并填充持有人信息
        if (certificate.getUserId() != null && certificate.getUserId() > 0) {
            User user = userService.getById(certificate.getUserId());
            if (user != null) {
                vo.setHolderName(user.getName());
                vo.setHolderIdCard(user.getIdCard());
            }
        }

        return vo;
    }

    @Override
    public int countByOrgId(Long orgId) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getOrgId, orgId);
        return (int) count(wrapper);
    }

    @Override
    public int countByOrgIdAndStatus(Long orgId, Integer status) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getOrgId, orgId)
                .eq(Certificate::getStatus, status);
        return (int) count(wrapper);
    }

    @Override
    public List<Long> getCertificateIdsByOrgId(Long orgId) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getOrgId, orgId)
                .select(Certificate::getId);

        List<Certificate> certificates = list(wrapper);
        return certificates.stream()
                .map(Certificate::getId)
                .collect(Collectors.toList());
    }
}