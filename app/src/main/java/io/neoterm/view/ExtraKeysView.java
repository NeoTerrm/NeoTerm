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
import android.widget.ScrollView;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.neoterm.customize.NeoTermPath;
import io.neoterm.customize.shortcut.ShortcutConfig;
import io.neoterm.customize.shortcut.ShortcutConfigParser;
import io.neoterm.utils.FileUtils;
import io.neoterm.view.eks.ControlButton;
import io.neoterm.view.eks.ExtraButton;
import io.neoterm.view.eks.StatedControlButton;

import static io.neoterm.view.eks.ExtraButton.KEY_CTRL;
import static io.neoterm.view.eks.ExtraButton.KEY_ESC;
import static io.neoterm.view.eks.ExtraButton.KEY_TAB;

/**
 * A view showing extra keys (such as Escape, Ctrl, Alt) not normally available on an Android soft
 * keyboard.
 */
public final class ExtraKeysView extends HorizontalScrollView {

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

    private LinearLayout contentView;

    public ExtraKeysView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFillViewport(true);
        contentView = new LinearLayout(context);
        contentView.setGravity(Gravity.CENTER);
        contentView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        contentView.setOrientation(LinearLayout.HORIZONTAL);
        contentView.setGravity(Gravity.LEFT);
        addView(contentView);
        builtinExtraKeys = new ArrayList<>(7);
        userDefinedExtraKeys = new ArrayList<>(7);
        loadDefaultBuiltinExtraKeys();
        loadDefaultUserDefinedExtraKeys();
        updateButtons();
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
        builtinExtraKeys.add(ARROW_UP);
        builtinExtraKeys.add(ARROW_DOWN);
        builtinExtraKeys.add(ARROW_LEFT);
        builtinExtraKeys.add(ARROW_RIGHT);
    }

    public void updateButtons() {
        contentView.removeAllViews();
        for (final ExtraButton extraButton : builtinExtraKeys) {
            addExtraButton(extraButton);
        }
        for (final ExtraButton extraButton : userDefinedExtraKeys) {
            addExtraButton(extraButton);
        }
    }

    private void addExtraButton(final ExtraButton extraButton) {
        final Button button;
        if (extraButton instanceof StatedControlButton) {
            StatedControlButton btn = ((StatedControlButton) extraButton);
            button = btn.toggleButton = new ToggleButton(getContext(), null, android.R.attr.buttonBarButtonStyle);
            button.setClickable(true);
        } else {
            button = new Button(getContext(), null, android.R.attr.buttonBarButtonStyle);
        }
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
