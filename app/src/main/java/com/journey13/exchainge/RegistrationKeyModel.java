package com.journey13.exchainge;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

import java.io.IOException;
import java.util.List;

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
        //this.identityKeyPair = new IdentityKeyPair(KeyUtils);

    }



}
