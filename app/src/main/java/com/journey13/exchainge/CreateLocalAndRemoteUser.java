package com.journey13.exchainge;

public class CreateLocalAndRemoteUser {
    public EncryptedRemoteUser encryptedRemoteUser;
    public EncryptedLocalUser encryptedLocalUser;

    public CreateLocalAndRemoteUser(EncryptedRemoteUser encryptedRemoteUser, EncryptedLocalUser encryptedLocalUser) {
        this.encryptedRemoteUser = encryptedRemoteUser;
        this.encryptedLocalUser = encryptedLocalUser;
    }

    public CreateLocalAndRemoteUser() {
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
