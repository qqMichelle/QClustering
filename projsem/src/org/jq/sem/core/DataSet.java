package org.jq.sem.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

//import org.apache.log4j.Logger;
import org.jq.sem.clustering.MyClusterer;
import org.jq.sem.util.WordValidator;

/**
 * Store data points info read from input file
 * Author: JQ 
 * Date: Dec 12, 2013 
 * Version 1.0
 */

public class DataSet {
	// static Logger log = Logger.getLogger(
	// DataSet.class.getName());
	Map<String, Integer> mDataPointIdMapping = new LinkedHashMap<String, Integer>();
	Map<Integer, String> mInverseDataPointIdMapping = new LinkedHashMap<Integer, String>();
	int mDataPointCount = 0, mAttributeCount = 0;
	Map<String, Integer> mAttributeIdMapping = new HashMap<String, Integer>();
	Map<Integer, String> mInverseAttributeIdMapping = new HashMap<Integer, String>();
	int mTestSize = -1;

	public DataSet(String fileName, int testSize) {
		this.mTestSize = testSize;
		init(fileName);
	}

	public DataSet(String fileName) {
		init(fileName);
	}

	void init(String fileName) {
		WordValidator wordValidator = new WordValidator();

		try {
			FileInputStream fstream;
			DataInputStream in;
			BufferedReader br;
			fstream = new FileInputStream(fileName);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			String line = "";

			while ((line = br.readLine()) != null) {
				if (!mDataPointIdMapping.containsKey(line)) {
					mDataPointCount++;
					mDataPointIdMapping.put(line, mDataPointCount - 1);
					mInverseDataPointIdMapping.put(mDataPointCount - 1, line);
				}
				for (String term : line.split(" ")) {
					// if (st.isStopWord(term))continue;
					if (wordValidator.isStopWord(term)
							|| !wordValidator.isDictionaryWord(term)) {
						// System.out.println("ignore invalid term: " + term);
						continue;
					}
					if (!mAttributeIdMapping.containsKey(term)) {
						mAttributeCount++;
						mAttributeIdMapping.put(term, mAttributeCount - 1);
						mInverseAttributeIdMapping.put(mAttributeCount - 1,
								term);
					}
				}

				if (mTestSize != -1 && mDataPointCount == mTestSize)
					break; // testing
			}

		} catch (Exception e) {
			e.printStackTrace();			
			// log.error(e.getMessage());
		}
	}

	public int getAttributeCount() {
		return this.mAttributeCount;
	}

	public int getDataPointCount() {
		return this.mDataPointCount;
	}

	public Map<String, Integer> getDataPointIdMapping() {
		return this.mDataPointIdMapping;
	}

	public Map<Integer, String> getInverseDataPointIdMapping() {
		return this.mInverseDataPointIdMapping;
	}

	public Map<Integer, String> getInverseAttributeIdMapping() {
		return this.mInverseAttributeIdMapping;
	}

	public Map<String, Integer> getAttributeIdMapping() {
		return this.mAttributeIdMapping;
	}

	public int getTestSize() {
		return this.mTestSize;
	}
}
