package tech.myevents.app;


import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentEvent extends Fragment implements View.OnClickListener {

    ListView listView;
    TextView tvEmpty;
    ProgressBar progressBar;

    SharedPreferences prefs;
    SharedPreferences prefsApp;
    SharedPreferences.Editor editor;

    static int SCROLL_POS = 0;
    static long LAST_LOAD = 0;
    static boolean NETWORK_AVAILABLE = false;
    static boolean PAUSED = false;

    DBOperations dbOperations;
    SQLiteDatabase db;
    List<String> eventKey;
    FirebaseAuth auth;
    FirebaseUser firebaseUser = null;

    ChildEventListener listener;
    static long echo;
    String accType, businessInterestCode;

    GetSetterUpdate setterUpdate;
    private static List listUpdate = new ArrayList<>();
    Parcelable state;

    public FragmentEvent() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        dbOperations = new DBOperations(getContext());
        db = dbOperations.getWritableDatabase();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eventKey = dbOperations.getEventKeys(db, localUid);
        auth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getContext().getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
        prefsApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        StaticVariables.eventsAdapter = new AdapterEvent(getContext(), R.layout.row_events);

        listView = (ListView) view.findViewById(R.id.listView);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        if (accType.equals(StaticVariables.business))
            for (int x = 0; x < StaticVariables.interestCodes.length; x++)
                if (prefs.getString(AppInfo.BUSINESS_CATEGORY, StaticVariables.categories[13])
                        .equals(StaticVariables.categories[x]))
                    businessInterestCode = StaticVariables.interestCodes[x];

        if (StaticVariables.listEvents != null)
            initializeListView();
        else
            new EventsBackTask().execute();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1258);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvEmpty:
                LAST_LOAD = 0;
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SCROLL_POS = listView.getFirstVisiblePosition();
    }







    private void initializeListView() {
        if (StaticVariables.listEvents.size() != 0) {
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            listView.setAdapter(StaticVariables.eventsAdapter);
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.setSelection(SCROLL_POS);
                }
            });
            updateValues();
        } else {
            //listView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            //progressBar.setVisibility(View.GONE);
        }
    }

    private long getCurrentTimestamp() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        return calendar.getTimeInMillis();
    }

    private class EventsBackTask extends AsyncTask<Void, GetSetterEvent, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Cursor cursor = dbOperations.getEvents(db, localUid);
            int count = cursor.getCount();

            if (count != 0)
                StaticVariables.listEvents = new ArrayList<>();

            int impressions, ticketSeats1, ticketSeats2;
            int ticketsPrice1, ticketPrice2, merchantCode, availableTickets, sigVer;
            long startTimestamp, endTimestamp, broadcastTime, updateTime;
            String broadcasterUid, eventKey, interestCode, locationCode, name, venue, details,
                    profileLink, eventStatus, startDate, startTime, mxeCode,
                    ticketName1, ticketName2, merchantName, ticketPromoCode, ecocashType, brandName, brandLink, id;
            boolean verified;

            while (cursor.moveToNext()) {
                id = cursor.getString(cursor.getColumnIndex(DBContract.Event.ID));
                startTimestamp = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBContract.Event.START_TIMESTAMP)));
                impressions = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Event.IMPRESSIONS)));
                ticketSeats1 = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Event.TICKET_SEATS_1)));
                ticketSeats2 = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Event.TICKET_SEATS_2)));
                ticketsPrice1 = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Event.TICKET_PRICE_1)));
                ticketPrice2 = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Event.TICKET_PRICE_2)));
                merchantCode = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Event.MERCHANT_CODE)));
                endTimestamp = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBContract.Event.END_TIMESTAMP)));
                broadcastTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBContract.Event.BROADCAST_TIME)));
                if (getCurrentTimestamp() > startTimestamp) {
                    count--;
                    continue;
                }
                broadcasterUid = cursor.getString(cursor.getColumnIndex(DBContract.Event.BROADCASTER_UID));
                eventKey = cursor.getString(cursor.getColumnIndex(DBContract.Event.KEY));
                interestCode = cursor.getString(cursor.getColumnIndex(DBContract.Event.INTEREST_CODE));
                locationCode = cursor.getString(cursor.getColumnIndex(DBContract.Event.LOCATION_CODE));
                name = cursor.getString(cursor.getColumnIndex(DBContract.Event.EVENT_NAME));
                venue = cursor.getString(cursor.getColumnIndex(DBContract.Event.EVENT_VENUE));
                details = cursor.getString(cursor.getColumnIndex(DBContract.Event.DETAILS));
                profileLink = cursor.getString(cursor.getColumnIndex(DBContract.Event.PROFILE_LINK));
                eventStatus = cursor.getString(cursor.getColumnIndex(DBContract.Event.EVENT_STATUS));
                startDate = cursor.getString(cursor.getColumnIndex(DBContract.Event.START_DATE));
                startTime = cursor.getString(cursor.getColumnIndex(DBContract.Event.START_TIME));

                ticketName1 = cursor.getString(cursor.getColumnIndex(DBContract.Event.TICKET_NAME_1));
                ticketName2 = cursor.getString(cursor.getColumnIndex(DBContract.Event.TICKET_NAME_2));
                merchantName = cursor.getString(cursor.getColumnIndex(DBContract.Event.MERCHANT_NAME));

                ticketPromoCode = cursor.getString(cursor.getColumnIndex(DBContract.Event.TICKET_PROMO_CODE));
                availableTickets = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Event.AVAILABLE_TICKETS)));
                updateTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBContract.Event.UPDATE_TIME)));
                ecocashType = cursor.getString(cursor.getColumnIndex(DBContract.Event.ECOCASH_TYPE));
                brandName = cursor.getString(cursor.getColumnIndex(DBContract.Event.BRAND_NAME));
                brandLink = cursor.getString(cursor.getColumnIndex(DBContract.Event.BRAND_LINK));
                mxeCode = cursor.getString(cursor.getColumnIndex(DBContract.Event.MXE_CODE));
                String veri = cursor.getString(cursor.getColumnIndex(DBContract.Event.VERIFIED));
                sigVer = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Event.SIGNATURE_VERSION)));

                if (veri == null || veri.equals("no"))
                    verified = false;
                else
                    verified = true;

                GetSetterEvent setter = new GetSetterEvent(impressions,
                        ticketSeats1, ticketSeats2, ticketsPrice1, ticketPrice2,
                        startTimestamp, endTimestamp, broadcastTime, broadcasterUid, brandName, brandLink, eventKey, interestCode,
                        locationCode, name, venue, details, profileLink, eventStatus, startDate, startTime
                        , ticketName1, ticketName2, 0, merchantCode, merchantName, "", locationCode,
                        ticketPromoCode, availableTickets, updateTime, ecocashType, mxeCode, verified, sigVer);

                setterUpdate = new GetSetterUpdate("business", id, broadcasterUid, "", "");
                listUpdate.add(setterUpdate);

                publishProgress(setter);
            }
            cursor.close();

            return count;
        }

        @Override
        protected void onProgressUpdate(GetSetterEvent... values) {
            StaticVariables.eventsAdapter.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count != 0) {
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
                listView.setAdapter(StaticVariables.eventsAdapter);
                LAST_LOAD = getCurrentTimestamp();
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        listView.setSelection(SCROLL_POS);
                    }
                });
                updateValues();
            } else {
                //progressBar.setVisibility(View.GONE);
                //listView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
            addFirebaseListener();
        }

    }

    private void updateValues() {
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childBusinesses)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        for (DataSnapshot snap: dataSnapshot.getChildren()) {
                            GetSetterBusiness setterBusiness = snap.getValue(GetSetterBusiness.class);

                            for (int x =0; x < listUpdate.size(); x++) {
                                GetSetterUpdate setterUpdate = (GetSetterUpdate) listUpdate.get(x);
                                if (setterUpdate.getUid().equals(setterBusiness.getUid())) {

                                    for (int y = 0; y < StaticVariables.listEvents.size(); y++) {
                                        GetSetterEvent setterEvent = (GetSetterEvent) StaticVariables.listEvents.get(y);
                                        if (setterEvent.getBroadcasterUid().equals(setterUpdate.getUid())) {
                                            setterEvent.setBrandName(setterBusiness.getBrandName());
                                            setterEvent.setBrandLink(setterBusiness.getProfileLink());
                                            setterEvent.setVerified(setterBusiness.isVerified());
                                            setterEvent.setSignatureVersion(setterBusiness.getSignatureVersion());
                                            StaticVariables.eventsAdapter.notifyDataSetChanged();
                                        }
                                    }

                                    ContentValues values = new ContentValues();
                                    values.put(DBContract.Event.BRAND_NAME, setterBusiness.getBrandName());
                                    values.put(DBContract.Event.BRAND_LINK, setterBusiness.getProfileLink());
                                    values.put(DBContract.Event.VERIFIED, setterBusiness.isVerified() ? "yes": "no");
                                    values.put(DBContract.Event.SIGNATURE_VERSION, "" + setterBusiness.getSignatureVersion());

                                    db.update(DBContract.Event.TABLE_NAME, values,
                                            DBContract.Event.ID + "='" + setterUpdate.getDbId() + "'",
                                            null);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void getBusinessDetails() {
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childBusinesses)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            GetSetterBusiness setter = snapshot.getValue(GetSetterBusiness.class);

                            ContentValues values = new ContentValues();
                            values.put(DBContract.Event.BRAND_NAME, setter.getBrandName());
                            values.put(DBContract.Event.BRAND_LINK, setter.getProfileLink());
                            values.put(DBContract.Event.VERIFIED, setter.isVerified() ? "yes": "no");
                            values.put(DBContract.Event.SIGNATURE_VERSION, "" + setter.getSignatureVersion());

                            db.update(DBContract.Event.TABLE_NAME, values,
                                    DBContract.Event.BROADCASTER_UID + "='" + setter.getUid() + "'", null);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void addFirebaseListener() {
        final String userInterest = prefs.getString(AppInfo.INTEREST_CODE, "1a1b1c1d1e1f1g1h1i1j1k1l1m1n1o1p1q");
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childEvents)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            for (DataSnapshot snap: snapshot.getChildren()) {
                                GetSetterEvent setterEvent = snap.getValue(GetSetterEvent.class);
                                if (setterEvent.getEventStatus().equals(StaticVariables.deleted)) {
                                    new DeleteEvent(getContext()).execute(setterEvent);
                                    continue;
                                }

                                if (setterEvent.getEventStatus().equals(StaticVariables.eventExclusive))
                                    continue;

                                if (!userInterest.contains(setterEvent.getInterestCode()))
                                    continue;

                                if (eventKey.contains(setterEvent.getEventKey()))
                                    continue;

                                if (getCurrentTimestamp() > setterEvent.getEndTimestamp())
                                    continue;

                                if (accType.equals(StaticVariables.business))
                                    if (!setterEvent.getInterestCode().equals(businessInterestCode))
                                        continue;

                                if (StaticVariables.listEvents == null)
                                    StaticVariables.listEvents = new ArrayList<>();

                                if (StaticVariables.listEvents.size() == 0) {
                                    tvEmpty.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);

                                    StaticVariables.eventsAdapter.add(setterEvent);
                                    listView.setAdapter(StaticVariables.eventsAdapter);
                                } else {
                                    StaticVariables.listEvents.add(0, setterEvent);
                                    StaticVariables.eventsAdapter.notifyDataSetChanged();
                                }

                                new InsertEvents(getContext()).execute(setterEvent);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private class InsertEvents extends AsyncTask<GetSetterEvent, Void, Void> {

        Context context;

        private InsertEvents(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(GetSetterEvent... params) {
            GetSetterEvent setter = params[0];
            String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

            if (!eventKey.contains(setter.getEventKey())) {
                ContentValues values = new ContentValues();
                values.put(DBContract.Event.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
                values.put(DBContract.Event.IMPRESSIONS, String.valueOf(setter.getImpressions()));
                values.put(DBContract.Event.TICKET_SEATS_1, String.valueOf(setter.getTicketSeats1()));
                values.put(DBContract.Event.TICKET_SEATS_2, String.valueOf(setter.getTicketSeats2()));
                values.put(DBContract.Event.TICKET_PRICE_1, String.valueOf(setter.getTicketsPrice1()));
                values.put(DBContract.Event.TICKET_PRICE_2, String.valueOf(setter.getTicketPrice2()));
                values.put(DBContract.Event.MERCHANT_CODE, String.valueOf(setter.getMerchantCode()));
                values.put(DBContract.Event.BROADCAST_TIME, String.valueOf(setter.getBroadcastTime()));
                values.put(DBContract.Event.BROADCASTER_UID, setter.getBroadcasterUid());
                values.put(DBContract.Event.KEY, setter.getEventKey());
                values.put(DBContract.Event.INTEREST_CODE, setter.getInterestCode());
                values.put(DBContract.Event.LOCATION_CODE, setter.getEventLocation());
                values.put(DBContract.Event.EVENT_NAME, setter.getName());
                values.put(DBContract.Event.EVENT_VENUE, setter.getVenue());
                values.put(DBContract.Event.DETAILS, setter.getDetails());
                values.put(DBContract.Event.PROFILE_LINK, setter.getProfileLink());
                values.put(DBContract.Event.EVENT_STATUS, setter.getEventStatus());
                values.put(DBContract.Event.START_DATE, setter.getStartDate());
                values.put(DBContract.Event.START_TIME, setter.getStartTime());
                values.put(DBContract.Event.TICKET_NAME_1, setter.getTicketName1());
                values.put(DBContract.Event.TICKET_NAME_2, setter.getTicketName2());
                values.put(DBContract.Event.MERCHANT_NAME, setter.getMerchantName());
                values.put(DBContract.Event.START_TIMESTAMP, String.valueOf(setter.getStartTimestamp()));
                values.put(DBContract.Event.END_TIMESTAMP, String.valueOf(setter.getEndTimestamp()));

                values.put(DBContract.Event.TICKET_PROMO_CODE, setter.getTicketPromoCode());
                values.put(DBContract.Event.AVAILABLE_TICKETS, String.valueOf(setter.getAvailableTickets()));
                values.put(DBContract.Event.ECOCASH_TYPE, String.valueOf(setter.getEcocashType()));
                values.put(DBContract.Event.UPDATE_TIME, String.valueOf(setter.getUpdateTime()));

                values.put(DBContract.Event.BRAND_NAME, setter.getBrandName());
                values.put(DBContract.Event.BRAND_LINK, setter.getBrandLink());
                values.put(DBContract.Event.MXE_CODE, setter.getMxeCode());
                values.put(DBContract.Event.VERIFIED, setter.isVerified() ? "yes": "no");
                values.put(DBContract.Event.SIGNATURE_VERSION, "" + setter.getSignatureVersion());

                db.insert(DBContract.Event.TABLE_NAME, null, values);
            }
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            eventKey = dbOperations.getEventKeys(db, localUid);
            addImpression(setter.getEventKey());

            return null;
        }

    }

    private class DeleteEvent extends AsyncTask<GetSetterEvent, Void, Void> {

        Context context;

        private DeleteEvent(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(GetSetterEvent... params) {
            GetSetterEvent setter = params[0];
            String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

            ContentValues values = new ContentValues();
            values.put(DBContract.Event.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
            values.put(DBContract.Event.IMPRESSIONS, String.valueOf(setter.getImpressions()));
            values.put(DBContract.Event.TICKET_SEATS_1, String.valueOf(setter.getTicketSeats1()));
            values.put(DBContract.Event.TICKET_SEATS_2, String.valueOf(setter.getTicketSeats2()));
            values.put(DBContract.Event.TICKET_PRICE_1, String.valueOf(setter.getTicketsPrice1()));
            values.put(DBContract.Event.TICKET_PRICE_2, String.valueOf(setter.getTicketPrice2()));
            values.put(DBContract.Event.MERCHANT_CODE, String.valueOf(setter.getMerchantCode()));
            values.put(DBContract.Event.BROADCAST_TIME, String.valueOf(setter.getBroadcastTime()));
            values.put(DBContract.Event.BROADCASTER_UID, setter.getBroadcasterUid());
            values.put(DBContract.Event.KEY, setter.getEventKey());
            values.put(DBContract.Event.INTEREST_CODE, setter.getInterestCode());
            values.put(DBContract.Event.LOCATION_CODE, setter.getEventLocation());
            values.put(DBContract.Event.EVENT_NAME, setter.getName());
            values.put(DBContract.Event.EVENT_VENUE, setter.getVenue());
            values.put(DBContract.Event.DETAILS, setter.getDetails());
            values.put(DBContract.Event.PROFILE_LINK, setter.getProfileLink());
            values.put(DBContract.Event.EVENT_STATUS, setter.getEventStatus());
            values.put(DBContract.Event.START_DATE, setter.getStartDate());
            values.put(DBContract.Event.START_TIME, setter.getStartTime());
            values.put(DBContract.Event.TICKET_NAME_1, setter.getTicketName1());
            values.put(DBContract.Event.TICKET_NAME_2, setter.getTicketName2());
            values.put(DBContract.Event.MERCHANT_NAME, setter.getMerchantName());
            values.put(DBContract.Event.START_TIMESTAMP, String.valueOf(setter.getStartTimestamp()));
            values.put(DBContract.Event.END_TIMESTAMP, String.valueOf(setter.getEndTimestamp()));

            values.put(DBContract.Event.TICKET_PROMO_CODE, setter.getTicketPromoCode());
            values.put(DBContract.Event.AVAILABLE_TICKETS, String.valueOf(setter.getAvailableTickets()));
            values.put(DBContract.Event.ECOCASH_TYPE, String.valueOf(setter.getEcocashType()));
            values.put(DBContract.Event.UPDATE_TIME, String.valueOf(setter.getUpdateTime()));

            values.put(DBContract.Event.BRAND_NAME, setter.getBrandName());
            values.put(DBContract.Event.BRAND_LINK, setter.getBrandLink());
            values.put(DBContract.Event.MXE_CODE, setter.getMxeCode());
            values.put(DBContract.Event.VERIFIED, setter.isVerified() ? "yes" : "no");
            values.put(DBContract.Event.SIGNATURE_VERSION, "" + setter.getSignatureVersion());

            db.update(DBContract.Event.TABLE_NAME, values,
                    DBContract.Event.KEY + "='" + setter.getEventKey() + "'",
                    null);

            return null;
        }

    }

    private void addImpression(String eventKey) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences prefsApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        String username = prefs.getString(AppInfo.BUSINESS_BRAND_NAME, prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "..."));
        String profileLink = prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, ""));
        String location = prefs.getString(AppInfo.BUSINESS_LOCATION, prefs.getString(AppInfo.INDIVIDUAL_LOCATION, "No location"));
        int sigVer = accType.equals(StaticVariables.individual)
                ? prefs.getInt(AppInfo.SIG_VER_INDI, 0) : prefs.getInt(AppInfo.SIG_VER_BUS, 0);

        GetSetterImpression setter = new GetSetterImpression(type, uid, eventKey, username, profileLink,
                location, false, getCurrentTimestamp(), sigVer);

        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childImpressions)
                .child(eventKey)
                .child(uid)
                .setValue(setter);
    }

}
