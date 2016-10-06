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

package it.baywaylabs.jumpersumo.twitter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.parrot.arsdk.arcontroller.ARDeviceController;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import it.baywaylabs.jumpersumo.optimized.Command;
import it.baywaylabs.jumpersumo.optimized.PriorityQueue;
import it.baywaylabs.jumpersumo.robot.Intelligence;
import it.baywaylabs.jumpersumo.robot.Interpreter;
import it.baywaylabs.jumpersumo.utility.Constants;
import it.baywaylabs.jumpersumo.utility.FileFilter;
import it.baywaylabs.jumpersumo.utility.Finder;
import twitter4j.Paging;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;


/**
 * This AsyncTask (Async Thread) is called by the main ({@link it.baywaylabs.jumpersumo.PilotingActivity}) every one minute.<br />
 * This thread find the last twitter mention (@User ... ) after <b>"idLastTwit"</b>, extract commands list,
 * send to Robot and after finish reply to mention with a Tweet.<br />
 * Created on 21/11/15.
 *
 * @author Massimiliano Fiori [massimiliano.fiori@aol.it]
 * @see <a href="http://developer.android.com/reference/android/os/AsyncTask.html" target="_blank">AsyncTask</a>
 * @see <a href="http://developer.android.com/reference/android/content/SharedPreferences.html" target="_blank">Shared Preferences</a>
 * @see <a href="http://twitter4j.org/en/code-examples.html target="_blank">Twitter4j Library</a>
 * @see it.baywaylabs.jumpersumo.PilotingActivity
 */
public class TwitterListener extends AsyncTask<String, Boolean, List<String>> {

    private static final String TAG = TwitterListener.class.getSimpleName();

    private List<String> execute = new ArrayList<String>();
    private Long idLastTwit;
    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private List<String> licenses = new ArrayList<String>();
    private List<twitter4j.Status> statuses;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private ARDeviceController deviceController;

    private Finder f = new Finder();
    private Twitter twitter;
    private String ip_host;

    /**
     * @param c       Context.
     * @param device  ARDeviceController.
     * @param twitter Twitter4j instance.
     */
    public TwitterListener(Context c, ARDeviceController device, Twitter twitter) {
        this.context = c;
        this.deviceController = device;
        this.twitter = twitter;
    }

    /**
     * @param c       Context.
     * @param device  ARDeviceController.
     * @param twitter Twitter4j instance.
     * @param ip      Robot ip address.
     */
    public TwitterListener(Context c, ARDeviceController device, Twitter twitter, String ip) {
        this.context = c;
        this.deviceController = device;
        this.twitter = twitter;
        this.ip_host = ip;
    }

    /**
     * @param c        Context.
     * @param device   ARDeviceController.
     * @param licenses Twitter users license.
     * @param twitter  Twitter4j instance.
     */
    public TwitterListener(Context c, ARDeviceController device, List<String> licenses, Twitter twitter) {
        this.context = c;
        this.deviceController = device;
        this.licenses = licenses;
        this.twitter = twitter;
    }

    /**
     * @param c        Context.
     * @param device   ARDeviceController.
     * @param licenses Twitter users license.
     * @param twitter  Twitter4j instance.
     * @param ip       Robot ip address.
     */
    public TwitterListener(Context c, ARDeviceController device, List<String> licenses, Twitter twitter, String ip) {
        this.context = c;
        this.deviceController = device;
        this.licenses = licenses;
        this.twitter = twitter;
        this.ip_host = ip;
    }

