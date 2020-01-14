package com.youken.backupmanager.utils;

import com.youken.backupmanager.model.TwoTuple;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 杨剑
 * @date 2019/12/25
 */
@Slf4j
public class BackupUtil {

	private static final List<String> EXCLUDES;
	private static final FileFilter FILE_FILTER;

	static {
		EXCLUDES = new ArrayList<>();
		EXCLUDES.add(".DS_Store");
		FILE_FILTER = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return !EXCLUDES.contains(pathname.getName());
			}
		};
	}

	private boolean writable;
	private boolean useMd5;
	private AtomicLong fileSize;
	private FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			fileSize.getAndAdd(file.toFile().length());
			return super.visitFile(file, attrs);
		}
	};

	public BackupUtil(boolean writable, boolean useMd5) {
		this.writable = writable;
		this.useMd5 = useMd5;
	}

	public void backup(String srcRootPath, String destRootPath, String... subDirs) throws IOException {
		long startTime = System.currentTimeMillis();
		try {
			List<TwoTuple<String, String>> rootPathTuple = assembleRootPathTuple(srcRootPath, destRootPath, subDirs);
			for (TwoTuple<String, String> tuple : rootPathTuple) {
				sync(tuple.getA(), tuple.getB());
			}
		} finally {
			long endTime = System.currentTimeMillis();
			log.info("costTime: {}", FormatUtil.formatCostMilliseconds(endTime - startTime));
		}
	}

	private List<TwoTuple<String, String>> assembleRootPathTuple(String srcRootPath, String destRootPath, String... subDirs) throws IOException {
		List<TwoTuple<String, String>> rootPathTuple;
		srcRootPath = StringUtils.trimToNull(srcRootPath);
		if (srcRootPath == null || BooleanUtils.isFalse(new File(srcRootPath).exists())) {
			throw new IOException("源目录为空或不存在");
		}
		destRootPath = StringUtils.trimToNull(destRootPath);
		if (destRootPath == null || BooleanUtils.isFalse(new File(destRootPath).exists())) {
			throw new IOException("目标目录为空或不存在");
		}
		if (subDirs == null || subDirs.length < 1) {
			rootPathTuple = new ArrayList<>(1);
			rootPathTuple.add(TwoTuple.<String, String>builder().a(srcRootPath).b(destRootPath).build());
		} else {
			rootPathTuple = new ArrayList<>(subDirs.length);
			for (String str : subDirs) {
				String subDirPath = StringUtils.trimToEmpty(str);
				String srcSubDirPath = FormatUtil.concatWithFileSeparator(srcRootPath, subDirPath);
				String destSubDirPath = FormatUtil.concatWithFileSeparator(destRootPath, subDirPath);
				if (BooleanUtils.isFalse(new File(srcSubDirPath).exists())) {
					throw new FileNotFoundException("源目录不存在");
				}
				rootPathTuple.add(TwoTuple.<String, String>builder()
						.a(srcSubDirPath)
						.b(destSubDirPath)
						.build());
			}
		}
		return rootPathTuple;
	}

	/**
	 * 递归同步
	 */
	private void sync(String srcFilePath, String destFilePath) {
		try {
			File srcFile = new File(srcFilePath);
			File destFile = new File(destFilePath);
			if (srcFile.isFile()) {
				if (destFile.isDirectory()) {
					// 源是文件，目标是目录，删除目标目录
					print("rm", destFile);
					if (writable) {
						FileUtils.deleteDirectory(destFile);
					}
				} else if (destFile.isFile()) {
					if (equals(srcFile, destFile)) {
						return;
					}
				}
				// 1. 源是文件，目标是目录，目标目录已删除
				// 2. 源是文件，目标是文件，文件内容不同
				// 3. 目标不存在
				// 以上三种情况，复制源到目标
				print("cp", srcFile);
				if (writable) {
					FileUtils.copyFile(srcFile, destFile);
				}
			} else if (srcFile.isDirectory()) {
				if (destFile.isDirectory()) {
					// 源是目录，目标是目录，删除目标目录存在但源目录不存在的子目录或文件
					Map<String, File> srcSubFileMap = mappingSubFileList(srcFile, FILE_FILTER);
					Map<String, File> destSubFileMap = mappingSubFileList(destFile, null);
					Set<String> removeFiles = new LinkedHashSet<>(destSubFileMap.keySet());
					removeFiles.removeAll(srcSubFileMap.keySet());
					for (String key : removeFiles) {
						File removeFile = destSubFileMap.get(key);
						print("rm", removeFile);
						if (writable) {
							FileUtils.deleteQuietly(removeFile);
						}
					}
					for (File f : srcSubFileMap.values()) {
						sync(f.getAbsolutePath(), FormatUtil.concatWithFileSeparator(destFilePath, f.getName()));
					}
				} else if (destFile.isFile()) {
					// 源是目录，目标是文件，删除目标文件，复制源到目标
					print("rm", destFile);
					if (writable) {
						FileUtils.deleteQuietly(destFile);
					}
					print("cp", srcFile);
					if (writable) {
						FileUtils.copyDirectory(srcFile, destFile, FILE_FILTER);
					}
				} else {
					// 目标不存在，复制源到目标
					print("cp", srcFile);
					if (writable) {
						FileUtils.copyDirectory(srcFile, destFile, FILE_FILTER);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void print(String op, File file) {
		try {
			fileSize = new AtomicLong(0L);
			String filePath = file.getAbsolutePath();
			Files.walkFileTree(Paths.get(filePath), visitor);
			log.info("{}\t{}\t{}", op, FormatUtil.formatSize(fileSize.get()), filePath);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 文件对比
	 * ps: 使用MD5算法可确保文件内容精准但效率太低，暂使用文件名、文件大小、最后修改时间三要素对比
	 */
	private boolean equals(File src, File dest) {
		if (useMd5) {
			String srcMd5 = MessageDigestUtil.md5(src.getAbsolutePath());
			String destMd5 = MessageDigestUtil.md5(dest.getAbsolutePath());
			return Objects.equals(srcMd5, destMd5);
		} else {
			return src.getName().equals(dest.getName())
					&& (src.length() == dest.length())
					&& (src.lastModified() == dest.lastModified());
		}
	}

	private Map<String, File> mappingSubFileList(File file, FileFilter filter) {
		Map<String, File> map = new LinkedHashMap<>();
		File[] files = file.listFiles(filter);
		if (files != null && files.length > 0) {
			for (File f : files) {
				map.put(f.getName(), f);
			}
		}
		return map;
	}
}
