package com.journey13.exchainge;

public class LocalAndRemoteUserModel {
    public EncryptedRemoteUser encryptedRemoteUser;
    public EncryptedLocalUser encryptedLocalUser;


    //Model to build the remote and local user into a single class
    public LocalAndRemoteUserModel(EncryptedRemoteUser encryptedRemoteUser, EncryptedLocalUser encryptedLocalUser) {
        this.encryptedRemoteUser = encryptedRemoteUser;
        this.encryptedLocalUser = encryptedLocalUser;
    }

    public LocalAndRemoteUserModel() {
        this.encryptedRemoteUser = null;
        this.encryptedLocalUser = null;
    }

    public EncryptedRemoteUser getEncryptedRemoteUser() {
        return encryptedRemoteUser;
    }

    public EncryptedLocalUser getEncryptedLocalUser() {
        return encryptedLocalUser;
    }

    public void setEncryptedRemoteUser(EncryptedRemoteUser encryptedRemoteUser) {
        this.encryptedRemoteUser = encryptedRemoteUser;
    }

    public void setEncryptedLocalUser(EncryptedLocalUser encryptedLocalUser) {
        this.encryptedLocalUser = encryptedLocalUser;
    }
}
