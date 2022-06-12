package tech.myevents.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static tech.myevents.app.MainActivity.currentPage;

public class Settings extends AppCompatActivity implements View.OnClickListener {

    View viewProfile, viewBroadcast, viewFriends;
    CardView cardImage, cardNotifications;
    TextView tvUsername, tvLocation,
            tvTickets, tvExclusive, tvShare, tvBroadcast,
            tvFriends, tvAccount, tvBusinessAccounts;
    ImageView ivProfile, ivVerified,  ivBroadcast, ivFriends, ivExclusive,
            ivTicket, ivBusiness, ivShare, ivAccount;

    SharedPreferences prefs;
    SharedPreferences prefsApp;
    SharedPreferences.Editor editor;
    String accType;
    Toolbar toolbar;

    int[] icons = {R.drawable.settings_broadcast_bus,
            R.drawable.settings_ticket_bus,
            R.drawable.settings_exclusive_bus,
            R.drawable.settings_business_bus,
            R.drawable.settings_share_bus,
            R.drawable.settings_account_bus};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MainActivity.currentPage = 2;

        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        statusBarColor();

        viewProfile = (View) findViewById(R.id.viewProfile);
        cardImage = (CardView) findViewById(R.id.cardImage);
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        tvTickets = (TextView) findViewById(R.id.tvTickets);
        tvExclusive = (TextView) findViewById(R.id.tvExclusive);
        tvShare = (TextView) findViewById(R.id.tvShare);
        tvBroadcast = (TextView) findViewById(R.id.tvBroadcast);
        tvFriends = (TextView) findViewById(R.id.tvFriends);
        tvAccount = (TextView) findViewById(R.id.tvAccount);
        ivVerified = (ImageView) findViewById(R.id.ivVerified);
        tvBusinessAccounts = (TextView) findViewById(R.id.tvBusinessAccounts);
        viewBroadcast = findViewById(R.id.viewBroadcast);
        ivBroadcast = (ImageView) findViewById(R.id.ivBroadcast);
        viewFriends = findViewById(R.id.viewFriends);
        ivFriends = (ImageView) findViewById(R.id.ivFriends);
        ivExclusive = (ImageView) findViewById(R.id.ivExclusive);
        ivTicket = (ImageView)findViewById(R.id.ivTicket);
        ivBusiness = (ImageView) findViewById(R.id.ivBusiness);
        ivShare = (ImageView) findViewById(R.id.ivShare);
        ivAccount = (ImageView) findViewById(R.id.ivAccount);
        cardNotifications = findViewById(R.id.cardNotifications);

        viewProfile.setOnClickListener(this);
        cardImage.setOnClickListener(this);
        tvTickets.setOnClickListener(this);
        tvExclusive.setOnClickListener(this);
        tvShare.setOnClickListener(this);
        tvBroadcast.setOnClickListener(this);
        tvFriends.setOnClickListener(this);
        tvAccount.setOnClickListener(this);
        tvBusinessAccounts.setOnClickListener(this);
        cardNotifications.setOnClickListener(this);

        View[] viewsBusiness = {ivBroadcast, viewBroadcast, tvBroadcast};
        View[] viewsIndividual = {ivFriends, viewFriends, tvFriends};
        ImageView[] imageViews = {ivBroadcast, ivTicket, ivExclusive, ivBusiness, ivShare, ivAccount};
        if (accType.equals(StaticVariables.business)) {
            for (View aViewsBusiness : viewsBusiness) aViewsBusiness.setVisibility(View.VISIBLE);
            for (View aViewsIndividual : viewsIndividual) aViewsIndividual.setVisibility(View.GONE);
            for (int x = 0; x < icons.length;  x++) imageViews[x].setImageResource(icons[x]);
        }


        if (prefs.getBoolean(AppInfo.BUSINESS_VERIFIED, false)) {
            if (accType.equals(StaticVariables.business))
                ivVerified.setVisibility(View.VISIBLE);
        } else {
                ivVerified.setVisibility(View.GONE);
                if (accType.equals(StaticVariables.business))
                    if (!prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "").equals("")
                            && !prefs.getString(AppInfo.BUSINESS_DESCRIPTION, "").equals(""))
                        FirebaseDatabase.getInstance().getReference()
                                .child(StaticVariables.childVerifiedBusinesses)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.exists())
                                            return;
                                        GetSetterVerified setter = dataSnapshot.getValue(GetSetterVerified.class);
                                        if (setter.isVerified()) {
                                            editor = prefs.edit();
                                            editor.putBoolean(AppInfo.BUSINESS_VERIFIED, true);
                                            editor.apply();
                                            ivVerified.setVisibility(View.VISIBLE);
                                            Toast.makeText(getApplicationContext(), "Your business account has been verified", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
            }


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Settings";
        GetSetterPageHits setterHits = new GetSetterPageHits(uid, "", pageName, System.currentTimeMillis());
        new PageHitsBackTask().execute(setterHits);
    }

    private void statusBarColor() {
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
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            finish();

        insertUserProfile();
        if (accType.equals(StaticVariables.individual)) {
            tvUsername.setText(prefs.getString(AppInfo.INDIVIDUAL_USERNAME, ""));
            tvLocation.setText(prefs.getString(AppInfo.INDIVIDUAL_LOCATION, ""));
        } else {
            tvUsername.setText(prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "Brand name"));
            tvLocation.setText(prefs.getString(AppInfo.BUSINESS_LOCATION, "Business location"));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cardImage:
            case R.id.viewProfile:
                if (accType.equals(StaticVariables.individual))
                    startActivity(new Intent(this, Profile.class));
                else
                    startActivity(new Intent(this, ProfileBusiness.class));
                break;
            case R.id.tvTickets:
                startActivity(new Intent(this, TicketSettings.class));
                break;
            case R.id.tvLoyalty:
                startActivity(new Intent(this, LoyaltyPoints.class));
                break;
            case R.id.tvShare:
                startActivity(new Intent(this, ShareApp.class));
                break;
            case R.id.tvBroadcast:
                startActivity(new Intent(this, Broadcasts.class));
                break;
            case R.id.tvFriends:
                startActivity(new Intent(this, Friends.class));
                break;
            case R.id.tvAccount:
                startActivity(new Intent(this, Account.class));
                break;
            case R.id.tvBusinessAccounts:
                startActivity(new Intent(this, Business.class));
                break;
            case R.id.tvExclusive:
                startActivity(new Intent(this, ExclusiveEvents.class));
                break;
            case R.id.cardNotifications:
                String finalMessage = "I want to receive WhatsApp notifications.";
                Intent i = new Intent();
                i.putExtra(Intent.EXTRA_TEXT, finalMessage);
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://api.whatsapp.com/send?phone=263775523763&text=" + urlEncode(finalMessage)));
                startActivity(i);
                break;
        }
    }

    private String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private void insertUserProfile() {
        String url;
        int version;
        if (accType.equals(StaticVariables.individual)) {
            version = prefs.getInt(AppInfo.SIG_VER_INDI, 0);
            url = prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "");
        } else {
            url = prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
            version = prefs.getInt(AppInfo.SIG_VER_BUS, 0);
        }

        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .signature(new StringSignature("" + version))
                    .crossFade()
                    .into(ivProfile);
        } catch (Exception e) {
            ivProfile.setImageResource(0);
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
