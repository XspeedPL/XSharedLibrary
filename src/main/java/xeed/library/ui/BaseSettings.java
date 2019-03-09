package xeed.library.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import xeed.library.common.R;
import xeed.library.common.SettingsManager;
import xeed.library.common.Utils;
import xeed.library.preference.AppListPreference;
import xeed.library.preference.IntListPreference;
import xeed.library.preference.RingtonePreference;
import xeed.library.preference.SeekBarPreference;
import xeed.library.preference.TextDialogPreference;
import xeed.library.preference.internal.AppListFragment;
import xeed.library.preference.internal.IntListFragment;
import xeed.library.preference.internal.RingtoneFragment;
import xeed.library.preference.internal.SeekBarFragment;
import xeed.library.preference.internal.TextDialogFragment;

public abstract class BaseSettings extends AppCompatActivity implements Preference.OnPreferenceChangeListener, PreferenceFragmentCompat.OnPreferenceStartScreenCallback {
    private static int mActTh = R.style.Theme_Compat;

    private SettingsFragment mFrag = null;
    private SettingsManager mPrefMgr = null;

    protected void onCreatePreferences(PreferenceManager mgr) {
    }

    protected void onPreferenceChanged(PreferenceManager mgr, SharedPreferences prefs, String key) {
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat fragment, PreferenceScreen screen) {
        fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, screen.getKey());
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment, fragment, screen.getKey()).addToBackStack(screen.getKey()).commit();
        return true;
    }

    @Override
    public final boolean onPreferenceChange(Preference p, Object val) {
        mFrag.mChange = true;
        return true;
    }

    protected enum Category {
        general, fixes, info
    }

    public static long getActiveVer() {
        return -1;
    }

    public static long getCurrentVer(Context c) {
        try {
            PackageInfo pkg = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            return PackageInfoCompat.getLongVersionCode(pkg);
        } catch (Exception ex) {
            return -1;
        }
    }

    protected final void hideDonations() {
        Preference pref = mFrag.findPreference("donation");
        PreferenceGroup group = mFrag.findPreference("g_info");
        group.removePreference(pref);
    }

    protected final void addPreferencesToCategory(int resId, @SuppressWarnings("SameParameterValue") Category cat) {
        PreferenceGroup group = mFrag.findPreference(cat == Category.fixes ? "g_fixes" : cat == Category.info ? "g_info" : "g_general");
        PreferenceScreen ps = mFrag.getPreferenceScreen();
        int last = ps.getPreferenceCount();
        mFrag.addPreferencesFromResource(resId);
        while (ps.getPreferenceCount() > last) {
            Preference p = ps.getPreference(last);
            ps.removePreference(p);
            group.addPreference(p);
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        mFrag.onActivityResult(req, res, data);
    }

    @Override
    protected final void onCreate(Bundle b) {
        super.onCreate(b);
        mPrefMgr = SettingsManager.getInstance(this);
        mPrefMgr.fixFolderPermissionsAsync();
        reloadThemes(mPrefMgr.getPrefs());
        setTheme(getActTh());
        setContentView(R.layout.libsettings);
        long installed = getCurrentVer(this);
        long active = getActiveVer();
        if (installed != active) {
            TextView tv = findViewById(R.id.update_msg);
            tv.setVisibility(View.VISIBLE);
            tv.setBackgroundResource(getStyleAttribute(this, R.attr.colorPrimary));
            tv.setTextColor(ContextCompat.getColor(this, getStyleAttribute(this, R.attr.colorAccent)));
            if (active == -1) tv.setText(R.string.diag_reboot);
            else tv.setText(getString(R.string.diag_update, installed, active));
        }
        if (b == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new SettingsFragment()).commit();
        }
    }

    @Override
    protected final void onStart() {
        super.onStart();
        mPrefMgr.onResume();
    }

    @Override
    protected final void onStop() {
        mPrefMgr.onPause();
        super.onStop();
    }

    private static int getStyleAttribute(Context c, int resId) {
        TypedValue tv = new TypedValue();
        TypedArray a = c.obtainStyledAttributes(tv.data, new int[]{resId});
        int ret = a.getResourceId(0, 0);
        a.recycle();
        return ret;
    }

    public static int getActTh() {
        return mActTh;
    }

    public static void reloadThemes(SharedPreferences prefs) {
        switch (prefs.getInt("theme", 0)) {
            case 0:
                mActTh = R.style.Theme_Compat;
                break;
            case 1:
                mActTh = R.style.Theme_Compat_Light;
                break;
            case 2:
                mActTh = R.style.Theme_Compat_Black;
                break;
        }
    }

    public static final class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {
        private static final String DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG";

        private BaseSettings mActivity = null;
        private boolean mChange = false;
        protected SharedPreferences mPrefs = null;

        @Override
        public final void onStart() {
            mActivity = (BaseSettings) requireActivity();
            mActivity.mFrag = this;
            super.onStart();
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public final void onCreatePreferences(Bundle b, String rootKey) {
            mActivity = (BaseSettings) getActivity();
            mActivity.mFrag = this;

            mPrefs = mActivity.mPrefMgr.getPrefs();
            getPreferenceManager().setSharedPreferencesName(Utils.PREFS_NAME);
            getPreferenceManager().setStorageDeviceProtected();
            addPreferencesFromResource(R.xml.prefs_common);

            try {
                int label = mActivity.getPackageManager().getActivityInfo(new ComponentName(mActivity, mActivity.getClass()), 0).labelRes;
                findPreference("hideApp").setSummary(getString(R.string.pref_hideapp_s, getString(label)));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                String ver = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionName;
                String app = getString(mActivity.getApplicationInfo().labelRes);
                findPreference("version").setSummary(app + " " + ver);
            } catch (PackageManager.NameNotFoundException ignored) {
            }

            String suffix = getString(R.string.pref_authors_s_suffix);
            if (!suffix.isEmpty()) {
                TextDialogPreference p = findPreference("authors");
                p.setDialogMessage(p.getDialogMessage() + "\n" + suffix);
            }

            mActivity.onCreatePreferences(getPreferenceManager());

            Preference screen = findPreference(rootKey);
            if (screen instanceof PreferenceScreen) {
                setPreferenceScreen((PreferenceScreen) screen);
            }
        }

        @Override
        public final void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            if ("hideApp".equals(key)) {
                ComponentName cn = new ComponentName(mActivity, mActivity.getPackageName() + ".Launcher");
                mActivity.getPackageManager().setComponentEnabledSetting(cn, sp.getBoolean(key, false) ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            } else if ("theme".equals(key)) {
                reloadThemes(sp);
                mActivity.finish();
            } else mChange = true;
            mActivity.onPreferenceChanged(getPreferenceManager(), mPrefs, key);
        }

        @Override
        public final void onResume() {
            super.onResume();
            mPrefs.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public final void onPause() {
            mPrefs.unregisterOnSharedPreferenceChangeListener(this);
            if (mChange) {
                Intent in = new Intent(mActivity.getPackageName() + ".Update");
                /*
                File f = new File(mActivity.getFilesDir(), "../shared_prefs/" + mActivity.getPrefsName() + ".xml");
                Uri u = FileProvider.getUriForFile(mActivity, mActivity.getPackageName() + ".fileprovider", f);
                mActivity.grantUriPermission("android", u, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                in.setData(u);
                in.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                */
                mActivity.sendBroadcast(in);
                mChange = false;
                Toast.makeText(mActivity, R.string.diag_prf_sav, Toast.LENGTH_SHORT).show();
            }
            super.onPause();
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public final void onDisplayPreferenceDialog(Preference pref) {
            boolean handled = false;
            if (getTargetFragment() instanceof PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback) {
                handled = ((PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback) getTargetFragment()).onPreferenceDisplayDialog(this, pref);
            }
            if (!handled && mActivity instanceof PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback) {
                handled = ((PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback) mActivity).onPreferenceDisplayDialog(this, pref);
            }
            if (!handled && getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
                handled = true;
            }
            if (!handled && DIALOG_REGISTRY.containsKey(pref.getClass())) {
                try {
                    Class<? extends DialogFragment> dlgClass = DIALOG_REGISTRY.get(pref.getClass());
                    DialogFragment fragment = dlgClass.newInstance();
                    Bundle b = new Bundle(1);
                    b.putString("key", pref.getKey());
                    fragment.setArguments(b);
                    fragment.setTargetFragment(this, 0);
                    fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
                    handled = true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (!handled) {
                super.onDisplayPreferenceDialog(pref);
            }
        }
    }

    private static final HashMap<Class, Class<? extends DialogFragment>> DIALOG_REGISTRY = new HashMap<>();

    static {
        registerDialog(IntListPreference.class, IntListFragment.class);
        registerDialog(SeekBarPreference.class, SeekBarFragment.class);
        registerDialog(AppListPreference.class, AppListFragment.class);
        registerDialog(TextDialogPreference.class, TextDialogFragment.class);
        registerDialog(RingtonePreference.class, RingtoneFragment.class);
    }

    public static void registerDialog(Class<? extends DialogPreference> prefClass, Class<? extends DialogFragment> dlgClass) {
        DIALOG_REGISTRY.put(prefClass, dlgClass);
    }
}
