package com.greg.webauthn.repositories;

import com.greg.webauthn.model.domain.WebAuthnCredentials;
import com.greg.webauthn.services.ICredentialStoreService;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebAuthnCredentialRepositoryTest {

    @Mock  private ICredentialStoreService store;
    @InjectMocks private WebAuthnCredentialRepository repo;

    // Sample Base64URL-encoded credential ID (43 chars, no padding)
    private static final String CRED_ID_B64 = "dGVzdC1jcmVkZW50aWFsLWlkLXBhZA==".replace("=","")
            // Use a proper 43-char base64url value
            .substring(0);

    @Test
    void getUserHandleForUsername_returnsEncodedUsername() throws Exception {
        Optional<ByteArray> handle = repo.getUserHandleForUsername("member1");
        assertThat(handle).isPresent();
        assertThat(new String(handle.get().getBytes())).isEqualTo("member1");
    }

    @Test
    void getUsernameForUserHandle_decodesCorrectly() throws Exception {
        ByteArray handle = new ByteArray("member1".getBytes());
        Optional<String> username = repo.getUsernameForUserHandle(handle);
        assertThat(username).contains("member1");
    }

    @Test
    void getCredentialIdsForUsername_returnsEmptyWhenNone() {
        when(store.getCredentialsByMemberId("member1")).thenReturn(List.of());
        Set<PublicKeyCredentialDescriptor> result = repo.getCredentialIdsForUsername("member1");
        assertThat(result).isEmpty();
    }

    @Test
    void lookupAll_returnsEmptyWhenCredentialNotFound() throws Exception {
        ByteArray credId = new ByteArray("credId123".getBytes());
        when(store.getCredentialById(credId.getBase64Url())).thenReturn(null);

        Set<RegisteredCredential> result = repo.lookupAll(credId);
        assertThat(result).isEmpty();
    }

    @Test
    void lookup_delegatesToLookupAll() throws Exception {
        ByteArray credId     = new ByteArray("credId123".getBytes());
        ByteArray userHandle = new ByteArray("member1".getBytes());
        when(store.getCredentialById(credId.getBase64Url())).thenReturn(null);

        Optional<RegisteredCredential> result = repo.lookup(credId, userHandle);
        assertThat(result).isEmpty();
    }
}
