package nl.triandria.odoowarehousing;

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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import database.StockPicking;


public class InventoryAdjust extends AppCompatActivity implements LoaderManager.LoaderCallbacks {

    private static final String TAG = InventoryAdjust.class.getName();
    SimpleCursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_adjust);
        Toolbar toolbar = findViewById(R.id.toolbar_activity_inventory_adjust);
        setSupportActionBar(toolbar);
        adapter = new SimpleCursorAdapter(
                this,
                R.layout.row_stock_inventory_line,
                null,
                new String[]{"name", "stock_location_name", "filter"},
                new int[]{R.id.textview_picking_name, R.id.textview_picking_partner, R.id.textview_picking_partner_address},
                0);
        ListView listView = findViewById(R.id.activity_inventory_adjust);
        listView.setOnItemClickListener(new ListViewOnItemClickListener());
        listView.setAdapter(adapter);
        Bundle args = new Bundle();
        getLoaderManager().initLoader(0, args, this);
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
                    "stock_inventory.rowid AS _id, " +
                    "stock_inventory.name AS name, " +
                    "stock_inventory.filter AS filter, " +
                    "stock_location.name AS stock_location_name " +
                    "FROM stock_inventory " +
                    "INNER JOIN stock_location " +
                    "ON stock_inventory.location_id = stock_location.id;";
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

    private class ListViewOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "onItemClick" + parent.getItemAtPosition(position).toString());
            SQLiteCursor cr = (SQLiteCursor) parent.getItemAtPosition(position);
            int _id = cr.getInt(cr.getColumnIndex("_id"));
            Intent intent = new Intent(parent.getContext(), FormInventoryAdjust.class);
            intent.putExtra("_id", _id);
            parent.getContext().startActivity(intent);
        }
    }

}
