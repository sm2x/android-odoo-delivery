package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;

import java.util.Arrays;
import java.util.HashMap;


public class StockPicking extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "stock.db";
    // TODO fill and test
    public static HashMap<String, JSONArray> TABLE_FIELDS = new HashMap<>();
    private static final int DATABASE_VERSION = 1;

    public StockPicking(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static {
        TABLE_FIELDS.put("res_company", new JSONArray(Arrays.asList("id", "name")));
        TABLE_FIELDS.put("res_partner", new JSONArray(Arrays.asList("id", "name", "company_id", "comment",
                "street", "street2", "city", "zip", "country_id", "ref", "barcode")));
        TABLE_FIELDS.put("res_users", new JSONArray(Arrays.asList("id", "company_id", "partner_id")));
        TABLE_FIELDS.put("stock_location", new JSONArray(Arrays.asList("id", "comment", "complete_name",
                "barcode", "name")));
        TABLE_FIELDS.put("stock_picking_type", new JSONArray(Arrays.asList("id", "code", "name")));
        TABLE_FIELDS.put("stock_picking", new JSONArray(Arrays.asList("id", "origin", "date_done", "write_uid",
                "location_id", "priority", "picking_type_id", "partner_id", "move_type", "company_id",
                "note", "state", "owner_id", "backorder_id", "create_uid", "min_date", "write_date",
                "date", "name", "create_date", "location_dest_id", "max_date")));
        TABLE_FIELDS.put("stock_move", new JSONArray(Arrays.asList("id", "product_id", "product_uom_id")));
        TABLE_FIELDS.put("product_template", new JSONArray(Arrays.asList("id", "ean13", "name")));
        TABLE_FIELDS.put("product_product", new JSONArray(Arrays.asList("id", "ean13", "name", "product_tmpl_id")));
        TABLE_FIELDS.put("stock_inventory", new JSONArray(Arrays.asList("id", "name", "date", "location_id", "filter")));
        TABLE_FIELDS.put("stock_inventory_line", new JSONArray(Arrays.asList("id", "product_id", "theoretical_qty", "product_qty")));
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
                    "(" +
                    "id INTEGER UNIQUE," +
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
                    "FOREIGN KEY (write_uid) REFERENCES res_users(id)," +
                    "FOREIGN KEY (location_id) REFERENCES stock_location(id)," +
                    "FOREIGN KEY (picking_type_id) REFERENCES stock_picking_type(id)," +
                    // this has to go because it complicates the sync.
                    //"FOREIGN KEY (partner_id) REFERENCES res_partner(id)," +
                    "FOREIGN KEY (company_id) REFERENCES res_company(id)," +
                    //"FOREIGN KEY (owner_id) REFERENCES res_partner(id)," +
                    //"FOREIGN KEY (backorder_id) REFERENCES stock_picking(id)," +
                    "FOREIGN KEY (create_uid) REFERENCES res_users(id)," +
                    "FOREIGN KEY (location_dest_id) REFERENCES stock_location(id)" +
                    ")");
            db.execSQL("CREATE TABLE res_company " +
                    "(" +
                    "id INTEGER UNIQUE," +
                    "name TEXT NOT NULL" +
                    ")");
            db.execSQL("CREATE TABLE res_users" +
                    "(" +
                    "id INTEGER UNIQUE," +
                    "company_id INTEGER," +
                    "partner_id INTEGER," +
                    "FOREIGN KEY (company_id) REFERENCES res_company(id)" +
                    //"FOREIGN KEY (partner_id) REFERENCES res_partner(id)" +
                    ")");
            db.execSQL("CREATE TABLE res_partner " +
                    "(" +
                    "id INTEGER UNIQUE," +
                    "name TEXT," +
                    "company_id INTEGER," +
                    "comment TEXT," +
                    "street TEXT," +
                    "street2 TEXT," +
                    "city TEXT," +
                    "zip INTEGER," +
                    "country_id INTEGER," +
                    "ref TEXT," +
                    "barcode TEXT" +
                    ")");
            db.execSQL("CREATE TABLE stock_location " +
                    "(" +
                    "id INTEGER UNIQUE," +
                    "comment TEXT," +
                    "complete_name TEXT," +
                    "barcode TEXT," +
                    "name TEXT NOT NULL" +
                    ")");
            db.execSQL("CREATE TABLE stock_picking_type " +
                    "(" +
                    "id INTEGER UNIQUE," +
                    "code TEXT NOT NULL," +
                    "name TEXT NOT NULL" +
                    ")");
            db.execSQL("CREATE TABLE stock_move " +
                    "(" +
                    "id INTEGER UNIQUE," +
                    "product_id INTEGER," +
                    "product_uom_qty FLOAT" +
                    ")");
            db.execSQL("CREATE TABLE product_template " +
                    "(" +
                    "id INTEGER UNIQUE," +
                    "ean13 TEXT," +
                    "name TEXT" +
                    ")");
            db.execSQL("CREATE TABLE product_product " +
                    "(" +
                    "id INTEGER UNIQUE," +
                    "ean13 TEXT," +
                    "name TEXT," +
                    "product_tmpl_id INTEGER NOT NULL," +
                    "FOREIGN KEY (product_tmpl_id) REFERENCES product_template(id)" +
                    ")");
            db.execSQL("CREATE TABLE stock_inventory " +
                    "(" +
                    "id INTEGER UNIQUE," +
                    "name TEXT," +
                    "date INTEGER," +
                    "location_id INTEGER," +
                    "filter TEXT," +
                    "FOREIGN KEY (location_id) REFERENCES stock_location(id)" +
                    ")");
            db.execSQL("CREATE TABLE stock_inventory_line " +
                    "(" +
                    "id INTEGER UNIQUE," +
                    "product_id INTEGER," +
                    "theoretical_qty FLOAT," +
                    "product_qty FLOAT," +
                    "FOREIGN KEY (product_id) REFERENCES product_product(id)" +
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
