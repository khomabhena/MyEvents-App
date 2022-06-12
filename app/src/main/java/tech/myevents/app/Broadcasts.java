package tech.myevents.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.google.firebase.auth.FirebaseAuth;

import static tech.myevents.app.MainActivity.currentPage;

public class Broadcasts extends AppCompatActivity implements View.OnClickListener {

    View viewAd, viewEvent, viewExclusive;

    SharedPreferences prefsApp;
    String type;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcasts);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar()!= null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MainActivity.currentPage = 7;
        setStatusBarColor();

        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        if (type.equals(StaticVariables.business)) {
            Window w = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.appStatusBar));
            toolbar.setBackgroundColor(getResources().getColor(R.color.appBarMainBus));
        }

        viewAd = findViewById(R.id.viewAd);
        viewEvent = findViewById(R.id.viewEvent);
        viewExclusive = findViewById(R.id.viewExclusive);

        viewAd.setOnClickListener(this);
        viewEvent.setOnClickListener(this);
        viewExclusive.setOnClickListener(this);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Broadcasts";
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
            case R.id.viewAd:
                startActivity(new Intent(this, BroadcastAd.class));
                break;
            case R.id.viewEvent:
                startActivity(new Intent(this, BroadcastEvent.class));
                break;
            case R.id.viewExclusive:
                startActivity(new Intent(this, BroadcastExclusiveEvent.class));
                break;
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
