package com.mobindustry.photoweatherapp.core.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileSaver {
    private FileSaver() {}

    public static void saveFile(String path, byte[] data) {
        final File file = new File(path);
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
