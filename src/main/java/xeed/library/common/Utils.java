package xeed.library.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.TypedValue;

import java.io.File;
import java.util.ArrayList;

public final class Utils {
    private static final int SDK = Build.VERSION.SDK_INT;

    public static final String PREFS_NAME = "settings";

    public static final String TAG = "XSharedLibrary";

    public static String getDataDir() {
        if (SDK > 23) {
            return "user_de/0/";
        } else {
            return "data/";
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("SetWorldReadable")
    public static void prefPermissionFix(File prefs) {
        File prefDir = prefs.getParentFile();

        File pkgDir = prefDir.getParentFile();
        pkgDir.setExecutable(true, false);
        pkgDir.setReadable(true, false);

        prefDir.setExecutable(true, false);
        prefDir.setReadable(true, false);

        prefs.setReadable(true, false);
    }

    public static float dpPx(Context c, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }

    public static float spPx(Context c, float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, c.getResources().getDisplayMetrics());
    }

    public static String serialize(Iterable<AppInfo<String>> data) {
        StringBuilder sb = new StringBuilder();
        for (AppInfo<String> ai : data)
            sb.append(ai.data).append(" ");
        return sb.toString();
    }

    public static ArrayList<AppInfo<String>> deserialize(String data) {
        String[] arr = data.split(" ");
        ArrayList<AppInfo<String>> ret = new ArrayList<>(arr.length);
        for (String s : arr) if (s.length() > 0) ret.add(new AppInfo<>(s));
        return ret;
    }
}
