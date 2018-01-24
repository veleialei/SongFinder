package songfinder;

import java.io.File;

public class ParseArg {
	private String input;
	private String output;
	private String order;
	private String searchInput;
	private String searchOutput;
	private int threadNum;

	public ParseArg(String args[]) {
		try {
			valid(args);
		} catch (WrongInputException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Process the input see if there is any error
	 * 
	 * @param args
	 */
	public void valid(String[] args) throws WrongInputException {
		boolean findInput = false;
		boolean findOutput = false;
		boolean findOrder = false;
		boolean isSearch = false;
		this.threadNum = 1;
		if (args.length % 2 != 0) {
			throw new WrongInputException("wrong arguments");
		} else {
			for (int i = 0; i < args.length - 1; i += 2) {
				if (args[i].equals("-input")) {
					findInput = true;
					if (new File(args[i + 1]).isDirectory()) {
						this.input = args[i + 1];
					} else {
						throw new WrongInputException("wrong input");
					}
				} else if (args[i].equals("-output")) {
					findOutput = true;
					this.output = args[i + 1];
				} else if (args[i].equals("-order")) {
					findOrder = true;
					if ((args[i + 1].equals("artist") || args[i + 1].equals("title") || args[i + 1].equals("tag"))) {
						this.order = args[i + 1];
					} else {
						throw new WrongInputException("wrong order");
					}
				} else if (args[i].equals("-threads")) {
					this.threadNum = Integer.parseInt(args[i + 1]);
				} else if (args[i].equals("-searchInput")) {
					this.searchInput = args[i + 1];
				} else if (args[i].equals("-searchOutput")) {
					this.searchOutput = args[i + 1];
				} else if (args[i].equals("-aim") && args[i + 1].equals("search")) {
					isSearch = true;
				}
			}
			if (isSearch == false) {
				if (findOrder == false || findOutput == false || findInput == false) {
					throw new WrongInputException("wrong arguments");
				}
			}
		}
	}

	/**
	 * Return the output path of songs
	 * 
	 * @return
	 */
	public String getOutputPath() {
		return this.output;
	}

	/**
	 * Return the input path of songs
	 * 
	 * @return
	 */
	public String getInputPath() {
		return this.input;
	}

	/**
	 * Return the order of songs
	 * 
	 * @return
	 */
	public String getOrder() {
		return this.order;
	}

	public int getThreadNum() {
		return threadNum;
	}

	public String getSearchInput() {
		return searchInput;
	}

	public String getSearchOutput() {
		return searchOutput;
	}

	public class WrongInputException extends Exception {
		private String message;

		public WrongInputException(String message) {
			this.message = message;
		}

		public String getAmount() {
			return message;
		}
	}
}
