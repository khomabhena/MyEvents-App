package tech.myevents.app;

/**
 * Created by user on 1/4/2018.
 */

public class GetSetterPageHits {

    public GetSetterPageHits() {
    }

    private String uid, key, pageName;
    private long timestamp;

    public GetSetterPageHits(String uid, String key, String pageName, long timestamp) {
        this.uid = uid;
        this.key = key;
        this.pageName = pageName;
        this.timestamp = timestamp;
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

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
