package com.certificate.event;

import com.certificate.entity.CertificateBlockchainApplication;
import com.certificate.service.BlockchainApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class CertificateEventListener {

    @Autowired
    private BlockchainApplicationService blockchainApplicationService;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCertificateCreatedEvent(CertificateCreatedEvent event) {
        try {
            CertificateBlockchainApplication application = new CertificateBlockchainApplication();
            application.setCertificateId(event.getCertificateId());
            application.setOrgId(event.getOrgId());
            application.setReason("证书创建后自动申请上链");
            blockchainApplicationService.createApplication(application);
            log.info("证书ID:{} 自动创建上链申请成功", event.getCertificateId());
        } catch (Exception e) {
            log.error("证书ID:{} 自动创建上链申请失败: {}", event.getCertificateId(), e.getMessage());
        }
    }
}