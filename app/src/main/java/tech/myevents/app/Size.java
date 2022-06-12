package tech.myevents.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class Size extends AppCompatActivity {

    TextView tv3, tv10, tv30, tv50, tv51;
    ImageView iv3, iv10, iv30, iv50, iv51;

    SharedPreferences prefs;
    SharedPreferences prefsApp;
    String type;
    SharedPreferences.Editor editor;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_size);
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

        tv3 = (TextView) findViewById(R.id.tv1);
        tv10 = (TextView) findViewById(R.id.tv2);
        tv30 = (TextView) findViewById(R.id.tv3);
        tv50 = (TextView) findViewById(R.id.tv4);
        tv51 = (TextView) findViewById(R.id.tv5);

        iv3 = (ImageView) findViewById(R.id.iv3);
        iv10 = (ImageView) findViewById(R.id.iv10);
        iv30 = (ImageView) findViewById(R.id.iv30);
        iv50 = (ImageView) findViewById(R.id.iv50);
        iv51 = (ImageView) findViewById(R.id.iv51);


        changeBusinessSize(prefs.getString(AppInfo.BUSINESS_SIZE, ""));

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Business Size";
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

    public void changeSize(View view) {
        switch (view.getId()) {
            case R.id.tv1:
                changeBusinessSize(StaticVariables.businessSize[0]);
                break;
            case R.id.tv2:
                changeBusinessSize(StaticVariables.businessSize[1]);
                break;
            case R.id.tv3:
                changeBusinessSize(StaticVariables.businessSize[2]);
                break;
            case R.id.tv4:
                changeBusinessSize(StaticVariables.businessSize[3]);
                break;
            case R.id.tv5:
                changeBusinessSize(StaticVariables.businessSize[4]);
                break;
        }
        finish();
    }

    private void changeBusinessSize(String size) {
        editor = prefs.edit();
        editor.putString(AppInfo.BUSINESS_SIZE, size);
        editor.apply();
        ImageView[] imageViews = {iv3, iv10, iv30, iv50, iv51};
        for (int x = 0; x < StaticVariables.businessSize.length; x++) {
            if (StaticVariables.businessSize[x].equals(size))
                imageViews[x].setVisibility(View.VISIBLE);
            else
                imageViews[x].setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
