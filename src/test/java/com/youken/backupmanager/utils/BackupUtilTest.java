package com.youken.backupmanager.utils;

import com.youken.backupmanager.BackupManagerApplicationTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author 杨剑
 * @date 2019/12/30
 */
@Slf4j
class BackupUtilTest extends BackupManagerApplicationTests {

	@Test
	void test() throws Exception {
		boolean writable = true;
		boolean useMd5 = false;
		String srcRootPath = "/Users/youken/Destiny";
		String destRootPath = "/Volumes/Seed/Destiny";
		String[] subDirs = {""};
		new BackupUtil(writable, useMd5).backup(srcRootPath, destRootPath, subDirs);
	}
}
