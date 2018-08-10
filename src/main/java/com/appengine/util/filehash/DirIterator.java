package com.appengine.util.filehash;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

public class DirIterator {

	private static final int FILEFLUSHTRIGGER = 100;
	private final CharSequence DELIMITER = ", ";
	private String outputFilename = null;
	private String rootDir = null;
	private Path rootPath;
	int fileCount = 0;

	public DirIterator(Path rootPath) {
		this.rootPath = rootPath;
	}

	private PrintWriter outPutFileWriter;

	public void setOutputFileWriter(PrintWriter outFile) {
		this.outPutFileWriter = outFile;
	}

	public void closeOutputFileWriter() {
		outPutFileWriter.close();
	}

	private Consumer<File> writefileInfo = (file) -> {

		long fSize = file.length();
		String md5 = generateMD5(file);
		/*String relPath = getRelativePath(rootPath, file);

		List<String> valuesList = Arrays.asList(relPath.concat(file.getName()),
				String.valueOf(fSize), md5);
*/
		String relPath = rootPath.relativize(file.toPath()).toString(); 
		
		//if file in root dir
		if (FilenameUtils.indexOfLastSeparator(relPath) == -1)  // file in root folder
			relPath = File.separator.concat(relPath);
					
		List<String> valuesList = 
				Arrays.asList(relPath, String.valueOf(fSize),md5);

		String delimiterSeparatedValues = String.join(DELIMITER, valuesList);

		// write out results
		if (outPutFileWriter != null) {
			outPutFileWriter.println(delimiterSeparatedValues);
			// outPutFileWriter.flush();
			if (fileCount > FILEFLUSHTRIGGER) { // flush after every n lines written to buffer
				outPutFileWriter.flush();
				fileCount = 0;
			} else
				fileCount++;
		} else
			System.out.println(delimiterSeparatedValues);
	};

	

	// we could make this static or separate util class
	private String getRelativePath(Path rootDir, File file) {
		// get relative path
		int indexStart = rootDir.toString().length();
		String relPath = file.getPath().substring(indexStart + 1);

		int lastSeparatorIndex = FilenameUtils.indexOfLastSeparator(relPath);
		if (lastSeparatorIndex == -1) { // then this file is in root folder
			relPath = File.separator;
		} else { // else file in sub folder
			relPath = relPath.substring(0, lastSeparatorIndex) + File.separator;
		}

		return relPath;
	}

	// using apache commons io
	public void generateFileHash(File rootDir) {
		if (rootDir == null) {
			System.err.println("File or dir cannot be null");
			System.exit(0);
		}
		File[] files = rootDir.listFiles((FilenameFilter) FileFilterUtils
				.fileFileFilter());

		// process files
		for (File file : files) {
			// System.out.println(file.getName());
			writefileInfo.accept(file);
		}

		// process dirs
		File[] dirs = rootDir.listFiles((FilenameFilter) FileFilterUtils
				.directoryFileFilter());

		for (File dir : dirs) {
			generateFileHash(dir);
		}
	}

	
	// use java 8 stream
	public void generateFileHashViaStream(Path path) {
		if (path == null) {
			System.err.println("File or dir cannot be null");
			System.exit(0);
		}

		try (Stream<Path> paths = Files.walk(path)) {
			paths.filter(Files::isRegularFile).forEach((file)->writefileInfo.accept(file.toFile()));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(0);
		}

	}

	// we could make this static or separate util class
	private String generateMD5(File file) {
		String md5 = null;
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			md5 = DigestUtils.md5Hex(IOUtils.toByteArray(fileInputStream));
			fileInputStream.close();
		} catch (IOException e) {
			// e.printStackTrace();
			System.err.println(e.getMessage());
			System.exit(0);
		}
		return md5;
	}

	// public void iterateToFile(PrintWriter out, String contents) {
	// // TODO Auto-generated method stub
	// out.write(contents);
	// }
	//
	// public void iterateToFile(String outputFilename, String contents) {
	// // TODO Auto-generated method stub
	//
	// }

}
