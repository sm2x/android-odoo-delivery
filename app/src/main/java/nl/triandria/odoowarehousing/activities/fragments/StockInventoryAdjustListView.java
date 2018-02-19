package nl.triandria.odoowarehousing.activities.fragments;


import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import database.StockPicking;
import nl.triandria.odoowarehousing.R;

public class StockInventoryAdjustListView extends ListFragment implements LoaderManager.LoaderCallbacks {

    private static final String TAG = StockInventoryAdjustListView.class.getName();
    SimpleCursorAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView listView = (ListView) inflater.inflate(R.layout.fragment_stock_inventory_adjust_list_view, container);
        adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.row_stock_inventory_line,
                null,
                new String[]{"name", "stock_location_name", "filter"},
                new int[]{R.id.textview_picking_name, R.id.textview_picking_partner, R.id.textview_picking_partner_address},
                0);
        listView.setOnItemClickListener(new ListViewOnItemClickListener());
        listView.setAdapter(adapter);
        getLoaderManager().initLoader(0, null, this);
        return listView;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CustomCursorLoader(getActivity());
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
            Bundle args = new Bundle();
            args.putInt("_id", _id);
            StockInventoryAdjustFormView formView = new StockInventoryAdjustFormView();
            formView.setArguments(args);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(parent.getId(), formView);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}
