package xeed.library.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import xeed.library.common.AppInfo;
import xeed.library.common.R;

public final class AppDialog<T> extends AlertDialog.Builder implements OnItemClickListener {
    private final AppInfoAdapter<T> mAdapter;
    private final AppInfoListener<T> mListener;
    private AlertDialog mDialog = null;

    @SuppressLint("InflateParams")
    private AppDialog(Context c, AppInfoCollector<T> aic, AppInfoListener<T> ail, int th) {
        super(c, th);
        setTitle(R.string.diag_pick_app);
        setNegativeButton(android.R.string.cancel, null);
        View v = LayoutInflater.from(c).inflate(R.layout.srchdialog, null, false);
        setView(v);
        mListener = ail;
        mAdapter = new AppInfoAdapter<>((ProgressBar) v.findViewById(android.R.id.progress), aic);
        mAdapter.registerView((SearchView) v.findViewById(R.id.search));
        ListView lv = v.findViewById(android.R.id.list);
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(this);
    }

    @Override
    public final AlertDialog create() {
        return mDialog = super.create();
    }

    public static <T> AlertDialog create(Context c, AppInfoCollector<T> aic, AppInfoListener<T> ail, int theme) {
        return new AppDialog<>(c, aic, ail, theme).create();
    }

    @Override
    public final void onItemClick(AdapterView<?> av, View v, int i, long id) {
        mDialog.dismiss();
        mListener.onDialogResult(mAdapter.getInfo(i));
    }

    public interface AppInfoListener<T> {
        void onDialogResult(AppInfo<T> info);
    }

    public interface AppInfoCollector<T> {
        ArrayList<AppInfo<T>> collectAsync(PackageManager pm, ProgressListener task);
    }

    public interface ProgressListener {
        void postProgress(Integer... vals);
    }

    public static final class AppInfoAdapter<T> extends FilteredAdapter<AppInfo<T>> {
        private final AppInfoCollector<T> mCollector;
        private final ProgressBar mBar;
        private final PackageManager mPkg;
        private boolean mReady = false;

        final AppInfo<T> getInfo(int pos) {
            return mFiltered.get(pos);
        }

        @Override
        protected final String getItemDescLower(AppInfo<T> item) {
            return (item.label + " " + item.desc).toLowerCase(Locale.getDefault());
        }

        @Override
        protected final boolean isDataReady() {
            return mReady;
        }

        protected final class Task extends AsyncTask<Void, Integer, ArrayList<AppInfo<T>>> implements ProgressListener {
            @Override
            protected final ArrayList<AppInfo<T>> doInBackground(Void... params) {
                mReady = false;
                return mCollector.collectAsync(mPkg, this);
            }

            @Override
            public final void postProgress(Integer... vals) {
                publishProgress(vals);
            }

            @Override
            protected final void onProgressUpdate(Integer... args) {
                if (args[0] == 0) {
                    mBar.setMax(args[1]);
                    mBar.setProgress(0);
                } else if (args[0] == 1) mBar.setProgress(args[1]);
            }

            @Override
            protected final void onPostExecute(ArrayList<AppInfo<T>> res) {
                mBar.setVisibility(View.GONE);
                View v = (View) mBar.getParent();
                v.findViewById(android.R.id.list).setVisibility(View.VISIBLE);
                mData.clear();
                Collections.sort(res);
                mData = res;
                mReady = true;
                getFilter().filter("");
            }
        }

        AppInfoAdapter(ProgressBar pb, AppInfoCollector<T> aic) {
            mBar = pb;
            mPkg = pb.getContext().getPackageManager();
            mCollector = aic;
            new Task().execute();
        }

        @Override
        public final long getItemId(int pos) {
            return getItem(pos).hashCode();
        }

        @Override
        public final View getView(int pos, View v, ViewGroup vg) {
            if (v == null) {
                LayoutInflater li = LayoutInflater.from(mBar.getContext());
                v = li.inflate(R.layout.app_item, vg, false);
            }
            AppInfo<?> ai = mFiltered.get(pos);
            ((ImageView) v.findViewById(R.id.app_icon)).setImageDrawable(ai.icon);
            ((TextView) v.findViewById(R.id.app_label)).setText(ai.label);
            ((TextView) v.findViewById(R.id.app_package)).setText(ai.desc);
            v.findViewById(R.id.remove).setVisibility(View.GONE);
            return v;
        }
    }
}
