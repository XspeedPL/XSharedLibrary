package xeed.library.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.*;
import android.widget.*;
import xeed.library.common.AppInfo;
import xeed.library.common.R;

public final class AppListView extends ListView
{
    private final ArrayList<AppInfo<String>> mData = new ArrayList<AppInfo<String>>();
    
    public AppListView(final Context c)
    {
        super(c);
        super.setAdapter(new AppAdapter());
    }
    
    public final void addItem(final AppInfo<String> item)
    {
        if (!mData.contains(item))
        {
            mData.add(item);
            Collections.sort(mData);
            getAdapter().notifyDataSetChanged();
        }
    }
    
    public final BaseAdapter getAdapter() { return (BaseAdapter)super.getAdapter(); }
    
    public final void setAdapter(final ListAdapter adapter) { throw new UnsupportedOperationException(); }
    
    public final void setData(final Collection<AppInfo<String>> src)
    {
        mData.clear();
        mData.addAll(src);
        new LoadTask().execute();
        getAdapter().notifyDataSetChanged();
    }
    
    public final ArrayList<AppInfo<String>> getData()
    {
        return new ArrayList<AppInfo<String>>(mData);
    }
    
    private final class AppAdapter extends BaseAdapter implements View.OnClickListener
    {
        @Override
        public int getCount() { return mData.size(); }

        @Override
        public final Object getItem(final int i) { return mData.get(i); }

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
            final AppInfo<String> ai = mData.get(i);
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
            mData.remove(v.getTag());
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
            for (int i = 0; i < mData.size(); ++i)
                try
                {
                    final AppInfo<String> ai = mData.get(i);
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
            publishProgress(mData.size());
            return null;
        }
        
        @Override
        protected final void onProgressUpdate(final Integer... vals)
        {
            final int first = getFirstVisiblePosition();
            final int last = Math.min(getLastVisiblePosition() + 1, vals[0]);
            for (int i = Math.max(first, upd); i < last; ++i)
                {
                    final View v = getChildAt(i - first);
                    ((ImageView)v.findViewById(R.id.app_icon)).setImageDrawable(mData.get(i).icon);
                    ((TextView)v.findViewById(R.id.app_label)).setText(mData.get(i).label);
                }
            upd = vals[0];
        }
        
        @Override
        protected final void onPostExecute(final Void res)
        {
            Collections.sort(mData);
            getAdapter().notifyDataSetChanged();
        }
    }
}
