package com.certificate.service;

import com.certificate.entity.Certificate;

/**
 * 区块链服务接口
 */
public interface BlockchainService {

    /**
     * 将证书上链
     * @param certificate 证书
     * @return 交易哈希
     */
    String uploadToBlockchain(Certificate certificate);

    /**
     * 验证证书是否在区块链上
     * @param certNo 证书编号
     * @param hash 证书哈希
     * @return 验证结果
     */
    boolean verifyCertificate(String certNo, String hash);

    /**
     * 撤销证书
     * @param certificate 证书
     * @return 交易哈希
     */
    String revokeCertificate(Certificate certificate);
}