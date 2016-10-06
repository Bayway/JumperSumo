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

import android.util.Log;

import com.parrot.arsdk.arcontroller.ARDeviceController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

import it.baywaylabs.jumpersumo.optimized.Command;
import it.baywaylabs.jumpersumo.optimized.MySuffixTree;
import it.baywaylabs.jumpersumo.optimized.PriorityQueue;
import it.baywaylabs.jumpersumo.optimized.SuffixTree;
import it.baywaylabs.jumpersumo.robot.Intelligence;
import it.baywaylabs.jumpersumo.robot.Interpreter;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.URLEntity;

/**
 * Utility class to manipulate generic String and Object.
 *
 * @author Massimiliano Fiori [massimiliano.fiori@aol.it]
 */
public class Finder {

    private static final String TAG = Finder.class.getSimpleName();


    public boolean boolContainsIgnoreCase(List<String> whats, String shouldBeContainedIn) {
        boolean ohyeah = false;
        for (String what : whats) {
            if (what.toUpperCase().contains(shouldBeContainedIn.toUpperCase())) ohyeah = true;
        }
        return ohyeah;
    }


    /**
     * This method extract Urls from text.
     *
     * @param text
     * @return Urls List.
     */
    public List<String> getUrls(String text) {

        List<String> result = new ArrayList<String>();

        String regex = "\\(?\\b(https://|http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        while (m.find()) {
            String urlStr = m.group();
            if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                urlStr = urlStr.substring(1, urlStr.length() - 1);
            }

