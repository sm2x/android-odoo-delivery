package nl.triandria.odoowarehousing;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.URI;
import java.util.HashMap;

import nl.triandria.utilities.SessionManager;

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
            final LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.dialog_login, container);
            Spinner protocols = dialogView.findViewById(R.id.protocol);
            ArrayAdapter protocolAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.protocols, android.R.layout.simple_spinner_item);
            protocolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            protocols.setAdapter(protocolAdapter);
            Spinner database = dialogView.findViewById(R.id.database);
            database.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        //TODO put back
                        HashMap<String, String> values = getLoginFragmentValues(dialogView);
//                        Adapter adapter = ((Spinner) view).getAdapter();
//                        if (adapter == null || adapter.isEmpty()
//                                && values.get("username") != null
//                                && values.get("password") != null
//                                && values.get("protocol") != null
//                                && values.get("url") != null
//                                && values.get("port") != null) {
                            new SessionManager.GetDatabasesTask(LoginDialog.this).execute(URI.create(values.get("url")));
                        //}
                    }
                    view.performClick();
                    return true;
                }
            });
            Button button_login_ok = dialogView.findViewById(R.id.button_login_ok);
            button_login_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String, String> values = getLoginFragmentValues(dialogView);
                    new SessionManager.LogInTask().execute(
                            values.get("url"),
                            values.get("username"),
                            values.get("password"),
                            values.get("database"),
                            dialogView.getContext());
                }
            });
            Button button_login_cancel = dialogView.findViewById(R.id.button_login_cancel);
            button_login_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().finish();
                }
            });
            return dialogView;
        }

        private HashMap<String, String> getLoginFragmentValues(View dialog) {
            HashMap<String, String> values = new HashMap<>();
            String database = "";
            Object selectedDatabase = ((Spinner) dialog.findViewById(R.id.database)).getSelectedItem();
            if (selectedDatabase != null) {
                database = selectedDatabase.toString();
            }
            // TODO put back
//            values.put("username", ((EditText) dialog.findViewById(R.id.username)).getText().toString());
//            values.put("password", ((EditText) dialog.findViewById(R.id.password)).getText().toString());
//            values.put("database", database);
//            values.put("url", getLoginUrl(dialog));
            values.put("username", "admin");
            values.put("password", "admin");
            values.put("database", database);
            values.put("url", "http://10.0.2.2:6069");
            Log.d(TAG, "getLoginValues");
            Log.d(TAG, values.toString());
            return values;
        }

        private String getLoginUrl(View dialog) {
            StringBuilder urlBuilder = new StringBuilder();
            String protocol = "";
            Object selectedProtocol = ((Spinner) dialog.findViewById(R.id.protocol)).getSelectedItem();
            if (selectedProtocol != null) {
                protocol = selectedProtocol.toString() + "://";
            }
            urlBuilder.append(protocol);
            urlBuilder.append(((TextView) dialog.findViewById(R.id.url)).getText().toString());
            urlBuilder.append(':');
            urlBuilder.append(((TextView) dialog.findViewById(R.id.port)).getText().toString());
            return urlBuilder.toString();
        }


    }

}
