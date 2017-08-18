package io.neomodule.layout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import io.neomodule.layout.abs.ImageLoader;
import io.neomodule.layout.attribute.AttributeApply;
import io.neomodule.layout.utils.UniqueId;

public class NeoLayoutInflater {
    private static Configuration CONFIG = new Configuration();

    public static void setImageLoader(ImageLoader il) {
        CONFIG.imageLoader = il;
    }

    public static void setDelegate(View root, Object delegate) {
        LayoutInfo info;
        if (root.getTag() == null || !(root.getTag() instanceof LayoutInfo)) {
            info = new LayoutInfo();
            root.setTag(info);
        } else {
            info = (LayoutInfo) root.getTag();
        }
        info.delegate = delegate;
    }

    @Nullable
    public static View inflateName(Context context, String name) {
        return inflateName(context, name, null);
    }

    @Nullable
    public static View inflateName(Context context, String name, ViewGroup parent) {
        if (name.startsWith("<")) {
            // Assume it's XML
            return NeoLayoutInflater.inflate(context, name, parent);
        } else {
            File savedFile = context.getFileStreamPath(name + ".xml");
            try {
                InputStream fileStream = new FileInputStream(savedFile);
                return NeoLayoutInflater.inflate(context, fileStream, parent);
            } catch (FileNotFoundException e) {
            }

            try {
                InputStream assetStream = context.getAssets().open(name + ".xml");
                return NeoLayoutInflater.inflate(context, assetStream, parent);
            } catch (IOException e) {
            }
            int id = context.getResources().getIdentifier(name, "layout", context.getPackageName());
            if (id > 0) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                return inflater.inflate(id, parent, false);
            }
        }
        return null;
    }

    @Nullable
    public static View inflate(Context context, File xmlPath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(xmlPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return NeoLayoutInflater.inflate(context, inputStream);
    }

    @Nullable
    public static View inflate(Context context, String xml) {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        return NeoLayoutInflater.inflate(context, inputStream);
    }

    @Nullable
    public static View inflate(Context context, String xml, ViewGroup parent) {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        return NeoLayoutInflater.inflate(context, inputStream, parent);
    }

    @Nullable
    public static View inflate(Context context, InputStream inputStream) {
        return inflate(context, inputStream, null);
    }

    @Nullable
    public static View inflate(Context context, InputStream inputStream, ViewGroup parent) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(inputStream);
            try {
                return inflate(context, document.getDocumentElement(), parent);
            } finally {
                inputStream.close();
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends View> T findViewById(View view, String id) {
        int idNum = UniqueId.idFromString(view, id);
        if (idNum == 0) return null;
        return (T) view.findViewById(idNum);
    }

    @Nullable
    private static View inflate(Context context, Node node) {
        return inflate(context, node, null);
    }

    @Nullable
    private static View inflate(Context context, Node node, ViewGroup parent) {
        View mainView = constructView(context, node.getNodeName());
        if (parent != null)
            parent.addView(mainView); // have to add to parent to enable certain layout attrs
        applyAttributes(mainView, getAttributesMap(node), parent);
        if (mainView instanceof ViewGroup && node.hasChildNodes()) {
            parseChildren(context, node, (ViewGroup) mainView);
        }
        return mainView;
    }

    /**
     * 遍历节点里的每一个字节点，并解析成 View
     *
     * @param context  Context
     * @param node     节点
     * @param mainView 父视图
     */
    private static void parseChildren(Context context, Node node, ViewGroup mainView) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() != Node.ELEMENT_NODE) continue;
            inflate(context, currentNode, mainView); // this recursively can call parseChildren
        }
    }

    /**
     * 创建 xml 里定义的节点名的 View
     * 如果节点名里没有带包名，就默认创建 android.widget 包下的实例，如 EditText, TextView
     * 如果节点名里带了包名，就创建对应的实例，如 com.xxx.view.SomeView
     *
     * @param context Context
     * @param name    xml里的节点名
     * @return 对应的 View
     */
    private static View constructView(Context context, String name) {
        try {
            if (!name.contains(".")) {
                name = "android.widget." + name;
            }
            Class<?> clazz = Class.forName(name);
            Constructor<?> constructor = clazz.getConstructor(Context.class);

            return (View) constructor.newInstance(context);
        } catch (ClassNotFoundException
                | NoSuchMethodException
                | InstantiationException
                | InvocationTargetException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 得到一个节点下所有的 xml 属性
     *
     * @param currentNode 节点
     * @return 属性的名字和值
     */
    private static HashMap<String, String> getAttributesMap(Node currentNode) {
        NamedNodeMap attributeMap = currentNode.getAttributes();
        int attributeCount = attributeMap.getLength();
        HashMap<String, String> attributes = new HashMap<>(attributeCount);

        for (int i = 0; i < attributeCount; i++) {
            Node attr = attributeMap.item(i);
            String nodeName = attr.getNodeName();

            // 跳过头部的 namespace
            if (nodeName.startsWith("android:")) {
                nodeName = nodeName.substring(8);
            }
            attributes.put(nodeName, attr.getNodeValue());
        }
        return attributes;
    }

    /**
     * 对一个 View 设置属性
     *
     * @param view   需要设置属性的view
     * @param attrs  所有属性
     * @param parent view的父视图
     */
    private static void applyAttributes(View view, Map<String, String> attrs, ViewGroup parent) {
        CONFIG.createViewRunnablesIfNeeded();
        new AttributeApply(view, attrs, parent).apply(CONFIG);
    }
}
