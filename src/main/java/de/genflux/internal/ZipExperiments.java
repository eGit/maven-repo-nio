package de.genflux.internal;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class ZipExperiments {

	public static void main(String[] args) throws IOException {
		Path zipfile = Paths.get("empty.zip").toAbsolutePath();
		Files.write(zipfile, new byte[] { 80, 75, 05, 06, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00 });
		
		Path file = Paths.get("file.txt");
		Files.write(file, Collections.singleton("Hello!"));
		
		System.out.println(zipfile.toUri());
		FileSystem fs = FileSystems.newFileSystem(zipfile, null);
		
		Path path = fs.getPath("/this/is/a/test");
		Files.createDirectories(path);
		Files.copy(file, path.resolve("hello.txt"));

		System.out.println(fs);
		System.out.println(fs.getClass());
		System.out.println(fs.getFileStores());
		System.out.println(fs.getRootDirectories());
		
		fs.close(); // !!! IMPORTANT !!!
	}
	
}