            result.add(urlStr);
        }

        return result;

    }

    /**
     * This method control if Url is valid.
     *
     * @param text Url
     */
    public Boolean isUrl(String text) {

        List<String> result = null;

        String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        while (m.find()) {
            String urlStr = m.group();
            if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                urlStr = urlStr.substring(1, urlStr.length() - 1);
            }

            return true;
        }

        return false;

    }

    /**
     * This method convert file in String.
     *
     * @param filePath
     * @throws Exception
     */
    public String getStringFromFile(String filePath) throws Exception {

        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    /**
     * This method returns the stream file value.
     *
     * @param is
     * @throws Exception
     */
    public static String convertStreamToString(InputStream is) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    /**
     * This method joins commands list in one List.
     *
     * @param params List to join.
     * @return List of all commands.
     */
    public List<String> joinListCommands(List<String>... params) {

        List<String> result = new ArrayList<String>();
        if (params.length != 0) {
            for (int i = 0; i < params.length; i++)
                result = ListUtils.union(result, params[i]);
        }

        return result;
    }

    /**
     * This method returns ordered commands list from HashMap.
     *
     * @param map
     * @return Ordered List of commands to execute.
     */
    public List<String> getOrderedExtractedCommands(HashMap<Integer, String> map) {

        List<String> result = new ArrayList<String>();
        if (map.size() != 0) {
            Map<Integer, String> mapSorted = new TreeMap<Integer, String>(map);
            System.out.println("After Sorting:");
            Set set = mapSorted.entrySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry me = (Map.Entry) iterator.next();
                Log.e(TAG, me.getKey() + ": " + me.getValue());
                result.add(me.getValue().toString());
            }
        }
        return result;
    }

    /**
     * Extract a priority queue with all commands found on message string.
     *
     * @param textMsg String of message.
     * @return Priority Queue with all commands found.
     */
    public PriorityQueue processingMessage(String textMsg) {

        String msg = textMsg.toLowerCase();
        Log.d(TAG, "Stringa da processare: " + msg);
        msg = msg.replaceAll("\\s+", "\\$");
        Log.d(TAG, "Stringa da processare: " + msg);
        msg = msg.replaceAll("\\p{Punct}", "");
        if (!"$".equals(msg.substring(msg.length())))
            msg += "$";

        // Make Suffix Tree from 'msg' string
        SuffixTree tree;
        tree = new MySuffixTree(msg);

        List<Command> commandsList = new ArrayList<Command>();
        Intelligence intelligence = new Intelligence();
        int[] position = null;
        Set<Integer> check = new HashSet<Integer>();
        List<Integer> a = new ArrayList<Integer>();
        List<Integer> b = new ArrayList<Integer>();

        Log.d(TAG, "Stringa da processare: " + msg);

        // FORWARD case:
        for (String com : intelligence.getMoveOn()) {

            // Controllo i comandi formati da più parole
            if (com.contains(" "))
            {
                HashMap<String, int[]> map = new HashMap<String, int[]>();
                String[] singleCommand  = com.split(" ");
                for (int i=0; i<singleCommand.length; i++)
                {
                    int[] trovato = tree.findAll(singleCommand[i]);
                    if (trovato.length > 0)
                    {
                        map.put(singleCommand[i], trovato);

                        String arrayTrovat = "";
                        for (int k=0; k<trovato.length; k++)
                            arrayTrovat += trovato[k] + ", ";
                        Log.d(TAG, singleCommand[i] + " -- " + arrayTrovat);

                    }
                }
                Integer[] consec = checkConsecutive(singleCommand, map);
                if (consec != null && consec.length > 0)
                {
                    for (Integer i : consec) {
                        check.add(i);
                        a.add(i);
                        b.add(com.length());
                    }

                }
            } else
            {
                int[] temp = tree.findAll(com);
                boolean daAggiungere = true;
                for (Integer i : tree.findAll(com))
                {
                    for (int j = 0; j<a.size(); j++)
                    {
                        Log.e(TAG, "Controllo i: " + i + ", con a: " + a.get(j) + " e b: " + b.get(j) );
                        if ( i >= a.get(j) && i + com.length() <= a.get(j) + b.get(j))
                        {
                            daAggiungere = false;
                            break;
                        }
                    }
                    if ( daAggiungere )
                    {
                        check.add(i);
                        a.add(i);
                        b.add(com.length());
                    }
                }
            }

            for (int p=0; p<a.size(); p++)
            {
                Log.e(TAG, "A: " + a.get(p) + ", B: " + b.get(p));
            }
        }
        position = toPrimitive(check);
        check.clear();
        a.clear();
        b.clear();
        if (position.length > 0) {
            for (int i = 0; i < position.length; i++) {
                Command c = new Command(position[i], "FORWARD");
                commandsList.add(c);
            }
        }

        // BACK case:
        for (String com : intelligence.getMoveBack()) {

            // Controllo i comandi formati da più parole
            if (com.contains(" "))
            {
                HashMap<String, int[]> map = new HashMap<String, int[]>();
                String[] singleCommand  = com.split(" ");
                for (int i=0; i<singleCommand.length; i++)
                {
                    int[] trovato = tree.findAll(singleCommand[i]);
                    if (trovato.length > 0)
                    {
                        map.put(singleCommand[i], trovato);

                        String arrayTrovat = "";
                        for (int k=0; k<trovato.length; k++)
                            arrayTrovat += trovato[k] + ", ";
                        Log.d(TAG, singleCommand[i] + " -- " + arrayTrovat);

                    }
                }
                Integer[] consec = checkConsecutive(singleCommand, map);
                if (consec != null && consec.length > 0)
                {
                    for (Integer i : consec) {
                        check.add(i);
                        a.add(i);
                        b.add(com.length());
                    }

                }
            } else
            {
                int[] temp = tree.findAll(com);
                boolean daAggiungere = true;
                for (Integer i : tree.findAll(com))
                {
                    for (int j = 0; j<a.size(); j++)
                    {
                        Log.e(TAG, "Controllo i: " + i + ", con a: " + a.get(j) + " e b: " + b.get(j) );
                        if ( i >= a.get(j) && i + com.length() <= a.get(j) + b.get(j))
                        {
                            daAggiungere = false;
                            break;
                        }
                    }
                    if ( daAggiungere )
                    {
                        check.add(i);
                        a.add(i);
                        b.add(com.length());
                    }
                }
            }

            for (int p=0; p<a.size(); p++)
            {
                Log.e(TAG, "A: " + a.get(p) + ", B: " + b.get(p));
            }
        }
        position = toPrimitive(check);
        check.clear();
        a.clear();
        b.clear();
        if (position.length > 0) {
            for (int i = 0; i < position.length; i++) {
                Command c = new Command(position[i], "BACK");
                commandsList.add(c);
            }
        }

        // LEFT case:
        for (String com : intelligence.getTurnLeft()) {

            // Controllo i comandi formati da più parole
            if (com.contains(" "))
            {
                HashMap<String, int[]> map = new HashMap<String, int[]>();
                String[] singleCommand  = com.split(" ");
                for (int i=0; i<singleCommand.length; i++)
                {
                    int[] trovato = tree.findAll(singleCommand[i]);
                    if (trovato.length > 0)
                    {
                        map.put(singleCommand[i], trovato);

                        String arrayTrovat = "";
                        for (int k=0; k<trovato.length; k++)
                            arrayTrovat += trovato[k] + ", ";
                        Log.d(TAG, singleCommand[i] + " -- " + arrayTrovat);

                    }
                }
                Integer[] consec = checkConsecutive(singleCommand, map);
                if (consec != null && consec.length > 0)
                {
                    for (Integer i : consec) {
                        check.add(i);
                        a.add(i);
                        b.add(com.length());
                    }

                }
            } else
            {
                int[] temp = tree.findAll(com);
                boolean daAggiungere = true;
                for (Integer i : tree.findAll(com))
                {
                    for (int j = 0; j<a.size(); j++)
                    {
                        Log.e(TAG, "Controllo i: " + i + ", con a: " + a.get(j) + " e b: " + b.get(j) );
                        if ( i >= a.get(j) && i + com.length() <= a.get(j) + b.get(j))
                        {
                            daAggiungere = false;
                            break;
                        }
                    }
                    if ( daAggiungere )
                    {
                        check.add(i);
                        a.add(i);
                        b.add(com.length());
                    }
                }
            }

            for (int p=0; p<a.size(); p++)
            {
                Log.e(TAG, "A: " + a.get(p) + ", B: " + b.get(p));
            }
        }
        position = toPrimitive(check);
        check.clear();
        a.clear();
        b.clear();
        if (position.length > 0) {
            for (int i = 0; i < position.length; i++) {
                Command c = new Command(position[i], "LEFT");
                commandsList.add(c);
            }
        }

        // RIGHT case:
        for (String com : intelligence.getTurnRight()) {

            // Controllo i comandi formati da più parole
            if (com.contains(" "))
            {
                HashMap<String, int[]> map = new HashMap<String, int[]>();
                String[] singleCommand  = com.split(" ");
                for (int i=0; i<singleCommand.length; i++)
                {
                    int[] trovato = tree.findAll(singleCommand[i]);
                    if (trovato.length > 0)
                    {
                        map.put(singleCommand[i], trovato);

                        String arrayTrovat = "";
                        for (int k=0; k<trovato.length; k++)
                            arrayTrovat += trovato[k] + ", ";
                        Log.d(TAG, singleCommand[i] + " -- " + arrayTrovat);

                    }
                }
                Integer[] consec = checkConsecutive(singleCommand, map);
                if (consec != null && consec.length > 0)
                {
                    for (Integer i : consec) {
                        check.add(i);
                        a.add(i);
                        b.add(com.length());
                    }

                }
            } else
            {
                int[] temp = tree.findAll(com);
                boolean daAggiungere = true;
                for (Integer i : tree.findAll(com))
                {
                    for (int j = 0; j<a.size(); j++)
                    {
                        Log.e(TAG, "Controllo i: " + i + ", con a: " + a.get(j) + " e b: " + b.get(j) );
                        if ( i >= a.get(j) && i + com.length() <= a.get(j) + b.get(j))
                        {
                            daAggiungere = false;
                            break;
                        }
                    }
                    if ( daAggiungere )
                    {
                        check.add(i);
                        a.add(i);
                        b.add(com.length());
                    }
                }
            }

            for (int p=0; p<a.size(); p++)
            {
                Log.e(TAG, "A: " + a.get(p) + ", B: " + b.get(p));
            }
        }
        position = toPrimitive(check);
        check.clear();
        a.clear();
        b.clear();
        if (position.length > 0) {
            for (int i = 0; i < position.length; i++) {
                Command c = new Command(position[i], "RIGHT");
                commandsList.add(c);
            }
        }

        // PHOTO case:
        for (String com : intelligence.getTakePhoto()) {

            // Controllo i comandi formati da più parole
            if (com.contains(" "))
            {
                HashMap<String, int[]> map = new HashMap<String, int[]>();
                String[] singleCommand  = com.split(" ");
                for (int i=0; i<singleCommand.length; i++)
                {
                    int[] trovato = tree.findAll(singleCommand[i]);
                    if (trovato.length > 0)
                    {
                        map.put(singleCommand[i], trovato);

                        String arrayTrovat = "";
                        for (int k=0; k<trovato.length; k++)
                            arrayTrovat += trovato[k] + ", ";
                        Log.d(TAG, singleCommand[i] + " -- " + arrayTrovat);

                    }
                }
                Integer[] consec = checkConsecutive(singleCommand, map);
                if (consec != null && consec.length > 0)
                {
                    for (Integer i : consec) {
                        check.add(i);
                        a.add(i);
                        b.add(com.length());
                    }

                }
            } else
            {
                int[] temp = tree.findAll(com);
                boolean daAggiungere = true;
                for (Integer i : tree.findAll(com))
                {
                    for (int j = 0; j<a.size(); j++)
                    {
                        Log.e(TAG, "Controllo i: " + i + ", con a: " + a.get(j) + " e b: " + b.get(j) );
                        if ( i >= a.get(j) && i + com.length() <= a.get(j) + b.get(j))
                        {
                            daAggiungere = false;
                            break;
                        }
                    }
                    if ( daAggiungere )
                    {
                        check.add(i);
                        a.add(i);
                        b.add(com.length());
                    }
                }
            }

            for (int p=0; p<a.size(); p++)
            {
                Log.e(TAG, "A: " + a.get(p) + ", B: " + b.get(p));
            }
        }
        position = toPrimitive(check);
        check.clear();
        a.clear();
        b.clear();
        if (position.length > 0) {
            for (int i = 0; i < position.length; i++) {
                Command c = new Command(position[i], "PHOTO");
                commandsList.add(c);
            }
        }

        // EXECUTE case:
        for (String com : intelligence.getExecuteCsv()) {

            // Controllo i comandi formati da più parole
            if (com.contains(" "))
            {
                HashMap<String, int[]> map = new HashMap<String, int[]>();
                String[] singleCommand  = com.split(" ");
                for (int i=0; i<singleCommand.length; i++)
                {
                    int[] trovato = tree.findAll(singleCommand[i]);
                    if (trovato.length > 0)
                    {
                        map.put(singleCommand[i], trovato);

                        String arrayTrovat = "";
                        for (int k=0; k<trovato.length; k++)
                            arrayTrovat += trovato[k] + ", ";
                        Log.d(TAG, singleCommand[i] + " -- " + arrayTrovat);

                    }
                }
                Integer[] consec = checkConsecutive(singleCommand, map);
                if (consec != null && consec.length > 0)
                {
                    for (Integer i : consec) {
                        check.add(i);
                        a.add(i);
                        b.add(com.length());
                    }

                }
            } else
            {
                int[] temp = tree.findAll(com);
                boolean daAggiungere = true;
                for (Integer i : tree.findAll(com))
                {
                    for (int j = 0; j<a.size(); j++)
                    {
                        Log.e(TAG, "Controllo i: " + i + ", con a: " + a.get(j) + " e b: " + b.get(j) );
                        if ( i >= a.get(j) && i + com.length() <= a.get(j) + b.get(j))
                        {
                            daAggiungere = false;
                            break;
                        }
                    }
                    if ( daAggiungere )
                    {
                        check.add(i);
                        a.add(i);
                        b.add(com.length());
                    }
                }
            }

            for (int p=0; p<a.size(); p++)
            {
                Log.e(TAG, "A: " + a.get(p) + ", B: " + b.get(p));
            }
        }
        position = toPrimitive(check);
        check.clear();
        a.clear();
        b.clear();
        if (position.length > 0) {
            for (int i = 0; i < position.length; i++) {
                Command c = new Command(position[i], "EXECUTE");
                commandsList.add(c);
            }
        }

        PriorityQueue pq = new PriorityQueue(commandsList);

        Log.d(TAG, "ListaCommands: " + commandsList.size());
        Log.d(TAG, String.valueOf(pq.size()));
        Log.d(TAG, "Coda Vuota: " + pq.isEmpty());
        return pq;
    }

    public Integer[] checkConsecutive(String[] key, HashMap<String, int[]> map) {
        Set<Integer> check = new HashSet<Integer>();

        boolean consecutivi = false;

        if (key.length > 0 && map.size() > 0)
        {
            /*
            for (int i=0; i<key.length; i++)
            {
                for (int j=0; j<map.get(key[i]).length; j++ )
                {
                    if (i+1 < key.length)
                    {
                        if ( map.get(key[i])[j] + key[i].length() +1 == map.get(key[i+1])[j] )
                        {
                            // check.add(map.get(key[0])[map.get(key[0]).length-1]);
                            check.add(map.get(key[0])[j]);
                        }
                    }
                }
            }
            */
            for (int i=0; i<key.length; i++)
            {
                int index = -1;
                if (i + 1 < key.length && map.get(key[i+1]) != null)
                {
                    index = map.get(key[i+1]).length-1;
                }
                for (int j=map.get(key[i]).length-1; j>=0; j-- )
                {
                    if (i+1 < key.length && index >= 0)
                    {
                        if ( map.get(key[i])[j] + key[i].length() +1 == map.get(key[i+1])[index] )
                        {
                            // check.add(map.get(key[0])[map.get(key[0]).length-1]);
                            check.add(map.get(key[0])[j]);
                            index--;
                        }
                    }
                }
            }
        }

        if(!check.isEmpty())
        {
            return check.toArray(new Integer[check.size()]);
            // return ArrayUtils.toPrimitive((Integer[]) check.toArray());
        }

        return null;
    }

    public Integer[] checkConsecutive(String[] key, HashMap<String, int[]> map, List<Integer> a, List<Integer> b) {
        Set<Integer> check = new HashSet<Integer>();

        if (key.length > 0 && map.size() > 0)
        {
            for (int i=0; i<key.length; i++)
            {
                int index = -1;
                if (i + 1 < key.length && map.get(key[i+1]) != null)
                {
                    index = map.get(key[i+1]).length-1;
                }
                for (int j=map.get(key[i]).length-1; j>=0; j-- )
                {
                    if (i+1 < key.length && index >= 0)
                    {
                        if ( map.get(key[i])[j] + key[i].length() +1 == map.get(key[i+1])[index] )
                        {
                            // check.add(map.get(key[0])[map.get(key[0]).length-1]);
                            check.add(map.get(key[0])[j]);

                            // aggiorno i due array di appoggio a e b
                            Log.e(TAG, "Dovrei aggiungere ad A: " + map.get(key[i])[j] + " ed a B: " +  (( map.get(key[i+1])[index] + key[i+1].length() ) - map.get(key[i])[j]) );
                            a.add(map.get(key[i])[j]);
                            b.add( ( map.get(key[i+1])[index] + key[i+1].length() ) - map.get(key[i])[j]);

                            index--;
                        }
                    }
                }
            }
        }

        if(!check.isEmpty())
        {
            return check.toArray(new Integer[check.size()]);
            // return ArrayUtils.toPrimitive((Integer[]) check.toArray());
        }

        return null;
    }

    public int executePQ(PriorityQueue pq, ARDeviceController deviceController, List<twitter4j.Status> statuses, Twitter twitter) {

        if (pq != null && !pq.isEmpty() && deviceController != null) {

            Interpreter interpreter = new Interpreter(deviceController);
            Intelligence ai = new Intelligence();
            Random r = new Random();
            int indexExecutedFile = 0;

            while (pq.isEmpty()) {
                Command c = (Command) pq.peek();
                Log.d(TAG, "Command Object: " + c.getPriority() + " -- " + c.getCmd());

                if ("EXECUTE".equals(c.getCmd())) {
                    if (statuses != null && twitter != null && this.getUrls(statuses.get(0).getText()).size() != 0) {

                        String url = statuses.get(0).getURLEntities()[indexExecutedFile].getExpandedURL();
                        // String url = f.getUrls(statuses.get(0).getText()).get(0);
                        File folder = new File(Constants.DIR_ROBOT);
                        Log.d(TAG, "URL Expanded: " + url);

                        if (downloadFileUrl(url, folder)) {
                            FileFilter ff = new FileFilter();
                            File[] list = folder.listFiles(ff);
                            if (list != null && list.length >= 1) {


                                if (list[0].getName().endsWith(".csv") || list[0].getName().endsWith(".txt")) {
                                    String commandsList = "";
                                    try {
                                        commandsList = this.getStringFromFile(list[0].getPath());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    Log.d(TAG, "Lista comandi: " + commandsList);
                                    interpreter.doListCommands(commandsList);
                                    list[0].delete();

                                    if (pq.size() == 1) {
                                        try {
                                            StatusUpdate reply = new StatusUpdate(ai.actionAnswer(commandsList.split(";")[commandsList.length() - 1]).get(r.nextInt(ai.actionAnswer(commandsList.split(";")[commandsList.length() - 1]).size())) + "@" + statuses.get(0).getUser().getScreenName());
                                            reply.setInReplyToStatusId(statuses.get(0).getId());
                                            twitter.updateStatus(reply);
                                        } catch (TwitterException te) {
                                            Log.e(TAG, "Twitter Post Error: " + te.getMessage());
                                        }
                                    }
                                }

                            }


                        }

                        pq.remove();
                        indexExecutedFile++;
                    }
                } else {

                    interpreter.doCommand(c.getCmd());
                    pq.remove();
                }
            }
            return 0;
        } else if (pq.isEmpty()) {
            return 2;
        } else {
            return 1;
        }
    }

    public int executePQ(PriorityQueue pq, ARDeviceController deviceController, String msg) {


        if (pq != null && !pq.isEmpty() && deviceController != null) {

            Interpreter interpreter = new Interpreter(deviceController);
            Intelligence ai = new Intelligence();
            Random r = new Random();
            int indexExecutedFile = 0;

            Log.d(TAG, "Coda passata come parametro di dimensione " + pq.size());
            Log.d(TAG, "Primo Elemento: " + ((Command) pq.peek()).getCmd() );

            while (!pq.isEmpty()) {
                Command c = (Command) pq.peek();
                Log.d(TAG, "Command Object: " + c.getPriority() + " -- " + c.getCmd());

                if ("EXECUTE".equals(c.getCmd())) {
                    if (this.getUrls(msg).size() != 0 ){

                        String urlReal = this.getUrls(msg).get(indexExecutedFile);
                        /*
                        String urlReal = "";
                        URL url = null;
                        try {
                            url = new URL(urlCompress);
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setInstanceFollowRedirects(false);
                            urlReal = urlConnection.getHeaderField("location");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        */

                        // String url = f.getUrls(statuses.get(0).getText()).get(0);
                        File folder = new File(Constants.DIR_ROBOT);
                        Log.d(TAG, "URL Expanded: " + urlReal);

                        if (!"".equals(urlReal) && downloadFileUrl(urlReal, folder)) {
                            FileFilter ff = new FileFilter();
                            File[] list = folder.listFiles(ff);
                            if (list != null && list.length >= 1) {


                                if (list[0].getName().endsWith(".csv") || list[0].getName().endsWith(".txt")) {
                                    String commandsList = "";
                                    try {
                                        commandsList = this.getStringFromFile(list[0].getPath());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    Log.d(TAG, "Lista comandi: " + commandsList);
                                    interpreter.doListCommands(commandsList);
                                    list[0].delete();
                                }

                            }


                        }

                        pq.remove();
                        indexExecutedFile++;
                    }
                } else
                {
                    interpreter.doCommand(c.getCmd());
                    pq.remove();
                }
            }
            return 0;
        } else if (pq.isEmpty()) {
            return 2;
        } else {
            return 1;
        }
    }

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
        if (!this.isUrl(url)) return false;

        Boolean downloadSuccess = false;
        try {
            URL Url = new URL(url);
            connection = (HttpURLConnection) Url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (!url.endsWith(".csv") || !url.endsWith(".txt") && connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
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

    public static int[] toPrimitive(Integer[] IntegerArray) {

        int[] result = new int[IntegerArray.length];
        for (int i = 0; i < IntegerArray.length; i++) {
            result[i] = IntegerArray[i].intValue();
        }
        return result;
    }

    public static int[] toPrimitive(Set<Integer> integerSet) {

        int[] result = new int[integerSet.size()];
        int i=0;
        for (Iterator<Integer> it = integerSet.iterator(); it.hasNext(); ) {
            Integer f = it.next();
            result[i] = f;
            i++;
        }
        return result;
    }

    public List<String> getStringQueue(PriorityQueue pq) {

        List<String> result = new ArrayList<String>();

        while (!pq.isEmpty())
        {
            result.add( ((Command)pq.peek()).getCmd() );
            pq.remove();
        }

        return result;
    }
}