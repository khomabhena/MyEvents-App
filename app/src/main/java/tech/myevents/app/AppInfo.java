package tech.myevents.app;

import com.google.firebase.auth.FirebaseAuth;

final class AppInfo {

    public AppInfo() {
    }


    static final String USER_PHONE_NUMEBER = "tech.myevents.app.USerPhone.Numberz";

    static final String APP_INFO = "tech.myevents.app.AppInfo";
    static final String ACCOUNT_TYPE = "tech.myevents.app.AccountTypeApp";
    static final String ACTIVE_USER = "tech.myevents.app.activeUser";

    static final String USER_INFO = "tech.myevents.app.UserInfo";
    static final String FIREBASE_TOKEN = "tech.myevents.app.firebaseToken";
    static final String FIREBASE_TOKEN_REFRESH = "tech.myevents.app.firebaseTokenRefresh";
    static final String INTEREST_CODE = "tech.myevents.app.userInterests";
    static final String RECEIVE_SMS_NOTIFICATIONS = "tech.myevents.app.receiveSmsNotification";
    static final String WHATSAPP_PHONE_NUMBER = "tech.myevents.app.smsPhoneNumber";
    static final String GET_WELCOME_MESSAGE = "tech.myevents.app.GotWelcomeMessages.";
    static final String GET_ADS_AND_EVENTS = "tech.myevents.app.GetAdsAndEvents";
    static final String GET_VERSION_CODE = "tech.myevents.app.GetVersionCode";
    static final String GET_VERSION_CODE_MEDIUM = "tech.myevents.app.GetVersionCodeMedium";

    static final String MTS_TICKET_SIGNING = "tech.myevents.app.JoinTicketSigningH";
    static final String MTS_SIGNING_UID = "tech.myevents.app.TicketSigningUidXI";
    static final String MTS_ENDTIMESTAMP = "tech.myevents.app.MTSEndTimestamp.OL";

    static final String USER_DAILY_UPDATE = "tech.myevents.app.UserDailyUpdate";
    static final String TICKET_BUYING_TEXT = "tech.myevents.app.TicketBuyingText";
    static final String FREE_TICKETS = "tech.myevents.app.FreeTickets.me";
    static final String CONTACT_VERIFICATION_CODE = "tech.myevents.app.ContactVerificationCOdex";
    static final String CONTACT_VERIFIED = "tech.myevents.app.ContactVerifiedYini";
    static final String CONTACT_NUMBER = "tech.myevents.app.UserContactNumberYakhe";
    static final String CONTACT_LIST = "tech.myevents.app.Contact.List";
    static final String MOMENT_LINK = "tech.myevents.app.Moment.Link";
    static final String SIG_VER_INDI = "tech.myeevnts.app.SigVerIndi";
    static final String SIG_VER_BUS = "tech.myeevnts.app.SigVerBus";


    static final String HQ_STATS_UPDATE = "tech.myevents.app.HQStatsUpdate";
    static final String HQ_USERS = "tech.myevents.app.HQUsers";
    static final String HQ_DAILY_USERS = "tech.myevents.app.HQDailyUsers";
    static final String HQ_WEEKLY_USERS = "tech.myevents.app.HQWeeklyUsers";
    static final String HQ_MONTHLY_USERS = "tech.myevents.app.HQMonthlyUsers";
    static final String HQ_CONTACTS_RETRIEVED = "tech.myevents.app.HQContactsRetrieved";
    static final String HQ_USER_INVITATIONS = "tech.myevents.app.HQUserInvitations";
    static final String HQ_APP_INVITIONS = "tech.myevents.app.HQAppInvitations";
    static final String HQ_EVENT_BROADCASTS = "tech.myevents.app.HQEventBroadcasts";
    static final String HQ_AD_BROADCASTS = "tech.myevents.app.HQAdBroadcasts";
    static final String HQ_EVENT_BROADCASTERS = "tech.myevents.app.HQEventBroadcasters";
    static final String HQ_AD_BROADCASTERS = "tech.myevents.app.HQAdBroadcasters";



