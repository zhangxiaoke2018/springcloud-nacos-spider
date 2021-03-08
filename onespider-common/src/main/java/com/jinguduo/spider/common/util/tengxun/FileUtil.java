package com.jinguduo.spider.common.util.tengxun;

import java.io.File;

public class FileUtil {
    public static void loadLibrary(String soFileName) {
        try {
            String soFilePath = "/data/data/com.qq.ac.android/libs/lib" + soFileName + ".so";
            File soFile = new File(soFilePath);

            if (soFile.exists()) {
                System.load(soFile.getAbsolutePath());
                return;
            }
            System.loadLibrary(soFileName);
        } catch (Throwable t) {
        }
    }
}
