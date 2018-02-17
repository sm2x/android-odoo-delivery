package nl.triandria.odoowarehousing.utilities;

import android.app.Activity;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;

import nl.triandria.odoowarehousing.R;


public class BarcodeScan implements View.OnClickListener {

    private Activity invokingActivity;

    public BarcodeScan(Activity invokingActivity) {
        this.invokingActivity = invokingActivity;
    }

    @Override
    public void onClick(View v) {
        IntentIntegrator integrator = new IntentIntegrator(this.invokingActivity);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setPrompt(this.invokingActivity.getString(R.string.scan_barcode));
        integrator.initiateScan();
    }
}
