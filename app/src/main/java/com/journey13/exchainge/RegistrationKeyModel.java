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

    public RegistrationKeyModel(String identityKeyPair, int registrationId, String[] preKeys, String signedPreKeyRecord) throws InvalidKeyException, IOException {
        this.identityKeyPair = new IdentityKeyPair(identityKeyPair.getBytes(StandardCharsets.UTF_8));
        this.registrationId = registrationId;
        List<PreKeyRecord> preKeyRecords = new ArrayList<>();
        for(String item : preKeys) {
            preKeyRecords.add(new PreKeyRecord(item.getBytes(StandardCharsets.UTF_8)));
        }
        this.preKeys = preKeyRecords;
        this.signedPreKeyRecord = new SignedPreKeyRecord(signedPreKeyRecord.getBytes(StandardCharsets.UTF_8));
    }

    public IdentityKeyPair getIdentityKeyPair() {
        return identityKeyPair;
    }

    public String getIdentityKeyPairString() {
        System.out.println("Oh shit oh fuck here we bloody go again! ");
        return Base64.getEncoder().encodeToString(identityKeyPair.serialize());
    }

    public String getIdentityKeyPublicString() {
        return Base64.getEncoder().encodeToString(identityKeyPair.getPublicKey().serialize());
    }

    public void setIdentityKeyPair(IdentityKeyPair identityKey) {
        this.identityKeyPair = identityKey;
    }

    public int getRegistrationId() {
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
            preKeyList.add(Base64.getEncoder().encodeToString(preKey.serialize()));
        }
        return new Gson().toJson(preKeyList);
    }

    public SignedPreKeyRecord getSignedPreKeyRecord() {
        return signedPreKeyRecord;
    }

    public String getSignedPreKeyRecordString() {
        return Base64.getEncoder().encodeToString(signedPreKeyRecord.serialize());
    }

    public void setSignedPreKeyRecord(SignedPreKeyRecord signedPreKeyRecord) {
        this.signedPreKeyRecord = signedPreKeyRecord;
    }

    public String getPublicIdentityKey() {
        return Base64.getEncoder().encodeToString(identityKeyPair.getPublicKey().serialize());
    }

    public String getSignedPreKeyPublicKey() {
        return Base64.getEncoder().encodeToString(signedPreKeyRecord.getKeyPair().getPublicKey().serialize());
    }

    public int getSignedPreKeyId() {
        return signedPreKeyRecord.getId();
    }

    public String getSignedPreKeyRecordSignature() {
        return Base64.getEncoder().encodeToString(signedPreKeyRecord.getSignature());
    }

//    IdentityKeyPair identityKeyPair;
//    int registrationId;
//    List<PreKeyRecord> preKeys;
//    SignedPreKeyRecord signedPreKeyRecord;
//
//    public RegistrationKeyModel(IdentityKeyPair identityKeyPair, int registrationId, List<PreKeyRecord> preKeys, SignedPreKeyRecord signedPreKeyRecord) {
//        this.identityKeyPair = identityKeyPair;
//        this.registrationId = registrationId;
//        this.preKeys = preKeys;
//        this.signedPreKeyRecord = signedPreKeyRecord;
//    }
//
//    public RegistrationKeyModel(String identityKeyPair, int registrationId, String[] preKeys, String signedPreKeyRecord) throws InvalidKeyException, IOException {
//        this.identityKeyPair = new IdentityKeyPair(identityKeyPair.getBytes(StandardCharsets.UTF_8));
//        this.registrationId = registrationId;
//        List<PreKeyRecord> preKeyRecords = new ArrayList<>();
//        for (String item : preKeys) {
//            preKeyRecords.add(new PreKeyRecord(item.getBytes(StandardCharsets.UTF_8)));
//        }
//        this.preKeys = preKeyRecords;
//        this.signedPreKeyRecord = new SignedPreKeyRecord(signedPreKeyRecord.getBytes(StandardCharsets.UTF_8));
//    }
//
//    public IdentityKeyPair getIdentityKeyPair() {
//        return identityKeyPair;
//    }
//
//    public String getIdentityKeyPairString() {
//        return Base64.getEncoder().encodeToString(identityKeyPair.serialize());
//    }
//
//    public String getIdentityKeyPublicString() {
//        return Base64.getEncoder().encodeToString(identityKeyPair.getPublicKey().serialize());
//    }
//
//    public void setIdentityKeyPair (IdentityKeyPair identityKey) {
//        this.identityKeyPair = identityKey;
//    }
//
//    public int getRegistrationId() {
//        return registrationId;
//    }
//
//    public void setRegistrationId(int registrationId) {
//        this.registrationId = registrationId;
//    }
//
//    public List<PreKeyRecord> getPreKeys() {
//        return preKeys;
//    }
//
//    public String getPreKeyIds() {
//        List<String> preKeyList = new ArrayList<>();
//        for (PreKeyRecord preKey : preKeys) {
//            preKeyList.add(Base64.getEncoder().encodeToString(preKey.serialize()));
//        }
//        return new Gson().toJson(preKeyList);
//    }
//
//    public SignedPreKeyRecord getSignedPreKeyRecord() {
//        return signedPreKeyRecord;
//    }
//
//    public String getSignedPreKeyRecordString() {
//        return Base64.getEncoder().encodeToString(signedPreKeyRecord.serialize());
//    }
//
//    public void setSignedPreKeyRecord(SignedPreKeyRecord signedPreKeyRecord) {
//        this.signedPreKeyRecord = signedPreKeyRecord;
//    }
//
//    public String getPublicIdentityKey() {
//        return Base64.getEncoder().encodeToString(identityKeyPair.getPublicKey().serialize());
//    }
//
//    public String getSignedPreKeyPublicKey() {
//        return Base64.getEncoder().encodeToString(signedPreKeyRecord.getKeyPair().getPublicKey().serialize());
//    }
//
//    public int getSignedPreKeyId() {
//        return signedPreKeyRecord.getId();
//    }
//
//    public String getSignedPreKeyRecordSignature() {
//        return Base64.getEncoder().encodeToString(signedPreKeyRecord.getSignature());
//    }
//
//
//
//


}
