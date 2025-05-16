package com.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.certificate.entity.AuditLog;
import com.certificate.mapper.AuditLogMapper;
import com.certificate.service.LogService;
import com.certificate.vo.log.LogQueryVO;
import com.certificate.vo.log.LogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class LogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements LogService {

    @Override
    public boolean addLog(String type, String description, String operator, Long operatorId,
                          String ipAddress, String status, String content) {
        AuditLog log = new AuditLog();
        log.setLogNo(generateLogNo());
        log.setType(type);
        log.setDescription(description);
        log.setOperator(operator);
        log.setOperatorId(operatorId);
        log.setIpAddress(ipAddress);
        log.setOperationTime(new Date());
        log.setStatus(status);
        log.setContent(content);
        log.setCreateTime(new Date());
        return save(log);
    }

    @Override
    public IPage<LogVO> getLogList(LogQueryVO queryVO) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();

        // 打印接收到的查询条件
        System.out.println("日志查询条件: " + queryVO);

        // 设置查询条件
        if (queryVO.getType() != null && !queryVO.getType().isEmpty()) {
            wrapper.eq(AuditLog::getType, queryVO.getType());
        }

        if (queryVO.getOperator() != null && !queryVO.getOperator().isEmpty()) {
            wrapper.like(AuditLog::getOperator, queryVO.getOperator());
        }

        if (queryVO.getStartDate() != null) {
            wrapper.ge(AuditLog::getOperationTime, queryVO.getStartDate());
            System.out.println("开始日期: " + queryVO.getStartDate());
        }

        if (queryVO.getEndDate() != null) {
            wrapper.le(AuditLog::getOperationTime, queryVO.getEndDate());
            System.out.println("结束日期: " + queryVO.getEndDate());
        }

        // 按操作时间降序排序
        wrapper.orderByDesc(AuditLog::getOperationTime);

        // 分页查询
        Page<AuditLog> page = new Page<>(queryVO.getPageNum(), queryVO.getPageSize());
        Page<AuditLog> logPage = page(page, wrapper);

        System.out.println("查询到日志记录数: " + logPage.getRecords().size());

        // 转换为VO对象
        List<LogVO> logVOList = new ArrayList<>();
        for (AuditLog log : logPage.getRecords()) {
            LogVO logVO = new LogVO();
            BeanUtils.copyProperties(log, logVO);
            logVOList.add(logVO);
        }

        // 构建返回结果
        Page<LogVO> resultPage = new Page<>();
        resultPage.setRecords(logVOList);
        resultPage.setTotal(logPage.getTotal());
        resultPage.setCurrent(logPage.getCurrent());
        resultPage.setSize(logPage.getSize());

        return resultPage;
    }

    @Override
    public LogVO getLogDetail(Long id) {
        AuditLog log = getById(id);
        if (log == null) {
            return null;
        }

        LogVO logVO = new LogVO();
        BeanUtils.copyProperties(log, logVO);
        return logVO;
    }

    @Override
    public String exportLogs(LogQueryVO queryVO) {
        // 查询所有符合条件的日志
        queryVO.setPageNum(1);
        queryVO.setPageSize(Integer.MAX_VALUE > 1000 ? 1000 : Integer.MAX_VALUE); // 限制最大导出数量
        IPage<LogVO> logPage = getLogList(queryVO);
        List<LogVO> logs = logPage.getRecords();

        try {
            // 创建临时目录（如果不存在）
            String tempDir = System.getProperty("java.io.tmpdir");
            File dir = new File(tempDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成CSV文件
            String fileName = "logs_" + System.currentTimeMillis() + ".csv";
            String filePath = tempDir + File.separator + fileName;

            System.out.println("导出日志到临时文件: " + filePath);

            try (FileOutputStream fos = new FileOutputStream(filePath);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                 BufferedWriter bw = new BufferedWriter(osw)) {

                // 写入CSV头
                bw.write("日志编号,日志类型,操作描述,操作人,IP地址,操作时间,状态,详细内容");
                bw.newLine();

                // 写入数据
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for (LogVO log : logs) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(csvEscape(log.getLogNo())).append(",");
                    sb.append(csvEscape(getLogTypeText(log.getType()))).append(",");
                    sb.append(csvEscape(log.getDescription())).append(",");
                    sb.append(csvEscape(log.getOperator())).append(",");
                    sb.append(csvEscape(log.getIpAddress())).append(",");
                    sb.append(csvEscape(log.getOperationTime() != null ? sdf.format(log.getOperationTime()) : "")).append(",");
                    sb.append(csvEscape("success".equals(log.getStatus()) ? "成功" : "失败")).append(",");
                    sb.append(csvEscape(log.getContent()));

                    bw.write(sb.toString());
                    bw.newLine();
                }

                bw.flush();
            }

            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * CSV字段转义
     */
    private String csvEscape(String field) {
        if (field == null) {
            return "";
        }

        // 如果字段包含逗号、双引号或换行符，需要用双引号包围并对内部双引号进行转义
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }

        return field;
    }

    /**
     * 生成日志编号
     */
    private String generateLogNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(new Date());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return "LOG" + date + uuid;
    }

    /**
     * 获取日志类型文本
     */
    private String getLogTypeText(String type) {
        switch (type) {
            case "login":
                return "登录日志";
            case "operation":
                return "操作日志";
            case "system":
                return "系统日志";
            case "blockchain":
                return "区块链日志";
            default:
                return type;
        }
    }
}