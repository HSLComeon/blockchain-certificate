package com.certificate.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CertificateCreatedEvent extends ApplicationEvent {
    private final Long certificateId;
    private final Long orgId;

    public CertificateCreatedEvent(Long certificateId, Long orgId) {
        super(certificateId);
        this.certificateId = certificateId;
        this.orgId = orgId;
    }
}