package tech.myevents.app;


import android.*;
import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class FragmentMoment extends Fragment {

    ListView listView;
    TextView tvEmpty;
    ProgressBar progressBar;

    SharedPreferences prefs;
    SharedPreferences prefsApp;
    SharedPreferences.Editor editor;

    static int SCROLL_POS = 0;
    static long LAST_LOAD = 0;
    static boolean NETWORK_AVAILABLE = false;
    static boolean PAUSED = false;

    DBOperations dbOperations;
    SQLiteDatabase db;
    static List<String> dataKey;
    FirebaseAuth auth;
    FirebaseUser firebaseUser = null;


    static MomentsAdapter momentsAdapter;
    static ChildEventListener listener;
    static String contactList = "";

    GetSetterUpdate setterUpdate;
    private static List listUpdate = new ArrayList<>();

    public FragmentMoment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_moments, container, false);

        dbOperations = new DBOperations(getContext());
        db = dbOperations.getWritableDatabase();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dataKey = dbOperations.getMomentKeys(db, localUid);
        auth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getContext().getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
        prefsApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);

        contactList = prefs.getString(AppInfo.CONTACT_LIST, "");
        contactList += prefs.getInt(AppInfo.CONTACT_NUMBER, 0);
        momentsAdapter = new MomentsAdapter(getContext(), R.layout.row_moments);

        listView = (ListView) view.findViewById(R.id.listView);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        if (!prefs.getBoolean(AppInfo.CONTACT_VERIFIED, false)) {
            progressBar.setVisibility(View.GONE);
            tvEmpty.setText("Setup your contact details to see content shared by friends, click to get started!!!" + "" + "");
            tvEmpty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(), ContactNumber.class));
                }
            });
        } else {
            if (contactList.equals(""))
                getContactsPermissions();
            if (StaticVariables.listMoments != null)
                initializeListView();
            else
                new MomentsBackTask().execute();
        }

        return view;
    }


    //TODO get moments from businesses you're interested in
    //TODO moments are being called from MainActivity when Moment tab is selected


    private void getContactsPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_CONTACTS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
            getContactList();
    }

    private void getContactList() {
        Cursor c = getContext()
                .getContentResolver()
                .query(ContactsContract
                        .CommonDataKinds
                        .Phone.CONTENT_URI, null, null, null, ContactsContract
                        .Contacts.DISPLAY_NAME + " ASC");
        String contacts = "CONTACTS\n" + StaticVariables.adminEcocashNumber + "\n";
        if (c != null) {
            while (c.moveToNext()) {
                //String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                if (phoneNumber.length() < 9)
                    continue;
                int num = phoneNumber.length();
                phoneNumber = phoneNumber.substring(num - 9, num);
                contacts += " " + phoneNumber + "\n";
            }
            c.close();
        }
        editor = prefs.edit();
        editor.putString(AppInfo.CONTACT_LIST, contacts);
        editor.apply();
        //Toast.makeText(getApplicationContext(), contacts, Toast.LENGTH_LONG).show();
    }


    private void initializeListView() {
        if (StaticVariables.listMoments.size() != 0) {
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            listView.setAdapter(momentsAdapter);
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.setSelection(SCROLL_POS);
                }
            });
            //updateValues();
        } else {
            //listView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            //progressBar.setVisibility(View.GONE);
        }
        //addFirebaseListener();
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



    private class MomentsBackTask extends AsyncTask<Void, GetSetterMoment, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Cursor cursor = dbOperations.getMoments(db, localUid);
            int count = cursor.getCount();

            if (count != 0)
                StaticVariables.listMoments = new ArrayList<>();

            String key, type, username, story, link, profileLink, id, uid;
            long timestamp;
            int contactNumber, sigVer;

            while (cursor.moveToNext()) {
                id = cursor.getString(cursor.getColumnIndex(DBContract.Moments.ID));
                key = cursor.getString(cursor.getColumnIndex(DBContract.Moments.KEY));
                type = cursor.getString(cursor.getColumnIndex(DBContract.Moments.TYPE));
                username = cursor.getString(cursor.getColumnIndex(DBContract.Moments.USERNAME));
                story = cursor.getString(cursor.getColumnIndex(DBContract.Moments.STORY));

                link = cursor.getString(cursor.getColumnIndex(DBContract.Moments.MOMENT_LINK));
                profileLink = cursor.getString(cursor.getColumnIndex(DBContract.Moments.PROFILE_LINK));
                timestamp = Long.parseLong(cursor.getString(cursor.getColumnIndex(DBContract.Moments.TIMESTAMP)));
                contactNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Moments.CONTACT_NUMBER)));
                sigVer = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Moments.SIGNATURE_VERSION)));

                if (timestamp + (86400000 * 7) < getCurrentTimestamp())
                    continue;

                GetSetterMoment setter = new GetSetterMoment(key, type, username, story, link,
                        timestamp, profileLink, contactNumber, sigVer);

                setterUpdate = new GetSetterUpdate("business", id, "" + contactNumber, "", "");
                listUpdate.add(setterUpdate);

                publishProgress(setter);
            }
            cursor.close();

            return count;
        }

        @Override
        protected void onProgressUpdate(GetSetterMoment... values) {
            momentsAdapter.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count != 0) {
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
                listView.setAdapter(momentsAdapter);
                LAST_LOAD = getCurrentTimestamp();
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        listView.setSelection(SCROLL_POS);
                    }
                });
                updateValues();
            } else {
                //progressBar.setVisibility(View.GONE);
                //listView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
            addFirebaseListener();
        }

    }

    private void updateValues() {
        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childUsers)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        for (DataSnapshot snap: dataSnapshot.getChildren()) {
                            GetSetterUser setterUser = snap.getValue(GetSetterUser.class);

                            for (int x =0; x < listUpdate.size(); x++) {
                                GetSetterUpdate setterUpdate = (GetSetterUpdate) listUpdate.get(x);
                                if (setterUpdate.getUid().equals(String.valueOf(setterUser.getContactNumber()))) {

                                    for (int y = 0; y < StaticVariables.listMoments.size(); y++) {
                                        GetSetterMoment setterMoment = (GetSetterMoment) StaticVariables.listMoments.get(y);
                                        if (setterMoment.getContactNumber() == Integer.parseInt(setterUpdate.getUid())) {
                                            setterMoment.setUsername(setterUser.getUsername());
                                            setterMoment.setProfileLink(setterUser.getProfileLink());
                                            setterMoment.setSignatureVersion(setterUser.getSignatureVersion());
                                            momentsAdapter.notifyDataSetChanged();
                                        }
                                    }

                                    ContentValues values = new ContentValues();
                                    values.put(DBContract.Moments.USERNAME, setterUser.getUsername());
                                    values.put(DBContract.Moments.PROFILE_LINK, setterUser.getProfileLink());
                                    values.put(DBContract.Moments.SIGNATURE_VERSION, "" + setterUser.getSignatureVersion());

                                    db.update(DBContract.Moments.TABLE_NAME, values,
                                            DBContract.Moments.ID + "='" + setterUpdate.getDbId() + "'",
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

    private void addFirebaseListener() {
        final String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
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

                                if (dataKey.contains(setter.getKey()))
                                    continue;

                                if (!email.equals(StaticVariables.adminEmail) && !email.equals("colwanymab@gmail.com"))
                                    if (!contactList.contains("" + setter.getContactNumber()))
                                        continue;

                                if (StaticVariables.listMoments == null)
                                    StaticVariables.listMoments = new ArrayList<>();

                                if (!setter.getType().equals(StaticVariables.deleted))
                                    if (StaticVariables.listMoments.size() == 0) {
                                        tvEmpty.setVisibility(View.GONE);
                                        progressBar.setVisibility(View.GONE);
                                        listView.setVisibility(View.VISIBLE);

                                        momentsAdapter.add(setter);
                                        listView.setAdapter(momentsAdapter);
                                    } else {
                                        StaticVariables.listMoments.add(0, setter);
                                        momentsAdapter.notifyDataSetChanged();
                                    }

                                new InsertMoments(getContext()).execute(setter);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private class InsertMoments extends AsyncTask<GetSetterMoment, Void, Void> {

        Context context;

        public InsertMoments(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(GetSetterMoment... params) {
            GetSetterMoment setter = params[0];
            String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

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

            if (!dataKey.contains(setter.getKey()))
                db.insert(DBContract.Moments.TABLE_NAME, null, values);
            else
                db.update(DBContract.Moments.TABLE_NAME, values,
                        DBContract.Moments.KEY + "='" + setter.getKey() + "'", null);

            addImpression(setter.getKey());
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            dataKey = dbOperations.getMomentKeys(db, localUid);
            return null;
        }

    }

    private void addImpression(String eventKey) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences prefApp = getContext().getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        String accType = prefApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

        String key = FirebaseDatabase.getInstance().getReference().child(StaticVariables.childImpressions).child(eventKey).child(uid).push().getKey();
        String username = prefs.getString(AppInfo.BUSINESS_BRAND_NAME, prefs.getString(AppInfo.INDIVIDUAL_USERNAME, "..."));
        String profileLink = prefs.getString(AppInfo.BUSINESS_PROFILE_LINK, prefs.getString(AppInfo.INDIVIDUAL_PROFILE_LINK, ""));
        String location = prefs.getString(AppInfo.BUSINESS_LOCATION, prefs.getString(AppInfo.INDIVIDUAL_LOCATION, "No location"));
        int sigver = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual).equals(StaticVariables.individual) ?
                prefs.getInt(AppInfo.SIG_VER_INDI, 0) : prefs.getInt(AppInfo.SIG_VER_BUS, 0);

        GetSetterImpression setter = new GetSetterImpression(accType, uid, key, username, profileLink, location,
                false, getCurrentTimestamp(), sigver);

        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childImpressions)
                .child(eventKey)
                .child(uid)
                .child(key)
                .setValue(setter);
    }






    public class MomentsAdapter extends ArrayAdapter {

        MomentsAdapter(Context context, int resource) {
            super(context, resource);
        }

        public void add(GetSetterMoment object) {
            StaticVariables.listMoments.add(object);
            super.add(object);
        }

        @Override
        public int getCount() {
            return StaticVariables.listMoments.size();
        }

        @Override
        public Object getItem(int position) {
            return StaticVariables.listMoments.get(position);
        }

        @NonNull
        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            View row = convertView;
            final Holder holder;
            if (row == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = layoutInflater.inflate(R.layout.row_moments, parent, false);
                holder = new Holder();

                holder.ivLinkEvent = (ImageView) row.findViewById(R.id.ivLinkEvent);
                holder.ivProfile = (ImageView) row.findViewById(R.id.ivProfile);
                holder.tvUsername = (TextView) row.findViewById(R.id.tvUsername);
                holder.tvMoment = (TextView) row.findViewById(R.id.tvMoment);
                holder.tvTime = (TextView) row.findViewById(R.id.tvTime);
                holder.tvStory = (TextView) row.findViewById(R.id.tvStory);
                holder.cardRow = (CardView) row.findViewById(R.id.cardRow);
                holder.ivLinkAd = (ImageView) row.findViewById(R.id.ivLinkAd);
                holder.ivDelete = row.findViewById(R.id.ivDelete);

                row.setTag(holder);
            } else {
                holder = (Holder) row.getTag();
            }

            final GetSetterMoment setter = (GetSetterMoment) getItem(position);

            insertImage(setter.getType(), holder, setter.getLink());

            if (setter.getStory().equals(""))
                holder.tvStory.setVisibility(View.GONE);
            else
                holder.tvStory.setVisibility(View.VISIBLE);

            try {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getProfileLink());
                Glide.with(getContext())
                        .using(new FirebaseImageLoader())
                        .load(storageReference)
                        .signature(new StringSignature("" + setter.getSignatureVersion()))
                        .crossFade()
                        .into(holder.ivProfile);
            } catch (Exception e) {
                holder.ivProfile.setImageResource(0);
            }
            if (!setter.getLink().equals("")) {
                try {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getLink());
                    Glide.with(getContext())
                            .using(new FirebaseImageLoader())
                            .load(storageReference)
                            .crossFade()
                            .into(holder.ivLinkEvent);
                } catch (Exception e) {
                    holder.ivLinkEvent.setVisibility(View.GONE);
                }
            }

            holder.tvUsername.setText(setter.getUsername());
            if (setter.getType().equals(StaticVariables.moment))
                holder.tvMoment.setText("Shared this moment" + "" + "");
            if (setter.getType().equals(StaticVariables.event))
                holder.tvMoment.setText("Shared this event" + "" + "");
            if (setter.getType().equals(StaticVariables.ad))
                holder.tvMoment.setText("Shared this advert" + "" + "");

            holder.tvTime.setText(getDate(setter.getTimestamp()));
            holder.tvStory.setText(setter.getStory());
            holder.cardRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*GetSetterMoment setterZ = (GetSetterMoment) StaticVariables.listMoments.get(position);
                    Intent intent = new Intent(getContext(), Impressions.class);
                    intent.putExtra("key", setterZ.getKey());
                    getContext().startActivity(intent);*/
                }
            });
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final GetSetterMoment setter = (GetSetterMoment) StaticVariables.listMoments.get(position);
                    if (setter.getContactNumber() == prefs.getInt(AppInfo.CONTACT_NUMBER, 45850)) {
                        Toast.makeText(getContext(), "Moment deleted", Toast.LENGTH_SHORT).show();
                        setter.setType(StaticVariables.deleted);
                        FirebaseDatabase.getInstance().getReference()
                                .child(StaticVariables.childMoments)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(setter.getKey())
                                .setValue(setter)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            StaticVariables.listMoments.remove(position);
                                            momentsAdapter.notifyDataSetChanged();

                                            ContentValues values = new ContentValues();
                                            values.put(DBContract.Moments.TYPE, StaticVariables.deleted);
                                            db.update(DBContract.Moments.TABLE_NAME, values, DBContract.Moments.KEY + "='" + setter.getKey() + "'", null);
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(), "Moment hidden", Toast.LENGTH_SHORT).show();
                        ContentValues values = new ContentValues();
                        values.put(DBContract.Moments.TYPE, StaticVariables.deleted);
                        db.update(DBContract.Moments.TABLE_NAME, values, DBContract.Moments.KEY + "='" + setter.getKey() + "'", null);
                        StaticVariables.listMoments.remove(position);
                        momentsAdapter.notifyDataSetChanged();
                    }
                }
            });

            return row;

        }


        private class Holder {
            ImageView ivLinkAd, ivLinkEvent, ivProfile, ivDelete;
            TextView tvUsername, tvMoment, tvTime, tvStory;
            CardView cardRow;
        }


        String getDate(long timeReceived) {
            String[] monthsSmall = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(getCurrentTimestamp());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);

            long midnight = calendar.getTimeInMillis();

            if (midnight > getCurrentTimestamp()) {
                calendar.setTimeInMillis(timeReceived);
                return "" + getTheValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getTheValue(calendar.get(Calendar.MINUTE));
            } else {
                calendar.setTimeInMillis(timeReceived);
                return "" + getTheValue(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getTheValue(calendar.get(Calendar.MINUTE)) + ",   " +
                        calendar.get(Calendar.DAY_OF_MONTH) + "-" + monthsSmall[calendar.get(Calendar.MONTH) +1] + "-" +
                        calendar.get(Calendar.YEAR);
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

        private void insertImage(String type, MomentsAdapter.Holder holder, String link) {
            if (type.equals(StaticVariables.event)) {
                holder.ivLinkAd.setVisibility(View.GONE);
                holder.ivLinkEvent.setVisibility(View.VISIBLE);

                try {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(link);
                    Glide.with(getContext())
                            .using(new FirebaseImageLoader())
                            .load(storageReference)
                            .crossFade()
                            .into(holder.ivLinkEvent);
                } catch (Exception e) {
                    //
                }
            } else if (type.equals(StaticVariables.ad)) {
                holder.ivLinkEvent.setVisibility(View.GONE);
                holder.ivLinkAd.setVisibility(View.VISIBLE);

                try {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(link);
                    Glide.with(getContext())
                            .using(new FirebaseImageLoader())
                            .load(storageReference)
                            .crossFade()
                            .into(holder.ivLinkAd);
                } catch (Exception e) {
                    //
                }

            } else if (type.equals(StaticVariables.moment)) {
                holder.ivLinkAd.setVisibility(View.GONE);
                if (link.equals(""))
                    holder.ivLinkEvent.setVisibility(View.GONE);
                else {
                    holder.ivLinkEvent.setVisibility(View.VISIBLE);

                    try {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(link);
                        Glide.with(getContext())
                                .using(new FirebaseImageLoader())
                                .load(storageReference)
                                .crossFade()
                                .into(holder.ivLinkEvent);
                    } catch (Exception e) {
                        //
                    }
                }
            }
        }

    }


}
