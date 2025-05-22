package com.certificate.controller;

import com.certificate.service.BlockchainService;
import com.certificate.service.CertificateService;
import com.certificate.service.LogService;
import com.certificate.util.IpUtil;
import com.certificate.util.JwtUtil;
import com.certificate.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/blockchain")
public class BlockchainController {

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private CertificateService certificateService;

    @Autowired(required = false)
    private LogService logService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 部署智能合约
     */
    @PostMapping("/deploy")
    public ResponseVO<String> deployContract(HttpServletRequest request) {
        try {
            // 权限检查 - 只有管理员可以部署合约
            Long userId = jwtUtil.getUserIdFromRequest(request);
            // TODO: 检查用户是否是管理员

            String contractAddress = blockchainService.deployContract();

            // 记录日志
            if (logService != null) {
                logService.addLog(
                        "blockchain",
                        "合约部署",
                        "admin",
                        userId,
                        IpUtil.getIpAddress(request),
                        "success",
                        "部署智能合约，地址：" + contractAddress
                );
            }

            return ResponseVO.success("合约部署成功", contractAddress);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("部署失败: " + e.getMessage());
        }
    }

    /**
     * 获取区块链统计信息
     */
    @GetMapping("/stats")
    public ResponseVO<Map<String, Object>> getBlockchainStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCertificates", blockchainService.getCertificateCount());
            stats.put("blockchainStatus", blockchainService.getBlockchainStatus());

            // 添加系统中的证书总数（对比链上和链下）
            try {
                stats.put("systemCertificates", certificateService.getCertificateCount());
            } catch (Exception e) {
                stats.put("systemCertificates", "获取失败");
            }

            return ResponseVO.success("获取成功", stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取交易信息
     */
    @GetMapping("/transaction/{txHash}")
    public ResponseVO<Map<String, Object>> getTransactionInfo(@PathVariable String txHash) {
        try {
            // 调用区块链服务获取交易信息
            Map<String, Object> txInfo = blockchainService.getTransactionInfo(txHash);
            return ResponseVO.success("获取成功", txInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("获取交易信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取区块链状态
     */
    @GetMapping("/status")
    public ResponseVO<Map<String, Object>> getBlockchainStatus() {
        try {
            Map<String, Object> status = blockchainService.getBlockchainStatus();
            return ResponseVO.success("获取成功", status);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("获取区块链状态失败: " + e.getMessage());
        }
    }

    /**
     * 检查区块链健康状态
     */
    @GetMapping("/health")
    public ResponseVO<Boolean> checkBlockchainHealth() {
        try {
            // 简单测试区块链连接
            Map<String, Object> status = blockchainService.getBlockchainStatus();
            boolean healthy = !status.containsKey("error");

            if (healthy) {
                return ResponseVO.success("区块链连接正常", true);
            } else {
                return ResponseVO.error("区块链连接异常: " + status.get("error"), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("区块链连接异常: " + e.getMessage(), false);
        }
    }

    /**
     * 从区块链获取证书详情
     */
    @GetMapping("/certificate/{certNo}")
    public ResponseVO<Map<String, Object>> getCertificateFromBlockchain(@PathVariable String certNo) {
        try {
            Map<String, Object> certInfo = blockchainService.getCertificateFromBlockchain(certNo);
            return ResponseVO.success("获取成功", certInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("获取证书信息失败: " + e.getMessage());
        }
    }
}