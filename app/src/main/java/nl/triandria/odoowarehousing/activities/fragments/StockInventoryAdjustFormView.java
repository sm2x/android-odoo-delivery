package nl.triandria.odoowarehousing.activities.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import database.StockPicking;
import nl.triandria.odoowarehousing.R;


public class StockInventoryAdjustFormView extends Fragment implements LoaderManager.LoaderCallbacks {

    private static final String TAG = StockInventoryAdjustFormView.class.getName();
    private SimpleCursorAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final Bundle args = getArguments();
        View formview = inflater.inflate(R.layout.fragment_stock_inventory_adjust_form, container);
        Button qtyToZero = formview.findViewById(R.id.button_inventory_adjust_zero_all);
        Button validate = formview.findViewById(R.id.button_stock_inventory_adjust_validate);
        Button cancel = formview.findViewById(R.id.button_stock_inventory_adjust_cancel);
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.activity_picking_line,
                null,
                new String[]{"name", "location_id", "theoretical_qty", "product_qty"},
                new int[]{R.id.textview_product_name, R.id.textview_product_location_id,
                        R.id.textview_product_theoretical_qty, R.id.textview_product_product_qty},
                0);
        final ListView inventoryAdjustLines = formview.findViewById(R.id.listview_stock_inventory_adjust_lines);
        inventoryAdjustLines.setAdapter(adapter);
        final SQLiteDatabase db = SQLiteDatabase.openDatabase(
                this.getActivity().getDatabasePath(StockPicking.DATABASE_NAME).getAbsolutePath(),
                null,
                SQLiteDatabase.OPEN_READWRITE);
        qtyToZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.execSQL("UPDATE stock_inventory_line SET theoretical_qty = 0 WHERE inventory_id = " + args.getInt("_id"));
                inventoryAdjustLines.invalidate();
            }
        });
        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateInventory();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelInventory();
            }
        });
        return formview;
    }

    private void validateInventory() {

    }

    private void cancelInventory() {

    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CustomCursorLoader(getActivity(), args);
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

        private int _id;

        private CustomCursorLoader(Context context, Bundle args) {
            super(context);
            this._id = args.getInt("_id");
        }

        @Override
        public Cursor loadInBackground() {
            Log.d(TAG, "LoadinBackground " + this.isStarted());
            final String select_stmt = "SELECT " +
                    "rowid AS _id, " +
                    "product_product.name AS name, " +
                    "theoretical_qty, " +
                    "product_qty" +
                    "FROM stock_inventory_line " +
                    "WHERE stock_inventory_line.inventory_id = " +
                    this._id +
                    "INNER JOIN product_product " +
                    "ON stock_inventory_line.product_id = product_product.id ";
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
