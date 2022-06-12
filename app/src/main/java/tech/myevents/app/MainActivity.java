package tech.myevents.app;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

import static android.support.v4.app.NotificationCompat.COLOR_DEFAULT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static int currentTabItem = 0;
    static int versionCode = 28;
    static int currentPage = 1;
    static boolean USER_NOTIFICATIONS = false;
    static boolean GET_SIGNATURE_VERSIONS = false;


    FloatingActionButton fabHome, fabSettings, fabAdmin, fabBroadcast, fabMessage;
    FloatingActionButton fabMoment, fabDecoy, fabFriends,  fabHomeBus, fabBroadcastBus, fabSettingsBus;
    FloatingActionButton fabHomeAd, fabBusiness, fabBroadcastAd;
    CardView cardImage;
    ImageView ivProfile;


    SharedPreferences prefs;
    SharedPreferences prefsApp;
    SharedPreferences.Editor editor;
    DBOperations dbOperations;
    SQLiteDatabase db;

    androidx.viewpager.widget.ViewPager viewPager;
    SectionsPagerAdapter sectionsPagerAdapter;
    private static final int REQUEST_READ_CONTACTS = 1994;
    AlertDialog.Builder builder;
    TabLayout tabLayout;
    AppBarLayout appbar;

    FirebaseAuth auth;
    Handler handler;
    Toolbar toolbar;
    int fabType = 0;
    int FLICKER_CALLS = 0;
    static int FLICK_TAB = 0;
    static boolean APP_SWITCH_ON = true;

    int[] appBarColor = {R.color.appBarMain, R.color.appBarMoment,
            R.color.appBarAd, R.color.appBarPlay};
    int[] appBarColorBus = {R.color.appBarMainBus, R.color.appBarMessages,
            R.color.appBarComments, R.color.appBarReviews};
    FloatingActionButton[] floats = {fabHome, fabMoment, fabHomeAd, null};

    String[] tabNames = {};
    int[] icons = {};
    int[] iconSelected = {};
    String type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbOperations = new DBOperations(this);
        db = dbOperations.getWritableDatabase();
        auth = FirebaseAuth.getInstance();
        handler = new Handler();
        APP_SWITCH_ON = true;

        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            if (prefsApp.getString(AppInfo.ACTIVE_USER, "").equals(""))
                startActivity(new Intent(MainActivity.this, SignUp.class));
            else
                startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            prefs = getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);

            try {
                currentTabItem = getIntent().getExtras().getInt("tabItem");
                currentPage = getIntent().getExtras().getInt("currentPage");
            } catch (Exception e) {/**/}

            fabBroadcast = (FloatingActionButton) findViewById(R.id.fabBroadcast);
            fabSettings = (FloatingActionButton) findViewById(R.id.fabSettings);
            fabHome = (FloatingActionButton) findViewById(R.id.fabHome);
            fabAdmin = (FloatingActionButton) findViewById(R.id.fabAdmin);
            fabMessage = (FloatingActionButton) findViewById(R.id.fabMessage);
            fabMoment = (FloatingActionButton) findViewById(R.id.fabMoment);
            fabBroadcastAd = (FloatingActionButton) findViewById(R.id.fabBroadcastAd);
            fabBusiness = (FloatingActionButton) findViewById(R.id.fabBusiness);
            fabHomeAd = (FloatingActionButton) findViewById(R.id.fabHomeAd);
            fabDecoy = (FloatingActionButton) findViewById(R.id.fabDecoy);
            fabFriends = (FloatingActionButton) findViewById(R.id.fabFriends);
            fabHomeBus = (FloatingActionButton) findViewById(R.id.fabHomeBus);
            fabSettingsBus = (FloatingActionButton) findViewById(R.id.fabSettingsBus);
            fabBroadcastBus = (FloatingActionButton) findViewById(R.id.fabBroadcastBus);

            floats[0] = fabHome;
            floats[1] = fabMoment;
            floats[2] = fabHomeAd;
            cardImage = (CardView) findViewById(R.id.cardImage);
            ivProfile = (ImageView) findViewById(R.id.ivProfile);

            startActivitiesAndListener();

            if (!USER_NOTIFICATIONS) {
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                if (email.equals(StaticVariables.adminEmail) || email.equals("colwanymab@gmail.com"))
                    FirebaseDatabase.getInstance().getReference()
                            .child(StaticVariables.childNotificationsAdmin)
                            .addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    if (!dataSnapshot.exists())
                                        return;
                                    USER_NOTIFICATIONS = true;
                                    GetSetterNotifications setterNot = dataSnapshot.getValue(GetSetterNotifications.class);
                                    if (!setterNot.isSeen()) {
                                        if (setterNot.getType().equals(StaticVariables.event)
                                                || setterNot.getType().equals(StaticVariables.eventExclusive)) {
                                            setterNot.setSeen(true);
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child(StaticVariables.childNotificationsAdmin)
                                                    .child(setterNot.getKey())
                                                    .setValue(setterNot);

                                            sendBroadNot(StaticVariables.event);
                                        } else if (setterNot.getType().equals(StaticVariables.ad)) {
                                            setterNot.setSeen(true);
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child(StaticVariables.childNotificationsAdmin)
                                                    .child(setterNot.getKey())
                                                    .setValue(setterNot);

                                            sendBroadNot(StaticVariables.ad);
                                        } else if (setterNot.getType().equals(StaticVariables.verifyContact)) {
                                            setterNot.setSeen(true);
                                            //ToDo ask permission upon phone upgrade.
                                            SmsManager smsManager = SmsManager.getDefault();
                                            smsManager.sendTextMessage(setterNot.getBroadcasterUid(), null,
                                                    "MyEvents App code: " + setterNot.getPostKey(), null, null);

                                            Toast.makeText(MainActivity.this, "Message sent.\nNumber: " + setterNot.getBroadcasterUid()
                                                    + "\nCode: " + setterNot.getPostKey(), Toast.LENGTH_SHORT).show();

                                            FirebaseDatabase.getInstance().getReference()
                                                    .child(StaticVariables.childNotificationsAdmin)
                                                    .child(setterNot.getKey())
                                                    .setValue(setterNot);

                                            sendBroadNot(StaticVariables.verifyContact);
                                        } else if (setterNot.getType().equals(StaticVariables.signUp)) {
                                            setterNot.setSeen(true);
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child(StaticVariables.childNotificationsAdmin)
                                                    .child(setterNot.getKey())
                                                    .setValue(setterNot);

                                            sendBroadNot(StaticVariables.signUp, setterNot);
                                        }
                                    }
                                }

                                private void sendBroadNot(String type, GetSetterNotifications setter) {
                                    Intent intent = new Intent(MainActivity.this, PublishEvent.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, (int) getCurrentTimestamp(),
                                            intent, 0);

                                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                    NotificationCompat.Builder builder = (NotificationCompat.Builder) new
                                            NotificationCompat.Builder(MainActivity.this)
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_big))
                                            .setContentTitle(setter.getBroadcasterUid()) // Login type
                                            .setContentText(setter.getPostKey()) // Login email
                                            .setContentIntent(pendingIntent)
                                            .setSound(alarmSound);

                                    NotificationManager notificationManager = (NotificationManager)
                                            getSystemService(Context.NOTIFICATION_SERVICE);
                                    notificationManager.notify(new Random().nextInt(787), builder.build());
                                }

                                private void sendBroadNot(String type) {
                                    if (type.equals(StaticVariables.event)) {
                                        Intent intent = new Intent(MainActivity.this, PublishEvent.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, (int) getCurrentTimestamp(),
                                                intent, 0);

                                        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                        NotificationCompat.Builder builder = (NotificationCompat.Builder) new
                                                NotificationCompat.Builder(MainActivity.this)
                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_big))
                                                .setContentTitle("New broadcast")
                                                .setContentText("New event broadcast")
                                                .setContentIntent(pendingIntent)
                                                .setSound(alarmSound);

                                        NotificationManager notificationManager = (NotificationManager)
                                                getSystemService(Context.NOTIFICATION_SERVICE);
                                        notificationManager.notify(new Random().nextInt(4547), builder.build());

                                    } else if (type.equals(StaticVariables.ad)) {

                                        Intent intent = new Intent(MainActivity.this, PublishAd.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,
                                                (int) getCurrentTimestamp(), intent, 0);

                                        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                        NotificationCompat.Builder builder = (NotificationCompat.Builder) new
                                                NotificationCompat.Builder(MainActivity.this)
                                                .setSmallIcon(R.drawable.notification_mail)
                                                .setContentTitle("New broadcast")
                                                .setContentText("New advert broadcast")
                                                .setContentIntent(pendingIntent)
                                                .setSound(alarmSound);

                                        NotificationManager notificationManager = (NotificationManager)
                                                getSystemService(Context.NOTIFICATION_SERVICE);
                                        notificationManager.notify(1232222, builder.build());
                                    } else if (type.equals(StaticVariables.verifyContact)) {

                                        Intent intent = new Intent(MainActivity.this, VerifyContacts.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this,
                                                (int) getCurrentTimestamp(), intent, 0);

                                        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                        NotificationCompat.Builder builder = (NotificationCompat.Builder) new
                                                NotificationCompat.Builder(MainActivity.this)
                                                .setSmallIcon(R.drawable.notification_mail)
                                                .setContentTitle("Verify contact.")
                                                .setContentText("New contact needs to be verified.")
                                                .setContentIntent(pendingIntent)
                                                .setSound(alarmSound);

                                        NotificationManager notificationManager = (NotificationManager)
                                                getSystemService(Context.NOTIFICATION_SERVICE);
                                        notificationManager.notify(12233442, builder.build());
                                    }
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                FirebaseDatabase.getInstance().getReference()
                        .child(StaticVariables.childNotificationsUser)
                        .child(uid)
                        .addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                if (!dataSnapshot.exists())
                                    return;
                                USER_NOTIFICATIONS = true;
                                final GetSetterNotifications setterNotifications = dataSnapshot.getValue(GetSetterNotifications.class);
                                if (setterNotifications.isSeen())
                                    return;
                                else {
                                    setterNotifications.setSeen(true);
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(StaticVariables.childNotificationsUser)
                                            .child(uid) //User uid
                                            .child(setterNotifications.getKey()) //Notification Key
                                            .setValue(setterNotifications);
                                }

                                if (setterNotifications.getType().equals(StaticVariables.message)) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(StaticVariables.childMessages)
                                            .child(setterNotifications.getBroadcasterUid()) //chatRoom
                                            .child(setterNotifications.getPostKey()) //messageKey
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    GetSetterInbox setterInbox = dataSnapshot.getValue(GetSetterInbox.class);
                                                    int x = checkMessageExistence(db, setterInbox.getKey());

                                                    if (x == 0)
                                                        insertMessage(setterInbox);

                                                    if (StaticVariables.listMessages != null)
                                                        for (int y = 0; y < StaticVariables.listMessages.size(); y++) {
                                                            GetSetterInbox inbox = (GetSetterInbox) StaticVariables.listMessages.get(y);
                                                            if (inbox.getChatRoom().equals(setterInbox.getChatRoom())) {
                                                                StaticVariables.listMessages.remove(y);
                                                                StaticVariables.listMessages.add(0, setterInbox);
                                                            }
                                                        }

                                                    if (Inbox.theChatRoom.equals(setterInbox.getChatRoom()))
                                                        if (StaticVariables.listInbox != null && StaticVariables.inboxAdapter != null) {
                                                            StaticVariables.inboxAdapter.add(setterInbox);
                                                            StaticVariables.inboxAdapter.notifyDataSetChanged();
                                                            Inbox.lvInbox.setSelection(Inbox.lvInbox.getAdapter().getCount() - 1);
                                                        }

                                                    if (StaticVariables.messagesAdapter != null)
                                                        StaticVariables.messagesAdapter.notifyDataSetChanged();

                                                    if (!Inbox.theChatRoom.equals(setterInbox.getChatRoom())
                                                            || (Inbox.theChatRoom.equals(setterInbox.getChatRoom())
                                                            && !Inbox.isInboxOn))
                                                        sendMessageNotification(setterInbox);
                                                }

                                                private void sendMessageNotification(GetSetterInbox setterInbox) {
                                                    Intent intent = new Intent(MainActivity.this, Messages.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, (int) getCurrentTimestamp(),
                                                            intent, 0);

                                                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                    NotificationCompat.Builder builder = (NotificationCompat.Builder) new
                                                            NotificationCompat.Builder(MainActivity.this)
                                                            .setSmallIcon(R.drawable.incoming_mail)
                                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_big))
                                                            .setContentTitle("Message from: " + setterInbox.getSender())
                                                            .setContentText(setterInbox.getMessage())
                                                            .setContentIntent(pendingIntent)
                                                            .setSound(alarmSound);
                                                    //.addAction(R.drawable.settings_mail, "Click to open", pendingIntent);
                                                    NotificationManager notificationManager = (NotificationManager)
                                                            getSystemService(Context.NOTIFICATION_SERVICE);
                                                    notificationManager.notify(1267764, builder.build());
                                                }

                                                private void insertMessage(GetSetterInbox setter) {
                                                    ContentValues values = new ContentValues();
                                                    values.put(DBContract.Messages.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
                                                    values.put(DBContract.Messages.SEND_TO, setter.getSendTo());
                                                    values.put(DBContract.Messages.CHAT_ROOM, setter.getChatRoom());
                                                    values.put(DBContract.Messages.KEY, setter.getKey());
                                                    values.put(DBContract.Messages.SENDER, setter.getSender());
                                                    values.put(DBContract.Messages.RECEIVER, setter.getReceiver());
                                                    values.put(DBContract.Messages.SENDER_UID, setter.getSenderUid());
                                                    values.put(DBContract.Messages.RECEIVER_UID, setter.getReceiverUid());
                                                    values.put(DBContract.Messages.MESSAGE, setter.getMessage());
                                                    values.put(DBContract.Messages.SENDER_VERIFIED, setter.isSenderVerified() ? "yes" : "no");
                                                    values.put(DBContract.Messages.RECEIVER_VERIFIED, setter.isReceiverVerified() ? "yes" : "no");
                                                    values.put(DBContract.Messages.LINK_SENDER, setter.getLinkSender());
                                                    values.put(DBContract.Messages.LINK_RECEIVER, setter.getLinkReceiver());
                                                    values.put(DBContract.Messages.TIMESTAMP, String.valueOf(setter.getTimestamp()));
                                                    values.put(DBContract.Messages.SIGNATURE_VERSION, "" + setter.getSignatureVersion());
                                                    values.put(DBContract.Messages.ACC_TYPE, setter.getAccType());

                                                    if (setter.getSendTo() != null && setter.getChatRoom() != null
                                                            && setter.getSenderUid() != null
                                                            && setter.getReceiverUid() != null
                                                            && setter.getMessage() != null)
                                                        db.insert(DBContract.Messages.TABLE_NAME, null, values);
                                                }

                                                private int checkMessageExistence(SQLiteDatabase db, String postKey) {
                                                    return dbOperations.checkMessageExistence(db, postKey);
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }
                                if (setterNotifications.getType().equals(StaticVariables.event)) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(StaticVariables.childEvents)
                                            .child(setterNotifications.getBroadcasterUid())
                                            .child(setterNotifications.getPostKey())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    GetSetterEvent setterEvent = dataSnapshot.getValue(GetSetterEvent.class);
                                                    if (setterEvent.getEventStatus().equals(StaticVariables.deleted))
                                                        if (setterEvent.getEventStatus().equals(StaticVariables.deleted)) {
                                                            insertEvent(setterEvent, StaticVariables.deleted);
                                                            return;
                                                        }

                                                    if (setterEvent.getEventStatus().equals(StaticVariables.eventExclusive))
                                                        return;

                                                    if (dbOperations.checkEventExistence(db, setterEvent.getEventKey()) == 0)
                                                        insertEvent(setterEvent, "");

                                                    if (StaticVariables.listEvents != null)
                                                        StaticVariables.listEvents.add(0, setterEvent);
                                                    if (StaticVariables.eventsAdapter != null)
                                                        StaticVariables.eventsAdapter.notifyDataSetChanged();

                                                    sendMessageNotification(setterEvent);
                                                    addImpression(setterEvent.getEventKey());
                                                }

                                                private void addImpression(String eventKey) {
                                                    String accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
                                                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                    String username = prefs.getString(AppInfo.BUSINESS_BRAND_NAME, prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "..."));
                                                    String profileLink = prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, ""));
                                                    String location = prefs.getString(AppInfo.BUSINESS_LOCATION, prefs.getString(AppInfo.INDIVIDUAL_LOCATION, "No location"));
                                                    int sigVer = accType.equals(StaticVariables.individual)
                                                            ? prefs.getInt(AppInfo.SIG_VER_INDI, 0) : prefs.getInt(AppInfo.SIG_VER_BUS, 0);

                                                    GetSetterImpression setter = new GetSetterImpression(accType, uid, eventKey, username, profileLink,
                                                            location, false, getCurrentTimestamp(), sigVer);

                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child(StaticVariables.childImpressions)
                                                            .child(eventKey)
                                                            .child(uid)
                                                            .setValue(setter);
                                                }

                                                private void sendMessageNotification(GetSetterEvent setterInbox) {
                                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                    intent.putExtra("tabItem", 0);
                                                    intent.putExtra("currentPage", 1);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, (int) getCurrentTimestamp(),
                                                            intent, 0);

                                                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                    NotificationCompat.Builder builder = (NotificationCompat.Builder) new
                                                            NotificationCompat.Builder(MainActivity.this)
                                                            .setSmallIcon(R.mipmap.ic_launcher)
                                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_big))
                                                            .setContentTitle("Event from: " + setterInbox.getBrandName())
                                                            .setContentText(setterInbox.getName())
                                                            .setContentIntent(pendingIntent)
                                                            .setSound(alarmSound);
                                                    //.addAction(R.drawable.settings_mail, "Click to open", pendingIntent);
                                                    NotificationManager notificationManager = (NotificationManager)
                                                            getSystemService(Context.NOTIFICATION_SERVICE);
                                                    notificationManager.notify(1258, builder.build());
                                                }

                                                private void insertEvent(GetSetterEvent setter, String type) {
                                                    ContentValues values = new ContentValues();
                                                    values.put(DBContract.Event.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
                                                    values.put(DBContract.Event.IMPRESSIONS, String.valueOf(setter.getImpressions()));
                                                    values.put(DBContract.Event.TICKET_SEATS_1, String.valueOf(setter.getTicketSeats1()));
                                                    values.put(DBContract.Event.TICKET_SEATS_2, String.valueOf(setter.getTicketSeats2()));
                                                    values.put(DBContract.Event.TICKET_PRICE_1, String.valueOf(setter.getTicketsPrice1()));
                                                    values.put(DBContract.Event.TICKET_PRICE_2, String.valueOf(setter.getTicketPrice2()));
                                                    values.put(DBContract.Event.MERCHANT_CODE, String.valueOf(setter.getMerchantCode()));
                                                    values.put(DBContract.Event.BROADCAST_TIME, String.valueOf(setter.getBroadcastTime()));
                                                    values.put(DBContract.Event.BROADCASTER_UID, setter.getBroadcasterUid());
                                                    values.put(DBContract.Event.KEY, setter.getEventKey());
                                                    values.put(DBContract.Event.INTEREST_CODE, setter.getInterestCode());
                                                    values.put(DBContract.Event.LOCATION_CODE, setter.getEventLocation());
                                                    values.put(DBContract.Event.EVENT_NAME, setter.getName());
                                                    values.put(DBContract.Event.EVENT_VENUE, setter.getVenue());
                                                    values.put(DBContract.Event.DETAILS, setter.getDetails());
                                                    values.put(DBContract.Event.PROFILE_LINK, setter.getProfileLink());
                                                    values.put(DBContract.Event.EVENT_STATUS, setter.getEventStatus());
                                                    values.put(DBContract.Event.START_DATE, setter.getStartDate());
                                                    values.put(DBContract.Event.START_TIME, setter.getStartTime());
                                                    values.put(DBContract.Event.TICKET_NAME_1, setter.getTicketName1());
                                                    values.put(DBContract.Event.TICKET_NAME_2, setter.getTicketName2());
                                                    values.put(DBContract.Event.MERCHANT_NAME, setter.getMerchantName());
                                                    values.put(DBContract.Event.START_TIMESTAMP, String.valueOf(setter.getStartTimestamp()));
                                                    values.put(DBContract.Event.END_TIMESTAMP, String.valueOf(setter.getEndTimestamp()));

                                                    values.put(DBContract.Event.TICKET_PROMO_CODE, setter.getTicketPromoCode());
                                                    values.put(DBContract.Event.AVAILABLE_TICKETS, String.valueOf(setter.getAvailableTickets()));
                                                    values.put(DBContract.Event.ECOCASH_TYPE, String.valueOf(setter.getEcocashType()));
                                                    values.put(DBContract.Event.UPDATE_TIME, String.valueOf(setter.getUpdateTime()));

                                                    values.put(DBContract.Event.BRAND_NAME, setter.getBrandName());
                                                    values.put(DBContract.Event.BRAND_LINK, setter.getBrandLink());
                                                    values.put(DBContract.Event.MXE_CODE, setter.getMxeCode());
                                                    values.put(DBContract.Event.VERIFIED, setter.isVerified() ? "yes" : "no");
                                                    values.put(DBContract.Event.SIGNATURE_VERSION, "" + setter.getSignatureVersion());

                                                    if (!type.equals(StaticVariables.deleted))
                                                        db.insert(DBContract.Event.TABLE_NAME, null, values);
                                                    else {
                                                        db.update(DBContract.Event.TABLE_NAME, values,
                                                                DBContract.Event.KEY + "='" + setter.getEventKey() + "'",
                                                                null);

                                                        if (StaticVariables.listEvents != null && StaticVariables.eventsAdapter != null)
                                                            for (int x = 0; x < StaticVariables.listEvents.size(); x++) {
                                                                GetSetterEvent setterEvent = (GetSetterEvent) StaticVariables.listEvents.get(x);
                                                                if (setterEvent.getEventKey().equals(setter.getEventKey())) {
                                                                    StaticVariables.listEvents.remove(x);
                                                                    StaticVariables.eventsAdapter.notifyDataSetChanged();
                                                                }
                                                            }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }
                                if (setterNotifications.getType().equals(StaticVariables.ad)) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(StaticVariables.childAds)
                                            .child(setterNotifications.getBroadcasterUid())
                                            .child(setterNotifications.getPostKey())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    GetSetterAd setterAd = dataSnapshot.getValue(GetSetterAd.class);
                                                    if (setterAd.getStatus().equals(StaticVariables.deleted)) {
                                                        insertAd(setterAd, StaticVariables.deleted);
                                                        return;
                                                    }

                                                    if (dbOperations.checkAdExistence(db, setterAd.getAdKey()) == 0)
                                                        insertAd(setterAd, "");

                                                    if (StaticVariables.listAds != null)
                                                        StaticVariables.listAds.add(0, setterAd);
                                                    if (StaticVariables.adsAdapter != null)
                                                        StaticVariables.adsAdapter.notifyDataSetChanged();

                                                    sendMessageNotification(setterAd);
                                                    addImpression(setterAd.getAdKey());
                                                }

                                                private void addImpression(String adKey) {
                                                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                    String username = prefs.getString(AppInfo.BUSINESS_BRAND_NAME, prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "..."));
                                                    String profileLink = prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, ""));
                                                    String location = prefs.getString(AppInfo.BUSINESS_LOCATION, prefs.getString(AppInfo.INDIVIDUAL_LOCATION, "No location"));
                                                    int signatureVersion = prefs.getInt(AppInfo.SIG_VER_BUS, prefs.getInt(AppInfo.SIG_VER_INDI, 0));

                                                    GetSetterImpression setter = new GetSetterImpression(type, uid, adKey, username, profileLink,
                                                            location, false, getCurrentTimestamp(), signatureVersion);

                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child(StaticVariables.childImpressions)
                                                            .child(adKey)
                                                            .child(uid)
                                                            .setValue(setter);
                                                }

                                                private void sendMessageNotification(GetSetterAd setterInbox) {
                                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                    intent.putExtra("tabItem", 2);
                                                    intent.putExtra("currentPage", 1);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, (int) getCurrentTimestamp(),
                                                            intent, 0);

                                                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                    NotificationCompat.Builder builder = (NotificationCompat.Builder) new
                                                            NotificationCompat.Builder(MainActivity.this)
                                                            .setSmallIcon(R.mipmap.ic_launcher)
                                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_big))
                                                            .setContentTitle("Advert from: " + setterInbox.getBrandName())
                                                            .setContentText(setterInbox.getTitle())
                                                            .setContentIntent(pendingIntent)
                                                            .setSound(alarmSound);
                                                    //.addAction(R.drawable.settings_mail, "Click to open", pendingIntent);
                                                    NotificationManager notificationManager = (NotificationManager)
                                                            getSystemService(Context.NOTIFICATION_SERVICE);
                                                    notificationManager.notify(12548, builder.build());
                                                }

                                                private void insertAd(GetSetterAd setter, String type) {
                                                    ContentValues values = new ContentValues();
                                                    values.put(DBContract.Ad.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
                                                    values.put(DBContract.Ad.IMPRESSIONS, String.valueOf(setter.getImpressions()));
                                                    values.put(DBContract.Ad.DURATION, String.valueOf(setter.getDuration()));
                                                    values.put(DBContract.Ad.KEY, setter.getAdKey());
                                                    values.put(DBContract.Ad.BROADCASTER_UID, setter.getBroadcasterUid());
                                                    values.put(DBContract.Ad.BRAND_NAME, setter.getBrandName());
                                                    values.put(DBContract.Ad.BRAND_LINK, setter.getBrandLink());
                                                    values.put(DBContract.Ad.TITLE, setter.getTitle());
                                                    values.put(DBContract.Ad.DETAILS, setter.getDetails());
                                                    values.put(DBContract.Ad.PROFILE_LINK, setter.getProfileLink());
                                                    values.put(DBContract.Ad.LOCATION_CODE, setter.getAdLocation());
                                                    values.put(DBContract.Ad.INTEREST_CODE, setter.getInterestCode());
                                                    values.put(DBContract.Ad.STATUS, setter.getStatus());
                                                    values.put(DBContract.Ad.VERIFIED, setter.isVerified() ? "yes" : "no");
                                                    values.put(DBContract.Ad.BROADCAST_TIME, "" + setter.getBroadcastTime());

                                                    values.put(DBContract.Ad.UPDATE_TIME, String.valueOf(setter.getUpdateTime()));
                                                    values.put(DBContract.Ad.SIGNATURE_VERSION, "" + setter.getSignatureVersion());

                                                    if (!type.equals(StaticVariables.deleted))
                                                        db.insert(DBContract.Ad.TABLE_NAME, null, values);
                                                    else {
                                                        db.update(DBContract.Ad.TABLE_NAME, values,
                                                                DBContract.Ad.KEY + "='" + setter.getAdKey() + "'",
                                                                null);

                                                        if (StaticVariables.listAds != null && StaticVariables.adsAdapter != null)
                                                            for (int x = 0; x < StaticVariables.listAds.size(); x++) {
                                                                GetSetterAd setterAd = (GetSetterAd) StaticVariables.listAds.get(x);
                                                                if (setterAd.getAdKey().equals(setter.getAdKey())) {
                                                                    StaticVariables.listAds.remove(x);
                                                                    StaticVariables.adsAdapter.notifyDataSetChanged();
                                                                }
                                                            }
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }
                                if (setterNotifications.getType().equals(StaticVariables.comment)) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(StaticVariables.childComments)
                                            .child(setterNotifications.getBroadcasterUid())
                                            .child(setterNotifications.getPostKey())
                                            .child(setterNotifications.getKey())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    GetSetterComments setterComments = dataSnapshot.getValue(GetSetterComments.class);

                                                    if (dbOperations.checkCommentExistence(db, setterComments.getKey()) == 0)
                                                        insertEvent(setterComments);

                                                    if (StaticVariables.listComments != null)
                                                        StaticVariables.listComments.add(0, setterComments);
                                                    if (FragmentComments.adapterComments != null)
                                                        FragmentComments.adapterComments.notifyDataSetChanged();

                                                    sendMessageNotification(setterComments);
                                                }

                                                private void sendMessageNotification(GetSetterComments setterInbox) {
                                                    Intent intent = new Intent(MainActivity.this, Comments.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, (int) getCurrentTimestamp(),
                                                            intent, 0);

                                                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                    NotificationCompat.Builder builder = (NotificationCompat.Builder) new
                                                            NotificationCompat.Builder(MainActivity.this)
                                                            .setSmallIcon(R.mipmap.ic_launcher)
                                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_big))
                                                            .setContentTitle("Comment from: " + setterInbox.getUsername())
                                                            .setContentText(setterInbox.getComment())
                                                            .setContentIntent(pendingIntent)
                                                            .setSound(alarmSound);
                                                    //.addAction(R.drawable.settings_mail, "Click to open", pendingIntent);
                                                    NotificationManager notificationManager = (NotificationManager)
                                                            getSystemService(Context.NOTIFICATION_SERVICE);
                                                    notificationManager.notify(125483, builder.build());
                                                }

                                                private void insertEvent(GetSetterComments setter) {
                                                    ContentValues values = new ContentValues();
                                                    values.put(DBContract.Comments.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
                                                    values.put(DBContract.Comments.BROADCASTER_UID, setter.getBroadcastUid());
                                                    values.put(DBContract.Comments.BROADCAST_NAME, setter.getBroadcastName());
                                                    values.put(DBContract.Comments.PLOFILE_LINK, setter.getProfileLink());
                                                    values.put(DBContract.Comments.UID, setter.getUid());
                                                    values.put(DBContract.Comments.KEY, setter.getKey());
                                                    values.put(DBContract.Comments.POST_KEY, setter.getPostKey());
                                                    values.put(DBContract.Comments.USERNAME, setter.getUsername());
                                                    values.put(DBContract.Comments.COMMENT, setter.getComment());
                                                    values.put(DBContract.Comments.VERIFIED, setter.isVerified() ? "yes" : "no");
                                                    values.put(DBContract.Comments.SIGNATURE_VERSION, "" + setter.getSignatureVersion());
                                                    values.put(DBContract.Comments.TIMESTAMP, String.valueOf(setter.getTimestamp()));

                                                    if (setter.getBroadcastUid() != null && setter.getComment() != null && setter.getKey() != null)
                                                        db.insert(DBContract.Comments.TABLE_NAME, null, values);
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }
                                if (setterNotifications.getType().equals(StaticVariables.review)) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(StaticVariables.childComments)
                                            .child(setterNotifications.getBroadcasterUid())
                                            .child(setterNotifications.getPostKey())
                                            .child(setterNotifications.getKey())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    GetSetterComments setterComments = dataSnapshot.getValue(GetSetterComments.class);

                                                    if (dbOperations.checkCommentExistence(db, setterComments.getKey()) == 0)
                                                        insertEvent(setterComments);

                                                    if (StaticVariables.listReview != null)
                                                        StaticVariables.listReview.add(0, setterComments);
                                                    if (FragmentReview.adapterReview != null)
                                                        FragmentReview.adapterReview.notifyDataSetChanged();

                                                    sendMessageNotification(setterComments);
                                                }

                                                private void sendMessageNotification(GetSetterComments setterInbox) {
                                                    Intent intent = new Intent(MainActivity.this, Comments.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, (int) getCurrentTimestamp(),
                                                            intent, 0);

                                                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                    NotificationCompat.Builder builder = (NotificationCompat.Builder) new
                                                            NotificationCompat.Builder(MainActivity.this)
                                                            .setSmallIcon(R.mipmap.ic_launcher)
                                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_big))
                                                            .setContentTitle("Comment from: " + setterInbox.getUsername())
                                                            .setContentText(setterInbox.getComment())
                                                            .setContentIntent(pendingIntent)
                                                            .setSound(alarmSound);
                                                    //.addAction(R.drawable.settings_mail, "Click to open", pendingIntent);
                                                    NotificationManager notificationManager = (NotificationManager)
                                                            getSystemService(Context.NOTIFICATION_SERVICE);
                                                    notificationManager.notify(125483, builder.build());
                                                }

                                                private void insertEvent(GetSetterComments setter) {
                                                    ContentValues values = new ContentValues();
                                                    values.put(DBContract.Comments.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
                                                    values.put(DBContract.Comments.BROADCASTER_UID, setter.getBroadcastUid());
                                                    values.put(DBContract.Comments.BROADCAST_NAME, setter.getBroadcastName());
                                                    values.put(DBContract.Comments.PLOFILE_LINK, setter.getProfileLink());
                                                    values.put(DBContract.Comments.UID, setter.getUid());
                                                    values.put(DBContract.Comments.KEY, setter.getKey());
                                                    values.put(DBContract.Comments.POST_KEY, setter.getPostKey());
                                                    values.put(DBContract.Comments.USERNAME, setter.getUsername());
                                                    values.put(DBContract.Comments.COMMENT, setter.getComment());
                                                    values.put(DBContract.Comments.VERIFIED, setter.isVerified() ? "yes" : "no");
                                                    values.put(DBContract.Comments.TIMESTAMP, String.valueOf(setter.getTimestamp()));

                                                    db.insert(DBContract.Comments.TABLE_NAME, null, values);
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });
            }

            startActiveUserThread();
        }

    }

    private void startActiveUserThread() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            long diffVersion = getCurrentTimestamp() - prefs.getLong(AppInfo.GET_VERSION_CODE, 0);
            final long diffVersionMedium = getCurrentTimestamp() - prefs.getLong(AppInfo.GET_VERSION_CODE_MEDIUM, 0);
            if (diffVersion > 86400000) {
                FirebaseDatabase.getInstance().getReference()
                        .child(StaticVariables.childAppVersion)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                GetSetterAppVersion setter = dataSnapshot.getValue(GetSetterAppVersion.class);
                                editor = prefs.edit();
                                editor.putLong(AppInfo.GET_VERSION_CODE, getCurrentTimestamp());
                                editor.apply();
                                if (versionCode == setter.getVersionCode())
                                    return;
                                if (versionCode < setter.getVersionCode()) {
                                    if (setter.getIndividualPriority().equals("high")) {
                                        editor = prefs.edit();
                                        editor.putLong(AppInfo.GET_VERSION_CODE, 0);
                                        editor.apply();

                                        builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setCancelable(false);
                                        builder.setTitle("App Outdated:");
                                        builder.setMessage("Your app version has become obsolete. Click OK to upgrade to a new version!!!.");
                                        builder.setIcon(R.mipmap.ic_launcher);
                                        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse("market://details?id=tech.myevents.app"));
                                                startActivity(intent);
                                            }
                                        });
                                        builder.show();
                                        return;
                                    }

                                    if (setter.getIndividualPriority().equals("medium")) {
                                        if (diffVersionMedium > (86400000 * 7)) {
                                            editor = prefs.edit();
                                            editor.putLong(AppInfo.GET_VERSION_CODE_MEDIUM, getCurrentTimestamp());
                                            editor.apply();

                                            builder = new AlertDialog.Builder(MainActivity.this);
                                            builder.setCancelable(false);
                                            builder.setTitle("New Update:");
                                            builder.setMessage("There's a new a update available, click OK to download!!!.");
                                            builder.setIcon(R.mipmap.ic_launcher);
                                            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                                    intent.setData(Uri.parse("market://details?id=tech.myevents.app"));
                                                    startActivity(intent);
                                                }
                                            });
                                            builder.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    builder.setCancelable(true);
                                                }
                                            });
                                            builder.show();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);
        if (FirebaseAuth.getInstance().getCurrentUser() == null && !prefsApp.getString(AppInfo.ACTIVE_USER, "").equals(""))
            finish();

        appbar = (AppBarLayout) findViewById(R.id.appbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.mainToolbarText);
        setSupportActionBar(toolbar);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = (androidx.viewpager.widget.ViewPager) findViewById(R.id.container);
        addTabFragments();

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabTextColors(getResources().getColor(R.color.barWhitish), getResources().getColor(R.color.barWhite));
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.barWhite));
        tabLayout.setupWithViewPager(viewPager);

        setTabIcons();
        insertUserProfile();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setTabIcons();

                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String pageName = tabNames[position];
                GetSetterPageHits setterHits = new GetSetterPageHits(uid, "", pageName, getCurrentTimestamp());
                new PageHitsBackTask().execute(setterHits);

                if (type.equals(StaticVariables.individual) && position == 1)
                    FirebaseDatabase.getInstance().getReference()
                            .child(StaticVariables.childMoments)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists())
                                        return;

                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        for (DataSnapshot snap: snapshot.getChildren()) {
                                            GetSetterMoment setter = snap.getValue(GetSetterMoment.class);

                                            if (setter.getTimestamp() + (86400000 * 7) < getCurrentTimestamp())
                                                continue;

                                            if (FragmentMoment.dataKey == null)
                                                FragmentMoment.dataKey = new LinkedList<>(Arrays.asList(""));

                                            if (StaticVariables.listMoments == null)
                                                StaticVariables.listMoments = new ArrayList<>();

                                            if (setter.getType().equals(StaticVariables.deleted))
                                                for (int y = 0; y < StaticVariables.listMoments.size(); y++) {
                                                    GetSetterMoment inbox = (GetSetterMoment) StaticVariables.listMoments.get(y);
                                                    if (inbox.getKey().equals(setter.getKey())) {
                                                        StaticVariables.listMoments.remove(y);
                                                        ContentValues values = new ContentValues();
                                                        values.put(DBContract.Moments.TYPE, StaticVariables.deleted);
                                                        db.update(DBContract.Moments.TABLE_NAME, values, DBContract.Moments.KEY + "='" + setter.getKey() + "'", null);

                                                        if (FragmentMoment.momentsAdapter != null)
                                                            FragmentMoment.momentsAdapter.notifyDataSetChanged();
                                                    }
                                                }

                                            if (!FragmentMoment.contactList.contains("" + setter.getContactNumber()))
                                                continue;

                                            if (FragmentMoment.dataKey.contains(setter.getKey()))
                                                continue;

                                            if (StaticVariables.listMoments.size() != 0) {
                                                StaticVariables.listMoments.add(0, setter);
                                                FragmentMoment.momentsAdapter.notifyDataSetChanged();
                                            }

                                            ContentValues values = new ContentValues();
                                            values.put(DBContract.Moments.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
                                            values.put(DBContract.Moments.KEY, setter.getKey());
                                            values.put(DBContract.Moments.TYPE, setter.getType());
                                            values.put(DBContract.Moments.USERNAME, setter.getUsername());
                                            values.put(DBContract.Moments.STORY, setter.getStory());
                                            values.put(DBContract.Moments.MOMENT_LINK, setter.getLink());
                                            values.put(DBContract.Moments.PROFILE_LINK, setter.getProfileLink());
                                            values.put(DBContract.Moments.TIMESTAMP, setter.getTimestamp());
                                            values.put(DBContract.Moments.CONTACT_NUMBER, "" + setter.getContactNumber());
                                            values.put(DBContract.Moments.SIGNATURE_VERSION, "" + setter.getSignatureVersion());

                                            db.insert(DBContract.Moments.TABLE_NAME, null, values);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void addTabFragments() {
        if (type.equals(StaticVariables.individual)) {
            floats = new FloatingActionButton[]{fabHome, fabMoment, fabHomeAd, null};

            tabNames = new String[]{"MyEvents App", "Shared moments", "Local adverts", "Playing events"};
            icons = new int[]{R.drawable.tab_home,
                    R.drawable.tab_friends,
                    R.drawable.tab_ads,
                    R.drawable.tab_play};
            iconSelected = new int[]{R.drawable.tab_home_selected,
                    R.drawable.tab_friends_selected,
                    R.drawable.tab_ads_selected,
                    R.drawable.tab_play_selected};
        } else {
            floats = new FloatingActionButton[]{fabDecoy, fabDecoy, fabDecoy, fabDecoy};

            tabNames = new String[]{"MyEvents Business", "Client messages", "Broadcast comments", "Brand reviews"};
            icons = new int[]{R.drawable.tab_home_bus,
                    R.drawable.tab_message,
                    R.drawable.tab_comment,
                    R.drawable.tab_review};
            iconSelected = new int[]{R.drawable.tab_home_selected,
                    R.drawable.tab_message_selected,
                    R.drawable.tab_comment_selected,
                    R.drawable.tab_review_selected};
        }

        if (type.equals(StaticVariables.individual)) {

            sectionsPagerAdapter.addFragments(new FragmentEvent(), "");
            sectionsPagerAdapter.addFragments(new FragmentMoment(), "");
            sectionsPagerAdapter.addFragments(new FragmentAd(), "");
            sectionsPagerAdapter.addFragments(new FragmentPlaying(), "");
            viewPager.setAdapter(sectionsPagerAdapter);
            viewPager.setCurrentItem(currentTabItem, true);
        } else {

            sectionsPagerAdapter.addFragments(new FragmentEvent(), "");
            sectionsPagerAdapter.addFragments(new FragmentMessages(), "");
            sectionsPagerAdapter.addFragments(new FragmentComments(), "");
            sectionsPagerAdapter.addFragments(new FragmentReview(), "");
            viewPager.setAdapter(sectionsPagerAdapter);
            viewPager.setCurrentItem(currentTabItem, true);
        }
    }

    private void setTabIcons() {
        int pos = viewPager.getCurrentItem();
        tabLayout.getTabAt(pos).setIcon(iconSelected[pos]);

        toolbar.setTitle(tabNames[pos]);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbarWhite));
        if (type.equals(StaticVariables.individual)) {
            Window w = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

            fabHome.setVisibility(View.VISIBLE);
            fabHomeBus.setVisibility(View.GONE);
            toolbar.setBackgroundColor(getResources().getColor(appBarColor[pos]));
            appbar.setBackgroundColor(getResources().getColor(appBarColor[pos]));
        } else {
            Window w = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                w.setStatusBarColor(getResources().getColor(R.color.appStatusBar));

            fabHome.setVisibility(View.GONE);
            fabHomeBus.setVisibility(View.VISIBLE);
            toolbar.setBackgroundColor(getResources().getColor(appBarColorBus[pos]));
            appbar.setBackgroundColor(getResources().getColor(appBarColorBus[pos]));
        }

        for (int x = 0; x < icons.length; x++) {
            if (x != pos)
                tabLayout.getTabAt(x).setIcon(icons[x]);
        }
        if (type.equals(StaticVariables.individual)) {
            if (pos == 0) {
                floats[0].setVisibility(View.VISIBLE);
                floats[1].setVisibility(View.GONE);
                floats[2].setVisibility(View.GONE);
                closeFabs();
                closeFabsAd();
            }
            if (pos == 1) {
                floats[1].setVisibility(View.VISIBLE);
                floats[0].setVisibility(View.GONE);
                floats[2].setVisibility(View.GONE);
                closeFabs();
                closeFabsAd();
            }
            if (pos == 2) {
                floats[2].setVisibility(View.VISIBLE);
                floats[1].setVisibility(View.GONE);
                floats[0].setVisibility(View.GONE);
                closeFabs();
                closeFabsAd();
            }
            if (pos == 3) {
                floats[0].setVisibility(View.GONE);
                floats[1].setVisibility(View.GONE);
                floats[2].setVisibility(View.GONE);
                closeFabs();
                closeFabsAd();
            }
        }
    }

    private void startActivitiesAndListener() {
        if (currentPage != 1) {
            switch (currentPage) {
                case 2:
                    startActivity(new Intent(this, Settings.class));
                    break;
                case 3:
                    startActivity(new Intent(this, BroadcastEvent.class));
                    break;
                case 4:
                    startActivity(new Intent(this, BroadcastAd.class));
                    break;
                case 5:
                    startActivity(new Intent(this, Profile.class));
                    break;
                case 6:
                    startActivity(new Intent(this, ProfileBusiness.class));
                    break;
                case 7:
                    startActivity(new Intent(this, Broadcasts.class));
                    break;
                case 8:
                    startActivity(new Intent(this, TicketSettings.class));
                    break;
                case 9:
                    startActivity(new Intent(this, Friends.class));
                    break;
                case 10:
                    startActivity(new Intent(this, ShareApp.class));
                    break;
                case 11:
                    startActivity(new Intent(this, Account.class));
                    break;
                case 12:
                    startActivity(new Intent(this, Messages.class));
                    break;
            }
        }

        fabBroadcast.setOnClickListener(this);
        fabAdmin.setOnClickListener(this);
        fabSettings.setOnClickListener(this);
        fabHome.setOnClickListener(this);
        fabMessage.setOnClickListener(this);
        fabMoment.setOnClickListener(this);
        fabBroadcastAd.setOnClickListener(this);
        fabHomeAd.setOnClickListener(this);
        fabBusiness.setOnClickListener(this);
        cardImage.setOnClickListener(this);
        fabHomeBus.setOnClickListener(this);
        fabSettingsBus.setOnClickListener(this);
        fabBroadcastBus.setOnClickListener(this);
        fabFriends.setOnClickListener(this);
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabBroadcast:
                startActivity(new Intent(this, BroadcastEvent.class));
                closeFabs();
                break;
            case R.id.fabMessage:
                startActivity(new Intent(this, Messages.class));
                closeFabs();
                break;
            case R.id.fabSettings:
            case R.id.fabSettingsBus:
                startActivity(new Intent(this, Settings.class));
                closeFabs();
                break;
            case R.id.fabHome:
            case R.id.fabHomeBus:
                showFabs();
                break;
            case R.id.fabAdmin:
                startActivity(new Intent(this, SettingsAdmin.class));
                closeFabs();
                break;
            case R.id.fabMoment:
                startActivity(new Intent(this, MomentShare.class));
                break;
            case R.id.fabBroadcastAd:
                startActivity(new Intent(this, BroadcastAd.class));
                closeFabsAd();
                break;
            case R.id.fabBusiness:
                startActivity(new Intent(this, Business.class));
                closeFabsAd();
                break;
            case R.id.fabHomeAd:
                showFabsAd();
                break;
            case R.id.cardImage:
                startActivity(new Intent(this, UserAccounts.class));
                break;
            case R.id.fabBroadcastBus:
                startActivity(new Intent(this, Broadcasts.class));
                break;
            case R.id.fabFriends:
                startActivity(new Intent(this, Friends.class));
                break;

        }
    }




    private void insertUserProfile() {
        int version;
        String url;
        if (prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual).equals(StaticVariables.individual)) {
            url = prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "");
            version = prefs.getInt(AppInfo.SIG_VER_INDI, 0);
        } else {
            url = prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
            version = prefs.getInt(AppInfo.SIG_VER_BUS, 0);
        }

        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .signature(new StringSignature("" + version))
                    .crossFade()
                    .into(ivProfile);
        } catch (Exception e) {
            ivProfile.setImageResource(0);
        }
    }

    public void notificationFlicker() {
        final Handler handler = new Handler();
        handler.post(runnableFlick);
    }

    final Runnable runnableFlick = new Runnable() {
        @Override
        public void run() {
            try {
                handler.postDelayed(this, 80);
                flickTabs();
            } catch (Exception e){
                //
            }
        }
    };

    private void flickTabs() {
        int flick = R.drawable.ic_notification;
        int decoy = R.drawable.ic_decoy;
        if (FLICKER_CALLS < 20) {
            if (FLICKER_CALLS % 2 == 1)
                tabLayout.getTabAt(FLICK_TAB).setIcon(flick);
            else
                tabLayout.getTabAt(FLICK_TAB).setIcon(decoy);
            FLICKER_CALLS++;
        }
        if (FLICKER_CALLS == 20)
            tabLayout.getTabAt(FLICK_TAB).setIcon(iconSelected[FLICK_TAB]);
    }





    private void showFabs() {
        if (fabType == 0) {
            fabType = 1;
            if (type.equals(StaticVariables.individual)) {
                fabFriends.setVisibility(View.VISIBLE);
                fabSettings.setVisibility(View.VISIBLE);
                fabMessage.setVisibility(View.VISIBLE);
            }
            else {
                fabBroadcast.setVisibility(View.VISIBLE);
                fabSettingsBus.setVisibility(View.VISIBLE);
                fabBroadcastBus.setVisibility(View.VISIBLE);
            }
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if (email.equals(StaticVariables.adminEmail))
                fabAdmin.setVisibility(View.VISIBLE);
            if (email.equals("colwanymab@gmail.com"))
                fabAdmin.setVisibility(View.VISIBLE);
        } else {
            closeFabs();
        }
    }

    private void closeFabs() {
        fabType = 0;
        if (type.equals(StaticVariables.individual))
            fabFriends.setVisibility(View.GONE);
        else
            fabBroadcast.setVisibility(View.GONE);
        fabAdmin.setVisibility(View.GONE);
        fabSettings.setVisibility(View.GONE);
        fabMessage.setVisibility(View.GONE);
        fabBroadcastBus.setVisibility(View.GONE);
        fabSettingsBus.setVisibility(View.GONE);
    }

    private void showFabsAd() {
        if (fabType == 0) {
            fabType = 1;
            fabBroadcastAd.setVisibility(View.VISIBLE);
            fabBusiness.setVisibility(View.VISIBLE);
        } else {
            closeFabsAd();
        }
    }

    private void closeFabsAd() {
        fabType = 0;
        fabBroadcastAd.setVisibility(View.GONE);
        fabBusiness.setVisibility(View.GONE);
    }




    @Override
    public void onBackPressed() {
        switch (viewPager.getCurrentItem()) {
            case 3:
                viewPager.setCurrentItem(2);
                break;
            case 2:
                viewPager.setCurrentItem(1);
                break;
            case 1:
                viewPager.setCurrentItem(0);
                break;
            case 0:
                super.onBackPressed();
                break;
        }
    }

    private long getCurrentTimestamp(){
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
    protected void onPause() {
        super.onPause();
        currentTabItem = viewPager.getCurrentItem();
        APP_SWITCH_ON = false;
        closeFabs();
        //getContactsList();
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings)
            startActivity(new Intent(MainActivity.this, Settings.class));
        if (id == R.id.action_message)
            startActivity(new Intent(MainActivity.this, Messages.class));

        return super.onOptionsItemSelected(item);
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        ArrayList<androidx.fragment.app.Fragment> fragments = new ArrayList<>();
        ArrayList<String> tabTitles = new ArrayList<>();

        public void addFragments(Fragment fragments, String titles){
            this.fragments.add(fragments);
            this.tabTitles.add(titles);
        }

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public androidx.fragment.app.Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {

            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles.get(position);
        }
    }

    private void getWelcomeMessages() {
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

    private void sendNotification() {
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.setBigContentTitle("MyEvents App Message");
        bigText.bigText("Welcome to MyEvents App!! Thank you for installing our new updated version.\n" +
                "Click to get started.");
        bigText.setSummaryText("By. Customer care.");

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification.setSmallIcon(R.drawable.notification_mail);
        notification.setColor(COLOR_DEFAULT);
        notification.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_big));
        notification.setTicker("Login Notification");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("Login Notification");
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





    private void getContactsList() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
            getContacts();
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

}
