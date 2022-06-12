package tech.myevents.app;

import android.os.AsyncTask;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by user on 1/4/2018.
 */

public class PageHitsBackTask extends AsyncTask<GetSetterPageHits, Void, Void> {

    @Override
    protected Void doInBackground(GetSetterPageHits... params) {
        GetSetterPageHits setterPageHits = params[0];

        String uid = setterPageHits.getUid();
        String pageName = setterPageHits.getPageName();
        long timestamp = setterPageHits.getTimestamp();

        /*String key = FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childPageHits)
                .push()
                .getKey();
        GetSetterPageHits  setterHits = new GetSetterPageHits(uid, key, pageName, timestamp);

        FirebaseDatabase.getInstance().getReference()
                .child(StaticVariables.childPageHits)
                .child(key)
                .setValue(setterHits);*/

        return null;
    }
}
