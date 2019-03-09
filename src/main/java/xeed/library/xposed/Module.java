package xeed.library.xposed;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;

public interface Module extends IXposedHookLoadPackage, IXposedHookZygoteInit {
    void log(String txt);

    void dlog(String txt);

    void log(Throwable t);

    long getVersion();

    String getMainPackage();

    String getModulePackage();

    @Override
    void initZygote(StartupParam param);
}
