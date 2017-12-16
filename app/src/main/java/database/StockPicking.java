package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.List;


public class StockPicking extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "stock.db";
    // TODO fill and test
    public static final HashMap<String, List<String>> TABLE_FIELDS = new HashMap<>();
    private static final int DATABASE_VERSION = 1;
    public StockPicking(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        super.onConfigure(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("CREATE TABLE stock_picking " +
                    "(id INTEGER UNIQUE," +
                    "origin TEXT, " +
                    "date_done INTEGER, " +
                    "write_uid INTEGER," +
                    "location_id INTEGER," +
                    "priority TEXT," +
                    "picking_type_id INTEGER," +
                    "partner_id INTEGER," +
                    "move_type TEXT," +
                    "company_id INTEGER," +
                    "note TEXT," +
                    "state TEXT," +
                    "owner_id INTEGER," +
                    "backorder_id INTEGER," +
                    "create_uid INTEGER," +
                    "min_date INTEGER," +
                    "write_date INTEGER," +
                    "date INTEGER," +
                    "name TEXT," +
                    "create_date INTEGER," +
                    "location_dest_id INTEGER," +
                    "max_date INTEGER," +
                    "FOREIGN KEY (write_uid) REFERENCES res_user(id)," +
                    "FOREIGN KEY (location_id) REFERENCES stock_location(id)," +
                    "FOREIGN KEY (picking_type_id) REFERENCES stock_picking_type(id)," +
                    "FOREIGN KEY (partner_id) REFERENCES res_partner(id)," +
                    "FOREIGN KEY (company_id) REFERENCES res_company(id)," +
                    "FOREIGN KEY (owner_id) REFERENCES res_partner(id)," +
                    "FOREIGN KEY (backorder_id) REFERENCES stock_picking(id)," +
                    "FOREIGN KEY (create_uid) REFERENCES res_users(id)," +
                    "FOREIGN KEY (location_dest_id) REFERENCES stock_location(id)" +
                    ")");
            db.execSQL("CREATE TABLE res_company " +
                    "(id INTEGER UNIQUE," +
                    "name TEXT NOT NULL)");
            db.execSQL("CREATE TABLE res_users" +
                    "(id INTEGER UNIQUE," +
                    "company_id INTEGER," +
                    "partner_id INTEGER," +
                    "FOREIGN KEY (company_id) REFERENCES res_company(id)," +
                    "FOREIGN KEY (partner_id) REFERENCES res_partner(id)" +
                    ")");
            db.execSQL("CREATE TABLE res_partner " +
                    "(id INTEGER UNIQUE," +
                    "name TEXT," +
                    "company_id INTEGER," +
                    "comment TEXT," +
                    "steet TEXT," +
                    "street2 TEXT," +
                    "city TEXT," +
                    "zip INTEGER," +
                    "country_id INTEGER," +
                    "ref TEXT," +
                    "barcode TEXT" +
                    ")");
            db.execSQL("CREATE TABLE stock_location " +
                    "(id INTEGER UNIQUE," +
                    "comment TEXT," +
                    "complete_name TEXT," +
                    "barcode TEXT," +
                    "name TEXT NOT NULL" +
                    ")");
            db.execSQL("CREATE TABLE stock_picking_type " +
                    "(id INTEGER UNIQUE," +
                    "code TEXT NOT NULL," +
                    "name TEXT NOT NULL" +
                    ")");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
