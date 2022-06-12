package tech.myevents.app;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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

import static tech.myevents.app.MainActivity.currentPage;

public class BroadcastAd extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    CardView cardImage, cardBroadcast, cardPay;
    ImageView ivProfile;
    EditText etTitle, etBrandName, etDetails;
    AutoCompleteTextView autoLocation;
    TextView tvCategory, tvDuration, tvAudience, tvCost;
    ProgressBar progressBar;

    GalleryPhoto galleryPhoto;
    final int GALLERY_REQUEST = 27277;
    private static final int REQUEST_WRITE_IMAGE = 1994;

    SharedPreferences prefs;
    SharedPreferences prefsApp;
    String accType;
    SharedPreferences.Editor editor;

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
        setContentView(R.layout.activity_broadcast_ad);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MainActivity.currentPage = 4;
        setStatusBarColor();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
        galleryPhoto = new GalleryPhoto(this);
        dbOperations = new DBOperations(this);
        db = dbOperations.getWritableDatabase();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        if (accType.equals(StaticVariables.business)) {
            Window w = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.appStatusBar));
            toolbar.setBackgroundColor(getResources().getColor(R.color.appBarMainBus));
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, StaticVariables.locations);

        tvAudience = (TextView) findViewById(R.id.tvAudience);
        tvCost = (TextView) findViewById(R.id.tvCost);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        cardImage = (CardView) findViewById(R.id.cardImage);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        etTitle = (EditText) findViewById(R.id.etTitle);
        etBrandName = (EditText) findViewById(R.id.etBrandName);
        etDetails = (EditText) findViewById(R.id.etDetails);
        autoLocation = (AutoCompleteTextView) findViewById(R.id.autoLocation);
        tvCategory = (TextView) findViewById(R.id.tvCategory);
        tvDuration = (TextView) findViewById(R.id.tvDuration);
        cardPay = (CardView)  findViewById(R.id.cardPay);
        cardBroadcast = (CardView) findViewById(R.id.cardBroadcast);

        autoLocation.setAdapter(arrayAdapter);
        autoLocation.setOnItemClickListener(this);

        cardImage.setOnClickListener(this);
        tvDuration.setOnClickListener(this);
        tvCategory.setOnClickListener(this);
        cardPay.setOnClickListener(this);
        cardBroadcast.setOnClickListener(this);

        etDetails.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable != null && editable.length() > 0 && editable.charAt(editable.length() - 1) == ' '){
                    Toast toast = null;
                }
            }
        });

        insertBroadcastImage();
        putValues();
        getAppVersion();
        getBroadcastCharge();


        String pageName = "Broadcast Ad";
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
        tvCategory.setText(prefs.getString(AppInfo.AD_INTEREST, ""));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cardImage:
                getImageFromStorage();
                break;
            case R.id.tvDuration:
                PopupMenu popupMenu1 = new PopupMenu(this, tvDuration);
                popupMenu1.getMenuInflater().inflate(R.menu.menu_duration, popupMenu1.getMenu());
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("Disappears after: ").append(String.valueOf(item.getTitle()));
                        tvDuration.setText(builder);
                        editor = prefs.edit();
                        editor.putString(AppInfo.AD_DURATION_CODE, item.getTitle().toString());
                        editor.putString(AppInfo.AD_DURATION, builder.toString());
                        editor.apply();
                        return true;
                    }
                });
                popupMenu1.show();
                break;
            case R.id.tvCategory:
                startActivity(new Intent(this, AdCategory.class));
                break;
            case R.id.cardPay:
                if (FirebaseAuth.getInstance().getCurrentUser() == null)
                    return;
                if (!NETWORK_AVAILABLE) {
                    getBroadcastCharge();
                    Toast.makeText(getApplicationContext(), "Internet Connection Error", Toast.LENGTH_LONG).show();
                    return;
                }
                startActivity(new Intent(this, PaymentsAd.class));
                break;
            case R.id.cardBroadcast:
                broadcastAd();
                break;

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        editor = prefs.edit();
        editor.putString(AppInfo.EVENT_LOCATION, String.valueOf(parent.getItemAtPosition(position)));
        editor.apply();
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor = prefs.edit();
        editor.putString(AppInfo.AD_TITLE, etTitle.getText().toString());
        editor.putString(AppInfo.AD_BRAND_NAME, etBrandName.getText().toString());
        editor.putString(AppInfo.AD_DETAILS, etDetails.getText().toString());
        editor.putString(AppInfo.AD_INTEREST, tvCategory.getText().toString());
        editor.putString(AppInfo.AD_DURATION, tvDuration.getText().toString());
        editor.putString(AppInfo.AD_LOCATION, autoLocation.getText().toString());
        editor.apply();
    }

    private void insertBroadcastImage() {
        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(prefs.getString(AppInfo.AD_PROFILE_LINK, ""));
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .crossFade()
                    .into(ivProfile);
        } catch (Exception e) {
            //
        }
    }


    private void broadcastAd() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            return;
        if (!NETWORK_AVAILABLE) {
            getBroadcastCharge();
            Toast.makeText(getApplicationContext(), "Internet Connection Error", Toast.LENGTH_LONG).show();
            return;
        }

        if ((prefs.getBoolean(AppInfo.AD_BROADCAST_PAYMENT, false)
                ||
                (prefs.getInt(AppInfo.AD_COST, 2) == 0))
                && !prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "").equals("")
                && !prefs.getString(AppInfo.BUSINESS_LOCATION, "").equals("")
                && !prefs.getString(AppInfo.BUSINESS_EMAIL, "").equals("")) {

            if (appVersion != null) {
                GetSetterAppVersion setter = (GetSetterAppVersion) appVersion.get(0);
                if (MainActivity.versionCode < setter.getVersionCode()) {
                    if (setter.getBusinessPriority().equals("high")) {
                        builder = new AlertDialog.Builder(this);
                        builder.setTitle("New Update:");
                        builder.setMessage("Your app needs to be updated before broadcasting your ad.\n");
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
            int impressions = 0, clicksIndividual = 0, clicksBusiness = 0;
            long broadcastTime = getCurrentTimestamp();

            String approvalCode = prefs.getString(AppInfo.AD_APPROVAL_CODE, "");
            long duration = getDuration(prefs.getString(AppInfo.AD_DURATION_CODE, "2 Days"));
            String interestCode = prefs.getString(AppInfo.AD_INTEREST_CODE, "");
            String locationCode, adLocation = autoLocation.getText().toString();
            if (!prefs.getString(AppInfo.AD_BROADCAST_RANGE_CODE, "").equals("National")) {
                locationCode = autoLocation.getText().toString();
            } else {
                locationCode = "National";
            }
            final String title = etTitle.getText().toString();
            final String brandName = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "") : prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "");
            String details = etDetails.getText().toString();
            final String profileLink = prefs.getString(AppInfo.AD_PROFILE_LINK, "");
            String status = "waiting";
            int broadcastCharge = prefs.getInt(AppInfo.AD_COST, 2);
            int merchantCode = prefs.getInt(AppInfo.AD_ECOCACH_MERCHANT_CODE, 12345);
            String merchantName = prefs.getString(AppInfo.AD_ECOCASH_MERCHANT_NAME, "");
            String brandLink = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
            boolean verified = !accType.equals(StaticVariables.individual) && prefs.getBoolean(AppInfo.BUSINESS_VERIFIED, false);

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference ref = database.getReference();
            final String adKey = ref.child(StaticVariables.childAdsWaiting).child(broadcasterUid).push().getKey();
            int signatureVersion = prefs.getInt(AppInfo.SIG_VER_BUS, 0);

            GetSetterAd setter = new GetSetterAd(broadcasterUid, adKey, impressions, clicksIndividual,
                    clicksBusiness, brandName, brandLink, title, details, profileLink, locationCode,
                    interestCode, status, duration, broadcastTime, broadcastCharge,
                    merchantCode, merchantName, approvalCode, adLocation, 0, verified, signatureVersion);

            ref
                    .child(StaticVariables.childAdsWaiting)
                    .child(broadcasterUid)
                    .child(adKey)
                    .setValue(setter)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.INVISIBLE);
                            if (task.isSuccessful()) {
                                insertBroadcastImage();

                                editor = prefs.edit();
                                editor.putString(AppInfo.AD_PROFILE_LINK, "");
                                editor.putBoolean(AppInfo.AD_BROADCAST_PAYMENT, false);
                                editor.apply();
                                clearValues();
                                Toast.makeText(getApplicationContext(), "Broadcast Successful. Thank You", Toast.LENGTH_LONG).show();

                                String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                String key = FirebaseDatabase.getInstance().getReference()
                                        .child(StaticVariables.childNotificationsAdmin)
                                        .push()
                                        .getKey();
                                GetSetterNotifications setterNotifications = new
                                        GetSetterNotifications(StaticVariables.ad, key, broadcasterUid, adKey, false);
                                FirebaseDatabase.getInstance().getReference()
                                        .child(StaticVariables.childNotificationsAdmin)
                                        .child(key)
                                        .setValue(setterNotifications);

                            } else {
                                Toast.makeText(getApplicationContext(), "Broadcast Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "You need to pay first / setup your business profile", Toast.LENGTH_LONG).show();
        }
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
        final String broadcastRange = prefs.getString(AppInfo.AD_BROADCAST_RANGE_CODE, "National");
        final String interestCode = prefs.getString(AppInfo.AD_INTEREST_CODE, "1a");

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
                    .child(StaticVariables.childAds)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            NUMBER_OF_BROADCASTS = (int) dataSnapshot.getChildrenCount();
                            getBroadcastChargeAndUsers(businessSize, broadcastRange);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        else
            getBroadcastChargeAndUsers(businessSize, broadcastRange);

    }

    private void getBroadcastChargeAndUsers(final String businessSize, final String broadcastRange) {
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
        if (targetAudience > 3000)
            broadcastCharge = 60;
        else
            broadcastCharge = 40;

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
        editor.putInt(AppInfo.AD_COST, broadcastCharge);
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
        final String broadcastRange = prefs.getString(AppInfo.AD_BROADCAST_RANGE_CODE, "National");
        final String interestCode = prefs.getString(AppInfo.AD_INTEREST_CODE, "1a");

        for (int x = 0; x < targetAudience.size(); x++) {
            setter = (GetSetterUser) targetAudience.get(x);
            if (setter.getInterestCode().contains(interestCode))
                targetAud += 1;
        }

        return targetAud;
    }

    long getDuration(String duration) {
        long adDuration = 0;
        switch (duration) {
            case "24 Hrs":
                adDuration = getCurrentTimestamp() + 86400000;
                break;
            case "2 Days":
                //get current timestamp and add two days
                adDuration = getCurrentTimestamp() + 172800000;
                break;
            case "4 Days":
                //add 4 days
                adDuration = getCurrentTimestamp() + 345600000;
                break;
            case "7 Days":
                // add 7 days
                adDuration = getCurrentTimestamp() + 604800000;
                break;
            case "10 Days":
                //add 10 days
                adDuration = getCurrentTimestamp() + 864000000;
                break;
        }
        return adDuration;
    }







    private void putValues() {
        etTitle.setText(prefs.getString(AppInfo.AD_TITLE, ""));
        etBrandName.setText(prefs.getString(AppInfo.AD_BRAND_NAME, ""));
        etDetails.setText(prefs.getString(AppInfo.AD_DETAILS, ""));
        tvCategory.setText(prefs.getString(AppInfo.AD_INTEREST, ""));
        tvDuration.setText(prefs.getString(AppInfo.AD_DURATION, ""));
        autoLocation.setText(prefs.getString(AppInfo.AD_LOCATION, ""));
    }

    private void clearValues() {
        ivProfile.setImageResource(0);
        etTitle.setText("");
        etBrandName.setText("");
        etDetails.setText("");
        tvCategory.setText("");
        tvDuration.setText("");
        autoLocation.setText("");

        editor = prefs.edit();
        editor.putString(AppInfo.AD_INTEREST, "");
        editor.putString(AppInfo.AD_INTEREST_CODE, "");
        editor.putString(AppInfo.AD_BROADCAST_RANGE, "");
        editor.putString(AppInfo.AD_BROADCAST_RANGE_CODE, "");
        editor.putString(AppInfo.AD_TITLE, "");
        editor.putString(AppInfo.AD_BRAND_NAME, "");
        editor.putString(AppInfo.AD_DETAILS, "");
        editor.putString(AppInfo.AD_INTEREST, "");
        editor.putString(AppInfo.AD_DURATION, "");
        editor.putString(AppInfo.AD_LOCATION, "");
        editor.apply();
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
        final String previousImage = prefs.getString(AppInfo.AD_PROFILE_LINK, "");

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
                editor.putString(AppInfo.AD_PROFILE_LINK, downloadUrl.toString());
                editor.apply();

                try {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl.toString());
                    Glide.with(BroadcastAd.this)
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

    public long getCurrentTimestamp() {
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
