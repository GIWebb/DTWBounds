package TSTester;

import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


import distance.elastic.DTW;
import distance.Bounds;
import distance.BoundsID;

import TSTester.Dataset;
import TSTester.ClassifiedSequence;

import Utils.OSUtils;
import Utils.FileIterator;



public class TSTester {
	private enum experimentType {
		unsorted, shuffled, sorted, testTightness;
	}
	
	public static void main(String[] args) {
		int window = -1;
		experimentType experiment = experimentType.unsorted;
		boolean winPercent = false;
		String bounds[] = {"none", "keogh", "improved", "enhanced5", "petitjean", "webb"};
		String datasets[] = null;
		String outName = ".";
		String datasetDirectory = "UCR_TS_Archive_2015";
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].charAt(0) == '-'){
				switch (args[i].charAt(1)) {
				case 'b':
					bounds = args[i].substring(2).split("\\s*,\\s*");
					System.out.print("Using bounds");
					for (String b : bounds) System.out.print(" "+b);
					System.out.println();
					break;
					
				case 'd':
					datasets = args[i].substring(2).split("\\s*,\\s*");
					System.out.print("Using data sets");
					for (String d : datasets) System.out.print(" "+d);
					System.out.println();
					break;

				case 'D':
					if (args[i].length() > 2) {
						datasetDirectory = args[i].substring(2);
					}
					else if (args.length == i-1) {
						System.out.println("Directory name not specified!");
						System.err.println("Directory name not specified!");
						return;
					}
					else {
						datasetDirectory = args[++i];
					}
					System.out.println("D=" + datasetDirectory);
					break;
					
				case 'g':
					winPercent = true;
					if (args[i].length() > 2) {
						window = Integer.parseInt(args[i].substring(2));
					}
					else if (args.length == i-1) {
						System.out.println("Window percentage size not specified!");
						System.err.println("Window percentage size not specified!");
						return;
					}
					else {
						window = Integer.parseInt(args[++i]);
					}
					System.out.println("G=" + window);
					break;

				case 'n':
					if (args[i].length() > 2) {
						outName = args[i].substring(2);
					}
					else if (args.length == i-1) {
						System.out.println("Output name not specified!");
						System.err.println("Output name not specified!");
						return;
					}
					else {
						outName = args[++i];
					}
					System.out.println("n=" + outName);
					break;
					
				case 'o':
					experiment = experimentType.shuffled;
					break;
					
				case 's':
					experiment = experimentType.sorted;
					break;
					
				case 't':
					experiment = experimentType.testTightness;
					break;
					
				case 'w': 
					if (args[i].length() > 2) {
						window = Integer.parseInt(args[i].substring(2));
					}
					else if (args.length == i-1) {
						System.out.println("Window size not specified!");
						return;
					}
					else {
						window = Integer.parseInt(args[++i]);
					}
					System.out.println("W=" + window);
					break;
					
				case 'W': 
					window = -2;
					System.out.println("Use 'optimal' window for each dataset");
					break;
					
				case 'x': 
					getStats(datasetDirectory);
					return;
				}
			}
		}
		
		doTest(window, winPercent, bounds, datasets, experiment, outName, datasetDirectory);
	}
	
	static void getStats(String datasetDirectory) {
		DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
		Date dateobj = new Date();
		String date = df.format(dateobj);

		File outDir = new File(date);
		while (outDir.exists()) {
			date += "0";
			outDir = new File(date);
		}
		if (!outDir.exists()) {
			if (!outDir.mkdir()) {
				System.out.println("Failed to create directory " + date + "!");
				return;
			}
		}

		FileWriter statsStream = null;
		
			try {
				statsStream = new FileWriter(outDir.getName() + Utils.OSUtils.directorySep() + "stats" + ".csv");
				
				FileIterator files = new FileIterator(verbosity, datasetDirectory);
				File trainFile = null;
				File testFile = null;

				files.getNext();
				trainFile = files.getTrain();
				testFile = files.getTest();
				while (trainFile != null) {
					try {
						statsStream.write(","+files.getName());
					} catch (IOException e) {
						System.out.println("Output error!");
						return;
					}
					
					files.getNext();
					trainFile = files.getTrain();
					testFile = files.getTest();
				}
				try {
					statsStream.write("\n");
				} catch (IOException e) {
					System.out.println("Output error!");
					return;
				}

				files.reset();
				files.getNext();
				trainFile = files.getTrain();
				testFile = files.getTest();
				while (trainFile != null) {
					Dataset train = new Dataset();
					train.load(trainFile);

					try {
						statsStream.write(","+train.series.size());
					} catch (IOException e) {
						System.out.println("Output error!");
						return;
					}
				
					files.getNext();
					trainFile = files.getTrain();
					testFile = files.getTest();
				}
				try {
					statsStream.write("\n");
				} catch (IOException e) {
					System.out.println("Output error!");
					return;
				}

				files.reset();
				files.getNext();
				trainFile = files.getTrain();
				testFile = files.getTest();
				while (trainFile != null) {
					Dataset test = new Dataset();

					test.load(testFile);
					
					try {
						statsStream.write(","+test.series.size());
					} catch (IOException e) {
						System.out.println("Output error!");
						return;
					}
					
					files.getNext();
					trainFile = files.getTrain();
					testFile = files.getTest();
				}

				try {
					statsStream.write("\n");
				} catch (IOException e) {
					System.out.println("Output error!");
					return;
				}

			} catch (IOException e) {
				System.out.println("Could not open output files!");
				return;
			} finally {
				try {
					if (statsStream != null) {
						statsStream.close();
						statsStream = null;
					}

				} catch (IOException e) {
					System.out.println("Could not close output files!");
					return;
				}
			}

	}
	
	static class boundInfo {
		boundInfo(distance.BoundsID b, int K) {
			bound = b;
			k=K;
		}
		
		distance.BoundsID bound;
		int k;
	}
		
	static void doTest(int window, boolean winPercent, String boundsStr[], String datasets[], experimentType experiment, String outName, String datasetDirectory) {
		boundInfo bounds[] = new boundInfo[boundsStr.length];
		DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
		Date dateobj = new Date();
		String date = df.format(dateobj);
		Random rnd = new Random();
		String base = date;
		date = base + "-" + rnd.nextInt();
		File baseDir = new File(outName);

		if (!baseDir.exists()) {	
			if (!baseDir.mkdir()) {
				System.out.println("Failed to create directory " + outName + "!");
				return;
			}
		}

		File outDir = new File(baseDir.getName() + Utils.OSUtils.directorySep() + date);
		
		if (outDir.exists()) {	
			while (outDir.exists()) {
				date = base + "-" + rnd.nextInt();
				outDir = new File(baseDir.getName() + Utils.OSUtils.directorySep() + date);
			}
		}

		if (!outDir.exists()) {
			if (!outDir.mkdir()) {
				System.out.println("Failed to create directory " + date + "!");
				return;
			}
			
			System.out.println("Output directory: "+date);
		}

		int startW, endW;
		
		if (window == -2) {
			// optimal window for each dataset
			startW = endW = -2;
		}
		else if (window <0) {
			startW = 1;
			endW = 10;
		}
		else {
			startW = endW = window;
		}
		
		// parse the bounds
		for (int i = 0; i < boundsStr.length; i++) {
			String b = boundsStr[i];
			int k = 0;
			distance.BoundsID bID = distance.BoundsID.None;
			
			if (b.regionMatches(true, 0, "enhancedwebb", 0, "enhancedwebb".length())) {
				b = b.substring("enhancedwebb".length());	// remove "enhanced"
				
				while (b.length()>0 && b.charAt(0) >= '0' && b.charAt(0)<= '9') {
					k *= 10;
					k += (b.charAt(0)-'0');
					b = b.substring(1);
				}
				
				bID = distance.BoundsID.EnhancedWebb;
			}
			else if (b.regionMatches(true, 0, "enhanced", 0, "enhanced".length())) {
				b = b.substring("enhanced".length());	// remove "enhanced"
				
				while (b.length()>0 && b.charAt(0) >= '0' && b.charAt(0)<= '9') {
					k *= 10;
					k += (b.charAt(0)-'0');
					b = b.substring(1);
				}
				
				bID = distance.BoundsID.Enhanced;
			}
			else {
				boolean found = false;
			
				for (distance.BoundsID bid : distance.BoundsID.values()) {
					if (b.equalsIgnoreCase(bid.name())) {
						bID = bid;
						found = true;
						break;
					}
				}
				
				if (!found) {
					System.err.println("Cannot set bound "+b);
					return;
				}
			}
			
			bounds[i] = new boundInfo(bID, k);
		}

		for (int w = startW; w <= endW; w++) {
			String suffix = "w";
			
			if (w == -2) {
				suffix = "W";
			}
			else {
				if (winPercent) suffix = "g";
				
				suffix += w;
			}
			
			if (experiment == experimentType.sorted) {
				suffix += "-sort";
			}
			else if (experiment == experimentType.unsorted) {
				suffix += "-nosort";
			}
			else if (experiment == experimentType.testTightness) {
			}
			
			suffix += ".csv";
			
			if (experiment == experimentType.testTightness) {
				doTestTightness(baseDir, outDir, suffix, datasets, bounds, w, winPercent, experiment, datasetDirectory);
			}
			else {
				testNN(baseDir, outDir, suffix, datasets, bounds, w, winPercent, experiment, datasetDirectory);
			}
		}
	}
	
	static void testNN(File baseDir, File outDir, String suffix, String datasets[], boundInfo bounds[], int w, boolean winPercent, experimentType sort, String datasetDirectory) {
		FileWriter timesStream = null;
		FileWriter timeVarStream = null;
		FileWriter accuracyStream = null;
		FileWriter prunedStream = null;
		
		try {
			timesStream = new FileWriter(baseDir.getName() + Utils.OSUtils.directorySep() +outDir.getName() + Utils.OSUtils.directorySep() + "times-" + suffix);
			timeVarStream = new FileWriter(baseDir.getName() + Utils.OSUtils.directorySep() +outDir.getName() + Utils.OSUtils.directorySep() + "time-var-" + suffix);
			accuracyStream = new FileWriter(baseDir.getName() + Utils.OSUtils.directorySep() +outDir.getName() + Utils.OSUtils.directorySep() + "accuracy-" + suffix);
			prunedStream = new FileWriter(baseDir.getName() + Utils.OSUtils.directorySep() +outDir.getName() + Utils.OSUtils.directorySep() + "pruned-" + suffix);

			if (datasets == null) {
				for (int i = 0; i < bounds.length; i++) {
					runNN(bounds[i].bound, bounds[i].k, w, winPercent, sort, timesStream, timeVarStream, accuracyStream, prunedStream, datasetDirectory);
				}
			}
			else {
				for (String d: datasets) {
					timesStream.write(d);
					timeVarStream.write(d);
					accuracyStream.write(d);
					prunedStream.write(d);
					
					for (int i = 0; i < bounds.length; i++) {
						runNNd(bounds[i].bound, bounds[i].k,  w, winPercent, d, sort, timesStream, timeVarStream, accuracyStream, prunedStream, datasetDirectory);
					}

					timesStream.write("\n");
					timeVarStream.write("\n");
					accuracyStream.write("\n");
					prunedStream.write("\n");
				}
			}
		} catch (IOException e) {
			System.out.println("Could not open output files!");
			return;
		} finally {
			try {
				if (timesStream != null) {
					timesStream.close();
					timesStream = null;
				}
				if (timeVarStream != null) {
					timeVarStream.close();
					timeVarStream = null;
				}
				if (accuracyStream != null) {
					accuracyStream.close();
					accuracyStream = null;
				}
				if (prunedStream != null) {
					prunedStream.close();
					prunedStream = null;
				}
			} catch (IOException e) {
				System.out.println("Could not close output files!");
				return;
			}
		}
	}
	
	// runNN for a specified single dataset
	static void runNNd(distance.BoundsID bound, int k, int w, boolean winPercent, String dataset, experimentType sort, FileWriter timesStream, FileWriter timeVarStream, FileWriter accuracyStream, FileWriter prunedStream, String datasetDirectory){
		FileIterator files = new FileIterator(verbosity, datasetDirectory);
		File trainFile = null;
		File testFile = null;

		String boundDesc = bound.name();
	
		if (bound == distance.BoundsID.Enhanced || bound == distance.BoundsID.EnhancedWebb) {
			boundDesc += "(" + k + ")";
		}

		if (!files.getByName(dataset)) {
			System.err.println("Cannot open dataset " + dataset);
			return;
		}

		trainFile = files.getTrain();
		testFile = files.getTest();
		// call garbage collector before processing to minimise the amount that
		// it pollutes timing.
		// Note, make the call as far in advance of starting the timer as
		// possible in case it runs in parallel
		System.gc();

		System.out.println("Processing: " + files.getName() + " with " + boundDesc);

		Dataset train = new Dataset();
		Dataset test = new Dataset();

		train.load(trainFile);
		test.load(testFile);

		int thisW = w;

		if (w == -2) {
			thisW = Utils.UCRInfo.getWindow(files.getName());
		}

		doNN(bound, k, train, test, thisW, winPercent, sort, timesStream, timeVarStream, accuracyStream, prunedStream);
		
		// flush the output files in case the process times out before all bounds are completed
		try {
			timesStream.flush();
			timeVarStream.flush();
			accuracyStream.flush();
			prunedStream.flush();
		} catch (IOException e) {
		}
	}
	
	static void runNN(distance.BoundsID bound, int k, int w, boolean winPercent, experimentType sort, FileWriter timesStream, FileWriter timeVarStream, FileWriter accuracyStream, FileWriter prunedStream, String datasetDirectory){
		FileIterator files = new FileIterator(verbosity, datasetDirectory);
		File trainFile = null;
		File testFile = null;

		try {
			String boundDesc = bound.name();
			
			if (bound == distance.BoundsID.Enhanced || bound == distance.BoundsID.EnhancedWebb) {
				boundDesc += "(" + k + ")";
			}
			
			timesStream.write(boundDesc);
			timeVarStream.write(boundDesc);
			accuracyStream.write(boundDesc);
			prunedStream.write(boundDesc);

			files.getNext();
			if (w == -2) {
				while (!files.atEnd() && Utils.UCRInfo.getWindow(files.getName()) == 0) {
					files.getNext();
				}
			}
			
			trainFile = files.getTrain();
			testFile = files.getTest();
			while (trainFile != null) {
				// call garbage collector before processing to minimise the amount that it pollutes timing.
				// Note, make the call as far in advance of starting the timer as possible in case it runs in parallel
				System.gc();
				
				System.out.println("Processing: " + files.getName()+" with "+boundDesc);
	
				Dataset train = new Dataset();
				Dataset test = new Dataset();
	
				train.load(trainFile);
				test.load(testFile);
				
				int thisW = w;
				
				if (w == -2) {
					thisW = Utils.UCRInfo.getWindow(files.getName());
				}
				
				doNN(bound, k, train, test, thisW, winPercent, sort, timesStream, timeVarStream, accuracyStream, prunedStream);
	
				files.getNext();
				if (w == -2) {
					while (!files.atEnd() && Utils.UCRInfo.getWindow(files.getName()) == 0) {
						files.getNext();
					}
				}
				trainFile = files.getTrain();
				testFile = files.getTest();
			}

			timesStream.write("\n");
			timeVarStream.write("\n");
			accuracyStream.write("\n");
			prunedStream.write("\n");
		} catch (IOException e) {
			System.out.println("Output error!");
			return;
		}
	}
	
	private static class statistics {
		public int errors = 0;
		public int correct = 0;
		public int pruned = 0;
		public int prepruned = 0;
	}
	
	static void doNN(distance.BoundsID bound, int k, Dataset train, Dataset test, int w, boolean winPercent, experimentType sort, FileWriter timesStream, FileWriter timeVarStream, FileWriter accuracyStream, FileWriter prunedStream){
		double uetest[] = null;
		double letest[] = null;
		//lb2[i] = new double[data.series.get(i).sequence.length];
		//ub2[i] = new double[data.series.get(i).sequence.length];
		//distance.Bounds.lemireGetEnvelopes(data.series.get(i).sequence, 2, ub2[i], lb2[i]);
		statistics stats = new statistics();
		
		// get the upper and lower envelopes of all training series unless the bound does not need it
		if (bound != distance.BoundsID.None) {
			for (int i = 0; i < train.series.size(); i++) {
				ClassifiedSequence seq = train.series.get(i);
				int l = seq.sequence.length;
				int thisW;
				if (winPercent) thisW = l*w/100;
				else thisW = w;
				
				seq.lower = new double[l];
				seq.upper = new double[l];
				if (debug) distance.Bounds.simpleGetEnvelopes(seq.sequence, thisW, seq.upper, seq.lower);
				else distance.Bounds.lemireGetEnvelopes(seq.sequence, thisW, seq.upper, seq.lower);
				
				if (bound == distance.BoundsID.Webb || bound == distance.BoundsID.EnhancedWebb) {
					seq.lowerUpper = new double[l];
					seq.upperLower = new double[l];
					distance.Bounds.lemireGetUpper(seq.lower, thisW, seq.upperLower);
					distance.Bounds.lemireGetLower(seq.upper, thisW, seq.lowerUpper);
				}
			}
		}
		
		double meanTime = 0;
		double varianceTime = 0;
		
		long startTime = System.currentTimeMillis();
		for (int t = 1; t <= 10; t++) {
			
			switch (sort) {
			case unsorted:
				do1NNunsorted(bound, k, train, test, w, winPercent, timesStream, timeVarStream, accuracyStream, prunedStream, uetest, letest, stats);
				break;
			case shuffled:
				train.shuffle(t);
				do1NNunsorted(bound, k, train, test, w, winPercent, timesStream, timeVarStream, accuracyStream, prunedStream, uetest, letest, stats);
				break;
			case sorted:
				do1NNsorted(bound, k, train, test, w, winPercent, timesStream, timeVarStream, accuracyStream, prunedStream, uetest, letest, stats);
				break;
			}

			long endTime = System.currentTimeMillis();
			double thisTime= endTime - startTime;
			double oldMeanTime = meanTime;
			meanTime += (thisTime-meanTime)/t;
			varianceTime += (thisTime - meanTime) * (thisTime - oldMeanTime);
			startTime = endTime;
		}
		
		try {
			timesStream.write(","+meanTime);
			timeVarStream.write(","+Math.sqrt(varianceTime/10.0));
			accuracyStream.write(","+(stats.correct/(double)(stats.correct+stats.errors)));
			prunedStream.write(","+stats.pruned);
		} catch (IOException e) {
			System.out.println("Output error!");
			return;
		}
	}
	
	private static void do1NNunsorted (
			distance.BoundsID bound, int k, Dataset train, Dataset test, int w, boolean winPercent, FileWriter timesStream, FileWriter timeVarStream, FileWriter accuracyStream, FileWriter prunedStream,
			double uetest[],
			double letest[],
			statistics stats
			) {
		
		for (int i = 0; i < test.series.size(); i++) {
			ClassifiedSequence testSeries = test.series.get(i);
			int l = testSeries.sequence.length;
			double nearestD = Double.MAX_VALUE;
			double nearestClass = 0;
			
			if (bound == distance.BoundsID.Webb || bound == distance.BoundsID.EnhancedWebb || bound == distance.BoundsID.Petitjean) {
				if (uetest == null) {
					uetest = new double[l];
					letest = new double[l];
				}
				else if (uetest.length < l) {
					uetest = new double[l];
					letest = new double[l];
				}
				int thisW;
				if (winPercent) thisW = l*w/100;
				else thisW = w;
				if (debug) distance.Bounds.simpleGetEnvelopes(testSeries.sequence, thisW, uetest, letest);
				else distance.Bounds.lemireGetEnvelopes(testSeries.sequence, thisW, uetest, letest);
			}

			for (int j = 0; j < train.series.size(); j++) {
				double thisBound;
				int thisW;
				if (winPercent) thisW = l*w/100;
				else thisW = w;
				
				ClassifiedSequence trainSeries = train.series.get(j);
				
				if (j==0) {
					thisBound = 0;
				}
				else {

					switch (bound) {
					case None:
						thisBound = 0;
						break;

					case Keogh:
						thisBound = distance.Bounds.lbKeogh(testSeries.sequence, trainSeries.upper, trainSeries.lower, nearestD);
						break;
						
					case Improved:
						thisBound = distance.Bounds.lbImproved(testSeries.sequence, trainSeries.sequence, trainSeries.upper, trainSeries.lower, thisW, nearestD);
						break;

					case Enhanced:
						thisBound = distance.Bounds.lbKeogh(testSeries.sequence, trainSeries.upper, trainSeries.lower, nearestD);
						break;
						
					case Petitjean:
						thisBound = distance.Bounds.lbPetitjean(testSeries.sequence, uetest, letest, trainSeries.sequence, trainSeries.upper, trainSeries.lower, thisW, nearestD);
						break;

					case Webb:
						thisBound = distance.Bounds.lbWebb(testSeries.sequence, uetest, letest, trainSeries.sequence, trainSeries.upper, trainSeries.lower, trainSeries.lowerUpper, trainSeries.upperLower, thisW, nearestD);
						break;

					case EnhancedWebb:
						thisBound = distance.Bounds.enhancedLBWebb(testSeries.sequence, uetest, letest, trainSeries.sequence, trainSeries.upper, trainSeries.lower, trainSeries.lowerUpper, trainSeries.upperLower, k, thisW, nearestD);
						break;
						
					default:
						System.out.println("Bound "+bound+" not handled!");
						return;
					}
				}

//				if (thisBound > distance.elastic.DTW.distance(testSeries.sequence, trainSeries.sequence, thisW)){
//					System.out.println("Lower bound exceeds distance!");
//				}
				
				if (thisBound >= nearestD) {
					stats.pruned++;
				}
				else {
					double thisDist = distance.elastic.DTW.distance(testSeries.sequence, trainSeries.sequence, thisW);
					
					if (thisDist < nearestD) {
						nearestD = thisDist;
						nearestClass = trainSeries.klass;
					}
				}
			}
			
			if (nearestClass == testSeries.klass) stats.correct++;
			else stats.errors++;
		}
		
//		if ((stats.correct + stats.errors) % test.series.size() != 0) {
//			System.out.println(stats.correct + " correct, " + stats.errors + " errors, but " + test.series.size() + " cases.");
//			System.out.println("Correct plus errors does not equal N!");
//		}
//		else {
//			System.out.println(stats.correct + " correct, " + stats.errors + " errors, " + test.series.size() + " cases.");
//		}
	}
	
	private static void do1NNsorted (
			distance.BoundsID bound, int k, Dataset train, Dataset test, int w, boolean winPercent, FileWriter timesStream, FileWriter timeVarStream, FileWriter accuracyStream, FileWriter prunedStream,
			double uetest[],
			double letest[],
			statistics stats
			) {

		for (int i = 0; i < test.series.size(); i++) {
			ClassifiedSequence testSeries = test.series.get(i);
			int l = testSeries.sequence.length;
			int thisW;
			if (winPercent) thisW = l*w/100;
			else thisW = w;
			double nearestClass = 0;
			
			if (bound == distance.BoundsID.Petitjean || bound == distance.BoundsID.Webb || bound == distance.BoundsID.EnhancedWebb) {
				if (uetest == null) {
					uetest = new double[l];
					letest = new double[l];
				}
				else if (uetest.length < l) {
					uetest = new double[l];
					letest = new double[l];
				}
				if (debug) distance.Bounds.simpleGetEnvelopes(testSeries.sequence, thisW, uetest, letest);
				else distance.Bounds.lemireGetEnvelopes(testSeries.sequence, thisW, uetest, letest);
			}
			
			// sort the data
			for (int j = 0; j < train.series.size(); j++) {
				ClassifiedSequence trainSeries = train.series.get(j);

				double thisBound = 0.0;
				
				switch (bound) {
				case None:
					thisBound = 0;
					break;
	
				case Keogh:
					thisBound = distance.Bounds.lbKeogh(testSeries.sequence, trainSeries.upper, trainSeries.lower, Double.MAX_VALUE);
					break;
					
				case Improved:
					thisBound = distance.Bounds.lbImproved(testSeries.sequence, trainSeries.sequence, trainSeries.upper, trainSeries.lower, thisW, Double.MAX_VALUE);
					break;
					
				case Enhanced:
					thisBound = distance.Bounds.lbEnhanced(testSeries.sequence, trainSeries.sequence, trainSeries.upper, trainSeries.lower, k, thisW, Double.MAX_VALUE);
					break;
					
				case Petitjean:
					thisBound = distance.Bounds.lbPetitjean(testSeries.sequence, uetest, letest, trainSeries.sequence, trainSeries.upper, trainSeries.lower, thisW, Double.MAX_VALUE);
					break;
	
				case Webb:
					thisBound = distance.Bounds.lbWebb(testSeries.sequence, uetest, letest, trainSeries.sequence, trainSeries.upper, trainSeries.lower, trainSeries.lowerUpper, trainSeries.upperLower, thisW, Double.MAX_VALUE);
					break;
					
				case EnhancedWebb:
					thisBound = distance.Bounds.enhancedLBWebb(testSeries.sequence, uetest, letest, trainSeries.sequence, trainSeries.upper, trainSeries.lower, trainSeries.lowerUpper, trainSeries.upperLower, k, thisW, Double.MAX_VALUE);
					break;
					
				default:
					System.out.println("Bound "+bound+" not handled!");
					return;
				}
				trainSeries.lb = thisBound;
				if (debug && thisBound > distance.elastic.DTW.distance(testSeries.sequence, trainSeries.sequence, thisW)) {
					System.out.println("Lower bound exceeds true distance: " + thisBound + " - " + distance.elastic.DTW.distance(testSeries.sequence, trainSeries.sequence, thisW));
					testSeries.print(System.out);
					trainSeries.print(System.out);
					return;
				}
			}
			
			Collections.sort(train.series);

			double nearestD = distance.elastic.DTW.distance(testSeries.sequence, train.series.get(0).sequence, thisW);
			nearestClass = train.series.get(0).klass;

			for (int j = 1; j < train.series.size(); j++) {
				double thisBound = train.series.get(j).lb;
				
				if (thisBound >= nearestD) {
					stats.pruned += train.series.size() - j;
					break;
				}
				else {
					double thisDist = distance.elastic.DTW.distance(testSeries.sequence, train.series.get(j).sequence, thisW);
					
					if (thisDist < nearestD) {
						nearestD = thisDist;
						nearestClass = train.series.get(j).klass;
					}
				}
			}
			
			if (nearestClass == testSeries.klass) stats.correct++;
			else stats.errors++;
		}
	}
	

