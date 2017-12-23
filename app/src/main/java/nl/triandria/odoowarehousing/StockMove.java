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

public class StockMove extends AppCompatActivity {

    private static final String TAG = "StockMove";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_move);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(toolbar);
        Button scanProduct = (Button) findViewById(R.id.scan_product);
        BarcodeScan onClickListener = new BarcodeScan();
        scanProduct.setOnClickListener(onClickListener);
        Button selectProduct = (Button) findViewById(R.id.select_product);
        Button scanFromLocation = (Button) findViewById(R.id.scan_from_location);
        scanFromLocation.setOnClickListener(onClickListener);
        Button selectFromLocation = (Button) findViewById(R.id.select_from_location);
        Button scanToLocation = (Button) findViewById(R.id.scan_to_location);
        scanToLocation.setOnClickListener(onClickListener);
        Button selectToLocation = (Button) findViewById(R.id.select_to_location);
        Button stockMoveOk = (Button) findViewById(R.id.button_stock_move_ok);
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
            IntentIntegrator integrator = new IntentIntegrator(StockMove.this);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.setPrompt(getString(R.string.scan_barcode));
            integrator.initiateScan();
        }
    }
}
