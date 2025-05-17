// blockchain-certificate/src/main/java/com/certificate/mapper/CertificateRevocationMapper.java
package com.certificate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.certificate.entity.CertificateRevocation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CertificateRevocationMapper extends BaseMapper<CertificateRevocation> {
}