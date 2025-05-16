package com.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.certificate.service.LogService;
import com.certificate.util.IpUtil;
import com.certificate.vo.ResponseVO;
import com.certificate.vo.log.LogQueryVO;
import com.certificate.vo.log.LogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

@RestController
@RequestMapping("/admin/logs")
public class LogController {

    @Autowired
    private LogService logService;

    /**
     * 查询日志列表
     */
    @PostMapping("/list")
    public ResponseVO<IPage<LogVO>> getLogList(@RequestBody LogQueryVO queryVO) {
        try {
            System.out.println("接收到日志查询请求: " + queryVO);
            IPage<LogVO> page = logService.getLogList(queryVO);
            return ResponseVO.success("查询成功", page);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取日志详情
     */
    @GetMapping("/{id}")
    public ResponseVO<LogVO> getLogDetail(@PathVariable Long id) {
        try {
            LogVO logVO = logService.getLogDetail(id);
            if (logVO == null) {
                return ResponseVO.error("日志不存在");
            }
            return ResponseVO.success("查询成功", logVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 导出日志
     */
    @PostMapping("/export")
    public ResponseVO<String> exportLogs(@RequestBody LogQueryVO queryVO,
                                         HttpServletRequest request) {
        try {
            // 记录导出操作日志
            logService.addLog(
                    "operation",
                    "导出系统日志",
                    "管理员",
                    null,
                    IpUtil.getIpAddress(request),
                    "success",
                    "导出日志数据，过滤条件：" + queryVO
            );

            String filePath = logService.exportLogs(queryVO);
            if (filePath == null) {
                return ResponseVO.error("导出失败");
            }

            return ResponseVO.success("导出成功", filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error("导出失败: " + e.getMessage());
        }
    }

    /**
     * 下载导出的日志文件
     */
    @GetMapping("/download")
    public void downloadLogs(@RequestParam String filePath,
                             HttpServletResponse response) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"message\":\"文件不存在\"}");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        String fileName = "系统日志_" + System.currentTimeMillis() + ".csv";
        try {
            // 设置响应头
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 写入文件流
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                 OutputStream os = response.getOutputStream()) {

                byte[] buffer = new byte[1024];
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                os.flush();
            }

            // 删除临时文件
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"message\":\"下载失败:" + e.getMessage() + "\"}");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}