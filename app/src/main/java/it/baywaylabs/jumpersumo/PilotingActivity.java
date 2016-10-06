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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARDeviceControllerStreamListener;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;


import it.baywaylabs.jumpersumo.robot.Daemon;
import it.baywaylabs.jumpersumo.robot.Interpreter;
import it.baywaylabs.jumpersumo.robot.ServerPolling;
import it.baywaylabs.jumpersumo.robot.WebService;
import it.baywaylabs.jumpersumo.utility.Constants;
import it.baywaylabs.jumpersumo.twitter.TwitterListener;
import it.baywaylabs.jumpersumo.utility.FTPDownloadImage;
import it.baywaylabs.jumpersumo.utility.FileFilter;
import it.baywaylabs.jumpersumo.utility.Finder;

import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * This class is just based on PilotingActivity of BebopPilotingNewAPI project
 * Addition, this project focus on receive stream video MJpeg of Jumping Sumo <br />
 * Modified by Massimiliano Fiori [massimiliano.fiori@aol.it]
 *
 * @author nguyenquockhai (nqkhai1706@gmail.com) create on 16/07/2015 at Robotics Club.
 * @author Massimiliano Fiori [massimiliano.fiori@aol.it]
 */
public class PilotingActivity extends Activity implements ARDeviceControllerListener, ARDeviceControllerStreamListener {
    private static String TAG = PilotingActivity.class.getSimpleName();
    public static String EXTRA_DEVICE_SERVICE = "pilotingActivity.extra.device.service";
    private String m_Text = "";

    private Finder f = new Finder();

    public ARDeviceController deviceController;
    public ARDiscoveryDeviceService service;
    public ARDiscoveryDevice device;
    public twitter4j.Twitter twitter;

    private String ip_host;

    private ProgressDialog mprogress;
    private ProgressBar spinner;

    private Boolean taskRunning = false;
    private final Handler handler = new Handler();
    private Runnable eachMinute;

    private Button jumHightBt;
    private Button jumLongBt;

    private Button turnLeftBt;
    private Button turnRightBt;

    private Button forwardBt;
    private Button backBt;

    private Button photoBt;

    private Button twitterSyncButton;
    private Button twitterDeSyncButton;

    private Button fileExecuteButton;

    private Button daemonStart;
    private Button daemonStop;

    private Button webserviceStart;
    private Button webserviceStop;

    private TextView batteryLabel;

    private AlertDialog alertDialog;

