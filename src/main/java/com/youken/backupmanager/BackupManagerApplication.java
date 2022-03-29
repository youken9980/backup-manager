package com.youken.backupmanager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        List<String> subList = Arrays.asList(args).subList(2, args.length);
        String[] subDirs = new String[subList.size()];
        subDirs = subList.toArray(subDirs);
        log.info("args: {}", Arrays.asList(args));
        log.info("args length: {}", args.length);
        log.info("srcRootPath: {}", srcRootPath);
        log.info("destRootPath: {}", destRootPath);
        log.info("subDirs: {}", Arrays.asList(subDirs));
        new BackupUtil().backup(srcRootPath, destRootPath, subDirs);
    }
}
