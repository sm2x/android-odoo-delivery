package nl.triandria.odoowarehousing.activities;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.net.URI;
import java.util.HashMap;

import nl.triandria.odoowarehousing.R;
import nl.triandria.odoowarehousing.SettingsActivity;
import nl.triandria.odoowarehousing.activities.fragments.Main;
import nl.triandria.utilities.SessionManager;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

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
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (!isLoggedIn) {
            Log.d(TAG, "User is NOT logged in.");
            transaction.replace(R.id.layout_main_activity, new LoginDialog());
        } else {
            Log.d(TAG, "User is logged in");
            transaction.replace(R.id.layout_main_activity, new Main());
        }
        transaction.commit();
    }

    public static class LoginDialog extends DialogFragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            final LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.dialog_login, container);
            Spinner protocols = dialogView.findViewById(R.id.protocol);
            ArrayAdapter protocolAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.protocols, android.R.layout.simple_spinner_item);
            protocolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            protocols.setAdapter(protocolAdapter);
            Spinner database = dialogView.findViewById(R.id.database);
            database.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        HashMap<String, String> values = getLoginFragmentValues(dialogView);
                        Adapter adapter = ((Spinner) view).getAdapter();
                        if (adapter == null || adapter.isEmpty()
                                && values.get("username") != null
                                && values.get("password") != null
                                && values.get("protocol") != null
                                && values.get("url") != null
                                && values.get("port") != null) {
                            new SessionManager.GetDatabasesTask(LoginDialog.this).execute(URI.create(values.get("url")));
                        }
                    }
                    view.performClick();
                    return true;
                }
            });
            Button button_login_ok = dialogView.findViewById(R.id.button_login_ok);
            button_login_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String, String> values = getLoginFragmentValues(dialogView);
                    boolean valid = validateLoginFragmentValues(values);
                    if (!valid) {
                        Log.d(TAG, "Invalid data entry");
                        Toast.makeText(dialogView.getContext(), "Invalid data.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    new SessionManager.LogInTask(getDialog()).execute(
                            values.get("url"),
                            values.get("username"),
                            values.get("password"),
                            values.get("database"),
                            dialogView.getContext());
                }
            });
            Button button_login_cancel = dialogView.findViewById(R.id.button_login_cancel);
            button_login_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().finish();
                }
            });
            return dialogView;
        }

        private boolean validateLoginFragmentValues(HashMap<String, String> values) {
            for (String key : values.keySet()) {
                String value = values.get(key);
                if (value == null || value.trim().isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        private HashMap<String, String> getLoginFragmentValues(View dialog) {
            HashMap<String, String> values = new HashMap<>();
            String database = "";
            Object selectedDatabase = ((Spinner) dialog.findViewById(R.id.database)).getSelectedItem();
            if (selectedDatabase != null) {
                database = selectedDatabase.toString();
            }
            values.put("username", ((EditText) dialog.findViewById(R.id.username)).getText().toString());
            values.put("password", ((EditText) dialog.findViewById(R.id.password)).getText().toString());
            values.put("database", database);
            values.put("url", getLoginUrl(dialog));
            Log.d(TAG, "getLoginValues");
            Log.d(TAG, values.toString());
            return values;
        }

        private String getLoginUrl(View dialog) {
            StringBuilder urlBuilder = new StringBuilder();
            String protocol = "";
            Object selectedProtocol = ((Spinner) dialog.findViewById(R.id.protocol)).getSelectedItem();
            if (selectedProtocol != null) {
                protocol = selectedProtocol.toString() + "://";
            }
            urlBuilder.append(protocol);
            urlBuilder.append(((TextView) dialog.findViewById(R.id.url)).getText().toString());
            urlBuilder.append(':');
            urlBuilder.append(((TextView) dialog.findViewById(R.id.port)).getText().toString());
            return urlBuilder.toString();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_action_licence:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(R.id.dialog_licence);
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
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG, "Filtering data " + query);
        adapter.getFilter().filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            adapter.getFilter().filter("");
            return true;
        }
        return false;
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

}
