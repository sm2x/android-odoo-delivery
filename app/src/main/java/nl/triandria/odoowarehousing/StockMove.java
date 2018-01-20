package nl.triandria.odoowarehousing;

import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import database.StockPicking;

public class StockMove extends AppCompatActivity {

    private static final String TAG = StockMove.class.getName();
    private SimpleCursorAdapter adapter;
    private int product_id, location_from_id, location_to_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_move);
        Toolbar toolbar = findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(toolbar);
        Button scanProduct = findViewById(R.id.scan_product);
        BarcodeScan onClickListener = new BarcodeScan();
        scanProduct.setOnClickListener(onClickListener);
        Button selectProduct = findViewById(R.id.select_product);
        selectProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("buttonFrom", "selectProduct");
                new SelectDialog(StockMove.this, args).show();
            }
        });
        Button scanFromLocation = findViewById(R.id.scan_from_location);
        scanFromLocation.setOnClickListener(onClickListener);
        Button selectFromLocation = findViewById(R.id.select_from_location);
        selectFromLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("buttonFrom", "selectFromLocation");
                new SelectDialog(StockMove.this, args).show();
            }
        });
        Button scanToLocation = findViewById(R.id.scan_to_location);
        scanToLocation.setOnClickListener(onClickListener);
        Button selectToLocation = findViewById(R.id.select_to_location);
        selectToLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("buttonFrom", "selectToLocation");
                new SelectDialog(StockMove.this, args).show();
            }
        });
        Button stockMoveOk = findViewById(R.id.button_stock_move_ok);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null && scanResult.getContents() != null) {
            Log.d(TAG, "Successful barcode scan, the encoded information is: " + scanResult.getContents());
            // TODO check who started the search, then depending on that search on the buttonFrom you might have to search remote
        } else {
            Log.d(TAG, "Barcode scan error");
            Toast.makeText(this, getString(R.string.error_barcode_scan), Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class BarcodeScan implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            IntentIntegrator integrator = new IntentIntegrator(StockMove.this);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.setPrompt(getString(R.string.scan_barcode));
            integrator.initiateScan();
        }
    }

    private class SelectDialog extends Dialog implements LoaderManager.LoaderCallbacks {

        private String buttonFrom;

        SelectDialog(Context ctx, Bundle args) {
            super(ctx);
            this.buttonFrom = args.getString("buttonFrom");
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            Log.d(TAG, "SelectDialog on Create called");
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_select);
            ListView dialogSelectListview = findViewById(R.id.dialog_select);
            dialogSelectListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SQLiteCursor cr = (SQLiteCursor) parent.getItemAtPosition(position);
                    switch (buttonFrom) {
                        case "selectProduct":
                            ((TextView) parent.findViewById(R.id.textview_stock_move_product)).setText(
                                    cr.getString(cr.getColumnIndex("name")));
                            StockMove.this.product_id = cr.getInt(cr.getColumnIndex("id"));
                            break;
                        case "selectFromLocation":
                            ((TextView) parent.findViewById(R.id.textview_stock_move_location_from)).setText(
                                    cr.getString(cr.getColumnIndex("name")));
                            StockMove.this.location_from_id = cr.getInt(cr.getColumnIndex("id"));
                            break;
                        case "selectToLocation":
                            ((TextView) parent.findViewById(R.id.textview_stock_move_location_to)).setText(
                                    cr.getString(cr.getColumnIndex("name")));
                            StockMove.this.location_to_id = cr.getInt(cr.getColumnIndex("id"));
                            break;
                    }
                }
            });
            adapter = new SimpleCursorAdapter(
                    this.getContext(),
                    android.R.layout.simple_list_item_1,
                    null,
                    new String[]{"name"},
                    new int[]{android.R.id.text1, android.R.id.text2},
                    0
            );
            dialogSelectListview.setAdapter(adapter);
            Bundle args = new Bundle();
            args.putString("buttonFrom", buttonFrom);
            getLoaderManager().initLoader(0, args, this);
        }

        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            return new StockMove.CustomCursorLoader(this.getContext(), args);
        }

        @Override
        public void onLoadFinished(Loader loader, Object data) {
            adapter.swapCursor((Cursor) data);
        }

        @Override
        public void onLoaderReset(Loader loader) {
            adapter.swapCursor(null);
        }
    }

    static class CustomCursorLoader extends CursorLoader {

        private String buttonFrom;

        private CustomCursorLoader(Context context, Bundle args) {
            super(context);
            buttonFrom = args.getString("buttonFrom");
        }

        @Override
        public Cursor loadInBackground() {
            Log.d(TAG, "LoadinBackground " + this.isStarted());
            String model;
            if (buttonFrom.equals("selectProduct")) {
                model = "product_product";
            } else {
                model = "stock_location";
            }
            final String select_stmt = "SELECT " +
                    "product_product.rowid AS _id, " +
                    "name " +
                    "FROM " + model;
            if (this.isStarted()) {
                SQLiteDatabase db = SQLiteDatabase.openDatabase(
                        this.getContext().getDatabasePath(StockPicking.DATABASE_NAME).getAbsolutePath(),
                        null,
                        SQLiteDatabase.OPEN_READONLY);
                Log.d(TAG, select_stmt);
                return db.rawQuery(select_stmt, null);
            }
            return null;
        }
    }
}
