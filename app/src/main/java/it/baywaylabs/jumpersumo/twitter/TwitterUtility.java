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

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Twitter Utility Class for general purpose.
 * Created on 27/11/15.
 *
 * @author Massimiliano Fiori [massimiliano.fiori@aol.it]
 */
public final class TwitterUtility {

    private static final String TAG = TwitterUtility.class.getSimpleName();

    /**
     * Twitter utility for extract id from twitter username.
     *
     * @param name Twitter User Name.
     * @param twitter Twitter4j instance.
     * @return Twitter Id user, or 0 if is not find.
     */
    public Long getIdUser (String name, Twitter twitter) {
        if (twitter != null ){
            User user = null;
            try {
                user = twitter.showUser(name);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return user.getId();
        }
        return 0l;
    }

    /**
     *
     * Twitter utility for extract twitter username from id.
     *
     * @param id Twitter User id.
     * @param twitter Twitter4j instance.
     * @return Twitter User Name or blank string if is not find.
     */
    public String getNameUser (Long id, Twitter twitter) {
        if (twitter != null ){
            User user = null;
            try {
                user = twitter.showUser(id);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return user.getName();
        }
        return "";
    }
}