package tech.myevents.app;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Inbox extends AppCompatActivity implements View.OnClickListener {

    static ListView lvInbox;
    ProgressBar progressBar;
    EditText etMessage;
    View viewSend;

    private String sendTo, chatRoom1, chatRoom2, sender, receiver, senderUid, receiverUid, linkSender, linkReceiver;
    private boolean senderVerified, receiverVerified;

    SharedPreferences prefs;
    SharedPreferences prefsApp;

    DBOperations dbOperations;
    SQLiteDatabase db;
    List<String> messageKeys;


    static boolean NETWORK_AVAILABLE = false;
    int position;
    String from, accType;

    static String theChatRoom = "";
    static boolean isInboxOn = false;

    ChildEventListener listener;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        statusBarColor();

        String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        StaticVariables.inboxAdapter= new AdapterInbox(Inbox.this, R.layout.row_inbox);
        dbOperations = new DBOperations(Inbox.this);
        db = dbOperations.getWritableDatabase();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        messageKeys = dbOperations.getMessagesKeys(db, localUid);

        from = getIntent().getExtras().getString("from");
        position = getIntent().getExtras().getInt("position");

        getValuesFrom(from);

        lvInbox = (ListView) findViewById(R.id.lvInbox);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        etMessage = (EditText) findViewById(R.id.etMessage);
        viewSend = findViewById(R.id.viewSend);

        StaticVariables.listInbox = new ArrayList<>();
        new InboxBackTask().execute();

        getSupportActionBar().setTitle(receiver);

        lvInbox.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        lvInbox.setStackFromBottom(true);
        viewSend.setOnClickListener(this);

        lvInbox.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1267764);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1267764);
            }
        });

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Inbox";
        GetSetterPageHits setterHits = new GetSetterPageHits(uid, "", pageName, getCurrentTimestamp());
        new PageHitsBackTask().execute(setterHits);
    }

    private void statusBarColor() {
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

    private void getValuesFrom(String from) {
        if (from.equals(StaticVariables.message)) {
            GetSetterInbox setterMessages = (GetSetterInbox) StaticVariables.listMessages.get(position);
            if (setterMessages.getSendTo().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                //Directed to me, flip values
                sendTo = setterMessages.getSenderUid();
                chatRoom1 = setterMessages.getChatRoom();
                chatRoom2 = setterMessages.getChatRoom();
                sender = accType.equals(StaticVariables.individual) ?
                        prefs.getString(AppInfo.INDIVIDUAL_USERNAME, FirebaseAuth.getInstance().getCurrentUser().getEmail())
                        : prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "");

                senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                receiverUid = setterMessages.getSenderUid();

                receiver = setterMessages.getSender().equals("MyEvents App") ? "Customer care" : setterMessages.getSender();
                linkSender = accType.equals(StaticVariables.individual) ?
                        prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
                linkReceiver = setterMessages.getLinkSender();
                receiverVerified = setterMessages.isSenderVerified();
                senderVerified = !accType.equals(StaticVariables.individual) && prefs.getBoolean(AppInfo.BUSINESS_VERIFIED, false);
            } else {
                //I'm the one sending
                sendTo = setterMessages.getSendTo();
                chatRoom1 = setterMessages.getChatRoom();
                chatRoom2 = setterMessages.getChatRoom();
                sender = setterMessages.getSender();
                receiver = setterMessages.getReceiver().equals("MyEvents App") ? "Customer care" : setterMessages.getReceiver();
                senderUid = setterMessages.getSenderUid();
                receiverUid = setterMessages.getReceiverUid();
                linkSender = accType.equals(StaticVariables.individual) ?
                        prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
                linkReceiver = setterMessages.getLinkReceiver();
                receiverVerified = setterMessages.isReceiverVerified();
                senderVerified = !accType.equals(StaticVariables.individual) && prefs.getBoolean(AppInfo.BUSINESS_VERIFIED, false);
            }
        }
        else if (from.equals(StaticVariables.event)
                || from.equals(StaticVariables.playing)
                || from.equals(StaticVariables.eventExclusive)
                || from.equals(StaticVariables.eventWaiting)) {

            String uid =  FirebaseAuth.getInstance().getCurrentUser().getUid();
            GetSetterEvent setter;
            if (from.equals(StaticVariables.playing))
                setter = (GetSetterEvent) StaticVariables.listPlaying.get(position);
            else if (from.equals(StaticVariables.eventExclusive))
                setter = (GetSetterEvent) StaticVariables.listEventsExclusive.get(position);
            else if (from.equals(StaticVariables.eventWaiting))
                setter = (GetSetterEvent) StaticVariables.listEventsWaiting.get(position);
            else
                setter = (GetSetterEvent) StaticVariables.listEvents.get(position);

            sendTo = setter.getBroadcasterUid();
            chatRoom1 = uid + "event" + setter.getBroadcasterUid();
            chatRoom2 = setter.getBroadcasterUid() + "event" + uid;
            sender = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "No Name") : prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "");
            receiver = setter.getBrandName() == null ? setter.getBroadcasterUid() : setter.getBrandName();
            senderUid = uid;
            receiverUid = setter.getBroadcasterUid();
            linkSender = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
            linkReceiver = setter.getBrandLink();
            receiverVerified = setter.isVerified();
            senderVerified = !accType.equals(StaticVariables.individual) && prefs.getBoolean(AppInfo.BUSINESS_VERIFIED, false);
        }
        else if (from.equals(StaticVariables.ad)) {
            String uid =  FirebaseAuth.getInstance().getCurrentUser().getUid();
            GetSetterAd setter = (GetSetterAd) StaticVariables.listAds.get(position);
            sendTo = setter.getBroadcasterUid();
            chatRoom1 = uid + "ad" + setter.getBroadcasterUid();
            chatRoom2 = setter.getBroadcasterUid() + "ad" + uid;
            sender =  accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "No Name") : prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "");
            receiver = setter.getBrandName() == null ? setter.getBroadcasterUid() : setter.getBrandName();
            senderUid = uid;
            receiverUid = setter.getBroadcasterUid();
            linkSender = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
            linkReceiver = setter.getProfileLink();
            receiverVerified = setter.isVerified();
            senderVerified = !accType.equals(StaticVariables.individual) && prefs.getBoolean(AppInfo.BUSINESS_VERIFIED, false);
        }
        else if (from.equals(StaticVariables.friend)) {
            String uid =  FirebaseAuth.getInstance().getCurrentUser().getUid();
            GetSetterUser setter = (GetSetterUser) StaticVariables.listFriends.get(position);
            sendTo = setter.getUid();
            chatRoom1 = uid + "friend" + setter.getUid();
            chatRoom2 = setter.getUid() + "friend" + uid;
            sender = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "No Name") : prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "");
            receiver = setter.getUsername() == null ? setter.getUid() : setter.getUsername();
            senderUid = uid;
            receiverUid = setter.getUid();
            linkSender = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
            linkReceiver = setter.getProfileLink();
            receiverVerified = false;
            senderVerified = !accType.equals(StaticVariables.individual) && prefs.getBoolean(AppInfo.BUSINESS_VERIFIED, false);
        }
        else if (from.equals(StaticVariables.business)) {
            String uid =  FirebaseAuth.getInstance().getCurrentUser().getUid();
            GetSetterBusiness setter = (GetSetterBusiness) StaticVariables.listBusinesses.get(position);
            sendTo = setter.getUid();
            chatRoom1 = uid + "bus" + setter.getUid();
            chatRoom2 = setter.getUid() + "bus" + uid;
            sender = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "No Name") : prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "");
            receiver = setter.getBrandName() == null ? setter.getUid() : setter.getBrandName();
            senderUid = uid;
            receiverUid = setter.getUid();
            linkSender = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");
            linkReceiver = setter.getProfileLink();
            receiverVerified = setter.isVerified();
            senderVerified = !accType.equals(StaticVariables.individual) && prefs.getBoolean(AppInfo.BUSINESS_VERIFIED, false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        isInboxOn = true;
    }

    @Override
    public void onClick(View v) {
        sendMessage();
    }

    private void sendMessage() {
        if (!etMessage.getText().toString().isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String key = FirebaseDatabase.getInstance().getReference().child(StaticVariables.childMessages).child(uid).push().getKey();
            String message = etMessage.getText().toString();
            int sigVer = accType.equals(StaticVariables.individual) ?
                    prefs.getInt(AppInfo.SIG_VER_INDI, 0) : prefs.getInt(AppInfo.SIG_VER_BUS, 0);

            etMessage.setText("");
            GetSetterInbox setter = new GetSetterInbox(sendTo, key, chatRoom1, sender, receiver, senderUid,
                    receiverUid, message, getCurrentTimestamp(), senderVerified, receiverVerified, linkSender, linkReceiver, sigVer, accType);

            if (StaticVariables.listInbox.size() == 0) {
                StaticVariables.inboxAdapter.add(setter);
                lvInbox.setAdapter(StaticVariables.inboxAdapter);
            } else {
                StaticVariables.inboxAdapter.add(setter);
                StaticVariables.inboxAdapter.notifyDataSetChanged();
                lvInbox.setSelection(lvInbox.getAdapter().getCount() - 1);
            }
            sendMessageToDB(setter, key);
        }
    }

    private void sendMessageToDB(final GetSetterInbox setter, final String key) {
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childMessages)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(chatRoom1)) {
                            setter.setChatRoom(chatRoom1);
                            theChatRoom = setter.getChatRoom();
                            FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child(StaticVariables.childMessages)
                                    .child(setter.getChatRoom())
                                    .child(key)
                                    .setValue(setter)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                new InsertMessages(Inbox.this).execute(setter);
                                            }
                                        }
                                    });
                        } else if (dataSnapshot.hasChild(chatRoom2)) {
                            setter.setChatRoom(chatRoom2);
                            theChatRoom = setter.getChatRoom();
                            FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child(StaticVariables.childMessages)
                                    .child(setter.getChatRoom())
                                    .child(key)
                                    .setValue(setter)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                new InsertMessages(Inbox.this).execute(setter);
                                            }
                                        }
                                    });
                        } else {
                            setter.setChatRoom(chatRoom1);
                            theChatRoom = setter.getChatRoom();
                            FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childMessages)
                                    .child(setter.getChatRoom())
                                    .child(key)
                                    .setValue(setter)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                new InsertMessages(Inbox.this).execute(setter);
                                            }
                                        }
                                    });
                        }

                        String notKey = FirebaseDatabase.getInstance().getReference()
                                .child(StaticVariables.childNotificationsUser)
                                .child(setter.getSendTo())
                                .push()
                                .getKey();

                        GetSetterNotifications setterNotifications = new
                                GetSetterNotifications(StaticVariables.message, notKey, setter.getChatRoom(), key, false);

                        FirebaseDatabase.getInstance().getReference()
                                .child(StaticVariables.childNotificationsUser)
                                .child(setter.getSendTo())
                                .child(notKey)
                                .setValue(setterNotifications);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }




    private class InboxBackTask extends AsyncTask<Void, GetSetterInbox, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            Cursor cursor;
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            cursor = dbOperations.getInboxDetailed(db, chatRoom1, chatRoom2, localUid);

            int count = cursor.getCount();

            String sendTo, chatRoom, sender, receiver, senderUid, receiverUid,
                    message, key, linkSender, linkReceiver, accType;
            long timestamp;
            boolean verified, receiverVerified;
            int sigVer;

            while (cursor.moveToNext()) {
                sendTo = cursor.getString(cursor.getColumnIndex(DBContract.Messages.SEND_TO));
                chatRoom = cursor.getString(cursor.getColumnIndex(DBContract.Messages.CHAT_ROOM));
                sender = cursor.getString(cursor.getColumnIndex(DBContract.Messages.SENDER));
                receiver = cursor.getString(cursor.getColumnIndex(DBContract.Messages.RECEIVER));
                senderUid = cursor.getString(cursor.getColumnIndex(DBContract.Messages.SENDER_UID));
                receiverUid = cursor.getString(cursor.getColumnIndex(DBContract.Messages.RECEIVER_UID));
                message = cursor.getString(cursor.getColumnIndex(DBContract.Messages.MESSAGE));
                key = cursor.getString(cursor.getColumnIndex(DBContract.Messages.KEY));
                linkSender = cursor.getString(cursor.getColumnIndex(DBContract.Messages.LINK_SENDER));
                linkReceiver = cursor.getString(cursor.getColumnIndex(DBContract.Messages.LINK_RECEIVER));
                String stamp = cursor.getString(cursor.getColumnIndex(DBContract.Messages.TIMESTAMP));

                if (stamp == null)
                    timestamp = getCurrentTimestamp();
                else
                    timestamp = Long.parseLong(stamp);

                String veri = cursor.getString(cursor.getColumnIndex(DBContract.Messages.SENDER_VERIFIED));
                String veriReceiver = cursor.getString(cursor.getColumnIndex(DBContract.Messages.RECEIVER_VERIFIED));
                String sig = cursor.getString(cursor.getColumnIndex(DBContract.Messages.SIGNATURE_VERSION));

                if (sig == null)
                    sigVer = 1;
                else
                    sigVer = Integer.parseInt(sig);

                accType = cursor.getString(cursor.getColumnIndex(DBContract.Messages.ACC_TYPE));

                if (sendTo == null || chatRoom == null || message == null || receiverUid == null || senderUid == null)
                    continue;

                if (veri == null) verified = false;
                else {
                    if (veri.equals("yes"))
                        verified = true;
                    else
                        verified = false;
                }

                if (veriReceiver == null) receiverVerified = false;
                else {
                    if (veriReceiver.equals("yes"))
                        receiverVerified = true;
                    else
                        receiverVerified = false;
                }

                GetSetterInbox setter = new GetSetterInbox(sendTo, key, chatRoom, sender,
                        receiver, senderUid, receiverUid, message, timestamp,
                        verified, receiverVerified, linkSender, linkReceiver, sigVer, accType);

                theChatRoom = setter.getChatRoom();
                publishProgress(setter);
            }

            return count;
        }

        @Override
        protected void onProgressUpdate(GetSetterInbox... values) {
            StaticVariables.inboxAdapter.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count != 0) {
                progressBar.setVisibility(View.GONE);
                lvInbox.setAdapter(StaticVariables.inboxAdapter);
            } else {
                progressBar.setVisibility(View.GONE);
            }
            loadInbox();
        }
    }

    private void loadInbox() {
        //progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childMessages)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(chatRoom1)) {
                            theChatRoom = chatRoom1;
                            if (StaticVariables.listInbox == null)
                                StaticVariables.listInbox = new ArrayList<>();
                            long count = 0;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                GetSetterInbox setter = snapshot.getValue(GetSetterInbox.class);
                                if (setter.getSendTo() == null || setter.getChatRoom() == null
                                        || setter.getMessage() == null || setter.getReceiverUid() == null
                                        || setter.getSenderUid() == null)
                                    continue;

                                if (messageKeys.contains(setter.getKey()))
                                    continue;

                                count++;
                                StaticVariables.inboxAdapter.add(setter);
                                new InsertMessages(Inbox.this).execute(setter);
                            }

                            if (count != 0) {
                                NETWORK_AVAILABLE = true;
                                lvInbox.setVisibility(View.VISIBLE);
                                lvInbox.setAdapter(StaticVariables.inboxAdapter);
                                lvInbox.setSelection(lvInbox.getAdapter().getCount() - 1);
                                progressBar.setVisibility(View.GONE);
                            }

                        } else if (dataSnapshot.hasChild(chatRoom2)) {
                            theChatRoom = chatRoom2;
                            if (StaticVariables.listInbox == null)
                                StaticVariables.listInbox = new ArrayList<>();
                            long count = 0;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                GetSetterInbox setter = snapshot.getValue(GetSetterInbox.class);
                                if (setter.getSendTo() == null || setter.getChatRoom() == null
                                        || setter.getMessage() == null || setter.getReceiverUid() == null
                                        || setter.getSenderUid() == null)
                                    continue;

                                if (messageKeys.contains(setter.getKey()))
                                    continue;

                                count++;
                                StaticVariables.inboxAdapter.add(setter);
                                new InsertMessages(Inbox.this).execute(setter);
                            }

                            if (count != 0) {
                                NETWORK_AVAILABLE = true;
                                lvInbox.setVisibility(View.VISIBLE);
                                lvInbox.setAdapter(StaticVariables.inboxAdapter);
                                lvInbox.setSelection(lvInbox.getAdapter().getCount() - 1);
                                progressBar.setVisibility(View.GONE);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //
                    }
                });
    }

    private class InsertMessages extends AsyncTask<GetSetterInbox, Void, Void> {

        Context context;

        public InsertMessages(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(GetSetterInbox... params) {
            GetSetterInbox setter = params[0];
            String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

            if (!messageKeys.contains(setter.getKey())) {
                messageKeys.add(setter.getKey());
                //makeAdImpression(setterEvent);
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
                        && setter.getSenderUid() != null && setter.getReceiverUid() != null && setter.getMessage() != null)
                    db.insert(DBContract.Messages.TABLE_NAME, null, values);
            }

            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            messageKeys = dbOperations.getMessagesKeys(db, localUid);

            return null;
        }

    }

    private void loadInboxListener(String chatRoom) {
        listener = FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childMessages)
                .child(chatRoom)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        try {
                            if (StaticVariables.listInbox == null)
                                StaticVariables.listInbox = new ArrayList<>();
                            GetSetterInbox setter = dataSnapshot.getValue(GetSetterInbox.class);
                            if (setter.getSendTo() == null || setter.getChatRoom() == null
                                    || setter.getMessage() == null || setter.getReceiverUid() == null
                                    || setter.getSenderUid() == null)
                                return;

                            if (!messageKeys.contains(setter.getKey())) {
                                if (setter.getSendTo().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    StaticVariables.inboxAdapter.add(setter);
                                    StaticVariables.inboxAdapter.notifyDataSetChanged();
                                    lvInbox.setSelection(lvInbox.getAdapter().getCount() - 1);
                                    new InsertMessages(Inbox.this).execute(setter);
                                }
                            }
                        } catch (Exception e) {
                            //
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

    @Override
    protected void onPause() {
        super.onPause();
        isInboxOn = false;
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

}
