package nl.triandria.odoowarehousing;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import database.StockPicking;


public class PickingActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks {

    private static final String TAG = "PickingActivity";
    SimpleCursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picking);
        final SQLiteDatabase db = SQLiteDatabase.openDatabase(
                getDatabasePath(StockPicking.DATABASE_NAME).getAbsolutePath(),
                null,
                SQLiteDatabase.OPEN_READONLY);
        adapter = new SimpleCursorAdapter(
                this,
                R.layout.activity_picking_line,
                null,
                new String[]{"name", "partner_id_name", "street"},
                new int[]{R.id.textview_picking_name, R.id.textview_picking_partner, R.id.textview_picking_partner_address},
                0);
        ListView listView = findViewById(R.id.activity_picking_layout);
        listView.setOnItemClickListener(new DeliveryActivity.ListViewOnItemClickListener());
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String filter = constraint.toString();
                return db.rawQuery("SELECT " +
                                "stock_picking.rowid _id, " +
                                "stock_picking.name AS name, " +
                                "res_partner.name AS partner_id_name, " +
                                "res_partner.street AS street " +
                                "FROM " +
                                "stock_picking " +
                                "INNER JOIN res_partner on res_partner.id = stock_picking.partner_id " +
                                "INNER JOIN stock_picking_type on stock_picking.picking_type_id = stock_picking_type.id " +
                                "WHERE " +
                                "(stock_picking.name LIKE '%" + filter + "%' " +
                                "OR res_partner.name LIKE '%" + filter + "%' " +
                                "OR res_partner.street LIKE '%" + filter + "%') " +
                                "AND stock_picking_type.code = 'incoming'",
                        null);
            }
        });
        listView.setAdapter(adapter);
        Toolbar toolbar = findViewById(R.id.toolbar_activity_picking);
        setSupportActionBar(toolbar);
        Bundle args = new Bundle();
        getLoaderManager().initLoader(0, args, this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG, "Filtering data " + query);
        adapter.getFilter().filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            adapter.getFilter().filter("");
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_activity_delivery_barcode:
                IntentIntegrator integrator = new IntentIntegrator(PickingActivity.this);
                integrator.setBeepEnabled(true);
                integrator.setBarcodeImageEnabled(true);
                integrator.setPrompt(getString(R.string.scan_barcode));
                integrator.initiateScan();
                break;
            case R.id.toolbar_action_licence:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(R.id.dialog_licence);
                builder.show();
                break;
            case R.id.toolbar_action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null && scanResult.getContents() != null) {
            Log.d(TAG, "Successful barcode scan, the encoded information is: " + scanResult.getContents());
            adapter.getFilter().filter(scanResult.getContents());
        } else {
            Log.d(TAG, "Barcode scan error");
            Toast.makeText(this, getString(R.string.error_barcode_scan), Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem searchViewMenuItem = menu.findItem(R.id.toolbar_activity_delivery_search);
        SearchView searchView = (SearchView) searchViewMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CustomCursorLoader(this);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        adapter.swapCursor((Cursor) data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        adapter.swapCursor(null);
    }


    static class CustomCursorLoader extends CursorLoader {

        private CustomCursorLoader(Context context) {
            super(context);
        }

        @Override
        public Cursor loadInBackground() {
            Log.d(TAG, "LoadinBackground " + this.isStarted());
            final String select_stmt = "SELECT " +
                    "stock_picking.rowid _id, " +
                    "stock_picking.name, " +
                    "res_partner.name as partner_id_name, " +
                    "res_partner.street " +
                    "FROM stock_picking INNER JOIN stock_picking_type " +
                    "ON stock_picking.picking_type_id = stock_picking_type.id " +
                    "INNER join res_partner on res_partner.id = stock_picking.partner_id " +
                    "WHERE stock_picking_type.code = 'incoming';";
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
