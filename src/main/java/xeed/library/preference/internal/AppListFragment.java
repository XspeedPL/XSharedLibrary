package xeed.library.preference.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xeed.library.common.AppInfo;
import xeed.library.common.R;
import xeed.library.common.Utils;
import xeed.library.preference.AppListPreference;
import xeed.library.ui.AppDialog;
import xeed.library.ui.BaseSettings;

public class AppListFragment extends CommonDialogFragment<String, AppListPreference> {
    private final ArrayList<AppInfo<String>> mData = new ArrayList<>();
    private ListView mView = null;

    @Override
    public void onInflate(Context c, AttributeSet as, Bundle b) {
        super.onInflate(c, as, b);
    }

    @Override
    public final void onPrepareDialogBuilder(AlertDialog.Builder b) {
        super.onPrepareDialogBuilder(b);
        mData.clear();
        mData.addAll(Utils.deserialize(mCurrentValue));
        mView = new ListView(b.getContext());
        mView.setAdapter(new AppAdapter());
        new LoadTask(b.getContext().getPackageManager(), mData, mView).execute();
        b.setView(mView);
        b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public final void onClick(DialogInterface di, int i) {
                getPreference().setValue(Utils.serialize(mData));
            }
        });
        b.setNeutralButton(R.string.btn_add_new, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog ad = (AlertDialog) getDialog();
        ad.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View v) {
                AppDialog.create(getContext(), new AppDialog.AppInfoCollector<String>() {
                    @Override
                    public final ArrayList<AppInfo<String>> collectAsync(PackageManager pm, AppDialog.ProgressListener task) {
                        List<ApplicationInfo> pkgs = pm.getInstalledApplications(0);
                        ArrayList<AppInfo<String>> res = new ArrayList<>(pkgs.size());
                        task.postProgress(0, pkgs.size());
                        for (int i = 0; i < pkgs.size(); ++i) {
                            ApplicationInfo appi = pkgs.get(i);
                            AppInfo<String> ai = new AppInfo<>(appi.packageName);
                            ai.icon = appi.loadIcon(pm);
                            ai.label = appi.loadLabel(pm).toString();
                            res.add(ai);
                            task.postProgress(1, i + 1);
                        }
                        return res;
                    }
                }, new AppDialog.AppInfoListener<String>() {
                    @Override
                    public final void onDialogResult(AppInfo<String> ai) {
                        if (!mData.contains(ai)) {
                            mData.add(ai);
                            Collections.sort(mData);
                            ((BaseAdapter) mView.getAdapter()).notifyDataSetChanged();
                        }
                    }
                }, BaseSettings.getDiagTh()).show();
            }
        });
    }

    private final class AppAdapter extends BaseAdapter implements View.OnClickListener {
        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public final AppInfo<String> getItem(int i) {
            return mData.get(i);
        }

        @Override
        public final long getItemId(int i) {
            return getItem(i).hashCode();
        }

        @Override
        public final View getView(int i, View v, ViewGroup vg) {
            if (v == null) {
                LayoutInflater li = LayoutInflater.from(getContext());
                v = li.inflate(R.layout.app_item, vg, false);
            }
            AppInfo<String> ai = getItem(i);
            ((ImageView) v.findViewById(R.id.app_icon)).setImageDrawable(ai.icon);
            ((TextView) v.findViewById(R.id.app_label)).setText(ai.label);
            ((TextView) v.findViewById(R.id.app_package)).setText(ai.data);
            ImageButton ib = v.findViewById(R.id.remove);
            ib.setFocusable(false);
            ib.setOnClickListener(this);
            ib.setTag(ai);
            return v;
        }

        @Override
        public final void onClick(View v) {
            mData.remove(v.getTag());
            notifyDataSetChanged();
        }
    }

    private static final class LoadTask extends AsyncTask<Void, Integer, Void> {
        private int mUpdate = 0;
        private final PackageManager mManager;
        private final List<AppInfo<String>> mData;
        @SuppressLint("StaticFieldLeak")
        private final ListView mView;

        LoadTask(PackageManager manager, List<AppInfo<String>> data, ListView view) {
            mManager = manager;
            mData = data;
            mView = view;
        }

        @Override
        protected final Void doInBackground(Void... pars) {
            long t = SystemClock.uptimeMillis();
            for (int i = 0; i < mData.size(); ++i)
                try {
                    AppInfo<String> ai = mData.get(i);
                    ApplicationInfo appi = mManager.getApplicationInfo(ai.data, 0);
                    ai.icon = appi.loadIcon(mManager);
                    ai.label = appi.loadLabel(mManager).toString();
                    long nt = SystemClock.uptimeMillis();
                    if (nt - t > 250) {
                        publishProgress(i + 1);
                        t = nt;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            publishProgress(mData.size());
            return null;
        }

        @Override
        protected final void onProgressUpdate(Integer... vals) {
            int first = mView.getFirstVisiblePosition();
            int last = Math.min(mView.getLastVisiblePosition() + 1, vals[0]);
            for (int i = Math.max(first, mUpdate); i < last; ++i) {
                View v = mView.getChildAt(i - first);
                ((ImageView) v.findViewById(R.id.app_icon)).setImageDrawable(mData.get(i).icon);
                ((TextView) v.findViewById(R.id.app_label)).setText(mData.get(i).label);
            }
            mUpdate = vals[0];
        }

        @Override
        protected final void onPostExecute(Void res) {
            Collections.sort(mData);
            ((BaseAdapter) mView.getAdapter()).notifyDataSetChanged();
        }
    }
}