    static final String BUSINESS_CATEGORY = "tech.myEvents.tech.BusinessCategory2";
    static final String BUSINESS_BRAND_NAME = "tech.myevents.app.businessBrandName";
    static final String BUSINESS_LOCATION = "tech.myevents.app.businessLocation";
    static final String BUSINESS_EMAIL = "tech.myevents.app.businessEmail";
    static final String BUSINESS_DESCRIPTION = "tech.myevents.app.businessDescription";
    static final String BUSINESS_PROFILE_LINK = "tech.myevents.app.businessProfileLink";
    static final String BUSINESS_SIZE = "tech.myevents.app.businessSize";
    static final String BUSINESS_WEBSITE = "tech.myevents.app.businessWebsite";
    static final String BUSINESS_ECOCASH_TYPE = "tech.myevents.app.businessEcocash.Type";
    static final String BUSINESS_ECOCASH_CODE = "tech.myevents.app.business.Ecocash.TYpe";
    static final String BUSINESS_ECOCASH_NAME = "tech.myevents.app.business.Ecocash.Namez";
    static final String BUSINESS_VERIFIED= "tech.myevents.app.Businessz.Verified";




    static final String INDIVIDUAL_USERNAME = "tech.myevents.app.individualUsername";
    static final String INDIVIDUAL_LOCATION = "tech.myevents.app.individualLocation";
    static final String INDIVIDUAL_PROFILE_LINK = "tech.myevents.app.individualProfileLink";




    static final String EVENT_LOCATION = "tech.myevents.app.eventsLocation";
    static final String EVENT_NAME = "tech.myevents.app.eventName";
    static final String EVENT_VENUE = "tech.myevents.app.eventVenue";
    static final String EVENT_DETAILS = "tech.myevents.app.eventDetails";
    static final String EVENT_TICKET_NAME_1 = "tech.myevents.app.eventTicketName1";
    static final String EVENT_TICKET_PRICE_1 = "tech.myevents.app.eventTicketPrice1";
    static final String EVENT_TICKET_SEATS_1 = "tech.myevents.app.eventTicketSeats1";
    static final String EVENT_TICKET_NAME_2 = "tech.myevents.app.eventTicketName2";
    static final String EVENT_TICKET_PRICE_2 = "tech.myevents.app.eventTicketPrice2";
    static final String EVENT_TICKET_SEATS_2 = "tech.myevents.app.eventTicketSeats2";
    static final String EVENT_ECOCASH_NUMBER = "tech.myevents.app.eventEcocashNumber";
    static final String EVENT_AUDIENCE = "tech.myevents.app.eventAudience";
    static final String EVENT_COST = "tech.myevents.app.eventCost";
    static final String EVENT_PROFILE_LINK = "tech.myevents.app.eventProfileLink";
    static final String EVENT_INTEREST = "tech.myevents.app.eventInterest";
    static final String EVENT_INTEREST_CODE = "tech.myevents.app.eventInterestCode";
    static final String EVENT_BROADCAST_RANGE = "tech.myevents.app.eventBroadcastRange";
    static final String EVENT_BROADCAST_RANGE_CODE = "tech.myevents.app.eventBroadcastRangeCode";
    static final String EVENT_START_TIME = "tech.myevents.app.eventStartTime";
    static final String EVENT_START_DATE = "tech.myevents.app.eventStartDate";
    static final String EVENT_END_TIME = "tech.myevents.app.eventEndTime";
    static final String EVENT_END_DATE = "tech.myevents.app.eventEndDate";
    static final String EVENT_START_TIMESTAMP = "tech.myevents.app.eventStartTimestamp";
    static final String EVENT_END_TIMESTAMP = "tech.myevents.app.eventEndTimestamp";
    static final String EVENT_TICKET_SWITCH = "tech.myevents.app.eventTicketSwitch";
    static final String EVENT_BROADCAST_PAYMENT = "tech.myevents.app.eventBroadcastPayment";
    static final String EVENT_ECOCACH_MERCHANT_CODE = "tech.myevents.app.eventEcocashMerchantCode";
    static final String EVENT_ECOCASH_MERCHANT_NAME = "tech.myevents.app.eventEcocashMerchantName";
    static final String EVENT_BROADCASTS_MADE = "tech.myevents.app.eventsBroadcastsMade";
    static final String EVENT_APPROVAL_CODE = "tech.myevents.app.eventApprovalCode";
    static final String EVENT_AVAILABLE_TICKETS = "tech,myevents.app.EventAvailableTickets";


