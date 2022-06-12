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
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class TicketSell extends AppCompatActivity implements View.OnClickListener {

    EditText etName1, etName2, etPrice1, etPrice2, etSeats1, etSeats2, etFreeTickets;
    TextView tvCategoryR, tvCategoryS, tvTicketR, tvTicketS, tvTimeR, tvTimeS, tvMonthR, tvMonthS, tvVenueR, tvVenueS,
            tvLocationR, tvLocationS, tvNameR, tvNameS, tvMerchantNameR, tvMerchantNameS, tvPriceR, tvPriceS;
    CardView cardTicketR, cardTicketS;
    View viewRefresh;

    TextView[] textViews = {tvCategoryR, tvCategoryS, tvTicketR, tvTicketS, tvTimeR, tvTimeS, tvMonthR, tvMonthS,
            tvVenueR, tvVenueS,
            tvLocationR, tvLocationS, tvNameR, tvNameS, tvMerchantNameR, tvMerchantNameS, tvPriceR, tvPriceS};

    int[] textViewIds = {R.id.tvCategoryR, R.id.tvCategoryS, R.id.tvTicketR, R.id.tvTicketS, R.id.tvTimeR, R.id.tvTimeS,
            R.id.tvMonthR, R.id.tvMonthS, R.id.tvVenueR, R.id.tvVenueS, R.id.tvLocationR, R.id.tvLocationS,
            R.id.tvNameR, R.id.tvNameS, R.id.tvMerchantNameR, R.id.tvMerchantNameS, R.id.tvPriceR, R.id.tvPriceS};

    EditText[] editTexts = {etName1, etName2, etPrice1, etPrice2, etSeats1, etSeats2};

    int[] editTextIds = {R.id.etName1, R.id.etName2, R.id.etPrice1, R.id.etPrice2, R.id.etSeats1, R.id.etSeats2};

    SharedPreferences prefs;
    SharedPreferences prefsApp;
    String type;
    SharedPreferences.Editor editor;
    String from = "";

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_tickets);
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

        cardTicketR = (CardView) findViewById(R.id.cardTicketR);
        cardTicketS = (CardView) findViewById(R.id.cardTicketS);
        tvCategoryR = (TextView) findViewById(R.id.tvCategoryR);
        viewRefresh = findViewById(R.id.viewRefresh);
        etFreeTickets = (EditText) findViewById(R.id.etFreeTickets);
        tvCategoryR = (TextView) findViewById(R.id.tvCategoryR);
        tvCategoryS = (TextView) findViewById(R.id.tvCategoryS);
        tvTicketR = (TextView) findViewById(R.id.tvTicketR);
        tvTicketS = (TextView) findViewById(R.id.tvTicketS);
        tvTimeR = (TextView) findViewById(R.id.tvTimeR);
        tvTimeS = (TextView) findViewById(R.id.tvTimeS);
        tvMonthR = (TextView) findViewById(R.id.tvMonthR);
        tvMonthS = (TextView) findViewById(R.id.tvMonthS);
        tvVenueR = (TextView) findViewById(R.id.tvVenueR);
        tvVenueS = (TextView) findViewById(R.id.tvVenueS);
        tvLocationR = (TextView) findViewById(R.id.tvLocationR);
        tvLocationS = (TextView) findViewById(R.id.tvLocationS);
        tvNameR = (TextView) findViewById(R.id.tvNameR);
        tvNameS = (TextView) findViewById(R.id.tvNameS);
        tvMerchantNameR = (TextView) findViewById(R.id.tvMerchantNameR);
        tvMerchantNameS = (TextView) findViewById(R.id.tvMerchantNameS);
        tvPriceR = (TextView) findViewById(R.id.tvPriceR);
        tvPriceS = (TextView) findViewById(R.id.tvPriceS);

        typecastEditTexts();
        insertStaticTextValues();

        viewRefresh.setOnClickListener(this);

        if (from.equals("")) {
            etName1.setText(prefs.getString(AppInfo.EVENT_TICKET_NAME_1, ""));
            etName2.setText(prefs.getString(AppInfo.EVENT_TICKET_NAME_2, ""));
            if (prefs.getInt(AppInfo.EVENT_TICKET_PRICE_1, 0) != 0)
                etPrice1.setText("" + "" + prefs.getInt(AppInfo.EVENT_TICKET_PRICE_1, 0));
            if (prefs.getInt(AppInfo.EVENT_TICKET_PRICE_2, 0) != 0)
                etPrice2.setText("" + "" + prefs.getInt(AppInfo.EVENT_TICKET_PRICE_2, 0));
            if (prefs.getInt(AppInfo.EVENT_TICKET_SEATS_1, 0) != 0)
                etSeats1.setText("" + "" + prefs.getInt(AppInfo.EVENT_TICKET_SEATS_1, 0));
            if (prefs.getInt(AppInfo.EVENT_TICKET_SEATS_2, 0) != 0)
                etSeats2.setText("" + "" + prefs.getInt(AppInfo.EVENT_TICKET_SEATS_2, 0));
            tvTimeR.setText(prefs.getString(AppInfo.EVENT_START_TIME, ""));
            tvTimeS.setText(prefs.getString(AppInfo.EVENT_START_TIME, ""));
            tvMonthR.setText(prefs.getString(AppInfo.EVENT_START_DATE, ""));
            tvMonthS.setText(prefs.getString(AppInfo.EVENT_START_DATE, ""));
            tvPriceR.setText("" + "$" + prefs.getInt(AppInfo.EVENT_TICKET_PRICE_1, 0));
            tvPriceS.setText("" + "$" + prefs.getInt(AppInfo.EVENT_TICKET_PRICE_2, 0));
            if (prefs.getInt(AppInfo.FREE_TICKETS, 0) != 0)
                etFreeTickets.setText("" + "" + prefs.getInt(AppInfo.FREE_TICKETS, 0));
        } else {
            etName1.setText(prefs.getString(AppInfo.EVENT_TICKET_NAME_1_EX, ""));
            etName2.setText(prefs.getString(AppInfo.EVENT_TICKET_NAME_2_EX, ""));
            if (prefs.getInt(AppInfo.EVENT_TICKET_PRICE_1_EX, 0) != 0)
                etPrice1.setText("" + "" + prefs.getInt(AppInfo.EVENT_TICKET_PRICE_1_EX, 0));
            if (prefs.getInt(AppInfo.EVENT_TICKET_PRICE_2_EX, 0) != 0)
                etPrice2.setText("" + "" + prefs.getInt(AppInfo.EVENT_TICKET_PRICE_2_EX, 0));
            if (prefs.getInt(AppInfo.EVENT_TICKET_SEATS_1_EX, 0) != 0)
                etSeats1.setText("" + "" + prefs.getInt(AppInfo.EVENT_TICKET_SEATS_1_EX, 0));
            if (prefs.getInt(AppInfo.EVENT_TICKET_SEATS_2_EX, 0) != 0)
                etSeats2.setText("" + "" + prefs.getInt(AppInfo.EVENT_TICKET_SEATS_2_EX, 0));
            tvTimeR.setText(prefs.getString(AppInfo.EVENT_START_TIME_EX, ""));
            tvTimeS.setText(prefs.getString(AppInfo.EVENT_START_TIME_EX, ""));
            tvMonthR.setText(prefs.getString(AppInfo.EVENT_START_DATE_EX, ""));
            tvMonthS.setText(prefs.getString(AppInfo.EVENT_START_DATE_EX, ""));
            tvPriceR.setText("" + "$" + prefs.getInt(AppInfo.EVENT_TICKET_PRICE_1_EX, 0));
            tvPriceS.setText("" + "$" + prefs.getInt(AppInfo.EVENT_TICKET_PRICE_2_EX, 0));
            if (prefs.getInt(AppInfo.FREE_TICKETS_EX, 0) != 0)
                etFreeTickets.setText("" + "" + prefs.getInt(AppInfo.FREE_TICKETS_EX, 0));
        }


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Sell Tickets";
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
        checkAndInsertTexts();
    }



    private void typecastEditTexts() {
        for (int x = 0; x < editTexts.length; x++)
            editTexts[x] = (EditText) findViewById(editTextIds[x]);
        etName1 = editTexts[0]; etName2 = editTexts[1]; etPrice1 = editTexts[2];
        etPrice2 = editTexts[3]; etSeats1 = editTexts[4]; etSeats2 = editTexts[5];
    }

    private void insertStaticTextValues() {
        TextView[] views = {tvVenueR, tvVenueS, tvLocationR, tvLocationS, tvNameR, tvNameS, tvMerchantNameR, tvMerchantNameS};

        String[] values;
        if (from.equals(""))
            values = new String[]{prefs.getString(AppInfo.EVENT_VENUE, ""), prefs.getString(AppInfo.EVENT_VENUE, ""),
                    prefs.getString(AppInfo.EVENT_LOCATION, ""), prefs.getString(AppInfo.EVENT_LOCATION, ""),
                    prefs.getString(AppInfo.EVENT_NAME, ""), prefs.getString(AppInfo.EVENT_NAME, ""),
                    prefs.getString(AppInfo.BUSINESS_ECOCASH_NAME, ""), prefs.getString(AppInfo.BUSINESS_ECOCASH_NAME, "")};
        else
            values = new String[]{prefs.getString(AppInfo.EVENT_VENUE_EX, ""), prefs.getString(AppInfo.EVENT_VENUE_EX, ""),
                    prefs.getString(AppInfo.EVENT_LOCATION_EX, ""), prefs.getString(AppInfo.EVENT_LOCATION_EX, ""),
                    prefs.getString(AppInfo.EVENT_NAME_EX, ""), prefs.getString(AppInfo.EVENT_NAME_EX, ""),
                    prefs.getString(AppInfo.BUSINESS_ECOCASH_NAME, ""), prefs.getString(AppInfo.BUSINESS_ECOCASH_NAME, "")};


        for (int x = 0; x < views.length; x++)
            views[x].setText(values[x]);
    }


    private void checkAndInsertTexts() {
        tvCategoryR.setText(etName1.getText().toString());
        tvCategoryS.setText(etName2.getText().toString());
        tvPriceR.setText("$" + etPrice1.getText().toString() + "");
        tvPriceS.setText("$" + etPrice2.getText().toString() + "");
    }


    @Override
    protected void onPause() {
        super.onPause();
        finish();
        saveValues();
    }

    private void saveValues() {
        editor = prefs.edit();
        if (from.equals("")) {
            editor.putString(AppInfo.EVENT_TICKET_NAME_1, etName1.getText().toString());
            editor.putString(AppInfo.EVENT_TICKET_NAME_2, etName2.getText().toString());
            if (!etPrice1.getText().toString().equals(""))
                editor.putInt(AppInfo.EVENT_TICKET_PRICE_1, Integer.parseInt(etPrice1.getText().toString()));
            else
                editor.putInt(AppInfo.EVENT_TICKET_PRICE_1, 0);
            if (!etPrice2.getText().toString().equals(""))
                editor.putInt(AppInfo.EVENT_TICKET_PRICE_2, Integer.parseInt(etPrice2.getText().toString()));
            else
                editor.putInt(AppInfo.EVENT_TICKET_PRICE_2, 0);
            if (!etSeats1.getText().toString().equals(""))
                editor.putInt(AppInfo.EVENT_TICKET_SEATS_1, Integer.parseInt(etSeats1.getText().toString()));
            else
                editor.putInt(AppInfo.EVENT_TICKET_SEATS_1, 0);
            if (!etSeats2.getText().toString().equals(""))
                editor.putInt(AppInfo.EVENT_TICKET_SEATS_2, Integer.parseInt(etSeats2.getText().toString()));
            else
                editor.putInt(AppInfo.EVENT_TICKET_SEATS_2, 0);
            if (!etFreeTickets.getText().toString().equals(""))
                editor.putInt(AppInfo.FREE_TICKETS, Integer.parseInt(etFreeTickets.getText().toString()));
            else
                editor.putInt(AppInfo.FREE_TICKETS, 0);
        } else {
            editor.putString(AppInfo.EVENT_TICKET_NAME_1_EX, etName1.getText().toString());
            editor.putString(AppInfo.EVENT_TICKET_NAME_2_EX, etName2.getText().toString());
            if (!etPrice1.getText().toString().equals(""))
                editor.putInt(AppInfo.EVENT_TICKET_PRICE_1_EX, Integer.parseInt(etPrice1.getText().toString()));
            else
                editor.putInt(AppInfo.EVENT_TICKET_PRICE_1_EX, 0);
            if (!etPrice2.getText().toString().equals(""))
                editor.putInt(AppInfo.EVENT_TICKET_PRICE_2_EX, Integer.parseInt(etPrice2.getText().toString()));
            else
                editor.putInt(AppInfo.EVENT_TICKET_PRICE_2_EX, 0);
            if (!etSeats1.getText().toString().equals(""))
                editor.putInt(AppInfo.EVENT_TICKET_SEATS_1_EX, Integer.parseInt(etSeats1.getText().toString()));
            else
                editor.putInt(AppInfo.EVENT_TICKET_SEATS_1_EX, 0);
            if (!etSeats2.getText().toString().equals(""))
                editor.putInt(AppInfo.EVENT_TICKET_SEATS_2_EX, Integer.parseInt(etSeats2.getText().toString()));
            else
                editor.putInt(AppInfo.EVENT_TICKET_SEATS_2_EX, 0);
            if (!etFreeTickets.getText().toString().equals(""))
                editor.putInt(AppInfo.FREE_TICKETS_EX, Integer.parseInt(etFreeTickets.getText().toString()));
            else
                editor.putInt(AppInfo.FREE_TICKETS_EX, 0);
        }
        editor.apply();
    }


}
