package tech.myevents.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExclusiveEvents extends AppCompatActivity implements View.OnClickListener {

    ListView listView;
    TextView tvEmpty;
    ProgressBar progressBar;
    AutoCompleteTextView autoSearch;
    CardView cardSearchEvent;

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
    AdapterEvent eventsAdapter;
    ChildEventListener listener;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exclusive_events);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        dbOperations = new DBOperations(this);
        db = dbOperations.getWritableDatabase();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eventKey = dbOperations.getEventKeys(db, localUid);
        auth = FirebaseAuth.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);

        eventsAdapter = new AdapterEvent(this, R.layout.row_events);

        listView = (ListView) findViewById(R.id.listView);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        autoSearch = (AutoCompleteTextView) findViewById(R.id.autoSearch);
        cardSearchEvent = (CardView) findViewById(R.id.cardSearchEvent);

        if (StaticVariables.listEventsExclusive != null)
            initializeListView();
        else
            new EventsBackTask().execute();

        cardSearchEvent.setOnClickListener(this);

        String pageName = "Exclusive Events";
        GetSetterPageHits setterHits = new GetSetterPageHits(uid, "", pageName, getCurrentTimestamp());
        new PageHitsBackTask().execute(setterHits);
    }

    private void setStatusBarColor() {
        SharedPreferences prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        String accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        Window w = getWindow();
        if (accType.equals(StaticVariables.business)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.appStatusBar));
            toolbar.setBackgroundColor(getResources().getColor(R.color.appBarMainBus));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    @Override
    public void onClick(View v) {
        String mxeCode = autoSearch.getText().toString().trim();
        if (mxeCode.equals(""))
            return;
        addFirebaseListener(mxeCode);
    }

    @Override
    public void onPause() {
        super.onPause();
        SCROLL_POS = listView.getFirstVisiblePosition();
    }

    private void initializeListView() {
        if (StaticVariables.listEventsExclusive.size() != 0) {
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            listView.setAdapter(eventsAdapter);
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.setSelection(SCROLL_POS);
                }
            });
        } else {
            //listView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            //progressBar.setVisibility(View.GONE);
        }
    }





    private long getCurrentTimestamp(){
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
            Cursor cursor = dbOperations.getEventsExclusive(db, localUid);
            int count = cursor.getCount();

            if (count != 0)
                StaticVariables.listEventsExclusive = new ArrayList<>();

            int impressions, ticketSeats1, ticketSeats2, sigVer;
            int ticketsPrice1, ticketPrice2, merchantCode, availableTickets;
            long startTimestamp = 0, endTimestamp = 0, broadcastTime, updateTime;
            String broadcasterUid, eventKey, interestCode, locationCode, name, venue, details,
                    profileLink, eventStatus, startDate, startTime, mxeCode,
                    ticketName1, ticketName2, merchantName, ticketPromoCode, ecocashType, brandName = "", brandLink = "";
            boolean verified;

            while (cursor.moveToNext()) {
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

                publishProgress(setter);
            }
            cursor.close();

            return count;
        }

        @Override
        protected void onProgressUpdate(GetSetterEvent... values) {
            eventsAdapter.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count != 0) {
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
                listView.setAdapter(eventsAdapter);
                LAST_LOAD = getCurrentTimestamp();
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        listView.setSelection(SCROLL_POS);
                    }
                });
            } else {
                //progressBar.setVisibility(View.GONE);
                //listView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        }

    }

    private void addFirebaseListener(final String mxeCode) {
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childEvents)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                            for (DataSnapshot snap: dataSnapshot1.getChildren()) {
                                GetSetterEvent setter = snap.getValue(GetSetterEvent.class);
                                if (!setter.getEventStatus().equals(StaticVariables.eventExclusive))
                                    continue;

                                if (!setter.getMxeCode().equals(mxeCode))
                                    continue;

                                if (getCurrentTimestamp() > setter.getEndTimestamp())
                                    continue;

                                if (StaticVariables.listEventsExclusive == null)
                                    StaticVariables.listEventsExclusive = new ArrayList<>();

                                if (!eventKey.contains(setter.getEventKey()))
                                    if (StaticVariables.listEventsExclusive.size() == 0) {
                                        tvEmpty.setVisibility(View.GONE);
                                        progressBar.setVisibility(View.GONE);
                                        listView.setVisibility(View.VISIBLE);

                                        eventsAdapter.add(setter);
                                        listView.setAdapter(eventsAdapter);
                                    } else {
                                        StaticVariables.listEventsExclusive.add(0, setter);
                                        eventsAdapter.notifyDataSetChanged();
                                    }

                                new InsertEvents(ExclusiveEvents.this).execute(setter);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        /**/
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
                values.put(DBContract.Event.SIGNATURE_VERSION, "" + setter.getSignatureVersion());

                db.insert(DBContract.Event.TABLE_NAME, null, values);
            }
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            eventKey = dbOperations.getEventKeysExclusive(db, localUid);

            return null;
        }

    }






    class AdapterEvent extends ArrayAdapter {

        AdapterEvent(Context context, int resource) {
            super(context, resource);
        }

        SharedPreferences prefs, prefsApp;
        String accType;

        public void add(GetSetterEvent object) {
            StaticVariables.listEventsExclusive.add(object);
            super.add(object);
        }

        @Override
        public int getCount() {
            return StaticVariables.listEventsExclusive.size();
        }

        @Override
        public Object getItem(int position) {
            return StaticVariables.listEventsExclusive.get(position);
        }

        @NonNull
        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            View row = convertView;
            final Holder holder;
            if (row == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = layoutInflater.inflate(R.layout.row_events, parent, false);
                holder = new Holder();

                holder.ivBitmap = (ImageView) row.findViewById(R.id.ivBitmap);
                holder.tvName = (TextView) row.findViewById(R.id.tvName);
                holder.tvVenue = (TextView) row.findViewById(R.id.tvVenue);
                holder.tvDate = (TextView) row.findViewById(R.id.tvDate);
                holder.cardRow = (CardView) row.findViewById(R.id.cardRow);
                holder.tvTime = (TextView) row.findViewById(R.id.tvTime);
                holder.ivShare = (ImageView) row.findViewById(R.id.ivShare);
                holder.ivMessage = (ImageView) row.findViewById(R.id.ivMessage);
                holder.llComments = (LinearLayout) row.findViewById(R.id.llComments);
                holder.tvTimeReceived = (TextView) row.findViewById(R.id.tvTimeReceived);
                holder.progressBar = (ProgressBar) row.findViewById(R.id.progressBar);
                holder.tvBrandName = (TextView) row.findViewById(R.id.tvBrandName);
                holder.ivProfile = (ImageView) row.findViewById(R.id.ivProfile);
                holder.ivVerified = (ImageView) row.findViewById(R.id.ivVerified);
                holder.cardImage = (CardView) row.findViewById(R.id.cardImage);

                row.setTag(holder);
            } else {
                holder = (Holder) row.getTag();
            }

            final GetSetterEvent setter = (GetSetterEvent) getItem(position);
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            prefs = getContext().getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
            prefsApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
            accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

            try {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getBrandLink());
                Glide.with(getContext())
                        .using(new FirebaseImageLoader())
                        .load(storageReference)
                        .signature(new StringSignature("" + setter.getSignatureVersion()))
                        .crossFade()
                        .into(holder.ivProfile);
            } catch (Exception e) { }

            try {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getProfileLink());
                Glide.with(getContext())
                        .using(new FirebaseImageLoader())
                        .load(storageReference)
                        .crossFade()
                        .into(holder.ivBitmap);
            } catch (Exception e) { }

            if (setter.isVerified())
                holder.ivVerified.setVisibility(View.VISIBLE);
            else
                holder.ivVerified.setVisibility(View.GONE);

            holder.tvBrandName.setText(setter.getBrandName());
            holder.tvTimeReceived.setText(calculateTimeReceived(setter.getBroadcastTime()));
            holder.tvName.setText(setter.getName());
            holder.tvVenue.setText(setter.getVenue());
            holder.tvDate.setText(setter.getStartDate());
            holder.tvTime.setText(setter.getStartTime());

            holder.cardRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), EventDetails.class);
                    intent.putExtra("from", StaticVariables.eventExclusive);
                    intent.putExtra("position", position);
                    getContext().startActivity(intent);
                }
            });

            holder.cardImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), BusinessDetails.class);
                    intent.putExtra("from", StaticVariables.eventExclusive);
                    intent.putExtra("position", position);
                    getContext().startActivity(intent);
                }
            });
            holder.ivMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), Inbox.class);
                    intent.putExtra("from", StaticVariables.eventExclusive);
                    intent.putExtra("position", position);
                    getContext().startActivity(intent);
                }
            });
            holder.llComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), Comments.class);
                    intent.putExtra("from", StaticVariables.eventExclusive);
                    intent.putExtra("position", position);
                    getContext().startActivity(intent);
                }
            });
            holder.ivShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "This is an exclusive event", Toast.LENGTH_SHORT).show();
                }
            });

            return row;

        }

        private void shareMoment(final Holder holder, GetSetterEvent setterZ) {
            if (!prefs.getBoolean(AppInfo.CONTACT_VERIFIED, false) || accType.equals(StaticVariables.business)) {
                if (prefsApp.getBoolean(AppInfo.CONTACT_VERIFIED, false))
                    Toast.makeText(getContext(), "Verify your contact", Toast.LENGTH_SHORT).show();
                if (accType.equals(StaticVariables.business))
                    Toast.makeText(getContext(), "Switch to individual acc", Toast.LENGTH_SHORT).show();
                return;
            }

            holder.progressBar.setVisibility(View.VISIBLE);
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String key = FirebaseDatabase.getInstance().getReference().child(StaticVariables.childMoments).child(uid).push().getKey();
            String type = StaticVariables.event;
            String username = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_USERNAME, ""): prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "");
            String story = "";
            String link = setterZ.getProfileLink();
            String profileLink = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
            long timestamp = getCurrentTimestamp();
            int contactNumber = prefs.getInt(AppInfo.CONTACT_NUMBER, 0);
            int signatureVersion = prefs.getInt(AppInfo.SIG_VER_BUS, 0);

            GetSetterMoment setter = new GetSetterMoment(key, type, username, story, link,
                    timestamp, profileLink, contactNumber, signatureVersion);

            FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childMoments)
                    .child(uid)
                    .child(key)
                    .setValue(setter)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                holder.progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Moment shared", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        private void shareOnWhatsApp(GetSetterEvent setter) {
            String merchantName = setter.getMerchantName();
            int ticketPrice1 = setter.getTicketsPrice1();
            int ticketSeats1 = setter.getTicketSeats1();
            int merchantCode = setter.getMerchantCode();
            int ticketPrice2 = setter.getTicketPrice2();
            int ticketSeats2 = setter.getTicketSeats2();

            String message = "_Shared via MyEvents App_\n\n" +
                    "Event Name:\n*" + setter.getName().trim() + "*\n\n" +
                    "Event Venue:\n*" + setter.getVenue().trim() + "*\n\n" +
                    "Event City:\n*" + setter.getEventLocation().trim() + "*\n\n" +
                    "*" + setter.getStartTime() + "*\n\n" +
                    "Event Details: \n";

            if ((!merchantName.equals("") && ticketPrice1 != 0 && ticketSeats1 != 0 && merchantCode != 12345) || (!merchantName.equals("") && ticketPrice2 != 0 && ticketSeats2 != 0 && merchantCode != 12345)) {
                message += "*Tickets are Available, purchase via Ecocash.*\n";
                if (setter.getAvailableTickets() != 0)
                    message += "*Free tickets will be issued to " + setter.getAvailableTickets()+ " lucky people.*\nClick on a ticket and select Promotion Code then Confirm Promotion.\n\n" + setter.getDetails();
                else
                    message += setter.getDetails();
            } else message += "*Tickets are not Available.*\n" + setter.getDetails();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setPackage("com.whatsapp");
            getContext().startActivity(intent);
        }

        private void shareOnTwitter(String finalMessage) {
            Intent tweetIntent = new Intent(Intent.ACTION_SEND);
            tweetIntent.putExtra(Intent.EXTRA_TEXT, finalMessage);
            tweetIntent.setType("text/plain");

            PackageManager packageManager = getContext().getPackageManager();
            List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

            boolean resolved = false;
            for (ResolveInfo resolveInfo: resolveInfoList) {
                if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                    tweetIntent.setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                    resolved = true;
                    break;
                }
            }
            if (resolved) {
                getContext().startActivity(tweetIntent);
            } else {
                Intent i = new Intent();
                i.putExtra(Intent.EXTRA_TEXT, finalMessage);
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://twitter.com/intent/tweet?text=" + urlEncode(finalMessage)));
                getContext().startActivity(i);
                Toast.makeText(getContext(), "Twitter app not found", Toast.LENGTH_SHORT).show();
            }
        }


        private class Holder {
            ImageView ivBitmap, ivShare, ivMessage, ivProfile, ivVerified, ivSeen, ivSeenComments;
            TextView tvName, tvVenue, tvDate, tvTime, tvTimeReceived, tvBrandName;
            CardView cardRow, cardImage;
            LinearLayout llComments;
            ProgressBar progressBar;
        }

        private String urlEncode(String s) {
            try {
                return URLEncoder.encode(s, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return "";
            }
        }

        String getDate(long timeReceived) {
            String[] monthsSmall = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(getCurrentTimestamp());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);

            long midnight = calendar.getTimeInMillis();

            if (midnight > getCurrentTimestamp()) {
                calendar.setTimeInMillis(timeReceived);
                return "" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
            } else {
                calendar.setTimeInMillis(timeReceived);
                return "" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ",   " +
                        calendar.get(Calendar.DAY_OF_MONTH) + "-" + monthsSmall[calendar.get(Calendar.MONTH) +1] + "-" +
                        calendar.get(Calendar.YEAR);
            }
            //

        }

        String calculateTimeReceived(long timeReceived) {
            long currentTimestamp = getCurrentTimestamp();
            long min = 60000;
            long hour = 3600000;
            long day = 86400000;
            long month = day * 30;
            long year = month * 12;

            long timeBetween = currentTimestamp - timeReceived;
            String attach;
            if (timeBetween > year) {
                long unit = timeBetween / year;
                return " . " + unit + "y";
            }
            if (timeBetween > month) {
                long unit = timeBetween / month;
                return " . " + unit + "m";
            }
            if (timeBetween > day) {
                if (timeBetween > (day * 2)) {
                    attach = "d";
                } else {
                    attach = "d";
                }
                long result = timeBetween / day;

                return " . " + String.valueOf(result) + attach;
            } else if (timeBetween > hour) {
                if (timeBetween > (hour * 2)) {
                    attach = "hrs";
                } else {
                    attach = "hr";
                }

                long result = timeBetween / hour;
                return " . " + String.valueOf(result) + attach;
            } else if (timeBetween > min){

                long result = timeBetween / min;
                return String.valueOf(result) + "min";
            } else {
                long result = timeBetween / 1000;
                return " . " + String.valueOf(result) + "sec";
            }
        }

        public String getTheValue(int value){
            String theValue = String.valueOf(value);
            if (theValue.length() == 1){
                return "0"+theValue;
            } else {
                return theValue;
            }
        }

        private String getImpression(String impression) {
            char[] aud = impression.toCharArray();
            int len = impression.length();
            StringBuilder builder = new StringBuilder();
            for (int x = 0; x < aud.length; x++) {
                if (x == len - 9) {
                    if (len != 9) {
                        builder.append(",");
                    }
                }
                if (x == len - 6) {
                    if (len != 6) {
                        builder.append(",");
                    }
                }
                if (x == len - 3) {
                    if (len != 3) {
                        builder.append(",");
                    }
                }
                builder.append(aud[x]);
            }

            return String.valueOf(builder);
        }

        private long getCurrentTimestamp(){
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

    }


}
