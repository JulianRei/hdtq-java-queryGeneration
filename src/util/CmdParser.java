package util;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import enums.CmdOption;

public class CmdParser {
	
	private static String[] args = null;
	private static Options options = new Options();
	private static CommandLine commandLine = null;
	
	public static void setArgs(String[] args) {
		CmdParser.args = args;
	}
	
	public static CommandLine getCommandLine() throws IllegalArgumentException {
		if(args == null) {
			throw new IllegalArgumentException("Cannot create CommandLineParser, call setArgs() first");
		}
		if(commandLine == null) {
			commandLine = parseOptions();
		}
		return commandLine;
	}
	
	public static CommandLine parseOptions() {
		Option option;
		for (CmdOption opt : CmdOption.values()) {
			option = new Option(opt.shortOption, opt.longOption, opt.hasArg, opt.description);
			option.setRequired(opt.required);
			options.addOption(option);
		}

		@SuppressWarnings("deprecation")
		org.apache.commons.cli.CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption(CmdOption.HELP.shortOption)) {
				help();
			}
			return cmd;
		} catch (ParseException e) {
			System.err.println("Parsing failed.  Reason: " + e.getMessage());
			help();
			System.exit(-1);
		}
		return null; // this will never be reached
	}
	
	/**
	 * Print help and exit
	 */
	private static void help() {
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", options);
		System.exit(0);
	}
}
