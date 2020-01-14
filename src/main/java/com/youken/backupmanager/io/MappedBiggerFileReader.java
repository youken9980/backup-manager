package com.youken.backupmanager.io;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author 杨剑
 * @date 2019/12/25
 */
@Slf4j
public class MappedBiggerFileReader implements Closeable {

	private static final int BUFFER_SIZE = 1024 * 1024 * 128;

	private MappedByteBuffer[] mappedBufArray;
	private int count = 0;
	private int number;
	private FileInputStream fileIn;
	private long fileLength;
	private int bufferSize;
	private byte[] buffer;

	public MappedBiggerFileReader(@NonNull String filePath) throws IOException {
		this(filePath, BUFFER_SIZE);
	}

	private MappedBiggerFileReader(@NonNull String filePath, int bufferSize) throws IOException {
		this.fileIn = new FileInputStream(filePath);
		FileChannel fileChannel = fileIn.getChannel();
		this.fileLength = fileChannel.size();
		this.number = (int) Math.ceil((double) fileLength / (double) Integer.MAX_VALUE);
		// 内存文件映射数组
		this.mappedBufArray = new MappedByteBuffer[number];
		long preLength = 0;
		// 映射区域的大小
		long regionSize = Integer.MAX_VALUE;
		// 将文件的连续区域映射到内存文件映射数组中
		for (int i = 0; i < number; i++) {
			if (fileLength - preLength < (long) Integer.MAX_VALUE) {
				// 最后一片区域的大小
				regionSize = fileLength - preLength;
			}
			mappedBufArray[i] = fileChannel.map(FileChannel.MapMode.READ_ONLY, preLength, regionSize);
			// 下一片区域的开始
			preLength += regionSize;
		}
		this.bufferSize = bufferSize;
	}

	public int read() {
		if (count >= number) {
			return -1;
		}
		int limit = mappedBufArray[count].limit();
		int position = mappedBufArray[count].position();
		if (limit - position > bufferSize) {
			buffer = new byte[bufferSize];
			mappedBufArray[count].get(buffer);
			return bufferSize;
		} else {
			// 本内存文件映射最后一次读取数据
			buffer = new byte[limit - position];
			mappedBufArray[count].get(buffer);
			if (count < number) {
				// 转换到下一个内存文件映射
				count++;
			}
			return limit - position;
		}
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public long getFileLength() {
		return fileLength;
	}

	@Override
	public void close() throws IOException {
		fileIn.close();
		buffer = null;
	}
}
