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

public class BusinessCategory extends AppCompatActivity {

    TextView tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv10, tv11, tv12, tv13, tv14, tv15, tv16, tv17;
    ImageView iv1, iv2, iv3, iv4, iv5, iv6, iv7, iv8, iv9, iv10, iv11, iv12, iv13, iv14, iv15, iv16, iv17;

    SharedPreferences prefs;
    SharedPreferences prefsApp;
    String type;
    SharedPreferences.Editor editor;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_category);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        if (type.equals(StaticVariables.business)) {
            Window w = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.appStatusBar));
            toolbar.setBackgroundColor(getResources().getColor(R.color.appBarMainBus));
        }

        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv3 = (TextView) findViewById(R.id.tv3);
        tv4 = (TextView) findViewById(R.id.tv4);
        tv5 = (TextView) findViewById(R.id.tv5);
        tv6 = (TextView) findViewById(R.id.tv6);
        tv7 = (TextView) findViewById(R.id.tv7);
        tv8 = (TextView) findViewById(R.id.tv8);
        tv9 = (TextView) findViewById(R.id.tv9);
        tv10 = (TextView) findViewById(R.id.tv10);
        tv11 = (TextView) findViewById(R.id.tv11);
        tv12 = (TextView) findViewById(R.id.tv12);
        tv13 = (TextView) findViewById(R.id.tv13);
        tv14 = (TextView) findViewById(R.id.tv14);
        tv15 = (TextView) findViewById(R.id.tv15);
        tv16 = (TextView) findViewById(R.id.tv16);
        tv17 = (TextView) findViewById(R.id.tv17);

        iv1 = (ImageView) findViewById(R.id.iv1);
        iv2 = (ImageView) findViewById(R.id.iv2);
        iv3 = (ImageView) findViewById(R.id.iv3);
        iv4 = (ImageView) findViewById(R.id.iv4);
        iv5 = (ImageView) findViewById(R.id.iv5);
        iv6 = (ImageView) findViewById(R.id.iv6);
        iv7 = (ImageView) findViewById(R.id.iv7);
        iv8 = (ImageView) findViewById(R.id.iv8);
        iv9 = (ImageView) findViewById(R.id.iv9);
        iv10 = (ImageView) findViewById(R.id.iv10);
        iv11 = (ImageView) findViewById(R.id.iv11);
        iv12 = (ImageView) findViewById(R.id.iv12);
        iv13 = (ImageView) findViewById(R.id.iv13);
        iv14 = (ImageView) findViewById(R.id.iv14);
        iv15 = (ImageView) findViewById(R.id.iv15);
        iv16 = (ImageView) findViewById(R.id.iv16);
        iv17 = (ImageView) findViewById(R.id.iv17);


        changeBusinessSize(prefs.getString(AppInfo.BUSINESS_CATEGORY, ""));

        String pageName = "Business Category";
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
                changeBusinessSize(StaticVariables.categories[0]);
                break;
            case R.id.tv2:
                changeBusinessSize(StaticVariables.categories[1]);
                break;
            case R.id.tv3:
                changeBusinessSize(StaticVariables.categories[2]);
                break;
            case R.id.tv4:
                changeBusinessSize(StaticVariables.categories[3]);
                break;
            case R.id.tv5:
                changeBusinessSize(StaticVariables.categories[4]);
                break;
            case R.id.tv6:
                changeBusinessSize(StaticVariables.categories[5]);
                break;
            case R.id.tv7:
                changeBusinessSize(StaticVariables.categories[6]);
                break;
            case R.id.tv8:
                changeBusinessSize(StaticVariables.categories[7]);
                break;
            case R.id.tv9:
                changeBusinessSize(StaticVariables.categories[8]);
                break;
            case R.id.tv10:
                changeBusinessSize(StaticVariables.categories[9]);
                break;
            case R.id.tv11:
                changeBusinessSize(StaticVariables.categories[10]);
                break;
            case R.id.tv12:
                changeBusinessSize(StaticVariables.categories[11]);
                break;
            case R.id.tv13:
                changeBusinessSize(StaticVariables.categories[12]);
                break;
            case R.id.tv14:
                changeBusinessSize(StaticVariables.categories[13]);
                break;
            case R.id.tv15:
                changeBusinessSize(StaticVariables.categories[14]);
                break;
            case R.id.tv16:
                changeBusinessSize(StaticVariables.categories[15]);
                break;
            case R.id.tv17:
                changeBusinessSize(StaticVariables.categories[16]);
                break;
        }
        finish();
    }

    private void changeBusinessSize(String interest) {
        editor = prefs.edit();
        ImageView[] imageViews = {iv1, iv2, iv3, iv4, iv5, iv6, iv7, iv8, iv9, iv10, iv11, iv12, iv13, iv14, iv15, iv16, iv17};
        for (int x = 0; x < StaticVariables.categories.length; x++) {
            if (StaticVariables.categories[x].equals(interest)) {
                imageViews[x].setVisibility(View.VISIBLE);
                editor.putString(AppInfo.BUSINESS_CATEGORY, StaticVariables.categories[x]);
            } else
                imageViews[x].setVisibility(View.GONE);
        }
        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

}
