package nl.triandria.utilities;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.alexd.jsonrpc.JSONRPCException;
import org.alexd.jsonrpc.JSONRPCHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import nl.triandria.odoowarehousing.R;

public class SessionManager {

    // TODO: Separate input validation and state validation (are we connected to the internet?) into different functions
    private static final String TAG = "SessionManager";
    static final String SHARED_PREFERENCES_FILENAME = "odoo.sec";

    public static class LogInTask extends AsyncTask<Object, Integer, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            String url = (String) params[0];
            String username = (String) params[1];
            String password = (String) params[2];
            String database = (String) params[3];
            Context context = (Context) params[4];
            ProgressBar bar = new ProgressBar(context);
            bar.setIndeterminate(true);
            bar.setVisibility(View.VISIBLE);
            if (url.isEmpty() || username.isEmpty() || password.isEmpty() || database.isEmpty()) {
                bar.setVisibility(View.GONE);
                throw new InvalidCredentials();
            }
            try {
                new URL(url);
                JSONRPCHttpClient client = new JSONRPCHttpClient(url);
                Object uid = client.call("authenticate", database, username, password);
                if (uid instanceof Integer) {
                    SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
                    preferences.edit().putInt("uid", (int) uid)
                            .putString("username", username)
                            .putString("password", password)
                            .putString("database", database)
                            .putString("url", url)
                            .apply();
                    bar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(context, context.getString(R.string.error_login_failed_invalid_credentials), Toast.LENGTH_LONG).show();
                    bar.setVisibility(View.GONE);
                }
            } catch (JSONRPCException e) {
                Toast.makeText(context, context.getString(R.string.error_login_failed_generic_error), Toast.LENGTH_LONG).show();
                bar.setVisibility(View.GONE);
            } catch (MalformedURLException e) {
                Toast.makeText(context, context.getString(R.string.error_malformed_url), Toast.LENGTH_LONG).show();
                bar.setVisibility(View.GONE);
            }
            return null;
        }
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        Integer uid = preferences.getInt("uid", 0);
        return uid != 0;
    }


    public static class GetDatabasesTask extends AsyncTask<URI, Integer, ArrayList<String>> {

        WeakReference<DialogFragment> weakReference;

        public GetDatabasesTask(DialogFragment dialog) {
            weakReference = new WeakReference<>(dialog);
        }

        @Override
        protected ArrayList<String> doInBackground(URI... urls) {

            JSONRPCHttpClient client = new JSONRPCHttpClient(urls[0] + "/jsonrpc");
            ArrayList<String> databases = new ArrayList<>();
            HashMap<String, String> params = new HashMap<>();
            params.put("service", "db");
            params.put("method", "list");
            try {
                JSONArray result = client.callJSONArray("list", params);
                Log.d(TAG, result.toString());
                for (int i = 0; i < result.length(); i++) {
                    databases.add(result.getString(i));
                }
            } catch (JSONRPCException | JSONException e) {
                e.printStackTrace();
            }
            return databases;
        }

        @Override
        protected void onPostExecute(ArrayList<String> databases) {
            super.onPostExecute(databases);
            ArrayAdapter databaseAdapter = new ArrayAdapter<>(
                    weakReference.get().getActivity(), android.R.layout.simple_list_item_1, databases);
            ((Spinner) weakReference.get().getDialog().findViewById(R.id.database)).setAdapter(databaseAdapter);
        }
    }
}