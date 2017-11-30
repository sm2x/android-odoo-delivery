package nl.triandria.odoowarehousing;

import android.app.ListActivity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import database.StockPicking;


public class PickingActivity extends ListFragment implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks {

    private static final int LOAD_LIMIT = 50;
    SimpleCursorAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No pickings.");
        adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.activity_picking_line,
                null,
                new String[]{"name", "customer_name", "street1"},
                new int[]{R.id.textview_picking_name, R.id.textview_picking_customer_name, R.id.textview_picking_street1},
                0);
        setListAdapter(adapter);
        Bundle args = new Bundle();
        args.putInt("load_limit", LOAD_LIMIT);
        getLoaderManager().initLoader(0, args, this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // TODO search from the already loaded records, if not found search on the database if not found return false
        if (!TextUtils.isEmpty(query) && query.length() > 2) {
            adapter.getFilter().filter(query);
            return true;
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflater.inflate(R.layout.activity_picking, container);
        return super.onCreateView(inflater, container, savedInstanceState);
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
            // TODO does this load everything? make sure that sort order can be defined by the user
            // make sure that only 50 are loaded and make sure you have an index to load
            final String select_stmt = "SELECT stock_picking.name, res_partner.name, res_partner.id FROM " +
                    " stock_picking INNER JOIN res_partner ON stock_picking.partner_id = res_partner.id" + getSortOrder();
            if (!this.isStarted()) {
                SQLiteDatabase db = SQLiteDatabase.openDatabase(
                        this.getContext().getDatabasePath(StockPicking.DATABASE_NAME).getAbsolutePath(),
                        null,
                        SQLiteDatabase.OPEN_READONLY);
                return db.rawQuery(select_stmt, null);
            }
            return null;
        }
    }
}
