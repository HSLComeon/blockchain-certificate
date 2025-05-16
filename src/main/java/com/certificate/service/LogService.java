package com.certificate.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.certificate.entity.AuditLog;
import com.certificate.vo.log.LogQueryVO;
import com.certificate.vo.log.LogVO;

public interface LogService extends IService<AuditLog> {

    /**
     * 添加日志
     * @param type 日志类型
     * @param description 操作描述
     * @param operator 操作人
     * @param operatorId 操作人ID
     * @param ipAddress IP地址
     * @param status 状态
     * @param content 内容
     * @return 是否成功
     */
    boolean addLog(String type, String description, String operator, Long operatorId,
                   String ipAddress, String status, String content);

    /**
     * 查询日志列表
     * @param queryVO 查询条件
     * @return 日志列表
     */
    IPage<LogVO> getLogList(LogQueryVO queryVO);

    /**
     * 获取日志详情
     * @param id 日志ID
     * @return 日志详情
     */
    LogVO getLogDetail(Long id);

    /**
     * 导出日志
     * @param queryVO 查询条件
     * @return 文件路径
     */
    String exportLogs(LogQueryVO queryVO);
}