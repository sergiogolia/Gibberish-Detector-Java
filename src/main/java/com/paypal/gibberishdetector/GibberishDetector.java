package com.paypal.gibberishdetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * gibberish detector used to train and classify sentences as gibberish or not.
 * @author sfiszman
 *
 */
public class GibberishDetector {
	
        private final Map<Character, Integer> alphabetPositionMap = new HashMap<Character, Integer>();
	private static final int MIN_COUNT_VAL = 10;

        private final Character nonChar = '\u0466';
	private final String alphabet;
	private double[][] logProbabilityMatrix = null;
	public double threshold = 0d;
	public double weight = 0d;
	
	public GibberishDetector(List<String> trainingLinesList, List<String> goodLinesList, List<String> badLinesList, String alphabet) {
		this.alphabet = alphabet + nonChar;
		train(trainingLinesList, goodLinesList, badLinesList);		
	}
			
	private void train(List<String> trainingLinesList, List<String> goodLinesList, List<String> badLinesList) {
		initializePositionMap();
		
		int[][] alphabetCouplesMatrix = getAlphaBetCouplesMatrix(trainingLinesList);
		logProbabilityMatrix = getLogProbabilityMatrix(alphabetCouplesMatrix);
			
		List<Double> goodProbability = getAvgTransitionProbability(goodLinesList, logProbabilityMatrix);
		List<Double> badProbability = getAvgTransitionProbability(badLinesList, logProbabilityMatrix);
		
		double minGood = Collections.min(goodProbability);
		double maxBad = Collections.max(badProbability);
				
		if (minGood <= maxBad) {
			throw new AssertionError("cannot create a threshold");
		}
		threshold = getThreshold(minGood, maxBad);
                // Create weighting to allow comparing different result sets
                weight = 0.50/threshold;
                System.out.printf("Threshold: %f, weight: %f\n", threshold, weight);
	}
	
	// can be overridden for another threshold heuristic implementation
	protected double getThreshold(double minGood, double maxBad) {
		return (minGood + maxBad) / 2;
	}
		
	private void initializePositionMap() {
		char[] alphabetChars = alphabet.toCharArray();
		for (int i = 0; i < alphabetChars.length; i++) {
			alphabetPositionMap.put(alphabetChars[i], i);
		}
	}
	
        // Normalize to character array. Chars outside array set to nonChar to get Prob
        private String normalize(String line) {
		StringBuilder normalizedLine = new StringBuilder();
		for (char c: line.toLowerCase().toCharArray()) {
			normalizedLine.append(alphabet.contains(Character.toString(c)) ? c : nonChar);
		}
		return normalizedLine.toString();
	}
	
	private List<String> getNGram(int n, String line) {
		String filteredLine = normalize(line);
		List<String> nGram = new ArrayList<>();
		for (int start = 0; start < filteredLine.length() - n + 1; start++) {
			nGram.add(filteredLine.substring(start, start + n));			
		}
		return nGram;
	}
	
	private int[][] getAlphaBetCouplesMatrix(List<String> trainingLinesList) {
		int[][] counts = createArray(alphabet.length());		
		for (String line : trainingLinesList) {
			List<String> nGram = getNGram(2, line);
			for (String touple : nGram) {
				counts[alphabetPositionMap.get(touple.charAt(0))][alphabetPositionMap.get(touple.charAt(1))]++;				
			}	
		}
		return counts;
	}
	
	private double[][] getLogProbabilityMatrix(int[][] alphabetCouplesMatrix) {
		int alphabetLength = alphabet.length();
		double[][] logProbabilityMatrix = new double[alphabetLength][alphabetLength];
		for (int i = 0; i < alphabetCouplesMatrix.length; i++) {
			double sum = getSum(alphabetCouplesMatrix[i]); 
			for (int j = 0; j < alphabetCouplesMatrix[i].length; j++) {				
				logProbabilityMatrix[i][j] = Math.log(alphabetCouplesMatrix[i][j]/sum);
			}
		}
		return logProbabilityMatrix;
	}
	
	private List<Double> getAvgTransitionProbability(List<String> lines, double[][] logProbabilityMatrix) {		
		List<Double> result = new ArrayList<Double>();		
		for (String line : lines) {
			result.add(getAvgTransitionProbability(line, logProbabilityMatrix));
		}
		return result;
	}
	
	private double getAvgTransitionProbability(String line, double[][] logProbabilityMatrix) {
		double logProb = 0d;
		int transitionCount = 0;
		List<String> nGram = getNGram(2, line);
		for (String touple : nGram) {
			logProb += logProbabilityMatrix[alphabetPositionMap.get(touple.charAt(0))][alphabetPositionMap.get(touple.charAt(1))];
			transitionCount++;
		}
		return Math.exp(logProb / Math.max(transitionCount, 1));
	}
		
	private int[][] createArray(int length){
		int[][] counts = new int[length][length];
            for (int[] count : counts) {
                Arrays.fill(count, MIN_COUNT_VAL);
            }
		return counts;
	}
	
	private double getSum(int[] array) {
		double sum = 0; 
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		return sum; 
	}
	
	/**
	 * determines if a sentence is gibberish or not.
	 * @param line a sentence to be classified as gibberish or not.
	 * @return true if the sentence is gibberish, false otherwise.
	 */
	public boolean isGibberish(String line) {
		return !(getAvgTransitionProbability(line, logProbabilityMatrix) > threshold);
	}
        
        /*
         * Return the transitionProbabilty for tracking and debugging
         * @param line is a sentence to check
         * @return double for probability
        */
        public double getProbability(String line) {
		return getAvgTransitionProbability(line, logProbabilityMatrix);
	}
}
