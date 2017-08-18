package io.neomodule.layout.attribute;

import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import io.neomodule.layout.abs.ViewAttributeRunnable;

/**
 * @author kiva
 */

class InputTypeApply implements ViewAttributeRunnable {
    @Override
    public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
        if (view instanceof TextView) {
            int inputType = 0;
            switch (value) {
                case "textEmailAddress":
                    inputType |= InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                    break;
                case "number":
                    inputType |= InputType.TYPE_CLASS_NUMBER;
                    break;
                case "phone":
                    inputType |= InputType.TYPE_CLASS_PHONE;
                    break;
            }
            if (inputType > 0) ((TextView) view).setInputType(inputType);
        }
    }
}
