package de.genflux.internal;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class MinimalBug {

	public static void main(String[] args) throws IOException {
		Path zipfile = Paths.get("empty.zip").toAbsolutePath();
		Files.write(zipfile, new byte[] { 80, 75, 05, 06, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00 });
		
		Path file = Paths.get("file.txt");
		Files.write(file, Collections.singleton("Hello!"));
		
		
		try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:" + zipfile), Collections.emptyMap())) {
			Path path1 = fs.getPath("/file1");
			Path path2 = fs.getPath("/file2/");
			Path path3 = fs.getPath("/file1.txt");
			Path path4 = fs.getPath("/file2.txt/");
			
			System.out.println(path1 + "\t\t" + Files.isDirectory(path1));
			System.out.println(path2 + "\t\t" + Files.isDirectory(path2));
			System.out.println(path3 + "\t"   + Files.isDirectory(path3));
			System.out.println(path4 + "\t"   + Files.isDirectory(path4));
			
			Files.copy(file, path1);  // -> a file   is created in the zip filesystem 
			Files.copy(file, path2);  // -> a FOLDER is created in the zip filesystem 
			Files.copy(file, path3);  // -> a file   is created in the zip filesystem 
			Files.copy(file, path4);  // -> a file   is created in the zip filesystem
			
		}
	}
	
}
