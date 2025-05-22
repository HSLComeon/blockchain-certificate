package com.certificate.service;

import com.certificate.entity.Certificate;

import java.util.Map;

/**
 * 区块链服务接口
 */
public interface BlockchainService {

    /**
     * 部署合约
     * @return 合约地址
     */
    String deployContract();

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
     * 获取证书详情
     * @param certNo 证书编号
     * @return 证书详情
     */
    Map<String, Object> getCertificateFromBlockchain(String certNo);

    /**
     * 撤销证书
     * @param certificate 证书
     * @param reason 撤销原因
     * @return 交易哈希
     */
    String revokeCertificate(Certificate certificate, String reason);

    /**
     * 更新证书
     * @param certNo 证书编号
     * @param newHash 新的证书哈希
     * @return 交易哈希
     */
    String updateCertificate(String certNo, String newHash);

    /**
     * 获取链上证书总数
     * @return 证书总数
     */
    int getCertificateCount();

    /**
     * 获取交易信息
     * @param txHash 交易哈希
     * @return 交易详情
     */
    Map<String, Object> getTransactionInfo(String txHash);

    /**
     * 获取区块链状态
     * @return 区块链状态信息
     */
    Map<String, Object> getBlockchainStatus();
}