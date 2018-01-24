package songfinder;

/**
 * Main class for SongFinder lab and projects.
 * 
 * @author shu16
 *
 */

public class Driver {

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {
		try {
			ParseArg parser = new ParseArg(args);
			LibraryBuilder builder = new LibraryBuilder(parser.getInputPath(), parser.getThreadNum());
			builder.loadSongData();
			SongsLibrary mySongLib = builder.getSongsLibrary();
			mySongLib.outputFile(parser.getOutputPath(), parser.getOrder());
			mySongLib.search(parser.getSearchInput(), parser.getSearchOutput());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
