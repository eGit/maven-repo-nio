package de.genflux;

import static de.genflux.NIO.Type.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import de.genflux.NIOActions.Action;

public class NIO extends NIOA {
	
	private final static byte[] EmptyZip={80,75,05,06,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00};
	enum Type {File, Folder, Zip, FCUK}
	
	
	private NIO() {
		super();
	}
	
	public static NIO quiet() {
		return new NIO();
	}
	
	public static NIO info() {
		NIO nio = quiet();
		nio.info = true;
		return nio;
	}
	
	public static NIO debug() {
		NIO nio = info();
		nio.debug = true;
		return nio;
	}

	
//	***** START: FILE OPERATIONS *****
	
	public NIO file2file(String file1, String file2) throws IOException { return file2file(Paths.get(file1), Paths.get(file2)); }
	public NIO file2file(Path file1, Path file2) throws IOException {
		check(file1, File, file2, File);
//		copywalk(file1, file2);
		Files.copy(file1, file2);
		return this;
	}
	
	public NIO file2zip(Path file, Path zipfile, String zipfolder) throws IOException {
		check(file, File, zipfile, Zip);
		try (FileSystem fs = newFileSystem(zipfile)) {
//			copywalk(file, check(fs, zip, zipSubfolder).resolve(file.getFileName().toString()));
			Path folder = checkSubfolder(fs, zipfile, zipfolder);
			Files.copy(file, folder.resolve(file.getFileName().toString()));
		}
		return this;
	}

	public NIO filefromZip2folder(Path zipfile, String filepath, Path folder) throws IOException {
		try (FileSystem fs = newFileSystem(zipfile)) {
//			copywalk(file, check(fs, zip, zipSubfolder).resolve(file.getFileName().toString()));
			Path fileinzip = fs.getPath(filepath);
			check(zipfile, File, folder, Folder); // first check the existence of the zipfile
			check(fileinzip, File, folder, Folder);  // then the existence of the file in the zipfile
			Files.copy(fileinzip, folder.resolve(fileinzip.getFileName().toString()));
		}
		return this;
	}
	
	public NIO file2folder(String file, String folder) throws IOException { return file2folder(Paths.get(file), Paths.get(folder));	}
	public NIO file2folder(Path file, Path folder) throws IOException {
		check(file, File, folder, Folder);
//		copywalk(file, folder.resolve(file.getFileName()));
		Files.copy(file, folder.resolve(file.getFileName().toString()));
		return this;
	}

//	***** END: FILE OPERATIONS *****	
	
	
	

//	***** START: FOLDER OPERATIONS *****
	/**
	 * Copy contents of folder1 to folder2
	 */
	public NIO folder2folder(String folder1, String folder2) throws IOException { return folder2folder(Paths.get(folder1), Paths.get(folder2)); }
	public NIO folder2folder(Path folder1, Path folder2) throws IOException {
		check(folder1, Folder, folder2, Folder);
		copywalk(folder1, folder2);
		return this;
	}
	
	/**
	 * Copy contents of a folder to a zip
	 */
	public NIO folder2zip(String folder, String zipfile) throws IOException { return folder2zip(folder, zipfile, "/"); }
	public NIO folder2zip(Path folder, Path zipfile) throws IOException {
		return folder2zip(folder, zipfile, "/");
	}
	
	/**
	 * Copy contents of a folder to a zip-subfolder
	 * @param zipSubfolder valid example:&emsp;  /make/bin  &emsp;  IMPORTANT: starts with / but does end with /
	 */
	public NIO folder2zip(String folder, String zipfile, String zipSubfolder) throws IOException { return folder2zip(Paths.get(folder), Paths.get(zipfile), zipSubfolder); }
	public NIO folder2zip(Path folder, Path zipfile, String zipSubfolder) throws IOException {
		check(folder, Folder, zipfile, Zip);
		try (FileSystem fs = newFileSystem(zipfile)) { copywalk(folder, fs.getPath(zipSubfolder)); }
		return this;
	}
	
	
	/**
	 * Copy contents of a zip to a folder
	 */
	public NIO zip2folder(String zipfile, String folder) throws IOException { return zip2folder(zipfile, "/", folder); }
	public NIO zip2folder(Path zipfile, Path folder) throws IOException {
		return zip2folder(zipfile, "/", folder);
	}
	
