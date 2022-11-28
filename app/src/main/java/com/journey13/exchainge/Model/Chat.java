package com.journey13.exchainge.Model;

import java.util.UUID;

public class Chat {

    private String sender;
    private String receiver;
    private String message;
    private boolean isSeen;
    private String messageTimestamp;
    private String UMID;

    public Chat(String sender, String receiver, String message, boolean isSeen, String messageTimestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isSeen = isSeen;
        this.messageTimestamp = messageTimestamp;
        this.UMID = UUID.randomUUID().toString();
    }

    public Chat(String sender, String receiver, String message, boolean isSeen, String messageTimestamp, String UMID) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isSeen = isSeen;
        this.messageTimestamp = messageTimestamp;
        this.UMID = UMID;
    }

    public Chat() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getMessageTimestamp() {
        return messageTimestamp;
    }

    public void setMessageTimestamp(String messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }

    public String getUMID() {
        return UMID;
    }
}
