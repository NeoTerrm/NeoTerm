package io.neoterm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import io.neoterm.R;
import io.neoterm.backend.TerminalSession;

/**
 * A view showing extra keys (such as Escape, Ctrl, Alt) not normally available on an Android soft
 * keyboard.
 */
public final class ExtraKeysView extends GridLayout {
    public static abstract class ExtraButton implements OnClickListener {
        public String buttonText;

        @Override
        public abstract void onClick(View view);
    }

    public static class ControlButton extends ExtraButton {
        public ControlButton(String text) {
            buttonText = text;
        }

        @Override
        public void onClick(View view) {
            ExtraKeysView.sendKey(view, buttonText);
        }
    }

    public static class TextButton extends ExtraButton {
        boolean withEnter = false;

        public TextButton(String text) {
            this(text, false);
        }

        public TextButton(String text, boolean withEnter) {
            this.buttonText = text;
            this.withEnter = withEnter;
        }

        @Override
        public void onClick(View view) {
            ExtraKeysView.sendKey(view, buttonText);
            if (withEnter) {
                ExtraKeysView.sendKey(view, "\n");
            }
        }
    }

    public static class StatedControlButton extends ControlButton {
        public ToggleButton toggleButton;

        public StatedControlButton(String text) {
            super(text);
        }

        @Override
        public void onClick(View view) {
            toggleButton.setChecked(toggleButton.isChecked());
            toggleButton.setTextColor(toggleButton.isChecked() ? 0xFF80DEEA : TEXT_COLOR);
        }

        public boolean readState() {
            if (toggleButton.isPressed()) return true;
            boolean result = toggleButton.isChecked();
            if (result) {
                toggleButton.setChecked(false);
                toggleButton.setTextColor(TEXT_COLOR);
            }
            return result;
        }
    }

    public static final ControlButton ESC = new ControlButton("ESC");
    public static final ControlButton TAB = new ControlButton("TAB");
    public static final StatedControlButton CTRL = new StatedControlButton("CTRL");
    public static final StatedControlButton ALT = new StatedControlButton("ALT");
    public static final StatedControlButton FN = new StatedControlButton("FN");

    public static final ControlButton ARROW_UP = new ControlButton("▲");
    public static final ControlButton ARROW_DOWN = new ControlButton("▼");
    public static final ControlButton ARROW_LEFT = new ControlButton("◀");
    public static final ControlButton ARROW_RIGHT = new ControlButton("▶");

    public static final TextButton HORIZONTAL = new TextButton("-");
    public static final TextButton SLASH = new TextButton("/");
    public static final TextButton PIPE = new TextButton("|");

    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private List<ExtraButton> extraButtons;
    private List<ExtraButton> externalButtons;

    public ExtraKeysView(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
            externalButtons = new ArrayList<>(3);
            extraButtons = new ArrayList<>();
            resetExternalButtons();
            updateButtons();
        } catch (Throwable e) {
            Log.e("NeoTerm", e.toString());
            throw e;
        }
    }

    public static void sendKey(View view, String keyName) {
        int keyCode = 0;
        String chars = null;
        switch (keyName) {
            case "ESC":
                keyCode = KeyEvent.KEYCODE_ESCAPE;
                break;
            case "TAB":
                keyCode = KeyEvent.KEYCODE_TAB;
                break;
            case "▲":
                keyCode = KeyEvent.KEYCODE_DPAD_UP;
                break;
            case "◀":
                keyCode = KeyEvent.KEYCODE_DPAD_LEFT;
                break;
            case "▶":
                keyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
                break;
            case "▼":
                keyCode = KeyEvent.KEYCODE_DPAD_DOWN;
                break;
            case "―":
                chars = "-";
                break;
            default:
                chars = keyName;
        }

        if (keyCode > 0) {
            view.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
            view.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
        } else {
            TerminalView terminalView = (TerminalView) view.findViewById(R.id.terminal_view);
            TerminalSession session = terminalView.getCurrentSession();
            if (session != null) session.write(chars);
        }
    }

    public boolean readControlButton() {
        return CTRL.readState();
    }

    public boolean readAltButton() {
        return ALT.readState();
    }

    public boolean readFnButton() {
        return FN.readState();
    }

    public void addExternalButton(ExtraButton button) {
        externalButtons.add(button);
    }

    public void removeExternalButton(ExtraButton button) {
        externalButtons.remove(button);
    }

    public void clearExternalButton() {
        externalButtons.clear();
    }

    public void resetExternalButtons() {
        clearExternalButton();
        externalButtons.add(ALT);
        externalButtons.add(HORIZONTAL);
        externalButtons.add(SLASH);
        externalButtons.add(PIPE);
    }

    void loadDefaultButtons(List<ExtraButton> buttons) {
        buttons.add(ESC);
        buttons.add(CTRL);
        buttons.add(TAB);
    }

    void loadExternalButtons(List<ExtraButton> buttons) {
        buttons.addAll(externalButtons);
    }

    public void updateButtons() {
        removeAllViews();

        extraButtons.clear();
        loadDefaultButtons(extraButtons);
        loadExternalButtons(extraButtons);

        setRowCount(1);
        setColumnCount(extraButtons.size());

        for (int col = 0; col < extraButtons.size(); col++) {
            final ExtraButton extraButton = extraButtons.get(col);

            Button button;
            if (extraButton instanceof StatedControlButton) {
                StatedControlButton btn = ((StatedControlButton) extraButton);
                button = btn.toggleButton = new ToggleButton(getContext(), null, android.R.attr.buttonBarButtonStyle);
                button.setClickable(true);
            } else {
                button = new Button(getContext(), null, android.R.attr.buttonBarButtonStyle);
            }

            button.setText(extraButton.buttonText);
            button.setTextColor(TEXT_COLOR);
            button.setAllCaps(false);

            final Button finalButton = button;
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    finalButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    View root = getRootView();
                    extraButton.onClick(root);
                }
            });

            LayoutParams param = new LayoutParams();
            param.height = param.width = 0;
            param.rightMargin = param.topMargin = 0;
            param.setGravity(Gravity.START);
            float weight = "▲▼◀▶".contains(extraButton.buttonText) ? 0.7f : 1.f;
            param.columnSpec = GridLayout.spec(col, GridLayout.FILL, weight);
            param.rowSpec = GridLayout.spec(0, GridLayout.FILL, 1.f);
            button.setLayoutParams(param);

            addView(button);
        }
    }

}
