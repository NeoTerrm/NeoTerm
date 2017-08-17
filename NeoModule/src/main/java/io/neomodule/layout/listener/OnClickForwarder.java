package io.neomodule.layout.listener;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.neomodule.layout.LayoutInfo;

/**
 * @author kiva
 */

public class OnClickForwarder implements View.OnClickListener {
    private ViewGroup parent;
    private String methodName;

    public OnClickForwarder(ViewGroup parent, String methodName) {
        this.parent = parent;
        this.methodName = methodName;
    }

    @Override
    public void onClick(View view) {
        ViewGroup root = parent;
        LayoutInfo info = null;
        while (root != null && (root.getParent() instanceof ViewGroup)) {
            if (root.getTag() != null && root.getTag() instanceof LayoutInfo) {
                info = (LayoutInfo) root.getTag();
                if (info.delegate != null) break;
            }
            root = (ViewGroup) root.getParent();
        }
        if (info != null && info.delegate != null) {
            final Object delegate = info.delegate;
            invokeMethod(delegate, methodName, false, view);
        } else {
            Log.e("DynamicLayoutInflater", "Unable to find valid delegate for click named " + methodName);
        }
    }

    private void invokeMethod(Object delegate, final String methodName, boolean withView, View view) {
        Object[] args = null;
        String finalMethod = methodName;
        if (methodName.endsWith(")")) {
            String[] parts = methodName.split("[(]", 2);
            finalMethod = parts[0];
            try {
                String argText = parts[1].replace("&quot;", "\"");
                JSONArray arr = new JSONArray("[" + argText.substring(0, argText.length() - 1) + "]");
                args = new Object[arr.length()];
                for (int i = 0; i < arr.length(); i++) {
                    args[i] = arr.get(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (withView) {
            args = new Object[1];
            args[0] = view;
        }
        Class<?> klass = delegate.getClass();
        try {

            Class<?>[] argClasses = null;
            if (args != null && args.length > 0) {
                argClasses = new Class[args.length];
                if (withView) {
                    argClasses[0] = View.class;
                } else {
                    for (int i = 0; i < args.length; i++) {
                        Class<?> argClass = args[i].getClass();
                        if (argClass == Integer.class)
                            argClass = int.class; // Nobody uses Integer...
                        argClasses[i] = argClass;
                    }
                }
            }
            Method method = klass.getMethod(finalMethod, argClasses);
            method.invoke(delegate, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            if (!withView && !methodName.endsWith(")")) {
                invokeMethod(delegate, methodName, true, view);
            }
        }
    }
}
