package xeed.library.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.FileObserver;

import java.io.File;

import androidx.core.content.ContextCompat;

public class SettingsManager {
    private final Context mContext;
    private final PublicPreferences mPrefs;
    private final FileObserver mFileObserver;

    private SettingsManager(Context context) {
        mContext = Build.VERSION.SDK_INT >= 24 && !ContextCompat.isDeviceProtectedStorage(context)
                ? ContextCompat.createDeviceProtectedStorageContext(context) : context;
        mPrefs = new PublicPreferences(mContext, Utils.PREFS_NAME);

        mFileObserver = new FileObserver(mContext.getFilesDir().getParentFile() + "/shared_prefs", FileObserver.ATTRIB | FileObserver.CLOSE_WRITE) {
            @Override
            public void onEvent(int event, String path) {
                if ((event & FileObserver.ATTRIB) != 0)
                    mPrefs.onFileAttributesChanged(path);
                if ((event & FileObserver.CLOSE_WRITE) != 0)
                    mPrefs.onFileUpdated(path);
            }
        };
        mFileObserver.startWatching();
    }

    public static synchronized SettingsManager getInstance(Context context) {
        if (context == null) throw new IllegalArgumentException("Context cannot be null");
        if (context.getApplicationContext() != null) context = context.getApplicationContext();
        return new SettingsManager(context);
    }

    public void fixFolderPermissionsAsync() {
        AsyncTask.execute(new Runnable() {
            @SuppressLint("SetWorldReadable")
            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public void run() {
                File pkgFolder = mContext.getFilesDir().getParentFile();
                if (pkgFolder.exists()) {
                    pkgFolder.setExecutable(true, false);
                    pkgFolder.setReadable(true, false);
                }
                File cacheFolder = mContext.getCacheDir();
                if (cacheFolder.exists()) {
                    cacheFolder.setExecutable(true, false);
                    cacheFolder.setReadable(true, false);
                }
                File filesFolder = mContext.getFilesDir();
                if (filesFolder.exists()) {
                    filesFolder.setExecutable(true, false);
                    filesFolder.setReadable(true, false);
                    for (File f : filesFolder.listFiles()) {
                        f.setExecutable(true, false);
                        f.setReadable(true, false);
                    }
                }
            }
        });
    }

    public PublicPreferences getPrefs() {
        return mPrefs;
    }

    public void onPause() {
        mFileObserver.stopWatching();
        mPrefs.fixPermissions(true);
    }

    public void onResume() {
        mPrefs.fixPermissions(true);
        mFileObserver.startWatching();
    }
}
