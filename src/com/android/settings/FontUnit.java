package com.android.settings;

import android.os.Environment;
import android.graphics.Typeface;
import android.util.Log;

import java.io.*;
import java.util.*;

public class FontUnit {
    private static final String TAG = "FontUnit";

    private static Map<String,File[]> ourFontMap;
    private static Set<File> ourFileSet;
    private static long ourTimeStamp;
    private static final HashMap<String,Typeface[]> ourTypefaces = new HashMap<String,Typeface[]>();

    private static Map<String,File[]> getFontMap(boolean forceReload) {
        final long timeStamp = System.currentTimeMillis();
        if (forceReload && timeStamp < ourTimeStamp + 1000) {
            forceReload = false;
        }
        ourTimeStamp = timeStamp;
        if (ourFileSet == null || forceReload) {
            final HashSet<File> fileSet = new HashSet<File>();
            final FilenameFilter filter1 = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.startsWith(".")) {
                        return false;
                    }
                    if (name.indexOf("Clockopia") != -1) {
                        return false;
                    }
                    final String lcName = name.toLowerCase();
                    return lcName.endsWith(".ttf") || lcName.endsWith(".otf");
                }
            };
            final FilenameFilter filter2 = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.startsWith(".")) {
                        return false;
                    }
                    final String lcName = name.toLowerCase();
                    return lcName.endsWith(".ttf") || lcName.endsWith(".otf");
                }
            };
            final File[] fileList1 = new File("/system/fonts/").listFiles(filter1);
            final File[] fileList2 = new File("/flash").listFiles(filter2);
            if (fileList1 != null) {
                fileSet.addAll(Arrays.asList(fileList1));
            }
            if (fileList2 != null) {
                fileSet.addAll(Arrays.asList(fileList2));
            }
            if (!fileSet.equals(ourFileSet)) {
                ourFileSet = fileSet;
                ourFontMap = new FontInfoDetector().collectFonts(fileSet);
            }
        }
        return ourFontMap;
    }

    public static void fillFamiliesList(ArrayList<String> families) {
        final TreeSet<String> familySet = new TreeSet<String>(getFontMap(true).keySet());
        families.addAll(familySet);
    }

    public static void setDefaultFont(String family) {
        File targetFont = null;
        File targetRegularFont = null;
        File targetBoldFont = null;
        try {
            if (ourFontMap.get(family).length > 0) {
                for (int i = 0; i < ourFontMap.get(family).length; i++) {
                    if (ourFontMap.get(family)[i] != null) {
                        Log.d(TAG, i + "    " + ourFontMap.get(family)[i].getPath());
                    }
                }
            }
            String familyName = family.replaceAll(" ", "");
            if (ourFontMap.get(family).length > 0) {
                if (family.equals("Droid Sans")) {
                    targetFont = new File("/system/fonts/DroidSansBackup.ttf");
                    targetBoldFont = new File("/system/fonts/DroidSansBackup-Bold.ttf");
                } else {
                    targetFont = new File("/system/fonts/" + familyName + ".ttf");
                    targetBoldFont = new File("/system/fonts/" + familyName + "-Bold.ttf");
                }
                targetRegularFont = new File("/system/fonts/" + familyName + "-Regular.ttf");
                File font = null;
                File boldFont = null;
                if (targetFont.exists()) {
                    font = targetFont;
                } else if (targetRegularFont.exists()) {
                    font = targetRegularFont;
                } else {
                    font = ourFontMap.get(family)[0];
                }
                if (targetBoldFont.exists()) {
                    boldFont = targetBoldFont;
                } else if (targetFont.exists()) {
                    boldFont = targetFont;
                } else if (targetRegularFont.exists()) {
                    boldFont = targetRegularFont;
                } else {
                    boldFont = ourFontMap.get(family)[0];
                }
                Log.d(TAG, "font : " + font.getPath() + "  boldFont : " + boldFont.getPath());
                copyFile(font, new File("/data/fonts/DroidSansTmp.ttf"));
                copyFile(boldFont, new File("/data/fonts/DroidSans-BoldTmp.ttf"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            File parentFolder = targetFile.getParentFile();
            if (!parentFolder.exists()) {
                parentFolder.mkdir();
            }
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            outBuff.flush();
        } finally {
            Log.d(TAG, "copy finish!");
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }
}
