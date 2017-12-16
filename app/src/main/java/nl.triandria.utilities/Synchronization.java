package nl.triandria.utilities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.alexd.jsonrpc.JSONRPCException;
import org.alexd.jsonrpc.JSONRPCHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import database.StockPicking;

public class Synchronization extends BroadcastReceiver {

    private static final String TAG = "Synchronization";

    private static final String[] MODELS_TO_SYNC = {
            "stock_picking",
            "res_users",
            "res_company",
            "res_partner",
            "stock_location",
    };

    private static class TaskSync extends AsyncTask<Context, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Context... contexts) {
            SharedPreferences sharedPreferences = contexts[0].getSharedPreferences(
                    SessionManager.SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
            String last_sync_date = sharedPreferences.getString("last_sync_date", null);
            String url = sharedPreferences.getString("url", null);
            int uid = sharedPreferences.getInt("uid", 0);
            String password = sharedPreferences.getString("password", null);
            String database_name = sharedPreferences.getString("database", null);
            SQLiteDatabase db = new StockPicking(contexts[0]).getWritableDatabase();
            Log.d(TAG, "Starting synchronization, last_sync_date " + last_sync_date);
            try {
                db.beginTransaction();
                for (final String model : MODELS_TO_SYNC) {
                    Log.d(TAG, "Syncing model: " + model);
                    final List<String> modelFields = StockPicking.TABLE_FIELDS.get(model);
                    JSONArray local_ids = new JSONArray();
                    Cursor cr = db.rawQuery("SELECT id FROM " + model, null);
                    // if we already have some
                    if (cr.moveToFirst()) {
                        do {
                            local_ids.put(cr.getInt(0));
                        } while (cr.moveToNext());
                        cr.close();
                    }
                    Log.d(TAG, "local_ids " + local_ids.toString());
                    JSONRPCHttpClient client = new JSONRPCHttpClient(url);
                    // TODO clean this, see if there are any libraries available
                    JSONArray argsArray = new JSONArray();
                    JSONObject params = new JSONObject();
                    params.put("service", "object");
                    params.put("method", "execute_kw");
                    JSONArray outerDomain2 = new JSONArray();
                    JSONArray outerDomain = new JSONArray();
                    JSONArray domain = new JSONArray();
                    domain.put("id");
                    domain.put("not in");
                    domain.put(local_ids);
                    outerDomain.put(domain);
                    outerDomain2.put(outerDomain);
                    argsArray.put(database_name)
                            .put(uid)
                            .put(password)
                            .put(model.replace('_', '.'))
                            .put("search")
                            .put(outerDomain2);
                    params.put("args", argsArray);
                    Log.d(TAG, params.toString());
                    JSONArray remote_ids = client.callJSONArray("execute_kw", params);
                    Log.d(TAG, "Record ids to fetch" + remote_ids.toString());

                    JSONObject read_params = new JSONObject();
                    read_params.put("service", "object");
                    read_params.put("method", "execute_kw");
                    JSONArray argsRead = new JSONArray();
                    JSONArray outerArray = new JSONArray();
                    JSONArray outerArray2 = new JSONArray();
                    outerArray.put(remote_ids);
                    argsRead.put(database_name)
                            .put(uid)
                            .put(password)
                            .put(model.replace('_', '.'))
                            .put("read")
                            .put(outerArray)
                            .put(modelFields);
                    read_params.put("args", argsRead);
                    JSONArray records = client.callJSONArray("execute_kw", read_params);
                    Log.d(TAG, "Records read: " + records.toString());
                    // TODO continue debugging here
                    db.execSQL("INSERT INTO " + model + modelFields + records);
                    // fetch all the records that have changed
                    if (last_sync_date != null && !last_sync_date.isEmpty()) {
                        JSONArray remote_changed_record_ids = client.callJSONArray(
                                "search",
                                model,
                                Arrays.asList(
                                        Arrays.asList("id", "in", local_ids),
                                        Arrays.asList("write_date", ">", last_sync_date)));
                        JSONArray updated_records = client.callJSONArray(
                                "read",
                                model,
                                remote_changed_record_ids,
                                new HashMap() {{
                                    put("fields", modelFields);
                                }});
                        Log.d(TAG, "Records that were updated after " + last_sync_date + "\n " + updated_records.toString());
                        for (int i = 0; i < updated_records.length(); i++) {
                            JSONObject rec = updated_records.getJSONObject(i);
                            db.execSQL("UPDATE " + model +
                                    " SET " + modelFields +
                                    " VALUES " + rec.toString() +
                                    " WHERE id=" + rec.getInt("id"));
                        }
                    }
                    // TODO make sure that this is in the same timezone as the server
                    sharedPreferences.edit().putString("last_sync_date", format_date(new Date())).apply();
                }
            } catch (JSONRPCException | JSONException e) {
                // TODO inform user,
                e.printStackTrace();
                return false;
            } finally {
                db.endTransaction();
            }
            return true;
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        new TaskSync().execute(context);
    }

    private static String format_date(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS").format(date);
    }
}
