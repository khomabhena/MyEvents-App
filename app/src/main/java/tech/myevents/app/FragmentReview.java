package tech.myevents.app;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentReview extends Fragment {

    ListView listView;
    ProgressBar progressBar;
    TextView tvEmpty;

    SharedPreferences prefsApp;

    DBOperations dbOperations;
    SQLiteDatabase db;
    List<String> dataKeys;

    static int SCROLL_POS = 0;
    static boolean NETWORK_AVAILABLE = false;
    static boolean DB_LISTENING = false;

    ChildEventListener listener;
    //AdapterReviews adapterReviews;
    String broadcasterUid;
    static AdapterReview adapterReview;

    public FragmentReview() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        prefsApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        dbOperations = new DBOperations(getContext());
        db = dbOperations.getWritableDatabase();
        broadcasterUid = FirebaseAuth.getInstance().getCurrentUser().getUid() + StaticVariables.commentReview;
        dataKeys = dbOperations.getCommentKeys(db, broadcasterUid, FirebaseAuth.getInstance().getCurrentUser().getUid());

        adapterReview = new AdapterReview(getContext(), R.layout.row_comments);

        listView = (ListView) view.findViewById(R.id.listView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);

        StaticVariables.listReview = new ArrayList<>();
        new CommentsBackTask().execute();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        SCROLL_POS = listView.getFirstVisiblePosition();
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

    private class CommentsBackTask extends AsyncTask<Void, GetSetterComments, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Cursor cursor = dbOperations.getReviews(db, broadcasterUid, localUid);

            int count = cursor.getCount();
            if (StaticVariables.listReview == null)
                StaticVariables.listReview = new ArrayList<>();

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
            adapterReview.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count != 0) {
                listView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.GONE);
                listView.setAdapter(adapterReview);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
            }
            loadAllComments();
        }

    }

    private void loadAllComments() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childComments)
                .child(broadcasterUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        int count = 0;
                        for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                            for (DataSnapshot snapshot : dataSnapshot1.getChildren()) {
                                GetSetterComments setterComments = snapshot.getValue(GetSetterComments.class);

                                if (dataKeys.contains(setterComments.getKey()))
                                    continue;
                                count++;
                                tvEmpty.setVisibility(View.GONE);

                                if (StaticVariables.listReview.size() == 0)
                                    adapterReview.add(setterComments);
                                else
                                    StaticVariables.listReview.add(0, setterComments);

                                new InsertComments(getContext()).execute(setterComments);
                            }
                        }
                        if (count == 0)
                            return;
                        listView.setAdapter(adapterReview);
                        adapterReview.notifyDataSetChanged();
                        loadInboxListener();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //
                    }
                });
    }

    private void loadInboxListener() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childComments)
                .child(broadcasterUid)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (!dataSnapshot.exists())
                            return;
                        try {
                            int count = 0;
                            for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                                for (DataSnapshot snapshot : dataSnapshot1.getChildren()) {
                                    GetSetterComments setterComments = snapshot.getValue(GetSetterComments.class);

                                    if (dataKeys.contains(setterComments.getKey()))
                                        continue;
                                    count++;
                                    tvEmpty.setVisibility(View.GONE);
                                    if (StaticVariables.listReview.size() == 0)
                                        adapterReview.add(setterComments);
                                    else
                                        StaticVariables.listReview.add(0, setterComments);

                                    new InsertComments(getContext()).execute(setterComments);
                                }
                            }
                            if (count == 0)
                                return;
                            listView.setAdapter(adapterReview);
                            adapterReview.notifyDataSetChanged();
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

    private class InsertComments extends AsyncTask<GetSetterComments, Void, Void> {

        Context context;

        public InsertComments(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(GetSetterComments... params) {
            GetSetterComments setter = params[0];
            String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

            if (!dataKeys.contains(setter.getKey())) {
                //makeAdImpression(setterEvent);

                ContentValues values = new ContentValues();
                values.put(DBContract.Comments.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid()  + type);
                values.put(DBContract.Comments.BROADCASTER_UID, setter.getBroadcastUid());
                values.put(DBContract.Comments.BROADCAST_NAME, setter.getBroadcastName());
                values.put(DBContract.Comments.PLOFILE_LINK, setter.getProfileLink());
                values.put(DBContract.Comments.UID, setter.getUid());
                values.put(DBContract.Comments.KEY, setter.getKey());
                values.put(DBContract.Comments.POST_KEY, setter.getPostKey());
                values.put(DBContract.Comments.USERNAME, setter.getUsername());
                values.put(DBContract.Comments.COMMENT, setter.getComment());
                values.put(DBContract.Comments.VERIFIED, setter.isVerified() ? "yes": "no");
                values.put(DBContract.Comments.TIMESTAMP, String.valueOf(setter.getTimestamp()));

                db.insert(DBContract.Comments.TABLE_NAME, null, values);
            }

            dataKeys = dbOperations.getCommentKeys(db, broadcasterUid, FirebaseAuth.getInstance().getCurrentUser().getUid());

            return null;
        }

    }








    public class AdapterReview extends ArrayAdapter {

        AdapterReview(Context context, int resource) {
            super(context, resource);
        }

        SharedPreferences prefs;

        public void add(GetSetterComments object) {
            StaticVariables.listReview.add(object);
            super.add(object);
        }

        @Override
        public int getCount() {
            return StaticVariables.listReview.size();
        }

        @Override
        public Object getItem(int position) {
            return StaticVariables.listReview.get(position);
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
                holder.tvComment =  (TextView) row.findViewById(R.id.tvComment);
                holder.tvTime = (TextView) row.findViewById(R.id.tvTime);
                holder.tvDate = (TextView) row.findViewById(R.id.tvDate);

                row.setTag(holder);
            } else {
                holder = (Holder) row.getTag();
            }

            final GetSetterComments setter = (GetSetterComments) getItem(position);
            String uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();
            prefs = getContext().getSharedPreferences(AppInfo.USER_INFO + uidU, Context.MODE_PRIVATE);

            holder.tvUsername.setText(setter.getUsername());
            holder.tvComment.setText(setter.getComment().replace("\n", " "));
            if (setter.isVerified())
                holder.ivVerified.setVisibility(View.VISIBLE);
            else
                holder.ivVerified.setVisibility(View.GONE);
            holder.tvTime.setText(getDate(setter.getTimestamp(), "time"));
            holder.tvDate.setText(getDate(setter.getTimestamp(), "date"));

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

        private String getYear(int year) {
            String year2 = String.valueOf(year);
            return year2.substring(2, 4);
        }

        public String getTheValue(int value){
            String theValue = String.valueOf(value);
            if (theValue.length() == 1){
                return "0"+theValue;
            } else {
                return theValue;
            }
        }

        private String getImpression(String impression) {
            char[] aud = impression.toCharArray();
            int len = impression.length();
            StringBuilder builder = new StringBuilder();
            for (int x = 0; x < aud.length; x++) {
                if (x == len - 9) {
                    if (len != 9) {
                        builder.append(",");
                    }
                }
                if (x == len - 6) {
                    if (len != 6) {
                        builder.append(",");
                    }
                }
                if (x == len - 3) {
                    if (len != 3) {
                        builder.append(",");
                    }
                }
                builder.append(aud[x]);
            }

            return String.valueOf(builder);
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

}
