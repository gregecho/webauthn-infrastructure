package com.greg.webauthn.repositories;

import com.greg.webauthn.model.entity.WebAuthnCredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CredentialJpaRepository extends JpaRepository<WebAuthnCredentialEntity, String> {

    List<WebAuthnCredentialEntity> findByMemberId(String memberId);

    @Modifying
    @Transactional
    @Query("DELETE FROM WebAuthnCredentialEntity c WHERE c.memberId = :memberId")
    void deleteByMemberId(String memberId);

    @Modifying
    @Transactional
    @Query("UPDATE WebAuthnCredentialEntity c SET c.signatureCount = :count WHERE c.credentialId = :credentialId")
    void updateSignatureCount(String credentialId, long count);
}
