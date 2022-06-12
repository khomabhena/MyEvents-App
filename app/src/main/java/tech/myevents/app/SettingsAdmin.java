package tech.myevents.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsAdmin extends AppCompatActivity implements View.OnClickListener {

    TextView tvAppVersion, tvBroadcast, tvBroadcastCharges, tvPromoCode, tvWelcomeMessages, tvVerifyContacts, tvVerifyBusinesses;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setStatusBarColor();

        tvAppVersion = (TextView) findViewById(R.id.tvAppVersion);
        tvBroadcast = (TextView) findViewById(R.id.tvBroadcast);
        tvBroadcastCharges = (TextView) findViewById(R.id.tvBroadcastCharges);
        tvPromoCode = (TextView) findViewById(R.id.tvPromoCode);
        tvWelcomeMessages = (TextView) findViewById(R.id.tvWelcomeMessages);
        tvVerifyContacts = (TextView) findViewById(R.id.tvVerifyContacts);
        tvVerifyBusinesses = (TextView) findViewById(R.id.tvVerifyBusinesses);

        tvAppVersion.setOnClickListener(this);
        tvBroadcast.setOnClickListener(this);
        tvBroadcastCharges.setOnClickListener(this);
        tvPromoCode.setOnClickListener(this);
        tvWelcomeMessages.setOnClickListener(this);
        tvVerifyContacts.setOnClickListener(this);
        tvVerifyBusinesses.setOnClickListener(this);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Settings Admin";
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
        switch (v.getId()) {
            case R.id.tvAppVersion:
                startActivity(new Intent(this, AppVersion.class));
                break;
            case R.id.tvBroadcast:
                startActivity(new Intent(this, Publish.class));
                break;
            case R.id.tvBroadcastCharges:
                startActivity(new Intent(this, BroadcastCharges.class));
                break;
            case R.id.tvPromoCode:
                startActivity(new Intent(this, PromoCode.class));
                break;
            case R.id.tvWelcomeMessages:
                startActivity(new Intent(this, WelcomeMessages.class));
                break;
            case R.id.tvVerifyContacts:
                startActivity(new Intent(this, VerifyContacts.class));
                break;
            case R.id.tvVerifyBusinesses:
                startActivity(new Intent(this, VerifyBusiness.class));
                break;
        }
    }

}
