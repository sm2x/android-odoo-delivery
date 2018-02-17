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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import database.StockPicking;
import nl.triandria.odoowarehousing.R;

public class StockPickingFormView extends Fragment implements LoaderManager.LoaderCallbacks {

    private static final String TAG = StockPickingFormView.class.getName();
    SimpleCursorAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        LinearLayout formview = (LinearLayout) inflater.inflate(R.layout.fragment_stock_picking_form_view, container);
        Bundle args = getArguments();
        int id = args.getInt("id", 0);
        final String source = args.getString("source");
        Log.d(TAG, "Showing stock_picking record " + id);
        TextView stock_picking_name = formview.findViewById(R.id.textview_stock_picking_name);
        TextView partner_id_name = formview.findViewById(R.id.textview_partner_id_name);
        TextView partner_id_street = formview.findViewById(R.id.textview_partner_id_street);
        ListView stock_picking_lines = formview.findViewById(R.id.listview_stock_picking_lines);
        stock_picking_name.setText(args.getString("stock_picking_name"));
        partner_id_name.setText(args.getString("res_partner_name"));
        partner_id_street.setText(args.getString("res_partner_street"));
        if (source.equals("internal")) {
            partner_id_name.setText(args.getString("location_id_name"));
            partner_id_street.setText(args.getString("location_dest_id_name"));
        }
        adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.stock_move_line,
                null,
                new String[]{"name", "product_qty"},
                new int[]{R.id.textview_stock_move_product_name, R.id.textview_stock_move_product_qty},
                0
        );
        stock_picking_lines.setAdapter(adapter);
        args.putInt("id", id);
        getLoaderManager().initLoader(0, args, this);
        return formview;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CustomCursorLoader(getActivity(), args.getInt("id"));
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
