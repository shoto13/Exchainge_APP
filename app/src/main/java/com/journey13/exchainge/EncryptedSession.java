package com.journey13.exchainge;

import android.util.Log;

import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptedSession {

    private enum Operation {ENCRYPT, DECRYPT}

    private SessionCipher mSessionCipher;

    private Operation lastOp;

    private final SignalProtocolAddress protocolAddress;

    private final EncryptedLocalUser localUser;
    private final EncryptedRemoteUser remoteUser;

    public EncryptedSession(EncryptedLocalUser localUser, EncryptedRemoteUser remoteUser) throws UntrustedIdentityException, InvalidKeyException {
        this.protocolAddress = remoteUser.getSignalProtocolAddress();
        this.localUser = localUser;
        this.remoteUser = remoteUser;
        //initSessionFromPreKey();
    }

    private void createSession(Operation operation) throws UntrustedIdentityException, InvalidKeyException {
        if (operation == lastOp) {
            return;
        }

        lastOp = operation;

        InMemorySignalProtocolStore protocolStore = new InMemorySignalProtocolStore(localUser.getIdentityKeyPair(), localUser.getRegistrationId());

        for (PreKeyRecord record : localUser.getPreKeys()) {
            protocolStore.storePreKey(record.getId(), record);
        }

        protocolStore.storeSignedPreKey(localUser.getSignedPreKey().getId(), localUser.getSignedPreKey());

        //Session
        Log.d("Prekey_info", "Here is the preKeyid being passed to the session builder prekeybundle builder " + remoteUser.getPreKeyId());

        SessionBuilder sessionBuilder = new SessionBuilder(protocolStore, remoteUser.getSignalProtocolAddress());
        PreKeyBundle preKeyBundle = new PreKeyBundle(
                remoteUser.getRegistrationId(),
                remoteUser.getSignalProtocolAddress().getDeviceId(),
                remoteUser.getPreKeyId(),
                remoteUser.getPreKeyPublicKey(),
                remoteUser.getSignedPreKeyId(),
                remoteUser.getSignedPreKeyPublicKey(),
                remoteUser.getSignedPreKeySignature(),
                remoteUser.getIdentityKeyPairPublicKey()

        );


        sessionBuilder.process(preKeyBundle);
        mSessionCipher = new SessionCipher(protocolStore, protocolAddress);
    }

    public String encrypt(String message) {
        try {
            createSession(Operation.ENCRYPT);
            CiphertextMessage ciphertextMessage = mSessionCipher.encrypt(message.getBytes());
            PreKeySignalMessage preKeySignalMessage = new PreKeySignalMessage(ciphertextMessage.serialize());
            //return preKeySignalMessage.serialize().toString();
            return Base64.getEncoder().encodeToString(preKeySignalMessage.serialize());
        } catch (UntrustedIdentityException | InvalidKeyException | InvalidVersionException | InvalidMessageException e) {
            return e.getMessage();
        }
    }

// | DuplicateMessageException | InvalidMessageException | InvalidKeyIdException | InvalidVersionException | LegacyMessageException
    public String decrypt(String message) {
        try {
            createSession(Operation.DECRYPT);
            //byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            byte[] bytes = Base64.getDecoder().decode(message);
            //return new String(bytes, StandardCharsets.UTF_8);
            byte[] decryptedMessage = mSessionCipher.decrypt(new PreKeySignalMessage(bytes));
            return new String(decryptedMessage, StandardCharsets.UTF_8);
        } catch (UntrustedIdentityException | InvalidKeyException | DuplicateMessageException | InvalidMessageException | InvalidKeyIdException | InvalidVersionException | LegacyMessageException e) {
            return e.getMessage();
        }
    }
}