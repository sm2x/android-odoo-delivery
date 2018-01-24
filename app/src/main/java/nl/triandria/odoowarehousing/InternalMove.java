package nl.triandria.odoowarehousing;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import database.StockPicking;


public class InternalMove extends AppCompatActivity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks {

    private static final String TAG = InternalMove.class.getName();
    SimpleCursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_move);
        Toolbar toolbar = findViewById(R.id.toolbar_activity_internal_move);
        setSupportActionBar(toolbar);
        adapter = new SimpleCursorAdapter(
                this,
                R.layout.activity_picking_line,
                null,
                new String[]{"stock_picking_name", "location_id_name", "location_dest_id_name"},
                new int[]{R.id.textview_picking_name, R.id.textview_picking_partner, R.id.textview_picking_partner_address},
                0);
        ListView listView = findViewById(R.id.activity_internal_move_layout);
        listView.setOnItemClickListener(new DeliveryActivity.ListViewOnItemClickListener());
        listView.setAdapter(adapter);
        Bundle args = new Bundle();
        getLoaderManager().initLoader(0, args, this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
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
                    "stock_picking.rowid AS _id, " +
                    "stock_picking.name AS stock_picking_name, " +
                    "stock_picking.id, " +
                    "from_location.name AS location_id_name, " +
                    "to_location.name AS location_dest_id_name " +
                    "FROM stock_picking INNER JOIN stock_picking_type " +
                    "ON stock_picking.picking_type_id = stock_picking_type.id " +
                    "INNER JOIN stock_location from_location ON from_location.id = stock_picking.location_id " +
                    "INNER JOIN stock_location to_location ON to_location.id = stock_picking.location_dest_id " +
                    "WHERE stock_picking_type.code = 'internal';";
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
