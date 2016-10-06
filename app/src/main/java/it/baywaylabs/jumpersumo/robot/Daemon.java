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

package it.baywaylabs.jumpersumo.robot;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.parrot.arsdk.arcontroller.ARDeviceController;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.FileFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import it.baywaylabs.jumpersumo.utility.Constants;
import it.baywaylabs.jumpersumo.utility.Finder;

/**
 *
 * This AsyncTask run a daemon that execute the .csv or .txt files in <b>DIR_ROBOT_DAEMON</b>. <br />
 * Created on 09/01/16.
 *
 * @author Massimiliano Fiori [massimiliano.fiori@aol.it]
 * @see it.baywaylabs.jumpersumo.utility.Constants
 * @see <a href="http://developer.android.com/reference/android/os/AsyncTask.html" target="_blank">AsyncTask</a>
 */
public class Daemon extends AsyncTask<String, Integer, String> {

    private static final String TAG = Daemon.class.getSimpleName();
    private Context context;
    private PowerManager.WakeLock mWakeLock;

    private Finder f = new Finder();

    private File folder;

    private ARDeviceController deviceController;

    /**
     * Construct needed to control the robot.
     *
     * @param c Context
     * @param deviceController ARDeviceController
     */
    public Daemon(Context c, ARDeviceController deviceController) {
        this.context = c;
        this.deviceController = deviceController;
    }

    /**
     * This method is called when invoke <b>execute()</b>.<br />
     * Do not invoke manually. Use: <i>new Daemon().execute("");</i>
     *
     * @param params blank string.
     * @return null if all is going ok.
     */
    @Override
    protected String doInBackground(String... params) {

        while (folder.listFiles().length > 0)
        {
            // Select only files, no directory.
            File[] files = folder.listFiles((FileFilter) FileFileFilter.FILE);
            // Sorting following FIFO idea.
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

            if (files[0].getName().endsWith(".csv") || files[0].getName().endsWith(".txt")) {
                String commandsList = "";
                try {
                    commandsList = f.getStringFromFile(files[0].getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e(TAG, "Lista comandi: " + commandsList);
                Interpreter in = new Interpreter(deviceController);
                in.doListCommands(commandsList);
                files[0].delete();
            } else {
                Log.e(TAG, "Error: There is no csv|txt files or is not a file but a directory.");
            }
        }
        return null;
    }

    /**
     * Method auto invoked pre execute the task.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
        folder = new File(Constants.DIR_ROBOT_DAEMON);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    /**
     * Method auto invoked post execute the task.
     */
    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
    }
}
