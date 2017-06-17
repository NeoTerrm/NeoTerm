package io.neoterm.view.eks;

import android.view.View;
import android.widget.ToggleButton;

import io.neoterm.view.ExtraKeysView;

/**
 * @author kiva
 */

public class StatedControlButton extends ControlButton {
    public ToggleButton toggleButton;

    public StatedControlButton(String text) {
        super(text);
    }

    @Override
    public void onClick(View view) {
        toggleButton.setChecked(toggleButton.isChecked());
        toggleButton.setTextColor(toggleButton.isChecked() ? 0xFF80DEEA : ExtraKeysView.NORMAL_TEXT_COLOR);
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
