package nl.triandria.odoowarehousing;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
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
        int _id = getIntent().getIntExtra("_id", 0);
        Log.d(TAG, "Showing stock_picking record " + _id);
        SQLiteDatabase db = SQLiteDatabase.openDatabase(
                this.getDatabasePath(StockPicking.DATABASE_NAME).getAbsolutePath(),
                null,
                SQLiteDatabase.OPEN_READONLY);
        Cursor cr = db.rawQuery("SELECT " +
                "stock_picking.name as stock_picking_name, " +
                "res_partner.name as res_partner_name, " +
                "res_partner.street as res_partner_street " +
                "FROM stock_picking " +
                "INNER JOIN res_partner " +
                "ON " +
                "stock_picking.partner_id = res_partner.id " +
                "WHERE stock_picking.rowid = " +
                _id, null);
        cr.moveToFirst();
        TextView stock_picking_name = findViewById(R.id.textview_stock_picking_name);
        TextView partner_id_name = findViewById(R.id.textview_partner_id_name);
        TextView partner_id_street = findViewById(R.id.textview_partner_id_street);
        ListView stock_picking_lines = findViewById(R.id.listview_stock_picking_lines);
        stock_picking_name.setText(cr.getString(cr.getColumnIndex("stock_picking_name")));
        partner_id_name.setText(cr.getString(cr.getColumnIndex("res_partner_name")));
        partner_id_street.setText(cr.getString(cr.getColumnIndex("res_partner_street")));
        // TODO continue setting up the adapter
        //stock_picking_lines.setAdapter(new SimpleCursorAdapter(this,));
        cr.close();
        db.close();
        Bundle args = new Bundle();
        args.putInt("id", _id);
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
                    "FROM stock_move INNER JOIN product_product " +
                    "ON stock_move.product_id = product_product.id" +
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
