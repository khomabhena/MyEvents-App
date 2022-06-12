package tech.myevents.app;

public class GetSetterTicket {

    public GetSetterTicket() {
    }

    private String eventKey, uid, broadcasterUid, ticketNumber,
            venue, location, name, category,
            merchantName, ticketKey, mtsCode;
    private int  ticketPrice, id, num;
    private boolean valid;
    private long endTimestamp, startTimestamp;

    public GetSetterTicket(int num, int id, String eventKey, String ticketKey,
                           String uid, String broadcasterUid, String ticketNumber, String venue,
                           String location, String name, String category,
                           String merchantName, int ticketPrice,
                           boolean valid,
                           long endTimestamp, long startTimestamp, String mtsCode) {
        this.num = num;
        this.id = id;
        this.eventKey = eventKey;
        this.ticketKey = ticketKey;
        this.uid = uid;
        this.broadcasterUid = broadcasterUid;
        this.ticketNumber = ticketNumber;
        this.venue = venue;
        this.location = location;
        this.name = name;
        this.category = category;
        this.merchantName = merchantName;
        this.ticketPrice = ticketPrice;
        this.valid = valid;
        this.endTimestamp = endTimestamp;
        this.startTimestamp = startTimestamp;
        this.mtsCode = mtsCode;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBroadcasterUid() {
        return broadcasterUid;
    }

    public void setBroadcasterUid(String broadcasterUid) {
        this.broadcasterUid = broadcasterUid;
    }

    public String getMtsCode() {
        return mtsCode;
    }

    public void setMtsCode(String mtsCode) {
        this.mtsCode = mtsCode;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getTicketKey() {
        return ticketKey;
    }

    public void setTicketKey(String ticketKey) {
        this.ticketKey = ticketKey;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public int getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(int ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

}
