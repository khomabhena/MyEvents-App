package tech.myevents.app;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMessages extends Fragment {

    ListView listView;
    ProgressBar progressBar;
    TextView tvEmpty;

    SharedPreferences prefsApp;

    DBOperations dbOperations;
    SQLiteDatabase db;
    List<String> messageKeys;

    private static boolean CHECKED_PROFILE = false;
    ChildEventListener listener;

    GetSetterUpdate setterUpdate;
    private static List listUpdate = new ArrayList<>();

    public FragmentMessages() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        prefsApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        dbOperations = new DBOperations(getContext());
        db = dbOperations.getWritableDatabase();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        messageKeys = dbOperations.getMessagesKeys(db, localUid);

        StaticVariables.messagesAdapter = new AdapterMessage(getContext(), R.layout.row_messages);

        listView = (ListView) view.findViewById(R.id.listView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);

        StaticVariables.listMessages = new ArrayList<>();
        new MessagesBackTask().execute();

        return view;
    }


    private long getCurrentTimestamp() {
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

            String sendTo, chatRoom, sender, receiver, senderUid, receiverUid,
                    message, key, linkSender, linkReceiver, accType, id;
            long timestamp;
            int sigVer;
            boolean verified, receiverVerified;

            while (cursor.moveToNext()) {
                id = cursor.getString(cursor.getColumnIndex(DBContract.Messages.ID));
                sendTo = cursor.getString(cursor.getColumnIndex(DBContract.Messages.SEND_TO));
                chatRoom = cursor.getString(cursor.getColumnIndex(DBContract.Messages.CHAT_ROOM));
                if (("" + chatRoom).contains("friend"))
                    continue;
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
                accType = cursor.getString(cursor.getColumnIndex(DBContract.Messages.ACC_TYPE));

                if (sig == null)
                    sigVer = 1;
                else
                    sigVer = Integer.parseInt(sig);

                if (sendTo == null || chatRoom == null || message == null || receiverUid == null || senderUid == null)
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

                GetSetterInbox setter = new GetSetterInbox(sendTo, key, chatRoom, sender,
                        receiver, senderUid, receiverUid, message,
                        timestamp, verified,receiverVerified, linkSender, linkReceiver, sigVer, accType);

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
                listView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.GONE);
                listView.setAdapter(StaticVariables.messagesAdapter);
                //listView.setSelection(listView.getAdapter().getCount() - 1);
                updateValues();
            } else {
                listView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
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

    private void loadAllMessages() {
        //progressBar.setVisibility(View.VISIBLE);
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
                                    listView.setVisibility(View.VISIBLE);

                                    StaticVariables.messagesAdapter.add(setter);
                                    listView.setAdapter(StaticVariables.messagesAdapter);
                                } else {
                                    StaticVariables.listMessages.add(0, setter);
                                    StaticVariables.messagesAdapter.notifyDataSetChanged();
                                }

                                new InsertMessages(getContext()).execute(setter);
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

}
