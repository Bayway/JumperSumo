package it.baywaylabs.jumpersumo.utility;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by Bayway on 01/04/16.
 */
public class FileFilter implements FilenameFilter {

    public FileFilter() {
        super();
    }

    @Override
    public boolean accept(File dir, String filename) {
        return (filename.endsWith(".csv") || filename.endsWith(".txt"));

    }
}
