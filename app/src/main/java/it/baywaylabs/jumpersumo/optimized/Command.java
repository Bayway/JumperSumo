package it.baywaylabs.jumpersumo.optimized;

/**
 * Created by Massimiliano on 28/03/2016.
 */
public class Command {

    private int priority;
    private String cmd;

    public Command(int priority, String cmd) {
        this.priority = priority;
        this.cmd = cmd;
    }

    public Command() {
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
}
