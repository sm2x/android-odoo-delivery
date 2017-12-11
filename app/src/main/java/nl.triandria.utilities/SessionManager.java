package nl.triandria.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.alexd.jsonrpc.JSONRPCException;
import org.alexd.jsonrpc.JSONRPCHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    // TODO: Seperate input validation and state validation (are we connected to the internet?) into different functions
    private static final String TAG = "SessionManager";
    public static final String SHARED_PREFERENCES_FILENAME = "odoo.sec";

    public static int logIn(String username, String password, String database, String url, Context context) {
        if (username.isEmpty() || password.isEmpty()) {
            throw new InvalidCredentials();
        }
        try {
            //TODO: check if url in valid format
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
                return (int) uid;
            } else {
                // TODO login failed, inform user why
                return 0;
            }
        } catch (JSONRPCException e) {
            // TODO login failed, inform user why
            return 0;
        }
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
        Integer uid = preferences.getInt("uid", 0);
        return uid != 0;
    }

    public static List<String> getDatabases(String url) {
        JSONRPCHttpClient client = new JSONRPCHttpClient(url);
        ArrayList<String> databases = new ArrayList<>();
        try {
            JSONArray result = client.callJSONArray("list", "db", "list");
            for (int i = 0; i < result.length(); i++) {
                databases.add(result.getString(i));
            }
        } catch (JSONRPCException | JSONException e) {
            Log.d(TAG, "getDatabases error" + e.getMessage());
        }
        return databases;
    }
}
