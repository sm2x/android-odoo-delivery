package nl.triandria.odoowarehousing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class FormInventoryAdjust extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_inventory_adjust);
        Button qtyToZero = findViewById(R.id.button_inventory_adjust_zero_all);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.activity_picking_line,
                null,
                new String[]{"default_code", "name", "street"},
                new int[]{R.id.textview_picking_name, R.id.textview_picking_partner, R.id.textview_picking_partner_address},
                0);
        ListView inventoryAdjustLines = findViewById(R.id.listview_stock_inventory_adjust_lines);
        inventoryAdjustLines.setAdapter(adapter);
        qtyToZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
