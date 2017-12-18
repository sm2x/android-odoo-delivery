package nl.triandria.odoowarehousing;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import database.StockPicking;

public class DeliveryActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);
        SQLiteDatabase db = new StockPicking(this).getReadableDatabase();
        Cursor cursor = db.query("stock_picking", new String[]{"partner_name", "state"},
                null, null, null, null, null);
        ListAdapter adapter = new CustomCursorAdapter(this, cursor, CustomCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(adapter);
        cursor.close();
    }

    // TODO should accept an empty adapter.
    // As for the data loading, in all the times I want to show all the rows that can fill in the
    // screen plus 20 more
    class CustomCursorAdapter extends CursorAdapter {

        CustomCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return findViewById(R.id.row_delivery_layout);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //TODO if convertView == null, call newView. basically if you just call super here I think you are good
            return super.getView(position, convertView, parent);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView partner_name = view.findViewById(R.id.row_delivery_layout_partner_id);
            TextView state = view.findViewById(R.id.row_delivery_layout_state);
            partner_name.setText(cursor.getString(0));
            state.setText(cursor.getString(1));
        }
    }
}
