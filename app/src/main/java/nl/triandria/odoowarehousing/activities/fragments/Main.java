package nl.triandria.odoowarehousing.activities.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import nl.triandria.odoowarehousing.R;


public class Main extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return setupButtons(inflater.inflate(R.layout.fragment_main, container));
    }

    private View setupButtons(View layout) {
        FragmentManager manager = getFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();
        Button buttonDeliver = layout.findViewById(R.id.button_deliver);
        final Bundle args = new Bundle();
        buttonDeliver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StockInventoryAdjustListView fragment = new StockInventoryAdjustListView();
                args.putString("type", "outgoing");
                fragment.setArguments(args);
                transaction.replace(R.id.layout_main_fragment, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        Button buttonPickup = layout.findViewById(R.id.button_pickup);
        buttonPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StockInventoryAdjustListView fragment = new StockInventoryAdjustListView();
                args.putString("type", "incoming");
                fragment.setArguments(args);
                transaction.replace(R.id.layout_main_fragment, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        Button buttonInternalMoves = layout.findViewById(R.id.button_internal_move);
        buttonInternalMoves.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StockInventoryAdjustListView fragment = new StockInventoryAdjustListView();
                args.putString("type", "internal");
                fragment.setArguments(args);
                transaction.replace(R.id.layout_main_fragment, new StockInventoryAdjustListView());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        Button buttonStockMove = layout.findViewById(R.id.button_stock_move);
        buttonStockMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.layout_main_fragment, new StockInventoryAdjustFormView());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        Button buttonInventoryMove = layout.findViewById(R.id.button_inventory_move);
        buttonInventoryMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.layout_main_fragment, new StockMoveFormView());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        Button buttonInventoryAdjust = layout.findViewById(R.id.button_inventory_adjust);
        buttonInventoryAdjust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.layout_main_fragment, new StockInventoryAdjustListView());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        return layout;
    }
}

