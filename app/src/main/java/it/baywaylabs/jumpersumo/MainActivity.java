/*
 * Copyright (C) 2015 Massimiliano Fiori [massimiliano.fiori@aol.it].
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.baywaylabs.jumpersumo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;
import com.parrot.arsdk.arsal.ARSALPrint;
import com.parrot.arsdk.arsal.ARSAL_PRINT_LEVEL_ENUM;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import it.baywaylabs.jumpersumo.optimized.Command;
import it.baywaylabs.jumpersumo.optimized.PriorityQueue;
import it.baywaylabs.jumpersumo.robot.WebService;
import it.baywaylabs.jumpersumo.utility.Constants;
import it.baywaylabs.jumpersumo.utility.FileFilter;
import it.baywaylabs.jumpersumo.utility.Finder;

/**
 * Main Android Activity.<br />
 * This class search Jumping Robot on the WAN and show the list of devices found.
 *
 * @author Massimiliano Fiori [massimiliano.fiori@aol.it]
 */
public class MainActivity extends ActionBarActivity implements ARDiscoveryServicesDevicesListUpdatedReceiverDelegate, CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = MainActivity.class.getSimpleName();

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;

    // Carico tutte le librerie necessarie.
    static {
        try {
            System.loadLibrary("arsal");
            System.loadLibrary("arsal_android");
            System.loadLibrary("arnetworkal");
            System.loadLibrary("arnetworkal_android");
            System.loadLibrary("arnetwork");
            System.loadLibrary("arnetwork_android");
            System.loadLibrary("arcommands");
            System.loadLibrary("arcommands_android");
            System.loadLibrary("json");
            System.loadLibrary("ardiscovery");
            System.loadLibrary("ardiscovery_android");
            System.loadLibrary("arstream");
            System.loadLibrary("arstream_android");
            System.loadLibrary("arcontroller");
            System.loadLibrary("arcontroller_android");
            System.loadLibrary("arutils");
            System.loadLibrary("arutils_android");
            System.loadLibrary("ardatatransfer");
            System.loadLibrary("ardatatransfer_android");
            System.loadLibrary("armedia");
            System.loadLibrary("armedia_android");

            Log.i(TAG, "All libraries loaded...");

            System.loadLibrary("opencv_java3");

            Log.i(TAG, "OpenCV3 libraries loaded...");

            ARSALPrint.setMinimumLogLevel(ARSAL_PRINT_LEVEL_ENUM.ARSAL_PRINT_INFO);
        } catch (Exception e) {
            Log.e(TAG, "Problem occured during native library loading", e);
        }
    }

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<ARDiscoveryDeviceService> deviceList;
    private String[] deviceNameList;

    private ARDiscoveryService ardiscoveryService;
    private boolean ardiscoveryServiceBound = false;
    private ServiceConnection ardiscoveryServiceConnection;
    public IBinder discoveryServiceBinder;

    private BroadcastReceiver ardiscoveryServicesDevicesListUpdatedReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Testing
        // String bella = "Hello, it's me. Can you turn left and go on and turn right and go left ancora and take photo va?";
        String bella = "hello can go on and execute http://www.baywaylabs.it/tesi/commands.txt";
        Finder f = new Finder();
        PriorityQueue pq = f.processingMessage(bella);
        /*
        while(!pq.isEmpty())
        {
            Log.e(TAG, ((Command)pq.peek()).getCmd() );
            pq.remove();
        }
        */
        List<String> prova = f.getStringQueue(pq);
        for (String s : prova)
        {
            Log.e(TAG, "Stringa: " + s);
        }
        File folder = new File(Constants.DIR_ROBOT);
        FileFilter ff = new FileFilter();
        File[] list = folder.listFiles(ff);
        Log.e(TAG, "Lista file: " + list.length);
        for (int p=0; p<list.length; p++)
            Log.e(TAG, "Nome file: " + list[p].getName());
        String urlReal = f.getUrls(bella).get(0);
        Log.e(TAG, "Url Stringa numero 1: " + urlReal);



        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        // mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(MainActivity.this);
        // End Testing


        SharedPreferences sharedPref = MainActivity.this.getSharedPreferences(Constants.MY_PREFERENCES, Context.MODE_PRIVATE);
        Log.d(TAG, "Vediamo se ho memorizzato bene: " + sharedPref.getLong(Constants.LAST_ID_MENTIONED, 0));


        initBroadcastReceiver();
        initServiceConnection();

        listView = (ListView) findViewById(R.id.list);

        deviceList = new ArrayList<ARDiscoveryDeviceService>();
        deviceNameList = new String[]{};
        Log.d(TAG, "DEVICES: " + deviceList.toString());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row, R.id.textViewList, deviceNameList);
        adapter.notifyDataSetChanged();

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        //ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ARDiscoveryDeviceService service = deviceList.get(position);

                Intent intent = new Intent(MainActivity.this, PilotingActivity.class);
                intent.putExtra(PilotingActivity.EXTRA_DEVICE_SERVICE, service);


                startActivity(intent);
            }

        });
    }

    private void initServices() {
        if (discoveryServiceBinder == null) {
            Intent i = new Intent(getApplicationContext(), ARDiscoveryService.class);
            getApplicationContext().bindService(i, ardiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            ardiscoveryService = ((ARDiscoveryService.LocalBinder) discoveryServiceBinder).getService();
            ardiscoveryServiceBound = true;

            ardiscoveryService.start();
        }
    }

    private void closeServices() {
        Log.d(TAG, "closeServices ...");

        if (ardiscoveryServiceBound) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ardiscoveryService.stop();

                    getApplicationContext().unbindService(ardiscoveryServiceConnection);
                    ardiscoveryServiceBound = false;
                    discoveryServiceBinder = null;
                    ardiscoveryService = null;
                }
            }).start();
        }
    }

    private void initBroadcastReceiver() {
        ardiscoveryServicesDevicesListUpdatedReceiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(this);
    }

    private void initServiceConnection() {
        ardiscoveryServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                discoveryServiceBinder = service;
                ardiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();
                ardiscoveryServiceBound = true;

                ardiscoveryService.start();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                ardiscoveryService = null;
                ardiscoveryServiceBound = false;
            }
        };
    }

    private void registerReceivers() {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastMgr.registerReceiver(ardiscoveryServicesDevicesListUpdatedReceiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));

    }

    private void unregisterReceivers() {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastMgr.unregisterReceiver(ardiscoveryServicesDevicesListUpdatedReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);

        Log.d(TAG, "onResume ...");

        onServicesDevicesListUpdated();

        registerReceivers();

        initServices();

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause ...");

        unregisterReceivers();
        closeServices();

        super.onPause();
        // test
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onServicesDevicesListUpdated() {
        Log.d(TAG, "onServicesDevicesListUpdated ...");

        List<ARDiscoveryDeviceService> list;

        if (ardiscoveryService != null) {
            list = ardiscoveryService.getDeviceServicesArray();

            deviceList = new ArrayList<ARDiscoveryDeviceService>();
            List<String> deviceNames = new ArrayList<String>();

            if (list != null) {
                for (ARDiscoveryDeviceService service : list) {
                    Log.d(TAG, "service :  " + service + " name = " + service.getName());
                    ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(service.getProductID());
                    Log.d(TAG, "product :  " + product);
                    // only display Jumping Sumo EVO RACE
                    if (ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_JS_EVO_RACE.equals(product)) {
                        deviceList.add(service);
                        deviceNames.add(service.getName());
                    }
                }
            }

            deviceNameList = deviceNames.toArray(new String[deviceNames.size()]);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row, R.id.textViewList, deviceNameList);
            adapter.notifyDataSetChanged();

            // Assign adapter to ListView
            listView.setAdapter(adapter);

            Log.d(TAG, "DEVICES: " + deviceNameList.length);
        }

    }

    // OpenCV Test
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        try {
            zxing(mRgba);
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return mRgba;
    }

    public void zxing(Mat mRgba) throws ChecksumException, FormatException {

        Bitmap bMap = Bitmap.createBitmap(mRgba.width(), mRgba.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba, bMap);
        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(),intArray);

        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new QRCodeMultiReader();

        String sResult = "";
        Double AREA_RIFERIMENTO = 11500.0;

        try {

            Result result = reader.decode(bitmap);
            sResult = result.getText();
            if(result.getBarcodeFormat().compareTo(BarcodeFormat.QR_CODE) == 0)
                Log.d(TAG, "SI! E' Un QRCode");
            ResultPoint[] points = result.getResultPoints();
            Log.d(TAG, "PUNTI: " + points.toString());
            //for (ResultPoint point : result.getResultPoints()) {
            Point a = new Point(points[0].getX(), points[0].getY());
            Point b = new Point(points[2].getX(), points[2].getY());
            Rect rect = new Rect(a, b);
            Log.d(TAG, "Area del rettangolo: " + rect.area());
            if( rect.area() < AREA_RIFERIMENTO)
                Log.w(TAG, "Mi devo avvicinare!");
            else
                Log.w(TAG, "Mi devo allontanare!");
            Imgproc.rectangle(this.mRgba, new Point(points[0].getX(), points[0].getY()), new Point(points[2].getX(), points[2].getY()), new Scalar(0, 255, 0), 3);
            Log.d(TAG, sResult);
            Point center = new Point(0, 0);

            Imgproc.circle(this.mRgba, center, 10, new Scalar(0, 0, 255),2);
            //if (!"".equals(sResult))
                //Toast.makeText(MainActivity.this, "QRCode Scanned: " + sResult, Toast.LENGTH_LONG).show();
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "Code Not Found");
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

    }
}
