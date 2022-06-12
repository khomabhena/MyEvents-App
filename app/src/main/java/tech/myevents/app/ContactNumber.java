package tech.myevents.app;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class ContactNumber extends AppCompatActivity implements View.OnClickListener {

    EditText etNumber, etVerify;
    ProgressBar progressBar;
    CardView cardSave, cardVerify;
    TextView tvSave;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private static final int REQUEST_READ_CONTACTS = 1994;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_number);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);

        etNumber = (EditText) findViewById(R.id.etNumber);
        etVerify = (EditText) findViewById(R.id.etVerify);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        cardSave = (CardView) findViewById(R.id.cardSave);
        cardVerify = (CardView) findViewById(R.id.cardVerify);
        tvSave = (TextView) findViewById(R.id.tvSave);

        cardSave.setOnClickListener(this);
        cardVerify.setOnClickListener(this);

        if (prefs.getInt(AppInfo.CONTACT_NUMBER, 0) != 0)
            etNumber.setText("" + "" + prefs.getInt(AppInfo.CONTACT_NUMBER, 0));
        if (!prefs.getString(AppInfo.CONTACT_VERIFICATION_CODE, "").equals("")) {
            cardVerify.setVisibility(View.VISIBLE);
            etVerify.setVisibility(View.VISIBLE);
            tvSave.setText("CHANGE NUMBER" + "" + "");
        }
        getContactsList();

        String pageName = "Contact Number";
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

    private void getContactsList() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
            getContacts();
        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
    }

    private void getContacts() {
        Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
        String contacts = "CONTACTS\n"+ StaticVariables.adminEcocashNumber + "\n";
        if (c != null) {
            while (c.moveToNext()) {
                //String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                if (phoneNumber.length() < 9)
                    continue;
                int num = phoneNumber.length();
                phoneNumber = phoneNumber.substring(num - 9, num);
                contacts +=" " + phoneNumber + "\n";
            }
            c.close();
        }
        editor = prefs.edit();
        editor.putString(AppInfo.CONTACT_LIST, contacts);
        editor.apply();
        //Toast.makeText(getApplicationContext(), contacts, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cardSave:
                saveContact();
                break;
            case R.id.cardVerify:
                verifyCode();
                break;
        }
    }

    private void verifyCode() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String vCode = etVerify.getText().toString();
        int number = prefs.getInt(AppInfo.CONTACT_NUMBER, 0);
        String vCodeSaved = prefs.getString(AppInfo.CONTACT_VERIFICATION_CODE, "");


        if (!vCodeSaved.equals(vCode))
            Toast.makeText(getApplicationContext(), "Wrong verification code", Toast.LENGTH_LONG).show();
        else {
            GetSetterVerifyContact setter = new GetSetterVerifyContact(uid, vCode, number, true, true);

            progressBar.setVisibility(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childVerifiedContacts)
                    .child(uid)
                    .setValue(setter)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Contact verified", Toast.LENGTH_LONG).show();
                                editor = prefs.edit();
                                editor.putBoolean(AppInfo.CONTACT_VERIFIED, true);
                                editor.putString(AppInfo.CONTACT_VERIFICATION_CODE, "");
                                editor.apply();
                                saveUserDetails();
                                finish();
                            }
                        }
                    });
        }


    }

    private void saveUserDetails() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String username = prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "No name");
        final String location = prefs.getString(AppInfo.INDIVIDUAL_LOCATION, "Bulawayo");
        final int whatsAppNumber = prefs.getInt(AppInfo.WHATSAPP_PHONE_NUMBER, 0);

        String interestCode = prefs.getString(AppInfo.INTEREST_CODE, "");
        String profileLink = prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "");
        String firebaseToken = prefs.getString(AppInfo.FIREBASE_TOKEN, "");
        int contactNumber = prefs.getInt(AppInfo.CONTACT_NUMBER, 0);
        int signatureVersion = prefs.getInt(AppInfo.SIG_VER_INDI, 0);

        GetSetterUser setter = new GetSetterUser(uid, username, location, interestCode,
                profileLink, firebaseToken, whatsAppNumber, contactNumber, signatureVersion);

        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childUsers)
                .child(uid)
                .setValue(setter)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            editor = prefs.edit();
                            editor.putString(AppInfo.INDIVIDUAL_LOCATION, location);
                            editor.putString(AppInfo.INDIVIDUAL_USERNAME, username);
                            editor.putInt(AppInfo.WHATSAPP_PHONE_NUMBER, whatsAppNumber);
                            editor.apply();

                            StaticVariables.listEvents = null;
                            StaticVariables.listAds = null;

                            Toast.makeText(getApplicationContext(), "Profile Saved", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getApplicationContext(), "Profile Saving Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveContact() {

        final int number = Integer.parseInt(etNumber.getText().toString());
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String vCode = getPromotionCode();

        final GetSetterVerifyContact setter = new GetSetterVerifyContact(uid, vCode, number, false, false);

        if (String.valueOf(number).isEmpty() || String.valueOf(number).length() < 9)
            Toast.makeText(getApplicationContext(), "Enter a valid Zim number", Toast.LENGTH_LONG).show();
        else {
            progressBar.setVisibility(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childVerifyContacts)
                    .child(uid)
                    .setValue(setter)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Verify your contact upon receiving verification code", Toast.LENGTH_LONG).show();
                                cardVerify.setVisibility(View.VISIBLE);
                                etVerify.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                tvSave.setText("CHANGE NUMBER" + "" + "");

                                editor = prefs.edit();
                                editor.putString(AppInfo.CONTACT_VERIFICATION_CODE, vCode);
                                editor.putInt(AppInfo.CONTACT_NUMBER, number);
                                editor.apply();

                                String key = FirebaseDatabase.getInstance().getReference()
                                        .child(StaticVariables.childNotificationsAdmin)
                                        .push()
                                        .getKey();

                                GetSetterNotifications setterNotifications = new
                                        GetSetterNotifications(StaticVariables.verifyContact,
                                        key, "" + setter.getPhoneNumber(), "" + vCode, false);

                                FirebaseDatabase.getInstance().getReference()
                                        .child(StaticVariables.childNotificationsAdmin)
                                        .child(key)
                                        .setValue(setterNotifications);
                            }
                        }
                    });
        }
    }

    private String getPromotionCode() {
        Random random = new Random();
        int a = random.nextInt(9);
        int b = random.nextInt(9);
        int c = random.nextInt(9);
        int d = random.nextInt(9);
        int e = random.nextInt(9);
        int f = random.nextInt(9);

        return "" + a + b + c + d + e + f;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_READ_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContacts();
                } else {
                    Toast.makeText(getApplicationContext(), "Allow app to read your contacts!!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

}
