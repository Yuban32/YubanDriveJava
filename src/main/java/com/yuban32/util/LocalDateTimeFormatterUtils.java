package com.yuban32.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Yuban32
 * @ClassName LocalDateTimeFormatterUtils
 * @Description
 * @Date 2023年03月21日
 */
public class LocalDateTimeFormatterUtils {
    String format = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    LocalDateTime startDateTime = LocalDateTime.parse(format, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    public LocalDateTime getStartDateTime(){
        return startDateTime;
    }
}
