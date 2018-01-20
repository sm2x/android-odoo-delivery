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

import org.alexd.jsonrpc.JSONRPCException;
import org.alexd.jsonrpc.JSONRPCHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import database.StockPicking;

public class Synchronization extends BroadcastReceiver {

    private static final String TAG = "Synchronization";

    private static final String[] MODELS_TO_SYNC = {
            "res_company",
            "res_users",
            "stock_location",
            "stock_picking_type",
            "stock_picking",
            //"stock_move",
            "res_partner",
            //"product_template",
            //"product_product",
            //"stock_inventory"
    };

    // TODO this should be launched by a service, FIX OOM, sync in batches
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
            int uid_partner_id = sharedPreferences.getInt("uid_partner_id", 0);
            SQLiteDatabase db = new StockPicking(contexts[0]).getWritableDatabase();
            Log.d(TAG, "Starting synchronization, last_sync_date " + last_sync_date + " owner_id " + uid_partner_id);
            try {
                db.beginTransaction();
                // this will be filled after the stock_picking model is synced, we want to fetch only
                // relevant partners not all of them
                JSONArray partnersToFetch = new JSONArray();
                JSONArray stockInventoryIds = new JSONArray();
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
                    // TODO see if there are any libraries available, if not, create one, REFACTOR THIS
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
                    JSONArray pickingsAssigned = new JSONArray();
                    JSONArray pickingState = new JSONArray();
                    if (model.equals("stock_picking")) {
                        pickingsAssigned.put("owner_id");
                        pickingsAssigned.put("=");
                        pickingsAssigned.put(uid_partner_id);
                        outerDomain.put(pickingsAssigned);
                        pickingState.put("state");
                        pickingState.put("=");
                        pickingState.put("assigned");
                        outerDomain.put(pickingState);
                    } else if (model.equals("res_partner")) {
                        JSONArray partnersToFetchDomain = new JSONArray();
                        partnersToFetchDomain.put("id");
                        partnersToFetchDomain.put("in");
                        partnersToFetchDomain.put(partnersToFetch);
                        outerDomain.put(partnersToFetchDomain);
                    } else if (model.equals("stock_inventory")) {
                        JSONArray stockInventory = new JSONArray();
                        stockInventory.put("state");
                        stockInventory.put("=");
                        stockInventory.put("confirm");
                        outerDomain.put(stockInventory);
                    } else if (model.equals("stock_inventory_line")) {
                        JSONArray stockInventoryLine = new JSONArray();
                        stockInventoryLine.put("picking_id");
                        stockInventoryLine.put("in");
                        stockInventoryLine.put(stockInventoryIds);
                        outerDomain.put(stockInventoryLine);
                    }
                    outerDomain2.put(outerDomain);
                    JSONObject fields = new JSONObject();
                    fields.put("fields", modelFields);
                    argsArray.put(database_name)
                            .put(uid)
                            .put(password)
                            .put(model.replace('_', '.'))
                            .put("search_read")
                            .put(outerDomain2)
                            .put(fields);
                    params.put("args", argsArray);
                    Log.d(TAG, params.toString());
                    if (last_sync_date == null) {
                        Log.d(TAG, "Initial sync");
                        JSONArray records = client.callJSONArray("execute_kw", params);
                        for (int i = 0; i < records.length(); i++) {
                            JSONObject record = (JSONObject) records.get(i);
                            ContentValues values = getContentValues(record);
                            if (model.equals("stock_picking")) {
                                partnersToFetch.put(values.getAsInteger("partner_id"));
                            } else if (model.equals("stock_inventory")){
                                stockInventoryIds.put(values.getAsInteger("id"));
                            }
                            Log.d(TAG, "Inserting values to table: " + model + "\n" + values);
                            db.insertOrThrow(model, null, values);
                        }
                    } else {
                        // TODO test the differential syncing
                        Log.d(TAG, "Differential syncing.");
                        JSONArray dateDomain = new JSONArray();
                        dateDomain.put("write_date");
                        dateDomain.put(">");
                        dateDomain.put(last_sync_date);
                        outerDomain.put(dateDomain);
                        // remove the id not in local_ids domain, we do not need that
                        outerDomain.remove(0);
                        // basically replace the domain to have the date inside it as well
                        argsArray.put(5, outerDomain);
                        JSONArray remoteIdsSearchRead = client.callJSONArray("execute_kw", params);
                        Log.d(TAG, "Records that were updated after " + last_sync_date + "\n " + remoteIdsSearchRead.toString());
                        for (int i = 0; i < remoteIdsSearchRead.length(); i++) {
                            JSONObject record = (JSONObject) remoteIdsSearchRead.get(i);
                            ContentValues values = getContentValues(record);
                            if (model.equals("stock_picking")) {
                                partnersToFetch.put(values.getAsInteger("partner_id"));
                            } else if (model.equals("stock_inventory")){
                                stockInventoryIds.put(values.getAsInteger("id"));
                            }
                            int count = db.update(model, values, "id = " + record.getInt("id"), null);
                            Log.d(TAG, "Updating row with id " + record.get("id") + " because we last synced on "
                                    + last_sync_date + " and it has been updated." + "rows affected" + count);
                        }
                    }
                    // TODO make sure that this is in the same timezone as the server
                    sharedPreferences.edit().putString("last_sync_date", format_date(new Date())).apply();
                }
                db.setTransactionSuccessful();
            } catch (JSONRPCException | JSONException e) {
                e.printStackTrace();
                return false;
            } finally {
                Log.d(TAG, "Ending transaction");
                db.endTransaction();
                db.close();
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
