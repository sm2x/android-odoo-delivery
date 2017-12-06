package nl.triandria.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import org.alexd.jsonrpc.JSONRPCException;
import org.alexd.jsonrpc.JSONRPCHttpClient;

public class SessionManager {

    public static final String SHARED_PREFERENCES_FILENAME = "odoo.sec";
    public static int logIn(String username, String password, String database, String url, Context context) {
        if (username.isEmpty() || password.isEmpty()){
            throw new InvalidCredentials();
        }
        try {
            //TODO: check if url in valid format
            JSONRPCHttpClient client = new JSONRPCHttpClient(url);
            Object uid = client.call("authenticate", database, username, password);
            if (uid instanceof Integer) {
                SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_FILENAME, Context.MODE_PRIVATE);
                preferences.edit().putInt("uid", (int)uid)
                        .putString("username", username)
                        .putString("password", password)
                        .putString("database", database)
                        .putString("url", url)
                        .apply();
                return (int)uid;
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
}
