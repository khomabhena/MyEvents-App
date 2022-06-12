package tech.myevents.app;

public class GetSetterUpdate {

    public GetSetterUpdate() {
    }

    private String type, dbId, uid, messageKey, messagePerson;

    public GetSetterUpdate(String type, String dbId, String uid, String senderType, String messagePerson) {
        this.type = type;
        this.dbId = dbId;
        this.uid = uid;
        this.messageKey = senderType;
        this.messagePerson = messagePerson;
    }

    public String getMessagePerson() {
        return messagePerson;
    }

    public void setMessagePerson(String messagePerson) {
        this.messagePerson = messagePerson;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
