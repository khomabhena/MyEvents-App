package tech.myevents.app;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static tech.myevents.app.MainActivity.currentPage;

public class Messages extends AppCompatActivity implements View.OnClickListener {

    ListView lvMessages;
    ProgressBar progressBar;
    TextView tvEmpty;
    CardView cardMessage;

    SharedPreferences prefsApp;

    DBOperations dbOperations;
    SQLiteDatabase db;
    List<String> messageKeys;

    private static int MESSAGE_SCROLL_POS = 0;
    private static long MESSAGE_LAST_LOAD = 0;
    static boolean NETWORK_AVAILABLE = false;
    static boolean DB_LISTENING = false;

    ChildEventListener listener;
    String accType;

    Toolbar toolbar;

    GetSetterUpdate setterUpdate;
    private static List listUpdate = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        dbOperations = new DBOperations(Messages.this);
        db = dbOperations.getWritableDatabase();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        messageKeys = dbOperations.getMessagesKeys(db, localUid);
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        StaticVariables.messagesAdapter = new AdapterMessage(Messages.this, R.layout.row_messages);

        lvMessages = (ListView) findViewById(R.id.lvMessages);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        cardMessage = findViewById(R.id.cardMessage);
        cardMessage.setOnClickListener(this);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pageName = "Messages";
        GetSetterPageHits setterHits = new GetSetterPageHits(uid, "", pageName, getCurrentTimestamp());
        new PageHitsBackTask().execute(setterHits);
    }

    @Override
    public void onClick(View view) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://api.whatsapp.com/send?phone=263775523763"));
        startActivity(i);
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
    protected void onResume() {
        super.onResume();
        StaticVariables.listMessages = new ArrayList<>();
        new MessagesBackTask().execute();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1267764);
    }

    private void loadAllMessages() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childMessages)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (StaticVariables.listMessages == null)
                            StaticVariables.listMessages = new ArrayList<>();
                        long count = 0;
                        for (DataSnapshot snap: dataSnapshot.getChildren()) {
                            long recordCount = 0;
                            long records = snap.getChildrenCount();
                            for (DataSnapshot snapshot : snap.getChildren()) {
                                recordCount++;
                                if (recordCount != records)
                                    continue;
                                GetSetterInbox setter = snapshot.getValue(GetSetterInbox.class);

                                if (!setter.getChatRoom().contains(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                    return;
                                if (messageKeys.contains(setter.getKey()))
                                    continue;

                                count++;
                                if (StaticVariables.listMessages.size() == 0) {
                                    tvEmpty.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                    lvMessages.setVisibility(View.VISIBLE);

                                    StaticVariables.messagesAdapter.add(setter);
                                    lvMessages.setAdapter(StaticVariables.messagesAdapter);
                                } else {
                                    StaticVariables.listMessages.add(0, setter);
                                    StaticVariables.messagesAdapter.notifyDataSetChanged();
                                }

                                new InsertMessages(Messages.this).execute(setter);
                            }
                        }

                        if (count != 0) {
                            NETWORK_AVAILABLE = true;
                            MESSAGE_LAST_LOAD = getCurrentTimestamp();
                        }
                        if (!DB_LISTENING)
                            loadInboxListener();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //
                    }
                });
    }

    private void loadInboxListener() {
        listener = FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childMessages)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        try {
                            if (StaticVariables.listMessages == null)
                                StaticVariables.listMessages = new ArrayList<>();
                            GetSetterInbox setter = dataSnapshot.getValue(GetSetterInbox.class);

                            if (!setter.getChatRoom().contains(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                return;
                            if (!setter.getSendTo().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                return;
                            if (messageKeys.contains(setter.getKey()))
                                return;

                            StaticVariables.messagesAdapter.add(setter);
                            StaticVariables.messagesAdapter.notifyDataSetChanged();
                            lvMessages.setSelection(lvMessages.getAdapter().getCount() - 1);
                            new InsertMessages(Messages.this).execute(setter);
                            DB_LISTENING = true;

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

    private class MessagesBackTask extends AsyncTask<Void, GetSetterInbox, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Cursor cursor = dbOperations.getMessages(db, localUid);
            int count = cursor.getCount();

            String sendTo = null, chatRoom, sender, receiver, senderUid,
                    receiverUid, message, key, linkSender, linkReceiver, accType, id;
            long timestamp = 0;
            boolean verified, receiverVerified;
            int sigVer = 0;

            while (cursor.moveToNext()) {
                id = cursor.getString(cursor.getColumnIndex(DBContract.Messages.ID));
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
                String veriR = cursor.getString(cursor.getColumnIndex(DBContract.Messages.RECEIVER_VERIFIED));
                String sig = cursor.getString(cursor.getColumnIndex(DBContract.Messages.SIGNATURE_VERSION));

                if (sig == null)
                    sigVer = 1;
                else
                    sigVer = Integer.parseInt(sig);

                accType = cursor.getString(cursor.getColumnIndex(DBContract.Messages.ACC_TYPE));

                if (sendTo == null || message == null)
                    continue;

                if (veri == null) verified = false;
                else {
                    if (veri.equals("yes"))
                        verified = true;
                    else
                        verified = false;
                }

                if (veriR == null) receiverVerified = false;
                else {
                    if (veriR.equals("yes"))
                        receiverVerified = true;
                    else
                        receiverVerified = false;
                }

                GetSetterInbox setter = new GetSetterInbox(sendTo, key, chatRoom, sender, receiver,
                        senderUid, receiverUid, message, timestamp, verified, receiverVerified,
                        linkSender, linkReceiver, sigVer, accType);

                String uid, messagePerson;
                if (sendTo.equals(localUid)) {
                    uid = senderUid;
                    messagePerson = StaticVariables.sender;
                } else {
                    uid = receiverUid;
                    messagePerson = StaticVariables.receiver;
                }

                setterUpdate = new GetSetterUpdate(accType, id, uid, key, messagePerson);
                listUpdate.add(setterUpdate);

                publishProgress(setter);
            }

            return count;
        }

        @Override
        protected void onProgressUpdate(GetSetterInbox... values) {
            StaticVariables.messagesAdapter.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count != 0) {
                lvMessages.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.GONE);
                lvMessages.setAdapter(StaticVariables.messagesAdapter);
                updateValues();
            } else {
                lvMessages.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
            loadAllMessages();
        }
    }

    private void updateValues() {
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childBusinesses)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        for (DataSnapshot snap: dataSnapshot.getChildren()) {
                            GetSetterBusiness setterBusiness = snap.getValue(GetSetterBusiness.class);

                            for (int x =0; x < listUpdate.size(); x++) {
                                GetSetterUpdate setterUpdate = (GetSetterUpdate) listUpdate.get(x);
                                if (setterUpdate.getUid().equals(setterBusiness.getUid())) {

                                    if (setterUpdate.getType().equals(StaticVariables.business)) {

                                        for (int y = 0; y < StaticVariables.listMessages.size(); y++) {
                                            GetSetterInbox setterInbox = (GetSetterInbox) StaticVariables.listMessages.get(y);
                                            if (setterInbox.getKey().equals(setterUpdate.getMessageKey())) {
                                                ContentValues values = new ContentValues();
                                                if (setterUpdate.getMessagePerson().equals(StaticVariables.sender)) {
                                                    setterInbox.setSender(setterBusiness.getBrandName());
                                                    setterInbox.setLinkSender(setterBusiness.getProfileLink());
                                                    values.put(DBContract.Messages.SENDER, setterBusiness.getBrandName());
                                                    values.put(DBContract.Messages.LINK_SENDER, setterBusiness.getProfileLink());
                                                } else {
                                                    setterInbox.setReceiver(setterBusiness.getBrandName());
                                                    setterInbox.setLinkReceiver(setterBusiness.getProfileLink());
                                                    values.put(DBContract.Messages.RECEIVER, setterBusiness.getBrandName());
                                                    values.put(DBContract.Messages.LINK_RECEIVER, setterBusiness.getProfileLink());
                                                }
                                                setterInbox.setSignatureVersion(setterBusiness.getSignatureVersion());
                                                StaticVariables.messagesAdapter.notifyDataSetChanged();
                                                values.put(DBContract.Messages.SIGNATURE_VERSION, "" + setterBusiness.getSignatureVersion());

                                                db.update(DBContract.Messages.TABLE_NAME, values,
                                                        DBContract.Messages.ID + "='" + setterUpdate.getDbId() + "'",
                                                        null);
                                            }
                                        }


                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childUsers)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        for (DataSnapshot snap: dataSnapshot.getChildren()) {
                            GetSetterUser setterBusiness = snap.getValue(GetSetterUser.class);

                            for (int x =0; x < listUpdate.size(); x++) {
                                GetSetterUpdate setterUpdate = (GetSetterUpdate) listUpdate.get(x);
                                if (setterUpdate.getUid().equals(setterBusiness.getUid())) {

                                    if (setterUpdate.getType().equals(StaticVariables.individual)) {

                                        for (int y = 0; y < StaticVariables.listMessages.size(); y++) {
                                            GetSetterInbox setterInbox = (GetSetterInbox) StaticVariables.listMessages.get(y);
                                            if (setterInbox.getKey().equals(setterUpdate.getMessageKey())) {
                                                ContentValues values = new ContentValues();

                                                if (setterUpdate.getMessagePerson().equals(StaticVariables.sender)) {
                                                    setterInbox.setSender(setterBusiness.getUsername());
                                                    setterInbox.setLinkSender(setterBusiness.getProfileLink());
                                                    values.put(DBContract.Messages.SENDER, setterBusiness.getUsername());
                                                    values.put(DBContract.Messages.LINK_SENDER, setterBusiness.getProfileLink());
                                                } else {
                                                    setterInbox.setReceiver(setterBusiness.getUsername());
                                                    setterInbox.setLinkReceiver(setterBusiness.getProfileLink());
                                                    values.put(DBContract.Messages.RECEIVER, setterBusiness.getUsername());
                                                    values.put(DBContract.Messages.LINK_RECEIVER, setterBusiness.getProfileLink());
                                                }
                                                setterInbox.setSignatureVersion(setterBusiness.getSignatureVersion());
                                                StaticVariables.messagesAdapter.notifyDataSetChanged();
                                                values.put(DBContract.Messages.SIGNATURE_VERSION, "" + setterBusiness.getSignatureVersion());

                                                db.update(DBContract.Messages.TABLE_NAME, values,
                                                        DBContract.Messages.ID + "='" + setterUpdate.getDbId() + "'",
                                                        null);
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            currentPage = 1;
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        currentPage = 1;
        super.onBackPressed();
    }


}
