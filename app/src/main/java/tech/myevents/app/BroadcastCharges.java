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

public class BroadcastCharges extends AppCompatActivity implements View.OnClickListener {

    View viewSend;
    EditText etNumber, etBroadcastCity, etBroadcastNat, etLess50, etLess100, etBroadcast0, etBroadcast1, etBroadcast2, etBroadcast3,
            etSize3, etSize10, etSize30, etSize50, etSizeAbove50, etDuration2, etDuration4, etDuration7, etDuration10;
    ProgressBar progressBar;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_charges);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);

        viewSend = findViewById(R.id.viewSend);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        etNumber = (EditText) findViewById(R.id.etNumber);
        etBroadcastCity = (EditText) findViewById(R.id.etBroadcastCity);
        etBroadcastNat = (EditText) findViewById(R.id.etBroadcastNat);
        etLess50 = (EditText) findViewById(R.id.etLess50);
        etLess100 = (EditText) findViewById(R.id.etLess100);
        etBroadcast0 = (EditText) findViewById(R.id.etBroadcast0);
        etBroadcast1 = (EditText) findViewById(R.id.etBroadcast1);
        etBroadcast2 = (EditText) findViewById(R.id.etBroadcast2);
        etBroadcast3 = (EditText) findViewById(R.id.etBroadcast3);
        etSize3 = (EditText) findViewById(R.id.etSize3);
        etSize10 = (EditText) findViewById(R.id.etSize10);
        etSize30 = (EditText) findViewById(R.id.etSize30);
        etSize50 = (EditText) findViewById(R.id.etSize50);
        etSizeAbove50 = (EditText) findViewById(R.id.etSizeAbove50);
        etDuration2 = (EditText) findViewById(R.id.etDuration2);
        etDuration4 = (EditText) findViewById(R.id.etDuration4);
        etDuration7 = (EditText) findViewById(R.id.etDuration7);
        etDuration10 = (EditText) findViewById(R.id.etDuration10);

        viewSend.setOnClickListener(this);

        if (prefs.getInt(AppInfo.AUD_RANGE_CITY, 0) != 0)
            etBroadcastCity.setText(String.valueOf(prefs.getInt(AppInfo.AUD_RANGE_CITY, 0)));
        if (prefs.getInt(AppInfo.AUD_RANGE_NATIONAL, 0) != 0)
            etBroadcastNat.setText(String.valueOf(prefs.getInt(AppInfo.AUD_RANGE_NATIONAL, 0)));
        if (prefs.getInt(AppInfo.AUD_50, 0) != 0)
            etLess50.setText(String.valueOf(prefs.getInt(AppInfo.AUD_50, 0)));
        if (prefs.getInt(AppInfo.AUD_100, 0) != 0)
            etLess100.setText(String.valueOf(prefs.getInt(AppInfo.AUD_100, 0)));
        if (prefs.getInt(AppInfo.AUD_BROAD_0, 0) != 0)
            etBroadcast0.setText(String.valueOf(prefs.getInt(AppInfo.AUD_BROAD_0, 0)));
        if (prefs.getInt(AppInfo.AUD_BROAD_1, 0) != 0)
            etBroadcast1.setText(String.valueOf(prefs.getInt(AppInfo.AUD_BROAD_1, 0)));
        if (prefs.getInt(AppInfo.AUD_BROAD_2, 0) != 0)
            etBroadcast2.setText(String.valueOf(prefs.getInt(AppInfo.AUD_BROAD_2, 0)));
        if (prefs.getInt(AppInfo.AUD_BROAD_3, 0) != 0)
            etBroadcast3.setText(String.valueOf(prefs.getInt(AppInfo.AUD_BROAD_3, 0)));
        if (prefs.getInt(AppInfo.AUD_SIZE_4, 0) != 0)
            etSize3.setText(String.valueOf(prefs.getInt(AppInfo.AUD_SIZE_4, 0)));
        if (prefs.getInt(AppInfo.AUD_SIZE_11, 0) != 0)
            etSize10.setText(String.valueOf(prefs.getInt(AppInfo.AUD_SIZE_11, 0)));
        if (prefs.getInt(AppInfo.AUD_SIZE_31, 0) != 0)
            etSize30.setText(String.valueOf(prefs.getInt(AppInfo.AUD_SIZE_31, 0)));
        if (prefs.getInt(AppInfo.AUD_SIZE_50, 0) != 0)
            etSize50.setText(String.valueOf(prefs.getInt(AppInfo.AUD_SIZE_50, 0)));
        if (prefs.getInt(AppInfo.AUD_SIZE_ABOVE_50, 0) != 0)
            etSizeAbove50.setText(String.valueOf(prefs.getInt(AppInfo.AUD_SIZE_ABOVE_50, 0)));
        if (prefs.getInt(AppInfo.AUD_ECOCASH_NUMBER, 0) != 0)
            etNumber.setText(String.valueOf(prefs.getInt(AppInfo.AUD_ECOCASH_NUMBER, 0)));
        if (prefs.getInt(AppInfo.AUD_2_D, 0) != 0)
            etDuration2.setText(String.valueOf(prefs.getInt(AppInfo.AUD_2_D, 0)));
        if (prefs.getInt(AppInfo.AUD_4_D, 0) != 0)
            etDuration4.setText(String.valueOf(prefs.getInt(AppInfo.AUD_4_D, 0)));
        if (prefs.getInt(AppInfo.AUD_7_D, 0) != 0)
            etDuration7.setText(String.valueOf(prefs.getInt(AppInfo.AUD_7_D, 0)));
        if (prefs.getInt(AppInfo.AUD_10_D, 0) != 0)
            etDuration10.setText(String.valueOf(prefs.getInt(AppInfo.AUD_10_D, 0)));

        insertValues();


        String pageName = "Broadcast Charges";
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

    private void insertValues() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childCharges)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        GetSetterBroadcastCharges setter = dataSnapshot.getValue(GetSetterBroadcastCharges.class);
                        etNumber.setText("" + setter.getEcocashNumber());
                        etBroadcastCity.setText("" + ""  + setter.getBroadcastCity());
                        etBroadcastNat.setText("" + ""  + setter.getBroadcastNat());
                        etLess50.setText("" + ""  + setter.getAudienceLess50());
                        etLess100.setText("" + ""  + setter.getAudienceLess100());
                        etBroadcast0.setText("" + ""  + setter.getBroadcasts0());
                        etBroadcast1.setText("" + ""  + setter.getBroadcasts1());
                        etBroadcast2.setText("" + ""  + setter.getBroadcasts2());
                        etBroadcast3.setText("" + ""  + setter.getBroadcasts3());
                        etSize3.setText("" + ""  + setter.getSize3());
                        etSize10.setText("" + ""  + setter.getSize10());
                        etSize30.setText("" + ""  + setter.getSize30());
                        etSize50.setText("" + ""  + setter.getSize50());
                        etSizeAbove50.setText("" + ""  + setter.getSizeAbove50());
                        etDuration2.setText("" + ""  + setter.getDuration2());
                        etDuration4.setText("" + ""  + setter.getDuration4());
                        etDuration7.setText("" + ""  + setter.getDuration7());
                        etDuration10.setText("" + ""  + setter.getDuration10());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        int number = Integer.parseInt(etNumber.getText().toString());
        int broadcastCity = Integer.parseInt(etBroadcastCity.getText().toString());
        int broadcastNat = Integer.parseInt(etBroadcastNat.getText().toString());
        int  audienceLess50 = Integer.parseInt(etLess50.getText().toString());
        int audienceLess100 = Integer.parseInt(etLess100.getText().toString());
        int broadcasts0 = Integer.parseInt(etBroadcast0.getText().toString());
        int broadcasts1 = Integer.parseInt(etBroadcast1.getText().toString());
        int broadcasts2 = Integer.parseInt(etBroadcast2.getText().toString());
        int broadcasts3 = Integer.parseInt(etBroadcast3.getText().toString());
        int size3 = Integer.parseInt(etSize3.getText().toString());
        int size10 = Integer.parseInt(etSize10.getText().toString());
        int size30 = Integer.parseInt(etSize30.getText().toString());
        int size50 = Integer.parseInt(etSize50.getText().toString());
        int sizeAbove50 = Integer.parseInt(etSizeAbove50.getText().toString());
        int duration2 = Integer.parseInt(etDuration2.getText().toString());
        int duration4 = Integer.parseInt(etDuration4.getText().toString());
        int duration7 = Integer.parseInt(etDuration7.getText().toString());
        int duration10 = Integer.parseInt(etDuration10.getText().toString());

        GetSetterBroadcastCharges setter = new GetSetterBroadcastCharges(number, broadcastCity, broadcastNat, audienceLess50,
                audienceLess100, broadcasts0, broadcasts1, broadcasts2, broadcasts3, size3, size10, size30, size50,
                sizeAbove50, duration2, duration4, duration7, duration10);

        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childCharges)
                .setValue(setter)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Broadcast Charges Saved", Toast.LENGTH_SHORT).show();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Charges Saving Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor = prefs.edit();
        if (!etBroadcastCity.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_RANGE_CITY, Integer.parseInt(etBroadcastCity.getText().toString()));
        if (!etBroadcastNat.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_RANGE_NATIONAL, Integer.parseInt(etBroadcastNat.getText().toString()));
        if (!etLess50.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_50, Integer.parseInt(etLess50.getText().toString()));
        if (!etLess100.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_100, Integer.parseInt(etLess100.getText().toString()));
        if (!etBroadcast0.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_BROAD_0, Integer.parseInt(etBroadcast0.getText().toString()));
        if (!etBroadcast1.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_BROAD_1, Integer.parseInt(etBroadcast1.getText().toString()));
        if (!etBroadcast2.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_BROAD_2, Integer.parseInt(etBroadcast2.getText().toString()));
        if (!etBroadcast3.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_BROAD_3, Integer.parseInt(etBroadcast3.getText().toString()));
        if (!etSize3.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_SIZE_4, Integer.parseInt(etSize3.getText().toString()));
        if (!etSize10.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_SIZE_11, Integer.parseInt(etSize10.getText().toString()));
        if (!etSize30.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_SIZE_31, Integer.parseInt(etSize30.getText().toString()));
        if (!etSize50.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_SIZE_50, Integer.parseInt(etSize50.getText().toString()));
        if (!etSizeAbove50.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_SIZE_ABOVE_50, Integer.parseInt(etSizeAbove50.getText().toString()));
        if (!etNumber.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_ECOCASH_NUMBER, Integer.parseInt(etNumber.getText().toString()));
        if (!etDuration2.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_2_D, Integer.parseInt(etDuration2.getText().toString()));
        if (!etDuration4.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_4_D, Integer.parseInt(etDuration4.getText().toString()));
        if (!etDuration7.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_7_D, Integer.parseInt(etDuration7.getText().toString()));
        if (!etDuration10.getText().toString().equals(""))
            editor.putInt(AppInfo.AUD_10_D, Integer.parseInt(etDuration10.getText().toString()));

        editor.apply();
    }

}
