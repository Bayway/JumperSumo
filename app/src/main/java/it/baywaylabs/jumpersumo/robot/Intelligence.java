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

import java.util.ArrayList;
import java.util.List;

import it.baywaylabs.jumpersumo.utility.Finder;

/**
 * This class is a small robot consciousness. There are the keywords that robot use to
 * understand command and respective answers to reply with the world. <br />
 * If you want implement more simple commands this is your place.
 *
 * @author Massimiliano Fiori [massimiliano.fiori@aol.it]
 * @see it.baywaylabs.jumpersumo.robot.Interpreter
 */
public class Intelligence {


    private List<String> moveOn = new ArrayList<String>() {{
        add("go on");
        add("forward");
        add("avanti");
    }};


    private List<String> moveBack = new ArrayList<String>() {{
        add("back");
        add("indietro");
    }};


    private List<String> turnRight = new ArrayList<String>() {{
        // add("turn right");
        add("right");
        add("destra");
    }};


    private List<String> turnLeft = new ArrayList<String>() {{
        add("turn left");
        add("left");
        add("sinistra");
    }};


    private List<String> takePhoto = new ArrayList<String>() {{
        // add("what's going on");
        // add("cosa sta succedendo");
        add("photo");
        add("foto");
    }};


    private List<String> executeCsv = new ArrayList<String>() {{
        add("execute");
        add("eseguire");
        add("download");
        add("esegui");
    }};


    private List<String> moveOnAnswer = new ArrayList<String>() {{
        add("Yes, I can do it ");
        add("I'm going on ");
    }};

    private List<String> moveBackAnswer = new ArrayList<String>() {{
        add("Yes, I can do it ");
        add("I'm going back ");
    }};

    private List<String> turnLeftAnswer = new ArrayList<String>() {{
        add("Yes, I can do it ");
        add("Really? This is a new prospective ");
        add("I'm turning left ");
    }};

    private List<String> turnRightAnswer = new ArrayList<String>() {{
        add("Yes, I can do it ");
        add("Really? This is a new prospective ");
        add("I'm turning right ");
    }};

    private List<String> takePhotoAnswer = new ArrayList<String>() {{
        add("Yes, I can do it ");
        add("This is my place ");
        add("Do you like? ");
    }};

    private List<String> executeCsvAnswer = new ArrayList<String>() {{
        add("Yes, I can do it ");
        add("I try to do ");
        add("It wasn't easy, but i did it ");
    }};

    /**
     *
     * @return Commands list known to move forward.
     */
    public List<String> getMoveOn() {
        return moveOn;
    }

    /**
     * Commands list known to move back.
     */
    public List<String> getMoveBack() {
        return moveBack;
    }

    /**
     * Commands list known to turn right.
     */
    public List<String> getTurnRight() {
        return turnRight;
    }

    /**
     * Commands list known to turn left.
     */
    public List<String> getTurnLeft() {
        return turnLeft;
    }

    /**
     * Commands list known to take picture.
     */
    public List<String> getTakePhoto() {
        return takePhoto;
    }

    /**
     * Commands list known to execute a csv file.
     */
    public List<String> getExecuteCsv() {
        return executeCsv;
    }

    /**
     * Answers list known to move forward command.
     */
    public List<String> getMoveOnAnswer() {
        return moveOnAnswer;
    }

    /**
     * Answers list known to move back command.
     */
    public List<String> getMoveBackAnswer() {
        return moveBackAnswer;
    }

    /**
     * Answers list known to turn left command.
     */
    public List<String> getTurnLeftAnswer() {
        return turnLeftAnswer;
    }

    /**
     * Answers list known to turn right command.
     */
    public List<String> getTurnRightAnswer() {
        return turnRightAnswer;
    }

    /**
     * Answers list known to take picture command.
     */
    public List<String> getTakePhotoAnswer() {
        return takePhotoAnswer;
    }

    /**
     * Answers list known to to execute a csv file command.
     */
    public List<String> getExecuteCsvAnswer() {
        return executeCsvAnswer;
    }

    /**
     * Command type is one of:
     * <ul>
     *     <li>FORWARD</li>
     *     <li>BACK</li>
     *     <li>LEFT</li>
     *     <li>RIGHT</li>
     *     <li>PHOTO</li>
     *     <li>EXECUTE</li>
     * </ul>
     *
     * @param type command type.
     * @return answers list knew.
     */
    public List<String> actionAnswer(String type) {
        List<String> result = null;
        if (!"".equals(type)) {
            switch (type) {
                case "FORWARD":
                    result = getMoveOnAnswer();
                    break;
                case "BACK":
                    result = getMoveBackAnswer();
                    break;
                case "LEFT":
                    result = getTurnLeftAnswer();
                    break;
                case "RIGHT":
                    result = getTurnRightAnswer();
                    break;
                case "PHOTO":
                    result = getTakePhotoAnswer();
                    break;
                case "EXECUTE":
                    result = getExecuteCsvAnswer();
                    break;
            }
        }
        return result;
    }

    /**
     *
     * Command type is one of:
     * <ul>
     *     <li>FORWARD</li>
     *     <li>BACK</li>
     *     <li>LEFT</li>
     *     <li>RIGHT</li>
     *     <li>PHOTO</li>
     *     <li>EXECUTE</li>
     * </ul>
     *
     * @param type command type.
     * @return commands list knew.
     */
    public List<String> action(String type) {
        List<String> result = null;
        if (!"".equals(type)) {
            switch (type) {
                case "FORWARD":
                    result = getMoveOn();
                    break;
                case "BACK":
                    result = getMoveBack();
                    break;
                case "LEFT":
                    result = getTurnLeft();
                    break;
                case "RIGHT":
                    result = getTurnRight();
                    break;
                case "PHOTO":
                    result = getTakePhoto();
                    break;
                case "EXECUTE":
                    result = getExecuteCsv();
                    break;
            }
        }
        return result;
    }

    /**
     * @return List of all known actions.
     */
    public List<String> getAllActions() {
        Finder f = new Finder();
        return f.joinListCommands(getMoveOn(), getMoveBack(), getTurnLeft(), getTurnRight(), getTakePhoto(), getExecuteCsv());
    }
}
