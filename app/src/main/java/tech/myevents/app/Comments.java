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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

public class Comments extends AppCompatActivity implements View.OnClickListener {

    ListView listView;
    ProgressBar progressBar;
    EditText etComment;
    View viewSend;
    ImageView ivSend;

    DBOperations dbOperations;
    SQLiteDatabase db;
    List<String> commentKeys;
    SharedPreferences prefs;
    SharedPreferences prefsApp;

    static boolean DB_LISTENING = false;
    ChildEventListener listener;
    int position;
    String from, listenerType;
    CommentsAdapter commentsAdapter;

    private String uid, key, postKey, username, comment, title, postComment;
    private long timestamp, postTimestamp;
    private boolean verified;
    String accType, broadcasterUid, broadcastName, profileLink;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        position = getIntent().getExtras().getInt("position");
        from = getIntent().getExtras().getString("from");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
        prefsApp = getSharedPreferences(AppInfo.APP_INFO,  Context.MODE_PRIVATE);
        accType = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        getValues(from);

        dbOperations = new DBOperations(Comments.this);
        db = dbOperations.getWritableDatabase();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        commentKeys = dbOperations.getCommentInboxKeys(db, localUid);

        commentsAdapter = new CommentsAdapter(Comments.this, R.layout.row_comments);

        listView = (ListView) findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        etComment =(EditText) findViewById(R.id.etComment);
        viewSend = findViewById(R.id.viewSend);
        ivSend = (ImageView) findViewById(R.id.ivSend);

        StaticVariables.listCommentsInbox = new ArrayList<>();
        new CommentsBackTask().execute();
        loadAllComments();

        getSupportActionBar().setTitle(title);

        viewSend.setOnClickListener(this);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        listView.setStackFromBottom(true);

        if (from.equals(StaticVariables.review)) {
            ivSend.setVisibility(View.GONE);
            etComment.setVisibility(View.GONE);
            viewSend.setVisibility(View.GONE);
        }

        String pageName = "Comments";
        GetSetterPageHits setterHits = new GetSetterPageHits(uid, "", pageName, getCurrentTimestamp());
        new PageHitsBackTask().execute(setterHits);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(125483);
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

    private void getValues(String from) {
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        username = accType.equals(StaticVariables.individual) ?
                prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "") : prefs.getString(AppInfo.BUSINESS_BRAND_NAME, "");
        verified = !accType.equals(StaticVariables.individual)
                && prefs.getBoolean(AppInfo.BUSINESS_VERIFIED, false);

