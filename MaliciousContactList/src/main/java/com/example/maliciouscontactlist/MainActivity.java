package com.example.maliciouscontactlist;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the main activity (wrapper to call the background image loader)
 */
public class MainActivity extends FragmentActivity
        implements ItemListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * background image
     */
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ItemListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }

        mImageView = (ImageView) findViewById(R.id.backgroundImage);
        //new UpdateBackgroundImageTask().execute();
    }


    /**
     * Callback method from {@link com.example.maliciouscontactlist.ItemListFragment.Callbacks}
     * indicating that the item with the given uri was selected.
     */
    @Override
    public void onItemSelected(final String lookupKey) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_ITEM_ID, lookupKey);
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ItemDetailActivity.class);
            detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, lookupKey);
            startActivity(detailIntent);
        }
    }

    public class UpdateBackgroundImageTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... params) {
            return updateBackgroundImage();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                Log.e(Statics.NAME, "no image to change to");
                return;
            }
            if (mImageView == null) {
                Log.e(Statics.NAME, "imageView not found");
                return;
            }

            mImageView.setImageBitmap(result);
        }

        private Bitmap updateBackgroundImage() {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet("http://photography.nationalgeographic.com/photography/photo-of-the-day/");
                HttpResponse httpResponse = httpClient.execute(httpGet);

                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                StringBuilder stringBuilder = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                final String html = stringBuilder.toString();
                final Matcher matcher = Pattern.compile("(http://images.nationalgeographic.com/[^\"]*)").matcher(html);
                if (matcher.find()) {
                    final String url = matcher.group();
                    Log.i(Statics.NAME, url);
                    return getImage(url);
                } else {
                    Log.e(Statics.NAME, "image link not found in :" + html);
                }
            }
            catch (final ClientProtocolException e) {
                Log.e(Statics.NAME, e.getMessage());
            }
            catch (final IOException e) {
                Log.e(Statics.NAME, e.getMessage());
            }
            return null;
        }

        private Bitmap getImage(final String url) {
            try {
                final InputStream inputStream = new URL(url).openStream();
                return BitmapFactory.decodeStream(inputStream);
            }
            catch (final IOException e) {
                Log.e(Statics.NAME, "get image failed: " + e.getMessage());
            }
            return null;
        }
    }
}
