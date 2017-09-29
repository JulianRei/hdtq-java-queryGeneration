package enums;

public enum CmdOption{
	//		          short, long,               hasArg, required, description
	HELP              ("h",  "help",             false,  false,    "show help."),
	FOLDER	          ("f",  "folder",           true,   true,     "path to folder containing, hdt file, virtuoso and jena folder"),
	NUMBER_OF_QUERIES ("n",  "numberOfQueries",  true,   true,     "number of queries to generate per pattern"),
	DOUBLE_CHECK_HDT  ("d",  "doublecheck",      false,  false,    "if set, number of results of HDT-AT and HDT-AG will be checked"),
	OVERWRITE         ("o",  "overwrite",        false,  false,    "if set, existing query files will be overriden"),
	CHECK_OTHER_SYSTEM("c",  "checkOthers",      false,  false,    "if set, it will be checked if all systems report the same number of results for every query"),
	SPECIAL_PATTERNS  ("s",  "specialPatterns",  false,  false,    "if set, also special patterns will be created"),
	HDT_TYPE          ("t",  "hdtType",          true,   true,     "Choose either AT or AG. This kind of hdt will be used");
	
	public final String shortOption;
	public final String longOption;
	public final boolean required;
	public final String description;
	public final boolean hasArg;
	
	private CmdOption(String so, String lo, boolean ha, boolean req, String desc){
		shortOption = so;
		longOption = lo;
		required = req;
		description = desc;
		hasArg = ha;
	}
}
