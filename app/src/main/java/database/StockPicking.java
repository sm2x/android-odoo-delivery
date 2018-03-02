package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Set;


public class StockPicking extends SQLiteOpenHelper {

    private static final String TAG = StockPicking.class.getName();
    public static final String DATABASE_NAME = "stock.db";
    public static FieldMap<String, FieldMap<String, String>> TABLE_STRUCTURE = new FieldMap<>();
    private static final int DATABASE_VERSION = 1;

    public StockPicking(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static {
        FieldMap<String, String> res_company_fields = new FieldMap<>();
        res_company_fields.put("id", "INTEGER");
        res_company_fields.put("name", "TEXT");
        FieldMap<String, String> res_partner_fields = new FieldMap<>();
        res_partner_fields.put("id", "INTEGER");
        res_partner_fields.put("name", "TEXT");
        res_partner_fields.put("company_id", "INTEGER");
        res_partner_fields.put("comment", "TEXT");
        res_partner_fields.put("street", "TEXT");
        res_partner_fields.put("street2", "TEXT");
        res_partner_fields.put("city", "TEXT");
        res_partner_fields.put("zip", "TEXT");
        res_partner_fields.put("country_id", "INTEGER");
        res_partner_fields.put("ref", "TEXT");
        res_partner_fields.put("barcode", "TEXT");
        FieldMap<String, String> res_users_fields = new FieldMap<>();
        res_users_fields.put("id", "INTEGER");
        res_users_fields.put("company_id", "INTEGER");
        res_users_fields.put("partner_id", "INTEGER");
        FieldMap<String, String> stock_location_fields = new FieldMap<>();
        stock_location_fields.put("id", "INTEGER");
        stock_location_fields.put("comment", "TEXT");
        stock_location_fields.put("complete_name", "TEXT");
        stock_location_fields.put("barcode", "TEXT");
        stock_location_fields.put("name", "TEXT");
        FieldMap<String, String> stock_picking_type_fields = new FieldMap<>();
        stock_picking_type_fields.put("id", "INTEGER");
        stock_picking_type_fields.put("code", "TEXT");
        stock_picking_type_fields.put("name", "TEXT");
        FieldMap<String, String> stock_picking_fields = new FieldMap<>();
        stock_picking_fields.put("id", "INTEGER");
        stock_picking_fields.put("origin", "TEXT");
        stock_picking_fields.put("date_done", "TEXT");
        stock_picking_fields.put("write_uid", "INTEGER");
        stock_picking_fields.put("location_id", "INTEGER");
        stock_picking_fields.put("priority", "TEXT");
        stock_picking_fields.put("picking_type_id", "INTEGER");
        stock_picking_fields.put("partner_id", "INTEGER");
        stock_picking_fields.put("move_type", "TEXT");
        stock_picking_fields.put("company_id", "INTEGER");
        stock_picking_fields.put("note", "TEXT");
        stock_picking_fields.put("state", "TEXT");
        stock_picking_fields.put("owner_id", "INTEGER");
        stock_picking_fields.put("backorder_id", "INTEGER");
        stock_picking_fields.put("create_uid", "INTEGER");
        stock_picking_fields.put("min_date", "TEXT");
        stock_picking_fields.put("write_date", "TEXT");
        stock_picking_fields.put("date", "TEXT");
        stock_picking_fields.put("name", "TEXT");
        stock_picking_fields.put("create_date", "TEXT");
        stock_picking_fields.put("location_dest_id", "INTEGER");
        stock_picking_fields.put("max_date", "TEXT");
        FieldMap<String, String> stock_move_fields = new FieldMap<>();
        stock_move_fields.put("id", "INTEGER");
        stock_move_fields.put("product_id", "INTEGER");
        stock_move_fields.put("product_uom_qty", "FLOAT");
        stock_move_fields.put("picking_id", "INTEGER");
        FieldMap<String, String> product_template_fields = new FieldMap<>();
        product_template_fields.put("id", "INTEGER");
        product_template_fields.put("ean13", "TEXT");
        product_template_fields.put("name", "TEXT");
        FieldMap<String, String> product_product_fields = new FieldMap<>();
        product_product_fields.put("id", "INTEGER");
        product_product_fields.put("ean13", "TEXT");
        product_product_fields.put("name", "TEXT");
        product_product_fields.put("product_tmpl_id", "TEXT");
        FieldMap<String, String> stock_inventory_fields = new FieldMap<>();
        stock_inventory_fields.put("id", "INTEGER");
        stock_inventory_fields.put("name", "TEXT");
        stock_inventory_fields.put("date", "TEXT");
        stock_inventory_fields.put("location_id", "INTEGER");
        stock_inventory_fields.put("filter", "TEXT");
        FieldMap<String, String> stock_inventory_line_fields = new FieldMap<>();
        stock_inventory_line_fields.put("id", "INTEGER");
        stock_inventory_line_fields.put("product_id", "INTEGER");
        stock_inventory_line_fields.put("theoretical_qty", "INTEGER");
        stock_inventory_line_fields.put("product_qty", "INTEGER");
        stock_inventory_line_fields.put("inventory_id", "INTEGER");
        TABLE_STRUCTURE.put("res_company", res_company_fields);
        TABLE_STRUCTURE.put("res_partner", res_partner_fields);
        TABLE_STRUCTURE.put("res_users", res_users_fields);
        TABLE_STRUCTURE.put("stock_location", stock_location_fields);
        TABLE_STRUCTURE.put("stock_picking_type", stock_picking_type_fields);
        TABLE_STRUCTURE.put("stock_picking", stock_picking_fields);
        TABLE_STRUCTURE.put("stock_move", stock_move_fields);
        TABLE_STRUCTURE.put("product_template", product_template_fields);
        TABLE_STRUCTURE.put("product_product", product_product_fields);
        TABLE_STRUCTURE.put("stock_inventory", stock_inventory_fields);
        TABLE_STRUCTURE.put("stock_inventory_line", stock_inventory_line_fields);
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
            for (String table_name : (Set<String>) TABLE_STRUCTURE.keySet()) {
                StringBuilder sql_create_table_builder = new StringBuilder();
                sql_create_table_builder.append("CREATE TABLE ");
                sql_create_table_builder.append(table_name);
                sql_create_table_builder.append("(");
                int counter = 1;
                FieldMap<String, String> fieldMap = (FieldMap<String, String>) TABLE_STRUCTURE.get(table_name);
                int field_count = fieldMap.keySet().size();
                for (String field_name : ((Set<String>) fieldMap.keySet())) {
                    sql_create_table_builder.append(field_name);
                    sql_create_table_builder.append(' ');
                    sql_create_table_builder.append(fieldMap.get(field_name));
                    Log.d(TAG, new Integer(field_count).toString());
                    if (field_count != counter) {
                        sql_create_table_builder.append(',');
                    }
                    counter++;
                }
                sql_create_table_builder.append(")");
                sql_create_table_builder.append(';');
                db.execSQL(sql_create_table_builder.toString());
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static class FieldMap<K, V> extends HashMap {
        public JSONArray keySetToJsonArray() {
            JSONArray array = new JSONArray();
            // todo
            Log.d(TAG, this.keySet().toArray().toString());
            for (final Object key : this.keySet().toArray()) {
                array.put(key);
            }
            return array;
        }
    }
}
