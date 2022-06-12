package tech.myevents.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Random;

public class PromoCode extends AppCompatActivity implements View.OnClickListener {

    EditText etTicketCode, etEventCode, etAdCode;
    TextView tvTicketValidity, tvEventValidity, tvAdValidity;
    ImageView ivTicket, ivEvent, ivAd;
    View viewSend;
    ProgressBar progressBar;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo_code);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);

        ivTicket = (ImageView) findViewById(R.id.ivTicket);
        ivEvent = (ImageView) findViewById(R.id.ivEvent);
        ivAd = (ImageView) findViewById(R.id.ivAd);

        viewSend = findViewById(R.id.viewSend);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        etTicketCode = (EditText) findViewById(R.id.etTicketCode);
        etEventCode = (EditText) findViewById(R.id.etEventCode);
        etAdCode = (EditText) findViewById(R.id.etAdCode);
        tvTicketValidity = (TextView) findViewById(R.id.tvTicketValidity);
        tvEventValidity = (TextView) findViewById(R.id.tvEventValidity);
        tvAdValidity = (TextView) findViewById(R.id.tvAdValidity);

        tvTicketValidity.setOnClickListener(this);
        tvEventValidity.setOnClickListener(this);
        tvAdValidity.setOnClickListener(this);
        viewSend.setOnClickListener(this);
        ivTicket.setOnClickListener(this);
        ivEvent.setOnClickListener(this);
        ivAd.setOnClickListener(this);

        etTicketCode.setText(prefs.getString(AppInfo.AUD_TICKET_CODE, ""));
        etEventCode.setText(prefs.getString(AppInfo.AUD_EVENT_CODE, ""));
        etAdCode.setText(prefs.getString(AppInfo.AUD_AD_CODE, ""));

        tvTicketValidity.setText(prefs.getString(AppInfo.AUD_TICKET_VALIDITY,""));
        tvEventValidity.setText(prefs.getString(AppInfo.AUD_EVENT_VALIDITY, ""));
        tvAdValidity.setText(prefs.getString(AppInfo.AUD_AD_VALIDITY, ""));


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Promo Code";
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
        switch (v.getId()) {
            case R.id.ivTicket:
                getPromotionCode(etTicketCode, "t");
                break;
            case R.id.ivEvent:
                getPromotionCode(etEventCode, "e");
                break;
            case R.id.ivAd:
                getPromotionCode(etAdCode, "a");
                break;
            case R.id.viewSend:
                savePromotionCodes();
                break;
            case R.id.tvAdValidity:
                PopupMenu popupMenu1 = new PopupMenu(this, tvAdValidity);
                popupMenu1.getMenuInflater().inflate(R.menu.menu_duration, popupMenu1.getMenu());
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        StringBuilder builder = new StringBuilder();
                        builder.append(getDate(String.valueOf(item.getTitle())));
                        tvAdValidity.setText(builder);
                        editor = prefs.edit();
                        editor.putString(AppInfo.AUD_AD_VALIDITY_CODE, item.getTitle().toString());
                        editor.putString(AppInfo.AUD_AD_VALIDITY, builder.toString());
                        editor.apply();
                        return true;
                    }
                });
                popupMenu1.show();
                break;
            case R.id.tvEventValidity:
                PopupMenu popupMenu2 = new PopupMenu(this, tvAdValidity);
                popupMenu2.getMenuInflater().inflate(R.menu.menu_duration, popupMenu2.getMenu());
                popupMenu2.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        StringBuilder builder = new StringBuilder();
                        builder.append((getDate(String.valueOf(item.getTitle()))));
                        tvEventValidity.setText(builder);
                        editor = prefs.edit();
                        editor.putString(AppInfo.AUD_EVENT_VALIDITY_CODE, item.getTitle().toString());
                        editor.putString(AppInfo.AUD_EVENT_VALIDITY, builder.toString());
                        editor.apply();
                        return true;
                    }
                });
                popupMenu2.show();
                break;
            case R.id.tvTicketValidity:
                PopupMenu popupMenu3 = new PopupMenu(this, tvAdValidity);
                popupMenu3.getMenuInflater().inflate(R.menu.menu_duration, popupMenu3.getMenu());
                popupMenu3.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        StringBuilder builder = new StringBuilder();
                        builder.append((getDate(String.valueOf(item.getTitle()))));
                        tvTicketValidity.setText(builder);
                        editor = prefs.edit();
                        editor.putString(AppInfo.AUD_TICKET_VALIDITY_CODE, item.getTitle().toString());
                        editor.putString(AppInfo.AUD_TICKET_VALIDITY, builder.toString());
                        editor.apply();
                        return true;
                    }
                });
                popupMenu3.show();
                break;
        }
    }


    private void getPromotionCode(EditText editText, String s) {
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

        editText.setText(a + b + c + d + e + f + g + h);
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


    public void savePromotionCodes() {
        progressBar.setVisibility(View.VISIBLE);

        String ticketCode = etTicketCode.getText().toString();
        String eventCode = etEventCode.getText().toString();
        String adCode = etAdCode.getText().toString();
        long ticketValidity = getDuration(prefs.getString(AppInfo.AUD_TICKET_VALIDITY_CODE, "1"));
        long eventValidity = getDuration(prefs.getString(AppInfo.AUD_EVENT_VALIDITY_CODE, "1"));
        long adValidity = getDuration(prefs.getString(AppInfo.AUD_AD_VALIDITY_CODE, "1"));

        GetSetterPromotion setter = new GetSetterPromotion(ticketCode, eventCode, adCode, ticketValidity, eventValidity, adValidity);

        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childPromotionCode)
                .setValue(setter)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Promotion Code Saved", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Promotion Code saving failed", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
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

    String getDate(String duration) {
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(adDuration);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return "" + hour + ":" + minute + ", " + day + "-" + StaticVariables.months[month + 1] + "-" + year;
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




    @Override
    protected void onPause() {
        super.onPause();
        editor = prefs.edit();

        editor.putString(AppInfo.AUD_TICKET_CODE, etTicketCode.getText().toString());
        editor.putString(AppInfo.AUD_EVENT_CODE, etEventCode.getText().toString());
        editor.putString(AppInfo.AUD_AD_CODE, etAdCode.getText().toString());

        editor.putString(AppInfo.AUD_TICKET_VALIDITY, tvTicketValidity.getText().toString());
        editor.putString(AppInfo.AUD_EVENT_VALIDITY, tvEventValidity.getText().toString());
        editor.putString(AppInfo.AUD_AD_VALIDITY, tvAdValidity.getText().toString());

        editor.apply();
    }

}
