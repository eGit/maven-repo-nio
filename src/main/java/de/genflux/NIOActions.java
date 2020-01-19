package de.genflux;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class NIOActions {

	@FunctionalInterface
	public interface Action {
		/**
		 * @throws IOException return null if no change took place
		 */
		public String perform(Path file, List<String> lines, String line);
	}

	static class ReplaceAction implements Action {
		final String marker;
		final String with;

		public ReplaceAction(String marker, String replacewith) {
			this.marker = marker;
			this.with = replacewith;
		}

		@Override
		public String perform(Path file, List<String> lines, String line) {
			return line.contains(marker) ? line.replace(marker, with) : line;
		}
	}
	
	static class AddToLineAction implements Action {
		private String marker;
		private String tobeadded;
		private boolean tothefront;
		public AddToLineAction(String marker, String tobeadded, boolean tothefront) {
			this.marker = marker;
			this.tobeadded = tobeadded;
			this.tothefront = tothefront;
		}
		@Override
		public String perform(Path file, List<String> lines, String line) {
			return line.contains(marker) ? (tothefront ? tobeadded + line : line + tobeadded) : line;
		}
	}

}
