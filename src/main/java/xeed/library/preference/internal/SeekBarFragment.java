package xeed.library.preference.internal;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import xeed.library.common.R;
import xeed.library.common.Utils;
import xeed.library.preference.SeekBarPreference;
import xeed.library.view.TextSeekBar;

public class SeekBarFragment extends CommonDialogFragment<Integer, SeekBarPreference> implements SeekBar.OnSeekBarChangeListener {
    FrameLayout mView = null;
    TextSeekBar mBar = null;

    @Override
    public void onInflate(Context c, AttributeSet as, Bundle b) {
        super.onInflate(c, as, b);
        mView = new FrameLayout(c);
        mBar = new TextSeekBar(c, as);
        mBar.setOnSeekBarChangeListener(this);
        mBar.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
        mBar.setTextSize(Utils.spPx(c, 20));
        int px = (int) (mBar.getTextSize() * 3 / 2);
        mBar.setPadding(px, 0, px, 0);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        px = (int) Utils.dpPx(c, 10);
        lp.setMargins(px, px * 2, px, px);
        mBar.setLayoutParams(lp);
        mView.addView(mBar);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder b) {
        SeekBarPreference pref = getPreference();
        b.setTitle(pref.getSummary());
        mBar.setProgress(mCurrentValue / pref.getMultiplier());
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
