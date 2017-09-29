package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.commons.cli.CommandLine;
import org.apache.log4j.varia.NullAppender;
import org.json.simple.parser.ParseException;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import enums.CmdOption;
import enums.HDTType;
import exceptions.NoMoreRandomNumbersException;
import exceptions.NotEnoughPossibilitiesException;
import exceptions.ZeroPossibilitiesException;
import util.CmdParser;
import util.ComponentQuad;
import util.Logger;
import util.Writer;

public class Go {
	
	private static String PATH_TO_FOLDER;
	public static int MIN_NUMBER_OF_QUERIES;
	private static boolean OVERWRITE;
	private static boolean CHECK_OTHER_SYSTEMS;
	
	private static boolean DOUBLE_CHECK_HDT; 
	private static boolean CREATE_SPECIAL_PATTERNS;
	public static HDTType HDT_TYPE;
	
	private static final String[] SPECIAL_PATTERNS = new String[]{"?P??S", "?P??L"};
	private static final int SPECIAL_BORDER = 100; // below and equal --> S, above --> L
	
	public static void main(String[] args) throws IOException, ParseException {
		org.apache.log4j.BasicConfigurator.configure(new NullAppender());
		CmdParser.setArgs(args);
		parseOptions();
		HashSet<ComponentQuad> queries;
		QueryTester.init(PATH_TO_FOLDER);
		QueryTester.loadVirtuoso();
		QueryTester.loadHDT();
		
		String[] patterns = getPatterns();
		if(patterns[0] == null) {
			System.out.println("All query files exist already");
		} else {
			for(String pattern: patterns) {
				if(pattern != null) {
					queries = generateQueries(pattern);
					saveQueries(queries, pattern);
				}
			}
		}
	}

	private static String[] getPatterns() {
		String[] patterns = new String[16+SPECIAL_PATTERNS.length];
		int i = 0;
		for(String subject: new String[] {"S","?"}) {
			for(String predicate: new String[] {"P","?"}) {
				for(String object: new String[] {"O","?"}) {
					for(String graph: new String[] {"G","?"}) {
						String pattern = subject + predicate + object + graph;
						File f = new File(getFilePath(pattern));
						if(!f.exists() || OVERWRITE) {
							patterns[i] = pattern;
							i++;
						}
					}
				}
			}
		}
		if(CREATE_SPECIAL_PATTERNS) {
			for(String pattern: SPECIAL_PATTERNS) {
				File f = new File(getFilePath(pattern));
				if(!f.exists() || OVERWRITE) {
					patterns[i] = pattern;
					i++;
				}
			}
		}
		return patterns;
	}
	
	private static String getFilePath(String pattern) {
		return PATH_TO_FOLDER + File.separator + "queries" + File.separator + pattern.replaceAll("\\?", "V") + ".txt";
	}
	
	private static void saveQueries(HashSet<ComponentQuad> queries, String pattern) throws IOException, ParseException {
		String outputFile = getFilePath(pattern);
		new File(outputFile).delete();
		Writer.writeToFile(ComponentQuad.getJSONString(queries), outputFile, false);
		// make sure that if we read the queries they are exactly the same
		ArrayList<ComponentQuad> list = Writer.readQueriesFromFile(outputFile);
		for(ComponentQuad item : list) {
			if(!queries.contains(item)) {
				throw new RuntimeException("Item is not included!" + item);
			}
		}
	}
	
