package io.neoterm.view.eks;

import android.view.View;

/**
 * @author kiva
 */

public abstract class ExtraButton implements View.OnClickListener {
    public String buttonText;

    @Override
    public abstract void onClick(View view);
}