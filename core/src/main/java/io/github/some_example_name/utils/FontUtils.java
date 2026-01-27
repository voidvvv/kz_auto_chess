package io.github.some_example_name.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * 字体工具类，用于加载和管理字体
 */
public class FontUtils {
    private static BitmapFont defaultFont;
    private static BitmapFont largeFont;
    private static BitmapFont smallFont;
    
    private static final String[] FONT_PATHS = {
        "C:/Windows/Fonts/msyh.ttc",  // 微软雅黑（优先）
        "C:/Windows/Fonts/simhei.ttf", // 备用字体：黑体
        "C:/Windows/Fonts/simsun.ttc", // 备用字体：宋体
        "C:/Windows/Fonts/simkai.ttf"  // 备用字体：楷体
    };
    
    // 生成包含常用中文字符的字符串
    private static String generateChineseCharacters() {
        StringBuilder sb = new StringBuilder();
        // 基本ASCII字符
        sb.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
        // 常用标点符号
        sb.append("，。！？；：、\"\"''（）【】《》·…—～·");
        // 游戏界面常用字（确保这些字一定包含）
        sb.append("自走棋开始游戏选择关卡金币等级商店刷新购买返回");
        sb.append("战士法师射手刺客坦克");
        sb.append("新手精英高级传奇神话");
        sb.append("基础强化单位");
        sb.append("费用");
        // 常用汉字（覆盖常用字范围，但不过多）
        // 添加一些常用汉字以确保覆盖
        for (int i = 0x4E00; i <= 0x4EFF; i++) { // 常用汉字范围
            sb.append((char) i);
        }
        // 添加更多常用汉字
        for (int i = 0x4F00; i <= 0x9FFF; i += 20) { // 其他CJK汉字，每隔20个取一个
            sb.append((char) i);
        }
        return sb.toString();
    }
    
    /**
     * 初始化字体
     */
    public static void init() {
        FreeTypeFontGenerator generator = null;
        String usedFontPath = null;
        
        // 尝试加载字体文件
        for (String fontPath : FONT_PATHS) {
            try {
                FileHandle fontFile = Gdx.files.absolute(fontPath);
                Gdx.app.log("FontUtils", "Checking font file: " + fontPath + ", exists: " + fontFile.exists());
                if (fontFile.exists()) {
                    try {
                        generator = new FreeTypeFontGenerator(fontFile);
                        usedFontPath = fontPath;
                        Gdx.app.log("FontUtils", "Successfully loaded font from: " + fontPath);
                        break;
                    } catch (Exception e) {
                        Gdx.app.error("FontUtils", "Failed to create generator from " + fontPath + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Gdx.app.debug("FontUtils", "Failed to check font file " + fontPath + ": " + e.getMessage());
            }
        }
        
        if (generator == null) {
            // 如果所有字体文件都加载失败，使用项目自带的字体
            Gdx.app.log("FontUtils", "No system font found, trying default bitmap font");
            try {
                FileHandle defaultFontFile = Gdx.files.internal("ui/font.fnt");
                if (defaultFontFile.exists()) {
                    defaultFont = new BitmapFont(defaultFontFile);
                    largeFont = new BitmapFont(defaultFontFile);
                    smallFont = new BitmapFont(defaultFontFile);
                    Gdx.app.log("FontUtils", "Using default bitmap font from assets");
                    return;
                } else {
                    Gdx.app.log("FontUtils", "Default font file not found at ui/font.fnt");
                }
            } catch (Exception e) {
                Gdx.app.error("FontUtils", "Failed to load default font: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 最后的备用方案：使用libGDX默认字体
            defaultFont = new BitmapFont();
            largeFont = new BitmapFont();
            smallFont = new BitmapFont();
            Gdx.app.log("FontUtils", "Using libGDX default font (may not support Chinese)");
            return;
        }
        
        try {
            String chineseChars = generateChineseCharacters();
            Gdx.app.log("FontUtils", "Generated character set length: " + chineseChars.length());
            
            // 默认字体 24px
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 24;
            parameter.characters = chineseChars;
            parameter.mono = false;
            parameter.hinting = FreeTypeFontGenerator.Hinting.AutoMedium;
            parameter.gamma = 1.8f;
            defaultFont = generator.generateFont(parameter);
            Gdx.app.log("FontUtils", "Generated default font (24px)");
            
            // 大字体 36px
            parameter.size = 36;
            largeFont = generator.generateFont(parameter);
            Gdx.app.log("FontUtils", "Generated large font (36px)");
            
            // 小字体 18px
            parameter.size = 18;
            smallFont = generator.generateFont(parameter);
            Gdx.app.log("FontUtils", "Generated small font (18px)");
            
            // 测试字体是否能正确显示中文
            GlyphLayout testLayout = new GlyphLayout(defaultFont, "测试");
            Gdx.app.log("FontUtils", "Test Chinese text width: " + testLayout.width);
            
            Gdx.app.log("FontUtils", "All fonts generated successfully from: " + usedFontPath);
        } catch (Exception e) {
            Gdx.app.error("FontUtils", "Failed to generate fonts: " + e.getMessage());
            e.printStackTrace();
            // 如果生成失败，使用默认字体
            try {
                FileHandle defaultFontFile = Gdx.files.internal("ui/font.fnt");
                if (defaultFontFile.exists()) {
                    defaultFont = new BitmapFont(defaultFontFile);
                    largeFont = new BitmapFont(defaultFontFile);
                    smallFont = new BitmapFont(defaultFontFile);
                    Gdx.app.log("FontUtils", "Using default bitmap font as fallback");
                } else {
                    defaultFont = new BitmapFont();
                    largeFont = new BitmapFont();
                    smallFont = new BitmapFont();
                    Gdx.app.log("FontUtils", "Using libGDX default font as fallback");
                }
            } catch (Exception ex) {
                defaultFont = new BitmapFont();
                largeFont = new BitmapFont();
                smallFont = new BitmapFont();
                Gdx.app.error("FontUtils", "Complete fallback to libGDX default font");
            }
        } finally {
            if (generator != null) {
                generator.dispose();
            }
        }
    }
    
    public static BitmapFont getDefaultFont() {
        if (defaultFont == null) {
            init();
        }
        return defaultFont;
    }
    
    public static BitmapFont getLargeFont() {
        if (largeFont == null) {
            init();
        }
        return largeFont;
    }
    
    public static BitmapFont getSmallFont() {
        if (smallFont == null) {
            init();
        }
        return smallFont;
    }
    
    public static void dispose() {
        if (defaultFont != null) {
            defaultFont.dispose();
        }
        if (largeFont != null) {
            largeFont.dispose();
        }
        if (smallFont != null) {
            smallFont.dispose();
        }
    }
}

