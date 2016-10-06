package it.baywaylabs.jumpersumo;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.os.Handler;
import android.util.Log;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    private static final String TAG = ApplicationTest.class.getSimpleName();


    public ApplicationTest() {

        super(Application.class);


        final Handler handler = new Handler();
        final long oneMinuteMs = 60 * 1000;

        Runnable eachMinute = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Each minute task executing");
                handler.postDelayed(this, oneMinuteMs);
            }
        };

        // Schedule the first execution
        handler.postDelayed(eachMinute, oneMinuteMs);

    }
}