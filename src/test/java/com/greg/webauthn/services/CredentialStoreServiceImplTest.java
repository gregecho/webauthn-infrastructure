package com.greg.webauthn.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.webauthn.model.domain.WebAuthnChallengeRequest;
import com.greg.webauthn.model.domain.WebAuthnCredentials;
import com.greg.webauthn.model.entity.WebAuthnChallengeEntity;
import com.greg.webauthn.model.entity.WebAuthnCredentialEntity;
import com.greg.webauthn.repositories.ChallengeJpaRepository;
import com.greg.webauthn.repositories.CredentialJpaRepository;
import com.greg.webauthn.services.impl.CredentialStoreServiceImpl;
import com.greg.webauthn.utils.WebAuthnChallengeRequestTypeEnum;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialStoreServiceImplTest {

    @Mock private ChallengeJpaRepository  challengeRepo;
    @Mock private CredentialJpaRepository credentialRepo;
    @Mock private ObjectMapper            objectMapper;

    @InjectMocks
    private CredentialStoreServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "challengeTtlSeconds", 300L);
    }

    @Test
    void getCredentialsByMemberId_returnsMappedList() {
        WebAuthnCredentialEntity entity = WebAuthnCredentialEntity.builder()
                .credentialId("cred-id")
                .memberId("member1")
                .publicKeyCose("pk")
                .signatureCount(5)
                .channel("WEB")
                .build();
        when(credentialRepo.findByMemberId("member1")).thenReturn(List.of(entity));

        List<WebAuthnCredentials> result = service.getCredentialsByMemberId("member1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCredentialId()).isEqualTo("cred-id");
        assertThat(result.get(0).getMemberId()).isEqualTo("member1");
    }

    @Test
    void getCredentialsByMemberId_returnsEmptyListWhenNone() {
        when(credentialRepo.findByMemberId("nobody")).thenReturn(List.of());

        assertThat(service.getCredentialsByMemberId("nobody")).isEmpty();
    }

    @Test
    void getCredentialById_returnsNullWhenNotFound() {
        when(credentialRepo.findById("missing")).thenReturn(Optional.empty());

        assertThat(service.getCredentialById("missing")).isNull();
    }

    @Test
    void getCredentialById_returnsMappedEntity() {
        WebAuthnCredentialEntity entity = WebAuthnCredentialEntity.builder()
                .credentialId("cred-id")
                .memberId("member1")
                .publicKeyCose("pk")
                .signatureCount(3)
                .build();
        when(credentialRepo.findById("cred-id")).thenReturn(Optional.of(entity));

        WebAuthnCredentials result = service.getCredentialById("cred-id");

        assertThat(result).isNotNull();
        assertThat(result.getCredentialId()).isEqualTo("cred-id");
    }

    @Test
    void saveCredential_persistsEntity() {
        WebAuthnCredentials cred = WebAuthnCredentials.builder()
                .credentialId("cred-id").memberId("member1")
                .publicKeyCose("pk").count(0).channel("WEB").build();

        service.saveCredential(cred);

        verify(credentialRepo).save(any(WebAuthnCredentialEntity.class));
    }

    @Test
    void deleteChallengeByRequestId_callsDeleteById() {
        service.deleteChallengeByRequestId("req-id");

        verify(challengeRepo).deleteById("req-id");
    }

    @Test
    void getChallengeByRequestId_returnsNullWhenNotFound() {
        when(challengeRepo.findByRequestId("missing")).thenReturn(Optional.empty());

        assertThat(service.getChallengeByRequestId("missing", null)).isNull();
    }

    @Test
    void getChallengeByRequestId_returnsNullForExpiredChallenge() {
        WebAuthnChallengeEntity expired = WebAuthnChallengeEntity.builder()
                .requestId("req-id")
                .memberId("member1")
                .optionsJson("{}")
                .requestType("REGISTRATION")
                .expiresAt(Instant.now().minusSeconds(60))
                .build();
        when(challengeRepo.findByRequestId("req-id")).thenReturn(Optional.of(expired));

        assertThat(service.getChallengeByRequestId("req-id", null)).isNull();
    }

    @Test
    void updateSignatureCount_callsRepository() {
        service.updateSignatureCount("cred-id", 42L);

        verify(credentialRepo).updateSignatureCount("cred-id", 42L);
    }

    @Test
    void deleteCredentialsByMemberId_callsRepository() {
        service.deleteCredentialsByMemberId("member1");

        verify(credentialRepo).deleteByMemberId("member1");
    }
}
