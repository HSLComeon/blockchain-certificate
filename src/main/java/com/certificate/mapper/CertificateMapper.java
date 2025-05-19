package com.certificate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.certificate.entity.Certificate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CertificateMapper extends BaseMapper<Certificate> {

    /**
     * 根据证书编号查询证书
     */
    @Select("SELECT * FROM certificate WHERE certificate_no = #{certNo}")
    Certificate getByCertNo(@Param("certNo") String certNo);

    /**
     * 统计机构已发放的证书数量
     * @param orgId 机构ID
     * @return 证书数量
     */
    @Select("SELECT COUNT(*) FROM certificate WHERE org_id = #{orgId}")
    int countByOrgId(@Param("orgId") Long orgId);

    /**
     * 统计待上链的证书数量
     * @return 证书数量
     */
    @Select("SELECT COUNT(*) FROM certificate WHERE status = 0")
    int countPendingCertificates();

    /**
     * 统计已上链的证书数量
     * @return 证书数量
     */
    @Select("SELECT COUNT(*) FROM certificate WHERE status = 1")
    int countOnChainCertificates();
}