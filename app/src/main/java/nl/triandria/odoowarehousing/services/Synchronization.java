package nl.triandria.odoowarehousing.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;


public class Synchronization extends JobService {

    public static final int JOB_ID = 10000;

    @Override
    public boolean onStartJob(JobParameters params) {
        Intent startSync = new Intent();
        startSync.setAction("synchronize");
        sendBroadcast(startSync);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
