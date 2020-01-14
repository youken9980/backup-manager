package com.youken.backupmanager.utils;

import com.youken.backupmanager.constant.FileSizeEnum;
import lombok.NonNull;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author 杨剑
 * @date 2019/12/25
 */
public class FormatUtil {

	private static final String PATTERN_HH_MM_SS_SSS = "HH:mm:ss.SSS";
	private static final String TZ_GMT_0 = "GMT+00:00";

	private FormatUtil() {
	}

	public static String formatCostMilliseconds(long milliseconds) {
		SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_HH_MM_SS_SSS);
		sdf.setTimeZone(TimeZone.getTimeZone(TZ_GMT_0));
		return sdf.format(milliseconds);
	}

	public static String concatWithFileSeparator(@NonNull final String filePath, @NonNull final String subFilePath) {
		return (filePath.endsWith(File.separator) ? filePath.concat(subFilePath) : filePath.concat(File.separator)).concat(subFilePath);
	}

	public static String formatSize(long size) {
		FileSizeEnum fileSize = FileSizeEnum.getFileSize(size);
		BigDecimal result = new BigDecimal(size).divide(new BigDecimal(fileSize.getValue()), 2, RoundingMode.HALF_UP);
		return String.format("%s %s", String.format("%1$7.2f", result), fileSize.getKey());
	}
}
