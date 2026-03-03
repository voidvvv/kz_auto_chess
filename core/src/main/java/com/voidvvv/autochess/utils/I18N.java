package com.voidvvv.autochess.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Locale;

/**
 * 国际化工具类
 * 提供多语言支持，用于替换硬编码的中文字符串
 */
public class I18N {
    private static I18NBundle bundle;
    private static Locale currentLocale = Locale.ENGLISH; // 默认使用英文

    // 定义可用的语言
    public static final Locale CHINESE = Locale.SIMPLIFIED_CHINESE;
    public static final Locale ENGLISH = Locale.ENGLISH;
    public static final Locale JAPANESE = Locale.JAPANESE;

    private static boolean initialized = false;

    /**
     * 初始化i18n系统
     */
    public static void init() {
        init(Locale.getDefault());
    }

    /**
     * 使用指定语言初始化i18n系统
     * @param locale 语言环境
     */
    public static void init(Locale locale) {
        try {
            currentLocale = locale;

            // 尝试从不同路径加载资源文件
            FileHandle bundleFile = Gdx.files.internal("i18n/i18n");

            if (!bundleFile.exists()) {
                Gdx.app.error("I18N", "Bundle file not found: " + bundleFile.path());
                // 创建默认的bundle作为后备
                bundle = I18NBundle.createBundle(bundleFile, locale);
            } else {
                bundle = I18NBundle.createBundle(bundleFile);
            }

            initialized = true;
            Gdx.app.log("I18N", "I18N initialized with locale: " + locale + ", bundle size: " + bundle.getLocale().toString());
        } catch (Exception e) {
            Gdx.app.error("I18N", "Failed to initialize I18N: " + e.getMessage(), e);
            // 创建空bundle作为后备
            bundle = new I18NBundle();
            initialized = true;
        }
    }

    /**
     * 切换语言
     * @param locale 目标语言
     */
    public static void setLocale(Locale locale) {
        if (!initialized) {
            init(locale);
            return;
        }

        if (!locale.equals(currentLocale)) {
            try {
                FileHandle bundleFile = Gdx.files.internal("i18n/i18n");
                bundle = I18NBundle.createBundle(bundleFile, locale);
                currentLocale = locale;
                Gdx.app.log("I18N", "Switched to locale: " + locale);
            } catch (Exception e) {
                Gdx.app.error("I18N", "Failed to switch locale: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 获取当前语言
     * @return 当前语言环境
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * 获取国际化字符串
     * @param key 键名
     * @return 国际化字符串
     */
    public static String get(String key) {
        return get(key, key); // 默认返回key本身
    }

    /**
     * 获取国际化字符串，如果找不到则返回默认值
     * @param key 键名
     * @param defaultValue 默认值
     * @return 国际化字符串
     */
    public static String get(String key, String defaultValue) {
        if (!initialized) {
            init();
        }

        try {
            return bundle.get(key);
        } catch (Exception e) {
            Gdx.app.debug("I18N", "Key not found: " + key + ", using default: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * 格式化国际化字符串
     * @param key 键名
     * @param args 参数
     * @return 格式化后的字符串
     */
    public static String format(String key, Object... args) {
        if (!initialized) {
            init();
        }

        try {
            return bundle.format(key, args);
        } catch (Exception e) {
            Gdx.app.debug("I18N", "Format failed for key: " + key);
            return key;
        }
    }

    /**
     * 检查是否已初始化
     * @return 是否已初始化
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
