package tech.myevents.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class TicketJoinSigning extends AppCompatActivity implements View.OnClickListener {

    TextView tvPrice, tvMerchantName, tvName, tvLocation, tvVenue, tvMonth, tvTime, tvTicket, tvCategory;
    AutoCompleteTextView autoSearchTicket;
    CardView cardSearchCode, cardTicket, cardCancel;
    ProgressBar progressBar;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_ticket_signing);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);

        autoSearchTicket = (AutoCompleteTextView) findViewById(R.id.autoSearchTicket);
        cardSearchCode = (CardView) findViewById(R.id.cardSearchCode);
        tvPrice = (TextView) findViewById(R.id.tvPriceR);
        tvMerchantName = (TextView) findViewById(R.id.tvMerchantNameX);
        tvName = (TextView) findViewById(R.id.tvNameR);
        tvLocation = (TextView) findViewById(R.id.tvLocationR);
        tvVenue = (TextView) findViewById(R.id.tvVenueR);
        tvMonth = (TextView) findViewById(R.id.tvMonthR);
        tvTime = (TextView) findViewById(R.id.tvTimeR);
        tvTicket = (TextView) findViewById(R.id.tvTicketR);
        tvCategory = (TextView) findViewById(R.id.tvCategoryR);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        cardTicket = (CardView) findViewById(R.id.cardTicketR);
        cardCancel = (CardView) findViewById(R.id.cardCancel);

        if (prefs.getLong(AppInfo.MTS_ENDTIMESTAMP, 0) < getCurrentTimestamp()) {
            editor = prefs.edit();
            editor.putBoolean(AppInfo.MTS_TICKET_SIGNING, false);
            editor.putLong(AppInfo.MTS_ENDTIMESTAMP, 0);
            editor.putString(AppInfo.MTS_SIGNING_UID, "");
            editor.apply();
        } else {
            loadTicket();
        }

        cardSearchCode.setOnClickListener(this);
        cardCancel.setOnClickListener(this);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Join Ticket Signing";
        GetSetterPageHits setterHits = new GetSetterPageHits(uid, "", pageName, getCurrentTimestamp());
        new PageHitsBackTask().execute(setterHits);
    }

    private void loadTicket() {
        final String uid = prefs.getString(AppInfo.MTS_SIGNING_UID, "");
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childTickets)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        for (DataSnapshot snapKey : dataSnapshot.getChildren()) {
                            for (DataSnapshot snapUid: snapKey.getChildren()) {
                                for (DataSnapshot snapUser: snapUid.getChildren()) {
                                    GetSetterTicket setterTicket = snapUser.getValue(GetSetterTicket.class);
                                    if (setterTicket.getBroadcasterUid().equals(uid)) {
                                        if (getCurrentTimestamp() > (setterTicket.getEndTimestamp() + (86400000 * 2))) {
                                            editor = prefs.edit();
                                            editor.putBoolean(AppInfo.MTS_TICKET_SIGNING, false);
                                            editor.putLong(AppInfo.MTS_ENDTIMESTAMP, 0);
                                            editor.putString(AppInfo.MTS_SIGNING_UID, "");
                                            editor.apply();
                                            return;
                                        }

                                        cardTicket.setVisibility(View.VISIBLE);
                                        cardCancel.setVisibility(View.VISIBLE);

                                        editor = prefs.edit();
                                        editor.putBoolean(AppInfo.MTS_TICKET_SIGNING, true);
                                        editor.putLong(AppInfo.MTS_ENDTIMESTAMP, setterTicket.getEndTimestamp());
                                        editor.putString(AppInfo.MTS_SIGNING_UID, setterTicket.getBroadcasterUid());
                                        editor.apply();

                                        cardTicket.setVisibility(View.VISIBLE);
                                        tvCategory.setText(setterTicket.getCategory());
                                        tvMonth.setText(getDate(setterTicket.getStartTimestamp(), "month"));
                                        tvTime.setText(getDate(setterTicket.getStartTimestamp(), "time"));
                                        tvName.setText(setterTicket.getName());
                                        tvLocation.setText(setterTicket.getLocation());
                                        tvVenue.setText(setterTicket.getVenue());
                                        tvMerchantName.setText(setterTicket.getTicketNumber());
                                        tvPrice.setText("$" + setterTicket.getTicketPrice() + "");
                                        tvTicket.setText(setterTicket.getCategory());

                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(getApplicationContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
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
            case R.id.cardSearchCode:
                    searchForMTSCOde();
                break;
            case R.id.cardCancel:
                editor = prefs.edit();
                editor.putBoolean(AppInfo.MTS_TICKET_SIGNING, false);
                editor.putLong(AppInfo.MTS_ENDTIMESTAMP, 0);
                editor.putString(AppInfo.MTS_SIGNING_UID, "");
                editor.apply();
                cardTicket.setVisibility(View.GONE);
                cardCancel.setVisibility(View.GONE);
                break;
        }
    }

    private void searchForMTSCOde() {
        if (autoSearchTicket.getText().toString().trim().equals(""))
            return;
        final String mtsCode = autoSearchTicket.getText().toString();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childTickets)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        for (DataSnapshot snapKey : dataSnapshot.getChildren()) {
                            for (DataSnapshot snapUid: snapKey.getChildren()) {
                                for (DataSnapshot snapUser: snapUid.getChildren()) {
                                    GetSetterTicket setterTicket = snapUser.getValue(GetSetterTicket.class);
                                    if (setterTicket.getMtsCode().equals(mtsCode)) {
                                        if (getCurrentTimestamp() > (setterTicket.getEndTimestamp() + (86400000 * 2))) {
                                            editor = prefs.edit();
                                            editor.putBoolean(AppInfo.MTS_TICKET_SIGNING, false);
                                            editor.putLong(AppInfo.MTS_ENDTIMESTAMP, 0);
                                            editor.putString(AppInfo.MTS_SIGNING_UID, "");
                                            editor.apply();
                                            return;
                                        }
                                        editor = prefs.edit();
                                        editor.putBoolean(AppInfo.MTS_TICKET_SIGNING, true);
                                        editor.putLong(AppInfo.MTS_ENDTIMESTAMP, setterTicket.getEndTimestamp());
                                        editor.putString(AppInfo.MTS_SIGNING_UID, setterTicket.getBroadcasterUid());
                                        editor.apply();

                                        cardTicket.setVisibility(View.VISIBLE);
                                        cardCancel.setVisibility(View.VISIBLE);

                                        tvCategory.setText(setterTicket.getCategory());
                                        tvMonth.setText(getDate(setterTicket.getStartTimestamp(), "month"));
                                        tvTime.setText(getDate(setterTicket.getStartTimestamp(), "time"));
                                        tvName.setText(setterTicket.getName());
                                        tvLocation.setText(setterTicket.getLocation());
                                        tvVenue.setText(setterTicket.getVenue());
                                        tvMerchantName.setText(setterTicket.getTicketNumber());
                                        tvPrice.setText("$" + setterTicket.getTicketPrice() + "");
                                        tvTicket.setText(setterTicket.getCategory());

                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(getApplicationContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    String getDate(long timeReceived, String type) {
        String[] monthsSmall = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeReceived);


        if (type.equals("time")) {
            return "" + getTheValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getTheValue(calendar.get(Calendar.MINUTE));
        } else {
            return "" + calendar.get(Calendar.DAY_OF_MONTH) + "-" + monthsSmall[calendar.get(Calendar.MONTH) +1] + "-" +
                    getYear(calendar.get(Calendar.YEAR));
        }
    }

    private String getTheValue(int value){
        String theValue = String.valueOf(value);
        if (theValue.length() == 1){
            return "0"+theValue;
        } else {
            return theValue;
        }
    }

    private String getYear(int year) {
        String year2 = String.valueOf(year);
        return year2.substring(2, 4);
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
