package io.neoterm.frontend.completion;

/**
 * @author Kiva
 * @version 1.0
 */
public interface OnAutoCompleteListener {
    void onAutoComplete(String newText);

    void onKeyCode(int keyCode, int keyMod);

    void onCleanUp();
}