    private ImageView imgView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piloting);

        initIHM();
        initVideo();

        Intent intent = getIntent();
        service = intent.getParcelableExtra(EXTRA_DEVICE_SERVICE);

        TwitterFactory twitterFactory = new TwitterFactory();
        this.twitter = twitterFactory.getInstance();
        twitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
        twitter.setOAuthAccessToken(new AccessToken(Constants.ACCESS_TOKEN, Constants.ACCESS_TOKEN_SECRET));

        //create the device
        try {
            device = new ARDiscoveryDevice();

            ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) service.getDevice();

            this.ip_host = netDeviceService.getIp();

            device.initWifi(ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_JS, netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
        } catch (ARDiscoveryException e) {
            e.printStackTrace();
            Log.e(TAG, "Error: " + e.getError());
        }

        if (device != null) {
            try {
                //create the deviceController
                deviceController = new ARDeviceController(device);
                deviceController.addListener(this);
                deviceController.addStreamListener(this);
            } catch (ARControllerException e) {
                e.printStackTrace();
            }
        }
    }

    private void initIHM() {
        jumHightBt = (Button) findViewById(R.id.jumHightBt);
        jumHightBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().sendAnimationsJump(ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_HIGH);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        jumLongBt = (Button) findViewById(R.id.jumLongBt);
        jumLongBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().sendAnimationsJump(ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_ENUM.ARCOMMANDS_JUMPINGSUMO_ANIMATIONS_JUMP_TYPE_LONG);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });


        turnRightBt = (Button) findViewById(R.id.turnRightBt);
        turnRightBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) 50);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);

                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        turnLeftBt = (Button) findViewById(R.id.turnLeftBt);
        turnLeftBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) -50);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        forwardBt = (Button) findViewById(R.id.forwardBt);
        forwardBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 50);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 0);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
        backBt = (Button) findViewById(R.id.backBt);
        backBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) -50);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 0);
                            deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 0);
                        }
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        /*
         * TODO: it is possible to implement a different version for the file transfer: http://developer.parrot.com/docs/bebop/#download-pictures-and-videos
         */
        photoBt = (Button) findViewById(R.id.takePhoto);
        photoBt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        if (deviceController != null) {
                            deviceController.getFeatureJumpingSumo().sendMediaRecordPictureV2(); // sendMediaRecordPicture((byte)0);
                            new FTPDownloadImage(ip_host, 21, "anonymous", "", PilotingActivity.this).execute("local");
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);

                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        twitterSyncButton = (Button) findViewById(R.id.twitter_button);
        twitterDeSyncButton = (Button) findViewById(R.id.twitterDe_button);
        twitterSyncButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);

                        if (deviceController != null) {

                            setRepeatingAsyncTask(deviceController);

                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        twitterSyncButton.setVisibility(View.GONE);
                        twitterDeSyncButton.setVisibility(View.VISIBLE);
                        fileExecuteButton.setEnabled(false);
                        daemonStart.setEnabled(false);
                        webserviceStart.setEnabled(false);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
        twitterDeSyncButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);

                        fileExecuteButton.setEnabled(true);
                        webserviceStart.setEnabled(true);
                        twitterSyncButton.setEnabled(true);
                        daemonStart.setEnabled(true);
                        handler.removeCallbacks(eachMinute);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        twitterSyncButton.setVisibility(View.VISIBLE);
                        twitterDeSyncButton.setVisibility(View.GONE);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        spinner = (ProgressBar) findViewById(R.id.progressBar1);

        fileExecuteButton = (Button) findViewById(R.id.fileExecute_button);
        fileExecuteButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);

                        twitterSyncButton.setEnabled(false);
                        fileExecuteButton.setEnabled(false);
                        webserviceStart.setEnabled(false);
                        daemonStart.setEnabled(false);
                        handler.removeCallbacks(eachMinute);

                        AlertDialog.Builder builder = new AlertDialog.Builder(PilotingActivity.this);
                        builder.setTitle("Download Commands File");

                        final EditText input = new EditText(PilotingActivity.this);
                        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        builder.setView(input);

                        // Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                m_Text = input.getText().toString();
                                Log.d(TAG, "URL File dei comandi: " + m_Text);

                                if (!"".equals(m_Text)) {
                                    taskRunning = true;
                                    String result = "";
                                    try {
                                        result = new ServerPolling(PilotingActivity.this, mprogress).execute(m_Text).get();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                    Log.d(TAG, "RESULT : " + result);
                                    if (result == null) {
                                        File folder = new File(Constants.DIR_ROBOT);
                                        FileFilter ff = new FileFilter();
                                        File[] list = folder.listFiles(ff);
                                        if (list != null && list.length >= 1) {
                                            spinner.setVisibility(View.VISIBLE);
                                            for (int i = 0; i < list.length; i++) {
                                                if (list[i].getName().endsWith(".csv") || list[i].getName().endsWith(".txt")) {
                                                    String commandsList = "";
                                                    try {
                                                        commandsList = f.getStringFromFile(list[i].getPath());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    Log.e(TAG, "Lista comandi: " + commandsList);
                                                    Interpreter in = new Interpreter(deviceController);
                                                    in.doListCommands(commandsList);
                                                    list[i].delete();
                                                }
                                            }
                                        } else {
                                            Log.e(TAG, "Error: There is no csv|txt files or is not a file but a directory.");
                                        }
                                        spinner.setVisibility(View.GONE);
                                    }
                                    taskRunning = false;
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);

                        break;

                    default:

                        break;
                }
                twitterSyncButton.setEnabled(true);
                fileExecuteButton.setEnabled(true);
                webserviceStart.setEnabled(true);
                daemonStart.setEnabled(true);
                return true;
            }
        });

        daemonStart = (Button) findViewById(R.id.daemonStart);
        daemonStop = (Button) findViewById(R.id.daemonStop);
        daemonStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);

                        if (deviceController != null) {

                            setRepeatingAsyncTaskDaemon(deviceController);

                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        daemonStart.setVisibility(View.GONE);
                        daemonStop.setVisibility(View.VISIBLE);
                        fileExecuteButton.setEnabled(false);
                        twitterSyncButton.setEnabled(false);
                        webserviceStart.setEnabled(false);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
        daemonStop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);

                        fileExecuteButton.setEnabled(true);
                        twitterSyncButton.setEnabled(true);
                        webserviceStart.setEnabled(true);
                        handler.removeCallbacks(eachMinute);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        daemonStop.setVisibility(View.GONE);
                        daemonStart.setVisibility(View.VISIBLE);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        webserviceStart = (Button) findViewById(R.id.webservicStart);
        webserviceStop = (Button) findViewById(R.id.webserviceStop);
        webserviceStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);

                        if (deviceController != null) {

                            setRepeatingAsyncTaskWebservice(deviceController);

                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        webserviceStart.setVisibility(View.GONE);
                        webserviceStop.setVisibility(View.VISIBLE);
                        fileExecuteButton.setEnabled(false);
                        twitterSyncButton.setEnabled(false);
                        daemonStart.setEnabled(false);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });
        webserviceStop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);

                        fileExecuteButton.setEnabled(true);
                        twitterSyncButton.setEnabled(true);
                        daemonStart.setEnabled(true);
                        handler.removeCallbacks(eachMinute);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        webserviceStop.setVisibility(View.GONE);
                        webserviceStart.setVisibility(View.VISIBLE);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        batteryLabel = (TextView) findViewById(R.id.batteryLabel);
    }

    @Override
    public void onStart() {
        super.onStart();

        //start the deviceController
        if (deviceController != null) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PilotingActivity.this);

            // set title
            alertDialogBuilder.setTitle("Connecting ...");


            // create alert dialog
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            ARCONTROLLER_ERROR_ENUM error = deviceController.start();

            if (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
                finish();
            }
        }
    }

    private void stopDeviceController() {
        if (deviceController != null) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PilotingActivity.this);

            // set title
            alertDialogBuilder.setTitle("Disconnecting ...");

            // show it
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // create alert dialog
                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    ARCONTROLLER_ERROR_ENUM error = deviceController.stop();

                    if (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
                        finish();
                    }
                }
            });
        }
    }

    @Override
    protected void onStop() {
        if (deviceController != null) {
            deviceController.stop();
        }
        handler.removeCallbacks(eachMinute);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        stopDeviceController();
        handler.removeCallbacks(eachMinute);
    }

    public void onUpdateBattery(final int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                batteryLabel.setText(String.format("%d%%", percent));
            }
        });
    }


    @Override
    public void onStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error) {
        Log.i(TAG, "onStateChanged ... newState:" + newState + " error: " + error);

        switch (newState) {
            case ARCONTROLLER_DEVICE_STATE_RUNNING:
                //The deviceController is started
                Log.i(TAG, "ARCONTROLLER_DEVICE_STATE_RUNNING .....");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //alertDialog.hide();
                        alertDialog.dismiss();
                    }
                });
                deviceController.getFeatureJumpingSumo().sendMediaStreamingVideoEnable((byte) 1);
                break;

            case ARCONTROLLER_DEVICE_STATE_STOPPED:
                //The deviceController is stoped
                Log.i(TAG, "ARCONTROLLER_DEVICE_STATE_STOPPED .....");

                deviceController.dispose();
                deviceController = null;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //alertDialog.hide();
                        alertDialog.dismiss();
                        finish();
                    }
                });
                break;

            default:
                break;
        }
    }


    @Override
    public void onCommandReceived(ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary) {
        if (elementDictionary != null) {
            if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED) {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);

                if (args != null) {
                    Integer batValue = (Integer) args.get("arcontroller_dictionary_key_common_commonstate_batterystatechanged_percent");

                    onUpdateBattery(batValue);
                }
            }
        } else {
            Log.e(TAG, "elementDictionary is null");
        }
    }

    @Override
    public void onFrameReceived(ARDeviceController deviceController, ARFrame frame) {
        if (!frame.isIFrame())
            return;

        if (!taskRunning) {
            byte[] data = frame.getByteData();
            ByteArrayInputStream ins = new ByteArrayInputStream(data);
            Bitmap bmp = BitmapFactory.decodeStream(ins);

            // FrameDisplay fDisplay = new FrameDisplay(imgView, bmp);
            // Use FrameDisplayCV to find qr-code with robot cam.
            FrameDisplayCV fDisplay = new FrameDisplayCV(imgView, bmp, deviceController, true);
            fDisplay.execute();
        }
    }


    @Override
    public void onFrameTimeout(ARDeviceController deviceController) {
        Log.i(TAG, "onFrameTimeout ..... ");
    }

    //region video
    public void initVideo() {
        imgView = (ImageView) findViewById(R.id.imageView);
    }

    //endregion video

    private void setRepeatingAsyncTask(final ARDeviceController dc) {

        final long oneMinuteMs = 60 * 1000;

        eachMinute = new Runnable() {
            @Override
            public void run() {
                taskRunning = true;
                spinner.setVisibility(View.VISIBLE);
                Log.d(TAG, "Each minute task executing");
                try {
                    List<String> result = new TwitterListener(PilotingActivity.this, dc, twitter, ip_host).execute("").get();

                    if (result != null && result.size() != 0)
                        Log.d(TAG, "AsyncTask concluso");

                } catch (Exception e) {
                    // error, do something
                    Log.e(TAG, "AsyncTask Repeat Error: " + e.getMessage());
                }
                taskRunning = false;
                spinner.setVisibility(View.GONE);
                handler.postDelayed(this, oneMinuteMs);
            }
        };

        // Schedule the first execution
        handler.postDelayed(eachMinute, oneMinuteMs);

    }

    private void setRepeatingAsyncTaskDaemon(final ARDeviceController dc) {

        final long oneMinuteMs = 60 * 1000;

        eachMinute = new Runnable() {
            @Override
            public void run() {
                taskRunning = true;
                spinner.setVisibility(View.VISIBLE);
                Log.d(TAG, "Each minute task executing");
                try {
                    String result = new Daemon(PilotingActivity.this, dc).execute("").get();

                    if (result != null)
                        Log.d(TAG, "AsyncTask concluso");

                } catch (Exception e) {
                    // error, do something
                    Log.e(TAG, "AsyncTask Repeat Error: " + e.getMessage());
                }
                taskRunning = false;
                spinner.setVisibility(View.GONE);
                handler.postDelayed(this, oneMinuteMs);
            }
        };

        // Schedule the first execution
        handler.postDelayed(eachMinute, oneMinuteMs);

    }

    private void setRepeatingAsyncTaskWebservice(final ARDeviceController dc) {

        final long oneMinuteMs = 60 * 1000;

        eachMinute = new Runnable() {
            @Override
            public void run() {
                taskRunning = true;
                spinner.setVisibility(View.VISIBLE);
                Log.d(TAG, "Each minute task executing");
                try {
                    String result = new WebService(PilotingActivity.this, dc).execute("").get();

                    if (result != null)
                        Log.d(TAG, "AsyncTask concluso");

                } catch (Exception e) {
                    // error, do something
                    Log.e(TAG, "AsyncTask Repeat Error: " + e.getMessage());
                }
                taskRunning = false;
                spinner.setVisibility(View.GONE);
                handler.postDelayed(this, oneMinuteMs);
            }
        };

        // Schedule the first execution
        handler.postDelayed(eachMinute, oneMinuteMs);

    }

}