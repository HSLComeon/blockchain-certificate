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

    /**
     * 统计用户的证书数量
     * @param userId 用户ID
     * @return 证书数量
     */
    @Select("SELECT COUNT(*) FROM certificate WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户特定状态的证书数量
     * @param userId 用户ID
     * @param status 证书状态
     * @return 证书数量
     */
    @Select("SELECT COUNT(*) FROM certificate WHERE user_id = #{userId} AND status = #{status}")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 根据证书编号查询证书（兼容ServiceImpl调用）
     */
    @Select("SELECT * FROM certificate WHERE certificate_no = #{certificateNo}")
    Certificate selectByCertificateNo(@Param("certificateNo") String certificateNo);
}