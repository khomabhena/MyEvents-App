package tech.myevents.app;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static tech.myevents.app.MainActivity.currentPage;

public class TicketSend extends AppCompatActivity implements View.OnClickListener{

    ListView listView;
    TextView tvEmpty;
    ProgressBar progressBar, progressBarSend;

    SharedPreferences prefs;
    SharedPreferences prefsApp;
    SharedPreferences.Editor editor;
    DBOperations dbOperations;
    SQLiteDatabase db;
    List<String> friendKeys;
    FriendsAdapter friendsAdapter;
    AlertDialog.Builder builder;

    GetSetterTicket setterTickets;
    int positionTicket;
    private static final int REQUEST_READ_CONTACTS = 1994;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_ticket_contact);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = getSharedPreferences(AppInfo.USER_INFO + uid, Context.MODE_PRIVATE);
        prefsApp = getSharedPreferences(AppInfo.APP_INFO, Context.MODE_PRIVATE);
        dbOperations = new DBOperations(TicketSend.this);
        db = dbOperations.getWritableDatabase();
        String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendKeys = dbOperations.getFriendKeys(db, localUid);
        positionTicket = getIntent().getExtras().getInt("position");
        setterTickets = (GetSetterTicket) StaticVariables.listTickets.get(positionTicket);

        friendsAdapter = new FriendsAdapter(this, R.layout.row_contacts);

        listView = (ListView) findViewById(R.id.listView);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBarSend = (ProgressBar) findViewById(R.id.progressBarSend);

        getContactsList();

        String pageName = "Choose Ticket Contact";
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

    @Override
    protected void onResume() {
        super.onResume();
        StaticVariables.listFriends = new ArrayList<>();
        new FriendsBackTask().execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabSettings:
                startActivity(new Intent(this, ContactNumber.class));
                break;
        }
    }

    private void getContactsList() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
            getContacts();
        else
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
    }

    private void getContacts() {
        Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
        String contacts = "CONTACTS\n775523763\n";
        if (c != null) {
            while (c.moveToNext()) {
                String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
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
    }



    private class FriendsBackTask extends AsyncTask<Void, GetSetterUser, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Cursor cursor = dbOperations.getFriends(db, localUid);
            int count = cursor.getCount();

            String uid, username, profileLink;
            int contactNumber;

            while (cursor.moveToNext()) {
                uid = cursor.getString(cursor.getColumnIndex(DBContract.Friends.UID));
                username = cursor.getString(cursor.getColumnIndex(DBContract.Friends.USERNAME));
                profileLink = cursor.getString(cursor.getColumnIndex(DBContract.Friends.PROFILE_LINK));
                contactNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBContract.Friends.CONTACT_NUMBER)));

                GetSetterUser setter = new GetSetterUser(uid, username, "", "", profileLink, "", 0, contactNumber, 0);
                publishProgress(setter);
            }

            return count;
        }

        @Override
        protected void onProgressUpdate(GetSetterUser... values) {
            friendsAdapter.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer count) {
            if (count != 0) {
                listView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.GONE);
                listView.setAdapter(friendsAdapter);
                listView.setSelection(listView.getAdapter().getCount() - 1);
            } else {
                listView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
            loadAllContacts();
        }

    }

    private void loadAllContacts() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(StaticVariables.childUsers)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (StaticVariables.listFriends == null)
                            StaticVariables.listFriends = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            GetSetterUser setter = snapshot.getValue(GetSetterUser.class);


                            if (setter.getContactNumber() == prefs.getInt(AppInfo.CONTACT_NUMBER, 0))
                                continue;

                            if (!friendKeys.contains("" + setter.getContactNumber()))
                                if (StaticVariables.listFriends.size() == 0) {
                                    tvEmpty.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);

                                    friendsAdapter.add(setter);
                                    listView.setAdapter(friendsAdapter);
                                } else {
                                    StaticVariables.listFriends.add(0, setter);
                                    friendsAdapter.notifyDataSetChanged();
                                }

                            new InsertFriends(TicketSend.this).execute(setter);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //
                    }
                });
    }

    private class InsertFriends extends AsyncTask<GetSetterUser, Void, Void> {

        Context context;

        public InsertFriends(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(GetSetterUser... params) {
            GetSetterUser setter = params[0];
            String type = prefsApp.getString(AppInfo.ACCOUNT_TYPE, StaticVariables.individual);

            ContentValues values = new ContentValues();
            values.put(DBContract.Friends.LOCAL_UID, FirebaseAuth.getInstance().getCurrentUser().getUid() + type);
            values.put(DBContract.Friends.UID, setter.getUid());
            values.put(DBContract.Friends.USERNAME, setter.getUsername());
            values.put(DBContract.Friends.PROFILE_LINK, setter.getProfileLink());
            values.put(DBContract.Friends.CONTACT_NUMBER, "" + setter.getContactNumber());

            if (!friendKeys.contains("" + setter.getContactNumber()))
                db.insert(DBContract.Friends.TABLE_NAME, null, values);
            else
                db.update(DBContract.Friends.TABLE_NAME, values, DBContract.Friends.UID + "='" + setter.getUid() + "'", null);

            String localUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            friendKeys = dbOperations.getFriendKeys(db, localUid);

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




    private class FriendsAdapter extends ArrayAdapter {

        FriendsAdapter(Context context, int resource) {
            super(context, resource);
        }

        public void add(GetSetterUser object) {
            StaticVariables.listFriends.add(object);
            super.add(object);
        }

        @Override
        public int getCount() {
            return StaticVariables.listFriends.size();
        }

        @Override
        public Object getItem(int position) {
            return StaticVariables.listFriends.get(position);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            final Holder holder;
            if (row == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = layoutInflater.inflate(R.layout.row_contacts, parent, false);
                holder = new Holder();

                holder.tvPhoneNumber = (TextView) row.findViewById(R.id.tvPhoneNumber);
                holder.tvUsername = (TextView) row.findViewById(R.id.tvUsername);
                holder.constInvite = (ConstraintLayout) row.findViewById(R.id.constInvite);
                holder.ivVerified = (ImageView) row.findViewById(R.id.ivVerified);
                holder.ivProfile = (ImageView) row.findViewById(R.id.ivProfile);
                holder.cardRow = (CardView) row.findViewById(R.id.cardRow);

                row.setTag(holder);
            } else {
                holder = (Holder) row.getTag();
            }

            final GetSetterUser setter = (GetSetterUser) getItem(position);

            try {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(setter.getProfileLink());
                Glide.with(getContext())
                        .using(new FirebaseImageLoader())
                        .load(storageReference)
                        .crossFade()
                        .into(holder.ivProfile);
            } catch (Exception e) {
                //
            }

            if (setter.getContactNumber() == Integer.parseInt(StaticVariables.adminEcocashNumber))
                holder.ivVerified.setVisibility(View.VISIBLE);
            else
                holder.ivVerified.setVisibility(View.GONE);
            holder.tvUsername.setText(setter.getUsername());
            holder.tvPhoneNumber.setText("" + setter.getContactNumber() + "");

            holder.cardRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    builder = new AlertDialog.Builder(TicketSend.this);
                    builder.setCancelable(false);
                    builder.setTitle("Sending ticket:");
                    builder.setMessage("Are you sure you want to send a ticket to " + setter.getUsername() + "?");
                    builder.setIcon(R.mipmap.ic_launcher);
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Send Ticket to firebase
                            //Delete ticket from local database
                            //Remove it from user firebase database
                            progressBarSend.setVisibility(View.VISIBLE);
                            GetSetterUser setterZ = (GetSetterUser) StaticVariables.listFriends.get(position);
                            setterTickets.setUid(setterZ.getUid());

                            FirebaseDatabase.getInstance().getReference()
                                    .child(StaticVariables.childTickets)
                                    .child(setterTickets.getEventKey())
                                    .child(setterZ.getUid())
                                    .child(setterTickets.getTicketKey())
                                    .setValue(setterTickets)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                FirebaseDatabase.getInstance().getReference()
                                                        .child(StaticVariables.childTickets)
                                                        .child(setterTickets.getEventKey())
                                                        .child(uid)
                                                        .child(setterTickets.getTicketKey())
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(getApplicationContext(), "Ticket has been sent", Toast.LENGTH_LONG).show();

                                                                    progressBarSend.setVisibility(View.GONE);

                                                                    db.delete(DBContract.Tickets.TABLE_NAME, DBContract.Tickets.TICKET_KEY + "=?", new String[]{"" + setterTickets.getTicketKey()});
                                                                    StaticVariables.listTickets.remove(positionTicket);
                                                                    finish();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Ticket sending ailed", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                        }
                    });
                    builder.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.show();
                }
            });

            return row;
        }

        private class Holder {
            ImageView ivVerified, ivProfile;
            TextView tvPhoneNumber, tvUsername;
            ConstraintLayout constInvite;
            CardView cardRow;
        }

    }

}
