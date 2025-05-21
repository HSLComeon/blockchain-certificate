// blockchain-certificate/src/main/java/com/certificate/controller/AdminBlockchainApplicationController.java
package com.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.certificate.service.BlockchainApplicationService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.blockchain.BlockchainApplicationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/admin/blockchain-applications")
public class AdminBlockchainApplicationController {

    @Autowired
    private BlockchainApplicationService blockchainApplicationService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取上链申请列表
     */
    @GetMapping
    public ResponseVO<IPage<BlockchainApplicationVO>> getApplications(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            IPage<BlockchainApplicationVO> page = blockchainApplicationService.getApplicationList(pageNum, pageSize, status, keyword);
            return ResponseVO.success("获取成功", page);
        } catch (Exception e) {
            log.error("获取上链申请列表失败: {}", e.getMessage());
            return ResponseVO.error("获取上链申请列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取上链申请详情
     */
    @GetMapping("/{id}")
    public ResponseVO<BlockchainApplicationVO> getApplicationDetail(@PathVariable Long id) {
        try {
            BlockchainApplicationVO detail = blockchainApplicationService.getApplicationDetail(id);
            if (detail == null) {
                return ResponseVO.error("申请记录不存在");
            }
            return ResponseVO.success("获取成功", detail);
        } catch (Exception e) {
            log.error("获取上链申请详情失败: {}", e.getMessage());
            return ResponseVO.error("获取上链申请详情失败: " + e.getMessage());
        }
    }

    /**
     * 审核上链申请
     */
    @PostMapping("/{id}/review")
    public ResponseVO<Boolean> reviewApplication(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String rejectReason,
            HttpServletRequest request) {
        // 获取当前管理员ID
        Long adminId = jwtUtil.getUserIdFromRequest(request);
        if (adminId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        try {
            boolean result = blockchainApplicationService.reviewApplication(id, status, rejectReason, adminId);
            return ResponseVO.success("审核成功", result);
        } catch (Exception e) {
            log.error("审核上链申请失败: {}", e.getMessage());
            return ResponseVO.error("审核上链申请失败: " + e.getMessage());
        }
    }
}