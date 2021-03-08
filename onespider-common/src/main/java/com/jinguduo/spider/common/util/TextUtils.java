package com.jinguduo.spider.common.util;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class TextUtils {

    public static Set<String> extract(String text, String prefix, String postfix) {
        HashSet<String> tags = new HashSet<>();
        int pre = text.indexOf(prefix, 0);
        while (pre >= 0 && pre < text.length()) {
            int pos = text.indexOf(postfix, ++pre);
            if (pos > 0) {
                String s = text.substring(pre, pos);
                tags.add(s);
                // next
                if (++pos < text.length()) {
                    pre = text.indexOf(prefix, pos);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return tags;
    }

    // BadCase: 0xFFFD
    private final static Pattern CHINESE = Pattern.compile("[\\u4E00-\\u9FA5]");  // 基本汉字

    //private final static Pattern ENGLISH = Pattern.compile("[\\u0020-\\u007E]");  // 英文字母

    /**
     * 乱码检测（不含英文数字、字母或中文基本汉字）
     *
     * @param content
     * @return
     */
    public static boolean hasChinese(String content) {
        return CHINESE.matcher(content).find();
    }

    private final static Pattern MOJIBAKE = Pattern.compile("[\\uFFFD]");

    public static String removeMojibake(String content) {
        return MOJIBAKE.matcher(content).replaceAll("");
    }

    /**
     * UTF8 emoji  http://apps.timwhitlock.info/emoji/tables/unicode#block-6c-other-additional-symbols
     *   U+2190 to U+21FF
     *   U+2600 to U+26FF
     *   U+2700 to U+27BF
     *   U+3000 to U+303F
     *   U+1F300 to U+1F64F
     *   U+1F680 to U+1F6FF
     */
    //private final static Pattern EMOJI = Pattern.compile("[\\u2190-\\u21FF\\u2600-\\u26FF\\u2700-\\u27BF\\u3000-\\u303F\\u1F170-\\u1F251\\u1F300-\\u1F6C5\\u1F680-\\u1F6FF]");

    /**
     * 过滤emoji字符
     * 搜狗输入法会在表情符后插入 0xFE0F 字符（variation selector-16） http://unicodelookup.com/#65039/1
     *
     * @param source
     * @return
     */
    private final static Pattern VARIATION_SELECTOR = Pattern.compile(String.valueOf('\uFE0F'));
    private final static Pattern VARIATION_ZERO_WIDTH_SPACE = Pattern.compile(String.valueOf('\u200B'));

    public static String removeEmoji(String source) {
        if (org.apache.commons.lang3.StringUtils.isBlank(source)) {
            return source;
        }
        source = source.replaceAll("\\[.{1,3}\\]", "\uD83D\uDC66");
        String t = EmojiParser.removeAllEmojis(source);
        t = VARIATION_SELECTOR.matcher(t).replaceAll("");
        t = VARIATION_ZERO_WIDTH_SPACE.matcher(t).replaceAll("");
        return t;
    }

    /**
     * 过滤内容中的表情符
     * 如 '\xF0\x9F\x8D\x80'
     *
     * @param source
     * @return
     */
    public static String removeExpression(String source) {
        return source.replaceAll("[\\x{10000}-\\x{10FFFF}]", "");
    }

    /**
     * 无意义的句子
     * example:
     * [001]
     * 6666666
     * 2333333
     */
    private final static Pattern POPPYCOCK = Pattern.compile("\\[[\\w]{3,7}\\]|<[\\w]{3,7}>|[。\\.\\d\\s]{5,}");

    public static String removePoppycockChar(String source) {
        return POPPYCOCK.matcher(source).replaceAll("");
    }


    private static final String REVERSE_SOLIDUS = "\\";

    /**
     * 删除尾部多余的转义反斜线\
     *
     * @param s
     * @return
     */
    public static String removeExtraReverseSolid(String s) {
        if (s == null
                || s.length() == 0
                || !s.endsWith(REVERSE_SOLIDUS)) {
            return s;
        }
        int lastIdx = s.lastIndexOf(REVERSE_SOLIDUS);
        while (lastIdx > 0
                && REVERSE_SOLIDUS.equals(s.substring(lastIdx - 1, lastIdx))) {
            lastIdx--;
        }
        return s.substring(0, lastIdx);
    }

    private static final String DOUBLE_QUOTE = "\"";

    /**
     * 删除头或尾双引号
     *
     * @param source
     * @return
     */
    public static String removeCsvEscapeQuote(String source) {
        if (source == null
                || source.length() < 3
                || !source.startsWith(DOUBLE_QUOTE)
                || !source.endsWith(DOUBLE_QUOTE)) {
            return source;
        }
        return source.substring(1, source.length() - 1);
    }

    /**
     * 删除空白符
     *
     * @param source
     * @return
     */
    private static final Pattern SPECIAL_SPACE = Pattern.compile("\\r|\\n|\\t");

    public static String removeSpecialSpace(String source) {
        return SPECIAL_SPACE.matcher(source).replaceAll("").trim();
    }

    /**
     * 修复CSV字符串（多余的双引号或多余的转义反斜线）
     *
     * @param s
     * @return
     */
    public static String fixCsvFormat(String s) {
        return removeExtraReverseSolid(removeCsvEscapeQuote(removeSpecialSpace(s)));
    }

    public static String removeBadText(String source) {
        if (StringUtils.hasText(source)) {
            String t = TextUtils.removeMojibake(source);
            t = TextUtils.removeEmoji(t);
            t = TextUtils.removePoppycockChar(t);
            t = TextUtils.fixCsvFormat(t);
            if (StringUtils.hasText(t)
                    && TextUtils.hasChinese(t)) {
                return t;
            }
        }
        return null;
    }

    private static final String DELIMITER_ACTOR = "\\s*[/,，、|]\\s*";

    private static String[] splitAndTrim(String originText, String delimiter) {
        if (originText == null || originText.length() == 0 || originText.trim().length() == 0) return new String[0];
        return originText.trim().split(delimiter == null ? DELIMITER_ACTOR : delimiter);
    }

    public static Set<String> splitWords(String originText, String delimiter) {
        if (originText == null || originText.length() == 0) return null;
        Set<String> words = new HashSet<>();
        for (String word : splitAndTrim(originText, delimiter)) {
            if (!StringUtils.isEmpty(word) && word.trim().length() > 0) {
                words.add(word.trim());
            }
        }
        return words;
    }
}
