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
import com.google.firebase.database.FirebaseDatabase;

public class AppVersion extends AppCompatActivity implements View.OnClickListener {

    EditText etVersion, etIndividualPriority, etBusinessPriority;
    View viewSend;
    ProgressBar progressBar;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_version);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);

        etVersion = (EditText) findViewById(R.id.etVersion);
        etIndividualPriority = (EditText) findViewById(R.id.etIndividualPriority);
        etBusinessPriority = (EditText) findViewById(R.id.etBusinessPriority);
        viewSend = findViewById(R.id.viewSend);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        viewSend.setOnClickListener(this);

        etVersion.setText(String.valueOf(prefs.getInt(AppInfo.AUD_VERSION_CODE, 22)));
        etBusinessPriority.setText(prefs.getString(AppInfo.AUD_BUSINESS_PRIORITY, "low"));
        etIndividualPriority.setText(prefs.getString(AppInfo.AUD_INDIVIDUAL_PRIORITY, "low"));

        String pageName = "App Version";
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
        progressBar.setVisibility(View.VISIBLE);
        int version = Integer.parseInt(etVersion.getText().toString());
        String indiPri = etIndividualPriority.getText().toString();
        String busPri = etBusinessPriority.getText().toString();

        GetSetterAppVersion setter = new GetSetterAppVersion(version, indiPri, busPri);
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childAppVersion)
                .setValue(setter)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Version Code Saved", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Version Code saving failed", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor = prefs.edit();

        editor.putInt(AppInfo.AUD_VERSION_CODE, Integer.parseInt(etVersion.getText().toString()));
        editor.putString(AppInfo.AUD_BUSINESS_PRIORITY, etBusinessPriority.getText().toString());
        editor.putString(AppInfo.AUD_INDIVIDUAL_PRIORITY, etIndividualPriority.getText().toString());

        editor.apply();
    }

}
