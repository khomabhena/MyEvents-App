package tech.myevents.app;


class GetSetterAppVersion {

    public GetSetterAppVersion() {
    }

    private int versionCode;
    private String individualPriority, businessPriority;

    public GetSetterAppVersion(int appVersion, String indiPri, String busPri) {
        this.versionCode = appVersion;
        this.individualPriority = indiPri;
        this.businessPriority = busPri;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getIndividualPriority() {
        return individualPriority;
    }

    public void setIndividualPriority(String individualPriority) {
        this.individualPriority = individualPriority;
    }

    public String getBusinessPriority() {
        return businessPriority;
    }

    public void setBusinessPriority(String businessPriority) {
        this.businessPriority = businessPriority;
    }
}
