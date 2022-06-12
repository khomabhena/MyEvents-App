package tech.myevents.app;


public class GetSetterUser {

    private String uid, username, location, interestCode,
            profileLink, firebaseToken;
    private int whatsAppNumber, contactNumber, signatureVersion;

    public GetSetterUser() {
    }

    public GetSetterUser(String uid, String username, String location, String interestCode,
                         String profileLink, String firebaseToken,
                         int whatsAppNumber, int contactNumber, int signatureVersion) {
        this.uid = uid;
        this.username = username;
        this.location = location;
        this.interestCode = interestCode;
        this.profileLink = profileLink;
        this.firebaseToken = firebaseToken;
        this.whatsAppNumber = whatsAppNumber;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getInterestCode() {
        return interestCode;
    }

    public void setInterestCode(String interestCode) {
        this.interestCode = interestCode;
    }

    public String getProfileLink() {
        return profileLink;
    }

    public void setProfileLink(String profileLink) {
        this.profileLink = profileLink;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public int getWhatsAppNumber() {
        return whatsAppNumber;
    }

    public void setWhatsAppNumber(int whatsAppNumber) {
        this.whatsAppNumber = whatsAppNumber;
    }
}
