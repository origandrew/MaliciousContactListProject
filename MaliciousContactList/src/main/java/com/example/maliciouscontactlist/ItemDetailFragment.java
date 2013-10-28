package com.example.maliciouscontactlist;

import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.LoaderManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link com.example.maliciouscontactlist.ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends ListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = callbacks;

    // Defines the selection clause
    private static final String SELECTION = Phone.LOOKUP_KEY + " = ?";
    // Defines the array to hold the search criteria
    private String[] mSelectionArgs = { "" };
    /*
     * Defines a variable to contain the selection value. Once you
     * have the Cursor from the Contacts table, and you've selected
     * the desired row, move the row's LOOKUP_KEY value into this
     * variable.
     */
    private String mLookupKey;

    /*
     * Defines a string that specifies a sort order of MIME type
     */
    private static final String SORT_ORDER = Phone.MIMETYPE;

    /**
     * The adapter used to display the contacts data
     */
    private SimpleCursorAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(final String number);
    }

    private static Callbacks callbacks = new Callbacks() {
        @Override
        public void onItemSelected(final String number) {
            Log.i(Statics.NAME, "callback with number: " + number);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mLookupKey = getArguments().getString(ARG_ITEM_ID);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                android.R.layout.simple_list_item_activated_2, null,
                new String[] { Phone.TYPE, Phone.NUMBER },
                new int[] { android.R.id.text1, android.R.id.text2 }, 0);

        ViewBinder mViewBinder = new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                if (view.getId() == android.R.id.text1) {
                    ((TextView) view).setText(Phone.getTypeLabelResource(cursor.getInt(i)));
                    return true;
                }
                return false;
            }
        };

        mAdapter.setViewBinder(mViewBinder);

        setListAdapter(mAdapter);
        // Initializes the loader framework\
        getLoaderManager().initLoader(0, null, this);
    }

    private static final class CONTACT_DETAIL_QUERY {
        private static final String[] PROJECTION = {
                Phone._ID,
                Phone.TYPE,
                Phone.NUMBER,
                Phone.LOOKUP_KEY,
                Phone.CONTACT_ID
        };
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        mSelectionArgs[0] = mLookupKey;

        return new CursorLoader(getActivity(), Phone.CONTENT_URI,
                CONTACT_DETAIL_QUERY.PROJECTION, SELECTION, mSelectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        final Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);

        Log.i(Statics.NAME, "Clicked item: " + position +
                " ID: " + cursor.getString(cursor.getColumnIndex(Phone._ID)) +
                " LOOKUP KEY: " + cursor.getString(cursor.getColumnIndex(Phone.LOOKUP_KEY)));

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(cursor.getString(cursor.getColumnIndex(Phone.NUMBER)));
    }

}
