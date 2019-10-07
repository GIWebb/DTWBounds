package TSTester;

import java.io.PrintStream;
import java.util.*;

public class ClassifiedSequence implements Comparator<ClassifiedSequence>, Comparable<ClassifiedSequence> {
	// Overriding the compareTo method to sort on lb
	public int compareTo(ClassifiedSequence d) {
		if (this.lb > d.lb)
			return 1;
		else if (this.lb == d.lb)
			return 0;
		else
			return -1;
	}

	// Overriding the compare method to sort on lb
	public int compare(ClassifiedSequence s1, ClassifiedSequence s2) {
		if (s1.lb > s2.lb)
			return 1;
		else if (s1.lb == s2.lb)
			return 0;
		else
			return -1;
	}
	
	public void print(PrintStream out) {
		out.print(this.klass);
		for (int i = 0; i < this.sequence.length; i++) {
			out.print(","+this.sequence[i]);
		}
		out.println();
	}

	public double[] sequence;
	public double[] upper = null;
	public double[] lower = null;
	public double[] lowerUpper = null;
	public double[] upperLower = null;
	public int klass;
	public double lb;
}
