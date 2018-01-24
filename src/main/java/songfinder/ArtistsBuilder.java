package songfinder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import servlets.HTTPFetcher;
import utility.WorkQueue;

public class ArtistsBuilder {
	private List<File> files;
	private ArtistsLibrary artistLib;
	private Set<String> artists;
	private List<String> empty = new ArrayList<String>();
	private final static String APIKEY = "f44b253dc0ce2c849354d7b4fe67bc59";
	private final static String input = "artists/";
	// private List<String> temp1; //for process file
	public ArtistsBuilder(Set<String> artists) {
		this.artistLib = new ArtistsLibrary();
		this.artists = artists;

		this.files = getFiles();
		// List<String> temp =new LinkedList<String>(artists);
		// temp1 = temp.subList(3900, temp.size());
	}

	/**
	 * get the artist file list locally
	 * 
	 * @return
	 */
	public List<File> getFiles() {
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

	public void processPath(Path p, List<File> files) {
		if (p.toString().toLowerCase().endsWith(".json")) {
			files.add(p.toFile());
		}
	}

	/**
	 * return a LibraryData
	 * 
	 * @return
	 */
	public ArtistsLibrary getArtistsLibrary() {
		return this.artistLib;
	}

	/**
	 * loadData to Library
	 * 
	 * @return
	 */
	public void loadArtistData() {
		WorkQueue wq = new WorkQueue(10);
		// call the work queue to process all file
		for (File file : files) {
			
			wq.execute(new ArtistWorker(file));
			
		}
		wq.shutdown();
		wq.awaitTermination();
	}

	/**
	 * worker to create artist data
	 */
	class ArtistWorker implements Runnable {
		File file;

		public ArtistWorker(File file) {
			this.file = file;
		}

		@Override
		public void run() {
			ArtistData a = new ArtistData(file);
			JsonObject obj = a.getData();
			if (obj != null && obj.size()>0) {
				artistLib.addArtist(obj);
			}
		}
	}

	/**
	 * loadData to Library
	 * 
	 * @return
	 */
	public void writeArtistData() {
		Path outPutPath = Paths.get("artists/aa.json");
		outPutPath.getParent().toFile().mkdirs();
		WorkQueue wq = new WorkQueue(10);
		// call the work queue to process all file

		for (String artist : artists) {
			wq.execute(new FileWorker(artist));
		}
		wq.shutdown();
		wq.awaitTermination();
	}

	/**
	 * worker to fetch data from lastfm
	 */
	class FileWorker implements Runnable {
		String name;

		public FileWorker(String name) {
			this.name = name;
		}

		@Override
		public void run() {
			String url = "/2.0/?method=artist.getinfo&artist=" + name.toLowerCase() + "&api_key=" + APIKEY
					+ "&format=json";
			String page = HTTPFetcher.download("ws.audioscrobbler.com", url);
			int id = page.indexOf("{");
			if (id == -1) {
				empty.add(name);
				return;
			}
			String raw = page.substring(id);
			Path outPutPath = Paths.get("artists/" + id++ + ".json");
			System.out.println(name);
			try (Writer output = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outPutPath.toString()), "UTF-8"))) {
				output.write(raw);

			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
}
