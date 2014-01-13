package org.jq.sem.test;

import org.jq.sem.core.*;
import org.jq.sem.util.WordValidator;

import java.io.*;
import java.util.*;

/**
 * Implementation of Author: QQ Date: Dec 12, 2013 Version 1.0
 */
public class PreProcessTest {
	public static void main(String[] args) {
		try {
			FileOutputStream foStrem = new FileOutputStream(
					"testFiles/termCount.txt");
			DataOutputStream out = new DataOutputStream(foStrem);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			String newLine = System.getProperty("line.separator");
			FileInputStream fstream = new FileInputStream(
					"testFiles/IV6-RAWkeywords.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			WordValidator st = new WordValidator();
			String line;
			int count = 0;
			HashMap<String, Integer> termCnt = new HashMap<String, Integer>();
			// read file line by line
			while ((line = br.readLine()) != null) {
				count++;
				for (String term : line.split(" ")) {
					if (st.isStopWord(term))
						continue;
					if (termCnt.containsKey(term)) {
						termCnt.put(term, termCnt.get(term) + 1);
					} else
						termCnt.put(term, 1);
				}

			}
			// close the input stream
			in.close();
			bw.write("found " + count + " keywords" + newLine);
			bw.write("found " + termCnt.size() + " distinct terms" + newLine);
			Map<String, String> sortedTermCnt = sortByComparator(termCnt);
			for (Map.Entry entry : sortedTermCnt.entrySet()) {
				bw.write((String) entry.getKey() + " : "
						+ (Integer) entry.getValue() + newLine);
			}

			bw.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

	}

	private static Map sortByComparator(Map unsortMap) {

		List list = new LinkedList(unsortMap.entrySet());

		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		// put sorted list into map again
		// LinkedHashMap make sure order in which keys were inserted
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}
