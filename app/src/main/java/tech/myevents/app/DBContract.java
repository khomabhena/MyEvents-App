package tech.myevents.app;

public final class DBContract {

    public DBContract() {
    }

    static abstract class ApprovalCodes {
        static final String TABLE_NAME = "table_one";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String APPROVAL_CODE = "col_1";
        static final String TIMESTAMP = "col_2";
        static final String TYPE = "col_3";
        static final String KEY = "col_4";
        static final String COl_5 = "col_5";
        static final String COl_6 = "col_6";
        static final String COl_7 = "col_7";
        static final String COl_8 = "col_8";
        static final String COl_9 = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class LoyaltyPoints {
        static final String TABLE_NAME = "table_two";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String TYPE = "col_1";
        static final String POINTS = "col_2";
        static final String TIMESTAMP = "col_3";
        static final String COl_4 = "col_4";
        static final String COl_5 = "col_5";
        static final String COl_6 = "col_6";
        static final String COl_7 = "col_7";
        static final String COl_8 = "col_8";
        static final String COl_9 = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Event {
        static final String TABLE_NAME = "table_three";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String BROADCASTER_UID = "col_1";
        static final String KEY = "col_2";
        static final String INTEREST_CODE = "col_3";
        static final String LOCATION_CODE = "col_4";
        static final String EVENT_NAME = "col_5";
        static final String EVENT_VENUE = "col_6";
        static final String DETAILS = "col_7";
        static final String START_TIMESTAMP = "col_8";
        static final String END_TIMESTAMP = "col_9";
        static final String PROFILE_LINK = "col_10";
        static final String EVENT_STATUS = "col_11";
        static final String BROADCAST_TIME = "col_12";
        static final String UPDATED = "col_13";
        static final String IMPRESSIONS = "col_14";
        static final String TICKET_SEATS_1 = "col_15";
        static final String TICKET_SEATS_2 = "col_16";
        static final String TICKET_PRICE_1 = "col_17";
        static final String TICKET_PRICE_2 = "col_18";
        static final String MERCHANT_CODE = "col_19";
        static final String MERCHANT_NAME = "col_20";

        static final String TICKET_NAME_1 = "col_21";
        static final String TICKET_NAME_2 = "col_22";
        static final String TICKET_PROMO_CODE = "col_23";
        static final String AVAILABLE_TICKETS = "col_24";
        static final String UPDATE_TIME = "col_25";
        static final String ECOCASH_TYPE = "col_26";
        static final String START_DATE = "col_27";
        static final String START_TIME = "col_28";
        static final String BRAND_NAME = "col_29";
        static final String BRAND_LINK = "col_30";
        static final String MESSAGE_SEEN= "col_31";
        static final String MXE_CODE = "col_32";
        static final String VERIFIED = "col_33";
        static final String SIGNATURE_VERSION = "col_34";
        static final String COL_35 = "col_35";
        static final String COL_36 = "col_36";
        static final String COL_37 = "col_37";
        static final String COL_38 = "col_38";
        static final String COL_39 = "col_39";
        static final String COL_40 = "col_40";
    }

    static abstract class Ad {
        static final String TABLE_NAME = "table_four";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String BROADCASTER_UID = "col_1";
        static final String KEY = "col_2";
        static final String INTEREST_CODE = "col_3";
        static final String LOCATION_CODE = "col_4";
        static final String BRAND_NAME = "col_5";
        static final String TITLE = "col_6";
        static final String DETAILS = "col_7";
        static final String PROFILE_LINK = "col_8";
        static final String DURATION = "col_9";
        static final String BROADCAST_TIME = "col_10";
        static final String VIDEO = "col_11";
        static final String STATUS = "col_12";
        static final String IMPRESSIONS = "col_13";
        static final String UPDATE_TIME = "col_14";
        static final String BRAND_LINK = "col_15";
        static final String VERIFIED = "col_16";
        static final String SIGNATURE_VERSION = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Comments {
        static final String TABLE_NAME = "table_five";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String UID = "col_1";
        static final String KEY = "col_2";
        static final String POST_KEY = "col_3";
        static final String USERNAME = "col_4";
        static final String COMMENT = "col_5";
        static final String TIMESTAMP = "col_6";
        static final String VERIFIED = "col_7";
        static final String BROADCASTER_UID = "col_8";
        static final String BROADCAST_NAME = "col_9";
        static final String PLOFILE_LINK = "col_10";
        static final String SIGNATURE_VERSION = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Messages {
        static final String TABLE_NAME = "table_six";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String SEND_TO = "col_1";
        static final String CHAT_ROOM = "col_2";
        static final String SENDER = "col_3";
        static final String RECEIVER = "col_4";
        static final String SENDER_UID = "col_5";
        static final String RECEIVER_UID = "col_6";
        static final String KEY = "col_7";
        static final String MESSAGE = "col_8";
        static final String SENDER_VERIFIED = "col_9";
        static final String LINK_SENDER = "col_10";
        static final String LINK_RECEIVER = "col_11";
        static final String TIMESTAMP = "col_12";
        static final String SIGNATURE_VERSION = "col_13";
        static final String ACC_TYPE = "col_14";
        static final String RECEIVER_VERIFIED = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Impressions {
        static final String TABLE_NAME = "table_seven";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String UID = "col_1";
        static final String KEY = "col_2";
        static final String USERNAME = "col_3";
        static final String LOCATION = "col_4";
        static final String PROFILE_LINK = "col_5";
        static final String TIMESTAMP = "col_6";
        static final String VERIFIED = "col_7";
        static final String SIGNATURE_VERSION = "col_8";
        static final String TYPE = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Businesses {
        static final String TABLE_NAME = "table_eight";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String UID = "col_1";
        static final String KEY = "col_2";
        static final String BRAND_NAME = "col_3";
        static final String LOCATION = "col_4";
        static final String WEBSITE = "col_5";
        static final String EMAIL = "col_6";
        static final String DESCRIPTION = "col_7";
        static final String CATEGORY = "col_8";
        static final String VERIFIED = "col_9";
        static final String PROFILE_LINK = "col_10";
        static final String SIGNATURE_VERSION = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Tickets {
        static final String TABLE_NAME = "table_nine";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String UID = "col_1";
        static final String BROADCASTER_UID = "col_2";
        static final String EVENT_KEY = "col_3";
        static final String NAME = "col_4";
        static final String VENUE = "col_5";
        static final String LOCATION = "col_6";
        static final String TICKET_NUMBER = "col_7";
        static final String PRICE = "col_8";
        static final String START_TIMESTAMP = "col_9";
        static final String CATEGORY = "col_10";
        static final String END_TIMESTAMP = "col_11";
        static final String MARCHANT_NAME = "col_12";
        static final String TICKET_KEY = "col_13";
        static final String VALID = "col_14";
        static final String MTS_CODE = "col_15";
        static final String NUM = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Friends {
        static final String TABLE_NAME = "table_ten";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String UID = "col_1";
        static final String USERNAME = "col_2";
        static final String PROFILE_LINK = "col_3";
        static final String CONTACT_NUMBER = "col_4";
        static final String SIGNATURE_VERSION = "col_5";
        static final String COl_6 = "col_6";
        static final String COl_7 = "col_7";
        static final String COl_8 = "col_8";
        static final String COl_9 = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Moments {
        static final String TABLE_NAME = "table_eleven";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String KEY = "col_1";
        static final String TYPE = "col_2";
        static final String USERNAME = "col_3";
        static final String STORY = "col_4";
        static final String MOMENT_LINK = "col_5";
        static final String PROFILE_LINK = "col_6";
        static final String TIMESTAMP = "col_7";
        static final String CONTACT_NUMBER = "col_8";
        static final String SIGNATURE_VERSION = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }




    static abstract class Table12 {
        static final String TABLE_NAME = "table_twelve";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String COl_1 = "col_1";
        static final String COl_2 = "col_2";
        static final String COl_3 = "col_3";
        static final String COl_4 = "col_4";
        static final String COl_5 = "col_5";
        static final String COl_6 = "col_6";
        static final String COl_7 = "col_7";
        static final String COl_8 = "col_8";
        static final String COl_9 = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Table13 {
        static final String TABLE_NAME = "table_thirteen";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String COl_1 = "col_1";
        static final String COl_2 = "col_2";
        static final String COl_3 = "col_3";
        static final String COl_4 = "col_4";
        static final String COl_5 = "col_5";
        static final String COl_6 = "col_6";
        static final String COl_7 = "col_7";
        static final String COl_8 = "col_8";
        static final String COl_9 = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Table14 {
        static final String TABLE_NAME = "table_fourteen";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String COl_1 = "col_1";
        static final String COl_2 = "col_2";
        static final String COl_3 = "col_3";
        static final String COl_4 = "col_4";
        static final String COl_5 = "col_5";
        static final String COl_6 = "col_6";
        static final String COl_7 = "col_7";
        static final String COl_8 = "col_8";
        static final String COl_9 = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Table15 {
        static final String TABLE_NAME = "table_fifteen";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String COl_1 = "col_1";
        static final String COl_2 = "col_2";
        static final String COl_3 = "col_3";
        static final String COl_4 = "col_4";
        static final String COl_5 = "col_5";
        static final String COl_6 = "col_6";
        static final String COl_7 = "col_7";
        static final String COl_8 = "col_8";
        static final String COl_9 = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }




    static abstract class Table16 {
        static final String TABLE_NAME = "table_sixteen";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String COl_1 = "col_1";
        static final String COl_2 = "col_2";
        static final String COl_3 = "col_3";
        static final String COl_4 = "col_4";
        static final String COl_5 = "col_5";
        static final String COl_6 = "col_6";
        static final String COl_7 = "col_7";
        static final String COl_8 = "col_8";
        static final String COl_9 = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Table17 {
        static final String TABLE_NAME = "table_17";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String COl_1 = "col_1";
        static final String COl_2 = "col_2";
        static final String COl_3 = "col_3";
        static final String COl_4 = "col_4";
        static final String COl_5 = "col_5";
        static final String COl_6 = "col_6";
        static final String COl_7 = "col_7";
        static final String COl_8 = "col_8";
        static final String COl_9 = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Table18 {
        static final String TABLE_NAME = "table_18";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String COl_1 = "col_1";
        static final String COl_2 = "col_2";
        static final String COl_3 = "col_3";
        static final String COl_4 = "col_4";
        static final String COl_5 = "col_5";
        static final String COl_6 = "col_6";
        static final String COl_7 = "col_7";
        static final String COl_8 = "col_8";
        static final String COl_9 = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Table19 {
        static final String TABLE_NAME = "table_19";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String COl_1 = "col_1";
        static final String COl_2 = "col_2";
        static final String COl_3 = "col_3";
        static final String COl_4 = "col_4";
        static final String COl_5 = "col_5";
        static final String COl_6 = "col_6";
        static final String COl_7 = "col_7";
        static final String COl_8 = "col_8";
        static final String COl_9 = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Table20 {
        static final String TABLE_NAME = "table_20";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String COl_1 = "col_1";
        static final String COl_2 = "col_2";
        static final String COl_3 = "col_3";
        static final String COl_4 = "col_4";
        static final String COl_5 = "col_5";
        static final String COl_6 = "col_6";
        static final String COl_7 = "col_7";
        static final String COl_8 = "col_8";
        static final String COl_9 = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

    static abstract class Table21 {
        static final String TABLE_NAME = "table_21";
        static final String ID = "_id";
        static final String LOCAL_UID = "local_uid";
        static final String COl_1 = "col_1";
        static final String COl_2 = "col_2";
        static final String COl_3 = "col_3";
        static final String COl_4 = "col_4";
        static final String COl_5 = "col_5";
        static final String COl_6 = "col_6";
        static final String COl_7 = "col_7";
        static final String COl_8 = "col_8";
        static final String COl_9 = "col_9";
        static final String COl_10 = "col_10";
        static final String COl_11 = "col_11";
        static final String COl_12 = "col_12";
        static final String COl_13 = "col_13";
        static final String COl_14 = "col_14";
        static final String COl_15 = "col_15";
        static final String COl_16 = "col_16";
        static final String COl_17 = "col_17";
        static final String COl_18 = "col_18";
        static final String COl_19 = "col_19";
        static final String COl_20 = "col_20";
    }

}