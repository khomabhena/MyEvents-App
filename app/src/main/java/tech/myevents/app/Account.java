package tech.myevents.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static tech.myevents.app.MainActivity.currentPage;

public class Account extends AppCompatActivity implements View.OnClickListener {

    ProgressBar progressBar;
    View viewDelete, viewLogOut;

    SharedPreferences prefs;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        viewDelete = (View) findViewById(R.id.viewDelete);
        viewLogOut = (View) findViewById(R.id.viewLogOut);

        viewDelete.setOnClickListener(this);
        viewLogOut.setOnClickListener(this);


        String pageName = "Settings Account";
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
            case R.id.viewDelete:
                break;
            case R.id.viewLogOut:
                FirebaseAuth.getInstance().signOut();
                FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user == null) {
                            MainActivity.USER_NOTIFICATIONS = false;
                            MainActivity.GET_SIGNATURE_VERSIONS = false;

                            StaticVariables.listTickets = null;
                            StaticVariables.listEventsWaiting = null;
                            StaticVariables.listEvents = null;
                            StaticVariables.listEventsExclusive = null;
                            StaticVariables.listAds = null;
                            StaticVariables.listAdsWaiting = null;
                            StaticVariables.listPlaying = null;
                            StaticVariables.listMoments = null;
                            StaticVariables.listReview = null;
                            StaticVariables.listMessages = null;
                            StaticVariables.listInbox = null;
                            StaticVariables.listComments = null;

                            currentPage = 1;
                            startActivity(new Intent(Account.this, Login.class));
                            finish();
                        }
                    }
                });
                break;
        }
    }
}
