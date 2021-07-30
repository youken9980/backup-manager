package com.youken.backupmanager;

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
        String srcRootPath = "/Users/youken/Destiny/tmp/Downloads/as/1";
        String destRootPath = "/Users/youken/Destiny/tmp/Downloads/as/2";
        String[] subDirs = {""};
        new BackupUtil().backup(srcRootPath, destRootPath, subDirs);
    }
}
