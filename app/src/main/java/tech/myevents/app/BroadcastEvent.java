package tech.myevents.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kosalgeek.android.photoutil.GalleryPhoto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static tech.myevents.app.MainActivity.currentPage;

public class BroadcastEvent extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    CardView cardImage, cardBroadcast, cardPay;
    ImageView ivProfile;
    AutoCompleteTextView autoLocation;
    EditText etName, etVenue, etDetails;
    TextView tvCategory, tvStartTime, tvStartDate, tvEndTime, tvEndDate, tvTickets, tvAudience, tvCost;
    ProgressBar progressBar;

    SharedPreferences prefs;
    SharedPreferences prefsApp;
    String accType;
    SharedPreferences.Editor editor;

    int yearX, monthX, dayX, yearY, monthY, dayY;
    int hourX, minuteX, hourY, minuteY;
    static final int xDATE_DIALOG_ID = 275;
    static final int xTIME_DIALOG_ID = 282;
    static final int yDATE_DIALOG_ID = 556;
    static final int yTIME_DIALOG_ID = 543;

    GalleryPhoto galleryPhoto;
    final int GALLERY_REQUEST = 27277;
    private static final int REQUEST_WRITE_IMAGE = 1994;
    private static final int REQUEST_READ_SMS = 1884;
    private static long StartStamp = 0;
    private static long EndStamp = 0;

    AlertDialog.Builder builder;

    static List broadcastCharges;
    private static List targetAudience;
    private static List appVersion;
    static List promotionCode;
    DBOperations dbOperations;
    SQLiteDatabase db;


    private static int NUMBER_OF_BROADCASTS = 0;
    private static boolean NETWORK_AVAILABLE = false;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_event);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MainActivity.currentPage = 3;
        setStatusBarColor();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
        galleryPhoto = new GalleryPhoto(this);

        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        if (accType.equals(StaticVariables.business)) {
            Window w = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.appStatusBar));
            toolbar.setBackgroundColor(getResources().getColor(R.color.appBarMainBus));
        }

        dbOperations = new DBOperations(this);
        db = dbOperations.getWritableDatabase();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, StaticVariables.locations);


        tvTickets = (TextView) findViewById(R.id.tvTickets);
        cardImage = (CardView) findViewById(R.id.cardImage);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        autoLocation = (AutoCompleteTextView) findViewById(R.id.autoLocation);
        etName = (EditText) findViewById(R.id.etName);
        etVenue = (EditText) findViewById(R.id.etVenue);
        etDetails = (EditText) findViewById(R.id.etDetails);
        tvCategory = (TextView) findViewById(R.id.tvCategory);
        tvStartTime = (TextView) findViewById(R.id.tvStartTime);
        tvStartDate = (TextView) findViewById(R.id.tvStartDate);
        tvEndTime = (TextView) findViewById(R.id.tvEndTime);
        tvEndDate = (TextView) findViewById(R.id.tvEndDate);
        cardPay = (CardView) findViewById(R.id.cardPay);
        cardBroadcast = (CardView) findViewById(R.id.cardBroadcast);
        tvAudience = (TextView) findViewById(R.id.tvAudience);
        tvCost = (TextView) findViewById(R.id.tvCost);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        autoLocation.setAdapter(arrayAdapter);
        autoLocation.setOnItemClickListener(this);

        tvTickets.setOnClickListener(this);
        tvStartTime.setOnClickListener(this);
        tvStartDate.setOnClickListener(this);
        tvEndTime.setOnClickListener(this);
        tvEndDate.setOnClickListener(this);
        cardPay.setOnClickListener(this);
        tvCategory.setOnClickListener(this);
        cardBroadcast.setOnClickListener(this);
        cardImage.setOnClickListener(this);

        Calendar calendar = Calendar.getInstance();
        yearX = calendar.get(Calendar.YEAR); yearY = calendar.get(Calendar.YEAR);
        monthX = calendar.get(Calendar.MONTH); monthY = calendar.get(Calendar.MONTH);
        dayX = calendar.get(Calendar.DAY_OF_MONTH); dayY = calendar.get(Calendar.DAY_OF_MONTH);
        hourX = calendar.get(Calendar.HOUR_OF_DAY); hourY = calendar.get(Calendar.HOUR_OF_DAY);
        minuteX = calendar.get(Calendar.MINUTE); minuteY = calendar.get(Calendar.MINUTE);

        insertBroadcastImage();
        putValues();
        getAppVersion();
        getBroadcastCharge();


        String pageName = "Broadcast Event";
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
    protected void onResume() {
        super.onResume();
        tvCategory.setText(prefs.getString(AppInfo.EVENT_INTEREST, ""));
        if ((!prefs.getString(AppInfo.EVENT_TICKET_NAME_1, "").equals("")
                && prefs.getInt(AppInfo.EVENT_TICKET_PRICE_1, 0) != 0
                && prefs.getInt(AppInfo.EVENT_TICKET_SEATS_1, 0) != 0)
                &&
                (!prefs.getString(AppInfo.EVENT_TICKET_NAME_2, "").equals("")
                        && prefs.getInt(AppInfo.EVENT_TICKET_PRICE_2, 0) != 0
                        && prefs.getInt(AppInfo.EVENT_TICKET_SEATS_2, 0) != 0)) {
            tvTickets.setText("Selling Double Tickets" + "" + "");
        }
        else if ((!prefs.getString(AppInfo.EVENT_TICKET_NAME_1, "").equals("")
                && prefs.getInt(AppInfo.EVENT_TICKET_PRICE_1, 0) != 0
                && prefs.getInt(AppInfo.EVENT_TICKET_SEATS_1, 0) != 0)
                ||
                (!prefs.getString(AppInfo.EVENT_TICKET_NAME_2, "").equals("")
                        && prefs.getInt(AppInfo.EVENT_TICKET_PRICE_2, 0) != 0
                        && prefs.getInt(AppInfo.EVENT_TICKET_SEATS_2, 0) != 0)) {
            tvTickets.setText("Selling a set of Tickets" + "" + "");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cardImage:
                getImageFromStorage();
                break;
            case R.id.tvTickets:
                startActivity(new Intent(this, TicketSell.class));
                break;
            case R.id.tvStartDate:
                createFancyDateTimePicker(xDATE_DIALOG_ID).show();
                break;
            case R.id.tvStartTime:
                createFancyDateTimePicker(xTIME_DIALOG_ID).show();
                break;
            case R.id.tvEndDate:
                createFancyDateTimePicker(yDATE_DIALOG_ID).show();
                break;
            case R.id.tvEndTime:
                createFancyDateTimePicker(yTIME_DIALOG_ID).show();
                break;
            case R.id.tvCategory:
                startActivity(new Intent(this, EventCategory.class));
                break;
            case R.id.cardPay:
                if (FirebaseAuth.getInstance().getCurrentUser() == null)
                    return;
                if (!NETWORK_AVAILABLE) {
                    getBroadcastCharge();
                    Toast.makeText(getApplicationContext(), "Internet Connection Error", Toast.LENGTH_LONG).show();
                    return;
                }
                startActivity(new Intent(this, PaymentEvent.class));
                break;
            case R.id.cardBroadcast:
                broadcastEvent();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor = prefs.edit();

        editor.putString(AppInfo.EVENT_LOCATION, autoLocation.getText().toString());
        editor.putString(AppInfo.EVENT_NAME, etName.getText().toString());
        editor.putString(AppInfo.EVENT_VENUE, etVenue.getText().toString());
        editor.putString(AppInfo.EVENT_DETAILS, etDetails.getText().toString());

        editor.putString(AppInfo.EVENT_START_TIME, tvStartTime.getText().toString());
        editor.putString(AppInfo.EVENT_START_DATE, tvStartDate.getText().toString());
        editor.putString(AppInfo.EVENT_END_TIME, tvEndTime.getText().toString());
        editor.putString(AppInfo.EVENT_END_DATE, tvEndDate.getText().toString());

        editor.apply();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        editor = prefs.edit();
        editor.putString(AppInfo.EVENT_LOCATION, String.valueOf(parent.getItemAtPosition(position)));
        editor.apply();
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }




    private void insertBroadcastImage() {
        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(prefs.getString(AppInfo.EVENT_PROFILE_LINK, ""));
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .crossFade()
                    .into(ivProfile);
        } catch (Exception e) {
            //
        }
    }

    private void putValues() {
        autoLocation.setText(prefs.getString(AppInfo.EVENT_LOCATION, ""));
        etName.setText(prefs.getString(AppInfo.EVENT_NAME, ""));
        etVenue.setText(prefs.getString(AppInfo.EVENT_VENUE, ""));
        etDetails.setText(prefs.getString(AppInfo.EVENT_DETAILS, ""));

        StringBuilder builderAudience = new StringBuilder();
        builderAudience.append("Target Audience:\n ").append(prefs.getInt(AppInfo.EVENT_AUDIENCE, 0));
        tvAudience.setText(builderAudience);

        StringBuilder builderCost = new StringBuilder();
        builderCost.append("Broadcast Cost:\n $").append(prefs.getInt(AppInfo.EVENT_COST, 0));
        tvCost.setText(builderCost);

        tvCategory.setText(prefs.getString(AppInfo.EVENT_INTEREST, ""));
        tvStartTime.setText(prefs.getString(AppInfo.EVENT_START_TIME, ""));
        tvStartDate.setText(prefs.getString(AppInfo.EVENT_START_DATE, ""));
        tvEndTime.setText(prefs.getString(AppInfo.EVENT_END_TIME, ""));
        tvEndDate.setText(prefs.getString(AppInfo.EVENT_END_DATE, ""));
    }

    private void broadcastEvent() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            return;
        if (!NETWORK_AVAILABLE) {
            getBroadcastCharge();
            Toast.makeText(getApplicationContext(), "Internet Connection Error", Toast.LENGTH_LONG).show();
            return;
        }

        if ((prefs.getBoolean(AppInfo.EVENT_BROADCAST_PAYMENT, false)
                ||
                (prefs.getInt(AppInfo.EVENT_COST, 2) == 0))
                && !prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "").equals("")
                && !prefs.getString(AppInfo.BUSINESS_LOCATION, "").equals("")
                && !prefs.getString(AppInfo.BUSINESS_EMAIL, "").equals("")) {

            if (appVersion != null) {
                GetSetterAppVersion setter = (GetSetterAppVersion) appVersion.get(0);
                if (MainActivity.versionCode < setter.getVersionCode()) {
                    if (setter.getBusinessPriority().equals("high")) {
                        builder = new AlertDialog.Builder(this);
                        builder.setCancelable(false);
                        builder.setTitle("New Update:");
                        builder.setMessage("Your app needs to be updated before broadcasting your event.\n");
                        builder.setIcon(R.mipmap.ic_launcher);
                        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=tech.myevents.app"));
                                startActivity(intent);
                            }
                        });
                        builder.show();
                        return;
                    }
                }
            }


            progressBar.setVisibility(View.VISIBLE);

            final String broadcasterUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            int impressions = 0;
            int ticketSpace1 = prefs.getInt(AppInfo.EVENT_TICKET_SEATS_1, 0);
            int ticketSpace2 = prefs.getInt(AppInfo.EVENT_TICKET_SEATS_2, 0);
            int ticketPrice1 = prefs.getInt(AppInfo.EVENT_TICKET_PRICE_1, 0);
            int ticketPrice2 = prefs.getInt(AppInfo.EVENT_TICKET_PRICE_2, 0);
            long broadcastTime = getCurrentTimestamp();
            long startTimestamp = prefs.getLong(AppInfo.EVENT_START_TIMESTAMP, 0);
            long endTimestamp = prefs.getLong(AppInfo.EVENT_END_TIMESTAMP, 0);
            String approvalCode = prefs.getString(AppInfo.EVENT_APPROVAL_CODE, "");
            String interestCode = prefs.getString(AppInfo.EVENT_INTEREST_CODE, "");
            String locationCode, eventLocation = autoLocation.getText().toString();

            if (!prefs.getString(AppInfo.EVENT_BROADCAST_RANGE_CODE, "").equals("National")) {
                locationCode = autoLocation.getText().toString();
            } else {
                locationCode = "National";
            }
            final String name = etName.getText().toString();
            final String venue = etVenue.getText().toString();
            String details = etDetails.getText().toString();
            final String profileLink = prefs.getString(AppInfo.EVENT_PROFILE_LINK, "");
            String eventStatus = "waiting";
            String startDate = prefs.getString(AppInfo.EVENT_START_DATE, "");
            String startTime = prefs.getString(AppInfo.EVENT_START_TIME, "");
            String ticketName1 = prefs.getString(AppInfo.EVENT_TICKET_NAME_1, "");
            String ticketName2 = prefs.getString(AppInfo.EVENT_TICKET_NAME_2, "");
            int broadcastCharge = prefs.getInt(AppInfo.EVENT_COST, 2);
            int merchantCode = prefs.getInt(AppInfo.BUSINESS_ECOCASH_CODE, 0);
            String merchantName = prefs.getString(AppInfo.BUSINESS_ECOCASH_NAME, "");
            String ecocashType = prefs.getString(AppInfo.BUSINESS_ECOCASH_TYPE, "");

            String ticketPromoCode = getPromotionCode();
            int availableTickets = prefs.getInt(AppInfo.FREE_TICKETS, 0);

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference ref = database.getReference();
            final String eventKey = ref.child(StaticVariables.childEventsWaiting).child(broadcasterUid).push().getKey();

            String brandName = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "") : prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "");
            String brandLink = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
            boolean verified = !accType.equals(StaticVariables.individual) && prefs.getBoolean(AppInfo.BUSINESS_VERIFIED, false);
            int signatureVersion = prefs.getInt(AppInfo.SIG_VER_BUS, 0);

            GetSetterEvent setter = new GetSetterEvent(impressions,
                    ticketSpace1, ticketSpace2, ticketPrice1, ticketPrice2, startTimestamp, endTimestamp,
                    broadcastTime, broadcasterUid, brandName, brandLink, eventKey, interestCode, locationCode, name, venue,
                    details, profileLink, eventStatus, startDate, startTime, ticketName1, ticketName2, broadcastCharge,
                    merchantCode, merchantName, approvalCode, eventLocation, ticketPromoCode,
                    availableTickets, 0, ecocashType, "", verified, signatureVersion);

            ref
                    .child(StaticVariables.childEventsWaiting)
                    .child(broadcasterUid)
                    .child(eventKey)
                    .setValue(setter)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                insertBroadcastImage();
                                progressBar.setVisibility(View.GONE);

                                editor = prefs.edit();
                                editor.putBoolean(AppInfo.EVENT_BROADCAST_PAYMENT, false);
                                editor.putString(AppInfo.EVENT_PROFILE_LINK, "");
                                editor.apply();
                                clearValues();
                                Toast.makeText(getApplicationContext(), "Broadcast Successful. Thank You", Toast.LENGTH_SHORT).show();

                                String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                String key = FirebaseDatabase.getInstance().getReference()
                                        .child(StaticVariables.childNotificationsAdmin)
                                        .push()
                                        .getKey();

                                GetSetterNotifications setterNotifications = new
                                        GetSetterNotifications(StaticVariables.event, key, broadcasterUid, eventKey, false);

                                FirebaseDatabase.getInstance().getReference()
                                        .child(StaticVariables.childNotificationsAdmin)
                                        .child(key)
                                        .setValue(setterNotifications);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Broadcast Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "You need to pay first / create your business profile", Toast.LENGTH_LONG).show();
        }
    }

    private String getPromotionCode() {
        String[] alpha = {"", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

        Random random = new Random();
        String a = alpha[checkNum(random.nextInt(26))];
        String b = alpha[checkNum(random.nextInt(26))];
        int c = random.nextInt(9);
        int d = random.nextInt(9);
        int e = random.nextInt(9);
        int f = random.nextInt(9);
        String g = alpha[checkNum(random.nextInt(26))];
        String h = alpha[checkNum(random.nextInt(26))];

        return a + b + c + d + e + f + g + h;
    }

    private int checkNum(int a) {
        if (a == 0 || a == 27) {
            if (a == 0) {
                a += 1;
            }
            if (a == 27) {
                a -= 1;
            }
        }

        return a;
    }




    private void getAppVersion() {
        if (appVersion == null)
            FirebaseDatabase.getInstance()
                    .getReference()
                    .child(StaticVariables.childAppVersion)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            appVersion = new ArrayList<>();
                            appVersion.add(dataSnapshot.getValue(GetSetterAppVersion.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
    }

    private void getBroadcastCharge() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            return;
        final String businessSize = prefs.getString(AppInfo.BUSINESS_SIZE, "1 - 3 Employees");
        final String broadcastRange = prefs.getString(AppInfo.EVENT_BROADCAST_RANGE_CODE, "National");
        final String interestCode = prefs.getString(AppInfo.EVENT_INTEREST_CODE, "1a");

        if (promotionCode == null)
            FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childPromotionCode)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            promotionCode = new ArrayList();
                            promotionCode.add(dataSnapshot.getValue(GetSetterPromotion.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        if (NUMBER_OF_BROADCASTS == 0)
            FirebaseDatabase.getInstance()
                    .getReference()
                    .child(StaticVariables.childEvents)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            NUMBER_OF_BROADCASTS = (int) dataSnapshot.getChildrenCount();
                            getBroadcastChargeAndUsers(businessSize, broadcastRange, interestCode);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        else
            getBroadcastChargeAndUsers(businessSize, broadcastRange, interestCode);

    }

    private void getBroadcastChargeAndUsers(final String businessSize, final String broadcastRange, final String interestCode) {
        NETWORK_AVAILABLE = true;
        if (broadcastCharges == null)
            FirebaseDatabase.getInstance()
                    .getReference()
                    .child(StaticVariables.childCharges)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            broadcastCharges = new ArrayList();
                            broadcastCharges.add(dataSnapshot.getValue(GetSetterBroadcastCharges.class));

                            FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child(StaticVariables.childUsers)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (targetAudience == null)
                                                targetAudience = new ArrayList();
                                            for (DataSnapshot dataSnapshotChildren: dataSnapshot.getChildren()) {
                                                targetAudience.add(dataSnapshotChildren.getValue(GetSetterUser.class));
                                            }
                                            NETWORK_AVAILABLE = true;
                                            calculateBroadcastCharge(broadcastRange, businessSize);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        else
            calculateBroadcastCharge(broadcastRange, businessSize);
    }

    private void calculateBroadcastCharge(String location, String businessSize) {
        GetSetterBroadcastCharges setter = (GetSetterBroadcastCharges) broadcastCharges.get(0);
        int broadcastCharge;
        int targetAudience = getTargetAudience();
//        if (location.equals("National"))
        if (targetAudience < 3000)
            broadcastCharge = 40;
        else
            broadcastCharge = 60;

        if (targetAudience < 50)
            broadcastCharge = setter.getAudienceLess50();
        else if (targetAudience > 50 && targetAudience < 100)
            broadcastCharge = setter.getAudienceLess100();
        else {
            if (NUMBER_OF_BROADCASTS == 0) {
                broadcastCharge = setter.getBroadcasts0();
            } else if (NUMBER_OF_BROADCASTS == 1) {
                broadcastCharge = setter.getBroadcasts1();
            } else if (NUMBER_OF_BROADCASTS == 2) {
                broadcastCharge = setter.getBroadcasts2();
            } else if (NUMBER_OF_BROADCASTS == 3) {
                broadcastCharge = setter.getBroadcasts3();
            } else {
                switch (businessSize) {
                    case "1 - 3 Employees":
                        broadcastCharge += setter.getSize3();
                        break;
                    case "4 - 10 Employees":
                        broadcastCharge += setter.getSize10();
                        break;
                    case "11 - 30 Employees":
                        broadcastCharge += setter.getSize30();
                        break;
                    case "31 - 50 Employees":
                        broadcastCharge += setter.getSize50();
                        break;
                    case "Over 50 Employees":
                        broadcastCharge += setter.getSizeAbove50();
                        break;
                }
            }
        }

        editor = prefs.edit();
        editor.putInt(AppInfo.EVENT_COST, broadcastCharge);
        editor.putInt(AppInfo.AUD_ECOCASH_NUMBER, setter.getEcocashNumber());
        editor.apply();

        /*if (targetAudience < 50)
            targetAudience += 50;*/
        tvCost.setText(new StringBuilder().append("Broadcast Cost:\n$").append(broadcastCharge));
        tvAudience.setText(new StringBuilder().append("Target Audience:\n").append(targetAudience));
    }

    private int getTargetAudience() {
        GetSetterUser setter;
        int targetAud = 0;
        final String broadcastRange = prefs.getString(AppInfo.EVENT_BROADCAST_RANGE_CODE, "National");
        final String interestCode = prefs.getString(AppInfo.EVENT_INTEREST_CODE, "1a");

        for (int x = 0; x < targetAudience.size(); x++) {
            setter = (GetSetterUser) targetAudience.get(x);
            if (setter.getInterestCode().contains(interestCode))
                targetAud += 1;
        }

        return targetAud;
    }







    private void getImageFromStorage() {
        if (Build.VERSION.SDK_INT >= 23) {
            boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_IMAGE);
            } else {
                startActivityForResult(galleryPhoto.openGalleryIntent(), GALLERY_REQUEST);
            }
        } else {
            startActivityForResult(galleryPhoto.openGalleryIntent(), GALLERY_REQUEST);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            progressBar.setVisibility(View.VISIBLE);
            handlePosterResult(data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_IMAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //reload my activity with permission granted or use the features that required the permission
                    startActivityForResult(galleryPhoto.openGalleryIntent(), GALLERY_REQUEST);
                } else {
                    Toast.makeText(getApplicationContext(), "Allowed access to your storage.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void handlePosterResult(Intent data) {
        editor = prefs.edit();
        Uri uri = data.getData();
        final String previousImage = prefs.getString(AppInfo.EVENT_PROFILE_LINK, "");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("broadcast-pics/"+ uri.getLastPathSegment());
        UploadTask uploadTask = imageRef.putFile(uri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getApplicationContext(), "Image UploadFailed", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-accType, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                editor.putString(AppInfo.EVENT_PROFILE_LINK, downloadUrl.toString());
                editor.apply();

                try {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl.toString());
                    Glide.with(BroadcastEvent.this)
                            .using(new FirebaseImageLoader())
                            .load(storageReference)
                            .crossFade()
                            .into(ivProfile);

                    if (!previousImage.equals(""))
                        FirebaseStorage.getInstance().getReferenceFromUrl(previousImage).delete();
                } catch (Exception e) {
                    //
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void clearValues() {
        StartStamp = 0;
        EndStamp = 0;
        ivProfile.setImageResource(0);
        autoLocation.setText("");
        etName.setText("");
        etVenue.setText("");
        etDetails.setText("");

        StringBuilder builderAudience = new StringBuilder();
        builderAudience.append("Target Audience:\n").append(prefs.getInt(AppInfo.EVENT_AUDIENCE, 0));
        tvAudience.setText(builderAudience);
        StringBuilder builderCost = new StringBuilder();
        builderCost.append("Broadcast Cost:\n$").append(0);
        tvCost.setText(builderCost);
        tvCategory.setText("");
        tvStartTime.setText("");
        tvStartDate.setText("");
        tvEndTime.setText("");
        tvEndDate.setText("");

        editor = prefs.edit();
        editor.putString(AppInfo.EVENT_INTEREST, "");
        editor.putString(AppInfo.EVENT_INTEREST_CODE, "");
        editor.putString(AppInfo.EVENT_BROADCAST_RANGE, "");
        editor.putString(AppInfo.EVENT_BROADCAST_RANGE_CODE, "");
        editor.putBoolean(AppInfo.EVENT_TICKET_SWITCH, false);
        editor.putString(AppInfo.EVENT_LOCATION, "");
        editor.putString(AppInfo.EVENT_NAME, "");
        editor.putString(AppInfo.EVENT_VENUE, "");
        editor.putString(AppInfo.EVENT_DETAILS, "");
        editor.putString(AppInfo.EVENT_TICKET_NAME_1, "");
        editor.putInt(AppInfo.EVENT_TICKET_PRICE_1, 0);
        editor.putInt(AppInfo.EVENT_TICKET_SEATS_1, 0);
        editor.putString(AppInfo.EVENT_TICKET_NAME_2, "");
        editor.putInt(AppInfo.EVENT_TICKET_PRICE_2, 0);
        editor.putInt(AppInfo.EVENT_TICKET_SEATS_2, 0);
        editor.putString(AppInfo.EVENT_START_TIME, "");
        editor.putString(AppInfo.EVENT_START_DATE, "");
        editor.putString(AppInfo.EVENT_END_TIME, "");
        editor.putString(AppInfo.EVENT_END_DATE, "");
        editor.putInt(AppInfo.EVENT_ECOCACH_MERCHANT_CODE, 0);
        editor.putString(AppInfo.EVENT_ECOCASH_MERCHANT_NAME, "");
        editor.apply();
    }





    protected Dialog createFancyDateTimePicker(int id) {
        Calendar calendar = Calendar.getInstance();
        switch (id) {
            case xDATE_DIALOG_ID:
                DatePickerDialog dialog = new DatePickerDialog(this, xDateSetListener, yearX, monthX, dayX);
                if (EndStamp == 0)
                    dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                else {
                    dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                    dialog.getDatePicker().setMaxDate(EndStamp);
                }
                return dialog;

            case xTIME_DIALOG_ID:
                hourX = calendar.get(Calendar.HOUR_OF_DAY);
                minuteX = calendar.get(Calendar.MINUTE);
                return new TimePickerDialog(this, xTimeSetListener, hourX, minuteX, true);

            case yDATE_DIALOG_ID:
                DatePickerDialog dialog1 = new DatePickerDialog(this, yDateSetListener, yearY, monthY, dayY);
                if (StartStamp == 0)
                    StartStamp = calendar.getTimeInMillis();
                dialog1.getDatePicker().setMinDate(StartStamp);
                return dialog1;

            case yTIME_DIALOG_ID:
                hourY = calendar.get(Calendar.HOUR_OF_DAY);
                minuteY = calendar.get(Calendar.MINUTE);
                return new TimePickerDialog(this, yTimeSetListener, hourY, minuteY, true);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener xDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            yearX = year;
            monthX = monthOfYear;
            dayX = dayOfMonth;
            String builder = getTheValue(dayX) + "-" + StaticVariables.monthsSmall[monthX + 1] + "-" + getTheValue(yearX);
            tvStartDate.setText(builder);


            editor = prefs.edit();
            editor.putString(AppInfo.EVENT_START_DATE, builder);
            editor.apply();

            checkTimeDateValues("x");
        }
    };

    private TimePickerDialog.OnTimeSetListener xTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hourX = hourOfDay;
            //hourY = hourOfDay + 5;
            minuteX = minute;
            String builder = getTheValue(hourX) + ":" + getTheValue(minuteX);
            tvStartTime.setText(builder);


            editor = prefs.edit();
            editor.putString(AppInfo.EVENT_START_TIME, builder);
            editor.apply();

            checkTimeDateValues("x");
        }
    };

    private DatePickerDialog.OnDateSetListener yDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            yearY = year;
            monthY = monthOfYear;
            dayY = dayOfMonth;
            String builder = getTheValue(dayY) + "-" + StaticVariables.monthsSmall[monthY + 1] + "-" + getTheValue(yearY);
            tvEndDate.setText(builder);

            editor = prefs.edit();
            editor.putString(AppInfo.EVENT_END_DATE, builder);
            editor.apply();

            checkTimeDateValues("y");
        }
    };

    private TimePickerDialog.OnTimeSetListener yTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hourY = hourOfDay;
            minuteY = minute;
            String builder = getTheValue(hourY) + ":" + getTheValue(minuteY);
            tvEndTime.setText(builder);

            editor = prefs.edit();
            editor.putString(AppInfo.EVENT_END_TIME, builder);
            editor.apply();

            checkTimeDateValues("y");
        }
    };

    public void checkTimeDateValues(String source){
        if(source.equals("x")) {
            if (!tvStartTime.getText().toString().equals("") && !tvStartDate.getText().toString().equals("")) {
                generateTimestamp("x", monthX, yearX, dayX, hourX, minuteX);
            }
        }
        else if(source.equals("y")) {
            if (!tvEndTime.getText().toString().equals("") && !tvEndDate.getText().toString().equals("")) {
                generateTimestamp("y", monthY, yearY, dayY, hourY, minuteY);
            }
        }
    }

    public void generateTimestamp(String source,int month, int year, int day, int hour, int minute){

        long timestamp;

        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, month);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        timestamp = c.getTimeInMillis();

        if(source.equals("x")){
            editor = prefs.edit();
            StartStamp = timestamp;
            editor.putLong(AppInfo.EVENT_START_TIMESTAMP, timestamp);
            editor.apply();
        } else if (source.equals("y")){
            editor = prefs.edit();
            EndStamp = timestamp;
            editor.putLong(AppInfo.EVENT_END_TIMESTAMP, timestamp);
            editor.apply();
        }
    }

    public long getCurrentTimestamp(){
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

    public String getTheValue(int value){
        String theValue = String.valueOf(value);
        if (theValue.length() == 1){
            return "0"+theValue;
        } else {
            return theValue;
        }
    }








    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            currentPage = 1;
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        currentPage = 1;
        super.onBackPressed();
    }

}
