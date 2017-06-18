package io.neoterm.view.eks;

import android.view.View;
import android.widget.ToggleButton;

import io.neoterm.view.ExtraKeysView;

/**
 * @author kiva
 */

public class StatedControlButton extends ControlButton {
    public ToggleButton toggleButton;
    public boolean initState;

    public StatedControlButton(String text, boolean initState) {
        super(text);
        this.initState = initState;
    }

    public StatedControlButton(String text) {
        this(text, false);
    }

    @Override
    public void onClick(View view) {
        setStatus(toggleButton.isChecked());
    }

    public void setStatus(boolean status) {
        toggleButton.setChecked(status);
        toggleButton.setTextColor(status ? ExtraKeysView.SELECTED_TEXT_COLOR : ExtraKeysView.NORMAL_TEXT_COLOR);
    }

    public boolean readState() {
        if (toggleButton.isPressed()) return true;
        boolean result = toggleButton.isChecked();
        if (result) {
            toggleButton.setChecked(false);
            toggleButton.setTextColor(ExtraKeysView.NORMAL_TEXT_COLOR);
        }
        return result;
    }
}
