package com.certificate.service.impl;

import com.certificate.config.BlockchainConfig;
import com.certificate.contracts.CertificateContract;
import com.certificate.entity.Certificate;
import com.certificate.service.BlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class BlockchainServiceImpl implements BlockchainService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainServiceImpl.class);

    @Autowired
    private Web3j web3j;

    @Autowired
    private Credentials credentials;

    @Autowired
    private ContractGasProvider gasProvider;

    @Autowired
    private BlockchainConfig blockchainConfig;

    // 缓存合约实例
    private CertificateContract certificateContract;

    // 工具方法：String转bytes32
    private static byte[] stringToBytes32(String str) {
        byte[] byteValue = str.getBytes(StandardCharsets.UTF_8);
        return java.util.Arrays.copyOf(byteValue, 32);
    }

    // 工具方法：bytes32转String
    private static String bytes32ToString(byte[] bytes) {
        int len = 0;
        for (; len < bytes.length; len++) {
            if (bytes[len] == 0) break;
        }
        return new String(bytes, 0, len, StandardCharsets.UTF_8);
    }

    /**
     * 获取合约实例
     * @return 合约实例
     */
    private CertificateContract getContract() {
        if (certificateContract == null) {
            String contractAddress = blockchainConfig.getContractAddress();
            if (contractAddress == null || contractAddress.isEmpty()) {
                try {
                    // 尝试自动部署合约
                    log.info("Contract address not set. Attempting to deploy contract automatically.");
                    return deployAndGetContract();
                } catch (Exception e) {
                    throw new RuntimeException("合约地址未设置且自动部署失败: " + e.getMessage(), e);
                }
            }
            try {
                certificateContract = CertificateContract.load(
                        contractAddress,
                        web3j,
                        credentials,
                        gasProvider
                );
                // 测试合约连接 - 捕获异常避免启动失败
                try {
                    certificateContract.getCertificateCount().send();
                    log.info("成功连接到合约: {}", contractAddress);
                } catch (Exception e) {
                    log.warn("合约连接测试失败，但仍继续使用: {}", e.getMessage());
                }
            } catch (Exception e) {
                log.error("Failed to load contract at address {}: {}", contractAddress, e.getMessage());
                throw new RuntimeException("加载合约失败: " + e.getMessage(), e);
            }
        }
        return certificateContract;
    }

    /**
     * 部署合约并返回实例
     * @return 合约实例
     */
    private CertificateContract deployAndGetContract() {
        try {
            log.info("Deploying smart contract...");
            CertificateContract contract = CertificateContract.deploy(
                    web3j,
                    credentials,
                    gasProvider
            ).send();

            String contractAddress = contract.getContractAddress();
            log.info("Contract deployed at address: {}", contractAddress);

            // 更新合约实例
            certificateContract = contract;

            return certificateContract;
        } catch (Exception e) {
            log.error("Failed to deploy contract: {}", e.getMessage());
            throw new RuntimeException("部署合约失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String deployContract() {
        try {
            log.info("正在开始部署智能合约...");

            // 检查区块链连接
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            log.info("当前区块高度: {}", blockNumber.getBlockNumber());

            // 检查账户余额
            EthGetBalance balance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            log.info("部署账户地址: {}, 余额: {} wei", credentials.getAddress(), balance.getBalance());

            if (balance.getBalance().compareTo(BigInteger.valueOf(1000000000000000000L)) < 0) {
                log.warn("账户余额较低，可能影响部署");
            }

            // 尝试部署合约，使用更高Gas限制
            CertificateContract contract = CertificateContract.deploy(
                    web3j,
                    credentials,
                    gasProvider
            ).send();

            String contractAddress = contract.getContractAddress();
            log.info("智能合约成功部署，地址: {}", contractAddress);

            // 验证合约部署
            BigInteger count = contract.getCertificateCount().send();
            log.info("合约初始证书数量: {}", count);

            // 更新合约实例
            certificateContract = contract;

            return certificateContract.getContractAddress();
        } catch (Exception e) {
            log.error("部署合约失败，详细错误: ", e);
            throw new RuntimeException("部署合约失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadToBlockchain(Certificate certificate) {
        try {
            log.info("Uploading certificate to blockchain: {}", certificate.getCertificateNo());

            // 获取合约实例
            CertificateContract contract = getContract();

            // 计算证书数据哈希
            String dataHash = calculateCertificateHash(certificate);

            // 计算时间戳
            BigInteger issueTimestamp = getTimestampFromDate(certificate.getIssueDate());

            // 合约只支持3个参数：bytes32, bytes32, uint256
            TransactionReceipt receipt = contract.issueCertificate(
                    stringToBytes32(certificate.getCertificateNo()),
                    stringToBytes32(dataHash),
                    issueTimestamp
            ).send();

            String txHash = receipt.getTransactionHash();
            log.info("Certificate uploaded to blockchain. Transaction hash: {}", txHash);

            return txHash;
        } catch (Exception e) {
            log.error("Failed to upload certificate to blockchain: {}", e.getMessage());
            throw new RuntimeException("上链失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyCertificate(String certNo, String hash) {
        try {
            log.info("Verifying certificate on blockchain: {}", certNo);

            // 获取合约实例
            CertificateContract contract = getContract();

            // 调用合约验证方法
            Tuple3<Boolean, Boolean, BigInteger> result = contract.verifyCertificate(
                    stringToBytes32(certNo)
            ).send();

            // 只判断证书是否存在且未撤销
            return result.getValue1() && !result.getValue2();
        } catch (Exception e) {
            log.error("Failed to verify certificate on blockchain: {}", e.getMessage());
            throw new RuntimeException("验证失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getCertificateFromBlockchain(String certNo) {
        try {
            log.info("Getting certificate from blockchain: {}", certNo);

            // 获取合约实例
            CertificateContract contract = getContract();

            // 查询证书详情
            Tuple3<byte[], BigInteger, Boolean> result = contract.certificates(
                    stringToBytes32(certNo)
            ).send();

            // 封装结果
            Map<String, Object> certificateData = new HashMap<>();
            certificateData.put("certificateNo", certNo);
            certificateData.put("hash", bytes32ToString(result.getValue1()));
            certificateData.put("issueDate", new Date(result.getValue2().longValue() * 1000));
            certificateData.put("isRevoked", result.getValue3());

            return certificateData;
        } catch (Exception e) {
            log.error("Failed to get certificate from blockchain: {}", e.getMessage());
            throw new RuntimeException("获取证书信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String revokeCertificate(Certificate certificate, String reason) {
        try {
            log.info("Revoking certificate on blockchain: {}", certificate.getCertificateNo());

            // 获取合约实例
            CertificateContract contract = getContract();

            // 合约只支持一个参数
            TransactionReceipt receipt = contract.revokeCertificate(
                    stringToBytes32(certificate.getCertificateNo())
            ).send();

            String txHash = receipt.getTransactionHash();
            log.info("Certificate revoked on blockchain. Transaction hash: {}", txHash);

            return txHash;
        } catch (Exception e) {
            log.error("Failed to revoke certificate on blockchain: {}", e.getMessage());
            throw new RuntimeException("撤销失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String updateCertificate(String certNo, String newHash) {
        return null;
    }

    // 合约没有 updateCertificate 方法，暂时注释掉
    /*
    @Override
    public String updateCertificate(String certNo, String newHash) {
        throw new UnsupportedOperationException("合约未实现 updateCertificate 方法");
    }
    */

    @Override
    public int getCertificateCount() {
        try {
            // 获取合约实例
            CertificateContract contract = getContract();

            // 调用合约方法
            BigInteger count = contract.getCertificateCount().send();

            return count.intValue();
        } catch (Exception e) {
            log.error("Failed to get certificate count from blockchain: {}", e.getMessage());
            throw new RuntimeException("获取证书总数失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getTransactionInfo(String txHash) {
        try {
            log.info("Getting transaction info: {}", txHash);

            // 获取交易收据
            TransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash)
                    .send()
                    .getTransactionReceipt()
                    .orElseThrow(() -> new RuntimeException("Transaction receipt not found"));

            // 获取交易
            Transaction tx = web3j.ethGetTransactionByHash(txHash)
                    .send()
                    .getTransaction()
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            // 获取区块信息
            EthBlock.Block block = web3j.ethGetBlockByHash(receipt.getBlockHash(), false)
                    .send()
                    .getBlock();

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("blockHeight", receipt.getBlockNumber());
            result.put("blockHash", receipt.getBlockHash());
            result.put("gasUsed", receipt.getGasUsed());
            result.put("status", receipt.isStatusOK());
            result.put("from", tx.getFrom());
            result.put("to", tx.getTo());
            result.put("value", tx.getValue());
            result.put("timestamp", block.getTimestamp().longValue() * 1000); // 转为毫秒

            return result;
        } catch (Exception e) {
            log.error("Failed to get transaction info: {}", e.getMessage());
            throw new RuntimeException("获取交易信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getBlockchainStatus() {
        try {
            log.info("Getting blockchain status");

            // 获取最新区块
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            BigInteger latestBlock = blockNumber.getBlockNumber();

            // 获取Gas价格
            EthGasPrice gasPrice = web3j.ethGasPrice().send();

            // 获取节点信息
            Web3ClientVersion clientVersion = web3j.web3ClientVersion().send();

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("latestBlock", latestBlock);
            result.put("gasPrice", gasPrice.getGasPrice());
            result.put("clientVersion", clientVersion.getWeb3ClientVersion());
            result.put("networkId", web3j.netVersion().send().getNetVersion());

            // 添加合约地址信息
            String contractAddress = blockchainConfig.getContractAddress();
            result.put("contractAddress", contractAddress != null && !contractAddress.isEmpty() ?
                    contractAddress : "未部署");

            // 添加连接状态
            result.put("connected", true);

            return result;
        } catch (Exception e) {
            log.error("Failed to get blockchain status: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            result.put("connected", false);
            return result;
        }
    }

    /**
     * 计算证书哈希
     * @param certificate 证书对象
     * @return 哈希值
     */
    private String calculateCertificateHash(Certificate certificate) {
        // 实际项目中应使用更安全的哈希算法，这里简化处理
        StringBuilder data = new StringBuilder();
        data.append(certificate.getCertificateNo())
                .append(certificate.getName())
                .append(certificate.getIssueDate())
                .append(certificate.getExpireDate())
                .append(certificate.getOrgId())
                .append(certificate.getCertificateTypeId());

        // 使用SHA-256哈希算法
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.toString().getBytes("UTF-8"));

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Failed to calculate certificate hash: {}", e.getMessage());
            throw new RuntimeException("计算证书哈希失败", e);
        }
    }

    /**
     * 将Date转换为时间戳（秒）
     * @param date 日期
     * @return 时间戳
     */
    private BigInteger getTimestampFromDate(Date date) {
        if (date == null) {
            return BigInteger.ZERO;
        }
        long timestamp = TimeUnit.MILLISECONDS.toSeconds(date.getTime());
        return BigInteger.valueOf(timestamp);
    }
}