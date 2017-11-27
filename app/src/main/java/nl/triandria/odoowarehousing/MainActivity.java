package nl.triandria.odoowarehousing;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.alexd.jsonrpc.JSONRPCException;
import org.json.JSONException;

import nl.triandria.utilities.SessionManager;
import nl.triandria.utilities.Synchronization;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean isLoggedIn = SessionManager.isLoggedIn(this);
        if (isLoggedIn) {
            //TODO start synch
        } else {
            LoginDialog loginDialog = new LoginDialog();
            loginDialog.show(getFragmentManager(), "dialog_login");
        }
    }

    public static class LoginDialog extends DialogFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View dialogView = inflater.inflate(R.layout.dialog_login, container);
            Button button_login_ok = dialogView.findViewById(R.id.button_login_ok);
            button_login_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences preferences = getActivity().getSharedPreferences(SessionManager.SHARED_PREFERENCES_FILENAME, MODE_PRIVATE);
                    int uid = preferences.getInt("uid", 0);
                    String username = preferences.getString("username", "null");
                    String password = preferences.getString("password", "null");
                    String database = preferences.getString("database", "null");
                    String url = preferences.getString("url", "null");
                    boolean success = SessionManager.logIn(username, password, database, url, getActivity());
                    if (success && uid !=0) {
                        //TODO start sync
                        try {
                            Synchronization.synchronize(getActivity());
                        } catch (JSONRPCException e) {
                            // TODO
                        } catch (JSONException e) {
                            // TODO
                        }
                    } else {
                        // TODO show failure toast with exact failure
                    }

                }
            });
            Button button_login_cancel = dialogView.findViewById(R.id.button_login_cancel);
            button_login_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            return dialogView;
        }
    }

}
