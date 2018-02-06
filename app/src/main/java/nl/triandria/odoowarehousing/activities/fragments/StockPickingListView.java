package nl.triandria.odoowarehousing.activities.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.Arrays;

import database.StockPicking;
import nl.triandria.odoowarehousing.FormStockPickingActivity;
import nl.triandria.odoowarehousing.R;


public class StockPickingListView extends ListFragment implements LoaderManager.LoaderCallbacks {

    SimpleCursorAdapter adapter;
    private static final String TAG = StockPickingListView.class.getName();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final SQLiteDatabase db = SQLiteDatabase.openDatabase(
                getActivity().getDatabasePath(StockPicking.DATABASE_NAME).getAbsolutePath(),
                null,
                SQLiteDatabase.OPEN_READONLY);
        adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.activity_picking_line,
                null,
                new String[]{"id", "stock_picking_name", "res_partner_name", "res_partner_street"},
                new int[]{R.id.textview_picking_name, R.id.textview_picking_partner, R.id.textview_picking_partner_address},
                0);
        ListView listView = container.findViewById(R.id.fragment_stock_picking_listview);
        listView.setOnItemClickListener(new ListViewOnItemClickListener());
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String filter = constraint.toString();
                return db.rawQuery("SELECT " +
                                "stock_picking.rowid _id, " +
                                "stock_picking.name AS stock_picking_name, " +
                                "res_partner.name AS res_partner_name, " +
                                "res_partner.street AS res_partner_street " +
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
        Bundle args = new Bundle();
        getLoaderManager().initLoader(0, args, this);
        return listView;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CustomCursorLoader(this.getActivity());
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
                    "stock_picking.id, " +
                    "stock_picking.name AS stock_picking_name, " +
                    "res_partner.name AS res_partner_name, " +
                    "res_partner.street AS res_partner_street " +
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

    // TODO move other stuff in here as well, the filters. Simplify, refactor. Move searching on the main activity and diversify
    // TODO when entering another fragment

    static class ListViewOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "onItemClick" + parent.getItemAtPosition(position).toString());
            SQLiteCursor cr = (SQLiteCursor) parent.getItemAtPosition(position);
            Log.d(TAG, Arrays.toString(cr.getColumnNames()));
            int _id = cr.getInt(cr.getColumnIndex("id"));
            Intent intent = new Intent(parent.getContext(), FormStockPickingActivity.class);
            intent.putExtra("id", _id);
            intent.putExtra("stock_picking_name", cr.getString(cr.getColumnIndex("stock_picking_name")));
            // TODO remove duplicates
            if (parent.getId() == R.id.activity_delivery_layout) {
                intent.putExtra("res_partner_name", cr.getString(cr.getColumnIndex("res_partner_name")));
                intent.putExtra("res_partner_street", cr.getString(cr.getColumnIndex("res_partner_street")));
                intent.putExtra("source", "outgoing");
            } else if (parent.getId() == R.id.activity_picking_layout) {
                intent.putExtra("res_partner_name", cr.getString(cr.getColumnIndex("res_partner_name")));
                intent.putExtra("res_partner_street", cr.getString(cr.getColumnIndex("res_partner_street")));
                intent.putExtra("source", "incoming");
            } else if (parent.getId() == R.id.activity_internal_move_layout) {
                intent.putExtra("location_id_name", cr.getString(cr.getColumnIndex("location_id_name")));
                intent.putExtra("location_dest_id_name", cr.getString(cr.getColumnIndex("location_dest_id_name")));
                intent.putExtra("source", "internal");
            }
            parent.getContext().startActivity(intent);
        }
    }
}
