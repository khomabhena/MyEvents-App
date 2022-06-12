package tech.myevents.app;

class GetSetterImpression {

    public GetSetterImpression() {
    }

    private String type, uid, key, username, profileLink, location;
    private boolean verified;
    private long timestamp;
    private int signatureVersion;

    public GetSetterImpression(String type, String uid, String key, String username,
                               String profileLink, String location,
                               boolean verified, long timestamp, int signatureVersion) {
        this.type = type;
        this.uid = uid;
        this.key = key;
        this.username = username;
        this.profileLink = profileLink;
        this.location = location;
        this.verified = verified;
        this.timestamp = timestamp;
        this.signatureVersion = signatureVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSignatureVersion() {
        return signatureVersion;
    }

    public void setSignatureVersion(int signatureVersion) {
        this.signatureVersion = signatureVersion;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileLink() {
        return profileLink;
    }

    public void setProfileLink(String profileLink) {
        this.profileLink = profileLink;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}