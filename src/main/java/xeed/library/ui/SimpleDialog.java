package xeed.library.ui;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;

import androidx.appcompat.app.AlertDialog;

public class SimpleDialog {
    public static AlertDialog create(Context c, int resPos, int resNeg, int resTitle, int resMsg, OnClickListener posLis) {
        return create(c, resPos, resNeg, resTitle, resMsg, posLis, null);
    }

    public static AlertDialog create(Context c, int resPos, int resTitle, int resMsg, OnDismissListener endLis) {
        return create(c, resPos, resTitle, c.getString(resMsg), endLis);
    }

    public static AlertDialog create(Context c, int resPos, int resNeg, int resTitle, int resMsg, OnClickListener posLis, OnDismissListener endLis) {
        AlertDialog.Builder b = new AlertDialog.Builder(c);
        if (resNeg != -1) b.setNegativeButton(resNeg, null);
        AlertDialog ad = b.setPositiveButton(resPos, posLis).setTitle(resTitle).setMessage(resMsg).create();
        ad.setOnDismissListener(endLis);
        return ad;
    }

    public static AlertDialog create(Context c, int resPos, int resTitle, CharSequence txtMsg, OnDismissListener endLis) {
        AlertDialog ad = new AlertDialog.Builder(c).setPositiveButton(resPos, null).setTitle(resTitle).setMessage(txtMsg).create();
        ad.setOnDismissListener(endLis);
        return ad;
    }
}
