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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import it.baywaylabs.jumpersumo.optimized.PriorityQueue;
import it.baywaylabs.jumpersumo.utility.Constants;
import it.baywaylabs.jumpersumo.utility.Finder;

/**
 * This AsyncTask run a daemon that invoke php pages. <br />
 * Created on 18/03/16.
 *
 * @author Massimiliano Fiori [massimiliano.fiori@aol.it]
 * @see it.baywaylabs.jumpersumo.utility.Constants
 * @see <a href="http://developer.android.com/reference/android/os/AsyncTask.html" target="_blank">AsyncTask</a>
 */
// TODO: More tests to run.
public class WebService extends AsyncTask<String, Integer, String> {

    private static final String TAG = WebService.class.getSimpleName();
    private Context context;
    private PowerManager.WakeLock mWakeLock;

    private Finder f = new Finder();

    private ARDeviceController deviceController;

    /**
     * Construct needed to control the robot.
     *
     * @param c                Context
     * @param deviceController ARDeviceController
     */
    public WebService(Context c, ARDeviceController deviceController) {
        this.context = c;
        this.deviceController = deviceController;
    }

    /**
     * This method is called when invoke <b>execute()</b>.<br />
     * Do not invoke manually. Use: <i>new WebService().execute("");</i>
     *
     * @param params blank string.
     * @return null if all is going ok.
     */
    @Override
    protected String doInBackground(String... params) {

        String id = params[0];
        if ("".equals(id)) {
            id = "0";
        }

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(Constants.WEBSERVICE_GET_MSG_URL + "?id=" + id));

            String responseStr = EntityUtils.toString(response.getEntity());
            if (!responseStr.contains("0 results")) {
                String idExec = responseStr.split("<br>")[0].split(" - ")[0].split(": ")[1];
                String textMsg = responseStr.split("<br>")[0].split(" - ")[1].split(": ")[1];

                PriorityQueue result = f.processingMessage(textMsg);
                Log.d(TAG, responseStr);
                Log.d(TAG, "[COMANDO RICONOSCIUTO] ID : " + idExec + " -- " + textMsg);
                // dopo aver eseguito il comando, se tutto è andato bene, aggiorno lo stato
                if (result != null) {
                    int wxecuteResult = f.executePQ(result, deviceController, textMsg);
                    if (wxecuteResult == 1) {
                        Log.e(TAG, "Errore nell'esecuzione della Coda di Priorità.");
                        return null;
                    } else if (wxecuteResult == 2) {
                        Log.e(TAG, "La Coda di Priorità risulta vuota.");
                    }

                    try {
                        HttpClient httpclientExecuted = new DefaultHttpClient();
                        HttpResponse responseExecuted = httpclientExecuted.execute(new HttpGet(Constants.WEBSERVICE_STATE_UPDATE_URL + "?id=" + idExec));
                        String responseStrExecuted = EntityUtils.toString(responseExecuted.getEntity());
                        if ("1".equals(responseStrExecuted)) {
                            Log.d(TAG, "Stato aggiornato con successo!");
                        } else {
                            Log.e(TAG, "ERROR - id " + id + " non aggiornato.");
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "[GET REQUEST ERROR] [" + Constants.WEBSERVICE_STATE_UPDATE_URL + "] " + e.getMessage());
                    }

                } else {
                    Log.e(TAG, "ERROR - errore durante l'esecuzione dei comandi.");
                }
            } else {
                Log.d(TAG, "Nessun comando da eseguire.");
            }
        } catch (Exception e)
        {
            Log.e(TAG, "[GET REQUEST ERROR] [" + Constants.WEBSERVICE_GET_MSG_URL + "] " + e.getMessage());
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
    }

    /**
     * Method auto invoked post execute the task.
     */
    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
    }
}