    /**
     * Method auto invoked pre execute the task.<br />
     * This method read the last mention id from Shared Preferences.
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

        sharedPref = context.getSharedPreferences(Constants.MY_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        idLastTwit = sharedPref.getLong(Constants.LAST_ID_MENTIONED, 0);

    }

    /**
     * This method is called when invoke <b>execute()</b>.<br />
     * Do not invoke manually. Use: <i>new TwitterListener().execute("");</i>
     *
     * @param params
     */
    @Override
    protected List<String> doInBackground(String... params) {

        Intelligence ai = new Intelligence();

        try {

            User user = twitter.verifyCredentials();
            Paging paging;
            if (idLastTwit != 0)
                paging = new Paging(idLastTwit + 1);
            else
                paging = new Paging();
            paging.count(100);
            statuses = twitter.getMentionsTimeline(paging);

            if (statuses.size() != 0 && statuses != null) {

                Log.d(TAG, "Showing @" + user.getScreenName() + "'s mentions:");
                Log.d(TAG, statuses.get(0).getText() + "-- ID: " + statuses.get(0).getId());

                if (!"".equals(statuses.get(0).getText()) && licenses.size() == 0) {

                    extractCommand(ai, statuses);
                    Log.d(TAG, "Non ho licenze attive!");

                } else if (!"".equals(statuses.get(0).getText()) && licenses.size() > 0 && f.boolContainsIgnoreCase(licenses, String.valueOf(statuses.get(0).getUser().getId()))) {

                    extractCommand(ai, statuses);
                    Log.i(TAG, "Seguo la lista delle licenze: " + licenses.toString());

                } else {
                    publishProgress(false);
                }

                if (deviceController != null) {

                    Random r = new Random();
                    Interpreter interp = new Interpreter(deviceController);

                    for (int i = 0; i < execute.size(); i++) {
                        Log.d(TAG, "Seguenza istruzioni rilevate: " + execute.toString());

                        if (execute.size() >= 1 && "EXECUTE".equals(execute.get(i)) && f.getUrls(statuses.get(0).getText()).size() != 0) {

                            String url = statuses.get(0).getURLEntities()[0].getExpandedURL();
                            // String url = f.getUrls(statuses.get(0).getText()).get(0);
                            File folder = new File(Constants.DIR_ROBOT);
                            Log.d(TAG, "URL Expanded: " + url);

                            if (downloadFileUrl(url, folder)) {
                                FileFilter ff = new FileFilter();
                                File[] list = folder.listFiles(ff);
                                Log.e(TAG, "Lista file: " + list.length);
                                if (list != null && list.length >= 1) {


                                    if (list[0].getName().endsWith(".csv") || list[0].getName().endsWith(".txt")) {
                                        String commandsList = "";
                                        try {
                                            commandsList = f.getStringFromFile(list[0].getPath());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        Log.d(TAG, "Lista comandi: " + commandsList);
                                        interp.doListCommands(commandsList);
                                        list[0].delete();

                                        if (i == execute.size() - 1) {
                                            try {
                                                StatusUpdate reply = new StatusUpdate(ai.actionAnswer(execute.get(i)).get(r.nextInt(ai.actionAnswer(execute.get(i)).size())) + "@" + statuses.get(0).getUser().getScreenName());
                                                reply.setInReplyToStatusId(statuses.get(0).getId());
                                                twitter.updateStatus(reply);
                                            } catch (TwitterException te) {
                                                Log.e(TAG, "Twitter Post Error: " + te.getMessage());
                                            }
                                        }
                                    }

                                }


                            }

                        } else if (execute.size() >= 1 && "PHOTO".equals(execute.get(i))) {
                            // Download immagine e post nel tweet
                            if (interp.doCommand(execute.get(i)) == 0) {

                                String result = FTPDownloadFile(ip_host, 21, "anonymous", "", context);


                                if (i == execute.size() - 1 && !"".equals(result)) {
                                    try {
                                        StatusUpdate reply = new StatusUpdate(ai.actionAnswer(execute.get(i)).get(r.nextInt(ai.actionAnswer(execute.get(i)).size())) + "@" + statuses.get(0).getUser().getScreenName());
                                        reply.setInReplyToStatusId(statuses.get(0).getId());
                                        File image = new File(Constants.DIR_ROBOT_IMG + "/" + result);
                                        Log.d(TAG, "Nome File immagine: " + image.getPath());
                                        reply.media(image);
                                        twitter.updateStatus(reply);
                                        image.delete();
                                    } catch (TwitterException te) {
                                        Log.e(TAG, "Twitter Post Error: " + te.getMessage());
                                    }
                                }
                            }
                        } else if (execute.size() >= 1 && !"PHOTO".equals(execute.get(i)) && !"EXECUTE".equals(execute.get(i))) {

                            if (interp.doCommand(execute.get(i)) == 0) {

                                if (i == execute.size() - 1) {
                                    try {
                                        StatusUpdate reply = new StatusUpdate(ai.actionAnswer(execute.get(i)).get(r.nextInt(ai.actionAnswer(execute.get(i)).size())) + "@" + statuses.get(0).getUser().getScreenName());
                                        reply.setInReplyToStatusId(statuses.get(0).getId());
                                        twitter.updateStatus(reply);
                                    } catch (TwitterException te) {
                                        Log.e(TAG, "Twitter Post Error: " + te.getMessage());
                                    }
                                }
                            }
                        }
                    }

                }
            }

            Log.d(TAG, "ready exit");

        } catch (TwitterException te) {
            te.printStackTrace();
            Log.e(TAG, "Failed to get timeline: " + te.getMessage());
        }

        return execute;
    }

    /**
     * This method extract ordered commands list.
     *
     * @param ai
     * @param statuses
     * @deprecated use {@link #extractCommandV2(Intelligence, List)} instead.
     */
    private void extractCommand(Intelligence ai, List<twitter4j.Status> statuses) {
        publishProgress(false);

        HashMap<Integer, String> map = new HashMap<Integer, String>();
        String twText = statuses.get(0).getText().toLowerCase();

        for (String command : ai.getMoveOn()) {

            int lastIndex = 0;
            int count = 0;

            while (lastIndex != -1) {

                lastIndex = twText.indexOf(command, lastIndex);

                if (lastIndex != -1) {
                    count++;
                    map.put(lastIndex, "FORWARD");
                    lastIndex += command.length();
                }
            }
        }
        for (String command : ai.getMoveBack()) {
            int lastIndex = 0;
            int count = 0;

            while (lastIndex != -1) {

                lastIndex = twText.indexOf(command, lastIndex);

                if (lastIndex != -1) {
                    count++;
                    map.put(lastIndex, "BACK");
                    lastIndex += command.length();
                }
            }
        }
        for (String command : ai.getTurnLeft()) {

            int lastIndex = 0;
            int count = 0;

            while (lastIndex != -1) {

                lastIndex = twText.indexOf(command, lastIndex);

                if (lastIndex != -1) {
                    count++;
                    map.put(lastIndex, "LEFT");
                    lastIndex += command.length();
                }
            }
        }
        for (String command : ai.getTurnRight()) {

            int lastIndex = 0;
            int count = 0;

            while (lastIndex != -1) {

                lastIndex = twText.indexOf(command, lastIndex);

                if (lastIndex != -1) {
                    count++;
                    map.put(lastIndex, "RIGHT");
                    lastIndex += command.length();
                }
            }
        }
        for (String command : ai.getTakePhoto()) {

            int lastIndex = 0;
            int count = 0;

            while (lastIndex != -1) {

                lastIndex = twText.indexOf(command, lastIndex);

                if (lastIndex != -1) {
                    count++;
                    map.put(lastIndex, "PHOTO");
                    lastIndex += command.length();
                }
            }
        }
        for (String command : ai.getExecuteCsv()) {

            int lastIndex = 0;
            int count = 0;

            while (lastIndex != -1) {

                lastIndex = twText.indexOf(command, lastIndex);

                if (lastIndex != -1) {
                    count++;
                    map.put(lastIndex, "EXECUTE");
                    lastIndex += command.length();
                }
            }
        }
        Log.d(TAG, "MAP: " + map.toString());
        List<String> ordered = f.getOrderedExtractedCommands(map);
        Log.d(TAG, "Lista Ordinata: " + ordered.toString());

        execute = f.joinListCommands(ordered);
        Log.d(TAG, "LISTA FINALE: " + execute.toString());

        editor.putLong(Constants.LAST_ID_MENTIONED, statuses.get(0).getId());
        editor.apply();
    }

    // TODO: create a new optimized extractCommand() method to use Priority Queue.
    public void extractCommandV2(Intelligence ai, List<twitter4j.Status> statuses) {
        // extractCommand(ai, statuses);

        PriorityQueue pq = f.processingMessage(statuses.get(0).getText());
        // TODO: popolare l'oggetto execute.
        execute = f.getStringQueue(pq);

        /*
        Random r = new Random();

        Command c = (Command) pq.lastPeek();
        String lastString = c.getCmd();

        Log.d(TAG, "Elemento più grande nella coda, quindi anche l'ultimo ad essere eseguito: " + lastString);
        int result = f.executePQ(pq, deviceController, statuses, twitter);
        if (result == 1) {
            Log.e(TAG, "Errore nell'esecuzione della coda di priorità");
        } else {
            Log.d(TAG, "Coda di priorità eseguita con successo.");

            try {
                StatusUpdate reply = new StatusUpdate(ai.actionAnswer(lastString).get(r.nextInt(ai.actionAnswer(lastString).size())) + "@" + statuses.get(0).getUser().getScreenName());
                reply.setInReplyToStatusId(statuses.get(0).getId());
                twitter.updateStatus(reply);
            } catch (TwitterException te) {
                Log.e(TAG, "Twitter Post Error: " + te.getMessage());
            }


            editor.putLong(Constants.LAST_ID_MENTIONED, statuses.get(0).getId());
            editor.apply();
        }
        **/
        editor.putLong(Constants.LAST_ID_MENTIONED, statuses.get(0).getId());
        editor.apply();
    }

    /**
     * Method auto invoked during execute the task.
     */
    protected void onProgressUpdate(Boolean progress) {
        if (progress) {
            Log.w(TAG, "Find command!");
        } else {
            Log.w(TAG, "Waiting for...");
        }
    }

    /**
     * Method auto invoked on post execute the task.
     */
    protected void onPostExecute(List<String> result) {
        mWakeLock.release();
        if (result.size() >= 1) {
            Log.d(TAG, "Ultimo comando da eseguire: " + result.get(result.size() - 1));
        }
    }

    // FIXME: find another efficiently way to do this step. this method do the same thing that ServerPolling task does, but i can't call another AsyncTask from class that is not main context.

    /**
     * Download File from Url in Folder; this method do the same thing that ServerPolling task does,
     * but i can't call another AsyncTask from class that is not main context.
     *
     * @param url
     * @param folder
     */
    private Boolean downloadFileUrl(String url, File folder) {
        InputStream input = null;
        OutputStream output = null;
        String baseName = FilenameUtils.getBaseName(url);
        String extension = FilenameUtils.getExtension(url);
        Log.d(TAG, "FileName: " + baseName + " - FileExt: " + extension);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        HttpURLConnection connection = null;
        if (!f.isUrl(url)) return false;

        Boolean downloadSuccess = false;
        try {
            URL Url = new URL(url);
            connection = (HttpURLConnection) Url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if ((!url.endsWith(".csv") || !url.endsWith(".txt")) && connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
                return false;
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(folder.getAbsolutePath() + "/" + baseName + "." + extension);


            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return false;
                }
                total += count;
                output.write(data, 0, count);

                downloadSuccess = true;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
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

        return true;
    }


    // FIXME: find another efficiently way to do this step. this method do the same thing that FTPDownloadImage task does, but i can't call another AsyncTask from class that is not main context.

    /**
     * @param host FTP Host name.
     * @param port FTP port.
     * @param user FTP User.
     * @param pswd FTP Password.
     * @param c    Context
     * @return Downloaded name file or blank list if something was going wrong.
     */
    private String FTPDownloadFile(String host, Integer port, String user, String pswd, Context c) {
        String result = "";
        FTPClient mFTPClient = null;

        try {
            mFTPClient = new FTPClient();
            // connecting to the host
            mFTPClient.connect(host, port);

            // Now check the reply code, if positive mean connection success
            if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {

                // Login using username & password
                boolean status = mFTPClient.login(user, pswd);
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
                    result = nameFile;
                    mFTPClient.deleteFile(nameFile);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (mFTPClient != null) {
                try {
                    mFTPClient.logout();
                    mFTPClient.disconnect();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }

        return result;
    }
}