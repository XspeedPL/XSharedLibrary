package xeed.library.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.*;
import android.preference.DialogPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.util.AttributeSet;
import android.view.*;
import android.widget.*;
import xeed.library.common.AppInfo;
import xeed.library.common.R;
import xeed.library.common.Utils;
import xeed.library.ui.AppDialog;
import xeed.library.ui.BaseSettings;

public final class AppListPreference extends DialogPreference
{
    private final ArrayList<AppInfo<String>> data = new ArrayList<AppInfo<String>>();
    private ListView lv = null;
    
    public AppListPreference(final Context c, final AttributeSet as)
    {
        super(c, as);
    }

    @Override
    public final void onPrepareDialogBuilder(final Builder b)
    {
        super.onPrepareDialogBuilder(b);
        data.clear();
        data.addAll(Utils.deserialize(getPersistedString("")));
        lv = new ListView(getContext());
        lv.setAdapter(new AppAdapter());
        new LoadTask().execute();
        b.setView(lv);
        b.setPositiveButton(android.R.string.ok, new OnClickListener()
        {
            @Override
            public final void onClick(final DialogInterface di, final int i)
            {
                persistString(Utils.serialize(data));
            }
        });
        b.setNeutralButton(R.string.btn_add_new, null);
    }
    
    @Override
    protected final void showDialog(final Bundle b)
    {
        super.showDialog(b);
        final AlertDialog ad = (AlertDialog)getDialog();
        ad.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public final void onClick(final View v)
            {
                AppDialog.create(getContext(), new AppDialog.AppInfoCollector<String>()
                {
                    @Override
                    public final ArrayList<AppInfo<String>> collectAsync(final PackageManager pm, final AppDialog.ProgressListener task)
                    {
                        final List<ApplicationInfo> pkgs = pm.getInstalledApplications(0);
                        final ArrayList<AppInfo<String>> res = new ArrayList<AppInfo<String>>(pkgs.size());
                        task.postProgress(0, pkgs.size());
                        for (int i = 0; i < pkgs.size(); ++i)
                        {
                            final ApplicationInfo appi = pkgs.get(i);
                            final AppInfo<String> ai = new AppInfo<String>(appi.packageName);
                            ai.icon = appi.loadIcon(pm);
                            ai.label = appi.loadLabel(pm).toString();
                            res.add(ai);
                            task.postProgress(1, i + 1);
                        }
                        return res;
                    }
                }, new AppDialog.AppInfoListener<String>()
                {
                    @Override
                    public final void onDialogResult(final AppInfo<String> ai)
                    {
                        if (!data.contains(ai))
                        {
                            data.add(ai);
                            Collections.sort(data);
                            ((BaseAdapter)lv.getAdapter()).notifyDataSetChanged();
                        }
                    }
                }, BaseSettings.getDiagTh()).show();
            }
        });
    }
    
    private final class AppAdapter extends BaseAdapter implements View.OnClickListener
    {
        @Override
        public int getCount() { return data.size(); }

        @Override
        public final Object getItem(final int i) { return data.get(i); }

        @Override
        public final long getItemId(final int i) { return getItem(i).hashCode(); }

        @Override
        public final View getView(final int i, View v, final ViewGroup vg)
        {
            if (v == null)
            {
                final LayoutInflater li = LayoutInflater.from(getContext());
                v = li.inflate(R.layout.app_item, vg, false);
            }
            final AppInfo<String> ai = data.get(i);
            ((ImageView)v.findViewById(R.id.app_icon)).setImageDrawable(ai.icon);
            ((TextView)v.findViewById(R.id.app_label)).setText(ai.label);
            ((TextView)v.findViewById(R.id.app_package)).setText(ai.data);
            final ImageButton ib = (ImageButton)v.findViewById(R.id.remove);
            ib.setFocusable(false);
            ib.setOnClickListener(this);
            ib.setTag(ai);
            return v;
        }
        
        public final void onClick(final View v)
        {
            data.remove(v.getTag());
            notifyDataSetChanged();
        }
    }
    
    private final class LoadTask extends AsyncTask<Void, Integer, Void>
    {
        private int upd = 0;
        
        @Override
        protected final Void doInBackground(final Void... pars)
        {
            final PackageManager pm = getContext().getPackageManager();
            long t = SystemClock.uptimeMillis();
            for (int i = 0; i < data.size(); ++i)
                try
                {
                    final AppInfo<String> ai = data.get(i);
                    final ApplicationInfo appi = pm.getApplicationInfo(ai.data, 0);
                    ai.icon = appi.loadIcon(pm);
                    ai.label = appi.loadLabel(pm).toString();
                    final long nt = SystemClock.uptimeMillis();
                    if (nt - t > 250)
                    {
                        publishProgress(i + 1);
                        t = nt;
                    }
                }
                catch (final Exception ex) { ex.printStackTrace(); }
            publishProgress(data.size());
            return null;
        }
        
        @Override
        protected final void onProgressUpdate(final Integer... vals)
        {
            final int first = lv.getFirstVisiblePosition();
            final int last = Math.min(lv.getLastVisiblePosition() + 1, vals[0]);
            for (int i = Math.max(first, upd); i < last; ++i)
                {
                    final View v = lv.getChildAt(i - first);
                    ((ImageView)v.findViewById(R.id.app_icon)).setImageDrawable(data.get(i).icon);
                    ((TextView)v.findViewById(R.id.app_label)).setText(data.get(i).label);
                }
            upd = vals[0];
        }
        
        @Override
        protected final void onPostExecute(final Void res)
        {
            Collections.sort(data);
            ((BaseAdapter)lv.getAdapter()).notifyDataSetChanged();
        }
    }
}
