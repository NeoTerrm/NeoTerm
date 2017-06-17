package io.neoterm.view.eks;

import android.view.View;

import io.neoterm.view.ExtraKeysView;

/**
 * @author kiva
 */

public class TextButton extends ExtraButton {
    private boolean withEnter = false;

    public TextButton(String text) {
        this(text, false);
    }

    public TextButton(String text, boolean withEnter) {
        this.buttonText = text;
        this.withEnter = withEnter;
    }

    @Override
    public void onClick(View view) {
        sendKey(view, buttonText);
        if (withEnter) {
            sendKey(view, "\n");
        }
    }
}
