package org.jq.sem.clustering;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.didion.jwnl.JWNLException;
import net.sf.javaml.clustering.*;
import net.sf.javaml.clustering.evaluation.*;
import net.sf.javaml.core.*;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.distance.ManhattanDistance;
import net.sf.javaml.tools.DatasetTools;
import net.sf.javaml.tools.data.FileHandler;

import org.apache.commons.lang3.time.StopWatch;
import org.jq.sem.core.DataSet;
import org.jq.sem.util.WordValidator;

import org.jq.sem.util.Matrix;
import org.jq.sem.util.SingularValueDecomposition;

/**
 * Implementation of a clusterer which can load data from input file
 * and cluster them into groups
 * Input file should be formated as follows:
 * one data point per line
 * each data point is represented as a string, attributes
 * are separated by spaces
 * 
 * Author: JQ 
 * Date: Dec 13, 2013 
 * Version 1.0
 */

public class MyClusterer {
	// static Logger log = Logger.getLogger(MyClusterer.class.getName());
	static final String TERM_VECTOR_FILE = "testFiles/termVector.txt";
	static final String TERM_VECTOR_FILE2 = "testFiles/termVector2.txt";
	static final String CLUSTER_RESULT_FILE = "testFiles/clusterResult.txt";
	static final int DEFAULT_CLUSTER_SIZE = 30;
	static final int DEFAULT_CLUSTER_NUM = 5;
	static final int DEFAULT_ITERATION_NUM = 100;
	DataSet mDataSet;

	public MyClusterer(String dataFileName) {
		// 1. preprocess input data
		mDataSet = new DataSet(dataFileName, -1);
		System.out.println("loading data done.");
	}
	

	/**
	 * this constructor which can change the number
	 * of data set size is mainly used for testing purpose
	 * 
	 * @param inputFileName
	 *            name of the file that contains data to be clustered
	 * @param testSize
	 *            size of the data points to be clustered.            
	 * 
	 */
	public MyClusterer(String inputFileName, int testSize) {
		// 1. preprocess input data
		System.out.println("loading data...");
		mDataSet = new DataSet(inputFileName, testSize);
		System.out.println("loading data done.");
	}

