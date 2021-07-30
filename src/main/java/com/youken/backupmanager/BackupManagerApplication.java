package com.youken.backupmanager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

/**
 * @author 杨剑
 * @date 2019/12/25
 */
@Slf4j
@SpringBootApplication
public class BackupManagerApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(BackupManagerApplication.class, args);
        backup(args);
    }

    private static void backup(String[] args) throws Exception {
        if (args == null || args.length < 2) {
            log.info("使用方法: java -jar BackupManager.jar 源目录 目标目录 子目录列表或文件列表...");
            return;
        }
        String srcRootPath = args[0];
        String destRootPath = args[1];
        String[] subDirs = {};
        Arrays.asList(args).subList(2, args.length).toArray(subDirs);
        new BackupUtil().backup(srcRootPath, destRootPath, subDirs);
    }
}
