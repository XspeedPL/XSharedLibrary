package xeed.library.ui;

import android.annotation.SuppressLint;
import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.support.v4.content.ContextCompat;
import android.support.v4.preference.PreferenceFragmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import xeed.library.common.R;

@SuppressLint("NewApi")
public abstract class BaseSettings extends AppCompatActivity implements OnPreferenceChangeListener
{
    private static int mActTh = R.style.Theme_Compat, mDiagTh = R.style.Theme_Compat_Dialog;
    
    private SettingsFragment mFrag = null;
    
    protected abstract String getPrefsName();
    
    protected void onCreatePreferences(final PreferenceManager mgr) { }
    protected void onPreferenceChanged(final PreferenceManager mgr, final SharedPreferences prefs, final String key) { }
    
    @Override
    public final boolean onPreferenceChange(final Preference p, final Object val)
    {
        mFrag.mChange = true;
        return true;
    }
    
    protected enum Category
    {
        general, fixes, info
    }
    
    protected static final int getActiveVer() { return -1; }
    
    protected final int getCurrentVer()
    {
        try { return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode; }
        catch (final Exception ex) { return -1; }
    }
    
    protected final void addPreferencesToCategory(final int resId, final Category cat)
    {
        final PreferenceGroup pg = (PreferenceGroup)mFrag.findPreference(cat == Category.fixes ? "g_fixes" : cat == Category.info ? "g_info" : "g_general");
        final PreferenceScreen ps = mFrag.getPreferenceScreen();
        final int last = ps.getPreferenceCount();
        mFrag.addPreferencesFromResource(resId);
        while (ps.getPreferenceCount() > last)
        {
            final Preference p = ps.getPreference(last);
            ps.removePreference(p);
            pg.addPreference(p);
        }
    }
    
    @Override
    protected void onActivityResult(final int req, final int res, final Intent data)
    {
        super.onActivityResult(req, res, data);
        mFrag.onActivityResult(req, res, data);
    }
    
    @SuppressLint("WorldReadableFiles")
    @SuppressWarnings("deprecation")
    @Override
    protected final void onCreate(final Bundle b)
    {
        super.onCreate(b);
        reloadThemes(getSharedPreferences(getPrefsName(), MODE_WORLD_READABLE));
        setTheme(getActTh());
        setContentView(R.layout.settings);
        if (getActiveVer() != getCurrentVer())
        {
            final TextView tv = (TextView)findViewById(R.id.update_msg);
            tv.setVisibility(View.VISIBLE);
            tv.setBackgroundResource(getStyleAttribute(this, R.attr.colorPrimary));
            tv.setTextColor(ContextCompat.getColor(this, getStyleAttribute(this, R.attr.colorAccent)));
            if (getActiveVer() == -1) tv.setText(R.string.diag_reboot);
            else tv.setText(getString(R.string.diag_update, getCurrentVer(), getActiveVer()));
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new SettingsFragment()).commit();
    }
    
    private static int getStyleAttribute(final Context c, final int resId)
    {
        TypedValue tv = new TypedValue();
        TypedArray a = c.obtainStyledAttributes(tv.data, new int[] { resId });
        final int ret = a.getResourceId(0, 0);
        a.recycle();
        return ret;
    }
    
    public static final int getActTh() { return mActTh; }
    
    public static final int getDiagTh() { return mDiagTh; }
    
    public static final void reloadThemes(final SharedPreferences prefs)
    {
        final int i = prefs.getInt("theme", 0);
        if (i == 0)
        {
            mActTh = R.style.Theme_Compat;
            mDiagTh = R.style.Theme_Compat_Dialog;
        }
        else if (i == 1)
        {
            mActTh = R.style.Theme_Compat_Light;
            mDiagTh = R.style.Theme_Compat_Light_Dialog;
        }
        else
        {
            mActTh = R.style.Theme_Compat_Black;
            mDiagTh = R.style.Theme_Compat_Black_Dialog;
        }
    }
    
    public static final class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener
    {
        private BaseSettings mActivity = null;
        private boolean mChange = false;

        @SuppressWarnings("deprecation")
        @Override
        public final void onCreate(final Bundle b)
        {
            super.onCreate(b);
            mActivity = (BaseSettings)getActivity();
            mActivity.mFrag = this;
            getPreferenceManager().setSharedPreferencesName(mActivity.getPrefsName());
            getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.prefs_common);
            try
            {
                final int label = mActivity.getPackageManager().getActivityInfo(new ComponentName(mActivity, mActivity.getClass()), 0).labelRes;
                findPreference("hideApp").setSummary(getString(R.string.pref_hideapp_s, getString(label)));
            }
            catch (final Exception ex) { ex.printStackTrace(); }
            mActivity.onCreatePreferences(getPreferenceManager());
        }
        
        @Override
        public final void onSharedPreferenceChanged(final SharedPreferences sp, final String key)
        {
            if (key.equals("hideApp"))
            {
                final ComponentName cn = new ComponentName(mActivity, mActivity.getPackageName() + ".Launcher");
                mActivity.getPackageManager().setComponentEnabledSetting(cn, sp.getBoolean(key, false) ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }
            else if (key.equals("theme"))
            {
                reloadThemes(sp);
                mActivity.finish();
            }
            else mChange = true;
            mActivity.onPreferenceChanged(getPreferenceManager(), getPreferenceManager().getSharedPreferences(), key);
        }
    
        @Override
        public final void onResume()
        {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }
    
        @Override
        public final void onPause()
        {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            if (mChange)
            {
                mActivity.sendBroadcast(new Intent(mActivity.getPackageName() + ".Update"));
                mChange = false;
                Toast.makeText(mActivity, R.string.diag_prf_sav, Toast.LENGTH_SHORT).show();
            }
            super.onPause();
        }
    }
}