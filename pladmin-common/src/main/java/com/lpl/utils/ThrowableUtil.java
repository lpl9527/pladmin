package com.lpl.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author lpl
 * 异常工具类
 */
public class ThrowableUtil {

    public static String getStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
            return stringWriter.toString();
        }
    }
}
