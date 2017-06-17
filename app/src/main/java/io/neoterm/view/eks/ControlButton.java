package io.neoterm.view.eks;

import android.view.View;

import io.neoterm.view.ExtraKeysView;

/**
 * @author kiva
 */

public class ControlButton extends ExtraButton {
    public ControlButton(String text) {
        buttonText = text;
    }

    @Override
    public void onClick(View view) {
        ExtraKeysView.sendKey(view, buttonText);
    }
}
