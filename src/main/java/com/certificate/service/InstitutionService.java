package com.certificate.service;

import com.certificate.dto.InstitutionLoginDTO;
import com.certificate.dto.InstitutionRegisterDTO;
import com.certificate.entity.Institution;

import java.util.Map;

public interface InstitutionService {
    Map<String, String> login(InstitutionLoginDTO institutionLoginDTO);

    void register(InstitutionRegisterDTO institutionRegisterDTO);

    Map<String, Object> getInfo();

    Institution getInstitutionByUsername(String username);
}