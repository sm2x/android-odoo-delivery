package nl.triandria.odoowarehousing.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import nl.triandria.odoowarehousing.R;
import nl.triandria.odoowarehousing.SettingsActivity;
import nl.triandria.odoowarehousing.activities.fragments.Login;
import nl.triandria.utilities.SessionManager;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<ArrayList<MainActivity.ResUser>> {

    private static final String TAG = MainActivity.class.getName();
    SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Oncreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar_main_activity);
        setActionBar(toolbar);
        initLoader();
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


    private void initLoader() {
        LoaderManager manager = getLoaderManager();
        manager.initLoader(0, null, this);
    }

    @Override
    public Loader<ArrayList<ResUser>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "oncreateloader");
        return new LoaderAppUsers(this);
    }


    @Override
    public void onLoaderReset(Loader<ArrayList<ResUser>> loader) {
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<ResUser>> loader, ArrayList<ResUser> data) {
        Log.d(TAG, "onloadfinished" + data.toString());
        ListView app_users = findViewById(R.id.listview_app_users);
        ArrayAdapter<ResUser> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, data);
        app_users.setAdapter(adapter);
        app_users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO show Login fragment as a popup, save connection info to res_users. Make sure you create different databases
                // if the user selects a different database (maybe different url?)
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Bundle args = new Bundle();
                args.putInt("userId", ((ResUser) parent.getItemAtPosition(position)).id);
                Login login = new Login();
                login.setArguments(args);
                transaction.add(login, Login.TAG);
                transaction.commit();
            }
        });
    }

    /**
     * This class loads the users of this application
     */
    static class LoaderAppUsers extends AsyncTaskLoader<ArrayList<ResUser>> {

        LoaderAppUsers(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public ArrayList<ResUser> loadInBackground() {
            Log.d(TAG, "loadinbackground");
            ArrayList<ResUser> records = new ArrayList<>();
            records.add(new ResUser(0, "New User..."));
            SharedPreferences preferences = getContext().getSharedPreferences(
                    SessionManager.SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
            Collection<?> values = preferences.getAll().values();
            for (Object user : values) {
                LinkedHashSet<String> set = (LinkedHashSet<String>) user;
                Iterator<String> iterator = set.iterator();
                int id = Integer.valueOf(iterator.next());
                String login = iterator.next();
                records.add(new ResUser(id, login));
            }
            return records;
        }
    }

    static class ResUser {

        private int id;
        private String login;

        ResUser(int id, String login) {
            this.id = id;
            this.login = login;
        }

        @Override
        public String toString() {
            return this.login;
        }
    }
}
