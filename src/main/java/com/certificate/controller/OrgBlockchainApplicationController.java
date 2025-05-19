// blockchain-certificate/src/main/java/com/certificate/controller/OrgBlockchainApplicationController.java
package com.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.certificate.entity.CertificateBlockchainApplication;
import com.certificate.service.BlockchainApplicationService;
import com.certificate.util.JwtUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.blockchain.BlockchainApplicationCreateVO;
import com.certificate.vo.blockchain.BlockchainApplicationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/org/blockchain-applications")
public class OrgBlockchainApplicationController {

    @Autowired
    private BlockchainApplicationService blockchainApplicationService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 创建上链申请
     */
    @PostMapping
    public ResponseVO<Boolean> createApplication(@RequestBody @Valid BlockchainApplicationCreateVO createVO,
                                                 HttpServletRequest request) {
        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        // 转换为实体
        CertificateBlockchainApplication application = new CertificateBlockchainApplication();
        application.setCertificateId(createVO.getCertificateId());
        application.setReason(createVO.getReason());
        application.setOrgId(orgId);

        try {
            boolean result = blockchainApplicationService.createApplication(application);
            return ResponseVO.success("申请提交成功", result);
        } catch (Exception e) {
            log.error("提交上链申请失败: {}", e.getMessage());
            return ResponseVO.error("提交上链申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取机构上链申请列表
     */
    @GetMapping
    public ResponseVO<IPage<BlockchainApplicationVO>> getOrgApplications(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        try {
            IPage<BlockchainApplicationVO> page = blockchainApplicationService.getOrgApplicationList(pageNum, pageSize, orgId, status, keyword);
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
    public ResponseVO<BlockchainApplicationVO> getApplicationDetail(@PathVariable Long id, HttpServletRequest request) {
        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        try {
            BlockchainApplicationVO detail = blockchainApplicationService.getApplicationDetail(id);
            if (detail == null) {
                return ResponseVO.error("申请记录不存在");
            }

            // 检查是否是当前机构的申请
            if (!orgId.equals(detail.getOrgId())) {
                return ResponseVO.error("无权查看此申请");
            }

            return ResponseVO.success("获取成功", detail);
        } catch (Exception e) {
            log.error("获取上链申请详情失败: {}", e.getMessage());
            return ResponseVO.error("获取上链申请详情失败: " + e.getMessage());
        }
    }

    /**
     * 取消上链申请
     */
    @PostMapping("/{id}/cancel")
    public ResponseVO<Boolean> cancelApplication(@PathVariable Long id, HttpServletRequest request) {
        // 获取当前机构ID
        Long orgId = jwtUtil.getUserIdFromRequest(request);
        if (orgId == null) {
            return ResponseVO.error("未登录或Token无效");
        }

        try {
            BlockchainApplicationVO detail = blockchainApplicationService.getApplicationDetail(id);
            if (detail == null) {
                return ResponseVO.error("申请记录不存在");
            }

            // 检查是否是当前机构的申请
            if (!orgId.equals(detail.getOrgId())) {
                return ResponseVO.error("无权取消此申请");
            }

            boolean result = blockchainApplicationService.cancelApplication(id);
            return ResponseVO.success("取消成功", result);
        } catch (Exception e) {
            log.error("取消上链申请失败: {}", e.getMessage());
            return ResponseVO.error("取消上链申请失败: " + e.getMessage());
        }
    }
}