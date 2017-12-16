package nl.triandria.utilities;

import android.app.Dialog;
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
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import nl.triandria.odoowarehousing.R;

public class SessionManager {

    // TODO: Separate input validation and state validation (are we connected to the internet?) into different functions
    private static final String TAG = "SessionManager";
    static final String SHARED_PREFERENCES_FILENAME = "odoo.sec";

    public static class LogInTask extends AsyncTask<Object, Integer, String> {

        WeakReference<Dialog> dialog;

        public LogInTask(Dialog dialog){
            this.dialog = new WeakReference<Dialog>(dialog);
        }

        @Override
        protected void onPreExecute() {
            ProgressBar progressBar = this.dialog.get().findViewById(R.id.indeterminateBar);
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String err_message) {
            ProgressBar progressBar = this.dialog.get().findViewById(R.id.indeterminateBar);
            progressBar.setVisibility(View.GONE);
            if (err_message != null){
                Toast.makeText(this.dialog.get().getContext(), err_message, Toast.LENGTH_LONG).show();
            } else {
                this.dialog.get().dismiss();
            }
            super.onPostExecute(err_message);
        }

        @Override
        protected String doInBackground(Object... args) {
            String url = args[0] + "/jsonrpc";
            String username = (String) args[1];
            String password = (String) args[2];
            String database = (String) args[3];
            Context context = (Context) args[4];
            try {
                new URL(url);
                JSONRPCHttpClient client = new JSONRPCHttpClient(url);
                JSONArray argsArray = new JSONArray();
                JSONObject params = new JSONObject();
                params.put("service", "common");
                params.put("method", "authenticate");
                argsArray.put(database)
                        .put(username)
                        .put(password)
                        .put(new JSONObject());
                params.put("args", argsArray);
                Object uid = client.call("authenticate", params);
                if (uid instanceof Integer && (int)uid != 0) {
                    Log.d(TAG, "Logged in with UID ===> " + uid.toString());
                    SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
                    preferences.edit().putInt("uid", (int) uid)
                            .putString("username", username)
                            .putString("password", password)
                            .putString("database", database)
                            .putString("url", url)
                            .apply();
                }
            } catch (JSONRPCException e) {
                e.printStackTrace();
                return context.getString(R.string.error_login_failed_invalid_credentials);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return context.getString(R.string.error_malformed_url);
            } catch (JSONException e) {
                e.printStackTrace();
                return context.getString(R.string.error_login_failed_generic_error);
                // TODO: non recoverable error, notify developer
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
            JSONObject params = new JSONObject();
            try {
                JSONObject args = new JSONObject();
                args.put("params", new JSONObject());
                params.put("service", "db");
                params.put("method", "list");
                params.put("args", args);
                JSONArray result = client.callJSONArray("list", params);
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