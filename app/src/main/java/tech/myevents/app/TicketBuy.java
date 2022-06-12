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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TicketBuy extends AppCompatActivity implements View.OnClickListener {

    View viewEcocash, viewPromoCode;
    TextView tvFreeTickets;
    ProgressBar progressBar;
    String eventKey, promoCode, ticketName1, ticketName2, broadcasterUid;
    int freeTickets, freeTicketsBought = 0, ticketsBought, position, ticketPrice1, ticketPrice2, num, ticketSeats1, ticketSeats2;

    GetSetterEvent setter;

    List listApprovalCodes;
    DBOperations dbOperations;
    SQLiteDatabase db;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    Handler paymentHandler;
    private static int PRICE;
    private static int TICKET_NUM;
    private static String ECOCASH_PAYMENT_MSG;
    private static int PAYMENT_CALLS = 0;
    private static final int REQUEST_READ_SMS = 1884;
    private static boolean NETWORK_AVAILABLE = false;

    DatabaseReference refTickets;
    Toolbar toolbar;
    String from = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_tickets);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setStatusBarColor();

        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);
        paymentHandler = new Handler();

        dbOperations = new DBOperations(this);
        db = dbOperations.getWritableDatabase();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        listApprovalCodes = dbOperations.getApprovalCodes(db, localUid);
        position = getIntent().getExtras().getInt("position");
        num = getIntent().getExtras().getInt("num");

        try {
            from = getIntent().getExtras().getString("from");
        } catch (Exception e) {
        }

        if (from.equals(StaticVariables.playing))
            setter = (GetSetterEvent) StaticVariables.listPlaying.get(position);
        else if (from.equals(StaticVariables.eventExclusive))
            setter = (GetSetterEvent) StaticVariables.listEventsExclusive.get(position);
        else if (from.equals(StaticVariables.eventWaiting))
            setter = (GetSetterEvent) StaticVariables.listEventsWaiting.get(position);
        else
            setter = (GetSetterEvent) StaticVariables.listEvents.get(position);

        eventKey = setter.getEventKey();
        freeTickets = setter.getAvailableTickets();
        promoCode = setter.getTicketPromoCode();
        ticketPrice1 = setter.getTicketsPrice1();
        ticketPrice2 = setter.getTicketPrice2();
        ticketName1 = setter.getTicketName1();
        ticketName2 = setter.getTicketName2();
        broadcasterUid = setter.getBroadcasterUid();
        ticketSeats1 = setter.getTicketSeats1();
        ticketSeats2 = setter.getTicketSeats2();

        viewEcocash = findViewById(R.id.viewEcocash);
        viewPromoCode = findViewById(R.id.viewPromoCode);
        tvFreeTickets = (TextView) findViewById(R.id.tvFreeTickets);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        refTickets = FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childTickets)
                .child(eventKey);

        tvFreeTickets.setText("Number of free tickets issued: " + freeTickets + "");
        viewEcocash.setOnClickListener(this);
        viewPromoCode.setOnClickListener(this);
        getFreeTickets();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Ticket Buy";
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

    private void getFreeTickets() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childTickets)
                .child(eventKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        NETWORK_AVAILABLE = true;
                        if (!dataSnapshot.exists())
                            return;
                        for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                            for (DataSnapshot snap: dataSnapshot1.getChildren()) {
                                GetSetterTicket setterTicket = snap.getValue(GetSetterTicket.class);
                                if (setterTicket.getMerchantName().equals(StaticVariables.free))
                                    freeTicketsBought++;
                                else {
                                    if (setterTicket.getNum() == num)
                                        ticketsBought++;
                                }
                            }
                        }
                        int diff = freeTickets - freeTicketsBought;
                        String free = "Free tickets available: "
                                + (diff < 1 ? freeTickets: diff) + " of " + freeTickets;
                        tvFreeTickets.setText(free);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(getApplicationContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.viewEcocash:
                if (num == 1) {
                    if (ticketName1.equals("") || ticketPrice1 == 0 || ticketSeats1 == 0)
                        Toast.makeText(getApplicationContext(), "Ticket not available", Toast.LENGTH_SHORT).show();
                    else
                        payUsingEcocash(num);
                }
                if (num == 2) {
                    if (ticketName2.equals("") || ticketPrice2 == 0 || ticketSeats2 == 0)
                        Toast.makeText(getApplicationContext(), "Ticket not available", Toast.LENGTH_SHORT).show();
                    else
                        payUsingEcocash(num);
                }
                break;
            case R.id.viewPromoCode:
                if (freeTickets == 0)
                    Toast.makeText(getApplicationContext(), "No free tickets issued.", Toast.LENGTH_SHORT).show();
                else
                    buyUsingPromoCode();
                break;
        }
    }




    private void payUsingEcocash(int num) {
        if (Build.VERSION.SDK_INT >= 23) {
            boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_SMS},
                        REQUEST_READ_SMS);
            } else {
                if (checkTicketValidity(num).equals("yes")) {
                    requestUSSD(num);
                } else {
                    Toast.makeText(getApplicationContext(), checkTicketValidity(num), Toast.LENGTH_SHORT).show();
                }

            }
        } else {
            if (checkTicketValidity(num).equals("yes")) {
                requestUSSD(num);
            } else {
                Toast.makeText(getApplicationContext(), checkTicketValidity(num), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String checkTicketValidity(int num) {
        if (NETWORK_AVAILABLE) {
            if (num == 1) {
                if (ticketsBought >= ticketSeats1) return "Tickets Sold Out";
                else return "yes";
            } else if (num == 2) {
                if (ticketsBought >= ticketSeats2) return "Tickets Sold Out";
                else return "yes";
            }
        } else
            return "Cannot buy ticket: System Connection Error";
        return "yes";
    }

    private void requestUSSD(int num) {
        String price;
        if (num == 1) {
            progressBar.setVisibility(View.VISIBLE);
            price = String.valueOf(ticketPrice1);
            PRICE = Integer.parseInt(price);
            TICKET_NUM = 1;
        } else {
            progressBar.setVisibility(View.VISIBLE);
            price = String.valueOf(ticketPrice2);
            PRICE = Integer.parseInt(price);
            TICKET_NUM = 2;
        }

        String ecocashCode = "*151*3*1*";
        ECOCASH_PAYMENT_MSG = "CashOut";
        if (setter.getEcocashType().equals(StaticVariables.paymentType[2])) {
            ecocashCode = "*151*2*2*";
            ECOCASH_PAYMENT_MSG = "successfully paid";
        } else if (setter.getEcocashType().equals(StaticVariables.paymentType[0])) {
            ecocashCode = "*151*1*1*";
            ECOCASH_PAYMENT_MSG = "Transfer Confirmation";
        }

        String sendMoney = ecocashCode + setter.getMerchantCode() + "*" + price;
        String harsh = Uri.encode("#");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
                    listenForEcocashMessage(PRICE, TICKET_NUM);

                if (PAYMENT_CALLS == 30)
                    PAYMENT_CALLS = 1;
            } catch (Exception e){
                //
            }
        }
    };

    private void listenForEcocashMessage(int cost, int ticketNum) {
        PAYMENT_CALLS += 1;
        Uri inboxUri = Uri.parse("content://sms/inbox");
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(inboxUri, null, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                if (c.getPosition() < 3) {
                    String number = c.getString(c.getColumnIndexOrThrow("address"));
                    String body = c.getString(c.getColumnIndexOrThrow("body"));

                    if (number.contains("+263164")
                            && body.toLowerCase().contains(setter.getMerchantName().toLowerCase())
                            && body.contains(cost + ".00")
                            && body.toLowerCase().contains(ECOCASH_PAYMENT_MSG.toLowerCase())) {
                        int approval = body.indexOf("Approval");
                        int codeIndex = body.indexOf(":", approval);
                        int dot = body.indexOf(".", codeIndex + 18);
                        String approvalCode = body.substring(codeIndex + 1, dot);

                        boolean result = false;
                        GetSetterApprovalCode setterA;
                        for (int x = 0; x < listApprovalCodes.size(); x++) {
                            setterA = (GetSetterApprovalCode) listApprovalCodes.get(x);
                            if (setterA.getApprovalCode().equals(approvalCode) && setterA.getKey().equals(eventKey))
                                result = true;
                        }
                        if (!result) {
                            editor = prefs.edit();
                            editor.putString(AppInfo.TICKET_APPROVAL_CODE, approvalCode);
                            editor.apply();
                            paymentHandler.removeCallbacks(runnablePaymentData);
                            uploadTicketToServer(ticketNum , "");
                            addApprovalCodeToDB("", approvalCode, eventKey, getCurrentTimestamp());

                            setterA = new GetSetterApprovalCode(StaticVariables.free, promoCode, eventKey, getCurrentTimestamp());

                            FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childApprovalCodes)
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .push()
                                    .setValue(setterA);

                            Toast.makeText(getApplicationContext(), "Payment approved, Thank You!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            c.close();
        }
    }





    private void buyUsingPromoCode() {
        if (!NETWORK_AVAILABLE) {
            Toast.makeText(getApplicationContext(), "Cannot buy ticket: System connection error", Toast.LENGTH_SHORT).show();
            getFreeTickets();
            return;
        }

        if (freeTickets <= freeTicketsBought) {
            Toast.makeText(getApplicationContext(), "Free tickets have been exhausted", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean result =false;
        GetSetterApprovalCode setterA = null;
        for (int x = 0; x < listApprovalCodes.size(); x++) {
            setterA = (GetSetterApprovalCode) listApprovalCodes.get(x);
            if (setterA.getType().equals(StaticVariables.free) && setterA.getApprovalCode().equals(promoCode)
                    && setterA.getKey().equals(eventKey))
                result = true;
        }
        if (result)
            Toast.makeText(getApplicationContext(), "Single ticket allowed per promo code", Toast.LENGTH_SHORT).show();
        else {
            editor = prefs.edit();
            editor.putString(AppInfo.TICKET_APPROVAL_CODE, promoCode);
            editor.apply();

            uploadTicketToServer(num, StaticVariables.free);
            freeTicketsBought++;
            addApprovalCodeToDB(StaticVariables.free, promoCode, eventKey, getCurrentTimestamp());
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Free ticket bought", Toast.LENGTH_SHORT).show();

            setterA = new GetSetterApprovalCode(StaticVariables.free, promoCode, eventKey, getCurrentTimestamp());

            FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childApprovalCodes)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .push()
                    .setValue(setterA);
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

    private void uploadTicketToServer(int ticketType, String paymentType) {
        progressBar.setVisibility(View.VISIBLE);
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String ticketNumber = getTicketOrPromotionCode();

        int ticketPrice;
        String ticketName;
        if (ticketType == 1) {
            ticketPrice = ticketPrice1;
            ticketName = ticketName1;
        } else {
            ticketPrice = ticketPrice2;
            ticketName = ticketName2;
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference();
        final String pushKey = ref.child(StaticVariables.childTickets).child(eventKey).child(uid).push().getKey();

        GetSetterTicket setterZ = new
                GetSetterTicket(num, 0, eventKey, pushKey, uid, setter.getBroadcasterUid(), ticketNumber, setter.getVenue(),
                setter.getEventLocation(), setter.getName(), ticketName,
                paymentType.equals("") ? setter.getMerchantName() : paymentType,
                ticketPrice,
                true, setter.getEndTimestamp(), setter.getStartTimestamp(), "");

        ref
                .child(StaticVariables.childTickets)
                .child(eventKey)
                .child(uid)
                .child(pushKey)
                .setValue(setterZ)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Ticket sent to event organiser", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Ticket Uploading Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private String getTicketOrPromotionCode() {
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

        return a + b + c + d + e + f + g + h;
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





    private long getCurrentTimestamp() {
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
                    payUsingEcocash(num);
                } else {
                    Toast.makeText(getApplicationContext(), "Allow app to receive Ecocash Confirmation Message", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

}