        if (from.equals(StaticVariables.event)
                || from.equals(StaticVariables.playing)
                || from.equals(StaticVariables.eventExclusive)) {

            GetSetterEvent setter;
            if (from.equals(StaticVariables.playing))
                setter = (GetSetterEvent) StaticVariables.listPlaying.get(position);
            else if (from.equals(StaticVariables.eventExclusive))
                setter = (GetSetterEvent) StaticVariables.listEventsExclusive.get(position);
            else
                setter = (GetSetterEvent) StaticVariables.listEvents.get(position);

            listenerType = StaticVariables.comment;
            broadcasterUid = setter.getBroadcasterUid() + StaticVariables.commentBroad;
            broadcastName = setter.getName();
            profileLink = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");

            postComment = "Share your views and comments about the event: <b>" +
                    setter.getName() + "</b>. Also address issues of concern to the event broadcaster.";
            postTimestamp = setter.getBroadcastTime();
            title = setter.getName();
            postKey = setter.getEventKey();
            key = FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childComments).child(postKey).push().getKey();

        }
        else if (from.equals(StaticVariables.ad)) {
            GetSetterAd setter = (GetSetterAd) StaticVariables.listAds.get(position);

            listenerType = StaticVariables.comment;
            broadcasterUid = setter.getBroadcasterUid() + StaticVariables.commentBroad;
            broadcastName = setter.getTitle();
            profileLink = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");

            postComment = "Share your views and comments about the advert: <b>" +
                    setter.getTitle() + "</b>. Also address issues of concern to the advert broadcaster.";
            postTimestamp = setter.getBroadcastTime();
            title = setter.getTitle();
            postKey = setter.getAdKey();
            key = FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childComments).child(postKey).push().getKey();
        }
        else if (from.equals(StaticVariables.business)) {
            GetSetterBusiness setter = (GetSetterBusiness) StaticVariables.listBusinesses.get(position);

            listenerType = StaticVariables.review;
            broadcasterUid = setter.getUid() + StaticVariables.commentReview;
            broadcastName = username;
            profileLink = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");

            postComment = "Leave a review for <b>" + setter.getBrandName() + "</b>";
            postTimestamp = getCurrentTimestamp();
            title = setter.getBrandName();
            postKey = setter.getUid();
            key = FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childComments).child(postKey).push().getKey();
        }
        else if (from.equals(StaticVariables.comment)) {
            GetSetterBusiness setter = (GetSetterBusiness) StaticVariables.listComments.get(position);

            listenerType = StaticVariables.comment;
            broadcasterUid = setter.getUid() + StaticVariables.commentBroad;
            broadcastName = username;
            profileLink = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");

            postComment = "Comments for broadcasts made by <b>" + setter.getBrandName() + "</b>";
            postTimestamp = getCurrentTimestamp();
            title = setter.getBrandName();
            postKey = setter.getUid();
            key = FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childComments).child(postKey).push().getKey();
        }
        else if (from.equals(StaticVariables.review)) {
            GetSetterComments setter = (GetSetterComments) StaticVariables.listReview.get(position);

            listenerType = StaticVariables.review;
            broadcasterUid = setter.getUid() + StaticVariables.commentReview;
            broadcastName = username;
            profileLink = accType.equals(StaticVariables.individual) ?
                    prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, "") : prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, "");

            postComment = setter.getComment();
            postTimestamp = getCurrentTimestamp();
            title = "From: " + setter.getUsername();
            postKey = setter.getUid();
            key = FirebaseDatabase.getInstance().getReference()
                    .child(StaticVariables.childComments).child(postKey).push().getKey();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.viewSend:
                sendMessage();
                break;
        }
    }

    private void sendMessage() {
        if (etComment.getText().toString().trim().isEmpty())
            return;
        progressBar.setVisibility(View.VISIBLE);
        comment = etComment.getText().toString().trim();
        etComment.setText("");
        timestamp = getCurrentTimestamp();
        GetSetterComments setter = new
                GetSetterComments(broadcasterUid, uid, key, postKey, username, comment,
                timestamp, verified, broadcastName, profileLink, accType.equals(StaticVariables.individual)
                ? prefs.getInt(AppInfo.SIG_VER_INDI, 0) : prefs.getInt(AppInfo.SIG_VER_BUS, 0));

        if (StaticVariables.listCommentsInbox.size() == 0) {
            commentsAdapter.add(setter);
            listView.setAdapter(commentsAdapter);
        } else {
            commentsAdapter.add(setter);
            commentsAdapter.notifyDataSetChanged();
            listView.setSelection(listView.getAdapter().getCount() - 1);
        }
        listView.setVisibility(View.VISIBLE);
        sendMessageToDB(setter);
    }

    private void sendMessageToDB(final GetSetterComments setter) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childComments)
                .child(broadcasterUid)
                .child(postKey)
                .child(key)
                .setValue(setter)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.INVISIBLE);
                            new InsertComments(Comments.this).execute(setter);

                            String notKey = FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childNotificationsUser)
                                    .child(broadcasterUid)
                                    .push()
                                    .getKey();

                            GetSetterNotifications setterNotifications = new
                                    GetSetterNotifications(listenerType, key, broadcasterUid, postKey, false);

                            FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childNotificationsUser)
                                    .child(broadcasterUid)
                                    .child(notKey)
                                    .setValue(setterNotifications);
                        }
                    }
                });
    }



    private void loadAllComments() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childComments)
                .child(broadcasterUid)
                .child(postKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            if (StaticVariables.listCommentsInbox == null)
                                StaticVariables.listCommentsInbox = new ArrayList<>();

                            long count = 0;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                GetSetterComments setter = snapshot.getValue(GetSetterComments.class);
                                if (setter.getBroadcastUid() == null || setter.getComment() == null || setter.getKey() == null)
                                    continue;

                                if (commentKeys.contains(setter.getKey()))
                                    continue;

                                count++;
                                commentsAdapter.add(setter);
                                new InsertComments(Comments.this).execute(setter);
                            }

                            if (count != 0) {
                                listView.setVisibility(View.VISIBLE);
                                listView.setAdapter(commentsAdapter);
                                listView.setSelection(listView.getAdapter().getCount() - 1);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
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
                .child(StaticVariables.childComments)
                .child(broadcasterUid)
                .child(postKey)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        try {
                            if (StaticVariables.listCommentsInbox == null)
                                StaticVariables.listCommentsInbox = new ArrayList<>();

                            GetSetterComments setter = dataSnapshot.getValue(GetSetterComments.class);
                            if (setter.getBroadcastUid() == null || setter.getComment() == null || setter.getKey() == null)
                                return;

                            if (commentKeys.contains(setter.getKey()))
                                return;
                            if (setter.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                return;

                            commentsAdapter.add(setter);
                            commentsAdapter.notifyDataSetChanged();
                            listView.setSelection(listView.getAdapter().getCount() - 1);
                            new InsertComments(Comments.this).execute(setter);
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

    private class InsertComments extends AsyncTask<GetSetterComments, Void, Void> {

        Context context;

        public InsertComments(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(GetSetterComments... params) {
            GetSetterComments setter = params[0];
            String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

            if (!commentKeys.contains(setter.getKey())) {
                commentKeys.add(setter.getKey());

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
                values.put(DBContract.Comments.SIGNATURE_VERSION, "" + setter.getSignatureVersion());

                if (setter.getBroadcastUid() != null && setter.getComment() != null && setter.getKey() != null)
                    db.insert(DBContract.Comments.TABLE_NAME, null, values);
            }

            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            commentKeys = dbOperations.getCommentInboxKeys(db, localUid);

            return null;
        }

    }

    private class CommentsBackTask extends AsyncTask<Void, GetSetterComments, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Cursor cursor = dbOperations.getCommentsInbox(db, postKey, localUid);
            int count = cursor.getCount();
            if (count == 0)
                count = 1;

            GetSetterComments setter1 = new GetSetterComments(null, null, null, null,
                    "MyEvents App", postComment, postTimestamp, true, null, null, 0);
            publishProgress(setter1);

            String broadcasterUid, uid, key, postKey, username, comment, broadcastName, profileLink;
            long timestamp;
            boolean verified;

            while (cursor.moveToNext()) {
                broadcasterUid = cursor.getString(cursor.getColumnIndex(DBContract.Comments.BROADCASTER_UID));
                uid = cursor.getString(cursor.getColumnIndex(DBContract.Comments.UID));
                key = cursor.getString(cursor.getColumnIndex(DBContract.Comments.KEY));
                postKey = cursor.getString(cursor.getColumnIndex(DBContract.Comments.POST_KEY));
                username = cursor.getString(cursor.getColumnIndex(DBContract.Comments.USERNAME));
                comment = cursor.getString(cursor.getColumnIndex(DBContract.Comments.COMMENT));
                broadcastName = cursor.getString(cursor.getColumnIndex(DBContract.Comments.BROADCAST_NAME));
                profileLink = cursor.getString(cursor.getColumnIndex(DBContract.Comments.PLOFILE_LINK));
                timestamp = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBContract.Comments.TIMESTAMP)));
                String veri = cursor.getString(cursor.getColumnIndex(DBContract.Comments.VERIFIED));

                if (broadcasterUid == null || comment == null || key == null)
                    continue;

                if (veri.equals("yes"))
                    verified = true;
                else
                    verified = false;

                GetSetterComments setter = new GetSetterComments(broadcasterUid, uid, key, postKey, username,
                        comment, timestamp, verified, broadcastName, profileLink, 0);

                publishProgress(setter);
            }

            return count;
        }

        @Override
        protected void onProgressUpdate(GetSetterComments... values) {
            commentsAdapter.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count != 0) {
                listView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                listView.setAdapter(commentsAdapter);
                listView.setSelection(listView.getAdapter().getCount() - 1);
            } else {
                listView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
        }
    }







    private class CommentsAdapter extends ArrayAdapter {

        CommentsAdapter(Context context, int resource) {
            super(context, resource);
        }

        public void add(GetSetterComments object) {
            StaticVariables.listCommentsInbox.add(object);
            super.add(object);
        }

        @Override
        public int getCount() {
            return StaticVariables.listCommentsInbox.size();
        }

        @Override
        public Object getItem(int position) {
            return StaticVariables.listCommentsInbox.get(position);
        }

        @NonNull
        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            View row = convertView;
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final Holder holder;
            if (row == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = layoutInflater.inflate(R.layout.row_comments, parent, false);
                holder = new Holder();

                holder.ivVerified = (ImageView) row.findViewById(R.id.ivVerified);
                holder.tvUsername = (TextView) row.findViewById(R.id.tvUsername);
                holder.tvComment = (TextView) row.findViewById(R.id.tvComment);
                holder.tvTime = (TextView) row.findViewById(R.id.tvTime);
                holder.tvDate = (TextView) row.findViewById(R.id.tvDate);

                row.setTag(holder);
            } else {
                holder = (Holder) row.getTag();
            }

            final GetSetterComments setter = (GetSetterComments) getItem(position);

            holder.tvUsername.setText(setter.getUsername());
            holder.tvComment.setText(Html.fromHtml(setter.getComment()));
            holder.tvTime.setText(getDate(setter.getTimestamp(), "time"));
            holder.tvDate.setText(getDate(setter.getTimestamp(), "date"));
            if (setter.isVerified())
                holder.ivVerified.setVisibility(View.VISIBLE);
            else
                holder.ivVerified.setVisibility(View.GONE);

            return row;

        }

        private class Holder {
            ImageView ivVerified;
            TextView tvUsername, tvComment, tvTime, tvDate;
        }

        String getDate(long timeReceived, String type) {
            String[] monthsSmall = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeReceived);


            if (type.equals("time")) {
                return "" + getTheValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getTheValue(calendar.get(Calendar.MINUTE));
            } else {
                return "" + calendar.get(Calendar.DAY_OF_MONTH) + "-" + monthsSmall[calendar.get(Calendar.MONTH) +1] + "-" +
                        getYear(calendar.get(Calendar.YEAR));
            }
        }

        private String getTheValue(int value){
            String theValue = String.valueOf(value);
            if (theValue.length() == 1){
                return "0"+theValue;
            } else {
                return theValue;
            }
        }

        private String getYear(int year) {
            String year2 = String.valueOf(year);
            return year2.substring(2, 4);
        }

    }

}
