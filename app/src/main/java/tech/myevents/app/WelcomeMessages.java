package tech.myevents.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class WelcomeMessages extends AppCompatActivity implements View.OnClickListener {

    EditText et1, et2, et3, et4, et5, et6, et7, et8, et9, et10;
    View viewSend;
    ProgressBar progressBar;

    SharedPreferences prefs, prefsApp;
    Toolbar toolbar;
    String accType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_messages);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setStatusBarColor();

        prefs = getSharedPreferences(AppInfo.USER_INFO, Context.MODE_PRIVATE);
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        et1 = (EditText) findViewById(R.id.et1);
        et2 = (EditText) findViewById(R.id.et2);
        et3 = (EditText) findViewById(R.id.et3);
        et4 = (EditText) findViewById(R.id.et4);
        et5 = (EditText) findViewById(R.id.et5);
        et6 = (EditText) findViewById(R.id.et6);
        et7 = (EditText) findViewById(R.id.et7);
        et8 = (EditText) findViewById(R.id.et8);
        et9 = (EditText) findViewById(R.id.et9);
        et10 = (EditText) findViewById(R.id.et10);
        viewSend = findViewById(R.id.viewSend);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        viewSend.setOnClickListener(this);
        getWelcomeMessages();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Welcome Messages";
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
        progressBar.setVisibility(View.VISIBLE);
        EditText[] editTexts = {et1, et1, et2, et3, et4, et5, et6, et7, et8, et9, et10};

        for (int x = 1; x <= editTexts.length; x++) {
            String sendTo = "";
            String sender = "";
            String receiver = "";
            String senderUid = "";
            String receiverUid = "";
            String key = "" + x;
            String chatRoom = FirebaseAuth.getInstance().getCurrentUser().getUid() + "myevents";
            String message = editTexts[x].getText().toString();
            long timestamp = getCurrentTimestamp();
            boolean verified = false;

            if (x % 2 == 1) {
                sender = "MyEvents App";
                senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                verified = true;
            } else {
                sendTo = FirebaseAuth.getInstance().getCurrentUser().getUid();
                receiver = "MyEvents App";
                receiverUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
            int sigVer = prefs.getInt(AppInfo.SIG_VER_BUS, 0);

            GetSetterInbox setter = new GetSetterInbox(sendTo, key, chatRoom, sender, receiver, senderUid,
                    receiverUid, message, timestamp, verified, false,
                    prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, ""), "", sigVer, accType);

            FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childWelcomeMessages)
                    .child("" + x)
                    .setValue(setter)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Welcome Messages Saved", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void getWelcomeMessages() {
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childWelcomeMessages)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        int count = 0;
                        EditText[] editTexts = {et1, et2, et3, et4, et5, et6, et7, et8, et9, et10};
                        for (DataSnapshot snap: dataSnapshot.getChildren()) {
                            GetSetterInbox setter = snap.getValue(GetSetterInbox.class);
                            editTexts[count].setText(setter.getMessage());
                            count++;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
