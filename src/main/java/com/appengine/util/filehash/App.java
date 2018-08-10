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
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

/**
 * Implement a command line utility in java that generates a CSV
 * (comma-separated values) report of the sizes and md5 hashes of all files
 * contained in a given directory tree of arbitrary depth. Each line of output
 * should have three values: path (relative to the provided input directory),
 * size (in bytes), and md5 hash. The output should be suitable for opening in
 * either a text editor or a spreadsheet tool such as Microsoft Excel or Google
 * Sheets. Be sure to handle file names that contain spaces and other commonly
 * used non-alphanumeric characters (hint: use a library and supply a pom.xml or
 * equivalent so that I can easily compile and run your code).
 *
 */
public class App {
	private static String outputFilename = null;
	private static String rootDir = null;
	private static Path rootPath;
	private static PrintWriter printWriter;


	public static void main(String[] args) {
		
		//check that we have the right number of arguments 
		//for example ... d:\wares d:\wares\out.csv
		//first arg is starting dir and second is path to output file
		
		if (0 < args.length) {
			rootDir = args[0]; // check of null
			outputFilename = args[1]; // check if null
			System.out.println(rootDir + " and " + outputFilename);
		} else {
			System.err.println("Invalid arguments count:" + args.length);
			System.exit(0);
		}
		//starting dir 
		rootPath = Paths.get(rootDir);
		if (!rootPath.toFile().isDirectory())		{
			System.err.println("Starting folder is a directory : " + rootDir);
			System.exit(0);
		}
		
		//get a buffered printer writer to send output to a file  
		try {
			printWriter = new PrintWriter(new BufferedWriter(
					new FileWriter(outputFilename)));
		} catch (IOException e) {
			System.err.println("Invalid output file :" + outputFilename);
			System.exit(0);
		}

		
		//this could/should probably be a utility class wiht static mehtods, 
		//and not object 
		DirIterator dirIterator = new DirIterator(rootPath);
		//comment out to write to stdio instead of to a file 
		dirIterator.setOutputFileWriter(printWriter);
		
		//COMMENT OUT 1. OR 2. TO SEE THE RESULTS BEFORE EXECUTING THE PROGRAM
		//1.recurse dir using apache io
		dirIterator.generateFileHash(rootPath.toFile());
		
		//2.recurse dir using Java 8 stream 
		//dirIterator.generateFileHashViaStream(rootPath);
		
		dirIterator.closeOutputFileWriter();

	}

}
