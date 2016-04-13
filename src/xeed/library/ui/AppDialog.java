package xeed.library.ui;

import java.util.*;
import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.*;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import xeed.library.common.AppInfo;
import xeed.library.common.R;

public final class AppDialog<T> extends AlertDialog.Builder implements OnItemClickListener
{
	private final AppInfoAdapter<T> mAdapter;
	private final AppInfoListener<T> mListener;
	private AlertDialog mDialog = null;
	
	@SuppressLint("InflateParams")
    private AppDialog(final Context c, final AppInfoCollector<T> aic, final AppInfoListener<T> ail, final int th)
	{
	    super(c, th);
		setTitle(R.string.diag_pick_app);
		setNegativeButton(android.R.string.cancel, null);
		final View v = LayoutInflater.from(c).inflate(R.layout.srchdialog, null, false);
		setView(v);
		mListener = ail;
		mAdapter = new AppInfoAdapter<T>((ProgressBar)v.findViewById(android.R.id.progress), aic);
		mAdapter.registerView((SearchView)v.findViewById(R.id.search));
		final ListView lv = (ListView)v.findViewById(android.R.id.list);
		lv.setAdapter(mAdapter);
		lv.setOnItemClickListener(this);
	}
	
	@Override
	public final AlertDialog create()
	{
	    return mDialog = super.create();
	}
	
	public static final <T> AlertDialog create(final Context c, final AppInfoCollector<T> aic, final AppInfoListener<T> ail, final int theme)
	{
	    return new AppDialog<T>(c, aic, ail, theme).create();
	}

	@Override
    public final void onItemClick(final AdapterView<?> av, final View v, final int i, final long id)
    {
	    mDialog.dismiss();
        mListener.onDialogResult(mAdapter.getInfo(i));
    }
	
	public interface AppInfoListener<T>
	{
		public void onDialogResult(final AppInfo<T> info);
	}
	
	public interface AppInfoCollector<T>
	{
	    public ArrayList<AppInfo<T>> collectAsync(final PackageManager pm, final ProgressListener task);
	}
	
	public interface ProgressListener
	{
	    public void postProgress(final Integer... vals);
	}
	
	public static final class AppInfoAdapter<T> extends FilteredAdapter<AppInfo<T>>
	{
	    private final AppInfoCollector<T> mCollector;
		private final ProgressBar mBar;
		private final PackageManager mPkg;
		private boolean mReady = false;
		
		public final AppInfo<T> getInfo(final int pos)
		{
			return mFiltered.get(pos);
		}
		
		protected final String getItemDescLower(final AppInfo<T> item)
		{
		    return (item.label + " " + item.desc).toLowerCase(Locale.getDefault());
		}
		
		protected final boolean isDataReady() { return mReady; }
		
		protected final class Task extends AsyncTask<Void, Integer, ArrayList<AppInfo<T>>> implements ProgressListener
		{
			@Override
			protected final ArrayList<AppInfo<T>> doInBackground(final Void... params)
			{
			    mReady = false;
			    return mCollector.collectAsync(mPkg, this);
			}

		     @Override
		    public final void postProgress(final Integer... vals)
		    {
		        publishProgress(vals);
		    }
			
			@Override
			protected final void onProgressUpdate(final Integer... args)
			{
				if (args[0] == 0)
				{
				    mBar.setMax(args[1]);
				    mBar.setProgress(0);
				}
				else if (args[0] == 1) mBar.setProgress(args[1]);
			}
			
			@Override
			protected final void onPostExecute(final ArrayList<AppInfo<T>> res)
			{
			    mBar.setVisibility(View.GONE);
			    final View v = (View)mBar.getParent();
			    v.findViewById(android.R.id.list).setVisibility(View.VISIBLE);
			    mData.clear();
				Collections.sort(res);
				mData = res;
				mReady = true;
				getFilter().filter("");
			}
		}
		
		protected AppInfoAdapter(final ProgressBar pb, final AppInfoCollector<T> aic)
		{
		    mBar = pb;
			mPkg = pb.getContext().getPackageManager();
			mCollector = aic;
			new Task().execute(new Void[0]);
		}

		@Override
		public final long getItemId(final int pos) { return getItem(pos).hashCode(); }

		@Override
		public final View getView(final int pos, View v, final ViewGroup vg)
		{
			if (v == null)
			{
			    final LayoutInflater li = LayoutInflater.from(mBar.getContext());
			    v = li.inflate(R.layout.app_item, vg, false);
			}
	        final AppInfo<?> ai = mFiltered.get(pos);
			((ImageView)v.findViewById(R.id.app_icon)).setImageDrawable(ai.icon);
			((TextView)v.findViewById(R.id.app_label)).setText(ai.label);
			((TextView)v.findViewById(R.id.app_package)).setText(ai.desc);
			v.findViewById(R.id.remove).setVisibility(View.GONE);
			return v;
		}
	}
}
