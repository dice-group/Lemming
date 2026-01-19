package org.aksw.simba.lemming.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Class to help read the old result files and build
 * with all the results from it.
 */
public class ParseResults {

	public static void main(String[] args) {
		// read LemmingEx.result, group based on dataset
		String dataset = "swdf";
		String content = "";
		String delimiter = "#----------------------------------------------------------------------#\n#";
		String personalDel = "#----------------------------------------------------------------------#";

		// map with group key - metric - multiple (initial, optimized) value pairs
		Map<String, Map<String, List<String>>> groups = new HashMap<>();
		try {
			List<String> lines = Files.readAllLines(Paths.get("LemmingEx.result"));
			content = String.join("\n", lines);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// split by experiment
		String[] sections = content.split(delimiter);
		big: for (String expResult : sections) {
			if (expResult.isBlank())
				continue;
			String[] graphData = expResult.split(personalDel);
			String mode = null, classSelector = null, vertexSelector = null, simplexProperty = null,
					simplexClass = null, duration = null, baselineModel = null, file = null;

			// handle metadata
			String[] resultContent = graphData[0].split("\n");
			for (String line : resultContent) {
				if (line.isBlank())
					continue;

				// skip unless it's the dataset we're interested in
				if (line.contains("dataset")) {
					if (!line.split(":")[1].strip().contentEquals(dataset)) {
						continue big;
					}
				}

				if (line.contains("Duration")) {
					Pattern pattern = Pattern.compile("\\d+");
					Matcher matcher = pattern.matcher(line);
					duration = matcher.find() ? matcher.group() : "";
				}

				if (line.contains("Saved file")) {
					file = getSecondMember(line);
				}

				if (line.contains("mode")) {
					mode = getSecondMember(line);
				}

				if (line.contains("classSelector")) {
					classSelector = getSecondMember(line);
				}

				if (line.contains("vertexSelector")) {
					vertexSelector = getSecondMember(line);
				}

				if (line.contains("simplexProperty")) {
					simplexProperty = getSecondMember(line);
				}

				if (line.contains("simplexClass")) {
					simplexClass = getSecondMember(line);
				}

				if (line.contains("baselineModel")) {
					baselineModel = getSecondMember(line);
				}

			}

			// initialize maps based on group found for this dataset if not existing
			String key = null;
			switch (mode) {
			case "Binary":
				key = classSelector.concat(vertexSelector);
				break;
			case "Simplex":
				key = simplexProperty.concat(simplexClass);
				break;
			case "Bl":
				key = baselineModel;
				break;
			default:
				break;
			}
			groups.putIfAbsent(key, new HashMap<String, List<String>>());

			// handle metrics from the second half, all in order of appearance
			String measures = graphData[1];
			List<String> perfMeasNames = extractMatches(measures, "Metric: .*?\\n");
			perfMeasNames.addAll(extractMatches(measures, "Expression: .*?\\n"));
			// these two will also match the error, pull it to the side
			List<String> initialValues = extractMatches(measures, "The first mimic graph: .*?\\n");
			List<String> optValues = extractMatches(measures, "The opimized mimic graph: .*?\\n");

			// save duration
			Map<String, List<String>> savedMetrics = groups.get(key);
			savedMetrics.putIfAbsent("duration", new ArrayList<>());
			savedMetrics.get("duration").add(duration);

			// the baseline doesn't have error scores since it doesn't go through
			// optimization
			String initError = "";
			String optError = "";
			if (perfMeasNames.size() < initialValues.size()) {
				initError = initialValues.remove(initialValues.size() - 1);
				optError = optValues.remove(optValues.size() - 1);
			}
			savedMetrics.putIfAbsent("initialError", new ArrayList<>());
			savedMetrics.putIfAbsent("optError", new ArrayList<>());
			savedMetrics.get("initialError").add(initError);
			savedMetrics.get("optError").add(optError);

			// save in map in pairs
			for (int i = 0; i < perfMeasNames.size(); i++) {
				// create if it doesn't exist
				savedMetrics.putIfAbsent(perfMeasNames.get(i), new ArrayList<>());
				savedMetrics.get(perfMeasNames.get(i)).add(initialValues.get(i));
				savedMetrics.get(perfMeasNames.get(i)).add(optValues.get(i));
			}
		}

		// TODO build and print tables
		System.out.println();
	}

	public static String getSecondMember(String line) {
		return line.split(":")[1].strip();
	}
	
	public static List<String> extractMatches(String input, String regex) {
		List<String> matches = new ArrayList<>();
		Matcher matcher = Pattern.compile(regex).matcher(input);
		while (matcher.find()) {
			matches.add(matcher.group().strip().split(":")[1]); 
		}
		return matches;
	}

	/**
	 * Sort based all lists based on the first list
	 * @param list1
	 * @param list2
	 * @param list3
	 */
	public static void sortLists(List<String> list1, List<String> list2, List<String> list3) {
		// Create a list of indices
		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < list1.size(); i++) {
			indices.add(i);
		}

		// Sort the indices based on the values in list1
		Collections.sort(indices, Comparator.comparing(list1::get));

		// Create copies of the original lists
		List<String> sortedList1 = new ArrayList<>(list1);
		List<String> sortedList2 = new ArrayList<>(list2);
		List<String> sortedList3 = new ArrayList<>(list3);

		// Reorder the lists based on the sorted indices
		for (int i = 0; i < indices.size(); i++) {
			list1.set(i, sortedList1.get(indices.get(i)));
			list2.set(i, sortedList2.get(indices.get(i)));
			list3.set(i, sortedList3.get(indices.get(i)));
		}
	}

}
