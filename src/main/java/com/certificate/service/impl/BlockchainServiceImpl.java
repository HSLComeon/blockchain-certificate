package com.certificate.service.impl;

import com.certificate.entity.Certificate;
import com.certificate.service.BlockchainService;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 区块链服务实现类
 * 注：这是一个模拟实现，实际项目中需要集成真实的区块链
 */
@Service
public class BlockchainServiceImpl implements BlockchainService {

    @Override
    public String uploadToBlockchain(Certificate certificate) {
        // 模拟上链操作
        try {
            // 模拟上链耗时
            Thread.sleep(1000);

            // 生成一个随机的交易哈希
            String txHash = "0x" + UUID.randomUUID().toString().replace("-", "");
            System.out.println("证书 " + certificate.getCertificateNo() + " 上链成功，交易哈希: " + txHash);

            return txHash;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("上链操作被中断", e);
        } catch (Exception e) {
            throw new RuntimeException("上链失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyCertificate(String certNo, String hash) {
        // 模拟验证操作
        try {
            // 模拟验证耗时
            Thread.sleep(500);

            // 简单模拟：证书编号末尾为偶数则验证通过
            boolean result = certNo.hashCode() % 2 == 0;
            System.out.println("证书 " + certNo + " 验证" + (result ? "通过" : "失败"));

            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("验证操作被中断", e);
        } catch (Exception e) {
            throw new RuntimeException("验证失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String revokeCertificate(Certificate certificate) {
        // 模拟撤销操作
        try {
            // 模拟撤销耗时
            Thread.sleep(800);

            // 生成一个随机的交易哈希
            String txHash = "0x" + UUID.randomUUID().toString().replace("-", "");
            System.out.println("证书 " + certificate.getCertificateNo() + " 撤销成功，交易哈希: " + txHash);

            return txHash;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("撤销操作被中断", e);
        } catch (Exception e) {
            throw new RuntimeException("撤销失败: " + e.getMessage(), e);
        }
    }
}