package nl.triandria.odoowarehousing;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.alexd.jsonrpc.JSONRPCException;
import org.json.JSONException;

import java.util.HashMap;

import nl.triandria.utilities.SessionManager;
import nl.triandria.utilities.Synchronization;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonDeliver = (Button) findViewById(R.id.button_deliver);
        buttonDeliver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), PickingActivity.class));
            }
        });
        Button buttonPickup = (Button) findViewById(R.id.button_pickup);
        buttonPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), DeliveryActivity.class));
            }
        });
        boolean isLoggedIn = SessionManager.isLoggedIn(this);

        if (isLoggedIn) {
            //TODO start sync
            Log.d(TAG, "User is logged in, starting sync");
        } else {
            Log.d(TAG, "User is NOT logged in.");
            LoginDialog loginDialog = new LoginDialog();
            loginDialog.setCancelable(false);
            loginDialog.show(getFragmentManager(), "dialog_login");
        }
    }

    public static class LoginDialog extends DialogFragment {

        @Override
        public void onStart() {
            super.onStart();
            this.getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.dialog_login, container);
            Spinner protocols = dialogView.findViewById(R.id.protocol);
            ArrayAdapter protocolAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.protocols, android.R.layout.simple_spinner_item);
            protocolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            protocols.setAdapter(protocolAdapter);
            Button button_login_ok = dialogView.findViewById(R.id.button_login_ok);
            button_login_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String, String> values = getLoginFragmentValues();
                    int uid = SessionManager.logIn(
                            values.get("username"),
                            values.get("password"),
                            values.get("database"),
                            values.get("url"),
                            getActivity());
                    if (uid != 0) {
                        Log.d(TAG, "Login successful, uid ==>" + uid);
                        try {
                            Log.d(TAG, "Starting synchronisation");
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

        private HashMap<String, String> getLoginFragmentValues() {
            HashMap<String, String> values = new HashMap<>();

            return values;
        }





    }

}
