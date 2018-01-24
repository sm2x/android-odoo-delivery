package nl.triandria.odoowarehousing;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import database.StockPicking;

public class FormStockPickingActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {

    private static final String TAG = FormStockPickingActivity.class.getName();
    SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_stock_picking);
        Intent sourceIntent = getIntent();
        int id = sourceIntent.getIntExtra("id", 0);
        final String source = sourceIntent.getStringExtra("source");
        Log.d(TAG, "Showing stock_picking record " + id);
        TextView stock_picking_name = findViewById(R.id.textview_stock_picking_name);
        TextView partner_id_name = findViewById(R.id.textview_partner_id_name);
        TextView partner_id_street = findViewById(R.id.textview_partner_id_street);
        ListView stock_picking_lines = findViewById(R.id.listview_stock_picking_lines);
        stock_picking_name.setText(sourceIntent.getStringExtra("stock_picking_name"));
        if (source.equals("incoming") || source.equals("outgoing")) {
            partner_id_name.setText(sourceIntent.getStringExtra("res_partner_name"));
            partner_id_street.setText(sourceIntent.getStringExtra("res_partner_street"));
        } else if (source.equals("internal")) {
            // TODO rename these fields to something more generic/appropriate for multi-usage
            partner_id_name.setText(sourceIntent.getStringExtra("location_id_name"));
            partner_id_street.setText(sourceIntent.getStringExtra("location_dest_id_name"));
        }
        adapter = new SimpleCursorAdapter(
                this,
                R.layout.stock_move_line,
                null,
                new String[]{"name", "product_qty"},
                new int[]{R.id.textview_stock_move_product_name, R.id.textview_stock_move_product_qty},
                0
        );
        stock_picking_lines.setAdapter(adapter);
        Bundle args = new Bundle();
        args.putInt("id", id);
        getLoaderManager().initLoader(0, args, this);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CustomCursorLoader(this, args.getInt("id"));
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

        private int orderId;

        private CustomCursorLoader(Context context, int orderId) {
            super(context);
            this.orderId = orderId;
        }

        @Override
        public Cursor loadInBackground() {
            Log.d(TAG, "LoadinBackground " + this.isStarted());
            final String select_stmt = "SELECT " +
                    "stock_move.rowid AS _id, " +
                    "product_product.name AS name, " +
                    "stock_move.product_uom_qty AS product_qty " +
                    "FROM stock_move INNER JOIN product_product " +
                    "ON stock_move.product_id = product_product.id " +
                    "WHERE stock_move.picking_id = " + this.orderId;
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
