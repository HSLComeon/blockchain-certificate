package com.certificate.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.common.constant.Constants;
import com.certificate.entity.*;
import com.certificate.event.CertificateCreatedEvent;
import com.certificate.mapper.CertificateMapper;
import com.certificate.service.*;
import com.certificate.util.QRCodeUtil;
import com.certificate.vo.certificate.*;
import com.google.zxing.WriterException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    private UserService userService;

    // 使用事件发布器替代直接依赖
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CertificateMapper certificateMapper;

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

        // 在事务提交后发布证书创建事件，触发自动上链申请
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    try {
                        // 发布证书创建事件
                        eventPublisher.publishEvent(new CertificateCreatedEvent(certificate.getId(), orgId));
                        log.info("证书ID:{} 发布创建事件成功", certificate.getId());
                    } catch (Exception e) {
                        log.error("证书ID:{} 发布创建事件失败: {}", certificate.getId(), e.getMessage());
                    }
                }
            });
        }

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
        certificate.setChainTime(new Date());
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

        // 调用区块链服务撤销证书
        String txHash = blockchainService.revokeCertificate(certificate, revokeVO.getReason());

        // 更新证书状态
        certificate.setStatus(Constants.CertificateStatus.REVOKED);
        certificate.setUpdateTime(new Date());

        // 保存撤销原因和交易哈希
        // 如果需要，可以添加撤销原因字段到证书表中

        // 更新数据库
        return updateById(certificate);
    }


    @Override
    public CertificateVerifyResultVO verifyCertificate(CertificateVerifyVO verifyVO) {
        CertificateVerifyResultVO resultVO = new CertificateVerifyResultVO();

        try {
            // 根据证书编号获取证书 - 修正方法名
            Certificate certificate = getByCertificateNo(verifyVO.getCertNo());
            if (certificate == null) {
                resultVO.setValid(false);
                resultVO.setErrorMessage("证书不存在");
                return resultVO;
            }

            // 从区块链获取证书信息
            Map<String, Object> blockchainCert = blockchainService.getCertificateFromBlockchain(verifyVO.getCertNo());

            // 检查证书是否存在于区块链上
            if (blockchainCert == null || blockchainCert.isEmpty()) {
                resultVO.setValid(false);
                resultVO.setErrorMessage("证书未上链");
                return resultVO;
            }

            // 检查证书是否已撤销
            boolean isRevoked = (boolean) blockchainCert.get("isRevoked");
            if (isRevoked) {
                resultVO.setValid(false);
                resultVO.setErrorMessage("证书已被撤销：" + blockchainCert.get("revokeReason"));
                return resultVO;
            }

            // 检查证书是否过期
            Date expireDate = (Date) blockchainCert.get("expireDate");
            if (expireDate != null && expireDate.before(new Date())) {
                resultVO.setValid(false);
                resultVO.setErrorMessage("证书已过期");
                return resultVO;
            }

            // 验证证书哈希
            String blockchainHash = (String) blockchainCert.get("hash");
            boolean hashValid = blockchainHash.equals(certificate.getHash());

            resultVO.setValid(hashValid);
            resultVO.setErrorMessage(hashValid ? "证书有效" : "证书哈希不匹配");
            resultVO.setCertificate(convertToVO(certificate));  // 修正方法名

            return resultVO;
        } catch (Exception e) {
            log.error("验证证书失败: {}", e.getMessage());
            resultVO.setValid(false);
            resultVO.setErrorMessage("验证失败：" + e.getMessage());
            return resultVO;
        }
    }

    @Override
    public CertificateVO getCertificateByCertNo(String certNo) {
        Certificate certificate = getOne(new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getCertificateNo, certNo));
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
            qrContent.put("certNo", certificate.getCertificateNo());
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
        sb.append(certificate.getCertificateNo())
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
        if (certificate.getOrgId() != null) {
            Organization org = organizationService.getById(certificate.getOrgId());
            if (org != null) {
                vo.setOrgName(org.getOrgName());
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

    @Override
    public int countByUserId(Long userId) {
        return certificateMapper.countByUserId(userId);
    }

    @Override
    public int countOnChainByUserId(Long userId) {
        return certificateMapper.countByUserIdAndStatus(userId, 2); // 状态2表示已上链
    }

    @Override
    public int countRevokedByUserId(Long userId) {
        return certificateMapper.countByUserIdAndStatus(userId, 3); // 状态3表示已撤销
    }

    @Override
    public Certificate getByCertificateNo(String certificateNo) {
        // 假设你有CertificateMapper
        return certificateMapper.selectByCertificateNo(certificateNo);
    }

    @Override
    public IPage<CertificateVO> getCertificateListByUserId(Long userId, Integer pageNum, Integer pageSize) {
        // 构造分页对象
        Page<Certificate> page = new Page<>(pageNum, pageSize);
        // 构造查询条件：只查当前用户的证书
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getUserId, userId);
        wrapper.orderByDesc(Certificate::getCreateTime);

        // 查询证书分页数据
        IPage<Certificate> certificatePage = page(page, wrapper);

        // 转换为VO对象
        return certificatePage.convert(this::convertToVO);
    }
    // ... existing code ...
    @Override
    public List<Map<String, Object>> getRecentCertificatesByOrgId(Long orgId, int limit) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getOrgId, orgId)
                .orderByDesc(Certificate::getIssueDate)
                .last("LIMIT " + limit);

        List<Certificate> certificates = list(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Certificate cert : certificates) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cert.getId());
            map.put("title", cert.getName());
            // 获取持有人
            String holder = "未知";
            if (cert.getUserId() != null && cert.getUserId() > 0) {
                User user = userService.getById(cert.getUserId());
                holder = user != null ? user.getName() : "未知";
            }
            map.put("recipient", holder);
            map.put("time", cert.getIssueDate());
            result.add(map);
        }
        return result;
    }

    /**
     * 统计某个证书类型已发放的证书数量
     * @param typeId 证书类型ID
     * @return 已发放数量
     */
    @Override
    public int countByTypeId(Long typeId) {
        // 构造查询条件：证书类型ID等于typeId
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getCertificateTypeId, typeId);
        // 查询并返回数量
        return (int) count(wrapper);
    }

    @Override
    public int getCertificateCount() {
        try {
            // 使用LambdaQueryWrapper来适配不同MyBatis-Plus版本
            LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
            return Math.toIntExact(count(wrapper));
        } catch (Exception e) {
            log.error("Failed to count certificates: {}", e.getMessage());
            return 0; // 出错时返回0，避免前端显示错误
        }
    }
}