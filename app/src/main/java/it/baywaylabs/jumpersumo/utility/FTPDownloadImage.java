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

package it.baywaylabs.jumpersumo.utility;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This AsyncTask downloads image from FTP server runs on robot.<br />
 * Created on 01/12/15.
 *
 * @author Massimiliano Fiori [massimiliano.fiori@aol.it]
 * @see <a href="http://developer.android.com/reference/android/os/AsyncTask.html" target="_blank">AsyncTask</a>
 */
public class FTPDownloadImage extends AsyncTask<String, Integer, String> {

    private FTPClient mFTPClient = null;

    private String host = "";
    private Integer port = 0;
    private String user = "";
    private String pass = "";

    private static final String TAG = FTPDownloadImage.class.getSimpleName();
    private Context context;
    private PowerManager.WakeLock mWakeLock;

    /**
     * @param host FTP Address.
     * @param port FTP Port.
     * @param user FTP User.
     * @param pswd FTP Password.
     * @param c Context.
     */
    public FTPDownloadImage(String host, Integer port, String user, String pswd, Context c) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pswd;
        this.context = c;
    }

    /**
     * Method auto invoked pre execute the task.<br />
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
     * Background thread that download photo from the robot and prepare it for local processing or for Twitter reply.<br />
     * This method is called when invoke <b>execute()</b>.<br />
     * Do not invoke manually. Use: <i>new FTPDownloadImage().execute("");</i>
     *
     * @param params The parameters of the task. They can be <b>"local"</b> or <b>"twitter"</b>.
     * @return Null if everything was going ok.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected String doInBackground(String... params) {

        String resultFileName = "";
        try {
            mFTPClient = new FTPClient();
            // connecting to the host
            mFTPClient.connect(host, port);

            // Now check the reply code, if positive mean connection success
            if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {

                // Login using username & password
                boolean status = mFTPClient.login(user, pass);
                mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                mFTPClient.enterLocalPassiveMode();

                mFTPClient.changeWorkingDirectory(Constants.DIR_ROBOT_MEDIA);
                FTPFile[] fileList = mFTPClient.listFiles();
                long timestamp = 0l;
                String nameFile = "";
                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].isFile() && fileList[i].getTimestamp().getTimeInMillis() > timestamp) {
                        timestamp = fileList[i].getTimestamp().getTimeInMillis();
                        nameFile = fileList[i].getName();
                    }
                }
                Log.d(TAG, "File da scaricare: " + nameFile);

                mFTPClient.enterLocalActiveMode();
                File folder = new File(Constants.DIR_ROBOT_IMG);
                OutputStream outputStream = null;
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdir();
                }

                if (params.length != 0 && !"".equals(nameFile))
                    if ("local".equals(params[0])) {
                        try {
                            outputStream = new FileOutputStream(folder.getAbsolutePath() + "/" + nameFile);
                            success = mFTPClient.retrieveFile(nameFile, outputStream);
                        } catch (Exception e) {
                            return e.getMessage();
                        } finally {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }
                        if (success) {
                            resultFileName = nameFile;
                            ContentResolver contentResolver = context.getContentResolver();
                            Bitmap bitmap = BitmapFactory.decodeFile(folder.getAbsolutePath() + "/" + nameFile);
                            Log.e(TAG, "FileName: " + folder.getAbsolutePath() + "/" + nameFile);
                            MediaStore.Images.Media.insertImage(contentResolver, bitmap, nameFile, "Jumper Sumo Photo");

                            mFTPClient.deleteFile(nameFile);

                            File[] list = folder.listFiles();
                            for (int i = 0; i < list.length; i++) {
                                if (nameFile.equals(list[i].getName()))
                                    list[i].delete();
                            }

                        }
                    }
                else if ("twitter".equals(params[0])) {
                        try {
                            outputStream = new FileOutputStream(folder.getAbsolutePath() + "/" + nameFile);
                            success = mFTPClient.retrieveFile(nameFile, outputStream);
                        } catch (Exception e) {
                            return e.getMessage();
                        } finally {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }
                        if (success) {
                            resultFileName = nameFile;
                            mFTPClient.deleteFile(nameFile);
                        }
                    }
            }
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            if (mFTPClient != null) {
                try {
                    mFTPClient.logout();
                    mFTPClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!"".equals(resultFileName))
            return resultFileName;
        return null;
    }

    /**
     * Method auto invoked on post execute the task.
     */
    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        if (result != null && !result.toLowerCase().contains(".jpg"))
            Toast.makeText(context, "FTP error: " + result, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, "File downloaded from FTP: " + result, Toast.LENGTH_LONG).show();
    }
}
