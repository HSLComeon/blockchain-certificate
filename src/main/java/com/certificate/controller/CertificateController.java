package com.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.certificate.service.CertificateService;
import com.certificate.service.LogService;
import com.certificate.util.IpUtil;
import com.certificate.util.JwtUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.certificate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/org/certificates")  // 保持原有路径
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private LogService logService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 创建证书
     */
    @PostMapping
    public ResponseVO<CertificateVO> createCertificate(
            @Valid @RequestBody CertificateCreateVO createVO,
            HttpServletRequest request) {
        try {
            // 从token中获取机构ID
            Long orgId = jwtUtil.getUserIdFromRequest(request);
            String username = "org_user"; // 默认值

            try {
                // 尝试从JWT中获取用户名
                username = orgId.toString(); // 如果JWT中没有存用户名，用ID代替
            } catch (Exception e) {
                // 忽略错误
            }

            CertificateVO certificateVO = certificateService.createCertificate(createVO, orgId);

            // 添加操作日志
            logService.addLog(
                    "operation",
                    "创建证书",
                    username,
                    orgId,
                    IpUtil.getIpAddress(request),
                    "success",
                    "机构管理员创建了证书，证书名称：" + createVO.getTitle() + "，持有人：" + createVO.getHolderName()
            );

            return ResponseVO.success("创建成功", certificateVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("创建证书失败: " + e.getMessage());
        }
    }

    /**
     * 获取证书列表
     */
    @GetMapping
    public ResponseVO<IPage<CertificateVO>> getCertificateList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            // 从token中获取机构ID
            Long orgId = jwtUtil.getUserIdFromRequest(request);

            IPage<CertificateVO> page = certificateService.getCertificateList(orgId, status, keyword, pageNum, pageSize);
            return ResponseVO.success("获取成功", page);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("获取证书列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取证书详情
     */
    @GetMapping("/{id}")
    public ResponseVO<CertificateVO> getCertificateDetail(@PathVariable Long id) {
        try {
            CertificateVO certificateVO = certificateService.getCertificateDetail(id);
            return ResponseVO.success("获取成功", certificateVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("获取证书详情失败: " + e.getMessage());
        }
    }

    /**
     * 将证书上链
     */
    @PostMapping("/{id}/blockchain")
    public ResponseVO<CertificateVO> uploadToBlockchain(@PathVariable Long id, HttpServletRequest request) {
        try {
            CertificateVO certificateVO = certificateService.uploadToBlockchain(id);

            // 从token中获取机构ID
            Long orgId = jwtUtil.getUserIdFromRequest(request);
            String username = "org_user"; // 默认值

            // 添加上链日志
            logService.addLog(
                    "blockchain",
                    "证书上链",
                    username,
                    orgId,
                    IpUtil.getIpAddress(request),
                    "success",
                    "证书ID" + id + "已成功上链，交易哈希：" + certificateVO.getBlockchainTxHash()
            );

            return ResponseVO.success("上链成功", certificateVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("上链失败: " + e.getMessage());
        }
    }
    /**
     * 批量上链
     */
    @PostMapping("/batch-upload")
    public ResponseVO<Integer> batchUploadToBlockchain(@RequestBody List<Long> ids, HttpServletRequest request) {
        try {
            int successCount = certificateService.batchUploadToBlockchain(ids);

            // 从token中获取机构ID
            Long orgId = jwtUtil.getUserIdFromRequest(request);
            String username = "org_user"; // 默认值

            // 添加批量上链日志
            logService.addLog(
                    "blockchain",
                    "批量证书上链",
                    username,
                    orgId,
                    IpUtil.getIpAddress(request),
                    "success",
                    "批量上链完成，成功数量: " + successCount + "，总提交数量：" + ids.size()
            );

            return ResponseVO.success("批量上链完成，成功数量: " + successCount, successCount);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("批量上链失败: " + e.getMessage());
        }
    }

    /**
     * 撤销证书
     */
    @PostMapping("/revoke")
    public ResponseVO<Boolean> revokeCertificate(@Valid @RequestBody CertificateRevokeVO revokeVO, HttpServletRequest request) {
        try {
            boolean success = certificateService.revokeCertificate(revokeVO);

            // 从token中获取机构ID
            Long orgId = jwtUtil.getUserIdFromRequest(request);
            String username = "org_user"; // 默认值

            // 添加撤销日志
            logService.addLog(
                    "blockchain",
                    "证书撤销",
                    username,
                    orgId,
                    IpUtil.getIpAddress(request),
                    "success",
                    "证书ID" + revokeVO.getId() + "已被撤销，撤销原因：" + revokeVO.getReason()
            );

            return ResponseVO.success("撤销成功", success);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("撤销失败: " + e.getMessage());
        }
    }

    /**
     * 生成证书二维码
     */
    @GetMapping("/{id}/qrcode")
    public ResponseVO<String> generateQRCode(@PathVariable Long id) {
        try {
            String qrCode = certificateService.generateQRCode(id);
            return ResponseVO.success("生成成功", qrCode);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("生成二维码失败: " + e.getMessage());
        }
    }
}