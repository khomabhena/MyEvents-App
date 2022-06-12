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
import com.bumptech.glide.signature.StringSignature;
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
public class FragmentComments extends Fragment {

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
    static AdapterComments adapterComments;
    String broadcasterUid;

    GetSetterUpdate setterUpdate;
    private static List listUpdate = new ArrayList<>();

    public FragmentComments() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        prefsApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        dbOperations = new DBOperations(getContext());
        db = dbOperations.getWritableDatabase();
        broadcasterUid = FirebaseAuth.getInstance().getCurrentUser().getUid() + StaticVariables.commentBroad;
        dataKeys = dbOperations.getCommentKeys(db, broadcasterUid, FirebaseAuth.getInstance().getCurrentUser().getUid());

        adapterComments = new AdapterComments(getContext(), R.layout.row_comments_business);

        listView = (ListView) view.findViewById(R.id.listView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);

        StaticVariables.listComments = new ArrayList<>();
        new CommentsBackTask().execute();

        return view;
    }

    private void initializeListView() {
        if (StaticVariables.listComments.size() != 0) {
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            listView.setAdapter(adapterComments);
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.setSelection(SCROLL_POS);
                }
            });
        } else {
            //listView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            //progressBar.setVisibility(View.GONE);
        }
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
            if (StaticVariables.listComments == null)
                StaticVariables.listComments = new ArrayList<>();

            String broadcasterUid, uid, key, postKey, username, comment, broadcastName, profileLink, id;
            int sigVer;
            long timestamp;
            boolean verified;

            while (cursor.moveToNext()) {
                id = cursor.getString(cursor.getColumnIndex(DBContract.Comments.ID));
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
                String ver = cursor.getString(cursor.getColumnIndex(DBContract.Comments.SIGNATURE_VERSION));

                if (ver == null)
                    sigVer = 0;
                else
                    sigVer = Integer.parseInt(ver);

                if (veri.equals("yes"))
                    verified = true;
                else
                    verified = false;

                GetSetterComments setter = new GetSetterComments(broadcasterUid, uid, key, postKey, username,
                        comment, timestamp, verified, broadcastName, profileLink, sigVer);

                setterUpdate = new GetSetterUpdate("business", id, uid, "", "");
                listUpdate.add(setterUpdate);

                publishProgress(setter);
            }

            return count;
        }

        @Override
        protected void onProgressUpdate(GetSetterComments... values) {
            adapterComments.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count != 0) {
                listView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.GONE);
                listView.setAdapter(adapterComments);
                //listView.setSelection(listView.getAdapter().getCount() - 1);
                updateValues();
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
            }
            loadAllComments();
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

                                    for (int y = 0; y < StaticVariables.listComments.size(); y++) {
                                        GetSetterComments setterComments = (GetSetterComments) StaticVariables.listComments.get(y);
                                        if (setterComments.getUid().equals(setterUpdate.getUid())) {
                                            setterComments.setUsername(setterBusiness.getBrandName());
                                            setterComments.setProfileLink(setterBusiness.getProfileLink());
                                            setterComments.setVerified(setterBusiness.isVerified());
                                            setterComments.setSignatureVersion(setterBusiness.getSignatureVersion());
                                            adapterComments.notifyDataSetChanged();
                                        }
                                    }

                                    ContentValues values = new ContentValues();
                                    values.put(DBContract.Comments.USERNAME, setterBusiness.getBrandName());
                                    values.put(DBContract.Comments.PLOFILE_LINK, setterBusiness.getProfileLink());
                                    values.put(DBContract.Comments.VERIFIED, setterBusiness.isVerified() ? "yes": "no");
                                    values.put(DBContract.Comments.SIGNATURE_VERSION, "" + setterBusiness.getSignatureVersion());

                                    db.update(DBContract.Comments.TABLE_NAME, values,
                                            DBContract.Comments.ID + "='" + setterUpdate.getDbId() + "'",
                                            null);
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

                                    for (int y = 0; y < StaticVariables.listComments.size(); y++) {
                                        GetSetterComments setterComments = (GetSetterComments) StaticVariables.listComments.get(y);
                                        if (setterComments.getUid().equals(setterUpdate.getUid())) {
                                            setterComments.setUsername(setterBusiness.getUsername());
                                            setterComments.setProfileLink(setterBusiness.getProfileLink());
                                            setterComments.setSignatureVersion(setterBusiness.getSignatureVersion());
                                            adapterComments.notifyDataSetChanged();
                                        }
                                    }

                                    ContentValues values = new ContentValues();
                                    values.put(DBContract.Comments.USERNAME, setterBusiness.getUsername());
                                    values.put(DBContract.Comments.PLOFILE_LINK, setterBusiness.getProfileLink());
                                    values.put(DBContract.Comments.SIGNATURE_VERSION, "" + setterBusiness.getSignatureVersion());

                                    db.update(DBContract.Comments.TABLE_NAME, values,
                                            DBContract.Comments.ID + "='" + setterUpdate.getDbId() + "'",
                                            null);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
                                if (StaticVariables.listComments.size() == 0)
                                    adapterComments.add(setterComments);
                                else
                                    StaticVariables.listComments.add(0, setterComments);

                                new InsertComments(getContext()).execute(setterComments);
                            }
                        }
                        if (count == 0)
                            return;
                        listView.setAdapter(adapterComments);
                        adapterComments.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //
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
                values.put(DBContract.Comments.SIGNATURE_VERSION, "" + setter.getSignatureVersion());
                values.put(DBContract.Comments.TIMESTAMP, String.valueOf(setter.getTimestamp()));

                db.insert(DBContract.Comments.TABLE_NAME, null, values);
            }

            dataKeys = dbOperations.getCommentKeys(db, broadcasterUid, FirebaseAuth.getInstance().getCurrentUser().getUid());

            return null;
        }

    }








    public class AdapterComments extends ArrayAdapter {

        AdapterComments(Context context, int resource) {
            super(context, resource);
        }

        SharedPreferences prefs;

        public void add(GetSetterComments object) {
            StaticVariables.listComments.add(object);
            super.add(object);
        }

        @Override
        public int getCount() {
            return StaticVariables.listComments.size();
        }

        @Override
        public Object getItem(int position) {
            return StaticVariables.listComments.get(position);
        }

        @NonNull
        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            View row = convertView;
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final Holder holder;
            if (row == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = layoutInflater.inflate(R.layout.row_comments_business, parent, false);
                holder = new Holder();

                holder.tvUsername = (TextView) row.findViewById(R.id.tvUsername);
                holder.tvMessage = (TextView) row.findViewById(R.id.tvMessage);
                holder.tvTimestamp = (TextView) row.findViewById(R.id.tvTimestamp);
                holder.constRow = (ConstraintLayout) row.findViewById(R.id.constRow);
                holder.ivProfile = (ImageView) row.findViewById(R.id.ivProfile);
                holder.ivVerified = (ImageView) row.findViewById(R.id.ivVerified);
                holder.tvBroadcastName = row.findViewById(R.id.tvBroadcastName);

                row.setTag(holder);
            } else {
                holder = (Holder) row.getTag();
            }

            final GetSetterComments setter = (GetSetterComments) getItem(position);
            String uidd = FirebaseAuth.getInstance().getCurrentUser().getUid();
            prefs = getContext().getSharedPreferences(AppInfo.USER_INFO + uidd, Context.MODE_PRIVATE);

            try {
                StorageReference storageReference;
                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getProfileLink());

                Glide.with(getContext())
                        .using(new FirebaseImageLoader())
                        .load(storageReference)
                        .signature(new StringSignature("" + setter.getSignatureVersion()))
                        .crossFade()
                        .into(holder.ivProfile);
            } catch (Exception e) {
                //
            }

            holder.tvUsername.setText(setter.getUsername());
            holder.tvMessage.setText(setter.getComment());
            holder.tvBroadcastName.setText(setter.getBroadcastName());

            if (setter.isVerified())
                holder.ivVerified.setVisibility(View.VISIBLE);
            else
                holder.ivVerified.setVisibility(View.GONE);

            holder.tvTimestamp.setText(getDate(setter.getTimestamp()));

            /*holder.constRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), Inbox.class);
                    intent.putExtra("from", StaticVariables.comment);
                    intent.putExtra("position", position);
                    getContext().startActivity(intent);
                }
            });*/

            return row;

        }

        private class Holder {
            ImageView ivProfile, ivVerified;
            TextView tvUsername, tvMessage, tvTimestamp, tvBroadcastName;
            ConstraintLayout constRow;
        }


        String getDate(long timeReceived) {
            String[] monthsSmall = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(getCurrentTimestamp());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);

            Calendar calendarR = Calendar.getInstance();
            calendarR.setTimeInMillis(timeReceived);
            calendarR.set(Calendar.HOUR_OF_DAY, 0);
            calendarR.set(Calendar.MINUTE, 0);

            long midnight = calendar.getTimeInMillis();
            long midnightReceiver = calendarR.getTimeInMillis();

            if (midnight == midnightReceiver) {
                calendar.setTimeInMillis(timeReceived);
                return "" + getTheValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getTheValue(calendar.get(Calendar.MINUTE));
            } else {
                calendar.setTimeInMillis(timeReceived);
                return "" + getTheValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getTheValue(calendar.get(Calendar.MINUTE)) + ", " +
                        calendar.get(Calendar.DAY_OF_MONTH) + "-" + monthsSmall[calendar.get(Calendar.MONTH) +1] + "-" +
                        getYear(calendar.get(Calendar.YEAR));
            }
            //

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
