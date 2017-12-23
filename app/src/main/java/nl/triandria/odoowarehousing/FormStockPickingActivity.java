package nl.triandria.odoowarehousing;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import database.StockPicking;

public class FormStockPickingActivity extends AppCompatActivity {

    // TODO switch every other TAG string to fetch its value like this
    private static final String TAG = FormStockPickingActivity.class.getName();

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
        cr.moveToFirst();// TODO if true continue
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
    }
}
