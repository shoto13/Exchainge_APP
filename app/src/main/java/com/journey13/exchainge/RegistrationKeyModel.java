package com.journey13.exchainge;

import com.google.firebase.installations.Utils;
import com.google.gson.Gson;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.nio.charset.Charset;


public class RegistrationKeyModel {

    IdentityKeyPair identityKeyPair;
    int registrationId;
    List<PreKeyRecord> preKeys;
    SignedPreKeyRecord signedPreKeyRecord;

    public RegistrationKeyModel(IdentityKeyPair identityKeyPair, int registrationId, List<PreKeyRecord> preKeys, SignedPreKeyRecord signedPreKeyRecord) {
        this.identityKeyPair = identityKeyPair;
        this.registrationId = registrationId;
        this.preKeys = preKeys;
        this.signedPreKeyRecord = signedPreKeyRecord;
    }

    public RegistrationKeyModel(byte[] identityKeyPair, int registrationId, String[] preKeys, byte[] signedPreKeyRecord) throws InvalidKeyException, IOException {
        System.out.println("BEFORE the identity key pair call, we should ALWAYS see this part");
        this.identityKeyPair = new IdentityKeyPair(identityKeyPair);
        this.registrationId = registrationId;
        List<PreKeyRecord> preKeyRecords = new ArrayList<>();
        for(String item : preKeys) {
            byte[] decoded = Base64.getDecoder().decode(item);
            preKeyRecords.add(new PreKeyRecord(decoded));
        }
        this.preKeys = preKeyRecords;
        this.signedPreKeyRecord = new SignedPreKeyRecord(signedPreKeyRecord);

    }

    public IdentityKeyPair getIdentityKeyPair() {
        return identityKeyPair;
    }

    public String getIdentityKeyPairString() {
        byte[] serialized = identityKeyPair.serialize();
        String identityKeyPairString = Base64.getEncoder().encodeToString(serialized);
        return identityKeyPairString;
    }

    public String getIdentityKeyPublicString() {
        return Base64.getEncoder().encodeToString(identityKeyPair.getPublicKey().serialize());
    }

    public void setIdentityKeyPair(IdentityKeyPair identityKey) {
        this.identityKeyPair = identityKey;
    }

    public int getRegistrationId() {
        System.out.println("The Registration id is: " + registrationId);
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public List<PreKeyRecord> getPreKeys() {
        return preKeys;
    }

    public String getPreKeyIds() {
        List<String> preKeyList = new ArrayList<>();
        for (PreKeyRecord preKey : preKeys) {
            byte[] serialized = preKey.serialize();
            preKeyList.add(Base64.getEncoder().encodeToString(serialized));
        }
        return preKeyList.toString();
    }

    public List<byte[]> getPreKeysAsByteArrays() {
        List<byte[]> preKeyList = new ArrayList<>();
        for (PreKeyRecord preKey : preKeys) {
            byte[] serialized = preKey.serialize();
            preKeyList.add(serialized);
        }
        return preKeyList;
    }

    public PreKeyRecord getPreKey() {
        PreKeyRecord preKeyRec = preKeys.get(0);
        return  preKeyRec;
    }

    public SignedPreKeyRecord getSignedPreKeyRecord() {
        return signedPreKeyRecord;
    }

    public String getSignedPreKeyRecordString() {
        byte[] serialized = signedPreKeyRecord.serialize();
        String signedPreKeyRecordString = Base64.getEncoder().encodeToString(serialized);
        return signedPreKeyRecordString;
    }

    public void setSignedPreKeyRecord(SignedPreKeyRecord signedPreKeyRecord) {
        this.signedPreKeyRecord = signedPreKeyRecord;
    }

    public byte[] getPublicIdentityKey() {
        return identityKeyPair.getPublicKey().serialize();
    }


    public String getSignedPreKeyPublicKey() {
        System.out.println("Here is the signed prekey public key " + signedPreKeyRecord.getKeyPair().getPublicKey());
        return Base64.getEncoder().encodeToString(signedPreKeyRecord.getKeyPair().getPublicKey().serialize());
    }

    public byte[] getSignedPreKeyPublicKeyByteArray() {
        return signedPreKeyRecord.getKeyPair().getPublicKey().serialize();
    }

    public int getSignedPreKeyId() {
        System.out.println("Here is the signed prekeyid " + signedPreKeyRecord.getId());
        return signedPreKeyRecord.getId();
    }

    public String getSignedPreKeyRecordSignature() {
        return Base64.getEncoder().encodeToString(signedPreKeyRecord.getSignature());
    }

    public byte[] getSignedPreKeySignatureByteArray() {
        return signedPreKeyRecord.getSignature();
    }


}
