package nl.triandria.odoowarehousing;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import database.StockPicking;

public class ApplicationSingleton extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // this creates the database
        new StockPicking(this).getReadableDatabase();
    }
}
