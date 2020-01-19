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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Testcase for the NIO
 *
 */
public class NIOTest {
    public static final String RESULT_FILESET = "result";
	
    private Path out;

    @Before
    public void setUp() throws IOException {
    	out = Files.createTempDirectory("output");
    }
    
    @After
    public void tearDown() throws IOException {
//    	delete(out);
    }
    
    /**
     * Test file/folder/zip copy operations 
     * @throws IOException 
     */
    @Test
    public void testCopyOperations() throws IOException {
    	Path srcFolder = Paths.get("./src");
		Path start = out.resolve("start.zip");
		Path file1 = out.resolve("file1");
		Path folder1 = out.resolve("folder1");
		Path folder2 = out.resolve("folder2");
		Path zip1 = out.resolve("zip1.zip");
		Path folder3 = out.resolve("folder3");
		Path zip2 = out.resolve("zip2.zip");
		
		NIO nio = NIO.info(); // debug();
		nio.folder2zip(srcFolder, start);
		assertTrue(exists(start));
		
		nio.file2file(start, file1);
		assertTrue(exists(file1));
		
		nio.file2zip(file1, start, "sub");
		assertTrue(existsInZip(start, "sub/file1"));
		
		String NIO = "main/java/de/genflux/NIO.java";
		nio.filefromZip2folder(start, NIO, out);
		assertTrue(exists(out.resolve("NIO.java")));
		
		nio.file2folder(file1, folder1);
		assertTrue(exists(folder1.resolve(file1)));
		
		nio.folder2folder(srcFolder, folder2);
		assertTrue(exists(folder2.resolve(NIO)));
		
		nio.folder2zip(folder1, zip1);
		assertTrue(existsInZip(zip1, "file1"));
		
		String zipSubfolder = "just/for/fun";
		nio.folder2zip(folder2, zip2, zipSubfolder);
		assertTrue(existsInZip(zip2, zipSubfolder + "/" + NIO));
		
		nio.zip2folder(zip1, folder3);
		assertTrue(exists(folder3.resolve("file1")));
		
		System.err.println("WARNING: fix this problem: " + this.getClass());
		nio.zip2folder(start, "/main/java", folder3); // does not work without the starting slash /
		assertTrue(exists(folder3.resolve("de/genflux/NIO.java")));

		nio.zip2zip(zip1, zip2);
		assertTrue(existsInZip(zip2, "file1"));
		
		System.err.println("WARNING: fix this problem: " + this.getClass());
		nio.zip2zip(zip2, "/just/for", start, "main");  // does not work without the starting slash /
		assertTrue(existsInZip(start, "main/fun/" + NIO));
    }

    
    private boolean exists(Path file) {
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
