package nl.triandria.utilities;


import android.content.BroadcastReceiver;
import android.content.ContentValues;
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
import java.util.Iterator;
import java.util.List;

import database.StockPicking;

public class Synchronization extends BroadcastReceiver {

    private static final String TAG = "Synchronization";

    private static final String[] MODELS_TO_SYNC = {
            "res_company",
            "res_partner",
            "res_users",
            "stock_location",
            "stock_picking_type",
            "stock_picking",
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
                    final JSONArray modelFields = StockPicking.TABLE_FIELDS.get(model);
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
                    // TODO clean this make one search_read call, see if there are any libraries available, if not, create one
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
                    outerArray.put(remote_ids);
                    JSONObject fields = new JSONObject();
                    fields.put("fields", modelFields);
                    argsRead.put(database_name)
                            .put(uid)
                            .put(password)
                            .put(model.replace('_', '.'))
                            .put("read")
                            .put(outerArray)
                            .put(fields);
                    read_params.put("args", argsRead);
                    JSONArray records = client.callJSONArray("execute_kw", read_params);
                    Log.d(TAG, "Records read: " + records.toString());
                    for (int i = 0; i < records.length(); i++) {
                        JSONObject record = (JSONObject) records.get(i);
                        db.insert(model, null, getContentValues(record));// TODO error here, foreign key
                    }
                    // fetch all the records that have changed
                    if (last_sync_date != null && !last_sync_date.isEmpty()) {
                        JSONArray argsArraySearchRead = new JSONArray();
                        JSONObject paramsSearchRead = new JSONObject();
                        paramsSearchRead.put("service", "object");
                        paramsSearchRead.put("method", "execute_kw");
                        JSONArray outerDomain2SearchRead = new JSONArray();
                        JSONArray outerDomainSearchRead = new JSONArray();
                        JSONArray domainSearchRead = new JSONArray();
                        domainSearchRead.put("id")
                                .put("in")
                                .put(local_ids);
                        JSONArray domainSearchRead2 = new JSONArray();
                        domainSearchRead2.put("write_date")
                                .put(">")
                                .put(last_sync_date);
                        outerDomainSearchRead.put(domainSearchRead);
                        outerDomainSearchRead.put(domainSearchRead2);
                        outerDomain2SearchRead.put(outerDomainSearchRead);
                        argsArraySearchRead.put(database_name)
                                .put(uid)
                                .put(password)
                                .put(model.replace('_', '.'))
                                .put("search_read")
                                .put(outerDomain2SearchRead)
                                .put(fields);
                        paramsSearchRead.put("args", argsArraySearchRead);
                        JSONArray remoteIdsSearchRead = client.callJSONArray("execute_kw", paramsSearchRead);
                        Log.d(TAG, "Records that were updated after " + last_sync_date + "\n " + remoteIdsSearchRead.toString());
                        for (int i = 0; i < remoteIdsSearchRead.length(); i++) {
                            JSONObject record = (JSONObject) remoteIdsSearchRead.get(i);
                            int count = db.update(model, getContentValues(record), "id = " + record.getInt("id"), null);
                            Log.d(TAG, "Updating row with id " + record.get("id") + " because we last synced on "
                                    + last_sync_date + " and it has been updated." + "rows affected" + count);
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

    private static ContentValues getContentValues(JSONObject record) throws JSONException {
        ContentValues values = new ContentValues();
        Iterator<String> keys = record.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            // on foreign keys we the id + name is returned. We only care about the id
            Object value = record.get(key);
            if (value instanceof JSONArray) {
                values.put(key, ((JSONArray) record.get(key)).get(0).toString());
            } else {
                values.put(key, record.get(key).toString());
            }
        }
        Log.d(TAG, "ContentValues for record " + values.toString());
        return values;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        new TaskSync().execute(context);
    }

    private static String format_date(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS").format(date);
    }
}
