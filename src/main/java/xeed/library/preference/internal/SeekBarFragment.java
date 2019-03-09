package xeed.library.preference.internal;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;
import xeed.library.common.R;
import xeed.library.common.Utils;
import xeed.library.preference.SeekBarPreference;
import xeed.library.view.TextSeekBar;

public class SeekBarFragment extends CommonDialogFragment<Integer, SeekBarPreference> implements SeekBar.OnSeekBarChangeListener {
    FrameLayout mView = null;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        Context c = getContext();
        TextSeekBar bar = getPreference().getSeekBar();
        mView = new FrameLayout(c);
        bar.setOnSeekBarChangeListener(this);
        bar.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
        bar.setTextSize(Utils.spPx(c, 20));
        int px = (int) (bar.getTextSize() * 3 / 2);
        bar.setPadding(px, 0, px, 0);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        px = (int) Utils.dpPx(c, 10);
        lp.setMargins(px, px * 2, px, px);
        bar.setLayoutParams(lp);
        mView.addView(bar);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder b) {
        super.onPrepareDialogBuilder(b);

        SeekBarPreference pref = getPreference();
        b.setTitle(pref.getSummary());
        getPreference().getSeekBar().setProgress(mCurrentValue / pref.getMultiplier());
        if (mView.getParent() != null) ((ViewGroup) mView.getParent()).removeView(mView);
        b.setView(mView);
    }

    @Override
    public final void onProgressChanged(SeekBar sb, int value, boolean user) {
        mCurrentValue = value * getPreference().getMultiplier();
    }

    @Override
    public final void onStartTrackingTouch(SeekBar seek) {
    }

    @Override
    public final void onStopTrackingTouch(SeekBar seek) {
    }
}
