package nl.triandria.odoowarehousing.activities.fragments;

import android.app.Dialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import database.StockPicking;
import nl.triandria.odoowarehousing.R;
import nl.triandria.odoowarehousing.utilities.BarcodeScan;


public class StockMoveFormView extends Fragment {

    private static final String TAG = StockMoveFormView.class.getName();
    private int product_id, location_from_id, location_to_id;
    private SimpleCursorAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        LinearLayout formview = (LinearLayout) inflater.inflate(R.layout.activity_stock_move, container, false);
        Button scanProduct = formview.findViewById(R.id.scan_product);
        BarcodeScan onClickListener = new BarcodeScan(getActivity());
        scanProduct.setOnClickListener(onClickListener);
        Button selectProduct = formview.findViewById(R.id.select_product);
        selectProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("buttonFrom", "selectProduct");
                new SelectDialog(getActivity(), args).show();
            }
        });
        Button scanFromLocation = formview.findViewById(R.id.scan_from_location);
        scanFromLocation.setOnClickListener(onClickListener);
        Button selectFromLocation = formview.findViewById(R.id.select_from_location);
        selectFromLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("buttonFrom", "selectFromLocation");
                new SelectDialog(getActivity(), args).show();
            }
        });
        Button scanToLocation = formview.findViewById(R.id.scan_to_location);
        scanToLocation.setOnClickListener(onClickListener);
        Button selectToLocation = formview.findViewById(R.id.select_to_location);
        selectToLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("buttonFrom", "selectToLocation");
                new SelectDialog(getActivity(), args).show();
            }
        });
        Button stockMoveOk = formview.findViewById(R.id.button_stock_move_ok);
        return formview;
    }

    private class SelectDialog extends Dialog implements LoaderManager.LoaderCallbacks {

        private String buttonFrom;

        SelectDialog(Context ctx, Bundle args) {
            super(ctx);
            this.buttonFrom = args.getString("buttonFrom");
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            Log.d(TAG, "SelectDialog on Create called");
            super.onCreate(savedInstanceState);
            setContentView(android.R.layout.list_content);
            ListView dialogSelectListview = findViewById(android.R.id.list);
            dialogSelectListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SQLiteCursor cr = (SQLiteCursor) parent.getItemAtPosition(position);
                    switch (buttonFrom) {
                        case "selectProduct":
                            ((TextView) parent.findViewById(R.id.textview_stock_move_product)).setText(
                                    cr.getString(cr.getColumnIndex("name")));
                            StockMoveFormView.this.product_id = cr.getInt(cr.getColumnIndex("id"));
                            break;
                        case "selectFromLocation":
                            ((TextView) parent.findViewById(R.id.textview_stock_move_location_from)).setText(
                                    cr.getString(cr.getColumnIndex("name")));
                            StockMoveFormView.this.location_from_id = cr.getInt(cr.getColumnIndex("id"));
                            break;
                        case "selectToLocation":
                            ((TextView) parent.findViewById(R.id.textview_stock_move_location_to)).setText(
                                    cr.getString(cr.getColumnIndex("name")));
                            StockMoveFormView.this.location_to_id = cr.getInt(cr.getColumnIndex("id"));
                            break;
                    }
                }
            });
            adapter = new SimpleCursorAdapter(
                    this.getContext(),
                    android.R.layout.simple_list_item_1,
                    null,
                    new String[]{"name"},
                    new int[]{android.R.id.text1, android.R.id.text2},
                    0
            );
            dialogSelectListview.setAdapter(adapter);
            Bundle args = new Bundle();
            args.putString("buttonFrom", buttonFrom);
            getLoaderManager().initLoader(0, args, this);
        }

        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            return new CustomCursorLoader(this.getContext(), args);
        }

        @Override
        public void onLoadFinished(Loader loader, Object data) {
            adapter.swapCursor((Cursor) data);
        }

        @Override
        public void onLoaderReset(Loader loader) {
            adapter.swapCursor(null);
        }
    }

    static class CustomCursorLoader extends CursorLoader {

        private String buttonFrom;

        private CustomCursorLoader(Context context, Bundle args) {
            super(context);
            buttonFrom = args.getString("buttonFrom");
        }

        @Override
        public Cursor loadInBackground() {
            Log.d(TAG, "LoadinBackground " + this.isStarted());
            String model;
            if (buttonFrom.equals("selectProduct")) {
                model = "product_product";
            } else {
                model = "stock_location";
            }
            final String select_stmt = "SELECT " +
                    "product_product.rowid AS _id, " +
                    "name " +
                    "FROM " + model;
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