    static final String EVENT_LOCATION_EX = "tech.myevents.app.eventsLocationEx";
    static final String EVENT_NAME_EX = "tech.myevents.app.eventNameEx";
    static final String EVENT_VENUE_EX = "tech.myevents.app.eventVenueEx";
    static final String EVENT_DETAILS_EX = "tech.myevents.app.eventDetailsEx";
    static final String EVENT_TICKET_NAME_1_EX = "tech.myevents.app.eventTicketName1Ex";
    static final String EVENT_TICKET_PRICE_1_EX = "tech.myevents.app.eventTicketPrice1Ex";
    static final String EVENT_TICKET_SEATS_1_EX = "tech.myevents.app.eventTicketSeats1Ex";
    static final String EVENT_TICKET_NAME_2_EX = "tech.myevents.app.eventTicketName2Ex";
    static final String EVENT_TICKET_PRICE_2_EX = "tech.myevents.app.eventTicketPrice2Ex";
    static final String EVENT_TICKET_SEATS_2_EX = "tech.myevents.app.eventTicketSeats2Ex";
    static final String EVENT_ECOCASH_NUMBER_EX = "tech.myevents.app.eventEcocashNumberEx";
    static final String EVENT_AUDIENCE_EX = "tech.myevents.app.eventAudienceEx";
    static final String EVENT_COST_EX = "tech.myevents.app.eventCostEx";
    static final String EVENT_PROFILE_LINK_EX = "tech.myevents.app.eventProfileLinkEx";
    static final String EVENT_INTEREST_EX = "tech.myevents.app.eventInterestEx";
    static final String EVENT_INTEREST_CODE_EX = "tech.myevents.app.eventInterestCodeEx";
    static final String EVENT_BROADCAST_RANGE_EX = "tech.myevents.app.eventBroadcastRangeEx";
    static final String EVENT_BROADCAST_RANGE_CODE_EX = "tech.myevents.app.eventBroadcastRangeCodeEx";
    static final String EVENT_START_TIME_EX = "tech.myevents.app.eventStartTimeEx";
    static final String EVENT_START_DATE_EX = "tech.myevents.app.eventStartDateEx";
    static final String EVENT_END_TIME_EX = "tech.myevents.app.eventEndTimeEx";
    static final String EVENT_END_DATE_EX = "tech.myevents.app.eventEndDateEx";
    static final String EVENT_START_TIMESTAMP_EX = "tech.myevents.app.eventStartTimestampEx";
    static final String EVENT_END_TIMESTAMP_EX = "tech.myevents.app.eventEndTimestampEx";
    static final String EVENT_TICKET_SWITCH_EX = "tech.myevents.app.eventTicketSwitchEx";
    static final String EVENT_BROADCAST_PAYMENT_EX = "tech.myevents.app.eventBroadcastPaymentEx";
    static final String EVENT_ECOCACH_MERCHANT_CODE_EX = "tech.myevents.app.eventEcocashMerchantCodeEx";
    static final String EVENT_ECOCASH_MERCHANT_NAME_EX = "tech.myevents.app.eventEcocashMerchantNameEx";
    static final String EVENT_BROADCASTS_MADE_EX = "tech.myevents.app.eventsBroadcastsMadeEx";
    static final String EVENT_APPROVAL_CODE_EX = "tech.myevents.app.eventApprovalCodeEx";
    static final String EVENT_AVAILABLE_TICKETS_EX = "tech,myevents.app.EventAvailableTicketsEx";
    static final String FREE_TICKETS_EX = "tech.myevents.app.FreeTickets.meEx";


    static final String TICKET_APPROVAL_CODE = "tech.myevents.app.ticketApprovalCode";



