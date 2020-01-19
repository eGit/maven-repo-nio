package de.genflux;

import static java.nio.file.FileVisitResult.*;
import static java.nio.file.StandardCopyOption.*;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.jgit.ignore.FastIgnoreRule;

import de.genflux.NIOActions.Action;

abstract class NIOA {
	
	protected final List<String> excludes;
	protected final List<ActionPatternWrapper> actions;
	protected boolean debug = false;
	protected boolean info = false;
//	{debug = info = true; System.err.println("debug set to true");}

	protected NIOA() {
		excludes = new ArrayList<>();
		actions = new ArrayList<>();
	}

	class ActionPatternWrapper {
		private Action action;
		private String[] patterns;
		ActionPatternWrapper(Action action, String... patterns) {
			this.action = action;
			this.patterns = patterns;
		}
		boolean match(Path file) {
			FileSystem fs = file.getFileSystem();
			for (String pattern : patterns) if (fs.getPathMatcher("glob:" + pattern).matches(file.getFileName())) return true;
			return false;
		}
	}

	protected void copywalk(Path source, Path target) throws IOException {
		Files.walkFileTree(source, new SimpleFileVisitor<Path>() { // extract FileVisitor to FilteredActionFileVisitor: excludes, actions
			@Override
			public FileVisitResult preVisitDirectory(final Path dir, BasicFileAttributes attrs) throws IOException {
				final Path out = targetPath(target, source, dir, true);
				boolean indluce = include(dir);
				if (indluce) Files.createDirectories(out);
				return debugX(indluce ? CONTINUE : SKIP_SUBTREE, indluce ? "\n[MKDIR]" : "\n[SKIP] ", out, " <- from: ", dir);
			}
			@Override
			public FileVisitResult visitFile(final Path file, BasicFileAttributes attrs) throws IOException {
				final Path out = targetPath(target, source, file, false);
				boolean include = include(file);
				if (include && !applyActions(file, out)) Files.copy(file, out, REPLACE_EXISTING); // move inside applyActions!
				return debugX(CONTINUE, include ? "\t[COPY] " : "\t[SKIP] ", out, " <- from: ", file);
			}

			boolean include(Path path) {
				for (String pattern : excludes) {
					boolean isMatch = new FastIgnoreRule(pattern).isMatch(source.relativize(path).toString(), Files.isDirectory(path));
					if (isMatch) {
						info("[SKIP] ", path);
						return false;
					}
				}
				return true;
			}
		});
	}

	Path targetPath(Path target, Path source, Path path, boolean isDirectory) {
//		DIESER WORKAROUND WUERDE ES ERLAUBEN NICHT NUR ORDNER SONDERN AUCH DATEIEN ZU KOPIEREN:
//		if (source == path) { // dies passiert wenn source eine Datei ist
//			Verhalten von Files.copy(source, target):
//			1. target: /ordner/file      -> file wird als Datei im Zipfile angelegt
//			2. target: /ordner/file.txt/ -> file wird als Datei im Zipfile angelegt
//			3. target: /ordner/file/     -> file wird als Ordner im Zipfile angelegt !!!
//			Bei file2zip ist in dieser Methode source == path und source.relativize(path) ein leerer String. 
//			Dadurch wird also target = /ordner/file durch target.resolve("") zu /ordner/file/ und dadurch wird ein Ordner im Zipfile angelegt
//			String relativize = source.relativize(path).toString();
//			Path resolve = relativize.isEmpty() ? target : target.resolve(relativize);
//		}

//		FOR DEBUGGING!
//		System.out.println("source: " + source);
//		System.out.println("target: " + target);
//		System.out.println("path: " + path);
//		System.out.println("source.relativize(path): " + source.relativize(path));
		Path resolve = target.resolve(source.relativize(path).toString());
//		System.out.println("resolve: " + resolve);
//		System.out.println();
		
		if (isDirectory == false) {
			String res = resolve.toString();
			if (res.length() != 1 && res.endsWith("/")) throw new RuntimeException(res);
		}
		
		return resolve;
	}
	
	boolean applyActions(Path file, Path out) throws IOException {
		if (actions.size() == 0) return false;
		List<Action> filtered = new ArrayList<>();
		for (ActionPatternWrapper ap : actions) if (ap.match(file)) filtered.add(ap.action);
		if (filtered.isEmpty()) return false;
		info("[PATTERN]", file);
		// at least on pattern machted - apply actions
		List<String> lines = Files.readAllLines(file);
		for (ListIterator<String> iterator = lines.listIterator(); iterator.hasNext();) {
			final String original = iterator.next(); // DO NOT INLINE!!!
			String line = original;
			for (Action action : filtered) line = action.perform(file, lines, line);
			if (line != original) iterator.set(line); // if all actions return the !identical! line-object we can use identity comparison
		}
		Files.write(out, lines); // without StandardOpenOption replaces existing file
//		Files.copy(new ByteArrayInputStream(String.join("\n", lines).getBytes()), path, StandardCopyOption.REPLACE_EXISTING);
		info("\t[ACTION]", out, "\n");
		
		return true;
	}



	void print(Object... messages) {
		StringBuilder sb = new StringBuilder();
		for (Object message : messages) {
			sb.append(message);
			sb.append(" ");
		}
		System.out.println(sb.toString());
	}
	void debug(Object... messages) {
		if (debug) print(messages);
	}
	private <T> T debugX(T returnValue, Object... messages) {
		debug(messages);
		return returnValue;
	}
	void info(Object... messages) {
		if (info) print(messages);
	}
	private <T> T infoX(T returnValue, Object... messages) {
		info(messages);
		return returnValue;
	}

	private static void fsInfo(FileSystem... fileSystem) {
		for (FileSystem fs : fileSystem) {
			System.out.println("fileSystem: " + fileSystem.getClass().getName() + "\t" + (fs.isReadOnly() ? "ReadOnly" : "Read/Write"));

			fs.getFileStores().forEach((FileStore filestore) -> System.out.println("name: " + filestore.name()));
			fs.getRootDirectories().forEach((Path root) -> System.out.println("root: " + root));

			System.out.println();
		}
	}

}
