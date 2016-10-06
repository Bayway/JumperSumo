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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import it.baywaylabs.jumpersumo.utility.Constants;
import it.baywaylabs.jumpersumo.utility.Finder;

/**
 * This AsyncTask downloads the <i>.csv</i> or <i>.txt</i> file from Url in <b>DIR_ROBOT</b>. <br />
 * Created on 26/11/15.
 *
 * @author Massimiliano Fiori [massimiliano.fiori@aol.it]
 * @see it.baywaylabs.jumpersumo.utility.Constants
 * @see <a href="http://developer.android.com/reference/android/os/AsyncTask.html" target="_blank">AsyncTask</a>
 */
public class ServerPolling extends AsyncTask<String, Integer, String> {

    private static final String TAG = ServerPolling.class.getSimpleName();
    private Context context;
    private PowerManager.WakeLock mWakeLock;

    private Finder f = new Finder();

    private ProgressDialog mProgressDialog = null;

    /**
     * Generic constructor.
     */
    public ServerPolling() {}

    /**
     * Construct needed to view progress bar.
     *
     * @param c Context
     * @param mProgressDialog ProgressDialog
     */
    public ServerPolling(Context c, ProgressDialog mProgressDialog) {
        this.context = c;
        this.mProgressDialog = mProgressDialog;
    }

    /**
     * This method is called when invoke <b>execute()</b>.<br />
     * Do not invoke manually. Use: new ServerPolling().execute(url1, url2, url3);
     *
     * @param sUrl List of Url that will be download.
     * @return null if all is going ok.
     */
    @Override
    protected String doInBackground(String... sUrl) {

        InputStream input = null;
        OutputStream output = null;
        File folder = new File(Constants.DIR_ROBOT);
        String baseName = FilenameUtils.getBaseName(sUrl[0]);
        String extension = FilenameUtils.getExtension(sUrl[0]);
        Log.d(TAG, "FileName: " + baseName + " - FileExt: " + extension);

        if (!folder.exists()) {
            folder.mkdir();
        }
        HttpURLConnection connection = null;
        if (!f.isUrl(sUrl[0])) return "Url malformed!";
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if ( !sUrl[0].endsWith(".csv") && connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream( folder.getAbsolutePath() + "/" + baseName + "." + extension);


            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    /**
     * Method auto invoked execute the task.
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
        if(mProgressDialog != null)
            mProgressDialog.show();
    }

    /**
     * Method auto invoked during execute the task.
     */
    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        if (mProgressDialog != null) {
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }
    }

    /**
     * Method auto invoked post execute the task.
     */
    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
        if (result != null)
            Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context,"File downloaded", Toast.LENGTH_LONG).show();
    }
}