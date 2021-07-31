package com.youken.backupmanager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.crypto.SecureUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * @author 杨剑
 * @date 2019/12/25
 */
@Slf4j
public class BackupUtil {

    /**
     * 忽略文件列表
     */
    private static final List<String> EXCLUDE_FILE_LIST;
    private static final FileFilter FILE_FILTER;

    static {
        EXCLUDE_FILE_LIST = new ArrayList<>();
        EXCLUDE_FILE_LIST.add(".DS_Store");
        EXCLUDE_FILE_LIST.add("._.DS_Store");
        FILE_FILTER = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !EXCLUDE_FILE_LIST.contains(pathname.getName());
            }
        };
    }

    private static boolean useMd5;

    @Value("${useMd5}")
    public void setUseMd5(final boolean useMd5) {
        BackupUtil.useMd5 = BooleanUtil.isTrue(useMd5);
    }

    public void backup(@NonNull final String srcRootPath, @NonNull final String destRootPath, final String... subDirs) throws FileNotFoundException {
        long startTime = System.currentTimeMillis();
        try {
            List<Pair<String, String>> rootPathPair = assembleRootPathPair(srcRootPath, destRootPath, subDirs);
            for (Pair<String, String> pair : rootPathPair) {
                sync(pair.getKey(), pair.getValue());
            }
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("costTime: {}", FormatUtil.formatCostMilliseconds(endTime - startTime));
        }
    }

    private List<Pair<String, String>> assembleRootPathPair(@NonNull final String srcRootPath, @NonNull final String destRootPath, final String... subDirs) throws FileNotFoundException {
        if (!FileUtil.exist(srcRootPath)) {
            throw new FileNotFoundException("源目录不存在。");
        }
        if (!FileUtil.exist(destRootPath)) {
            throw new FileNotFoundException("目标目录不存在。");
        }

        List<Pair<String, String>> rootPathPair;
        if (subDirs == null || subDirs.length < 1) {
            rootPathPair = new ArrayList<>(1);
            rootPathPair.add(Pair.of(srcRootPath, destRootPath));
        } else {
            rootPathPair = new ArrayList<>(subDirs.length);
            for (String str : subDirs) {
                String subDirPath = StringUtils.trimToNull(str);
                if (subDirPath == null) {
                    continue;
                }
                String srcSubDirPath = FormatUtil.concatWithFileSeparator(srcRootPath, subDirPath);
                String destSubDirPath = FormatUtil.concatWithFileSeparator(destRootPath, subDirPath);
                if (BooleanUtils.isFalse(new File(srcSubDirPath).exists())) {
                    throw new FileNotFoundException("源目录不存在。");
                }
                rootPathPair.add(Pair.of(srcSubDirPath, destSubDirPath));
            }
        }
        return rootPathPair;
    }

    /**
     * 同步
     *
     * @param srcFilePath  源目录
     * @param destFilePath 目标目录
     */
    private void sync(@NonNull final String srcFilePath, @NonNull final String destFilePath) {
        File srcFile = FileUtil.file(srcFilePath);
        File destFile = FileUtil.file(destFilePath);

        if (!FileUtil.exist(srcFile) || FileUtil.isSymlink(srcFile)) {
            // 源不存在或源是符号链接
            return;
        }

        if (FileUtil.isFile(srcFile)) {
            // 源是文件
            if (!FileUtil.exist(destFile)) {
                // 目标不存在
            } else if (FileUtil.isSymlink(destFile) || FileUtil.isDirectory(destFile)) {
                // 目标是符号链接或目录
                // 删除目标
                FormatUtil.print("rm", destFile);
                FileUtil.del(destFile);
            } else if (FileUtil.isFile(destFile)) {
                // 目标是文件
                if (equals(srcFile, destFile)) {
                    return;
                }
            }
            // 复制源
            FormatUtil.print("cp", srcFile);
            try {
                FileUtils.copyFile(srcFile, destFile);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else if (FileUtil.isDirectory(srcFile)) {
            // 源是目录
            if (!FileUtil.exist(destFile)) {
                // 目标不存在
                // 复制源
                FormatUtil.print("cp", srcFile);
                try {
                    FileUtils.copyDirectory(srcFile, destFile, FILE_FILTER);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            } else if (FileUtil.isSymlink(destFile) || FileUtil.isFile(destFile)) {
                // 目标是符号链接或文件
                // 删除目标
                FormatUtil.print("rm", destFile);
                FileUtil.del(destFile);
                // 复制源
                FormatUtil.print("cp", srcFile);
                try {
                    FileUtils.copyDirectory(srcFile, destFile, FILE_FILTER);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            } else if (FileUtil.isDirectory(destFile)) {
                // 目标是目录
                // 删除目标目录存在但源目录不存在的子目录或文件
                Map<String, File> srcSubFileMap = mappingSubFileList(srcFile, FILE_FILTER);
                Map<String, File> destSubFileMap = mappingSubFileList(destFile, null);
                Set<String> removeFileSet = new LinkedHashSet<>(destSubFileMap.keySet());
                removeFileSet.removeAll(srcSubFileMap.keySet());
                for (String key : removeFileSet) {
                    File removeFile = destSubFileMap.get(key);
                    FormatUtil.print("rm", removeFile);
                    FileUtil.del(removeFile);
                }
                // 递归同步其它子目录和文件
                for (File f : srcSubFileMap.values()) {
                    sync(f.getAbsolutePath(), FormatUtil.concatWithFileSeparator(destFilePath, f.getName()));
                }
            }
        }
    }

    /**
     * 文件对比
     * ps: 使用MD5算法可确保文件内容精准但效率太低，暂使用文件名、文件大小、最后修改时间三要素对比
     */
    private boolean equals(@NonNull final File src, @NonNull final File dest) {
        String srcName = src.getName();
        String destName = dest.getName();
        boolean isSameName = Objects.equals(srcName, destName);

        long srcLength = src.length();
        long destLength = dest.length();
        boolean isSameLength = Objects.equals(srcLength, destLength);

        String srcLastModified = DateUtil.formatDateTime(new Date(src.lastModified()));
        String destLastModified = DateUtil.formatDateTime(new Date(dest.lastModified()));
        boolean isSameLastModified = Objects.equals(srcLastModified, destLastModified);

        if (isSameName && isSameLength && isSameLastModified) {
            return true;
        }

        boolean isSameMd5 = true;
        if (useMd5) {
            String srcMd5 = SecureUtil.md5(src);
            String destMd5 = SecureUtil.md5(dest);
            isSameMd5 = Objects.equals(srcMd5, destMd5);
        }

        return useMd5 && isSameMd5;
    }

    private Map<String, File> mappingSubFileList(File file, FileFilter filter) {
        Map<String, File> map = new LinkedHashMap<>();
        List<File> fileList = Optional.ofNullable(file.listFiles(filter)).map(Arrays::asList).orElse(Collections.emptyList());
        fileList.sort(Comparator.comparing(File::getAbsolutePath));
        for (File f : fileList) {
            map.put(f.getName(), f);
        }
        return map;
    }
}
