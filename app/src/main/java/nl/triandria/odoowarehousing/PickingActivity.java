package nl.triandria.odoowarehousing;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import database.StockPicking;


public class PickingActivity extends AppCompatActivity{

    private static final String TAG = PickingActivity.class.getName();
    SimpleCursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picking);
        final SQLiteDatabase db = SQLiteDatabase.openDatabase(
                getDatabasePath(StockPicking.DATABASE_NAME).getAbsolutePath(),
                null,
                SQLiteDatabase.OPEN_READONLY);
        adapter = new SimpleCursorAdapter(
                this,
                R.layout.activity_picking_line,
                null,
                new String[]{"id", "stock_picking_name", "res_partner_name", "res_partner_street"},
                new int[]{R.id.textview_picking_name, R.id.textview_picking_partner, R.id.textview_picking_partner_address},
                0);
        ListView listView = findViewById(R.id.activity_picking_layout);
//        listView.setOnItemClickListener(new DeliveryActivity.ListViewOnItemClickListener());
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
        //getLoaderManager().initLoader(0, args, this);
    }



}
