package tech.myevents.app;

/**
 * Created by user on 14/3/2018.
 */

public class GetSetterVerifyContact {

    public GetSetterVerifyContact() {
    }

    private String verificationCode, key;
    private int phoneNumber;
    private boolean verified, verificationSent;

    public GetSetterVerifyContact(String key, String verificationCode, int phoneNumber, boolean verified, boolean verificationSent) {
        this.key = key;
        this.verificationCode = verificationCode;
        this.phoneNumber = phoneNumber;
        this.verified = verified;
        this.verificationSent = verificationSent;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isVerificationSent() {
        return verificationSent;
    }

    public void setVerificationSent(boolean verificationSent) {
        this.verificationSent = verificationSent;
    }
}
