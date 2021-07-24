package com.youken.backupmanager.constant;

/**
 * @author 杨剑
 * @date 2019/12/27
 */
public enum FileSizeEnum {

	// B
	B("B ", 1L),
	// KB
	KB("KB", 1024 * B.value),
	// MB
	MB("MB", 1024 * KB.value),
	// GB
	GB("GB", 1024 * MB.value),
	// TB
	TB("TB", 1024 * GB.value),
	;

	private final String key;
	private final long value;

	FileSizeEnum(String key, long value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public long getValue() {
		return value;
	}

	public static FileSizeEnum getFileSize(long size) {
		if (size < B.value) {
			return B;
		} else if (size < MB.value) {
			return KB;
		} else if (size < GB.value) {
			return MB;
		} else if (size < TB.value) {
			return GB;
		}
		return TB;
	}
}
