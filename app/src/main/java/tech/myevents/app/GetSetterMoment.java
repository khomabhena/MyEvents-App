package tech.myevents.app;

/**
 * Created by user on 8/3/2018.
 */

public class GetSetterMoment {

    public GetSetterMoment() {
    }

    private String key, type, username, story, link, profileLink;
    private long timestamp;
    private int contactNumber, signatureVersion;

    public GetSetterMoment(String key, String type, String username,
                           String story, String link,
                           long timestamp, String profileLink, int contactNumber, int signatureVersion) {
        this.key = key;
        this.type = type;
        this.username = username;
        this.story = story;
        this.link = link;
        this.timestamp = timestamp;
        this.profileLink = profileLink;
        this.contactNumber = contactNumber;
        this.signatureVersion = signatureVersion;
    }

    public int getSignatureVersion() {
        return signatureVersion;
    }

    public void setSignatureVersion(int signatureVersion) {
        this.signatureVersion = signatureVersion;
    }

    public int getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(int contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getProfileLink() {
        return profileLink;
    }

    public void setProfileLink(String profileLink) {
        this.profileLink = profileLink;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
