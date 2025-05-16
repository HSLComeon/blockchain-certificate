package com.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.certificate.entity.CertificateType;
import com.certificate.service.CertificateTypeService;
import com.certificate.service.LogService;
import com.certificate.util.IpUtil;
import com.certificate.util.JwtUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.certificateType.CertificateTypeCreateVO;
import com.certificate.vo.certificateType.CertificateTypeUpdateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/admin/certificate-types")  // 保持原有路径
public class CertificateTypeController {

    @Autowired
    private CertificateTypeService certificateTypeService;

    @Autowired
    private LogService logService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取证书类型列表
     */
    @GetMapping
    public ResponseVO<IPage<CertificateType>> getCertificateTypeList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        try {
            IPage<CertificateType> page = certificateTypeService.getCertificateTypeList(pageNum, pageSize, keyword);
            return ResponseVO.success("获取成功", page);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("获取证书类型列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取证书类型详情
     */
    @GetMapping("/{id}")
    public ResponseVO<CertificateType> getCertificateTypeDetail(@PathVariable Long id) {
        try {
            CertificateType certificateType = certificateTypeService.getById(id);
            if (certificateType == null) {
                return ResponseVO.error("证书类型不存在");
            }
            return ResponseVO.success("获取成功", certificateType);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("获取证书类型详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建证书类型
     */
    @PostMapping
    public ResponseVO<CertificateType> createCertificateType(
            @Valid @RequestBody CertificateTypeCreateVO createVO,
            HttpServletRequest request) {
        try {
            CertificateType certificateType = certificateTypeService.createCertificateType(createVO);

            // 添加操作日志
            Long adminId = jwtUtil.getUserIdFromRequest(request);
            String username = "admin";

            logService.addLog(
                    "operation",
                    "创建证书类型",
                    username,
                    adminId,
                    IpUtil.getIpAddress(request),
                    "success",
                    "管理员创建了证书类型，名称：" + createVO.getName() //+ "，编码：" + createVO.getCode()
            );

            return ResponseVO.success("创建成功", certificateType);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("创建证书类型失败: " + e.getMessage());
        }
    }

    /**
     * 更新证书类型
     */
    @PutMapping("/{id}")
    public ResponseVO<Boolean> updateCertificateType(
            @PathVariable Long id,
            @Valid @RequestBody CertificateTypeUpdateVO updateVO,
            HttpServletRequest request) {

        if (!id.equals(updateVO.getId())) {
            return ResponseVO.error("参数错误");
        }

        try {
            boolean success = certificateTypeService.updateCertificateType(updateVO);

            if (success) {
                // 添加操作日志
                Long adminId = jwtUtil.getUserIdFromRequest(request);
                String username = "admin";

                logService.addLog(
                        "operation",
                        "更新证书类型",
                        username,
                        adminId,
                        IpUtil.getIpAddress(request),
                        "success",
                        "管理员更新了证书类型，ID：" + id + "，名称：" + updateVO.getName()
                );

                return ResponseVO.success("更新成功", true);
            } else {
                return ResponseVO.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("更新证书类型失败: " + e.getMessage());
        }
    }

    /**
     * 删除证书类型
     */
    @DeleteMapping("/{id}")
    public ResponseVO<Boolean> deleteCertificateType(@PathVariable Long id, HttpServletRequest request) {
        try {
            boolean success = certificateTypeService.deleteCertificateType(id);

            if (success) {
                // 添加操作日志
                Long adminId = jwtUtil.getUserIdFromRequest(request);
                String username = "admin";

                logService.addLog(
                        "operation",
                        "删除证书类型",
                        username,
                        adminId,
                        IpUtil.getIpAddress(request),
                        "success",
                        "管理员删除了证书类型，ID：" + id
                );

                return ResponseVO.success("删除成功", true);
            } else {
                return ResponseVO.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("删除证书类型失败: " + e.getMessage());
        }
    }
}