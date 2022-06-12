package tech.myevents.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DBOperations extends SQLiteOpenHelper {

    private static final int DB_VERSION = 10; //8
    private static final String DB_NAME = "myevents.db";
    //private String localUid;

    Context context;
    SharedPreferences prefsApp;
    String type;

    DBOperations(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        prefsApp = context.getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
        /*if (FirebaseAuth.getInstance().getCurrentUser() !=null)
            localUid = FirebaseAuth.getInstance().getCurrentUser().getUid() + accType;*/
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(ApprovalCodes);
        db.execSQL(LoyaltyPoints);
        db.execSQL(Event);
        db.execSQL(Ad);
        db.execSQL(Comments);
        db.execSQL(Messages);
        db.execSQL(Impressions);
        db.execSQL(Businesses);
        db.execSQL(Tickets);
        db.execSQL(Friends);
        db.execSQL(Moments);
        db.execSQL(QUERY_12);
        db.execSQL(QUERY_13);
        db.execSQL(QUERY_14);
        db.execSQL(QUERY_15);

        db.execSQL(QUERY_16);
        db.execSQL(QUERY_17);
        db.execSQL(QUERY_18);
        db.execSQL(QUERY_19);
        db.execSQL(QUERY_20);
        db.execSQL(QUERY_21);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + DBContract.ApprovalCodes.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.LoyaltyPoints.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Event.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Ad.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Comments.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Messages.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Impressions.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Businesses.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Tickets.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Friends.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Moments.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Table12.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Table13.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Table14.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Table15.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Table16.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Table17.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Table18.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Table19.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Table20.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.Table21.TABLE_NAME);

        onCreate(db);
    }



    Cursor getEvents(SQLiteDatabase db, String localUid) {
        Cursor cursor;
        String[] projections = {
                DBContract.Event.ID,
                DBContract.Event.IMPRESSIONS,
                DBContract.Event.TICKET_SEATS_1,
                DBContract.Event.START_TIMESTAMP,
                DBContract.Event.END_TIMESTAMP,
                DBContract.Event.TICKET_SEATS_2,
                DBContract.Event.TICKET_PRICE_1,
                DBContract.Event.TICKET_PRICE_2,
                DBContract.Event.MERCHANT_CODE,
                DBContract.Event.BROADCAST_TIME,
                DBContract.Event.BROADCASTER_UID,
                DBContract.Event.UPDATED,
                DBContract.Event.KEY,
                DBContract.Event.INTEREST_CODE,
                DBContract.Event.LOCATION_CODE,
                DBContract.Event.EVENT_NAME,
                DBContract.Event.EVENT_VENUE,
                DBContract.Event.DETAILS,
                DBContract.Event.PROFILE_LINK,
                DBContract.Event.EVENT_STATUS,
                DBContract.Event.START_DATE,
                DBContract.Event.START_TIME,
                DBContract.Event.TICKET_NAME_1,
                DBContract.Event.TICKET_NAME_2,
                DBContract.Event.MERCHANT_NAME,
                DBContract.Event.MERCHANT_CODE,
                DBContract.Event.TICKET_PROMO_CODE,
                DBContract.Event.AVAILABLE_TICKETS,
                DBContract.Event.ECOCASH_TYPE,
                DBContract.Event.UPDATE_TIME,

                DBContract.Event.SIGNATURE_VERSION,
                DBContract.Event.BRAND_LINK,
                DBContract.Event.BRAND_NAME,
                DBContract.Event.MXE_CODE,
                DBContract.Event.VERIFIED
        };
// + DBContract.Event.LOCAL_UID + "='" + localUid + "'"
        cursor = db.query(
                true,
                DBContract.Event.TABLE_NAME, projections,
                "(" + DBContract.Event.EVENT_STATUS + "='" + StaticVariables.newBroad +"' OR "
                        + DBContract.Event.EVENT_STATUS + "='" + StaticVariables.eventExclusive +"') AND " +
                        DBContract.Event.LOCAL_UID + "='" + localUid + type + "'",
                null,
                DBContract.Event.KEY,
                null,
                DBContract.Event.BROADCAST_TIME + " DESC",
                null
        );

        return cursor;
    }

    List<String> getEventKeys(SQLiteDatabase db, String localUid) {
        Cursor cursor;

        String[] arrayMax = new String[]{""};
        List<String> listKeys = new LinkedList<>(Arrays.asList(arrayMax));
        String[] projections = {DBContract.Event.KEY};
        cursor = db.query(true, DBContract.Event.TABLE_NAME, projections,
                DBContract.Event.EVENT_STATUS + "='"+ StaticVariables.newBroad +"' AND " +
                        DBContract.Event.LOCAL_UID + "='" + localUid + type + "'",
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            listKeys.add(cursor.getString(cursor.getColumnIndex(DBContract.Event.KEY)));
        }
        cursor.close();

        return listKeys;
    }

    int checkEventExistence(SQLiteDatabase db, String postKey) {
        Cursor cursor;
        String[] projections = {
                DBContract.Event.ID
        };

        cursor = db.query(
                true,
                DBContract.Event.TABLE_NAME, projections,
                DBContract.Event.KEY + "='" + postKey +"'",
                null,
                DBContract.Event.KEY,
                null,
                DBContract.Event.ID + " ASC",
                null
        );
        int count = cursor.getCount();
        cursor.close();

        return count;
    }




    Cursor getEventsExclusive(SQLiteDatabase db, String localUid) {
        Cursor cursor;
        String[] projections = {
                DBContract.Event.IMPRESSIONS,
                DBContract.Event.TICKET_SEATS_1,
                DBContract.Event.START_TIMESTAMP,
                DBContract.Event.END_TIMESTAMP,
                DBContract.Event.TICKET_SEATS_2,
                DBContract.Event.TICKET_PRICE_1,
                DBContract.Event.TICKET_PRICE_2,
                DBContract.Event.MERCHANT_CODE,
                DBContract.Event.BROADCAST_TIME,
                DBContract.Event.BROADCASTER_UID,
                DBContract.Event.UPDATED,
                DBContract.Event.KEY,
                DBContract.Event.INTEREST_CODE,
                DBContract.Event.LOCATION_CODE,
                DBContract.Event.EVENT_NAME,
                DBContract.Event.EVENT_VENUE,
                DBContract.Event.DETAILS,
                DBContract.Event.PROFILE_LINK,
                DBContract.Event.EVENT_STATUS,
                DBContract.Event.START_DATE,
                DBContract.Event.START_TIME,
                DBContract.Event.TICKET_NAME_1,
                DBContract.Event.TICKET_NAME_2,
                DBContract.Event.MERCHANT_NAME,
                DBContract.Event.MERCHANT_CODE,
                DBContract.Event.TICKET_PROMO_CODE,
                DBContract.Event.AVAILABLE_TICKETS,
                DBContract.Event.ECOCASH_TYPE,
                DBContract.Event.UPDATE_TIME,

                DBContract.Event.SIGNATURE_VERSION,
                DBContract.Event.BRAND_LINK,
                DBContract.Event.BRAND_NAME,
                DBContract.Event.MXE_CODE,
                DBContract.Event.VERIFIED
        };

        cursor = db.query(
                true,
                DBContract.Event.TABLE_NAME, projections,
                DBContract.Event.EVENT_STATUS + "='"+ StaticVariables.eventExclusive +"' AND " +
                        DBContract.Event.LOCAL_UID + "='" + localUid + type + "'",
                null,
                DBContract.Event.KEY,
                null,
                DBContract.Event.BROADCAST_TIME + " DESC",
                null
        );

        return cursor;
    }

    List<String> getEventKeysExclusive(SQLiteDatabase db, String localUid) {
        Cursor cursor;

        String[] arrayMax = new String[]{""};
        List<String> listKeys = new LinkedList<>(Arrays.asList(arrayMax));
        String[] projections = {DBContract.Event.KEY};

        cursor = db.query(true, DBContract.Event.TABLE_NAME, projections,
                DBContract.Event.EVENT_STATUS + "='"+ StaticVariables.eventExclusive +"' AND " +
                        DBContract.Event.LOCAL_UID + "='" + localUid + type + "'",
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            listKeys.add(cursor.getString(cursor.getColumnIndex(DBContract.Event.KEY)));
        }
        cursor.close();

        return listKeys;
    }





    Cursor getAds(SQLiteDatabase db, String localUid) {
        Cursor cursor;

        String[] projections = {
                DBContract.Ad.ID,
                DBContract.Ad.IMPRESSIONS,
                DBContract.Ad.DURATION,
                DBContract.Ad.BROADCAST_TIME,
                DBContract.Ad.KEY,
                DBContract.Ad.BROADCASTER_UID,
                DBContract.Ad.BRAND_NAME,
                DBContract.Ad.TITLE,
                DBContract.Ad.BRAND_LINK,
                DBContract.Ad.DETAILS,
                DBContract.Ad.PROFILE_LINK,
                DBContract.Ad.LOCATION_CODE,
                DBContract.Ad.INTEREST_CODE,
                DBContract.Ad.STATUS,
                DBContract.Ad.UPDATE_TIME,
                DBContract.Ad.VERIFIED,

                DBContract.Ad.SIGNATURE_VERSION
        };
        cursor = db.query(
                true,
                DBContract.Ad.TABLE_NAME,
                projections,
                DBContract.Ad.STATUS + "='"+ StaticVariables.newBroad +"'",
                null,
                DBContract.Ad.KEY,
                null,
                DBContract.Ad.BROADCAST_TIME + " DESC",
                null
        );

        return cursor;
    }

    List<String> getAdKeys(SQLiteDatabase db, String localUid) {
        Cursor cursor;

        String[] arrayMax = new String[]{""};
        List<String> listKeys = new ArrayList<>(Arrays.asList(arrayMax));
        String[] projections = {DBContract.Ad.KEY};
        cursor = db.query(true, DBContract.Ad.TABLE_NAME, projections,
                null,
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            listKeys.add(cursor.getString(cursor.getColumnIndex(DBContract.Ad.KEY)));
        }
        cursor.close();

        return listKeys;
    }

    int checkAdExistence(SQLiteDatabase db, String postKey) {
        Cursor cursor;
        String[] projections = {
                DBContract.Ad.ID
        };

        cursor = db.query(
                true,
                DBContract.Ad.TABLE_NAME, projections,
                DBContract.Ad.KEY + "='" + postKey +"'",
                null,
                DBContract.Ad.KEY,
                null,
                DBContract.Ad.ID + " ASC",
                null
        );
        int count = cursor.getCount();
        cursor.close();

        return count;
    }



    List getApprovalCodes(SQLiteDatabase db, String localUid) {
        Cursor cursor;

        List listKeys = new ArrayList<>();

        String[] projections = {DBContract.ApprovalCodes.APPROVAL_CODE,
                DBContract.ApprovalCodes.TYPE,
                DBContract.ApprovalCodes.KEY,
                DBContract.ApprovalCodes.TIMESTAMP};

        cursor = db.query(true, DBContract.ApprovalCodes.TABLE_NAME, projections,
                DBContract.ApprovalCodes.LOCAL_UID + "='" + localUid + "'",
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            String approvalCode = cursor.getString(cursor.getColumnIndex(DBContract.ApprovalCodes.APPROVAL_CODE));
            String type = cursor.getString(cursor.getColumnIndex(DBContract.ApprovalCodes.TYPE));
            String key = cursor.getString(cursor.getColumnIndex(DBContract.ApprovalCodes.KEY));
            long timestamp = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBContract.ApprovalCodes.TIMESTAMP)));

            GetSetterApprovalCode setter = new GetSetterApprovalCode(type, approvalCode, key, timestamp);
            listKeys.add(setter);
        }
        cursor.close();

        return listKeys;
    }

    String[] getApprovalCodeKeys(SQLiteDatabase db, String localUid) {
        Cursor cursor;

        String[] arrayMax = new String[]{""};
        List<String> listKeys = new LinkedList<>(Arrays.asList(arrayMax));
        String[] projections = {DBContract.ApprovalCodes.TIMESTAMP};
        cursor = db.query(true, DBContract.ApprovalCodes.TABLE_NAME, projections,
                DBContract.ApprovalCodes.LOCAL_UID + "='" + localUid + type + "'",
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            listKeys.add(cursor.getString(cursor.getColumnIndex(DBContract.ApprovalCodes.TIMESTAMP)));
        }
        cursor.close();
        arrayMax = listKeys.toArray(new String[listKeys.size()]);

        return arrayMax;
    }




    int getPoints(SQLiteDatabase db, String type, String localUid) {
        Cursor cursor;
        String[] projections = {
                DBContract.LoyaltyPoints.ID
        };

        cursor = db.query(
                true,
                DBContract.LoyaltyPoints.TABLE_NAME, projections,
                DBContract.LoyaltyPoints.TYPE + "='" + type +"' AND " +
                        DBContract.LoyaltyPoints.LOCAL_UID + "='" + localUid + type + "'",
                null,
                null,
                null,
                DBContract.LoyaltyPoints.ID + " ASC",
                null
        );
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    int getAllPoints(SQLiteDatabase db, String localUid) {
        Cursor cursor;
        String[] projections = {
                DBContract.LoyaltyPoints.ID
        };

        cursor = db.query(
                true,
                DBContract.LoyaltyPoints.TABLE_NAME, projections,
                DBContract.LoyaltyPoints.LOCAL_UID + "='" + localUid + type + "'",
                null,
                null,
                null,
                DBContract.LoyaltyPoints.ID + " ASC",
                null
        );
        int count = cursor.getCount();
        cursor.close();

        return count;
    }




    Cursor getComments(SQLiteDatabase db, String broadcasterUid, String localUid) {
        Cursor cursor;
        String[] projections = {

                DBContract.Comments.ID,
                DBContract.Comments.UID,
                DBContract.Comments.KEY,
                DBContract.Comments.POST_KEY,
                DBContract.Comments.USERNAME,
                DBContract.Comments.COMMENT,
                DBContract.Comments.VERIFIED,
                DBContract.Comments.TIMESTAMP,
                DBContract.Comments.PLOFILE_LINK,
                DBContract.Comments.BROADCAST_NAME,
                DBContract.Comments.BROADCASTER_UID
        };
        cursor = db.query(
                true,
                DBContract.Comments.TABLE_NAME,
                projections,
                DBContract.Comments.LOCAL_UID + "='" + localUid + type + "' AND " +
                        DBContract.Comments.BROADCASTER_UID + "='" + broadcasterUid + "'",
                null,
                DBContract.Comments.POST_KEY,
                null,
                DBContract.Comments.TIMESTAMP + " DESC",
                null
        );

        return cursor;
    }

    Cursor getReviews(SQLiteDatabase db, String broadcasterUid, String localUid) {
        Cursor cursor;
        String[] projections = {

                DBContract.Comments.ID,
                DBContract.Comments.UID,
                DBContract.Comments.KEY,
                DBContract.Comments.POST_KEY,
                DBContract.Comments.USERNAME,
                DBContract.Comments.COMMENT,
                DBContract.Comments.VERIFIED,
                DBContract.Comments.TIMESTAMP,
                DBContract.Comments.PLOFILE_LINK,
                DBContract.Comments.BROADCAST_NAME,
                DBContract.Comments.SIGNATURE_VERSION,
                DBContract.Comments.BROADCASTER_UID
        };
        cursor = db.query(
                true,
                DBContract.Comments.TABLE_NAME,
                projections,
                DBContract.Comments.LOCAL_UID + "='" + localUid + type + "' AND " +
                        DBContract.Comments.BROADCASTER_UID + "='" + broadcasterUid + "'",
                null,
                DBContract.Comments.KEY,
                null,
                DBContract.Comments.TIMESTAMP + " DESC",
                null
        );

        return cursor;
    }

    Cursor getCommentsInbox(SQLiteDatabase db, String postKey, String localUid) {
        Cursor cursor;

        String[] projections = {
                DBContract.Comments.ID,
                DBContract.Comments.UID,
                DBContract.Comments.KEY,
                DBContract.Comments.POST_KEY,
                DBContract.Comments.USERNAME,
                DBContract.Comments.COMMENT,
                DBContract.Comments.VERIFIED,
                DBContract.Comments.TIMESTAMP,
                DBContract.Comments.PLOFILE_LINK,
                DBContract.Comments.BROADCAST_NAME,
                DBContract.Comments.BROADCASTER_UID
        };
        cursor = db.query(
                true,
                DBContract.Comments.TABLE_NAME,
                projections,
                DBContract.Comments.POST_KEY + "='" + postKey + "' AND " +
                        DBContract.Comments.LOCAL_UID + "='" + localUid + type + "'",
                null,
                DBContract.Comments.KEY,
                null,
                DBContract.Comments.TIMESTAMP + " ASC",
                null
        );

        return cursor;
    }

    List<String> getCommentKeys(SQLiteDatabase db, String broadcasterUid, String localUid) {
        Cursor cursor;

        String[] arrayMax = new String[]{""};
        List<String> listKeys = new LinkedList<>(Arrays.asList(arrayMax));
        String[] projections = {DBContract.Comments.KEY};
        cursor = db.query(true, DBContract.Comments.TABLE_NAME, projections,
                DBContract.Comments.BROADCASTER_UID + "='" + broadcasterUid + "' AND " +
                        DBContract.Comments.LOCAL_UID + "='" + localUid + type + "'",
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            listKeys.add(cursor.getString(cursor.getColumnIndex(DBContract.Comments.KEY)));
        }
        cursor.close();

        return listKeys;
    }

    List<String> getCommentInboxKeys(SQLiteDatabase db, String localUid) {
        Cursor cursor;

        String[] arrayMax = new String[]{""};
        List<String> listKeys = new LinkedList<>(Arrays.asList(arrayMax));
        String[] projections = {DBContract.Comments.KEY};
        cursor = db.query(true, DBContract.Comments.TABLE_NAME, projections,
                DBContract.Comments.LOCAL_UID + "='" + localUid + type + "'",
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            listKeys.add(cursor.getString(cursor.getColumnIndex(DBContract.Comments.KEY)));
        }
        cursor.close();

        return listKeys;
    }

    int checkCommentExistence(SQLiteDatabase db, String postKey) {
        Cursor cursor;
        String[] projections = {
                DBContract.Comments.ID
        };

        cursor = db.query(
                true,
                DBContract.Comments.TABLE_NAME, projections,
                DBContract.Comments.KEY + "='" + postKey +"'",
                null,
                DBContract.Comments.KEY,
                null,
                DBContract.Comments.ID + " ASC",
                null
        );
        int count = cursor.getCount();
        cursor.close();

        return count;
    }




    Cursor getInbox(SQLiteDatabase db, String localUid) {

        Cursor cursor;
        String[] projections = {
                DBContract.Messages.ID,
                DBContract.Messages.SEND_TO,
                DBContract.Messages.KEY,
                DBContract.Messages.SENDER,
                DBContract.Messages.RECEIVER,
                DBContract.Messages.SENDER_UID,
                DBContract.Messages.RECEIVER_UID,
                DBContract.Messages.SENDER_VERIFIED,
                DBContract.Messages.CHAT_ROOM,
                DBContract.Messages.MESSAGE,
                DBContract.Messages.LINK_SENDER,
                DBContract.Messages.LINK_RECEIVER,
                DBContract.Messages.TIMESTAMP,
                DBContract.Messages.ACC_TYPE,

                DBContract.Messages.SIGNATURE_VERSION
        };
        cursor = db.query(
                true,
                DBContract.Messages.TABLE_NAME,
                projections,
                DBContract.Messages.LOCAL_UID + "='" + localUid + type + "'",
                null,
                DBContract.Messages.KEY,
                null,
                DBContract.Messages.TIMESTAMP + " ASC",
                null
        );

        return cursor;
    }

    Cursor getInboxDetailed(SQLiteDatabase db, String chatRoom1, String chatRoom2, String localUid) {
        Cursor cursor;
        String[] projections = {
                DBContract.Messages.ID,
                DBContract.Messages.SEND_TO,
                DBContract.Messages.KEY,
                DBContract.Messages.SENDER,
                DBContract.Messages.RECEIVER,
                DBContract.Messages.SENDER_UID,
                DBContract.Messages.RECEIVER_UID,
                DBContract.Messages.SENDER_VERIFIED,
                DBContract.Messages.RECEIVER_VERIFIED,
                DBContract.Messages.CHAT_ROOM,
                DBContract.Messages.MESSAGE,
                DBContract.Messages.LINK_SENDER,
                DBContract.Messages.LINK_RECEIVER,
                DBContract.Messages.TIMESTAMP,
                DBContract.Messages.ACC_TYPE,

                DBContract.Messages.SIGNATURE_VERSION
        };

        cursor = db.query(
                true,
                DBContract.Messages.TABLE_NAME,
                projections,
                "(" + DBContract.Messages.CHAT_ROOM + "='" + chatRoom1 + "' OR "+
                        DBContract.Messages.CHAT_ROOM + "='" + chatRoom2 + "') AND " +
                        DBContract.Messages.LOCAL_UID + "='" + localUid + type + "'",
                null,
                DBContract.Messages.KEY,
                null,
                DBContract.Messages.TIMESTAMP + " ASC",
                null
        );

        return cursor;
    }

    Cursor getMessages(SQLiteDatabase db, String localUid) {
        Cursor cursor;

        String[] projections = {
                DBContract.Messages.ID,
                DBContract.Messages.SEND_TO,
                DBContract.Messages.KEY,
                DBContract.Messages.SENDER,
                DBContract.Messages.RECEIVER,
                DBContract.Messages.SENDER_UID,
                DBContract.Messages.RECEIVER_UID,
                DBContract.Messages.SENDER_VERIFIED,
                DBContract.Messages.RECEIVER_VERIFIED,
                DBContract.Messages.CHAT_ROOM,
                DBContract.Messages.MESSAGE,
                DBContract.Messages.LINK_SENDER,
                DBContract.Messages.LINK_RECEIVER,
                DBContract.Messages.TIMESTAMP,
                DBContract.Messages.ACC_TYPE,

                DBContract.Messages.SIGNATURE_VERSION
        };

        cursor = db.query(
                true,
                DBContract.Messages.TABLE_NAME,
                projections,
                DBContract.Messages.LOCAL_UID + "='" + localUid + type + "'",
                null,
                DBContract.Messages.CHAT_ROOM,
                null,
                DBContract.Messages.TIMESTAMP + " DESC",
                null
        );

        return cursor;
    }

    List<String> getMessagesKeys(SQLiteDatabase db, String localUid) {
        Cursor cursor;

        String[] arrayMax = new String[]{""};
        List<String> listKeys = new LinkedList<>(Arrays.asList(arrayMax));
        String[] projections = {DBContract.Messages.KEY};
        cursor = db.query(true, DBContract.Messages.TABLE_NAME, projections,
                DBContract.Messages.LOCAL_UID + "='" + localUid + type + "'",
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            listKeys.add(cursor.getString(cursor.getColumnIndex(DBContract.Messages.KEY)));
        }
        cursor.close();

        return listKeys;
    }

    int checkMessageExistence(SQLiteDatabase db, String postKey) {
        Cursor cursor;
        String[] projections = {
                DBContract.Messages.ID
        };

        cursor = db.query(
                true,
                DBContract.Messages.TABLE_NAME, projections,
                DBContract.Messages.KEY + "='" + postKey +"'",
                null,
                DBContract.Messages.KEY,
                null,
                DBContract.Messages.ID + " ASC",
                null
        );
        int count = cursor.getCount();
        cursor.close();

        return count;
    }




    Cursor getBusinesses(SQLiteDatabase db) {
        Cursor cursor;

        String[] projections = {
                DBContract.Businesses.ID,
                DBContract.Businesses.LOCAL_UID,
                DBContract.Businesses.UID,
                DBContract.Businesses.KEY,
                DBContract.Businesses.BRAND_NAME,
                DBContract.Businesses.LOCATION,
                DBContract.Businesses.WEBSITE,
                DBContract.Businesses.EMAIL,
                DBContract.Businesses.DESCRIPTION,
                DBContract.Businesses.PROFILE_LINK,
                DBContract.Businesses.CATEGORY,
                DBContract.Businesses.VERIFIED,

                DBContract.Businesses.SIGNATURE_VERSION
        };


        cursor = db.query(
                true,
                DBContract.Businesses.TABLE_NAME, projections,
                null,
                null,
                DBContract.Businesses.UID,
                null,
                DBContract.Businesses.ID + " DESC",
                null
        );

        return cursor;
    }

    Cursor getBusiness(SQLiteDatabase db, String uid) {
        Cursor cursor;
        String[] projections = {
                DBContract.Businesses.ID,
                DBContract.Businesses.LOCAL_UID,
                DBContract.Businesses.UID,
                DBContract.Businesses.KEY,
                DBContract.Businesses.BRAND_NAME,
                DBContract.Businesses.LOCATION,
                DBContract.Businesses.WEBSITE,
                DBContract.Businesses.EMAIL,
                DBContract.Businesses.PROFILE_LINK,
                DBContract.Businesses.DESCRIPTION,
                DBContract.Businesses.CATEGORY,
                DBContract.Businesses.VERIFIED,

                DBContract.Businesses.SIGNATURE_VERSION
        };


        cursor = db.query(
                true,
                DBContract.Businesses.TABLE_NAME, projections,
                DBContract.Businesses.UID + "='" + uid + "'",
                null,
                DBContract.Businesses.UID,
                null,
                DBContract.Businesses.ID + " DESC",
                null
        );

        return cursor;
    }

    List<String> getBusinessKeys(SQLiteDatabase db) {
        Cursor cursor;

        String[] arrayMax = new String[]{""};
        List<String> listKeys = new LinkedList<>(Arrays.asList(arrayMax));
        StaticVariables.listVerifiedAccounts = new LinkedList<>(Arrays.asList(arrayMax));
        String[] projections = {DBContract.Businesses.UID, DBContract.Businesses.VERIFIED};
        cursor = db.query(true, DBContract.Businesses.TABLE_NAME, projections,
                null,
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            listKeys.add(cursor.getString(cursor.getColumnIndex(DBContract.Businesses.UID)));
            StaticVariables.listVerifiedAccounts.add(cursor.getString(cursor.getColumnIndex(DBContract.Businesses.VERIFIED)));
        }
        cursor.close();

        return listKeys;
    }



    Cursor getFriends(SQLiteDatabase db, String localUid) {
        Cursor cursor;
        String[] projections = {
                DBContract.Friends.UID,
                DBContract.Friends.USERNAME,
                DBContract.Friends.PROFILE_LINK,
                DBContract.Friends.CONTACT_NUMBER,

                DBContract.Friends.SIGNATURE_VERSION
        };

        cursor = db.query(
                true,
                DBContract.Friends.TABLE_NAME, projections,
                DBContract.Friends.LOCAL_UID + "='" + localUid + type + "'",
                null,
                DBContract.Friends.CONTACT_NUMBER,
                null,
                DBContract.Friends.USERNAME + " DESC",
                null
        );

        return cursor;
    }

    List<String> getFriendKeys(SQLiteDatabase db, String localUid) {
        Cursor cursor;

        String[] arrayMax = new String[]{""};
        List<String> listKeys = new LinkedList<>(Arrays.asList(arrayMax));
        String[] projections = {DBContract.Friends.CONTACT_NUMBER};
        cursor = db.query(true, DBContract.Friends.TABLE_NAME, projections,
                DBContract.Friends.LOCAL_UID + "='" + localUid + type + "'",
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            listKeys.add(cursor.getString(cursor.getColumnIndex(DBContract.Friends.CONTACT_NUMBER)));
        }
        cursor.close();

        return listKeys;
    }



    Cursor getMoments(SQLiteDatabase db, String localUid) {
        Cursor cursor;
        String[] projections = {
                DBContract.Moments.ID,
                DBContract.Moments.KEY,
                DBContract.Moments.TYPE,
                DBContract.Moments.USERNAME,
                DBContract.Moments.STORY,

                DBContract.Moments.MOMENT_LINK,
                DBContract.Moments.PROFILE_LINK,
                DBContract.Moments.TIMESTAMP,
                DBContract.Moments.CONTACT_NUMBER,

                DBContract.Moments.SIGNATURE_VERSION
        };

        cursor = db.query(
                true,
                DBContract.Moments.TABLE_NAME, projections,
                DBContract.Moments.LOCAL_UID + "='" + localUid + type + "' AND " +
                DBContract.Moments.TYPE + "!='" + StaticVariables.deleted + "'",
                null,
                DBContract.Moments.KEY,
                null,
                DBContract.Moments.TIMESTAMP + " DESC",
                null
        );

        return cursor;
    }

    List<String> getMomentKeys(SQLiteDatabase db, String localUid) {
        Cursor cursor;

        String[] arrayMax = new String[]{""};
        List<String> listKeys = new LinkedList<>(Arrays.asList(arrayMax));
        String[] projections = {DBContract.Moments.KEY};
        cursor = db.query(true, DBContract.Moments.TABLE_NAME, projections,
                DBContract.Moments.LOCAL_UID + "='" + localUid + type + "'",
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            listKeys.add(cursor.getString(cursor.getColumnIndex(DBContract.Moments.KEY)));
        }
        cursor.close();

        return listKeys;
    }



    Cursor getTicketsUser(SQLiteDatabase db, String uid, String localUid) {
        Cursor cursor;
        String[] projections = {
                DBContract.Tickets.ID,
                DBContract.Tickets.NUM,
                DBContract.Tickets.UID,
                DBContract.Tickets.BROADCASTER_UID,
                DBContract.Tickets.EVENT_KEY,
                DBContract.Tickets.NAME,
                DBContract.Tickets.VENUE,
                DBContract.Tickets.LOCATION,
                DBContract.Tickets.TICKET_NUMBER,
                DBContract.Tickets.PRICE,
                DBContract.Tickets.START_TIMESTAMP,
                DBContract.Tickets.CATEGORY,
                DBContract.Tickets.END_TIMESTAMP,
                DBContract.Tickets.MARCHANT_NAME,
                DBContract.Tickets.TICKET_KEY,
                DBContract.Tickets.VALID,
                DBContract.Tickets.MTS_CODE

        };

        cursor = db.query(
                true,
                DBContract.Tickets.TABLE_NAME, projections,
                DBContract.Tickets.UID + "='" + uid + "' AND " +
                        DBContract.Tickets.LOCAL_UID + "='" + localUid + type + "'",
                null,
                DBContract.Tickets.TICKET_KEY,
                null,
                DBContract.Tickets.END_TIMESTAMP + " DESC",
                null
        );

        return cursor;
    }

    Cursor getTicketsSigner(SQLiteDatabase db, String uid, String localUid) {
        Cursor cursor;
        String[] projections = {
                DBContract.Tickets.ID,
                DBContract.Tickets.NUM,
                DBContract.Tickets.UID,
                DBContract.Tickets.BROADCASTER_UID,
                DBContract.Tickets.EVENT_KEY,
                DBContract.Tickets.NAME,
                DBContract.Tickets.VENUE,
                DBContract.Tickets.LOCATION,
                DBContract.Tickets.TICKET_NUMBER,
                DBContract.Tickets.PRICE,
                DBContract.Tickets.START_TIMESTAMP,
                DBContract.Tickets.CATEGORY,
                DBContract.Tickets.END_TIMESTAMP,
                DBContract.Tickets.MARCHANT_NAME,
                DBContract.Tickets.TICKET_KEY,
                DBContract.Tickets.VALID,
                DBContract.Tickets.MTS_CODE

        };

        cursor = db.query(
                true,
                DBContract.Tickets.TABLE_NAME, projections,
                DBContract.Tickets.BROADCASTER_UID + "='" + uid + "' AND " +
                        DBContract.Tickets.LOCAL_UID + "='" + localUid + type + "'",
                null,
                DBContract.Tickets.TICKET_KEY,
                null,
                DBContract.Tickets.END_TIMESTAMP + " DESC",
                null
        );

        return cursor;
    }

    Cursor checkTicketAvailablity(SQLiteDatabase db, String ticketKey, String localUid) {
        Cursor cursor;
        String[] projections = {
                DBContract.Tickets.ID,
                DBContract.Tickets.EVENT_KEY

        };

        cursor = db.query(
                true,
                DBContract.Tickets.TABLE_NAME, projections,
                DBContract.Tickets.TICKET_KEY + "='" + ticketKey + "' AND " +
                        DBContract.Tickets.LOCAL_UID + "='" + localUid + type + "'",
                null,
                DBContract.Tickets.TICKET_KEY,
                null,
                DBContract.Tickets.END_TIMESTAMP + " DESC",
                null
        );

        return cursor;
    }

    List<String> getTicketKeys(SQLiteDatabase db, String localUid) {
        Cursor cursor;

        String[] arrayMax = new String[]{""};
        List<String> listKeys = new LinkedList<>(Arrays.asList(arrayMax));
        String[] projections = {DBContract.Tickets.TICKET_KEY};
        cursor = db.query(true, DBContract.Tickets.TABLE_NAME, projections,
                DBContract.Tickets.LOCAL_UID + "='" + localUid + type + "'",
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            listKeys.add(cursor.getString(cursor.getColumnIndex(DBContract.Tickets.TICKET_KEY)));
        }
        cursor.close();

        return listKeys;
    }




    Cursor getImpressions(SQLiteDatabase db,  String key,String localUid) {
        Cursor cursor;

        String[] projections = {
                DBContract.Impressions.UID,
                DBContract.Impressions.KEY,
                DBContract.Impressions.USERNAME,
                DBContract.Impressions.LOCATION,
                DBContract.Impressions.PROFILE_LINK,
                DBContract.Impressions.TIMESTAMP,
                DBContract.Impressions.VERIFIED,

                DBContract.Impressions.SIGNATURE_VERSION
        };


        cursor = db.query(
                true,
                DBContract.Impressions.TABLE_NAME, projections,
                DBContract.Impressions.KEY + "='" + key + "' AND " +
                        DBContract.Impressions.LOCAL_UID + "='" + localUid + type + "'",
                null,
                DBContract.Impressions.UID,
                null,
                DBContract.Impressions.TIMESTAMP + " DESC",
                null
        );

        return cursor;
    }

    List<String> getImpressionKeys(SQLiteDatabase db,  String key, String localUid) {
        Cursor cursor;

        String[] arrayMax = new String[]{""};
        List<String> listKeys = new LinkedList<>(Arrays.asList(arrayMax));

        String[] projections = {DBContract.Impressions.UID};
        cursor = db.query(true, DBContract.Impressions.TABLE_NAME, projections,
                DBContract.Impressions.KEY + "='" + key + "' AND " +
                        DBContract.Impressions.LOCAL_UID + "='" + localUid + type + "'",
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            listKeys.add(cursor.getString(cursor.getColumnIndex(DBContract.Impressions.UID)));
        }
        cursor.close();

        return listKeys;
    }

    int getImpressionCount(SQLiteDatabase db, String postKey) {
        Cursor cursor;
        String[] projections = {
                DBContract.Impressions.ID
        };

        cursor = db.query(
                true,
                DBContract.Impressions.TABLE_NAME, projections,
                DBContract.Impressions.KEY + "='" + postKey +"'",
                null,
                DBContract.Impressions.KEY,
                null,
                DBContract.Impressions.ID + " ASC",
                null
        );
        int count = cursor.getCount();
        cursor.close();

        return count;
    }



    private static final String ApprovalCodes = "CREATE TABLE "+ DBContract.ApprovalCodes.TABLE_NAME +"("+
            DBContract.ApprovalCodes.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.ApprovalCodes.LOCAL_UID + " TEXT, " +
            DBContract.ApprovalCodes.APPROVAL_CODE + " TEXT, " +
            DBContract.ApprovalCodes.TIMESTAMP + " TEXT, " +
            DBContract.ApprovalCodes.TYPE + " TEXT, " +
            DBContract.ApprovalCodes.KEY + " TEXT, " +
            DBContract.ApprovalCodes.COl_5 + " TEXT, " +
            DBContract.ApprovalCodes.COl_6 + " TEXT, " +
            DBContract.ApprovalCodes.COl_7 + " TEXT, " +
            DBContract.ApprovalCodes.COl_8 + " TEXT, " +
            DBContract.ApprovalCodes.COl_9 + " TEXT, " +
            DBContract.ApprovalCodes.COl_10 + " TEXT, " +
            DBContract.ApprovalCodes.COl_11 + " TEXT, " +
            DBContract.ApprovalCodes.COl_12 + " TEXT, " +
            DBContract.ApprovalCodes.COl_13 + " TEXT, " +
            DBContract.ApprovalCodes.COl_14 + " TEXT, " +
            DBContract.ApprovalCodes.COl_15 + " TEXT, " +
            DBContract.ApprovalCodes.COl_16 + " TEXT, " +
            DBContract.ApprovalCodes.COl_17 + " TEXT, " +
            DBContract.ApprovalCodes.COl_18 + " TEXT, " +
            DBContract.ApprovalCodes.COl_19 + " TEXT, " +
            DBContract.ApprovalCodes.COl_20 +" TEXT);";

    private static final String LoyaltyPoints = "CREATE TABLE "+ DBContract.LoyaltyPoints.TABLE_NAME + "("+
            DBContract.LoyaltyPoints.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.LoyaltyPoints.LOCAL_UID + " TEXT, " +
            DBContract.LoyaltyPoints.TYPE + " TEXT, " +
            DBContract.LoyaltyPoints.POINTS + " TEXT, " +
            DBContract.LoyaltyPoints.TIMESTAMP + " TEXT, " +
            DBContract.LoyaltyPoints.COl_4 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_5 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_6 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_7 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_8 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_9 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_10 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_11 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_12 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_13 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_14 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_15 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_16 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_17 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_18 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_19 + " TEXT, " +
            DBContract.LoyaltyPoints.COl_20 +" TEXT);";

    private static final String Event = "CREATE TABLE "+ DBContract.Event.TABLE_NAME + "("+
            DBContract.Event.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Event.LOCAL_UID + " TEXT, " +
            DBContract.Event.BROADCASTER_UID + " TEXT, " +
            DBContract.Event.KEY + " TEXT, " +
            DBContract.Event.INTEREST_CODE + " TEXT, " +
            DBContract.Event.LOCATION_CODE + " TEXT, " +
            DBContract.Event.EVENT_NAME + " TEXT, " +
            DBContract.Event.EVENT_VENUE + " TEXT, " +
            DBContract.Event.DETAILS + " TEXT, " +
            DBContract.Event.START_TIMESTAMP + " TEXT, " +
            DBContract.Event.END_TIMESTAMP + " TEXT, " +
            DBContract.Event.PROFILE_LINK + " TEXT, " +
            DBContract.Event.EVENT_STATUS + " TEXT, " +
            DBContract.Event.BROADCAST_TIME + " TEXT, " +
            DBContract.Event.UPDATED + " TEXT, " +
            DBContract.Event.IMPRESSIONS + " TEXT, " +
            DBContract.Event.TICKET_SEATS_1 + " TEXT, " +
            DBContract.Event.TICKET_SEATS_2 + " TEXT, " +
            DBContract.Event.TICKET_PRICE_1 + " TEXT, " +
            DBContract.Event.TICKET_PRICE_2 + " TEXT, " +
            DBContract.Event.MERCHANT_CODE + " TEXT, " +

            DBContract.Event.MERCHANT_NAME + " TEXT, " +
            DBContract.Event.TICKET_NAME_1 + " TEXT, " +
            DBContract.Event.TICKET_NAME_2 + " TEXT, " +
            DBContract.Event.TICKET_PROMO_CODE + " TEXT, " +
            DBContract.Event.AVAILABLE_TICKETS + " TEXT, " +
            DBContract.Event.UPDATE_TIME + " TEXT, " +
            DBContract.Event.ECOCASH_TYPE + " TEXT, " +
            DBContract.Event.START_DATE + " TEXT, " +
            DBContract.Event.START_TIME + " TEXT, " +
            DBContract.Event.BRAND_NAME + " TEXT, " +
            DBContract.Event.BRAND_LINK + " TEXT, " +
            DBContract.Event.MESSAGE_SEEN + " TEXT, " +
            DBContract.Event.MXE_CODE + " TEXT, " +
            DBContract.Event.VERIFIED + " TEXT, " +
            DBContract.Event.SIGNATURE_VERSION + " TEXT, " +
            DBContract.Event.COL_35 + " TEXT, " +
            DBContract.Event.COL_36 + " TEXT, " +
            DBContract.Event.COL_37 + " TEXT, " +
            DBContract.Event.COL_38 + " TEXT, " +
            DBContract.Event.COL_39 + " TEXT, " +
            DBContract.Event.COL_40 +" TEXT);";

    private static final String Ad = "CREATE TABLE "+ DBContract.Ad.TABLE_NAME +"("+
            DBContract.Ad.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Ad.LOCAL_UID + " TEXT, " +
            DBContract.Ad.BROADCASTER_UID + " TEXT, " +
            DBContract.Ad.KEY + " TEXT, " +
            DBContract.Ad.INTEREST_CODE + " TEXT, " +
            DBContract.Ad.LOCATION_CODE + " TEXT, " +
            DBContract.Ad.BRAND_NAME + " TEXT, " +
            DBContract.Ad.TITLE + " TEXT, " +
            DBContract.Ad.DETAILS + " TEXT, " +
            DBContract.Ad.PROFILE_LINK + " TEXT, " +
            DBContract.Ad.DURATION + " TEXT, " +
            DBContract.Ad.BROADCAST_TIME + " TEXT, " +
            DBContract.Ad.VIDEO + " TEXT, " +
            DBContract.Ad.STATUS + " TEXT, " +
            DBContract.Ad.IMPRESSIONS + " TEXT, " +
            DBContract.Ad.UPDATE_TIME + " TEXT, " +
            DBContract.Ad.BRAND_LINK + " TEXT, " +
            DBContract.Ad.VERIFIED + " TEXT, " +
            DBContract.Ad.SIGNATURE_VERSION + " TEXT, " +
            DBContract.Ad.COl_18 + " TEXT, " +
            DBContract.Ad.COl_19 + " TEXT, " +
            DBContract.Ad.COl_20 +" TEXT);";

    private static final String Comments = "CREATE TABLE "+ DBContract.Comments.TABLE_NAME +"("+
            DBContract.Comments.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Comments.LOCAL_UID + " TEXT, " +
            DBContract.Comments.UID + " TEXT, " +
            DBContract.Comments.KEY + " TEXT, " +
            DBContract.Comments.POST_KEY + " TEXT, " +
            DBContract.Comments.USERNAME + " TEXT, " +
            DBContract.Comments.COMMENT + " TEXT, " +
            DBContract.Comments.TIMESTAMP + " TEXT, " +
            DBContract.Comments.VERIFIED + " TEXT, " +
            DBContract.Comments.BROADCASTER_UID + " TEXT, " +
            DBContract.Comments.BROADCAST_NAME + " TEXT, " +
            DBContract.Comments.PLOFILE_LINK + " TEXT, " +
            DBContract.Comments.SIGNATURE_VERSION + " TEXT, " +
            DBContract.Comments.COl_12 + " TEXT, " +
            DBContract.Comments.COl_13 + " TEXT, " +
            DBContract.Comments.COl_14 + " TEXT, " +
            DBContract.Comments.COl_15 + " TEXT, " +
            DBContract.Comments.COl_16 + " TEXT, " +
            DBContract.Comments.COl_17 + " TEXT, " +
            DBContract.Comments.COl_18 + " TEXT, " +
            DBContract.Comments.COl_19 + " TEXT, " +
            DBContract.Comments.COl_20 +" TEXT);";

    private static final String Messages = "CREATE TABLE "+ DBContract.Messages.TABLE_NAME +"("+
            DBContract.Messages.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Messages.LOCAL_UID + " TEXT, " +
            DBContract.Messages.SEND_TO + " TEXT, " +
            DBContract.Messages.CHAT_ROOM + " TEXT, " +
            DBContract.Messages.SENDER + " TEXT, " +
            DBContract.Messages.RECEIVER + " TEXT, " +
            DBContract.Messages.SENDER_UID + " TEXT, " +
            DBContract.Messages.RECEIVER_UID + " TEXT, " +
            DBContract.Messages.KEY + " TEXT, " +
            DBContract.Messages.MESSAGE + " TEXT, " +
            DBContract.Messages.SENDER_VERIFIED + " TEXT, " +
            DBContract.Messages.LINK_SENDER + " TEXT, " +
            DBContract.Messages.LINK_RECEIVER + " TEXT, " +
            DBContract.Messages.TIMESTAMP + " TEXT, " +
            DBContract.Messages.SIGNATURE_VERSION + " TEXT, " +
            DBContract.Messages.ACC_TYPE + " TEXT, " +
            DBContract.Messages.RECEIVER_VERIFIED + " TEXT, " +
            DBContract.Messages.COl_16 + " TEXT, " +
            DBContract.Messages.COl_17 + " TEXT, " +
            DBContract.Messages.COl_18 + " TEXT, " +
            DBContract.Messages.COl_19 + " TEXT, " +
            DBContract.Messages.COl_20 +" TEXT);";

    private static final String Impressions = "CREATE TABLE "+ DBContract.Impressions.TABLE_NAME +"("+
            DBContract.Impressions.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Impressions.LOCAL_UID + " TEXT, " +
            DBContract.Impressions.UID + " TEXT, " +
            DBContract.Impressions.KEY + " TEXT, " +
            DBContract.Impressions.USERNAME + " TEXT, " +
            DBContract.Impressions.LOCATION + " TEXT, " +
            DBContract.Impressions.PROFILE_LINK + " TEXT, " +
            DBContract.Impressions.TIMESTAMP + " TEXT, " +
            DBContract.Impressions.VERIFIED + " TEXT, " +
            DBContract.Impressions.SIGNATURE_VERSION + " TEXT, " +
            DBContract.Impressions.TYPE + " TEXT, " +
            DBContract.Impressions.COl_10 + " TEXT, " +
            DBContract.Impressions.COl_11 + " TEXT, " +
            DBContract.Impressions.COl_12 + " TEXT, " +
            DBContract.Impressions.COl_13 + " TEXT, " +
            DBContract.Impressions.COl_14 + " TEXT, " +
            DBContract.Impressions.COl_15 + " TEXT, " +
            DBContract.Impressions.COl_16 + " TEXT, " +
            DBContract.Impressions.COl_17 + " TEXT, " +
            DBContract.Impressions.COl_18 + " TEXT, " +
            DBContract.Impressions.COl_19 + " TEXT, " +
            DBContract.Impressions.COl_20 +" TEXT);";

    private static final String Businesses = "CREATE TABLE "+ DBContract.Businesses.TABLE_NAME +"("+
            DBContract.Businesses.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Businesses.LOCAL_UID + " TEXT, " +
            DBContract.Businesses.UID + " TEXT, " +
            DBContract.Businesses.KEY + " TEXT, " +
            DBContract.Businesses.BRAND_NAME + " TEXT, " +
            DBContract.Businesses.LOCATION + " TEXT, " +
            DBContract.Businesses.WEBSITE + " TEXT, " +
            DBContract.Businesses.EMAIL + " TEXT, " +
            DBContract.Businesses.DESCRIPTION + " TEXT, " +
            DBContract.Businesses.CATEGORY + " TEXT, " +
            DBContract.Businesses.VERIFIED + " TEXT, " +
            DBContract.Businesses.PROFILE_LINK + " TEXT, " +
            DBContract.Businesses.SIGNATURE_VERSION + " TEXT, " +
            DBContract.Businesses.COl_12 + " TEXT, " +
            DBContract.Businesses.COl_13 + " TEXT, " +
            DBContract.Businesses.COl_14 + " TEXT, " +
            DBContract.Businesses.COl_15 + " TEXT, " +
            DBContract.Businesses.COl_16 + " TEXT, " +
            DBContract.Businesses.COl_17 + " TEXT, " +
            DBContract.Businesses.COl_18 + " TEXT, " +
            DBContract.Businesses.COl_19 + " TEXT, " +
            DBContract.Businesses.COl_20 +" TEXT);";

    private static final String Tickets = "CREATE TABLE "+ DBContract.Tickets.TABLE_NAME +"("+
            DBContract.Tickets.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Tickets.LOCAL_UID + " TEXT, " +
            DBContract.Tickets.UID + " TEXT, " +
            DBContract.Tickets.BROADCASTER_UID + " TEXT, " +
            DBContract.Tickets.EVENT_KEY + " TEXT, " +
            DBContract.Tickets.NAME + " TEXT, " +
            DBContract.Tickets.VENUE + " TEXT, " +
            DBContract.Tickets.LOCATION + " TEXT, " +
            DBContract.Tickets.TICKET_NUMBER + " TEXT, " +
            DBContract.Tickets.PRICE + " TEXT, " +
            DBContract.Tickets.START_TIMESTAMP + " TEXT, " +
            DBContract.Tickets.CATEGORY + " TEXT, " +
            DBContract.Tickets.END_TIMESTAMP + " TEXT, " +
            DBContract.Tickets.MARCHANT_NAME + " TEXT, " +
            DBContract.Tickets.TICKET_KEY + " TEXT, " +
            DBContract.Tickets.VALID + " TEXT, " +
            DBContract.Tickets.MTS_CODE + " TEXT, " +
            DBContract.Tickets.NUM + " TEXT, " +
            DBContract.Tickets.COl_17 + " TEXT, " +
            DBContract.Tickets.COl_18 + " TEXT, " +
            DBContract.Tickets.COl_19 + " TEXT, " +
            DBContract.Tickets.COl_20 +" TEXT);";

    private static final String Friends = "CREATE TABLE "+ DBContract.Friends.TABLE_NAME +"("+
            DBContract.Friends.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Friends.LOCAL_UID + " TEXT, " +
            DBContract.Friends.UID + " TEXT, " +
            DBContract.Friends.USERNAME + " TEXT, " +
            DBContract.Friends.PROFILE_LINK + " TEXT, " +
            DBContract.Friends.CONTACT_NUMBER + " TEXT, " +
            DBContract.Friends.SIGNATURE_VERSION + " TEXT, " +
            DBContract.Friends.COl_6 + " TEXT, " +
            DBContract.Friends.COl_7 + " TEXT, " +
            DBContract.Friends.COl_8 + " TEXT, " +
            DBContract.Friends.COl_9 + " TEXT, " +
            DBContract.Friends.COl_10 + " TEXT, " +
            DBContract.Friends.COl_11 + " TEXT, " +
            DBContract.Friends.COl_12 + " TEXT, " +
            DBContract.Friends.COl_13 + " TEXT, " +
            DBContract.Friends.COl_14 + " TEXT, " +
            DBContract.Friends.COl_15 + " TEXT, " +
            DBContract.Friends.COl_16 + " TEXT, " +
            DBContract.Friends.COl_17 + " TEXT, " +
            DBContract.Friends.COl_18 + " TEXT, " +
            DBContract.Friends.COl_19 + " TEXT, " +
            DBContract.Friends.COl_20 +" TEXT);";

    private static final String Moments = "CREATE TABLE "+ DBContract.Moments.TABLE_NAME +"("+
            DBContract.Moments.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Moments.LOCAL_UID + " TEXT, " +
            DBContract.Moments.KEY + " TEXT, " +
            DBContract.Moments.TYPE + " TEXT, " +
            DBContract.Moments.USERNAME + " TEXT, " +
            DBContract.Moments.STORY + " TEXT, " +
            DBContract.Moments.MOMENT_LINK + " TEXT, " +
            DBContract.Moments.PROFILE_LINK + " TEXT, " +
            DBContract.Moments.TIMESTAMP + " TEXT, " +
            DBContract.Moments.CONTACT_NUMBER + " TEXT, " +
            DBContract.Moments.SIGNATURE_VERSION + " TEXT, " +
            DBContract.Moments.COl_10 + " TEXT, " +
            DBContract.Moments.COl_11 + " TEXT, " +
            DBContract.Moments.COl_12 + " TEXT, " +
            DBContract.Moments.COl_13 + " TEXT, " +
            DBContract.Moments.COl_14 + " TEXT, " +
            DBContract.Moments.COl_15 + " TEXT, " +
            DBContract.Moments.COl_16 + " TEXT, " +
            DBContract.Moments.COl_17 + " TEXT, " +
            DBContract.Moments.COl_18 + " TEXT, " +
            DBContract.Moments.COl_19 + " TEXT, " +
            DBContract.Moments.COl_20 +" TEXT);";

    private static final String QUERY_12 = "CREATE TABLE "+ DBContract.Table12.TABLE_NAME +"("+
            DBContract.Table12.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Table12.LOCAL_UID + " TEXT, " +
            DBContract.Table12.COl_1 + " TEXT, " +
            DBContract.Table12.COl_2 + " TEXT, " +
            DBContract.Table12.COl_3 + " TEXT, " +
            DBContract.Table12.COl_4 + " TEXT, " +
            DBContract.Table12.COl_5 + " TEXT, " +
            DBContract.Table12.COl_6 + " TEXT, " +
            DBContract.Table12.COl_7 + " TEXT, " +
            DBContract.Table12.COl_8 + " TEXT, " +
            DBContract.Table12.COl_9 + " TEXT, " +
            DBContract.Table12.COl_10 + " TEXT, " +
            DBContract.Table12.COl_11 + " TEXT, " +
            DBContract.Table12.COl_12 + " TEXT, " +
            DBContract.Table12.COl_13 + " TEXT, " +
            DBContract.Table12.COl_14 + " TEXT, " +
            DBContract.Table12.COl_15 + " TEXT, " +
            DBContract.Table12.COl_16 + " TEXT, " +
            DBContract.Table12.COl_17 + " TEXT, " +
            DBContract.Table12.COl_18 + " TEXT, " +
            DBContract.Table12.COl_19 + " TEXT, " +
            DBContract.Table12.COl_20 +" TEXT);";

    private static final String QUERY_13 = "CREATE TABLE "+ DBContract.Table13.TABLE_NAME +"("+
            DBContract.Table13.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Table13.LOCAL_UID + " TEXT, " +
            DBContract.Table13.COl_1 + " TEXT, " +
            DBContract.Table13.COl_2 + " TEXT, " +
            DBContract.Table13.COl_3 + " TEXT, " +
            DBContract.Table13.COl_4 + " TEXT, " +
            DBContract.Table13.COl_5 + " TEXT, " +
            DBContract.Table13.COl_6 + " TEXT, " +
            DBContract.Table13.COl_7 + " TEXT, " +
            DBContract.Table13.COl_8 + " TEXT, " +
            DBContract.Table13.COl_9 + " TEXT, " +
            DBContract.Table13.COl_10 + " TEXT, " +
            DBContract.Table13.COl_11 + " TEXT, " +
            DBContract.Table13.COl_12 + " TEXT, " +
            DBContract.Table13.COl_13 + " TEXT, " +
            DBContract.Table13.COl_14 + " TEXT, " +
            DBContract.Table13.COl_15 + " TEXT, " +
            DBContract.Table13.COl_16 + " TEXT, " +
            DBContract.Table13.COl_17 + " TEXT, " +
            DBContract.Table13.COl_18 + " TEXT, " +
            DBContract.Table13.COl_19 + " TEXT, " +
            DBContract.Table13.COl_20 +" TEXT);";

    private static final String QUERY_14 = "CREATE TABLE "+ DBContract.Table14.TABLE_NAME +"("+
            DBContract.Table14.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Table14.LOCAL_UID + " TEXT, " +
            DBContract.Table14.COl_1 + " TEXT, " +
            DBContract.Table14.COl_2 + " TEXT, " +
            DBContract.Table14.COl_3 + " TEXT, " +
            DBContract.Table14.COl_4 + " TEXT, " +
            DBContract.Table14.COl_5 + " TEXT, " +
            DBContract.Table14.COl_6 + " TEXT, " +
            DBContract.Table14.COl_7 + " TEXT, " +
            DBContract.Table14.COl_8 + " TEXT, " +
            DBContract.Table14.COl_9 + " TEXT, " +
            DBContract.Table14.COl_10 + " TEXT, " +
            DBContract.Table14.COl_11 + " TEXT, " +
            DBContract.Table14.COl_12 + " TEXT, " +
            DBContract.Table14.COl_13 + " TEXT, " +
            DBContract.Table14.COl_14 + " TEXT, " +
            DBContract.Table14.COl_15 + " TEXT, " +
            DBContract.Table14.COl_16 + " TEXT, " +
            DBContract.Table14.COl_17 + " TEXT, " +
            DBContract.Table14.COl_18 + " TEXT, " +
            DBContract.Table14.COl_19 + " TEXT, " +
            DBContract.Table14.COl_20 +" TEXT);";

    private static final String QUERY_15 = "CREATE TABLE "+ DBContract.Table15.TABLE_NAME +"("+
            DBContract.Table15.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Table15.LOCAL_UID + " TEXT, " +
            DBContract.Table15.COl_1 + " TEXT, " +
            DBContract.Table15.COl_2 + " TEXT, " +
            DBContract.Table15.COl_3 + " TEXT, " +
            DBContract.Table15.COl_4 + " TEXT, " +
            DBContract.Table15.COl_5 + " TEXT, " +
            DBContract.Table15.COl_6 + " TEXT, " +
            DBContract.Table15.COl_7 + " TEXT, " +
            DBContract.Table15.COl_8 + " TEXT, " +
            DBContract.Table15.COl_9 + " TEXT, " +
            DBContract.Table15.COl_10 + " TEXT, " +
            DBContract.Table15.COl_11 + " TEXT, " +
            DBContract.Table15.COl_12 + " TEXT, " +
            DBContract.Table15.COl_13 + " TEXT, " +
            DBContract.Table15.COl_14 + " TEXT, " +
            DBContract.Table15.COl_15 + " TEXT, " +
            DBContract.Table15.COl_16 + " TEXT, " +
            DBContract.Table15.COl_17 + " TEXT, " +
            DBContract.Table15.COl_18 + " TEXT, " +
            DBContract.Table15.COl_19 + " TEXT, " +
            DBContract.Table15.COl_20 +" TEXT);";





    private static final String QUERY_16 = "CREATE TABLE "+ DBContract.Table16.TABLE_NAME +"("+
            DBContract.Table16.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Table16.LOCAL_UID + " TEXT, " +
            DBContract.Table16.COl_1 + " TEXT, " +
            DBContract.Table16.COl_2 + " TEXT, " +
            DBContract.Table16.COl_3 + " TEXT, " +
            DBContract.Table16.COl_4 + " TEXT, " +
            DBContract.Table16.COl_5 + " TEXT, " +
            DBContract.Table16.COl_6 + " TEXT, " +
            DBContract.Table16.COl_7 + " TEXT, " +
            DBContract.Table16.COl_8 + " TEXT, " +
            DBContract.Table16.COl_9 + " TEXT, " +
            DBContract.Table16.COl_10 + " TEXT, " +
            DBContract.Table16.COl_11 + " TEXT, " +
            DBContract.Table16.COl_12 + " TEXT, " +
            DBContract.Table16.COl_13 + " TEXT, " +
            DBContract.Table16.COl_14 + " TEXT, " +
            DBContract.Table16.COl_15 + " TEXT, " +
            DBContract.Table16.COl_16 + " TEXT, " +
            DBContract.Table16.COl_17 + " TEXT, " +
            DBContract.Table16.COl_18 + " TEXT, " +
            DBContract.Table16.COl_19 + " TEXT, " +
            DBContract.Table16.COl_20 +" TEXT);";

    private static final String QUERY_17 = "CREATE TABLE "+ DBContract.Table17.TABLE_NAME +"("+
            DBContract.Table17.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Table17.LOCAL_UID + " TEXT, " +
            DBContract.Table17.COl_1 + " TEXT, " +
            DBContract.Table17.COl_2 + " TEXT, " +
            DBContract.Table17.COl_3 + " TEXT, " +
            DBContract.Table17.COl_4 + " TEXT, " +
            DBContract.Table17.COl_5 + " TEXT, " +
            DBContract.Table17.COl_6 + " TEXT, " +
            DBContract.Table17.COl_7 + " TEXT, " +
            DBContract.Table17.COl_8 + " TEXT, " +
            DBContract.Table17.COl_9 + " TEXT, " +
            DBContract.Table17.COl_10 + " TEXT, " +
            DBContract.Table17.COl_11 + " TEXT, " +
            DBContract.Table17.COl_12 + " TEXT, " +
            DBContract.Table17.COl_13 + " TEXT, " +
            DBContract.Table17.COl_14 + " TEXT, " +
            DBContract.Table17.COl_15 + " TEXT, " +
            DBContract.Table17.COl_16 + " TEXT, " +
            DBContract.Table17.COl_17 + " TEXT, " +
            DBContract.Table17.COl_18 + " TEXT, " +
            DBContract.Table17.COl_19 + " TEXT, " +
            DBContract.Table17.COl_20 +" TEXT);";

    private static final String QUERY_18 = "CREATE TABLE "+ DBContract.Table18.TABLE_NAME +"("+
            DBContract.Table12.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Table12.LOCAL_UID + " TEXT, " +
            DBContract.Table12.COl_1 + " TEXT, " +
            DBContract.Table12.COl_2 + " TEXT, " +
            DBContract.Table12.COl_3 + " TEXT, " +
            DBContract.Table12.COl_4 + " TEXT, " +
            DBContract.Table12.COl_5 + " TEXT, " +
            DBContract.Table12.COl_6 + " TEXT, " +
            DBContract.Table12.COl_7 + " TEXT, " +
            DBContract.Table12.COl_8 + " TEXT, " +
            DBContract.Table12.COl_9 + " TEXT, " +
            DBContract.Table12.COl_10 + " TEXT, " +
            DBContract.Table12.COl_11 + " TEXT, " +
            DBContract.Table12.COl_12 + " TEXT, " +
            DBContract.Table12.COl_13 + " TEXT, " +
            DBContract.Table12.COl_14 + " TEXT, " +
            DBContract.Table12.COl_15 + " TEXT, " +
            DBContract.Table12.COl_16 + " TEXT, " +
            DBContract.Table12.COl_17 + " TEXT, " +
            DBContract.Table12.COl_18 + " TEXT, " +
            DBContract.Table12.COl_19 + " TEXT, " +
            DBContract.Table12.COl_20 +" TEXT);";

    private static final String QUERY_19 = "CREATE TABLE "+ DBContract.Table19.TABLE_NAME +"("+
            DBContract.Table13.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Table13.LOCAL_UID + " TEXT, " +
            DBContract.Table13.COl_1 + " TEXT, " +
            DBContract.Table13.COl_2 + " TEXT, " +
            DBContract.Table13.COl_3 + " TEXT, " +
            DBContract.Table13.COl_4 + " TEXT, " +
            DBContract.Table13.COl_5 + " TEXT, " +
            DBContract.Table13.COl_6 + " TEXT, " +
            DBContract.Table13.COl_7 + " TEXT, " +
            DBContract.Table13.COl_8 + " TEXT, " +
            DBContract.Table13.COl_9 + " TEXT, " +
            DBContract.Table13.COl_10 + " TEXT, " +
            DBContract.Table13.COl_11 + " TEXT, " +
            DBContract.Table13.COl_12 + " TEXT, " +
            DBContract.Table13.COl_13 + " TEXT, " +
            DBContract.Table13.COl_14 + " TEXT, " +
            DBContract.Table13.COl_15 + " TEXT, " +
            DBContract.Table13.COl_16 + " TEXT, " +
            DBContract.Table13.COl_17 + " TEXT, " +
            DBContract.Table13.COl_18 + " TEXT, " +
            DBContract.Table13.COl_19 + " TEXT, " +
            DBContract.Table13.COl_20 +" TEXT);";

    private static final String QUERY_20= "CREATE TABLE "+ DBContract.Table20.TABLE_NAME +"("+
            DBContract.Table14.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Table14.LOCAL_UID + " TEXT, " +
            DBContract.Table14.COl_1 + " TEXT, " +
            DBContract.Table14.COl_2 + " TEXT, " +
            DBContract.Table14.COl_3 + " TEXT, " +
            DBContract.Table14.COl_4 + " TEXT, " +
            DBContract.Table14.COl_5 + " TEXT, " +
            DBContract.Table14.COl_6 + " TEXT, " +
            DBContract.Table14.COl_7 + " TEXT, " +
            DBContract.Table14.COl_8 + " TEXT, " +
            DBContract.Table14.COl_9 + " TEXT, " +
            DBContract.Table14.COl_10 + " TEXT, " +
            DBContract.Table14.COl_11 + " TEXT, " +
            DBContract.Table14.COl_12 + " TEXT, " +
            DBContract.Table14.COl_13 + " TEXT, " +
            DBContract.Table14.COl_14 + " TEXT, " +
            DBContract.Table14.COl_15 + " TEXT, " +
            DBContract.Table14.COl_16 + " TEXT, " +
            DBContract.Table14.COl_17 + " TEXT, " +
            DBContract.Table14.COl_18 + " TEXT, " +
            DBContract.Table14.COl_19 + " TEXT, " +
            DBContract.Table14.COl_20 +" TEXT);";

    private static final String QUERY_21 = "CREATE TABLE "+ DBContract.Table21.TABLE_NAME +"("+
            DBContract.Table21.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.Table21.LOCAL_UID + " TEXT, " +
            DBContract.Table21.COl_1 + " TEXT, " +
            DBContract.Table21.COl_2 + " TEXT, " +
            DBContract.Table21.COl_3 + " TEXT, " +
            DBContract.Table21.COl_4 + " TEXT, " +
            DBContract.Table21.COl_5 + " TEXT, " +
            DBContract.Table21.COl_6 + " TEXT, " +
            DBContract.Table21.COl_7 + " TEXT, " +
            DBContract.Table21.COl_8 + " TEXT, " +
            DBContract.Table21.COl_9 + " TEXT, " +
            DBContract.Table21.COl_10 + " TEXT, " +
            DBContract.Table21.COl_11 + " TEXT, " +
            DBContract.Table21.COl_12 + " TEXT, " +
            DBContract.Table21.COl_13 + " TEXT, " +
            DBContract.Table21.COl_14 + " TEXT, " +
            DBContract.Table21.COl_15 + " TEXT, " +
            DBContract.Table21.COl_16 + " TEXT, " +
            DBContract.Table21.COl_17 + " TEXT, " +
            DBContract.Table21.COl_18 + " TEXT, " +
            DBContract.Table21.COl_19 + " TEXT, " +
            DBContract.Table21.COl_20 +" TEXT);";
}