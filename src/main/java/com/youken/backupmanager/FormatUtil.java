package com.youken.backupmanager;

import cn.hutool.core.io.FileUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author 杨剑
 * @date 2019/12/25
 */
@Slf4j
public class FormatUtil {

    private static final String PATTERN_HH_MM_SS_SSS = "HH:mm:ss.SSS";
    private static final String TZ_GMT_0 = "GMT+00:00";

    private FormatUtil() {
    }

    public static String concatWithFileSeparator(@NonNull final String filePath, @NonNull final String subFilePath) {
        StringBuilder stb = new StringBuilder(filePath);
        if (!filePath.endsWith(File.separator)) {
            stb.append(File.separator);
        }
        stb.append(subFilePath, 0, subFilePath.length() - (subFilePath.endsWith(File.separator) ? File.separator.length() : 0));
        return stb.toString();
    }

    public static void print(@NonNull final String op, @NonNull final File file) {
        long size = FileUtil.size(file);
        FileSizeEnum fileSizeEnum = FileSizeEnum.getFileSize(size);
        BigDecimal fileSize = new BigDecimal(size).divide(new BigDecimal(fileSizeEnum.getValue()), 2, RoundingMode.HALF_UP);
        log.info("{}\t{}\t{}", op, String.format("%s %s", String.format("%1$7.2f", fileSize), fileSizeEnum.getKey()), file);
    }

    public static String formatCostMilliseconds(@NonNull final long milliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_HH_MM_SS_SSS);
        sdf.setTimeZone(TimeZone.getTimeZone(TZ_GMT_0));
        return sdf.format(milliseconds);
    }
}
