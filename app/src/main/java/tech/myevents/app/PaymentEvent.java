package tech.myevents.app;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static tech.myevents.app.BroadcastEvent.promotionCode;

public class PaymentEvent extends AppCompatActivity implements View.OnClickListener {

    TextView tvPromoCode, tvLoyalty;
    View viewEcocash, viewPromoCode, viewLoyalty;
    ProgressBar progressBar;

    List listApprovalCodes;
    DBOperations dbOperations;
    SQLiteDatabase db;

    SharedPreferences prefs;
    SharedPreferences prefsApp;
    SharedPreferences.Editor editor;

    Handler paymentHandler;
    private static int PAYMENT_CALLS = 0;
    private static final int REQUEST_READ_SMS = 1884;
    private static boolean NETWORK_AVAILABLE = false;

    GetSetterBroadcastCharges broadcastChargesSetter;
    GetSetterPromotion setterPromo;
    String from = "", type;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_payments);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        if (type.equals(StaticVariables.business)) {
            Window w = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.appStatusBar));
            toolbar.setBackgroundColor(getResources().getColor(R.color.appBarMainBus));
        }
        try {
            from = getIntent().getExtras().getString("from");
        } catch (Exception e) {
            //
        }
        paymentHandler = new Handler();

        dbOperations = new DBOperations(this);
        db = dbOperations.getWritableDatabase();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        listApprovalCodes = dbOperations.getApprovalCodes(db, localUid);

        if (!from.equals(""))
            setterPromo = (GetSetterPromotion) BroadcastExclusiveEvent.promotionCode.get(0);
        else
            setterPromo = (GetSetterPromotion) promotionCode.get(0);

        tvPromoCode = (TextView) findViewById(R.id.tvPromoCode);
        tvLoyalty = (TextView) findViewById(R.id.tvLoyalty);
        viewEcocash = findViewById(R.id.viewEcocash);
        viewPromoCode = findViewById(R.id.viewPromoCode);
        viewLoyalty = findViewById(R.id.viewLoyalty);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        viewEcocash.setOnClickListener(this);
        viewPromoCode.setOnClickListener(this);
        viewLoyalty.setOnClickListener(this);

        tvPromoCode.setText("the promotion code is: " + setterPromo.getEventPaymentCode() + "");

        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childPromotionCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        promotionCode = new ArrayList();
                        promotionCode.add(dataSnapshot.getValue(GetSetterPromotion.class));
                        setterPromo = (GetSetterPromotion) promotionCode.get(0);
                        tvPromoCode.setText("the promotion code is: " + setterPromo.getEventPaymentCode() + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Payment Event";
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
            case R.id.viewEcocash:
                payUsingEcocash();
                break;
            case R.id.viewPromoCode:
                confirmPromotionCode();
                break;
            case R.id.viewLoyalty:
                break;
        }
    }





    private void confirmPromotionCode() {
        if (promotionCode == null) {
            Toast.makeText(getApplicationContext(), "Network Error.", Toast.LENGTH_LONG).show();
            finish();
        }
        progressBar.setVisibility(View.VISIBLE);
        String promoCode = setterPromo.getEventPaymentCode();
        boolean result = false;
        GetSetterApprovalCode setterA = null;
        for (int x = 0; x < listApprovalCodes.size(); x++) {
            setterA = (GetSetterApprovalCode) listApprovalCodes.get(x);
            if (setterA.getType().equals(StaticVariables.free) && setterA.getApprovalCode().equals(promoCode)
                    && setterA.getTimestamp() == setterPromo.getAdValidUntil())
                result = true;
        }
        if (!result) {
            editor = prefs.edit();
            if (from.equals("")) {
                editor.putBoolean(AppInfo.EVENT_BROADCAST_PAYMENT, true);
                editor.putString(AppInfo.EVENT_APPROVAL_CODE, String.valueOf(promoCode));
            } else {
                editor.putBoolean(AppInfo.EVENT_BROADCAST_PAYMENT_EX, true);
                editor.putString(AppInfo.EVENT_APPROVAL_CODE_EX, String.valueOf(promoCode));
            }
            editor.apply();

            addApprovalCodeToDB(StaticVariables.free, promoCode, "event payment", setterPromo.getEventValidUntil());

            setterA = new GetSetterApprovalCode(StaticVariables.free, promoCode, "event payment", setterPromo.getEventValidUntil());

            FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childApprovalCodes)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .push()
                    .setValue(setterA);

            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Code was valid: You can now broadcast.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Already used the code", Toast.LENGTH_SHORT).show();
        }
    }




    private void payUsingEcocash() {
        if (!NETWORK_AVAILABLE) {
            Toast.makeText(getApplicationContext(), "Internet Connection Error", Toast.LENGTH_LONG).show();
            return;
        }
        if (from.equals("")) {
            if (prefs.getString(AppInfo.EVENT_NAME, "").isEmpty()
                    || prefs.getString(AppInfo.EVENT_VENUE, "").isEmpty()
                    || prefs.getString(AppInfo.EVENT_LOCATION, "").isEmpty()
                    || prefs.getString(AppInfo.EVENT_INTEREST_CODE, "").equals("")
                    || prefs.getLong(AppInfo.EVENT_START_TIMESTAMP, 0) == 0
                    || prefs.getLong(AppInfo.EVENT_END_TIMESTAMP, 0) == 0) {
                Toast.makeText(getApplicationContext(), "Fill out all details in your event form", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            if (prefs.getString(AppInfo.EVENT_NAME_EX, "").isEmpty()
                    || prefs.getString(AppInfo.EVENT_VENUE_EX, "").isEmpty()
                    || prefs.getString(AppInfo.EVENT_LOCATION_EX, "").isEmpty()
                    || prefs.getString(AppInfo.EVENT_INTEREST_CODE_EX, "").equals("")
                    || prefs.getLong(AppInfo.EVENT_START_TIMESTAMP_EX, 0) == 0
                    || prefs.getLong(AppInfo.EVENT_END_TIMESTAMP_EX, 0) == 0) {
                Toast.makeText(getApplicationContext(), "Fill out all details in your event form", Toast.LENGTH_LONG).show();
                return;
            }
        }
        progressBar.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= 23) {
            boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_SMS},
                        REQUEST_READ_SMS);
            } else {
                requestUSSD();
            }
        } else {
            requestUSSD();
        }
    }

    private void requestUSSD() {
        String ecocashNumber = StaticVariables.adminEcocashNumber;
        if (broadcastChargesSetter != null) {
            broadcastChargesSetter = (GetSetterBroadcastCharges) BroadcastEvent.broadcastCharges.get(0);
            if (broadcastChargesSetter.getEcocashNumber() != 0) {
                ecocashNumber = String.valueOf(broadcastChargesSetter.getEcocashNumber());
            }
        }
        String cost;
        if (from.equals(""))
            cost = String.valueOf(prefs.getInt(AppInfo.EVENT_COST, 2));
        else
            cost = String.valueOf(prefs.getInt(AppInfo.EVENT_COST_EX, 2));

        String sendMoney = "*151*1*1*" + ecocashNumber + "*" + cost;
        String harsh = Uri.encode("#");

        if (ActivityCompat.checkSelfPermission(PaymentEvent.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startSmsListen();
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + sendMoney + harsh)));
    }

    private void startSmsListen() {
        paymentHandler.post(runnablePaymentData);
    }

    private final Runnable runnablePaymentData = new Runnable() {
        @Override
        public void run() {
            try {
                paymentHandler.postDelayed(this, 10000);
                if (PAYMENT_CALLS < 30)
                    listenForEcocashMessage(1);

                if (PAYMENT_CALLS == 30)
                    PAYMENT_CALLS = 0;
            } catch (Exception e){
                //
            }
        }
    };

    private void listenForEcocashMessage(int cost) {
        PAYMENT_CALLS += 1;
        Uri inboxUri = Uri.parse("content://sms/inbox");
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(inboxUri, null, null, null, null);
        String name = "KHOLWANI MABHENA";
        if (c != null) {
            while (c.moveToNext()) {
                if (c.getPosition() < 8) {
                    String number = c.getString(c.getColumnIndexOrThrow("address"));
                    String body = c.getString(c.getColumnIndexOrThrow("body"));

                    if (number.contains("+263164") && body.toLowerCase().contains(name.toLowerCase()) && body.contains(cost + ".00") && body.contains("Transfer Confirmation")) {
                        int approval = body.indexOf("Approval");
                        int codeIndex = body.indexOf(":", approval);
                        int dot = body.indexOf(".", codeIndex + 18);
                        String approvalCode = body.substring(codeIndex + 1, dot);

                        GetSetterApprovalCode setterA;
                        boolean result = false;
                        for (int x = 0; x < listApprovalCodes.size(); x++) {
                            setterA = (GetSetterApprovalCode) listApprovalCodes.get(x);
                            if (setterA.getApprovalCode().equals(approvalCode))
                                result = true;
                        }
                        if (!result) {
                            progressBar.setVisibility(View.INVISIBLE);
                            editor = prefs.edit();
                            if (from.equals("")) {
                                editor.putBoolean(AppInfo.EVENT_BROADCAST_PAYMENT, true);
                                editor.putString(AppInfo.EVENT_APPROVAL_CODE, approvalCode);
                            } else {
                                editor.putBoolean(AppInfo.EVENT_BROADCAST_PAYMENT_EX, true);
                                editor.putString(AppInfo.EVENT_APPROVAL_CODE_EX, approvalCode);
                            }
                            editor.apply();
                            paymentHandler.removeCallbacks(runnablePaymentData);

                            addApprovalCodeToDB("", approvalCode, "ad payment", getCurrentTimestamp());

                            setterA = new GetSetterApprovalCode("", approvalCode, "event payment", getCurrentTimestamp());

                            FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childApprovalCodes)
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .push()
                                    .setValue(setterA);

                            Toast.makeText(getApplicationContext(), "Your payment has been processed, Thank You", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
            }
            c.close();
        }
    }

    private void addApprovalCodeToDB(String free, String promoCode, String eventKey, long currentTimestamp) {
        ContentValues values = new ContentValues();
        values.put(DBContract.ApprovalCodes.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
        values.put(DBContract.ApprovalCodes.TYPE, free);
        values.put(DBContract.ApprovalCodes.APPROVAL_CODE, promoCode);
        values.put(DBContract.ApprovalCodes.KEY, eventKey);
        values.put(DBContract.ApprovalCodes.TIMESTAMP, "" + currentTimestamp);
        db.insert(DBContract.ApprovalCodes.TABLE_NAME, null, values);

        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        listApprovalCodes = dbOperations.getApprovalCodes(db, localUid);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_READ_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //reload my activity with permission granted or use the features that required the permission
                    payUsingEcocash();
                } else {
                    Toast.makeText(getApplicationContext(), "Allow app to receive Ecocash Confirmation Message", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

}
