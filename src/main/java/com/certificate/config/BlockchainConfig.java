package com.certificate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

@Configuration
public class BlockchainConfig {
    private static final Logger log = LoggerFactory.getLogger(BlockchainConfig.class);

    @Value("${blockchain.ganache.url}")
    private String blockchainUrl;

    @Value("${blockchain.ganache.gasPrice}")
    private String gasPriceStr;

    @Value("${blockchain.ganache.gasLimit}")
    private String gasLimitStr;

    @Value("${blockchain.ganache.privateKey}")
    private String privateKey;

    @Value("${blockchain.contract.address:#{null}}")
    private String contractAddress;

    @Bean
    public Web3j web3j() {
        log.info("初始化Web3j，连接到: {}", blockchainUrl);
        return Web3j.build(new HttpService(blockchainUrl));
    }

    @Bean
    public Credentials credentials() {
        // 移除可能存在的0x前缀
        String privateKeyValue = privateKey;
        if (privateKeyValue.startsWith("0x")) {
            privateKeyValue = privateKeyValue.substring(2);
            log.info("已移除私钥0x前缀");
        }

        try {
            Credentials creds = Credentials.create(privateKeyValue);
            log.info("成功创建凭证，账户地址: {}", creds.getAddress());
            return creds;
        } catch (Exception e) {
            log.error("创建凭证失败: {}", e.getMessage());
            throw e;
        }
    }

    @Bean
    public ContractGasProvider gasProvider() {
        try {
            BigInteger gasPrice = new BigInteger(gasPriceStr);
            BigInteger gasLimit = new BigInteger(gasLimitStr);
            // 增加Gas限制，以防智能合约部署需要更多资源
            BigInteger adjustedGasLimit = gasLimit.multiply(BigInteger.valueOf(5)); // 5倍增加Gas限制
            log.info("配置Gas: 价格={}, 限制={}", gasPrice, adjustedGasLimit);
            return new StaticGasProvider(gasPrice, adjustedGasLimit);
        } catch (Exception e) {
            log.error("配置Gas提供者失败: {}", e.getMessage());
            throw e;
        }
    }

    public String getContractAddress() {
        return contractAddress;
    }
}