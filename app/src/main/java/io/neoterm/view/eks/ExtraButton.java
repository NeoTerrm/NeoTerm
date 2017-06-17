package io.neoterm.view.eks;

import android.view.KeyEvent;
import android.view.View;

import io.neoterm.R;
import io.neoterm.backend.TerminalSession;
import io.neoterm.view.TerminalView;

/**
 * @author kiva
 */

public abstract class ExtraButton implements View.OnClickListener {
    public static final String KEY_ESC = "Esc";
    public static final String KEY_TAB = "Tab";
    public static final String KEY_CTRL = "Ctrl";

    public String buttonText;

    @Override
    public abstract void onClick(View view);

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
}