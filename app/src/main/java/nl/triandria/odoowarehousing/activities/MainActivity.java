package nl.triandria.odoowarehousing.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

import database.StockPicking;
import nl.triandria.odoowarehousing.R;
import nl.triandria.odoowarehousing.SettingsActivity;
import nl.triandria.utilities.SessionManager;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();
    SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Oncreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar_main_activity);
        setActionBar(toolbar);
        boolean isLoggedIn = SessionManager.isLoggedIn(this);
        setListViewAppUsersData(this);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (!isLoggedIn) {
            Log.d(TAG, "User is NOT logged in.");
            //transaction.replace(R.id.layout_main_activity, new Login());
        } else {
            Log.d(TAG, "User is logged in");
            //transaction.replace(R.id.layout_main_activity, new Main());
        }
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_action_licence:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //builder.setView(R.id.dialog_licence);
                builder.show();
            case R.id.toolbar_action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem searchViewMenuItem = menu.findItem(R.id.toolbar_activity_delivery_search);
        SearchView searchView = (SearchView) searchViewMenuItem.getActionView();
        // TODO searchView.setOnQueryTextListener(new StockPickingListView());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null && scanResult.getContents() != null) {
            Log.d(TAG, "Successful barcode scan, the encoded information is: " + scanResult.getContents());
            adapter.getFilter().filter(scanResult.getContents());
        } else {
            Log.d(TAG, "Barcode scan error");
            Toast.makeText(this, getString(R.string.error_barcode_scan), Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private static void setListViewAppUsersData(final Activity MainActivity) {
        ListView appUsers = MainActivity.findViewById(R.id.listview_app_users);
        MainActivity.getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<ArrayList>() {

            @Override
            public Loader<ArrayList> onCreateLoader(int id, Bundle args) {
                return new LoaderAppUsers(MainActivity);
            }

            @Override
            public void onLoaderReset(Loader<ArrayList> loader) {
            }

            @Override
            public void onLoadFinished(Loader<ArrayList> loader, ArrayList data) {
            }
        });
    }

    static class LoaderAppUsers extends AsyncTaskLoader<ArrayList> {

        LoaderAppUsers(Context context) {
            super(context);
        }

        @Override
        public ArrayList<String> loadInBackground() {
            SQLiteDatabase database = SQLiteDatabase.openDatabase(
                    this.getContext().getDatabasePath(StockPicking.DATABASE_NAME).getAbsolutePath(),
                    null,
                    SQLiteDatabase.OPEN_READONLY);
            Cursor cursor = database.query("app_users", null, null, null,
                    null, null, null);
            ArrayList<String> app_users_names = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    app_users_names.add(cursor.getString(cursor.getColumnIndex("name")));
                } while (cursor.moveToNext());
            }
            cursor.close();
            return app_users_names;// TODO find out how this is used in populating the listview
        }
    }
}
