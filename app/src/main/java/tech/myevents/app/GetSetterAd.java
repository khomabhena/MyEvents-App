package tech.myevents.app;

class GetSetterAd {

    public GetSetterAd() {
    }

    private int impressions, clicksIndividual, clicksBusiness, broadcastCharge, merchantCode, signatureVersion;
    private String adKey, broadcasterUid, brandName, brandLink, title, details, profileLink, merchantName;
    private String locationCode, interestCode, status, approvalCode, adLocation;
    private long duration, broadcastTime, updateTime;
    private boolean verified;

    public GetSetterAd(String broadcasterUid, String adKey, int impressions, int clicksIndividual, int clicksBusiness,
                       String brandName, String brandLink, String title, String details, String profileLink,
                       String locationCode, String interestCode, String status,
                       long duration, long broadcastTime,
                       int broadcastCharge, int merchantCode, String merchantName, String approvalCode, String adLocation,
                       long updateTime, boolean verified, int signatureVersion) {
        this.broadcasterUid = broadcasterUid;
        this.adKey = adKey;
        this.impressions = impressions;
        this.clicksIndividual = clicksIndividual;
        this.clicksBusiness = clicksBusiness;
        this.brandName = brandName;
        this.brandLink = brandLink;
        this.title = title;
        this.details = details;
        this.profileLink = profileLink;
        this.locationCode = locationCode;
        this.interestCode = interestCode;
        this.status = status;
        this.duration = duration;
        this.broadcastTime = broadcastTime;
        this.broadcastCharge = broadcastCharge;
        this.merchantCode = merchantCode;
        this.merchantName = merchantName;
        this.approvalCode = approvalCode;
        this.adLocation = adLocation;
        this.updateTime = updateTime;
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

    public String getBrandLink() {
        return brandLink;
    }

    public void setBrandLink(String brandLink) {
        this.brandLink = brandLink;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getAdLocation() {
        return adLocation;
    }

    public void setAdLocation(String adLocation) {
        this.adLocation = adLocation;
    }

    public String getApprovalCode() {
        return approvalCode;
    }

    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    public String getProfileLink() {
        return profileLink;
    }

    public void setProfileLink(String profileLink) {
        this.profileLink = profileLink;
    }

    public int getClicksIndividual() {
        return clicksIndividual;
    }

    public void setClicksIndividual(int clicksIndividual) {
        this.clicksIndividual = clicksIndividual;
    }

    public int getClicksBusiness() {
        return clicksBusiness;
    }

    public void setClicksBusiness(int clicksBusiness) {
        this.clicksBusiness = clicksBusiness;
    }

    public int getBroadcastCharge() {
        return broadcastCharge;
    }

    public void setBroadcastCharge(int broadcastCharge) {
        this.broadcastCharge = broadcastCharge;
    }

    public int getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(int merchantCode) {
        this.merchantCode = merchantCode;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getBroadcasterUid() {
        return broadcasterUid;
    }

    public void setBroadcasterUid(String broadcasterUid) {
        this.broadcasterUid = broadcasterUid;
    }

    public String getAdKey() {
        return adKey;
    }

    public void setAdKey(String adKey) {
        this.adKey = adKey;
    }

    public int getImpressions() {
        return impressions;
    }

    public void setImpressions(int impressions) {
        this.impressions = impressions;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getInterestCode() {
        return interestCode;
    }

    public void setInterestCode(String interestCode) {
        this.interestCode = interestCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getBroadcastTime() {
        return broadcastTime;
    }

    public void setBroadcastTime(long broadcastTime) {
        this.broadcastTime = broadcastTime;
    }
}
