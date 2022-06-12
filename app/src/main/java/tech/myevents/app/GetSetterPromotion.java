package tech.myevents.app;

class GetSetterPromotion {

    public GetSetterPromotion() {
    }

    private String ticketCode, eventPaymentCode, adPaymentCode;

    private long ticketValidUntil, eventValidUntil,  adValidUntil;

    public GetSetterPromotion(String ticketCode, String eventPaymentCode, String adPaymentCode,
                              long ticketValidUntil, long eventValidUntil, long adValidUntil) {
        this.ticketCode = ticketCode;
        this.eventPaymentCode = eventPaymentCode;
        this.adPaymentCode = adPaymentCode;
        this.ticketValidUntil = ticketValidUntil;
        this.eventValidUntil = eventValidUntil;
        this.adValidUntil = adValidUntil;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getEventPaymentCode() {
        return eventPaymentCode;
    }

    public void setEventPaymentCode(String eventPaymentCode) {
        this.eventPaymentCode = eventPaymentCode;
    }

    public String getAdPaymentCode() {
        return adPaymentCode;
    }

    public void setAdPaymentCode(String adPaymentCode) {
        this.adPaymentCode = adPaymentCode;
    }

    public long getTicketValidUntil() {
        return ticketValidUntil;
    }

    public void setTicketValidUntil(long ticketValidUntil) {
        this.ticketValidUntil = ticketValidUntil;
    }

    public long getEventValidUntil() {
        return eventValidUntil;
    }

    public void setEventValidUntil(long eventValidUntil) {
        this.eventValidUntil = eventValidUntil;
    }

    public long getAdValidUntil() {
        return adValidUntil;
    }

    public void setAdValidUntil(long adValidUntil) {
        this.adValidUntil = adValidUntil;
    }
}
