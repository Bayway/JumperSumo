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

import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_JUMPINGSUMO_AUDIOSETTINGS_THEME_THEME_ENUM;
import com.parrot.arsdk.arcontroller.ARDeviceController;

/**
 * This class is the middleware between commands name and robot sdk. <br />
 * If you want implement more simple commands this is your place.
 *
 * @author Massimiliano Fiori [massimiliano.fiori@aol.it]
 * @see it.baywaylabs.jumpersumo.robot.Intelligence
 *
 */
public class Interpreter {

    private ARDeviceController deviceController;

    private static final String FORWARD = "FORWARD";
    private static final String BACK = "BACK";
    private static final String LEFT = "LEFT";
    private static final String RIGHT = "RIGHT";
    private static final String PHOTO = "PHOTO";

    private static final String TAG = Interpreter.class.getSimpleName();

    /**
     *
     * Constructor with deviceController instance.
     *
     * @param deviceController ARDeviceController
     */
    public Interpreter(ARDeviceController deviceController) {
        this.deviceController = deviceController;
    }


    /**
     *
     * This function execute a single command.
     *
     * @param command   one of: [FORWARD, BACK, LEFT, RIGHT, PHOTO, EXECUTE]
     * @return          0 if all is ok, -1 if something is go wrong.
     * @see #doListCommands(String)
     */
    public Integer doCommand (String command) {

        Log.d(TAG, "Comando ricevuto: " + command);

        // FIXME: All Thread.sleep() freeze the video streaming, have to find another way.
        if (deviceController != null) {

            switch (command) {

                case FORWARD:
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 25);
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                    // wait seconds
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 0);
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 0);
                    break;

                case BACK:
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) -25);
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                    // wait seconds
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDSpeed((byte) 0);
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 0);
                    break;

                case LEFT:
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) -25);
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                    // wait seconds
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) 0);
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 0);
                    break;

                case RIGHT:
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) 25);
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 1);
                    // wait seconds
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDTurn((byte) 0);
                    deviceController.getFeatureJumpingSumo().setPilotingPCMDFlag((byte) 0);
                    break;

                case PHOTO:
                    deviceController.getFeatureJumpingSumo().sendMediaRecordPictureV2();
                    break;

                default:
                    Log.w(TAG, "Comando non conosciuto: " + command);

            }
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * This functions execute a commands list. The commands string have to be a <i>csv format</i> (separate by ";").
     *
     * @param commands  example: [BACK;LEFT;FORWARD...]
     * @return          1 if all is ok, -1 if something is go wrong.
     *
     */
    public Integer doListCommands (String commands) {
        // FIXME: All Thread.sleep() freeze the video streaming, have to find another way.
        if (!"".equals(commands) && commands.split(";").length >= 2) {
            for (String command : commands.split(";")) {
                if (!"".equals(command)) {
                    Log.d(TAG, "Comando: " + command);
                    doCommand(command.toUpperCase());
                }else
                    Log.d(TAG, "Comando invalido!");

                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // Play Sound when finish.
            /*
             * TODO: Play sound is not possible at the moment: http://forum.developer.parrot.com/t/how-to-play-sound-file-in-jumpingsumo/158; i can only change audio theme in this way: deviceController.getFeatureJumpingSumo().sendAudioSettingsTheme(ARCOMMANDS_JUMPINGSUMO_AUDIOSETTINGS_THEME_THEME_ENUM.ARCOMMANDS_JUMPINGSUMO_AUDIOSETTINGS_THEME_THEME_MAX);
             */

            // I can play a sound with this unstable method:
            try {
                deviceController.getFeatureJumpingSumoDebug().sendAudioPlaySoundWithName("/media/audio/Max/shock.wav");
            } catch (Exception e) {
                Log.e(TAG, "sendAudioPlaySoundWithName Debug Feature returns an error.");
            }

            return 1;
        } else if (!"".equals(commands) && commands.split(";").length < 2) {
            doCommand(commands.toUpperCase());

            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Play Sound when finish.
            /*
             * TODO: Play sound is not possible at the moment: http://forum.developer.parrot.com/t/how-to-play-sound-file-in-jumpingsumo/158; i can only change audio theme in this way: deviceController.getFeatureJumpingSumo().sendAudioSettingsTheme(ARCOMMANDS_JUMPINGSUMO_AUDIOSETTINGS_THEME_THEME_ENUM.ARCOMMANDS_JUMPINGSUMO_AUDIOSETTINGS_THEME_THEME_MAX);
             */

            // I can play a sound with this unstable method:
            try {
                deviceController.getFeatureJumpingSumoDebug().sendAudioPlaySoundWithName("/media/audio/Max/shock.wav");
            } catch (Exception e) {
                Log.e(TAG, "sendAudioPlaySoundWithName Debug Feature returns an error.");
            }

            return 1;
        }

        Log.w(TAG, "List of commands are invalid!");
        return -1;
    }
}