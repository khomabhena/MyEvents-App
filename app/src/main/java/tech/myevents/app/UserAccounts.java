package tech.myevents.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserAccounts extends AppCompatActivity implements View.OnClickListener {

    View viewBusiness, viewIndividual;
    ImageView ivSelectedIndividual, ivSelectedBusiness, ivUnselectedIndividual, ivUnselectedBusiness,
            ivProfile, ivLogo;
    TextView tvUsername, tvLocation,  tvEmail, tvBrandName,  tvLocationBus, tvEmailBus, tvDescription;

    SharedPreferences prefsApp;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String type;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_accounts);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setStatusBarColor();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        prefs = getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
        type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        viewBusiness = findViewById(R.id.viewBusiness);
        viewIndividual = findViewById(R.id.viewIndividual);
        ivSelectedIndividual = (ImageView) findViewById(R.id.ivSelectedIndividual);
        ivSelectedBusiness = (ImageView) findViewById(R.id.ivSelectedBusiness);
        ivUnselectedIndividual = (ImageView) findViewById(R.id.ivUnselectedIndividual);
        ivUnselectedBusiness = (ImageView) findViewById(R.id.ivUnselectedBusiness);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
        ivLogo = (ImageView) findViewById(R.id.ivLogo);

        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvBrandName = (TextView) findViewById(R.id.tvBrandName);
        tvLocationBus = (TextView) findViewById(R.id.tvLocationBus);
        tvEmailBus = (TextView) findViewById(R.id.tvEmailBus);
        tvDescription = (TextView) findViewById(R.id.tvDescription);

        tvUsername.setText(prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "Username"));
        tvLocation.setText(prefs.getString(AppInfo.INDIVIDUAL_LOCATION, "Location"));
        tvEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        tvBrandName.setText(prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "Brand name"));
        tvLocationBus.setText(prefs.getString(AppInfo.BUSINESS_LOCATION, "Business location"));
        tvEmailBus.setText(prefs.getString(AppInfo.BUSINESS_EMAIL, "business@email.com"));
        tvDescription.setText(prefs.getString(AppInfo.BUSINESS_DESCRIPTION, "Business description"));

        viewIndividual.setOnClickListener(this);
        viewBusiness.setOnClickListener(this);

        setValues();
        insertUserProfile();


        String pageName = "User Accounts";
        GetSetterPageHits setterHits = new GetSetterPageHits(uid, "", pageName, System.currentTimeMillis());
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
        if (v.getId() == R.id.viewIndividual) {
            editor = prefsApp.edit();
            editor.putString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
            editor.apply();
        } else if (v.getId() == R.id.viewBusiness){
            editor = prefsApp.edit();
            editor.putString(AppInfo.ACCOUNT_TYPE, StaticVariables.business);
            editor.apply();
        }
        StaticVariables.listEvents = null;
        StaticVariables.listBusinesses = null;
        StaticVariables.listMessages = null;
        StaticVariables.listReview = null;
        StaticVariables.listComments = null;
        setValues();
    }

    private void setValues() {
        if (prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual).equals(StaticVariables.individual)) {
            ivUnselectedIndividual.setVisibility(View.GONE);
            ivSelectedIndividual.setVisibility(View.VISIBLE);
            ivUnselectedBusiness.setVisibility(View.VISIBLE);
            ivSelectedBusiness.setVisibility(View.GONE);
            getSupportActionBar().setTitle("Individual account selected");

            Window w = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        } else {
            ivUnselectedIndividual.setVisibility(View.VISIBLE);
            ivSelectedIndividual.setVisibility(View.GONE);
            ivUnselectedBusiness.setVisibility(View.GONE);
            ivSelectedBusiness.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("Business account selected");

            Window w = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.appStatusBar));
            toolbar.setBackgroundColor(getResources().getColor(R.color.appBarMainBus));

        }
    }

    private void insertUserProfile() {
        String urlBus = prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
        String urlIndi = prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "");

        int versionBus = prefs.getInt(AppInfo.SIG_VER_BUS, 0);
        int versionInd = prefs.getInt(AppInfo.SIG_VER_INDI, 0);

        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(urlIndi);
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .signature(new StringSignature("" + versionInd))
                    .crossFade()
                    .into(ivProfile);
        } catch (Exception e) {
            ivProfile.setImageResource(0);
        }
        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(urlBus);
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .signature(new StringSignature("" + versionBus))
                    .crossFade()
                    .into(ivLogo);
        } catch (Exception e) {
            ivLogo.setImageResource(0);
        }
    }

}
