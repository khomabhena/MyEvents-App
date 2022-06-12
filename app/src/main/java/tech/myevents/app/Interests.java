package tech.myevents.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Switch;

import com.google.firebase.auth.FirebaseAuth;

public class Interests extends AppCompatActivity {

    Switch switch1, switch2, switch3, switch4, switch5, switch6,
            switch7, switch8, switch9, switch10, switch11, switch12, switch13, switch14, switch15, switch16, switch17;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);

        switch1 = (Switch) findViewById(R.id.switch1);
        switch2 = (Switch) findViewById(R.id.switch2);
        switch3 = (Switch) findViewById(R.id.switch3);
        switch4 = (Switch) findViewById(R.id.switch4);
        switch5 = (Switch) findViewById(R.id.switch5);
        switch6 = (Switch) findViewById(R.id.switch6);
        switch7 = (Switch) findViewById(R.id.switch7);
        switch8 = (Switch) findViewById(R.id.switch8);
        switch9 = (Switch) findViewById(R.id.switch9);
        switch10 = (Switch) findViewById(R.id.switch10);
        switch11 = (Switch) findViewById(R.id.switch11);
        switch12 = (Switch) findViewById(R.id.switch12);
        switch13 = (Switch) findViewById(R.id.switch13);
        switch14 = (Switch) findViewById(R.id.switch14);
        switch15 = (Switch) findViewById(R.id.switch15);
        switch16 = (Switch) findViewById(R.id.switch16);
        switch17 = (Switch) findViewById(R.id.switch17);



        Switch[] switches = {switch1, switch2, switch3, switch4, switch5, switch6,
                switch7, switch8, switch9, switch10, switch11, switch12, switch13, switch14, switch15, switch16, switch17};
        checkSwitches(switches);


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Interests";
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

    private void checkSwitches(Switch[] switches) {
        String interestCode = prefs.getString(AppInfo.INTEREST_CODE, "");
        for (int x = 0; x < StaticVariables.interestCodes.length; x++) {
            if (interestCode.contains(StaticVariables.interestCodes[x]))
                switches[x].setChecked(true);
        }
    }

    private void changeInterests(String code, Switch theSwitch) {
        String interestCode = prefs.getString(AppInfo.INTEREST_CODE, "");
        editor = prefs.edit();
        if (interestCode.contains(code)) {
            editor.putString(AppInfo.INTEREST_CODE, interestCode.replace(code, ""));
            theSwitch.setChecked(false);
        } else {
            editor.putString(AppInfo.INTEREST_CODE, interestCode + code);
            theSwitch.setChecked(true);
        }
        editor.apply();
    }

    public void onSwitchAccount(View view) {
        switch (view.getId()) {
            case R.id.switch1:
                changeInterests("1a", switch1);
                break;
            case R.id.switch2:
                changeInterests("1b", switch2);
                break;
            case R.id.switch3:
                changeInterests("1c", switch3);
                break;
            case R.id.switch4:
                changeInterests("1d", switch4);
                break;
            case R.id.switch5:
                changeInterests("1e", switch5);
                break;
            case R.id.switch6:
                changeInterests("1f", switch6);
                break;
            case R.id.switch7:
                changeInterests("1g", switch7);
                break;
            case R.id.switch8:
                changeInterests("1h", switch8);
                break;
            case R.id.switch9:
                changeInterests("1i", switch9);
                break;
            case R.id.switch10:
                changeInterests("1j", switch10);
                break;
            case R.id.switch11:
                changeInterests("1k", switch11);
                break;
            case R.id.switch12:
                changeInterests("1l", switch12);
                break;
            case R.id.switch13:
                changeInterests("1m", switch13);
                break;
            case R.id.switch14:
                changeInterests("1n", switch14);
                break;
            case R.id.switch15:
                changeInterests("1o", switch15);
                break;
            case R.id.switch16:
                changeInterests("1p", switch16);
                break;
            case R.id.switch17:
                changeInterests("1q", switch17);
                break;
        }
    }


}
