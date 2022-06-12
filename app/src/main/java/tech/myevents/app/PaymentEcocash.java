package tech.myevents.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class PaymentEcocash extends AppCompatActivity implements View.OnClickListener {

    TextView tvNumber, tvMerchant, tvBiller;
    ImageView iv3, iv1, iv2;
    EditText etEcocashCode, etEcocashName;
    View viewNumber, viewMerchant, viewBiller;
    CardView cardSave;


    SharedPreferences prefs;
    SharedPreferences prefsApp;
    String type;
    SharedPreferences.Editor editor;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecocash_payments);
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

        tvMerchant = (TextView) findViewById(R.id.tv1);
        tvBiller = (TextView) findViewById(R.id.tv2);

        iv1 = (ImageView) findViewById(R.id.iv1);
        iv2 = (ImageView) findViewById(R.id.iv2);
        etEcocashCode = (EditText) findViewById(R.id.etEcocashCode);
        cardSave = (CardView) findViewById(R.id.cardSave);
        viewMerchant = (View) findViewById(R.id.viewMerchant);
        viewBiller = (View) findViewById(R.id.viewBiller);
        etEcocashName = (EditText) findViewById(R.id.etEcocashName);
        tvNumber = (TextView) findViewById(R.id.tvNumber);
        iv3 = (ImageView) findViewById(R.id.iv3);
        viewNumber = findViewById(R.id.viewNumber);

        viewMerchant.setOnClickListener(this);
        viewBiller.setOnClickListener(this);
        cardSave.setOnClickListener(this);
        viewNumber.setOnClickListener(this);

        if (prefs.getInt(AppInfo.BUSINESS_ECOCASH_CODE, 0) != 0)
            etEcocashCode.setText("" + prefs.getInt(AppInfo.BUSINESS_ECOCASH_CODE, 0) + "");
        etEcocashName.setText(prefs.getString(AppInfo.BUSINESS_ECOCASH_NAME, ""));
        changePaymentType(prefs.getString(AppInfo.BUSINESS_ECOCASH_TYPE, StaticVariables.paymentType[0]));

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Payment Ecocash";
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
            case R.id.viewNumber:
                changePaymentType(StaticVariables.paymentType[0]);
                break;
            case R.id.viewMerchant:
                changePaymentType(StaticVariables.paymentType[1]);
                break;
            case R.id.viewBiller:
                changePaymentType(StaticVariables.paymentType[2]);
                break;
            case R.id.cardSave:
                startActivity(new Intent(this, ProfileBusiness.class));
                break;
        }
    }

    private void changePaymentType(String type) {
        editor = prefs.edit();
        editor.putString(AppInfo.BUSINESS_ECOCASH_TYPE, type);
        editor.apply();

        ImageView[] imageViews = {iv3, iv1, iv2};

        for (int x = 0; x < StaticVariables.paymentType.length; x++) {
            if (StaticVariables.paymentType[x].equals(type))
                imageViews[x].setVisibility(View.VISIBLE);
            else
                imageViews[x].setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor = prefs.edit();
        if (etEcocashCode.getText().toString().equals(""))
            editor.putInt(AppInfo.BUSINESS_ECOCASH_CODE, 0);
        else
            editor.putInt(AppInfo.BUSINESS_ECOCASH_CODE, Integer.parseInt(etEcocashCode.getText().toString()));
        editor.putString(AppInfo.BUSINESS_ECOCASH_NAME, etEcocashName.getText().toString());
        editor.apply();
    }
}
