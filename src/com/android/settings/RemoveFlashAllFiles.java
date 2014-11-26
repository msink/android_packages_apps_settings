package com.android.settings;

import java.io.File;

public class RemoveFlashAllFiles {
    private static int count = 0;

    public static void removeAllFiles() {
        File flash = new File("/sdcard");
        File[] files = flash.listFiles();
        count = 0;
        for (File file : files) {
            if (file.getName().equals("dictionary")
                || file.getName().equals("ivona")
                || file.getName().equals("Guia_de_usuario-User_guide-tagusbooks.com.epub")
                || file.getName().equals("Библиотека")
                || file.getName().equals("Books")
                || file.getName().equals("Lektury"))
                continue;
            removeFlash(file);
        }
    }

    private static void removeFlash(File file) {
        try {
            if (!file.exists()) return;
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (file.isFile()) {
            count = count + 1;
            deleteFile(file);
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                count = count + 1;
                file.delete();
            } else {
                int length = files.length;
                for (int j = 0; j < length; j++) {
                    removeFlash(files[j]);
                }
                count = count + 1;
                deleteFile(file);
            }
        }
    }

    private static void deleteFile(File file) {
        try {
            file.delete();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static int getDeleteFilesCount() {
        return count;
    }
}
