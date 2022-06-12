package tech.myevents.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventDetails extends AppCompatActivity implements View.OnClickListener {

    FloatingActionButton fabBusiness;
    CardView cardTicketR, cardTicketS;
    View viewImpression;
    ImageView ivProfile;
    TextView tvDetails;
    TextView tvVenue, tvLocation, tvStartTime, tvStartDate, tvEndTime, tvEndDate, tvImpressions;

    TextView tvCategoryR, tvTicketR, tvTimeR, tvMonthR, tvVenueR, tvLocationR, tvNameR, tvMerchantNameR, tvPriceR;
    TextView tvCategoryS, tvTicketS, tvTimeS, tvMonthS, tvVenueS, tvLocationS, tvNameS, tvMerchantNameS, tvPriceS;


    String category1, category2, time, month, venue,
            location, name, merchantName;
    int price1, price2;

    DBOperations dbOperations;
    SQLiteDatabase db;
    List<String> impressionKeys;

    private static int position;
    private static String from = "";
    GetSetterEvent setterEvent;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setStatusBarColor();
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        assert collapsingToolbarLayout != null;

        dbOperations = new DBOperations(this);
        db = dbOperations.getWritableDatabase();

        try {
            position = getIntent().getExtras().getInt("position");
            from = getIntent().getExtras().getString("from");
        } catch (Exception e) {
        }

        if (from.equals(StaticVariables.playing))
            setterEvent = (GetSetterEvent) StaticVariables.listPlaying.get(position);
        else if (from.equals(StaticVariables.eventExclusive))
            setterEvent = (GetSetterEvent) StaticVariables.listEventsExclusive.get(position);
        else if (from.equals(StaticVariables.eventWaiting))
            setterEvent = (GetSetterEvent) StaticVariables.listEventsWaiting.get(position);
        else
            setterEvent = (GetSetterEvent) StaticVariables.listEvents.get(position);

        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        impressionKeys = dbOperations.getImpressionKeys(db, setterEvent.getEventKey(), localUid);

        fabBusiness = (FloatingActionButton) findViewById(R.id.fabBusiness);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        tvVenue = (TextView) findViewById(R.id.tvVenue);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvStartTime = (TextView) findViewById(R.id.tvStartTime);
        tvStartDate = (TextView) findViewById(R.id.tvStartDate);
        tvEndTime = (TextView) findViewById(R.id.tvEndTime);
        tvEndDate = (TextView) findViewById(R.id.tvEndDate);
        tvImpressions = (TextView) findViewById(R.id.tvImpressions);
        tvDetails = (TextView) findViewById(R.id.tvDetails);
        cardTicketR = (CardView) findViewById(R.id.cardTicketR);
        cardTicketS = (CardView) findViewById(R.id.cardTicketS);
        viewImpression = findViewById(R.id.viewImpression);

        tvCategoryR = (TextView) findViewById(R.id.tvCategoryR);
        tvTicketR = (TextView) findViewById(R.id.tvTicketR);
        tvTimeR = (TextView) findViewById(R.id.tvTimeR);
        tvMonthR = (TextView) findViewById(R.id.tvMonthR);
        tvVenueR = (TextView) findViewById(R.id.tvVenueR);
        tvLocationR = (TextView) findViewById(R.id.tvLocationR);
        tvNameR = (TextView) findViewById(R.id.tvNameR);
        tvMerchantNameR = (TextView) findViewById(R.id.tvMerchantNameR);
        tvPriceR = (TextView) findViewById(R.id.tvPriceR);

        tvCategoryS = (TextView) findViewById(R.id.tvCategoryS);
        tvTicketS = (TextView) findViewById(R.id.tvTicketS);
        tvTimeS = (TextView) findViewById(R.id.tvTimeS);
        tvMonthS = (TextView) findViewById(R.id.tvMonthS);
        tvVenueS = (TextView) findViewById(R.id.tvVenueS);
        tvLocationS = (TextView) findViewById(R.id.tvLocationS);
        tvNameS = (TextView) findViewById(R.id.tvNameS);
        tvMerchantNameS = (TextView) findViewById(R.id.tvMerchantNameS);
        tvPriceS = (TextView) findViewById(R.id.tvPriceS);

        category1 = setterEvent.getTicketName1();
        category2 = setterEvent.getTicketName2();
        time = setterEvent.getStartTime();
        month = setterEvent.getStartDate();
        venue = setterEvent.getVenue();
        location = setterEvent.getEventLocation();
        name = setterEvent.getName();
        merchantName = setterEvent.getMerchantName();
        price1 = setterEvent.getTicketsPrice1();
        price2 = setterEvent.getTicketPrice2();

        getDate(setterEvent.getEndTimestamp());
        tvStartTime.setText(time);
        tvStartDate.setText(month);
        tvLocation.setText(location);
        tvVenue.setText(venue);

        tvImpressions.setText("" + setterEvent.getImpressions() + "");
        tvImpressions.setText("" + "" + dbOperations.getImpressionCount(db, setterEvent.getEventKey()));
        tvDetails.setText(setterEvent.getDetails());

        insertUserProfile();
        insertStaticTextValues();
        cardTicketR.setOnClickListener(this);
        cardTicketS.setOnClickListener(this);
        tvImpressions.setOnClickListener(this);
        viewImpression.setOnClickListener(this);

        collapsingToolbarLayout.setTitle(setterEvent.getName());
        addFirebaseListener();
        getTicketsBought();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Event Details";
        GetSetterPageHits setterHits = new GetSetterPageHits(uid, "", pageName, System.currentTimeMillis());
        new PageHitsBackTask().execute(setterHits);

    }

    private void setStatusBarColor() {
        SharedPreferences prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        String accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        Window w = getWindow();
        if (accType.equals(StaticVariables.business))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.appStatusBar));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.viewImpression:
            case R.id.tvImpressions:/*
                Intent intent = new Intent(this, Impressions.class);
                intent.putExtra("key", setterEvent.getEventKey());
                startActivity(intent);*/
                break;
            case R.id.cardTicketR:
                Intent intentR = new Intent(this, TicketBuy.class);
                intentR.putExtra("from", from);
                intentR.putExtra("position", position);
                intentR.putExtra("num", 1);
                startActivity(intentR);
                break;
            case R.id.cardTicketS:
                Intent intentS = new Intent(this, TicketBuy.class);
                intentS.putExtra("from", from);
                intentS.putExtra("position", position);
                intentS.putExtra("num", 2);
                startActivity(intentS);
                break;
        }
    }

    private void getTicketsBought() {
        final int[] ticketsBought1 = {0};
        final int[] ticketsBought2 = {0};
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childTickets)
                .child(setterEvent.getEventKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                            for (DataSnapshot snap: dataSnapshot1.getChildren()) {
                                GetSetterTicket setterTicket = snap.getValue(GetSetterTicket.class);
                                if (!setterTicket.getMerchantName().equals(StaticVariables.free)) {
                                    if (setterTicket.getNum() == 1)
                                        ticketsBought1[0]++;
                                    else
                                        ticketsBought2[0]++;
                                }
                            }
                        }
                        int diff1 = ticketsBought1[0] > setterEvent.getTicketSeats1() ? setterEvent.getTicketSeats1() : ticketsBought1[0];
                        int diff2 = ticketsBought2[0] > setterEvent.getTicketSeats2() ? setterEvent.getTicketSeats2() : ticketsBought2[0];
                        String bought1 = "Tickets bought: " + diff1 + " of " + setterEvent.getTicketSeats1();
                        String bought2 = "Tickets bought: " + diff2 + " of " + setterEvent.getTicketSeats2();

                        tvTicketR.setText(bought1);
                        tvTicketS.setText(bought2);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(getApplicationContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void insertUserProfile() {
        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setterEvent.getProfileLink());
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .crossFade()
                    .into(ivProfile);
        } catch (Exception e) {
            ivProfile.setImageResource(0);
        }
    }

    private void insertStaticTextValues() {
        String[] ticketValues = {category1, category2, time, time, month, month,
                venue, venue, location, location, name, name, merchantName, merchantName, "$" + price1, "$" + price2};
        TextView[] ticketViews = {tvCategoryR, tvCategoryS, tvTimeR, tvTimeS, tvMonthR, tvMonthS,
                tvVenueR, tvVenueS, tvLocationR, tvLocationS, tvNameR, tvNameS, tvMerchantNameR, tvMerchantNameS,
                tvPriceR, tvPriceS};

        for (int x = 0; x < ticketValues.length; x++)
            ticketViews[x].setText(ticketValues[x]);
    }

    private void getDate(long timeReceived) {
        String[] monthsSmall = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeReceived);

        tvEndTime.setText("" + getTheValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getTheValue(calendar.get(Calendar.MINUTE)));

        tvEndDate.setText("" +
                getTheValue(calendar.get(Calendar.DAY_OF_MONTH)) + "-" + monthsSmall[calendar.get(Calendar.MONTH) + 1] + "-" +
                calendar.get(Calendar.YEAR));
    }

    public String getTheValue(int value){
        String theValue = String.valueOf(value);
        if (theValue.length() == 1){
            return "0"+theValue;
        } else {
            return theValue;
        }
    }



    private void addFirebaseListener() {
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childImpressions)
                .child(setterEvent.getEventKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;

                        tvImpressions.setText("" + dataSnapshot.getChildrenCount() + "");

                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {

                            GetSetterImpression setter = snapshot.getValue(GetSetterImpression.class);

                            if (impressionKeys.contains(setter.getUid()))
                                return;

                            new InsertImpressions(EventDetails.this).execute(setter);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private class InsertImpressions extends AsyncTask<GetSetterImpression, Void, Void> {

        Context context;

        public InsertImpressions(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(GetSetterImpression... params) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            GetSetterImpression setter = params[0];

            if (!impressionKeys.contains(setter.getUid())) {
                //makeAdImpression(setterEvent);
                ContentValues values = new ContentValues();
                values.put(DBContract.Impressions.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
                values.put(DBContract.Impressions.UID, uid);
                values.put(DBContract.Impressions.KEY, setter.getKey());
                values.put(DBContract.Impressions.USERNAME, setter.getUsername());
                values.put(DBContract.Impressions.LOCATION, setter.getLocation());
                values.put(DBContract.Impressions.PROFILE_LINK, setter.getProfileLink());
                values.put(DBContract.Impressions.TIMESTAMP, "" + setter.getTimestamp());
                values.put(DBContract.Impressions.VERIFIED, setter.isVerified() ? "yes":"no");

                db.insert(DBContract.Impressions.TABLE_NAME, null, values);
            }

            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            impressionKeys = dbOperations.getImpressionKeys(db, setterEvent.getEventKey(), localUid);

            return null;
        }

    }

}
