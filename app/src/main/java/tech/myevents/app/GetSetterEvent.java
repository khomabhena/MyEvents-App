package tech.myevents.app;

class GetSetterEvent {

    public GetSetterEvent() {
    }

    private int impressions, ticketSeats1, ticketSeats2, signatureVersion;
    private int ticketsPrice1, ticketPrice2, broadcastCharge, merchantCode, availableTickets;
    private long startTimestamp, endTimestamp, broadcastTime, updateTime;
    private String mxeCode, broadcasterUid, brandName, brandLink, eventKey, interestCode, locationCode, name, venue, details,
            profileLink, eventStatus, startDate, startTime,
            ticketName1, ticketName2, merchantName, approvalCode, eventLocation, ticketPromoCode, ecocashType;
    private boolean verified;

    public GetSetterEvent(int impressions,
                          int ticketSeats1, int ticketSeats2, int ticketsPrice1, int ticketPrice2,
                          long startTimestamp, long endTimestamp, long broadcastTime, String broadcasterUid,
                          String brandName, String brandLink, String eventKey, String interestCode, String locationCode, String name, String venue,
                          String details, String profileLink, String eventStatus, String startDate,
                          String startTime, String ticketName1, String ticketName2,
                          int broadcastCharge, int merchantCode, String merchantName,
                          String approvalCode, String eventLocation, String ticketPromoCode,
                          int availableTickets, long updateTime, String ecocashType,
                          String mxeCode, boolean verified, int signatureVersion) {
        this.impressions = impressions;
        this.ticketSeats1 = ticketSeats1;
        this.ticketSeats2 = ticketSeats2;
        this.ticketsPrice1 = ticketsPrice1;
        this.ticketPrice2 = ticketPrice2;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.broadcastTime = broadcastTime;
        this.broadcasterUid = broadcasterUid;
        this.brandName = brandName;
        this.brandLink = brandLink;
        this.eventKey = eventKey;
        this.interestCode = interestCode;
        this.locationCode = locationCode;
        this.name = name;
        this.venue = venue;
        this.details = details;
        this.profileLink = profileLink;
        this.eventStatus = eventStatus;
        this.startDate = startDate;
        this.startTime = startTime;
        this.ticketName1 = ticketName1;
        this.ticketName2 = ticketName2;
        this.broadcastCharge = broadcastCharge;
        this.merchantCode = merchantCode;
        this.merchantName = merchantName;
        this.approvalCode = approvalCode;
        this.eventLocation = eventLocation;
        this.ticketPromoCode = ticketPromoCode;
        this.availableTickets = availableTickets;
        this.updateTime = updateTime;
        this.ecocashType = ecocashType;
        this.mxeCode = mxeCode;
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

    public String getMxeCode() {
        return mxeCode;
    }

    public void setMxeCode(String mxeCode) {
        this.mxeCode = mxeCode;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getBrandLink() {
        return brandLink;
    }

    public void setBrandLink(String brandLink) {
        this.brandLink = brandLink;
    }

    public String getEcocashType() {
        return ecocashType;
    }

    public void setEcocashType(String ecocashType) {
        this.ecocashType = ecocashType;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    public void setAvailableTickets(int availableTickets) {
        this.availableTickets = availableTickets;
    }

    public String getTicketPromoCode() {
        return ticketPromoCode;
    }

    public void setTicketPromoCode(String ticketPromoCode) {
        this.ticketPromoCode = ticketPromoCode;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getApprovalCode() {
        return approvalCode;
    }

    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    public int getImpressions() {
        return impressions;
    }

    public void setImpressions(int impressions) {
        this.impressions = impressions;
    }


    public int getTicketSeats1() {
        return ticketSeats1;
    }

    public void setTicketSeats1(int ticketSeats1) {
        this.ticketSeats1 = ticketSeats1;
    }

    public int getTicketSeats2() {
        return ticketSeats2;
    }

    public void setTicketSeats2(int ticketSeats2) {
        this.ticketSeats2 = ticketSeats2;
    }

    public int getTicketsPrice1() {
        return ticketsPrice1;
    }

    public void setTicketsPrice1(int ticketsPrice1) {
        this.ticketsPrice1 = ticketsPrice1;
    }

    public int getTicketPrice2() {
        return ticketPrice2;
    }

    public void setTicketPrice2(int ticketPrice2) {
        this.ticketPrice2 = ticketPrice2;
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

    public long getBroadcastTime() {
        return broadcastTime;
    }

    public void setBroadcastTime(long broadcastTime) {
        this.broadcastTime = broadcastTime;
    }

    public String getBroadcasterUid() {
        return broadcasterUid;
    }

    public void setBroadcasterUid(String broadcasterUid) {
        this.broadcasterUid = broadcasterUid;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getInterestCode() {
        return interestCode;
    }

    public void setInterestCode(String interestCode) {
        this.interestCode = interestCode;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getProfileLink() {
        return profileLink;
    }

    public void setProfileLink(String profileLink) {
        this.profileLink = profileLink;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getTicketName1() {
        return ticketName1;
    }

    public void setTicketName1(String ticketName1) {
        this.ticketName1 = ticketName1;
    }

    public String getTicketName2() {
        return ticketName2;
    }

    public void setTicketName2(String ticketName2) {
        this.ticketName2 = ticketName2;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

}
