package com.certificate.config;

import com.certificate.service.BlockchainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 区块链初始化器，用于自动部署合约
 */
@Component
@Slf4j
public class BlockchainInitializer implements ApplicationRunner {

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        String contractAddress = environment.getProperty("blockchain.contract.address");

        if (contractAddress == null || contractAddress.isEmpty() || "CONTRACT_ADDRESS_AFTER_DEPLOYMENT".equals(contractAddress)) {
            try {
                log.info("No contract address found, deploying new contract...");
                String newAddress = blockchainService.deployContract();
                log.info("Contract deployed successfully at: {}", newAddress);

                // 可以保存合约地址到配置文件或数据库
                saveContractAddress(newAddress);
            } catch (Exception e) {
                log.error("Failed to deploy contract: {}", e.getMessage());
            }
        } else {
            log.info("Using existing contract at: {}", contractAddress);
        }
    }

    private void saveContractAddress(String address) {
        // 保存到数据库或配置文件的实现
        log.info("Contract address saved: {}", address);
        // 这里可以添加持久化逻辑，例如写入数据库
    }
}