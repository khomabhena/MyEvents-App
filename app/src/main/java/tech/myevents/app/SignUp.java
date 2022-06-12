package tech.myevents.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Random;

import static android.support.v4.app.NotificationCompat.COLOR_DEFAULT;

public class SignUp extends AppCompatActivity implements View.OnClickListener {

    CardView cardSignUp;
    EditText etEmail, etPassword;
    Button bReset, bLogin;
    ImageView ivViewPass;

    ProgressBar progressBar;
    FirebaseAuth auth;

    SharedPreferences prefs;
    SharedPreferences prefsApp;
    SharedPreferences.Editor editor;
    DBOperations dbOperations;
    SQLiteDatabase db;

    String email = "", password = "";
    boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        dbOperations = new DBOperations(SignUp.this);
        db = dbOperations.getWritableDatabase();
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);

        try {
            email = getIntent().getExtras().getString("email");
            password = getIntent().getExtras().getString("password");
        } catch (Exception e) {}

        if (android.os.Build.VERSION.SDK_INT > 20) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        cardSignUp = (CardView) findViewById(R.id.cardSignUp);
        etEmail = (EditText) findViewById(R.id.email);
        etPassword = (EditText) findViewById(R.id.password);
        bReset = (Button) findViewById(R.id.bReset);
        bLogin = (Button) findViewById(R.id.bLogin);
        ivViewPass = (ImageView) findViewById(R.id.ivViewPass);

        etEmail.setText(email);
        etPassword.setText(password);

        Animation animBounce = AnimationUtils.loadAnimation(this, R.anim.left_roll);
        cardSignUp.startAnimation(animBounce);

        ivViewPass.setOnClickListener(this);
        cardSignUp.setOnClickListener(this);
        bReset.setOnClickListener(this);
        bLogin.setOnClickListener(this);

    }

    private void getWelcomeMessages() {
        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childWelcomeMessages)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        for (DataSnapshot snap: dataSnapshot.getChildren()) {
                            GetSetterInbox setter = snap.getValue(GetSetterInbox.class);

                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                            String sendTo = setter.getSendTo();
                            String sender = setter.getSender();
                            String receiver = setter.getReceiver();
                            String senderUid = setter.getSenderUid();
                            String receiverUid = setter.getReceiverUid();
                            boolean verified = setter.isSenderVerified();

                            int x = Integer.parseInt(setter.getKey());
                            if (x % 2 == 1) {
                                sendTo = uid;
                                receiver = email;
                                receiverUid = uid;
                            } else {
                                sender = email;
                                senderUid = uid;
                                verified = prefs.getBoolean(AppInfo.BUSINESS_VERIFIED, false);
                            }

                            String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
                            ContentValues values = new ContentValues();
                            values.put(DBContract.Messages.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
                            values.put(DBContract.Messages.SEND_TO, sendTo);
                            values.put(DBContract.Messages.CHAT_ROOM, setter.getChatRoom() + uid);
                            values.put(DBContract.Messages.KEY, setter.getKey());
                            values.put(DBContract.Messages.SENDER, sender);
                            values.put(DBContract.Messages.RECEIVER, receiver);
                            values.put(DBContract.Messages.SENDER_UID, senderUid);
                            values.put(DBContract.Messages.RECEIVER_UID, receiverUid);
                            values.put(DBContract.Messages.MESSAGE, setter.getMessage());
                            values.put(DBContract.Messages.SENDER_VERIFIED, verified ? "yes": "no");
                            values.put(DBContract.Messages.LINK_SENDER, setter.getLinkSender());
                            values.put(DBContract.Messages.LINK_RECEIVER,setter.getLinkReceiver());
                            values.put(DBContract.Messages.TIMESTAMP, String.valueOf(setter.getTimestamp()));
                            values.put(DBContract.Messages.SIGNATURE_VERSION, "0");
                            values.put(DBContract.Messages.ACC_TYPE, setter.getAccType());

                            db.insert(DBContract.Messages.TABLE_NAME, null, values);
                        }
                        editor = prefs.edit();
                        editor.putString(AppInfo.GET_WELCOME_MESSAGE, "yes");
                        editor.apply();
                        sendNotification();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivViewPass:
                if (passwordVisible) {
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordVisible = false;
                } else {
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordVisible = true;
                }
                break;
            case R.id.cardSignUp:
                signUp();
                break;
            case R.id.bReset:
                reset();
                break;
            case R.id.bLogin:
                Intent intent = new Intent(this, Login.class);
                intent.putExtra("email", etEmail.getText().toString());
                intent.putExtra("password", etPassword.getText().toString());
                startActivity(intent);
                finish();
                break;
        }
    }

    private void reset() {
        final String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter your registered email address!", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "We have sent you instructions to reset your password",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to send reset email", Toast.LENGTH_LONG).show();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void signUp() {
        final String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        //create user
        auth.signInAnonymously();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUp.this, "Sign up failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);
                            editor = prefs.edit();
                            editor.clear();
                            editor.apply();

                            if (prefsApp.getString(AppInfo.ACTIVE_USER, "").equals("")) {
                                editor = prefsApp.edit();
                                editor.putString(AppInfo.ACTIVE_USER, "active");
                                editor.apply();
                                startActivity(new Intent(SignUp.this, Profile.class));
                            } else
                                startActivity(new Intent(SignUp.this, MainActivity.class));

                            if (prefs.getString(AppInfo.GET_WELCOME_MESSAGE, "").equals(""))
                                getWelcomeMessages();

                            String key = FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childNotificationsAdmin)
                                    .push()
                                    .getKey();

                            GetSetterNotifications setterNotifications = new
                                    GetSetterNotifications(StaticVariables.signUp, key, "User Sign Up", email, false);

                            FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childNotificationsAdmin)
                                    .child(key)
                                    .setValue(setterNotifications);

                            finish();
                        }
                    }
                });
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

    private void sendNotification() {
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.setBigContentTitle("MyEvents App Message");
        bigText.bigText("Welcome to MyEvents App!! Thank you for installing.\n" +
                "Click to get started.");
        bigText.setSummaryText("By. Customer care.");

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setColor(COLOR_DEFAULT);
        notification.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_big));
        notification.setTicker("SignUp Notification");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("SignUp Notification");
        notification.setContentText("Message from MyEvents App.");
        notification.setAutoCancel(true);
        notification.setStyle(bigText);

        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
        intent1.putExtra("tabItem", 0);
        intent1.putExtra("currentPage", 12);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                (int) Calendar.getInstance().getTimeInMillis(), intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setContentIntent(pendingIntent);
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.notify(new Random().nextInt(9999), notification.build());
    }

}
