package xeed.library.ui;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.support.v7.app.AlertDialog;

public class SimpleDialog
{
    public static final AlertDialog create(final Context c, final int resTheme, final int resPos, final int resNeg, final int resTitle, final int resMsg, final OnClickListener posLis)
    {
        return create(c, resTheme, resPos, resNeg, resTitle, resMsg, posLis, null);
    }
    
    public static final AlertDialog create(final Context c, final int resTheme, final int resPos, final int resTitle, final int resMsg, final OnDismissListener endLis)
    {
        return create(c, resTheme, resPos, resTitle, c.getString(resMsg), endLis);
    }
    
    public static final AlertDialog create(final Context c, final int resTheme, final int resPos, final int resNeg, final int resTitle, final int resMsg, final OnClickListener posLis, final OnDismissListener endLis)
    {
        final AlertDialog.Builder b = new AlertDialog.Builder(c, resTheme);
        if (resNeg != -1) b.setNegativeButton(resNeg, null);
        final AlertDialog ad = b.setPositiveButton(resPos, posLis).setTitle(resTitle).setMessage(resMsg).create();
        ad.setOnDismissListener(endLis);
        return ad;
    }
    
    public static final AlertDialog create(final Context c, final int resTheme, final int resPos, final int resTitle, final CharSequence txtMsg, final OnDismissListener endLis)
    {
        final AlertDialog ad = new AlertDialog.Builder(c, resTheme).setPositiveButton(resPos, null).setTitle(resTitle).setMessage(txtMsg).create();
        ad.setOnDismissListener(endLis);
        return ad;
    }
}
