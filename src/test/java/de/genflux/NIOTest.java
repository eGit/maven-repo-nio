package de.genflux;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;


/**
 * Testcase for the NIO
 *
 */
public class NIOTest {
    
    private static final String file = "main/java/de/genflux/NIO.java";

	@Rule public TestName name = new TestName();
    
    private static final String s = File.separator;
    private static Path srcFolder = Paths.get("./src");
    private static Path out;
	private static Path start;
	private static NIO nio;

    
    @BeforeClass
    public static void setUp() throws IOException {
    	out = Files.createTempDirectory("output");
		start = out.resolve("start.zip");
		nio = NIO.info();
		nio.folder2zip(srcFolder, start);
		assertTrue(exists(start));
    }
    
    @AfterClass
    public static void tearDown() throws IOException {
//    	delete(out);
    }
    
    private Path getTestZip(String name) throws IOException {
    	Path testZip = out.resolve(name + ".zip");
		Files.copy(start, testZip);
    	assertTrue(exists(testZip));
    	return testZip;
	}
    
    private String[] getArgs(String path) {
    	return new String[] {path, s + path, path + s, s + path + s};		
	}
    
    @Test
    public void file2file() throws IOException {
    	Path file2 = out.resolve(name.getMethodName() + ".zip");
		nio.file2file(start, file2);
		assertTrue(exists(file2) && Files.isDirectory(file2) == false);
    }
    
    @Test
    public void file2zip() throws IOException {
    	String sub = "sub";
    	String[] args = new String[] {sub, s + sub, sub + s, s + sub + s};
    	for (int i = 0; i < args.length; i++) {
    		Path testZip = getTestZip(name.getMethodName() + i);
    		nio.file2zip(start, testZip, args[i]);
    		assertTrue(existsInZip(testZip, sub + s + start.getFileName()));
		}
    }
    
    @Test
    public void filefromZip2Zip() throws IOException {
    	Path emptyZip = out.resolve(name.getMethodName() + ".zip");
		nio.filefromZip2zip(start, file, emptyZip);
    	assertTrue(existsInZip(emptyZip, file));
    	
    	String[] args = getArgs(file);
    	for (int i = 0; i < args.length; i++) {
    		String filepath2 = s + i + s + i + ".java";
			nio.filefromZip2zip(start, args[i], emptyZip, filepath2);
			assertTrue(existsInZip(emptyZip, filepath2));
    	}
    }
    
    @Test
    public void filefromZip2folder() throws IOException {
    	String[] args = getArgs(file);
    	for (int i = 0; i < args.length; i++) {
    		Path folder = out.resolve(name.getMethodName() + i);
    		nio.filefromZip2folder(start, args[i], folder);
    		assertTrue(exists(folder.resolve("NIO.java")));
    	}
    }
    
    @Test
    public void file2folder() throws IOException {
		Path folder = out.resolve(name.getMethodName());
		nio.file2folder(start, folder);
		assertTrue(exists(folder.resolve(start.getFileName())));
    }
    
    @Test
    public void folder2folder() throws IOException {
    	Path folder = out.resolve(name.getMethodName());
		nio.folder2folder(srcFolder, folder);
		assertTrue(exists(folder.resolve(file)));
    }
    
    @Test
    public void folder2zip() throws IOException {
    	Path zip = out.resolve(name.getMethodName() + ".zip");
		nio.folder2zip(srcFolder, zip);
		assertTrue(existsInZip(zip, file));
		
		String[] args = getArgs("just/for/fun");
		for (int i = 0; i < args.length; i++) {
			Path testZip = getTestZip(name.getMethodName() + i);
			String zipSubFolder = args[i];
			nio.folder2zip(srcFolder, testZip, zipSubFolder);
			assertTrue(existsInZip(testZip, zipSubFolder + s + file));
		}
    }
    
    @Test
    public void zip2folder() throws IOException {
		Path folder = out.resolve(name.getMethodName());
		nio.zip2folder(start, folder);
		assertTrue(exists(folder.resolve(file)));
		
		String zipSubfolder = "main/java";
		String[] args = getArgs(zipSubfolder);
		for (int i = 0; i < args.length; i++) {
			folder = out.resolve(name.getMethodName() + i);
			nio.zip2folder(start, args[i], folder);
			assertTrue(exists(folder.resolve("de/genflux/NIO.java")));
		}
    }
    
    @Test
    public void zip2zip() throws IOException {
		Path testZip = getTestZip(name.getMethodName());
		nio.zip2zip(start, testZip);
		assertTrue(existsInZip(testZip, file));
		
		String zip1Subfolder = "main/java";
		String[] args1 = getArgs(zip1Subfolder);
		String zip2Subfolder = "just/for/fun";
		String[] args2 = getArgs(zip2Subfolder);
		
		for (int i = 0; i < args1.length; i++) {
			for (int j = 0; j < args2.length; j++) {
				testZip = getTestZip(name.getMethodName() + i + "-" + j);
				nio.zip2zip(start, args1[i], testZip, args2[j]);
				assertTrue(existsInZip(testZip, args2[j] + s + "de/genflux/NIO.java"));
			}
		}
    }

    
    private static boolean exists(Path file) {
		return Files.exists(file);
	}
    
	/**
	 * Checks if file or folder exists in zip file 
	 */
	private boolean existsInZip(Path zipfile, String fileInZip) throws IOException {
		try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:" + zipfile), Collections.emptyMap())) {
			return Files.exists(fs.getPath(fileInZip));
		} // try-with-resources java.lang.AutoCloseable
	}
	
	
	private void delete(Path pathToBeDeleted) throws IOException {
    	Files.walk(pathToBeDeleted)
    	.sorted(Comparator.reverseOrder())
    	.map(Path::toFile)
    	.forEach(File::delete);

    	assertFalse("Directory still exists", Files.exists(pathToBeDeleted));
    }

}
