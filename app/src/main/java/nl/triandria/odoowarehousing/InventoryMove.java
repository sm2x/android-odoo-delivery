package nl.triandria.odoowarehousing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class InventoryMove extends AppCompatActivity {

    private static final String TAG = "StockMove";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_move);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(toolbar);
        Button scanProduct = (Button) findViewById(R.id.button_scan_product_inventory_move);
        BarcodeScan onClickListener = new BarcodeScan();
        scanProduct.setOnClickListener(onClickListener);
        Button selectProduct = (Button) findViewById(R.id.button_select_product_inventory_move);
        Button scanFromLocation = (Button) findViewById(R.id.button_scan_from_location_inventory_move);
        scanFromLocation.setOnClickListener(onClickListener);
        Button selectFromLocation = (Button) findViewById(R.id.button_select_from_location_inventory_move);
        Button stockMoveOk = (Button) findViewById(R.id.button_inventory_move_ok);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null && scanResult.getContents() != null) {
            Log.d(TAG, "Successful barcode scan, the encoded information is: " + scanResult.getContents());
            // TODO search for product locally, if found assign it else search remote then assign, if not found again throw error
        }
        else if (scanResult != null && scanResult.getContents() == null) {
            Log.d(TAG, "Barcode scan cancelled");
        }
        else {
            Log.d(TAG, "Barcode scan error");
            Toast.makeText(this, getString(R.string.error_barcode_scan), Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class BarcodeScan implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            IntentIntegrator integrator = new IntentIntegrator(InventoryMove.this);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.setPrompt(getString(R.string.scan_barcode));
            integrator.initiateScan();
        }
    }
}
