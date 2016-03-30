package de.xavaro.android.common;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class BackupManager
{
    private static final String LOGTAG = BackupManager.class.getSimpleName();

    public static void backupPackageData(String prefix)
    {
        if (! isExternalStorageWritable()) return;

        File intFiles = Simple.getAnyContext().getFilesDir();
        File extFiles = Simple.getAnyContext().getExternalFilesDir(null);

        if (extFiles == null) return;

        Log.d(LOGTAG, "internal:" + intFiles.toString());
        Log.d(LOGTAG, "external:" + extFiles.toString());

        File[] targets= intFiles.listFiles();
        if (targets == null) return;

        for (File target : targets)
        {
            Log.d(LOGTAG, "internal:" + target.toString());
        }
    }

    public static boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
