package Utils;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

public class FileIterator {
	public FileIterator (int verbosity, String datasetDirectoryName) {
		datasetDirectory = datasetDirectoryName;
		rep = new File(datasetDirectory);
			
		if (verbosity>1) System.out.println("Opened files folder");
		
		listData = rep.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (verbosity>1) {
					if (pathname.isDirectory())
						System.out.println(pathname.getName()+" is a directory");
					else
						System.out.println(pathname.getName()+" is not a directory");
				}
				
				return pathname.isDirectory();
			}
		});
		System.out.println("found "+listData.length+" folders");
		Arrays.sort(listData);
		train = null;
		test = null;
		name = null;
		nextIndex = 0;
	}
	
	public void reset() {
		train = null;
		test = null;
		name = null;
		nextIndex = 0;
	}
	
	public boolean atEnd() {
		return (nextIndex >= listData.length);
	}
	
	public boolean getByName(String fileName) {
		boolean success = false;
		
				train = new File(datasetDirectory + "/" + fileName + "/" + fileName + "_TRAIN");

				test = new File(datasetDirectory+ "/" + fileName + "/" + fileName + "_TEST");

				if (train.exists() && train.canRead() && test.exists() && test.canRead()) {
					name = fileName;
					success = true;
				}

				return success;
	}

	public void getNext() {
		test = null;
		boolean finished = false;

		while (!finished) {
			if (nextIndex >= listData.length) {
				train = null;
				test = null;
				name = null;
				finished = true;
			}
			else {
				File dataRep = listData[nextIndex++];
				
				finished = getByName(dataRep.getName());
			}
		}
	}
	
	public File getTrain() {
		return train;
	}
	
	public File getTest () {
		return test;
	}
	
	public String getName () {
		return name;
	}

	// datasets folder: each subfolder contains a training and a test set
	private File train;
	private File test;
	private String name;
	private File rep;
	private File[] listData;
	private int nextIndex;
	private String datasetDirectory;
}
