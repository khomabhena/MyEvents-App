package tech.myevents.app;

/**
 * Created by user on 27/3/2018.
 */

public class GetSetterNotifications {

    public GetSetterNotifications() {
    }

    private String type,key, broadcasterUid, postKey;
    private boolean seen;

    public GetSetterNotifications(String type, String key, String broadcasterUid, String postKey, boolean seen) {
        this.type = type;
        this.key = key;
        this.broadcasterUid = broadcasterUid;
        this.postKey = postKey;
        this.seen = seen;
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

    public String getBroadcasterUid() {
        return broadcasterUid;
    }

    public void setBroadcasterUid(String broadcasterUid) {
        this.broadcasterUid = broadcasterUid;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
