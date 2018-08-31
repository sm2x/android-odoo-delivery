package nl.triandria.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.SortedSet;

import nl.triandria.odoowarehousing.R;
import nl.triandria.odoowarehousing.activities.fragments.Main;

public class SessionManager {

    // TODO: Separate input validation and state validation (are we connected to the internet?) into different functions
    // setup a broadcast receiver, if the receiver receives that the connection has been dropped for some reason
    // create a popup that is cannot be dismissed
    private static final String TAG = "SessionManager";
    private static final String ACTION_SYNCHRONIZE = "synchronize";
    public static final String SHARED_PREFERENCES_FILENAME = "odoo.sec";

    public static class LogInTask extends AsyncTask<Object, Integer, String> {

        WeakReference<Activity> activityWeakReference;
        AlertDialog progressBarDialog;
        DialogFragment dialogFragment;

        public LogInTask(DialogFragment dialogFragment) {
            this.activityWeakReference = new WeakReference<>(dialogFragment.getActivity());
            this.dialogFragment = dialogFragment;
            ProgressBar progressBar = new ProgressBar(this.activityWeakReference.get());
            progressBar.setIndeterminate(true);
            progressBar.setBackgroundColor(android.R.color.white);
            AlertDialog.Builder builder = new AlertDialog.Builder(this.activityWeakReference.get());
            builder.setView(progressBar);
            progressBarDialog = builder.create();
        }

        @Override
        protected void onPreExecute() {
            progressBarDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String err_message) {
            progressBarDialog.dismiss();
            dialogFragment.dismiss();
            if (err_message != null) {
                Toast.makeText(this.activityWeakReference.get(), err_message, Toast.LENGTH_LONG).show();
            } else {
                this.progressBarDialog.dismiss();
                FragmentManager manager = activityWeakReference.get().getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.layout_main_activity, new Main());
                transaction.commit();
            }
            Log.d(TAG, "Login successful, starting sync");
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this.activityWeakReference.get());
            manager.registerReceiver(new Synchronization(), new IntentFilter(ACTION_SYNCHRONIZE));
            manager.sendBroadcast(new Intent(ACTION_SYNCHRONIZE));
            super.onPostExecute(err_message);
        }

        /**
         * This function logs in the user, and saves their credentials locally.
         * */
        @Override
        protected String doInBackground(Object... args) {
            String url = args[0] + "/jsonrpc";
            String username = (String) args[1];
            String password = (String) args[2];
            String database = (String) args[3];
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
                if (uid instanceof Integer && (int) uid != 0) {
                    Log.d(TAG, "Logged in with UID ===> " + uid.toString());

                    int uid_partner_id = getUidPartnerId((int) uid, client, database, password);
                    Log.d(TAG, "UidPartnerId === >" + uid_partner_id);
                    LinkedHashSet<String> user = new LinkedHashSet<>();
                    user.add(Integer.toString(uid_partner_id));
                    user.add(username);
                    user.add(password);
                    user.add(database);
                    user.add(url);
                    SharedPreferences preferences = activityWeakReference.get().getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
                    preferences.edit()
                            .putStringSet(Integer.toString((int) uid), user)
                            .apply();
                }
            } catch (JSONRPCException e) {
                e.printStackTrace();
                return activityWeakReference.get().getString(R.string.error_login_failed_invalid_credentials);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return activityWeakReference.get().getString(R.string.error_malformed_url);
            } catch (JSONException e) {
                e.printStackTrace();
                return activityWeakReference.get().getString(R.string.error_generic);
            }
            return null;
        }
    }

    // TODO create a single wrapper (asynctask that will handle all the rpc calls, error handling will be delegated
    private static void execute_kw(JSONRPCHttpClient client, String service, String method, JSONObject args)
            throws JSONException, JSONRPCException {
        JSONObject executeArgs = new JSONObject();
        JSONArray functionArgs = new JSONArray();
        executeArgs.put("service", "common");
        executeArgs.put("method", "version");
        executeArgs.put("args", functionArgs);
        client.callJSONObject("execute_kw", executeArgs);
    }

    private static int getUidPartnerId(int uid, JSONRPCHttpClient client, String database_name, String password)
            throws JSONException, JSONRPCException {
        JSONArray argsArray = new JSONArray();
        JSONObject params = new JSONObject();
        params.put("service", "object");
        params.put("method", "execute_kw");
        JSONArray modelFields = new JSONArray();
        modelFields.put("partner_id");
        JSONObject fields = new JSONObject();
        fields.put("fields", modelFields);
        JSONArray uidArray = new JSONArray();
        uidArray.put(uid);
        argsArray.put(database_name)
                .put(uid)
                .put(password)
                .put("res.users")
                .put("read")
                .put(uidArray)
                .put(fields);
        params.put("args", argsArray);
        Log.d(TAG, "Getting partner_id from uid ===> " + params);
        JSONArray paramsArrayVersion = new JSONArray();
        JSONObject paramsVersion = new JSONObject();
        paramsVersion.put("service", "common");
        paramsVersion.put("method", "version");
        paramsVersion.put("args", paramsArrayVersion);
        double serverVersion = Double.parseDouble(client.callJSONObject("execute_kw", paramsVersion).getString("server_version"));
        if (serverVersion == 8.0) {
            return client.callJSONObject(
                    "execute_kw", params).getJSONArray("partner_id").getInt(0);
        }
        return client.callJSONArray("execute_kw", params).getJSONObject(0).getJSONArray("partner_id").getInt(0);
    }

    public static class GetDatabasesTask extends AsyncTask<URI, Integer, ArrayList<String>> {

        WeakReference<DialogFragment> weakReference;
        Exception exc;

        public GetDatabasesTask(DialogFragment dialog) {
            weakReference = new WeakReference<>(dialog);
        }

        @Override
        protected ArrayList<String> doInBackground(URI... urls) {
            Log.d(TAG, "Getting database list from the server.");
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
            } catch (JSONRPCException | JSONException | IllegalArgumentException e) {
                e.printStackTrace();
                exc = e;
            }
            return databases;
        }

        @Override
        protected void onPostExecute(ArrayList<String> databases) {
            super.onPostExecute(databases);
            if (exc == null) {
                if (!databases.isEmpty()) {
                    ArrayAdapter databaseAdapter = new ArrayAdapter<>(
                            weakReference.get().getActivity(), android.R.layout.simple_list_item_1, databases);
                    ((Spinner) weakReference.get().getView().findViewById(R.id.dialog_login_database)).setAdapter(databaseAdapter);
                }
            } else {
                Toast.makeText(weakReference.get().getActivity(),
                        exc.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}