	/**
	 * Copy contents of a zip-subfolder to a folder
	 * @param zipSubfolder valid example:&emsp;  /make/bin  &emsp;  IMPORTANT: starts with / but does end with /
	 */
	public NIO zip2folder(String zipfile, String zipSubfolder, String folder) throws IOException { return zip2folder(Paths.get(zipfile), zipSubfolder, Paths.get(folder)); }
	public NIO zip2folder(Path zipfile, String zipSubfolder, Path folder) throws IOException {
		check(zipfile, Zip, folder, Folder);
		try (FileSystem fs = newFileSystem(zipfile)) { copywalk(fs.getPath(zipSubfolder), folder); }
		return this;
	}
	
	
	/**
	 * Copy contents of zip1 to zip2
	 */
	public NIO zip2zip(String zipfile1, String zipfile2) throws IOException { return zip2zip(zipfile1, "/", zipfile2, "/");	}
	public NIO zip2zip(Path zipfile1, Path zipfile2) throws IOException { return zip2zip(zipfile1, "/", zipfile2, "/");	}
	/**
	 * Copy the contents of a subFolder of zipfile1 to a subfolder of zipfile2
	 * @param zipSubfolder valid example:&emsp;  /make/bin  &emsp;  IMPORTANT: starts with / but does end with /
	 */
	public NIO zip2zip(String zipfile1, String zip1Subfolder, String zipfile2, String zip2Subfolder) throws IOException { return zip2zip(Paths.get(zipfile1), zip1Subfolder, Paths.get(zipfile2), zip2Subfolder); }
	public NIO zip2zip(Path zipfile1, String zip1Subfolder, Path zipfile2, String zip2Subfolder) throws IOException {
		check(zipfile1, Zip, zipfile2, Zip);
		try (FileSystem fsZip1 = newFileSystem(zipfile1); FileSystem fsZip2 = newFileSystem(zipfile2)) { 
			copywalk(fsZip1.getPath(zip1Subfolder), fsZip2.getPath(zip2Subfolder));
		} // try-with-resources java.lang.AutoCloseable
		return this;
	}

//	***** END: FOLDER OPERATIONS *****
	
	
	/**
	 * Checks the existence of source and target. When missing creates them if possible or fails if not.
	 */
	private void check(Path source, Type sourceType, Path target, Type targetType) throws IOException {
		// catastrophic error when source is missing
		if (Files.exists(source) == false) message("Missing " + (sourceType == Folder ? "source folder: " : "file: ") + source);
		else if ((sourceType == File || sourceType == Zip) && Files.isDirectory(source)) message("Copy source is not a file but a directory: " + source);
		
		// recoverable error when target is missing
		if (Files.exists(target) == false) {
			if (targetType == Folder) Files.createDirectories(target);  // folder to be extracted to
			else if (targetType == Zip) Files.write(target, EmptyZip); // zipfile to be created
		}
		else if ((targetType == File || targetType == Zip) && Files.isDirectory(target)) message("Copy target is not a file but a directory: " + target);
	}
	

	/**
	 * Creates (if missing) and returns subfolder in zipfile
	 */
	private Path checkSubfolder(FileSystem fs, Path zip, String zipSubfolder) throws IOException {
		Path path = fs.getPath(zipSubfolder);
		if (Files.exists(path) == false) {
			info("Zip file ", zip, " does not contain subfolder: ", zipSubfolder);
			Files.createDirectories(path);
		}
		return path;
	}
	
	
	private FileSystem newFileSystem(Path path) throws IOException {
//		FileSystem fs = FileSystems.newFileSystem(zipfile, null); // would do the trick as well
		return FileSystems.newFileSystem(URI.create("jar:file:" + path), Collections.emptyMap());
	}
	
	
	public <T> NIO addAction(Action action, String... globPatterns) {
		actions.add(new ActionPatternWrapper(action, globPatterns));
		return this;
	}
	
	/**
	 * @see https://labs.consol.de/de/development/git/2017/02/22/gitignore.html
	 */
	public NIO addExcludes(Collection<String> globPattern) {
		excludes.addAll(globPattern);
		return this;
	}
	/**
	 * @see https://labs.consol.de/de/development/git/2017/02/22/gitignore.html
	 */
	public NIO setExcludes(Collection<String> globPattern) {
		excludes.clear();
		return addExcludes(globPattern);
	}
	/**
	 * @see https://labs.consol.de/de/development/git/2017/02/22/gitignore.html
	 */
	public NIO addExcludes(String... globPattern) {
		excludes.addAll(Arrays.asList(globPattern));
		return this;
	}
	/**
	 * @see https://labs.consol.de/de/development/git/2017/02/22/gitignore.html
	 */
	public NIO setExcludes(String... globPattern) {
		excludes.clear();
		return addExcludes(globPattern);
	}

	private static void message(String message) throws IOException {
		throw new IOException(message);
	}
}
