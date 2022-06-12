package tech.myevents.app;

/**
 * Created by user on 25/1/2018.
 */

public class GetSetterInbox {

    public GetSetterInbox() {
    }

    private String sendTo, chatRoom, sender, receiver, senderUid, receiverUid,
            message, key, linkSender, linkReceiver, accType;
    private long timestamp;
    private boolean senderVerified, receiverVerified;
    private int signatureVersion;

    public GetSetterInbox(String sendTo, String key, String chatRoom, String sender, String receiver,
                          String senderUid, String receiverUid,
                          String message,
                          long timestamp, boolean verified, boolean receiverVerified,
                          String linkSender, String linkReceiver, int signatureVersion, String accType) {
        this.sendTo = sendTo;
        this.key = key;
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.receiver = receiver;
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.message = message;
        this.timestamp = timestamp;
        this.senderVerified = verified;
        this.receiverVerified = receiverVerified;
        this.linkSender = linkSender;
        this.linkReceiver = linkReceiver;
        this.signatureVersion = signatureVersion;
        this.accType = accType;
    }

    public boolean isReceiverVerified() {
        return receiverVerified;
    }

    public void setReceiverVerified(boolean receiverVerified) {
        this.receiverVerified = receiverVerified;
    }

    public String getAccType() {
        return accType;
    }

    public void setAccType(String accType) {
        this.accType = accType;
    }

    public int getSignatureVersion() {
        return signatureVersion;
    }

    public void setSignatureVersion(int signatureVersion) {
        this.signatureVersion = signatureVersion;
    }

    public String getLinkSender() {
        return linkSender;
    }

    public void setLinkSender(String linkSender) {
        this.linkSender = linkSender;
    }

    public String getLinkReceiver() {
        return linkReceiver;
    }

    public void setLinkReceiver(String linkReceiver) {
        this.linkReceiver = linkReceiver;
    }

    public String getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(String chatRoom) {
        this.chatRoom = chatRoom;
    }

    public boolean isSenderVerified() {
        return senderVerified;
    }

    public void setSenderVerified(boolean senderVerified) {
        this.senderVerified = senderVerified;
    }

    public String getSendTo() {
        return sendTo;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
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

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
