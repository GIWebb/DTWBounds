package TSTester;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;

public class Dataset {
	public void Dataset() {
	}
	
	public void load(File Trainfile) {
		try {
			Scanner f = new Scanner(Trainfile);
			String line;
	
			while (f.hasNextLine()) {
				line = f.nextLine();
				if (verbosity>1) System.out.println("Processing line: " + line);
				
				Scanner lineScanner = new Scanner(line);
				
				buffer.clear();
				lineScanner.useDelimiter(", *");
	
				if (verbosity>1) System.out.println("Getting class");
				
				int thisKlass = lineScanner.nextInt();
			    
				if (verbosity>1) System.out.println("Class = " + thisKlass);
				
				if (verbosity>1) System.out.println("Getting sequence");
		        while (lineScanner.hasNextDouble()) {
		        	if (verbosity>1) System.out.println("Getting value");
					double dbl = lineScanner.nextDouble();
					if (verbosity>1) System.out.println("Value = " + dbl);

		        	buffer.add(dbl);
		        }
		        
				if (verbosity>1) System.out.println("Allocating s");
		        double[] s = new double[buffer.size()];
				if (verbosity>1) System.out.println("toArray(s)");
				for (int i = 0; i < buffer.size(); i++) {
					s[i] = buffer.get(i);
				}
		        
		        ClassifiedSequence cs = new ClassifiedSequence();
		        
		        cs.sequence = s;
		        cs.klass = thisKlass;
		        
				if (verbosity>1) System.out.println("series.add(cs)");
		        series.add(cs);
		        
		        if (lineScanner!=null) lineScanner.close();
			}
			
			if (f!= null) f.close();
			

		}
		catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException!");
		}
		finally {
	    }
	}
	
	public void shuffle(int seed) {
		rnd.setSeed(seed);
		Collections.shuffle(series, rnd);
	}

	public void print() {
		Iterator<ClassifiedSequence> seqenceIterator = series.iterator();
		
		while (seqenceIterator.hasNext()) {
			ClassifiedSequence cs = seqenceIterator.next();
			double[] seq = cs.sequence;
			
			System.out.print(cs.klass);
			
			for (int i = 0; i < seq.length; i++) {
				System.out.print(", ");
				System.out.print(seq[i]);
			}
			System.out.println();
		}
	}

	public ArrayList<ClassifiedSequence> series = new ArrayList<ClassifiedSequence>();
	public int count;		// number of series
	private int maxLength = 0;	// the maximum length of any series in the dataset
    private ArrayList<Double> buffer=new ArrayList<Double>();  
    private static int verbosity = 1;
    private Random rnd = new Random();
}