	private static HashSet<ComponentQuad> generateQueries(String pattern) throws IOException {
		Logger.log("current pattern: " + pattern);
		ComponentQuad compQuad = new ComponentQuad();
		
		HashSet<ComponentQuad> queries = new HashSet<ComponentQuad>();
		// handle special case of ????
		if(pattern.equals("????")) {
			queries.add(compQuad);
			return queries;
		}
		
		Logger.log("Searching for unique quads and adding to HashSet for given pattern: " + pattern);
		int numberOfZeroPossibilitiesExceptions = 0;
		int numberofResultsDoNotMatch = 0;
		int numberOfAlreadyIdentifiedAsAQuery = 0;
		int numberOfAlreadyIdentifiedAsACandidate = 0;
		int iterations = 0;
		RandomQuadRetriever rnd;
		HashSet<ComponentQuad> candidates = new HashSet<ComponentQuad>();
		try {
			rnd = new RandomQuadRetriever(pattern);
			while(queries.size() < MIN_NUMBER_OF_QUERIES) {
				Logger.log("Iterations: " + ++iterations);
				Logger.log("Retrieving next quad from Virtuoso, pattern: " + pattern);
				Logger.log("Number of ZeroPossibilitiesExceptions: " + numberOfZeroPossibilitiesExceptions);
				Logger.log("Number of ResultsDoNotMatch: " + numberofResultsDoNotMatch);
				Logger.log("Number of AlreadyIdentifiedAsAQuery: " + numberOfAlreadyIdentifiedAsAQuery);
				Logger.log("Number of numberOfAlreadyIdentifiedAsACandidate: " + numberOfAlreadyIdentifiedAsACandidate);
				
				rnd.reset(); // reset componentquad of RandomQuadRetriever
				
				ComponentQuad quad = null;
				try {
					quad = rnd.getNextRandomQuad();
				} catch(ZeroPossibilitiesException e) {
					System.out.println("Caught ZeroPossibilitiesException, trying to find another solution");
					numberOfZeroPossibilitiesExceptions++;
					continue;
				}
				
				if(candidates.contains(quad)) {
					System.out.println("This was already identified as a candidate, trying to find another solution");
					numberOfAlreadyIdentifiedAsACandidate++;
					continue;
				}
	
				if(queries.contains(quad)) {
					Logger.log("This is already identified as a query, trying to find another solution");
					numberOfAlreadyIdentifiedAsAQuery++;
					continue;
				}
				//Logger.log("This is not identified as a query, now checking if this is a one of the special patterns");
				// in case of special query, check if number of quads are ok
				if(contains(SPECIAL_PATTERNS, pattern)) {
					Logger.log("This is a special pattern case");
					try {
						if(pattern.endsWith("S") && !resultsAreLessOrEqualTo(SPECIAL_BORDER, quad)) {
							Logger.log("Pattern ends with S but there are too many results, continuing with the next item from the arraylist");
							continue;
						}
						if(pattern.endsWith("L") && resultsAreLessOrEqualTo(SPECIAL_BORDER, quad)) {
							Logger.log("Pattern ends with L but there are too less results, continuing with the next item from the arraylist");
							continue;
						}
					} catch (NotFoundException e) {
						System.out.println("Did not find that in HDT dictionary, continuing with the next candidate...");
						continue;
					}
					
				}
				
				// ensure all systems have the same number of results and min 1 result
				//Logger.log("Now ensuring that there is at least 1 result and all systems report same number of results");
				boolean queryHasResults = true;
				if(CHECK_OTHER_SYSTEMS) {
					queryHasResults = QueryTester.allHaveSameNumberOfResults(quad, DOUBLE_CHECK_HDT);
				}
				
				if(queryHasResults) {
					queries.add(quad);
					Logger.log("Found: " + queries.size());
				} else {
					Logger.log("Numbers of results do not match");
					numberofResultsDoNotMatch++;
				}
			}
		} catch(NotEnoughPossibilitiesException e) {
			System.out.println("caught NotEnoughPossibilitiesException");
			HashSet<ComponentQuad> possibleQueries = QueryTester.virtuosoTest.getAllElements(pattern);
			for(ComponentQuad possibleQuery : possibleQueries) {
				if(CHECK_OTHER_SYSTEMS) {
					if(QueryTester.allHaveSameNumberOfResults(possibleQuery, DOUBLE_CHECK_HDT)) {
						queries.add(possibleQuery);
					}
				} else {
					queries.add(possibleQuery);
				}
			}
		} catch(NoMoreRandomNumbersException e) {
			System.out.println("caught NoMoreRandomNumbersException");
		}
		if(queries.size() < MIN_NUMBER_OF_QUERIES) {
			Logger.log("*** WARNING: there are not enough combinations for " + pattern + ", only " + queries.size() + " combinations found ***");
		}
		return queries;
	}

	private static boolean resultsAreLessOrEqualTo(int specialBorder, ComponentQuad componentQuad) throws NotFoundException {
		IteratorTripleString it = QueryTester.hdtHDT(HDT_TYPE).search(componentQuad.getSubject().getForHDT(), componentQuad.getPredicate().getForHDT(), componentQuad.getObject().getForHDT(), componentQuad.getGraph().getForHDT());
		switch(it.numResultEstimation()) {
		case EXACT:
			return it.estimatedNumResults() <= specialBorder;
		case UP_TO:
			if(it.estimatedNumResults() <= specialBorder) {
				return true;
			}
			break;
		case MORE_THAN:
			if(it.estimatedNumResults() + 1 > specialBorder) {
				return false;
			}
			break;
		case EQUAL_OR_MORE_THAN:
			if(it.estimatedNumResults() > specialBorder) {
				return false;
			}
			break;
		}

		int results = 0;
		while(it.hasNext()) {
			it.next();
			results++;
			if(results > specialBorder) {
				return false;
			}
		}
		return true;
	}

	private static boolean contains(String[] haystack, String needle) {
		for(String string : haystack) {
			if(needle.equals(string)) {
				return true;
			}
		}
		return false;
	}
	
	private static void parseOptions() {
		CommandLine cmd = CmdParser.getCommandLine();
		PATH_TO_FOLDER = cmd.getOptionValue(CmdOption.FOLDER.shortOption);
		MIN_NUMBER_OF_QUERIES = Integer.parseInt(cmd.getOptionValue(CmdOption.NUMBER_OF_QUERIES.shortOption));
		DOUBLE_CHECK_HDT = cmd.hasOption(CmdOption.DOUBLE_CHECK_HDT.shortOption);
		OVERWRITE = cmd.hasOption(CmdOption.OVERWRITE.shortOption);
		CHECK_OTHER_SYSTEMS = cmd.hasOption(CmdOption.CHECK_OTHER_SYSTEM.shortOption);
		CREATE_SPECIAL_PATTERNS = cmd.hasOption(CmdOption.SPECIAL_PATTERNS.shortOption);
		if(cmd.getOptionValue(CmdOption.HDT_TYPE.shortOption).equals("AT")) {
			HDT_TYPE = HDTType.HDT_AT;
		} else {
			HDT_TYPE = HDTType.HDT_AG;
		}
	}
}
