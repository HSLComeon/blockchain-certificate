// SPDX-License-Identifier: MIT
pragma solidity ^0.8.9;

contract CertificateContract {
    struct Certificate {
        bytes32 hash;      // 使用bytes32代替string存储哈希
        uint256 issueDate;
        bool isRevoked;
    }

    // 证书编号映射到证书数据，使用bytes32代替string作为键
    mapping(bytes32 => Certificate) public certificates;

    // 记录证书数量
    uint256 public certificateCount;

    // 事件定义
    event CertificateIssued(bytes32 certificateNo, bytes32 hash, uint256 issueDate);
    event CertificateRevoked(bytes32 certificateNo);

    // 添加证书
    function issueCertificate(
        bytes32 _certificateNo,
        bytes32 _hash,
        uint256 _issueDate
    ) public {
        require(certificates[_certificateNo].issueDate == 0, "Certificate already exists");

        certificates[_certificateNo] = Certificate({
            hash: _hash,
            issueDate: _issueDate,
            isRevoked: false
        });

        certificateCount++;

        emit CertificateIssued(_certificateNo, _hash, _issueDate);
    }

    // 验证证书
    function verifyCertificate(bytes32 _certificateNo) public view returns (bool exists, bool isRevoked, uint256 issueDate) {
        Certificate memory cert = certificates[_certificateNo];

        exists = cert.issueDate > 0;

        return (exists, cert.isRevoked, cert.issueDate);
    }

    // 撤销证书
    function revokeCertificate(bytes32 _certificateNo) public {
        require(certificates[_certificateNo].issueDate > 0, "Certificate does not exist");
        require(!certificates[_certificateNo].isRevoked, "Certificate already revoked");

        certificates[_certificateNo].isRevoked = true;

        emit CertificateRevoked(_certificateNo);
    }

    // 获取证书总数
    function getCertificateCount() public view returns (uint256) {
        return certificateCount;
    }
}