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

import android.os.Environment;

/**
 * This Class contains all configurations needed. <br />
 * Created on 23/11/15.
 *
 * @author Massimiliano Fiori [massimiliano.fiori@aol.it]
 */
// FIXME: This class can be replace with properties file
public class Constants {

    /**
     * Server URL.
     */
    public static final String WEBSERVICE_URL = "http://baywaylabs.it/";
    /**
     * PHP get_msg PATH
     */
    public static final String WEBSERVICE_GET_MSG_URL = WEBSERVICE_URL + "tesi/get_msg.php";
    /**
     * PHP executed PATH
     */
	public static final String WEBSERVICE_STATE_UPDATE_URL = WEBSERVICE_URL + "tesi/executed.php";
	
	/**
     * Consumer Key of Twitter Application.
     *
     * @see <a href="https://twittercommunity.com/t/how-do-i-find-my-consumer-key-and-secret/646/4" target="_blank">Twitter Application</a>
     */
    public static final String CONSUMER_KEY = "";
    /**
     * Consumer Key Secret of Twitter Application.
     *
     * @see <a href="https://twittercommunity.com/t/how-do-i-find-my-consumer-key-and-secret/646/4" target="_blank">Twitter Application</a>
     */
    public static final String CONSUMER_SECRET= "";

    /**
     * Access Token of Twitter Application
     *
     * @see <a href="https://dev.twitter.com/oauth/overview/application-owner-access-tokens" target="_blank">Twitter Application</a>
     */
    public static final String ACCESS_TOKEN = "";
    /**
     * Access Token Secret of Twitter Application
     *
     * @see <a href="https://dev.twitter.com/oauth/overview/application-owner-access-tokens" target="_blank">Twitter Application</a>
     */
    public static final String ACCESS_TOKEN_SECRET = "";

    /**
     * Name of used Shared Preferences.
     *
     * @see <a href="http://developer.android.com/reference/android/content/SharedPreferences.html" target="_blank">Shared Preferences</a>
     */
    public static final String MY_PREFERENCES = "MyPref";
    /**
     * Id of last twitter mention stored on shared preferences.
     *
     * @see <a href="http://developer.android.com/reference/android/content/SharedPreferences.html" target="_blank">Shared Preferences</a>
     */
    public static final String LAST_ID_MENTIONED = "last_id_mentioned";
	/**
     * Id of last command from webservice call stored on shared preferences.
     *
     * @see <a href="http://developer.android.com/reference/android/content/SharedPreferences.html" target="_blank">Shared Preferences</a>
     */
	public static final String LAST_ID_WEBSERVICE = "last_id_webservice";

    /**
     * App Root Path on device.
     */
    public static final String DIR_ROBOT = Environment.getExternalStorageDirectory().getPath() + "/JumperSumo";
    /**
     * App Images Path on device.
     */
    public static final String DIR_ROBOT_IMG = DIR_ROBOT + "/images";
    /**
     * App Daemon Path on device.
     */
    public static final String DIR_ROBOT_DAEMON = DIR_ROBOT + "/daemon";

    /**
     * Dir Path of images stored on robot.
     */
    public static final String DIR_ROBOT_MEDIA = "/internal_000/DCIM/100DRONE";

}