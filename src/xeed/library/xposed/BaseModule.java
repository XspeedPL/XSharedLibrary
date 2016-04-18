package xeed.library.xposed;

import java.util.Locale;

import android.content.*;
import android.content.res.Resources;
import android.os.Build;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public abstract class BaseModule implements IXposedHookLoadPackage
{
    public static final int SDK = Build.VERSION.SDK_INT;
    
    protected final String mPackage;
    protected XSharedPreferences mPrefs = null;
    protected Context mCtx = null;
    
    private boolean mDebug;
    
    protected abstract int getVersion();
    protected abstract String getLogTag();
    protected abstract void reloadPrefs(final Intent i);
    protected abstract void handlePackage(final String pkgName, final ClassLoader src) throws Throwable;
    
    protected String getMainPackage() { return "android"; }
    protected boolean shouldHookPWM() { return true; }
    protected void initPWM(final Object pwm) { }
    
    public BaseModule()
    {
        mPackage = getClass().getPackage().getName();
    }
    
    protected final void log(final String txt)
    {
        XposedBridge.log(getLogTag() + ": " + txt);
    }
    
    protected final void dlog(final String txt)
    {
        if (mDebug) log(txt);
    }
    
    protected final void log(final Throwable t)
    {
        XposedBridge.log(getLogTag() + ": EXCEPTION");
        XposedBridge.log(t);
    }
    
    @Override
    public final void handleLoadPackage(final LoadPackageParam lpp) throws Throwable
    {
        if (lpp.packageName.equals("android"))
        {
            log("Android version " + SDK + ", module version " + getVersion());
            final Class<?> cPWM = tryFindClass(lpp.classLoader, ClassDB.PHONE_WINDOW_MANAGER);
            if (shouldHookPWM()) XposedBridge.hookAllMethods(cPWM, "init", handlePWMI);
        }
        if (lpp.packageName.equals(getMainPackage()))
        {
            mPrefs = new XSharedPreferences(getPackage(), getLogTag().toLowerCase(Locale.ENGLISH) + "settings");
            mPrefs.reload();
            mDebug = mPrefs.getBoolean("debugLog", false);
            log("Debug log is " + (mDebug ? "en" : "dis") + "abled");
            reloadPrefs(new Intent());
        }
        if (lpp.packageName.equals(mPackage))
        {
            XposedHelpers.findAndHookMethod("xeed.library.ui.BaseSettings", lpp.classLoader, "getActiveVer", XC_MethodReplacement.returnConstant(getVersion()));
        }
        handlePackage(lpp.packageName, lpp.classLoader);
    }
    
    protected static final Class<?> tryFindClass(final ClassLoader loader, final String... names)
    {
        for (final String s : names)
            try
            {
                final Class<?> c = XposedHelpers.findClass(s, loader);
                if (c != null) return c;
            }
            catch (final Throwable t) { }
        throw new RuntimeException("Class not found: " + names[0].substring(names[0].lastIndexOf(".") + 1));
    }
    
    protected final void registerWithContext(final Context c)
    {
        mCtx = c;
        mCtx.registerReceiver(new BroadcastReceiver()
        {
            public final void onReceive(final Context c, final Intent i)
            {
                mPrefs.reload();
                mDebug = mPrefs.getBoolean("debugLog", false);
                reloadPrefs(i);
                dlog("Preferences reloaded");
            }
        }, new IntentFilter(getPackage() + ".Update"), null, null);
    }
    
    private final XC_MethodHook handlePWMI = new XC_MethodHook()
    {
        @Override
        protected final void afterHookedMethod(final MethodHookParam mhp)
        {
            log("PWM init");
            registerWithContext((Context)mhp.args[0]);
            initPWM(mhp.thisObject);
        }
    };
    
    protected final boolean isReady() { return mCtx != null; }
    
    protected final String getPackage()
    {
        return mPackage;
    }
    
    protected final String getString(final int id, final Object... args)
    {
        try
        {
            final Resources r = mCtx.getPackageManager().getResourcesForApplication(getPackage());
            return r.getString(id, args);
        }
        catch (final Exception ex)
        {
            log(ex);
            return "ERROR: " + ex.getLocalizedMessage();
        }
    }
    
    protected static final class ClassDB
    {
        public static final String[] INPUT_MANAGER = new String[] { "com.android.server.input.InputManagerService", "com.android.server.wm.InputManager", "com.android.server.InputManager" };
        public static final String[] PHONE_WINDOW_MANAGER = new String[] { "com.android.server.policy.PhoneWindowManager", "com.android.internal.policy.impl.PhoneWindowManager" };
        public static final String[] NOTIFICATION_MANAGER = new String[] { "com.android.server.notification.NotificationManagerService", "com.android.server.NotificationManagerService" };
    }
}
