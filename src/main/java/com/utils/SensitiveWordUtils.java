package com.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感词归一化与替换工具。
 */
public final class SensitiveWordUtils {

    private static final Pattern KEYWORD_SPLIT_PATTERN = Pattern.compile("[,，、;；\\r\\n]+");

    private SensitiveWordUtils() {
    }

    public static List<String> normalizeKeywords(Collection<String> rawTexts) {
        if (rawTexts == null || rawTexts.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> keywords = new LinkedHashSet<String>();
        for (String rawText : rawTexts) {
            if (StringUtils.isBlank(rawText)) {
                continue;
            }
            String[] segments = KEYWORD_SPLIT_PATTERN.split(rawText);
            for (String segment : segments) {
                String keyword = StringUtils.trimToEmpty(segment);
                if (StringUtils.isNotBlank(keyword)) {
                    keywords.add(keyword);
                }
            }
        }
        List<String> normalized = new ArrayList<String>(keywords);
        Collections.sort(normalized, new Comparator<String>() {
            @Override
            public int compare(String left, String right) {
                return Integer.compare(right.length(), left.length());
            }
        });
        return normalized;
    }

    public static String maskContent(String content, Collection<String> keywords) {
        if (content == null || keywords == null || keywords.isEmpty()) {
            return content;
        }
        String masked = content;
        for (String keyword : keywords) {
            if (StringUtils.isBlank(keyword) || !masked.contains(keyword)) {
                continue;
            }
            masked = masked.replaceAll(Pattern.quote(keyword), Matcher.quoteReplacement("**"));
        }
        return masked;
    }
}
