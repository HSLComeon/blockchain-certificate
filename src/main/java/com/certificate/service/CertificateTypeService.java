// blockchain-certificate/src/main/java/com/certificate/service/CertificateTypeService.java
package com.certificate.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.certificate.entity.CertificateType;
import com.certificate.vo.certificateType.CertificateTypeCreateVO;
import com.certificate.vo.certificateType.CertificateTypeUpdateVO;

import java.util.List;

public interface CertificateTypeService extends IService<CertificateType> {

    /**
     * 分页查询证书类型
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 关键字
     * @return 分页结果
     */
    IPage<CertificateType> getCertificateTypeList(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 创建证书类型
     * @param createVO 创建参数
     * @return 创建的证书类型
     */
    CertificateType createCertificateType(CertificateTypeCreateVO createVO);

    /**
     * 更新证书类型
     * @param updateVO 更新参数
     * @return 是否成功
     */
    boolean updateCertificateType(CertificateTypeUpdateVO updateVO);

    /**
     * 删除证书类型
     * @param id 证书类型ID
     * @return 是否成功
     */
    boolean deleteCertificateType(Long id);

    /**
     * 获取所有启用状态的证书类型
     * @return 证书类型列表
     */
    List<CertificateType> getEnabledTypes();
}