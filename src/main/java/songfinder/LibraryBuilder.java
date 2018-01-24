package songfinder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import utility.WorkQueue;

/**
 * LibraryBuilder class to help build the LibraryData.
 * 
 * @author shu16
 */
public class LibraryBuilder {
	private List<File> files;
	private SongsLibrary songLib;
	private int threadNums;

	/**
	 * Constructor of LibraryBuilder
	 * 
	 * @param input
	 */
	public LibraryBuilder(String input, int threadNums) {
		this.threadNums = threadNums;
		this.files = getFiles(input);
		this.songLib = new SongsLibrary();
	}

	/**
	 * Return list of song files
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List<File> getFiles(String input) {
		List<File> files = new ArrayList<File>();
		Path path = Paths.get(input);
		// walk the file tree and get a Stream of Path
		try (Stream<Path> paths = Files.walk(path)) {
			// use aggregate operation forEach to process each file
			paths.forEach(p -> processPath(p, files));
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return files;
	}

	public static void processPath(Path p, List<File> files) {
		if (p.toString().toLowerCase().endsWith(".json")) {
			files.add(p.toFile());
		}
	}

	/**
	 * return a LibraryData
	 * 
	 * @return
	 */
	public SongsLibrary getSongsLibrary() {
		return this.songLib;
	}

	/**
	 * loadData to Library
	 * 
	 * @return
	 */
	public void loadSongData() {
		WorkQueue wq = new WorkQueue(threadNums);
		// call the work queue to process all file
		for (File file : files) {
			wq.execute(new SongWorker(file));
		}
		wq.shutdown();
		wq.awaitTermination();
	}

	class SongWorker implements Runnable {
		File file;

		public SongWorker(File file) {
			this.file = file;
		}

		@Override
		public void run() {
			SongData song = new SongData(file);
			songLib.addSong(song.getData());
		}
	}
}
