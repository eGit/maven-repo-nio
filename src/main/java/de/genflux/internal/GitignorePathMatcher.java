package de.genflux.internal;


import static java.nio.file.FileVisitResult.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.jgit.ignore.FastIgnoreRule;

public class GitignorePathMatcher {

	
	public static void main(String[] args) throws IOException {
		Path start = Paths.get("/mnt/Data/tools/gradle-init-java-project");
		List<String> lines = Files.readAllLines(start.resolve(".gitignore"));
		for (ListIterator<String> iterator = lines.listIterator(); iterator.hasNext();) {
			String line = iterator.next();
			String trim = line.trim();
			if (trim.length() == 0 || trim.contains("#")) iterator.remove();
			else iterator.set(trim);
		}
		
		// add additional filter entry not defined in .gitIgnore
		lines.add("/init");
		
		print(lines);
		System.out.println();
		
		filter(lines, start);
	}

	private static void filter(List<String> filters, Path start) throws IOException {
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				boolean include = include(dir, filters);
				if (!include) System.out.println("[SKIP] " + dir);
//				System.out.println((include ? "ADD " : "[SKIP] ") + dir);
				return include ? CONTINUE : SKIP_SUBTREE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				boolean include = include(file, filters);
				if (!include) System.out.println("[SKIP] \t\t" + file);
//				System.out.println((include ? "ADD \t" : "[SKIP] \t") + file);
				return CONTINUE;
			}
			
			boolean include(Path dir, List<String> filters) {
				for (String pattern : filters) {
					boolean isMatch = new FastIgnoreRule(pattern).isMatch(start.relativize(dir).toString(), Files.isDirectory(dir));
					if (isMatch) return false;
				}
				return true;
			}
		});
	}
	

	private static void print(List<String> lines) {
		for (String line : lines) System.out.println(line);
	}
	
}
