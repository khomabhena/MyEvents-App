package tech.myevents.app;

/**
 * Created by user on 13/3/2018.
 */

public class GetSetterVerified {

    public GetSetterVerified() {
    }

    private String uid;
    private boolean verified;

    public GetSetterVerified(String uid, boolean verified) {
        this.uid = uid;
        this.verified = verified;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
