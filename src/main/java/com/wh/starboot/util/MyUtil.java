package com.wh.starboot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyUtil {

    private static final Logger logger = LoggerFactory.getLogger(MyUtil.class);

    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取字符串中间值
     *
     * @param str    原字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 中间值
     */
    public static String getBetweenValue(String str, String prefix, String suffix) {
        int prefixIndex = str.indexOf(prefix);
        if (prefixIndex == -1) {
            return "";
        }
        int begin = prefixIndex + prefix.length();
        int end = str.indexOf(suffix, begin);
        if (end < begin) {
            return "";
        }
        return str.substring(begin, end);
    }

    public static List<String> getMatchers(String regex, String str) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    public static long dateStr2Timestamp(String dateStr) {
        long timestamp = 0L;
        SimpleDateFormat sdfDatetime = new SimpleDateFormat("yyyy" + Consts.DATE_SEPARATOR + "MM" + Consts.DATE_SEPARATOR + "dd HH:mm:ss");
        try {
            timestamp = sdfDatetime.parse(dateStr).getTime();
        } catch (ParseException e) {
            logger.error("dateStr2Timestamp|fail|dateStr:{}", dateStr, e);
        }
        return timestamp;
    }

}
