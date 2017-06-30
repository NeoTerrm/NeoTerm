package io.neoterm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.neoterm.customize.NeoTermPath;
import io.neoterm.customize.font.FontManager;
import io.neoterm.customize.eks.EksConfig;
import io.neoterm.customize.eks.EksConfigParser;
import io.neoterm.utils.FileUtils;
import io.neoterm.view.eks.ControlButton;
import io.neoterm.view.eks.ExtraButton;
import io.neoterm.view.eks.StatedControlButton;

import static io.neoterm.view.eks.ExtraButton.KEY_ARROW_DOWN;
import static io.neoterm.view.eks.ExtraButton.KEY_ARROW_LEFT;
import static io.neoterm.view.eks.ExtraButton.KEY_ARROW_RIGHT;
import static io.neoterm.view.eks.ExtraButton.KEY_ARROW_UP;
import static io.neoterm.view.eks.ExtraButton.KEY_CTRL;
import static io.neoterm.view.eks.ExtraButton.KEY_END;
import static io.neoterm.view.eks.ExtraButton.KEY_ESC;
import static io.neoterm.view.eks.ExtraButton.KEY_HOME;
import static io.neoterm.view.eks.ExtraButton.KEY_PAGE_DOWN;
import static io.neoterm.view.eks.ExtraButton.KEY_PAGE_UP;
import static io.neoterm.view.eks.ExtraButton.KEY_TAB;

/**
 * A view showing extra keys (such as Escape, Ctrl, Alt) not normally available on an Android soft
 * keyboard.
 */
public final class ExtraKeysView extends LinearLayout {

    public static final StatedControlButton CTRL = new StatedControlButton(KEY_CTRL);
    public static final ControlButton ESC = new ControlButton(KEY_ESC);
    public static final ControlButton TAB = new ControlButton(KEY_TAB);
    public static final ControlButton PAGE_UP = new ControlButton(KEY_PAGE_UP);
    public static final ControlButton PAGE_DOWN = new ControlButton(KEY_PAGE_DOWN);
    public static final ControlButton HOME = new ControlButton(KEY_HOME);
    public static final ControlButton END = new ControlButton(KEY_END);
    public static final ControlButton ARROW_UP = new ControlButton(KEY_ARROW_UP);
    public static final ControlButton ARROW_DOWN = new ControlButton(KEY_ARROW_DOWN);
    public static final ControlButton ARROW_LEFT = new ControlButton(KEY_ARROW_LEFT);
    public static final ControlButton ARROW_RIGHT = new ControlButton(KEY_ARROW_RIGHT);

    public static final String DEFAULT_FILE_CONTENT = "version " + EksConfigParser.PARSER_VERSION + "\n" +
            "program default\n" +
            "define - false\n" +
            "define / false\n" +
            "define | false\n";

    public static final int NORMAL_TEXT_COLOR = 0xFFFFFFFF;
    public static final int SELECTED_TEXT_COLOR = 0xFF80DEEA;

    private List<ExtraButton> builtinExtraKeys;
    private List<ExtraButton> userDefinedExtraKeys;

    private LinearLayout lineOne;
    private LinearLayout lineTwo;


    public ExtraKeysView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.TOP);
        setOrientation(VERTICAL);
        HorizontalScrollView scrollOne = new HorizontalScrollView(context);
        HorizontalScrollView scrollTwo = new HorizontalScrollView(context);
        loadDefaultBuiltinExtraKeys();
        loadDefaultUserDefinedExtraKeys();
        lineOne = initLine(scrollOne);
        lineTwo = initLine(scrollTwo);
        addView(scrollOne);
        addView(scrollTwo);
        updateButtons();
    }

    private LinearLayout initLine(HorizontalScrollView scroll) {
        scroll.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        scroll.setFillViewport(true);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout line = new LinearLayout(getContext());
        line.setGravity(Gravity.START);
        line.setOrientation(LinearLayout.HORIZONTAL);
        scroll.addView(line);
        return line;
    }

    public boolean readControlButton() {
        return CTRL.readState();
    }

    public boolean readAltButton() {
        return false;
    }

    public void addUserDefinedButton(ExtraButton button) {
        addButton(userDefinedExtraKeys, button);
    }

    public void addBuiltinButton(ExtraButton button) {
        addButton(builtinExtraKeys, button);
    }

    private void addButton(List<ExtraButton> buttons, ExtraButton button) {
        if (!buttons.contains(button)) {
            buttons.add(button);
        }
    }

    public void clearUserDefinedButton() {
        userDefinedExtraKeys.clear();
    }

    public void loadDefaultUserDefinedExtraKeys() {
        userDefinedExtraKeys = new ArrayList<>(7);
        File defaultFile = new File(NeoTermPath.EKS_DEFAULT_FILE);
        if (!defaultFile.exists()) {
            generateDefaultFile(defaultFile);
        }

        clearUserDefinedButton();
        try {
            EksConfigParser parser = new EksConfigParser();
            parser.setInput(defaultFile);
            EksConfig config = parser.parse();
            userDefinedExtraKeys.addAll(config.getShortcutKeys());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateDefaultFile(File defaultFile) {
        FileUtils.INSTANCE.writeFile(defaultFile, DEFAULT_FILE_CONTENT.getBytes());
    }

    void loadDefaultBuiltinExtraKeys() {
        builtinExtraKeys = new ArrayList<>(7);
        builtinExtraKeys.clear();
        builtinExtraKeys.add(ESC);
        builtinExtraKeys.add(CTRL);
        builtinExtraKeys.add(TAB);
        builtinExtraKeys.add(ARROW_UP);
        builtinExtraKeys.add(ARROW_DOWN);
        builtinExtraKeys.add(ARROW_LEFT);
        builtinExtraKeys.add(ARROW_RIGHT);
        builtinExtraKeys.add(PAGE_UP);
        builtinExtraKeys.add(PAGE_DOWN);
        builtinExtraKeys.add(HOME);
        builtinExtraKeys.add(END);
    }

    public void updateButtons() {
        lineOne.removeAllViews();
        lineTwo.removeAllViews();
        for (final ExtraButton extraButton : userDefinedExtraKeys) {
            addExtraButton(lineOne, extraButton);
        }
        for (final ExtraButton extraButton : builtinExtraKeys) {
            addExtraButton(lineTwo, extraButton);
        }
    }

    private void addExtraButton(LinearLayout contentView, final ExtraButton extraButton) {
        final Button button;
        if (extraButton instanceof StatedControlButton) {
            StatedControlButton btn = ((StatedControlButton) extraButton);
            button = btn.toggleButton = new ToggleButton(getContext(), null, android.R.attr.buttonBarButtonStyle);
            button.setClickable(true);
            if (btn.initState) {
                btn.toggleButton.setChecked(true);
                btn.toggleButton.setTextColor(SELECTED_TEXT_COLOR);
            }
        } else {
            button = new Button(getContext(), null, android.R.attr.buttonBarButtonStyle);
        }

        button.setTypeface(FontManager.INSTANCE.getDefaultFont());
        button.setText(extraButton.buttonText);
        button.setTextColor(NORMAL_TEXT_COLOR);
        button.setAllCaps(false);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                button.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                View root = getRootView();
                extraButton.onClick(root);
            }
        });
        contentView.addView(button);
    }

}
