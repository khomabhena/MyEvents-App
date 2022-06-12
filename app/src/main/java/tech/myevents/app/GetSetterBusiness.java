package tech.myevents.app;

/**
 * Created by user on 17/2/2018.
 */

public class GetSetterBusiness {

    public GetSetterBusiness() {
    }

    private String uid, brandName, email, website, description, size,
            location, profileLink, firebaseToken, category, ecocashType, ecocashName;
    private int ecocashCode, signatureVersion;
    private boolean verified;

    public GetSetterBusiness(String uid, String brandName, String email, String website, String description,
                             String size, String location, String profileLink,
                             String firebaseToken, String category, String ecocashType,
                             String  ecocashName, int ecocashCode, boolean verified, int signatureVersion) {
        this.uid = uid;
        this.brandName = brandName;
        this.email = email;
        this.website = website;
        this.description = description;
        this.size = size;
        this.location = location;
        this.profileLink = profileLink;
        this.firebaseToken = firebaseToken;
        this.category = category;
        this.ecocashType = ecocashType;
        this.ecocashName = ecocashName;
        this.ecocashCode = ecocashCode;
        this.verified = verified;
        this.signatureVersion = signatureVersion;
    }

    public int getSignatureVersion() {
        return signatureVersion;
    }

    public void setSignatureVersion(int signatureVersion) {
        this.signatureVersion = signatureVersion;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getEcocashType() {
        return ecocashType;
    }

    public void setEcocashType(String ecocashType) {
        this.ecocashType = ecocashType;
    }

    public String getEcocashName() {
        return ecocashName;
    }

    public void setEcocashName(String ecocashName) {
        this.ecocashName = ecocashName;
    }

    public int getEcocashCode() {
        return ecocashCode;
    }

    public void setEcocashCode(int ecocashCode) {
        this.ecocashCode = ecocashCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
}
