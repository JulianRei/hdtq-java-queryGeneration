package main;

import java.io.File;
import java.io.IOException;

import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.quads.QuadString;
import org.rdfhdt.hdt.rdf.RDFAccess;

import enums.ComponentType;
import enums.HDTType;
import tests.HDTTest;
import tests.JenaTest;
import tests.VirtuosoTest;
import util.ComponentQuad;
import util.ComponentString;
import util.Logger;

public class QueryTester {
	private static String folderPath;
	private static String dataset;
	private static final int minResults = 1;
	
	private static ComponentQuad componentQuad;
	
	public static HDTTest hdtATTest = null;
	public static HDTTest hdtAGTest = null;
	private static JenaTest jenaTest = null;
	public static VirtuosoTest virtuosoTest = null;
	
	public static void init(String folder) {
		folderPath = folder;
		String[] parts = folderPath.split(File.separator);
		dataset = parts[parts.length - 1];
	}
	
	public static boolean allHaveSameNumberOfResults(ComponentQuad componentQuad, boolean doubleCheckHDT) throws IOException {
		QueryTester.componentQuad = componentQuad;
		int hdtNumResults = -1;
		if(doubleCheckHDT) {
			Logger.log("Double check HDT is activated");
			int hdtATNumResults = hdtATNumResults();
			if(hdtATNumResults < minResults) {
				return false;
			}
			int hdtAGNumResults = hdtAGNumResults();
			if(hdtATNumResults != hdtAGNumResults) {
				return false;
			}
			hdtNumResults = hdtATNumResults;
		} else {
			Logger.log("Double check HDT is deactivated");
			// I reuse hdt from pattern extracting
			if(hdtATTest == null && hdtAGTest == null) {
				switch(Go.HDT_TYPE) {
				case HDT_AG:
					hdtNumResults = hdtAGNumResults();
					break;
				case HDT_AT:
					hdtNumResults = hdtATNumResults();
					break;
				}
				
			} else {
				if(hdtATTest != null) hdtNumResults = hdtATNumResults();
				if(hdtAGTest != null) hdtNumResults = hdtAGNumResults();
			}
			if(hdtNumResults < minResults) {
				return false;
			}
		}
		
		int jenaNumResults = jenaNumResults();
		if(hdtNumResults != jenaNumResults) {
			return false;
		}
		int virtuosoNumResults = virtuosoNumResults();
		if(hdtNumResults != virtuosoNumResults) {
			return false;
		}
		
		return true;
	}

	private static int hdtATNumResults() throws IOException {
		if(hdtATTest == null) {
			hdtATTest = new HDTTest();
			hdtATTest.dataFile = folderPath + File.separator + dataset + "AT.hdt";
			Logger.log("Loading HDT AT.");
			hdtATTest.loadData();
			Logger.log("Loading done");
		}
		
		hdtATTest.componentQuad = componentQuad;
		
		try {
			Logger.log("Searching for results in HDT AT");
			return hdtATTest.doSearch();
		} catch (NotFoundException e) {
			return -1;
		}
	}
	
	private static int hdtAGNumResults() throws IOException {
		if(hdtAGTest == null) {
			hdtAGTest = new HDTTest();
			hdtAGTest.dataFile = folderPath + File.separator + dataset + "AG.hdt";
			Logger.log("Loading HDT AG.");
			hdtAGTest.loadData();
			Logger.log("Loading done");
		}
		
		hdtAGTest.componentQuad = componentQuad;
		
		try {
			Logger.log("Searching for results in HDT AG");
			return hdtAGTest.doSearch();
		} catch (NotFoundException e) {
			return -1;
		}
	}
	
	private static int jenaNumResults() {
		if(jenaTest == null) {
			jenaTest = new JenaTest();
			jenaTest.dataFile = folderPath + File.separator + dataset + ".tdb";
			Logger.log("Loading Jena.");
			jenaTest.loadData();
			Logger.log("Loading done");
		}
		
		jenaTest.componentQuad = componentQuad;
		
		Logger.log("Preparing Jena query");
		jenaTest.prepareQuery();
		Logger.log("Searching for results in Jena");
		return jenaTest.doSearch();
	}
	
	public static void loadVirtuoso() {
		if(virtuosoTest == null) {
			virtuosoTest = new VirtuosoTest();
			virtuosoTest.dataFile = folderPath + File.separator + dataset + "+.vir";
			Logger.log("Loading Virtuoso+");
			virtuosoTest.loadData();
			Logger.log("Loading done");
		}
	}
	
	public static void loadHDT() throws IOException {
		switch(Go.HDT_TYPE) {
		case HDT_AG:
			if(hdtAGTest == null) {
				hdtAGTest = new HDTTest();
				hdtAGTest.dataFile = folderPath + File.separator + dataset + "AG.hdt";
				Logger.log("Loading HDT_AG");
				hdtAGTest.loadData();
				Logger.log("Loading done");
			}
			break;
		case HDT_AT:
			if(hdtATTest == null) {
				hdtATTest = new HDTTest();
				hdtATTest.dataFile = folderPath + File.separator + dataset + "AT.hdt";
				Logger.log("Loading HDT_AT");
				hdtATTest.loadData();
				Logger.log("Loading done");
			}
			break;
		}
		
	}
	
	private static int virtuosoNumResults() {
		loadVirtuoso();
		
		virtuosoTest.componentQuad = componentQuad;

		Logger.log("Preparing Virtuoso query");
		virtuosoTest.prepareQuery();
		Logger.log("Searching for results in Virtuoso");
		return virtuosoTest.doSearch();
	}

	/**
	 * Get record from Virtuoso
	 * @param position
	 * @return
	 */
	public static QuadString getRecord(String pattern, long position) {
		return virtuosoTest.getRecord(pattern, position);
	}

	public static long getNumberOfDistinctRecords(String pattern) {
		return virtuosoTest.getNumberOfDistinctRecords(pattern);
	}

	/**
	 * Get record from Virtuoso
	 * @param position
	 * @return
	 */
	public static ComponentString getRecord(ComponentType component, long position, ComponentQuad componentQuad) {
		return virtuosoTest.getRecord(component, position, componentQuad);
		
	}

	public static RDFAccess hdtHDT(HDTType hdtType) {
		switch(hdtType) {
		case HDT_AG:
			return hdtAGTest.hdt;
		case HDT_AT:
			return hdtATTest.hdt;
		}
		return null;
	}

}