//	static void testEnhanced() {
//		FileIterator trainFiles = new FileIterator(verbosity);
//		File trainFile = null;
//		ArrayList<ArrayList<Long>> time = new ArrayList<ArrayList<Long>>(0);
//		ArrayList<ArrayList<Double>> tightness = new ArrayList<ArrayList<Double>>(0);
//
//		trainFiles.getNext();
//		trainFile = trainFiles.getTrain();
//		while (trainFile != null) {
//			System.out.println("Processing: " + trainFile.getName());
//
//			Dataset d = new Dataset();
//			ArrayList<Long> thistime = new ArrayList<Long>(0);
//			ArrayList<Double> thistightness = new ArrayList<Double>(0);
//
//			d.load(trainFile, false);
//			doTestEnhanced(d, thistime, thistightness);
//			time.add(thistime);
//			tightness.add(thistightness);
//			trainFiles.getNext();
//			trainFile = trainFiles.getTrain();
//		}
//
//		System.out.println();
//		System.out.println("Tightness,W2+K2,W2+K4,W5+K2");
//		int i = 0;
//
//		trainFiles.reset();
//		trainFiles.getNext();
//		trainFile = trainFiles.getTrain();
//
//		while (trainFile != null) {
//			System.out.print(trainFiles.getName());
//
//			ArrayList<Double> thistightness = tightness.get(i++);
//			for (int j = 0; j < thistightness.size(); j++)
//				System.out.print("," + thistightness.get(j));
//			System.out.println();
//			trainFiles.getNext();
//			trainFile = trainFiles.getTrain();
//		}
//
//		System.out.println();
//		System.out.println("Time,W2+K2,W2+K3,W5+K3");
//		i = 0;
//		trainFiles.reset();
//		trainFiles.getNext();
//		trainFile = trainFiles.getTrain();
//
//		while (trainFile != null) {
//			System.out.print(trainFiles.getName());
//			ArrayList<Long> thistime = time.get(i++);
//			for (int j = 0; j < thistime.size(); j++)
//				System.out.print("," + thistime.get(j));
//			System.out.println();
//			trainFiles.getNext();
//			trainFile = trainFiles.getTrain();
//		}
//	}

	
	static void doTestTightness(File baseDir, File outDir, String suffix, String datasets[], boundInfo bounds[], int w, boolean winPercent, experimentType sort, String datasetDirectory) {
		FileWriter resultStream = null;
		
		try {
			resultStream = new FileWriter(baseDir.getName() + Utils.OSUtils.directorySep() +outDir.getName() + Utils.OSUtils.directorySep() + "tightness-" + suffix);

			if (datasets == null) {
				for (int i = 0; i < bounds.length; i++) {
					runTestTightness(bounds[i].bound, bounds[i].k, w, winPercent, resultStream, datasetDirectory);
				}
			}
			else {
				for (String d: datasets) {
					resultStream.write(d);
					
					for (int i = 0; i < bounds.length; i++) {
						runTestTightnessd(bounds[i].bound, bounds[i].k, w, winPercent, d, resultStream, datasetDirectory);
					}

					resultStream.write("\n");
				}
			}
		} catch (IOException e) {
			System.out.println("Could not open output files!");
			return;
		} finally {
			try {
				if (resultStream != null) {
					resultStream.close();
					resultStream = null;
				}
			} catch (IOException e) {
				System.out.println("Could not close output files!");
				return;
			}
		}
	}
	
	static void runTestTightness(distance.BoundsID bound, int k, int w, boolean winPercent, FileWriter resultStream, String datasetDirectory){
		FileIterator files = new FileIterator(verbosity, datasetDirectory);
		File trainFile = null;
		File testFile = null;

		try {
			String boundDesc = bound.name();
			
			if (bound == distance.BoundsID.Enhanced || bound == distance.BoundsID.EnhancedWebb) {
				boundDesc += "(" + k + ")";
			}
			
			resultStream.write(boundDesc);

			files.getNext();
			if (w == -2) {
				while (!files.atEnd() && Utils.UCRInfo.getWindow(files.getName()) == 0) {
					files.getNext();
				}
			}
			
			trainFile = files.getTrain();
			testFile = files.getTest();
			while (trainFile != null) {
				// call garbage collector before processing to minimise the amount that it pollutes timing.
				// Note, make the call as far in advance of starting the timer as possible in case it runs in parallel
				System.gc();
				
				System.out.println("Processing: " + files.getName()+" with "+boundDesc);
	
				Dataset train = new Dataset();
				Dataset test = new Dataset();
	
				train.load(trainFile);
				test.load(testFile);
				
				int thisW = w;
				
				if (w == -2) {
					thisW = Utils.UCRInfo.getWindow(files.getName());
				}
				
				doTT(bound, k, train, test, thisW, winPercent, resultStream);
	
				files.getNext();
				if (w == -2) {
					while (!files.atEnd() && Utils.UCRInfo.getWindow(files.getName()) == 0) {
						files.getNext();
					}
				}
				trainFile = files.getTrain();
				testFile = files.getTest();
			}

			resultStream.write("\n");
		} catch (IOException e) {
			System.out.println("Output error!");
			return;
		}
	}

	// runNN for a specified single dataset
	static void runTestTightnessd(distance.BoundsID bound, int k, int w, boolean winPercent, String dataset, FileWriter resultsStream, String datasetDirectory){
		FileIterator files = new FileIterator(verbosity, datasetDirectory);
		File trainFile = null;
		File testFile = null;

		String boundDesc = bound.name();
	
		if (bound == distance.BoundsID.Enhanced || bound == distance.BoundsID.EnhancedWebb) {
			boundDesc += "(" + k + ")";
		}

		if (!files.getByName(dataset)) {
			System.err.println("Cannot open dataset " + dataset);
			return;
		}

		trainFile = files.getTrain();
		testFile = files.getTest();
		// call garbage collector before processing to minimise the amount that
		// it pollutes timing.
		// Note, make the call as far in advance of starting the timer as
		// possible in case it runs in parallel
		System.gc();

		System.out.println("Processing: " + files.getName() + " with " + boundDesc);

		Dataset train = new Dataset();
		Dataset test = new Dataset();

		train.load(trainFile);
		test.load(testFile);

		int thisW = w;

		if (w == -2) {
			thisW = Utils.UCRInfo.getWindow(files.getName());
		}

		doTT(bound, k, train, test, thisW, winPercent, resultsStream);
		
		// flush the output files in case the process times out before all bounds are completed
		try {
			resultsStream.flush();
		} catch (IOException e) {
		}
	}

	static void doTT(distance.BoundsID bound, int k, Dataset train, Dataset test, int w, boolean winPercent, FileWriter resultStream){
		double uetest[] = null;
		double letest[] = null;
		double avTightness = 0.0;
		long count = 0;
		
		// get the upper and lower envelopes of all training series unless the bound
		// does not need it
		if (bound != distance.BoundsID.None) {
			for (int i = 0; i < train.series.size(); i++) {
				ClassifiedSequence seq = train.series.get(i);
				int l = seq.sequence.length;
				int thisW;
				if (winPercent)
					thisW = l * w / 100;
				else
					thisW = w;

				seq.lower = new double[l];
				seq.upper = new double[l];
				if (debug)
					distance.Bounds.simpleGetEnvelopes(seq.sequence, thisW, seq.upper, seq.lower);
				else
					distance.Bounds.lemireGetEnvelopes(seq.sequence, thisW, seq.upper, seq.lower);

				if (bound == distance.BoundsID.Webb || bound == distance.BoundsID.EnhancedWebb) {
					seq.lowerUpper = new double[l];
					seq.upperLower = new double[l];
					distance.Bounds.lemireGetUpper(seq.lower, thisW, seq.upperLower);
					distance.Bounds.lemireGetLower(seq.upper, thisW, seq.lowerUpper);
				}
			}
		}

		for (int i = 0; i < test.series.size(); i++) {
			ClassifiedSequence testSeries = test.series.get(i);
			int l = testSeries.sequence.length;

			if (bound == distance.BoundsID.Webb || bound == distance.BoundsID.Petitjean || bound == distance.BoundsID.EnhancedWebb) {
				if (uetest == null) {
					uetest = new double[l];
					letest = new double[l];
				} else if (uetest.length < l) {
					uetest = new double[l];
					letest = new double[l];
				}
				int thisW;
				if (winPercent)
					thisW = l * w / 100;
				else
					thisW = w;
				if (debug)
					distance.Bounds.simpleGetEnvelopes(testSeries.sequence, thisW, uetest, letest);
				else
					distance.Bounds.lemireGetEnvelopes(testSeries.sequence, thisW, uetest, letest);
			}

			for (int j = 0; j < train.series.size(); j++) {
				double thisBound;
				int thisW;
				if (winPercent)
					thisW = l * w / 100;
				else
					thisW = w;
				
				ClassifiedSequence trainSeries = train.series.get(j);

				switch (bound) {
				case None:
					thisBound = 0;
					break;

				case Keogh:
					thisBound = distance.Bounds.lbKeogh(testSeries.sequence, trainSeries.upper,
								trainSeries.lower, Double.MAX_VALUE);
					break;

				case Improved:
					thisBound = distance.Bounds.lbImproved(testSeries.sequence, trainSeries.sequence,
							trainSeries.upper, trainSeries.lower, thisW, Double.MAX_VALUE);
					break;
					
				case Enhanced:
					thisBound = distance.Bounds.lbEnhanced(testSeries.sequence, trainSeries.sequence, trainSeries.upper, trainSeries.lower, k, thisW, Double.MAX_VALUE);
					break;

				case Petitjean:
					thisBound = distance.Bounds.lbPetitjean(testSeries.sequence, uetest, letest,
								trainSeries.sequence, trainSeries.upper, trainSeries.lower,
								thisW, Double.MAX_VALUE);
					break;

				case Webb:
					thisBound = distance.Bounds.lbWebb(testSeries.sequence, uetest, letest,
								trainSeries.sequence, trainSeries.upper, trainSeries.lower,
								trainSeries.lowerUpper, trainSeries.upperLower, thisW, Double.MAX_VALUE);
					break;

				case EnhancedWebb:
					thisBound = distance.Bounds.enhancedLBWebb(testSeries.sequence, uetest, letest,
								trainSeries.sequence, trainSeries.upper, trainSeries.lower,
								trainSeries.lowerUpper, trainSeries.upperLower, k, thisW, Double.MAX_VALUE);
					break;

				default:
					System.err.println("Bound " + bound + " not handled!");
					return;
				}

				double thisDist = distance.elastic.DTW.distance(testSeries.sequence, trainSeries.sequence, thisW);
				
				if (thisBound > thisDist + delta) {
				//if (thisBound > thisDist) {
					String boundDesc = bound.name();
					
					if (bound == distance.BoundsID.Enhanced || bound == distance.BoundsID.EnhancedWebb) {
						boundDesc += "(" + k + ")";
					}
					
					System.err.println("Lower bound "+thisBound+" exceeds distance "+thisDist+" for bound " + boundDesc + "!");
					trainSeries.print(System.err);
					testSeries.print(System.err);
					return;
				}
				
				if (thisDist>0.0) {
					// Exclude cases where the true distance is 0.0
					double thisTightness = thisBound / thisDist;
					avTightness += (thisTightness-avTightness)/++count;
				}
			}
		}

		try {
			resultStream.write("," + avTightness);
		} catch (IOException e) {
			System.out.println("Output error!");
			return;
		}
	}

	private static int verbosity = 1;
	public static boolean debug=false;
	private static double delta = 1e-8;
}
