package io.neoterm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.neoterm.R;
import io.neoterm.backend.TerminalSession;
import io.neoterm.customize.NeoTermPath;
import io.neoterm.customize.font.FontManager;
import io.neoterm.customize.shortcut.ShortcutConfig;
import io.neoterm.customize.shortcut.ShortcutConfigParser;
import io.neoterm.preference.NeoTermPreference;
import io.neoterm.utils.FileUtils;
import io.neoterm.view.eks.ControlButton;
import io.neoterm.view.eks.ExtraButton;
import io.neoterm.view.eks.StatedControlButton;

/**
 * A view showing extra keys (such as Escape, Ctrl, Alt) not normally available on an Android soft
 * keyboard.
 */
public final class ExtraKeysView extends GridLayout {
    public static final String KEY_ESC = "Esc";
    public static final String KEY_TAB = "Tab";
    public static final String KEY_CTRL = "Ctrl";

    public static final ControlButton ESC = new ControlButton(KEY_ESC);
    public static final ControlButton TAB = new ControlButton(KEY_TAB);
    public static final StatedControlButton CTRL = new StatedControlButton(KEY_CTRL);

    public static final ControlButton ARROW_UP = new ControlButton("▲");
    public static final ControlButton ARROW_DOWN = new ControlButton("▼");
    public static final ControlButton ARROW_LEFT = new ControlButton("◀");
    public static final ControlButton ARROW_RIGHT = new ControlButton("▶");

    public static final String DEFAULT_FILE_CONTENT = "version " + ShortcutConfigParser.PARSER_VERSION + "\n" +
            "program default\n" +
            "define - false\n" +
            "define / false\n" +
            "define | false\n";

    public static final int NORMAL_TEXT_COLOR = 0xFFFFFFFF;

    private List<ExtraButton> builtinExtraKeys;
    private List<ExtraButton> userDefinedExtraKeys;

    public ExtraKeysView(Context context, AttributeSet attrs) {
        super(context, attrs);
        builtinExtraKeys = new ArrayList<>(7);
        userDefinedExtraKeys = new ArrayList<>(7);
        loadDefaultBuiltinExtraKeys();
        loadDefaultUserDefinedExtraKeys();
        updateButtons();
    }

    public static void sendKey(View view, String keyName) {
        int keyCode = 0;
        String chars = null;
        switch (keyName) {
            case KEY_ESC:
                keyCode = KeyEvent.KEYCODE_ESCAPE;
                break;
            case KEY_TAB:
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
        return false;
    }

    public void addExternalButton(ExtraButton button) {
        userDefinedExtraKeys.add(button);
    }

    public void clearExternalButton() {
        userDefinedExtraKeys.clear();
    }

    public void loadDefaultUserDefinedExtraKeys() {
        File defaultFile = new File(NeoTermPath.EKS_DEFAULT_FILE);
        if (!defaultFile.exists()) {
            generateDefaultFile(defaultFile);
        }

        clearExternalButton();
        try {
            ShortcutConfigParser parser = new ShortcutConfigParser();
            parser.setInput(defaultFile);
            ShortcutConfig config = parser.parse();
            userDefinedExtraKeys.addAll(config.getShortcutKeys());
        } catch (Exception ignore) {
        }
    }

    private void generateDefaultFile(File defaultFile) {
        FileUtils.INSTANCE.writeFile(defaultFile, DEFAULT_FILE_CONTENT.getBytes());
    }

    void loadDefaultBuiltinExtraKeys() {
        builtinExtraKeys.clear();
        builtinExtraKeys.add(ESC);
        builtinExtraKeys.add(CTRL);
        builtinExtraKeys.add(TAB);
        builtinExtraKeys.add(ARROW_LEFT);
        builtinExtraKeys.add(ARROW_RIGHT);
        builtinExtraKeys.add(ARROW_UP);
        builtinExtraKeys.add(ARROW_DOWN);
    }

    public void updateButtons() {
        removeAllViews();
        List[] buttons = new List[]{userDefinedExtraKeys, builtinExtraKeys};

        setRowCount(buttons[0].size() == 0 ? 1 : 2);
        setColumnCount(buttons[1].size());

        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].size(); col++) {
                final ExtraButton extraButton = (ExtraButton) buttons[row].get(col);

                Button button;
                if (extraButton instanceof StatedControlButton) {
                    StatedControlButton btn = ((StatedControlButton) extraButton);
                    button = btn.toggleButton = new ToggleButton(getContext(), null, android.R.attr.buttonBarButtonStyle);
                    button.setClickable(true);
                } else {
                    button = new Button(getContext(), null, android.R.attr.buttonBarButtonStyle);
                }

                button.setTypeface(FontManager.INSTANCE.getDefaultFont());
                button.setText(extraButton.buttonText);
                button.setTextColor(NORMAL_TEXT_COLOR);
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

                float weight = 1.f;
                if (NeoTermPreference.INSTANCE.loadBoolean(R.string.key_ui_wide_char_weigh_explicit, false)) {
                    weight = "▲▼◀▶".contains(extraButton.buttonText) ? 0.7f : 1.f;
                }
                param.columnSpec = GridLayout.spec(col, GridLayout.FILL, weight);
                param.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1.f);
                button.setLayoutParams(param);

                addView(button);
            }
        }
    }

}
