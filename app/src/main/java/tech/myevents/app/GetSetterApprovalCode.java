package tech.myevents.app;

public class GetSetterApprovalCode {

    public GetSetterApprovalCode() {
    }

    private String type, approvalCode, key;
    private long timestamp;

    public GetSetterApprovalCode(String type, String approvalCode, String key, long timestamp) {
        this.type = type;
        this.approvalCode = approvalCode;
        this.key = key;
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getApprovalCode() {
        return approvalCode;
    }

    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
