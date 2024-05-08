package org.aksw.simba.lemming.util;

import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.Hash;


public class Constants {
	public static final int IN_EDGE_DIRECTION = 1;
	public static final int OUT_EDGE_DIRECTION = -1;
	public static final double ROUND_DELTA = 0.5;
	
	public static final int REMOVE_ACTION = -1;
	public static final int BOTH_ACTION = 0;
	public static final int ADD_ACTION = 1;
	
	public static final int STEP_JUNHAO_POISSON = 500;
	public static final int ROUND_DECIMAL = 100;
	
	public static final boolean IS_EVALUATION_MODE = true;
	public static final boolean SINGLE_THREAD = false;
	public static final int MAX_EXPLORING_TIME = 5000;
	
	public static final String SIMULATED_URI= "http://dice.research.lemming.org/resource#";
	public static final String SIMULATED_CLASS_URI = "http://dice.research.lemming.org/class#";
	public static final String SIMULATED_PROPERTY_URI = "http://dice.research.lemming.org/property#";
	public static final String SIMULATED_DATA_TYPED_PROPERTY_URI = "http://dice.research.lemming.org/data_typed_property#";
	public static final String SIMULATED_BLANK_OBJECT_RESOURCE = "http://org.apache.jena.rdfxml/blankObj#";
	public static final String SIMULATED_BLANK_SUBJECT_RESOURCE = "http://org.apache.jena.rdfxml/blankSub#";
	public static final String TYPE_CLASS_PREFIX = "class_";
	public static final String TYPE_SUBCLASS_PREFIX = "subclass_";
	public static final int MAX_EXPLORING_TIME1 =500;
	public static final String PROPERTY = "property_";
	
	public static final int MAX_ITERATION_FOR_1_COLOUR = 5000;
	
	public static final Pattern DATETIME_PATTERN = Pattern.compile(
		      "^((2000|2400|2800|(19|2[0-9](0[48]|[2468][048]|[13579][26])))-02-29)$"
		      + "|^(((19|2[0-9])[0-9]{2})-02-(0[1-9]|1[0-9]|2[0-8]))$"
		      + "|^(((19|2[0-9])[0-9]{2})-(0[13578]|10|12)-(0[1-9]|[12][0-9]|3[01]))$"
		      + "|^(((19|2[0-9])[0-9]{2})-(0[469]|11)-(0[1-9]|[12][0-9]|30))$"
		      + "|^20\\d{2}(-|\\/)((0[1-9])|(1[0-2]))(-|\\/)((0[1-9])|([1-2][0-9])|(3[0-1]))(T|\\s)(([0-1][0-9])|(2[0-3])):([0-5][0-9]):([0-5][0-9]).*");
	
	public static final char[] PUNTUATION_MARKS = {'.', '?', '!', ';', ':', ',', '"', '\''};
	
	public static final String BASELINE_STRING = "barabasi_baseline_graph";
	
	public static final int DEFAULT_SIZE = Hash.DEFAULT_INITIAL_SIZE;
	
}