	/**
	 * data set -> term vectors files
	 * 
	 * @param dataSet
	 *            data set that needs to be clustered *
	 * @return file containing term vector for each data point
	 * 		   one vector per line, attributes' weights are 
	 * 			separated by comma
	 * 
	 * 
	 */
	File buildTermVectors(DataSet dataSet) {
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(TERM_VECTOR_FILE);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return null;
		}
		DataOutputStream dataOutStream = new DataOutputStream(outStream);
		BufferedWriter bufferedWriter = new BufferedWriter(
				new OutputStreamWriter(dataOutStream));
		String newLine = System.getProperty("line.separator");		
		int testCnt = 0;
		double[] vector = null;
		Map<String, Integer> keywordIdMap = dataSet.getDataPointIdMapping();
		Map<String, Integer> termIdMap = dataSet.getAttributeIdMapping();
		for (Map.Entry<String, Integer> entry : keywordIdMap.entrySet()) {
			String keyword = entry.getKey();
			vector = new double[dataSet.getAttributeCount()];
			for (String term : keyword.split(" ")) {				
				if (termIdMap.get(term) != null) {
					vector[termIdMap.get(term)] += 1.0;
				}
			}
			try {
				for (int i = 0; i < vector.length; i++) {
					if (i < vector.length - 1) {
						bufferedWriter.write(Double.toString(vector[i]) + ",");
					} else {
						bufferedWriter.write(Double.toString(vector[i]));
					}
				}
				bufferedWriter.write(newLine);
				testCnt++;
				if (dataSet.getTestSize() != -1
						&& testCnt == dataSet.getTestSize()) {
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
				try {
					bufferedWriter.close();
					outStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return null;
			}
		}
		try {
			bufferedWriter.close();
			outStream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return new File(TERM_VECTOR_FILE);
	}
	
	/**
	 * Matrix -> term vectors file
	 * 
	 * @param matrix	            
	 * @return file containing term vector for each data point
	 * 		   one vector per line, attributes' weights are 
	 * 			separated by comma
	 */
	File buildTermVectors(Matrix matrix) {
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(TERM_VECTOR_FILE2);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return null;
		}
		DataOutputStream dataOutStream = new DataOutputStream(outStream);
		BufferedWriter bufferedWriter = new BufferedWriter(
				new OutputStreamWriter(dataOutStream));
		String newLine = System.getProperty("line.separator");
		int i = 0, j = 0;
		try {
			for (i = 0; i < matrix.getRowDimension(); i++) {
				for (j = 0; j < matrix.getColumnDimension() - 1; j++) {
					bufferedWriter.write(Double.toString(matrix.get(i, j))
							+ ",");
				}
				bufferedWriter.write(Double.toString(matrix.get(i, j)));
				bufferedWriter.write(newLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			bufferedWriter.close();
			outStream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return new File(TERM_VECTOR_FILE2);
	}

	/**
	 * Data set -> Matrix
	 * 
	 * @param dataSet
	 *            data set that needs to be clustered *
	 * @return Matrix
	 * 
	 *         TODO: If dataset is too big, we must down sample it!!!
	 */
	private Matrix dataSetToMatrix(DataSet dataSet) {
		int columns = dataSet.getAttributeCount();
		int rows = dataSet.getDataPointCount();
		int i = 0, j = 0;
		// WARNING: rows must >= columns !!!!
		if (rows < columns) {
			throw new IllegalArgumentException("rows must >= columns");
		}
		Matrix matrix = new Matrix(rows, columns);
		Map<String, Integer> keywordIdMap = dataSet.getDataPointIdMapping();
		Map<String, Integer> termIdMap = dataSet.getAttributeIdMapping();
		// Build matrix. No need to do word validation again
		for (Map.Entry<String, Integer> entry : keywordIdMap.entrySet()) {
			String keyword = entry.getKey();
			for (String term : keyword.split(" ")) {
				if (termIdMap.get(term) != null) {
					j = termIdMap.get(term);
					matrix.set(i, j, matrix.get(i, j) + 1.0);
				}
			}
			if (dataSet.getTestSize() > 0 && i >= dataSet.getTestSize()) {
				break;
			}
			i++;
		}
		return matrix;
	}

	double kMeansClustering(File termVectorFile, int clusterNum, int clusterSize) {
		Clusterer km = new KMeans(clusterNum);
		return doClustering(termVectorFile, km, new EuclideanDistance(), clusterNum,
				clusterSize);
	}

	double kMedoidsClustering(File termVectorFile, int clusterNum, int clusterSize) {
		int maxIteration = DEFAULT_ITERATION_NUM;
		Clusterer km = new KMedoids(clusterNum, maxIteration,
				new EuclideanDistance());
		return doClustering(termVectorFile, km, new EuclideanDistance(), clusterNum,
				clusterSize);
	}

	double doClustering(File termVectorFile, Clusterer clusterer,
			DistanceMeasure dm, int clusterNum, int clusterSize) {
		if (clusterNum < 0)
			clusterNum = DEFAULT_CLUSTER_NUM;
		if (clusterSize < 0)
			clusterSize = DEFAULT_CLUSTER_SIZE;
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(CLUSTER_RESULT_FILE);
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
			return Double.POSITIVE_INFINITY;
		}
		DataOutputStream dataOutStream = new DataOutputStream(outStream);
		BufferedWriter bufferWriter = new BufferedWriter(
				new OutputStreamWriter(dataOutStream));
		String newLine = System.getProperty("line.separator");
		Dataset data;
		try {
			data = FileHandler.loadDataset(termVectorFile, ",");
		} catch (IOException e) {
			e.printStackTrace();
			return Double.POSITIVE_INFINITY;
		}
		StopWatch sw = new StopWatch();
		System.out.println("start clustering...");
		sw.start();
		Dataset[] clusters = clusterer.cluster(data);
		sw.stop();		
		System.out.println("clustering took " + sw.getTime() / 1000.0 + " s.");
		int clusterId = 0;
		Map<Integer, String> invIdKeywordMap = mDataSet
				.getInverseDataPointIdMapping();		
		ClusterEvaluation eval = new SumOfSquaredErrors();
		double sse = eval.score(clusters);
		try {
			bufferWriter.write("Clustering result of " + mDataSet.getDataPointCount()
					+ " data points\r\n\r\n");
			bufferWriter.write("Total " + clusters.length + " clusters ("
					+ "cluster size =  " + clusterSize
					+ ", SumOfSquaredErrors = " + sse
					+ "): \r\n\r\n");
			for (Dataset cluster : clusters) {
				// System.out.println("cluster " + clusterId++ + " : ");
				bufferWriter.write("cluster " + clusterId++ + " : "
						+ "(original size " + cluster.size() + ")");
				bufferWriter.write(newLine);
				Instance centroid = calculateCentroid(cluster);
				Set<Instance> knn = cluster.kNearest(clusterSize, centroid, dm);
				// for (Instance ins : cluster) {
				Iterator<Instance> itr = knn.iterator();
				while (itr.hasNext()) {
					Instance ins = (Instance) itr.next();
					// System.out.println(invIdKeywordMap.get(ins.getID()));
					//bufferWriter.write(ins.getID() + " : ");
					bufferWriter.write(invIdKeywordMap.get(ins.getID()));
					bufferWriter.write(newLine);
				}
				bufferWriter.write(newLine);
			}
		} catch (IOException e) {
			// log.error(e.getMessage());
		} finally {
			try {
				bufferWriter.close();				
				outStream.close();
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
		System.out.println("clustering done.");
		return sse;
	}

	Instance calculateCentroid(Dataset cluster) {
		Instance newCentroid = DatasetTools.average(cluster);
		/* if using kmeans algorithm, then return newCentroid now; if using
		 * kmedoid algorithm, then do following
		*/
		DistanceMeasure dm = new ManhattanDistance();
		double minCost = Double.MAX_VALUE;
		for (Instance centroid : cluster) {
			double totalCost = 0.0;
			for (Instance other : cluster) {
				if (other == centroid)
					continue;
				totalCost += dm.measure(centroid, other);
			}
			if (Double.compare(totalCost, minCost) < 0.0) {
				minCost = totalCost;
				newCentroid = centroid;
			}
		}
		return newCentroid;
	}

	/**
	 * cluster keywords
	 * 
	 * @param number of clusters(groups) produced by the clusterer
	 * @param number of keywords in each cluster
	 * @return  Sum of squared errors
	 * 	        output will be in clusterResult_clusterNum_clusterSize.txt
	 * 
	 * 
	 */
	public double clusterKeywords(int clusterNum, int clusterSize) {
		// 2. build term vectors and write to file
		// This was used before SVD
		// File termVectors = buildTermVectors(mDataSet);

		// 3. normalization
		Matrix matrix = dataSetToMatrix(mDataSet);
		
		System.out.println("performing svd...");
		// output matrix to file Matrix vectors = new Matrix(matrix);
		SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
		Matrix matrixU = svd.getU(); // m x k
		Matrix matrixS = svd.getS(); // k x k This is the singular matrix
		Matrix matrixV = svd.getV(); // k x n

		// Now reduce the dimentions of k to consolidate this matrix
		// http://home2.btconnect.com/mmhasan/papers/ipsj-tr.pdf Figure 1
		int newSize = 30; // hard code for now
		int i = 0, j = 0;
		Matrix matrixS2 = new Matrix(newSize, newSize);
		for (i = 0; i < newSize; i++) {
			matrixS2.set(i, i, matrixS.get(i, i));
		}
		Matrix matrixU2 = new Matrix(matrixU.getRowDimension(), newSize);
		for (i = 0; i < matrixU2.getRowDimension(); i++) {
			for (j = 0; j < matrixU2.getColumnDimension(); j++) {
				matrixU2.set(i, j, matrixU.get(i, j));
			}
		}
		Matrix matrixV2 = new Matrix(newSize, matrixV.getColumnDimension());
		for (i = 0; i < matrixV2.getRowDimension(); i++) {
			for (j = 0; j < matrixV2.getColumnDimension(); j++) {
				matrixV2.set(i, j, matrixV.get(i, j));
			}
		}
		// Finally, reconstruct the new matrix
		Matrix matrix2 = (matrixU2.times(matrixS2)).times(matrixV2);
		System.out.println("svd done.");
		
		// Build new term vectors.
		File termVectors2 = buildTermVectors(matrix2);

		// 4. clustering (using Java-ML library, kmeans/kmedoid)		
		//kMeansClustering(termVectors2, clusterNum, clusterSize);
		return kMedoidsClustering(termVectors2, clusterNum, clusterSize);
		
		// 5. measure the quality of the clustering (did in 4.)		
	}

	public static void main(String[] args) {
		String keywordsFileName = args[0]; //"testFiles/IV6-RAWkeywords.txt";
		int clusterSize = Integer.valueOf(args[3]), clusterNum = Integer
				.valueOf(args[2]), testSize = Integer.valueOf(args[1]);
		MyClusterer clusterer = new MyClusterer(keywordsFileName, testSize);
		clusterer.clusterKeywords(clusterNum, clusterSize);			
	}

}
