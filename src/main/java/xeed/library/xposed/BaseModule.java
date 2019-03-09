package xeed.library.xposed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;

import java.io.File;

import androidx.annotation.CallSuper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import xeed.library.common.Utils;

public abstract class BaseModule implements Module {
    protected static final int SDK = Build.VERSION.SDK_INT;

    protected final String mPackage;
    protected XSharedPreferences mPrefs = null;
    protected Context mCtx = null;

    private boolean mDebug;

    @Override
    public abstract long getVersion();

    protected abstract String getLogTag();

    protected abstract void reloadPrefs(Intent i);

    @Override
    public String getMainPackage() {
        return "android";
    }

    protected boolean shouldHookPWM() {
        return true;
    }

    protected void initPWM(Object pwm) {
    }

    @Override
    public String getModulePackage() {
        return getClass().getPackage().getName();
    }

    public BaseModule() {
        mPackage = getModulePackage();
    }

    @Override
    public final void log(String txt) {
        XposedBridge.log(getLogTag() + ": " + txt);
    }

    @Override
    public final void dlog(String txt) {
        if (mDebug) log(txt);
    }

    @Override
    public final void log(Throwable t) {
        XposedBridge.log(getLogTag() + ": EXCEPTION");
        XposedBridge.log(t);
    }

    @Override
    @CallSuper
    public void initZygote(StartupParam param) {
        File f = new File("/data/" + Utils.getDataDir() + mPackage + "/shared_prefs/" + Utils.PREFS_NAME + ".xml");
        mPrefs = new XSharedPreferences(f);
    }

    @Override
    @CallSuper
    public void handleLoadPackage(LoadPackageParam param) throws Throwable {
        if (param.packageName.equals("android")) {
            log("Android version " + SDK + ", module version " + getVersion());
            if (shouldHookPWM()) {
                Class<?> cPWM = tryFindClass(param.classLoader, ClassDB.PHONE_WINDOW_MANAGER);
                XposedBridge.hookAllMethods(cPWM, "init", handlePWMI);
            }
        }
        if (param.packageName.equals(getMainPackage())) {
            mDebug = mPrefs.getBoolean("debugLog", false);
            log("Debug log is " + (mDebug ? "en" : "dis") + "abled");
            reloadPrefs(new Intent());
        }
        if (param.packageName.equals(mPackage)) {
            XposedHelpers.findAndHookMethod("xeed.library.ui.BaseSettings", param.classLoader, "getActiveVer", XC_MethodReplacement.returnConstant(getVersion()));
        }
    }

    protected static Class<?> tryFindClass(ClassLoader loader, String... names) {
        for (String s : names)
            try {
                Class<?> c = XposedHelpers.findClass(s, loader);
                if (c != null) return c;
            } catch (Throwable ignored) {
            }
        throw new RuntimeException("Class not found: " + names[0].substring(names[0].lastIndexOf(".") + 1));
    }

    protected final void registerWithContext(Context c) {
        mCtx = c;
        c.registerReceiver(new BroadcastReceiver() {
            @Override
            public final void onReceive(Context c, Intent i) {
                mPrefs.reload();
                mDebug = mPrefs.getBoolean("debugLog", false);
                reloadPrefs(i);
                log("Preferences reloaded");
            }
        }, new IntentFilter(mPackage + ".Update"));
    }

    private final XC_MethodHook handlePWMI = new XC_MethodHook() {
        @Override
        protected final void afterHookedMethod(MethodHookParam mhp) {
            log("PWM init");
            registerWithContext((Context) mhp.args[0]);
            initPWM(mhp.thisObject);
        }
    };

    protected final boolean isReady() {
        return mCtx != null;
    }

    protected final String getString(int id, Object... args) {
        try {
            Resources r = mCtx.getPackageManager().getResourcesForApplication(mPackage);
            return r.getString(id, args);
        } catch (Exception ex) {
            log(ex);
            return "ERROR: " + ex.getLocalizedMessage();
        }
    }

    protected static final class ClassDB {
        public static final String[] INPUT_MANAGER = new String[]{"com.android.server.input.InputManagerService", "com.android.server.wm.InputManager", "com.android.server.InputManager"};
        public static final String[] PHONE_WINDOW_MANAGER = new String[]{"com.android.server.policy.PhoneWindowManager", "com.android.internal.policy.impl.PhoneWindowManager"};
        public static final String[] NOTIFICATION_MANAGER = new String[]{"com.android.server.notification.NotificationManagerService", "com.android.server.NotificationManagerService"};
    }
}
