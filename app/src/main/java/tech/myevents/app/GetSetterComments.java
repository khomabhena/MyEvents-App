package tech.myevents.app;

/**
 * Created by user on 27/1/2018.
 */

public class GetSetterComments {

    public GetSetterComments() {
    }

    private String broadcastUid, uid, key, postKey, username, comment, broadcastName, profileLink;
    private int signatureVersion;
    private long timestamp;
    private boolean verified;

    public GetSetterComments(String broadcastUid, String uid, String key, String postKey, String username,
                             String comment, long timestamp, boolean verified,
                             String broadcastName, String profileLink, int signatureVersion) {
        this.broadcastUid = broadcastUid;
        this.uid = uid;
        this.key = key;
        this.postKey = postKey;
        this.username = username;
        this.comment = comment;
        this.timestamp = timestamp;
        this.verified = verified;
        this.broadcastName = broadcastName;
        this.profileLink = profileLink;
        this.signatureVersion = signatureVersion;
    }

    public int getSignatureVersion() {
        return signatureVersion;
    }

    public void setSignatureVersion(int signatureVersion) {
        this.signatureVersion = signatureVersion;
    }

    public String getBroadcastUid() {
        return broadcastUid;
    }

    public void setBroadcastUid(String broadcastUid) {
        this.broadcastUid = broadcastUid;
    }

    public String getBroadcastName() {
        return broadcastName;
    }

    public void setBroadcastName(String broadcastName) {
        this.broadcastName = broadcastName;
    }

    public String getProfileLink() {
        return profileLink;
    }

    public void setProfileLink(String profileLink) {
        this.profileLink = profileLink;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