    static final String AD_TITLE = "tech.myevents.app.adTitle";
    static final String AD_BRAND_NAME = "tech.myevents.app.adBrandName";
    static final String AD_DETAILS = "tech.myevents.app.adDetails";
    static final String AD_INTEREST = "tech.myevents.app.adInterest";
    static final String AD_INTEREST_CODE = "tech.myevents.app.adInterestCode";
    static final String AD_BROADCAST_RANGE = "tech.myevents.app.adBroadcastRange";
    static final String AD_BROADCAST_RANGE_CODE = "tech.myevents.app.adBroadcastRangeCode";
    static final String AD_DURATION = "tech.myevents.app.adDuration";
    static final String AD_DURATION_CODE = "tech.myevents.app.adDurationCode";
    static final String AD_LOCATION = "tech.myevents.app.adLocation";
    static final String AD_PROFILE_LINK = "tech.myevents.app.adProfileLink";
    static final String AD_COST = "tech.myevents.app.adCost";
    static final String AD_BROADCAST_PAYMENT = "tech.myevents.app.adBroadcastPayment";
    static final String AD_ECOCACH_MERCHANT_CODE = "tech.myevents.app.adEcocashMerchantCode";
    static final String AD_ECOCASH_MERCHANT_NAME = "tech.myevents.app.adEcocashMerchantName";
    static final String AD_BROADCASTS_MADE = "tech.myevents.app.adBroadcastsMade";
    static final String AD_APPROVAL_CODE = "tech.myevents.app.adApprovalCode";




    static final String AUD_RANGE_CITY = "tech.myevents.app.audRangeCity";
    static final String AUD_RANGE_NATIONAL = "tech.myevents.app.audRangeNational";
    static final String AUD_50 = "tech.myevents.app.aud50";
    static final String AUD_100 = "tech.myevents.app.aud100";
    static final String AUD_ABOVE_100 = "tech.myevents.app.audAbove100";
    static final String AUD_BROAD_0 = "tech.myevents.app.audBroad0";
    static final String AUD_BROAD_1 = "tech.myevents.app.audBroad1";
    static final String AUD_BROAD_2 = "tech.myevents.app.audBroad2";
    static final String AUD_BROAD_3 = "tech.myevents.app.audBroad3";
    static final String AUD_BROAD_ABOVE_3 = "tech.myevents.app.audBroadAbove3";
    static final String AUD_SIZE_4 = "tech.myevents.app.audSize4";
    static final String AUD_SIZE_11 = "tech.myevents.app.audSIze11";
    static final String AUD_SIZE_31 = "tech.myevents.app.audSize31";
    static final String AUD_SIZE_50 = "tech.myevents.app.audSize50";
    static final String AUD_SIZE_ABOVE_50 = "tech.myevents.app.audSizeAbove50";
    static final String AUD_ECOCASH_NUMBER = "tech.myevents.app.audEcocashNumber";
    static final String AUD_2_D = "tech.myevents.app.aud2D";
    static final String AUD_4_D = "tech.myevents.app.aud4D";
    static final String AUD_7_D = "tech.myevents.app.aud7D";
    static final String AUD_10_D = "tech.myevents.app.aud10D";



    static final String AUD_TICKET_VALIDITY = "tech.myevents.app.audTicketValidity";
    static final String AUD_TICKET_VALIDITY_CODE = "tech.myevents.app.audTicketValidityCode";
    static final String AUD_TICKET_CODE = "tech.myevents.app.AudTicketCodeS";
    static final String AUD_EVENT_VALIDITY = "tech.myevents.app.audEventValidity";
    static final String AUD_EVENT_VALIDITY_CODE = "tech.myevents.app.audEventValidityCode";
    static final String AUD_EVENT_CODE = "tech.myevents.app.AudEventCodeS";
    static final String AUD_AD_VALIDITY = "tech.myevents.app.audAdValidity";
    static final String AUD_AD_VALIDITY_CODE = "tech.myevents.app.audAdValidityCode";
    static final String AUD_AD_CODE = "tech.myevents.app.AudAdCodeS";
    static final String AUD_VERSION_CODE = "tech.myevents.app.audVersionCode";
    static final String AUD_INDIVIDUAL_PRIORITY = "tech.myevents.app.audIndividualPriority";
    static final String AUD_BUSINESS_PRIORITY = "tech.myevents.app.audBusinessPriority";



}
