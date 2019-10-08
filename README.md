# DTWBounds
Code to reproduce experiments in Tight lower bounds for Dynamic Time Warping
Geoffrey I. Webb and Francois Petitjean

Presents two novel DTW lower bounds: LB_Petitjean and LB_Webb

To download the code, datasets and replicate the experiments on Linux

# download the source code:
git clone https://github.com/GIWebb/DTWBounds

# download the datasets
wget https://www.cs.ucr.edu/~eamonn/time_series_data/UCR_TS_Archive_2015.zip

# for password read instructions at
# https://www.cs.ucr.edu/~eamonn/time_series_data/UCR%20Time%20Series%20Classification%20Archive.pdf
unzip UCR_TS_Archive_2015.zip

# compile jave source
javac -sourcepath DTWBounds/src DTWBounds/src/*/*/*.java DTWBounds/src/*/*.java

# tightness
# -b argument lists the bounds to be tested
# -W indicates to used 'optimal' window size for each dataset
#    (and skip datasets for which it is zero)
# -t indicates to do a tightness test
# The output file will be generated in a directory with a random numeric name
# The output file will contain a row for each bound and a column for each dataset
java -cp DTWBounds/src TSTester.TSTester -bkeogh,improved,petitjean,webb,enhanced1,enhanced2,enhanced3,enhanced4,enhanced5,enhanced6,enhanced7,enhanced8,enhanced9,enhanced10,enhanced11,enhanced12,enhanced13,enhanced14,enhanced15,enhanced16,enhancedwebb0,enhancedwebb3 -W -t

# sorted optimal windows
# -b argument lists the bounds to be tested
# -W indicates to used 'optimal' window size for each dataset
#    (and skip datasets for which it is zero)
# -s indicates to search in sorted order
# The output files will be generated in a directory with a random numeric name
# The output files will contain a row for each bound and a column for each dataset
# accuracy-*.csv will list the accuracy. This should be the same for every bound
# pruned-*.csv will list the number of series that were pruned
# times-*.csv will list the average time in milliseconds to classify the test set
# time-*.csv will ist the variance in the time over the ten runs
java -cp DTWBounds/src TSTester.TSTester -bkeogh,improved,petitjean,webb,enhanced1,enhanced2,enhanced3,enhanced4,enhanced5,enhanced6,enhanced7,enhanced8,enhanced9,enhanced10,enhanced11,enhanced12,enhanced13,enhanced14,enhanced15,enhanced16,enhancedwebb0,enhancedwebb3 -W -s

# unsorted optimal windows
# -b argument lists the bounds to be tested
# -W indicates to used 'optimal' window size for each dataset
#    (and skip datasets for which it is zero)
# unsorted is the default
# The output files will be generated in a directory with a random numeric name
# The output files will contain a row for each bound and a column for each dataset
# accuracy-*.csv will list the accuracy. This should be the same for every bound
# pruned-*.csv will list the number of series that were pruned
# times-*.csv will list the average time in milliseconds to classify the test set
# time-*.csv will ist the variance in the time over the ten runs
java -cp DTWBounds/src TSTester.TSTester -bkeogh,improved,petitjean,webb,enhanced1,enhanced2,enhanced3,enhanced4,enhanced5,enhanced6,enhanced7,enhanced8,enhanced9,enhanced10,enhanced11,enhanced12,enhanced13,enhanced14,enhanced15,enhanced16,enhancedwebb0,enhancedwebb3 -W

# sorted 1% windows
# -b argument lists the bounds to be tested
# -W indicates to used 'optimal' window size for each dataset
#    (and skip datasets for which it is zero)
# -s indicates to search in sorted order
# -g1 indicates to use windows 1% of series length
# The output files will be generated in a directory with a random numeric name
# The output files will contain a row for each bound and a column for each dataset
# accuracy-*.csv will list the accuracy. This should be the same for every bound
# pruned-*.csv will list the number of series that were pruned
# times-*.csv will list the average time in milliseconds to classify the test set
# time-*.csv will ist the variance in the time over the ten runs
java -cp DTWBounds/src TSTester.TSTester -bkeogh,improved,petitjean,webb,enhanced1,enhanced2,enhanced3,enhanced4,enhanced5,enhanced6,enhanced7,enhanced8,enhanced9,enhanced10,enhanced11,enhanced12,enhanced13,enhanced14,enhanced15,enhanced16,enhancedwebb0,enhancedwebb3 -W -s -g1

# sorted 10% windows
# -b argument lists the bounds to be tested
# -W indicates to used 'optimal' window size for each dataset
#    (and skip datasets for which it is zero)
# -s indicates to search in sorted order
# -g10 indicates to use windows 10% of series length
# The output files will be generated in a directory with a random numeric name
# The output files will contain a row for each bound and a column for each dataset
# accuracy-*.csv will list the accuracy. This should be the same for every bound
# pruned-*.csv will list the number of series that were pruned
# times-*.csv will list the average time in milliseconds to classify the test set
# time-*.csv will ist the variance in the time over the ten runs
java -cp DTWBounds/src TSTester.TSTester -bkeogh,improved,petitjean,webb,enhanced1,enhanced2,enhanced3,enhanced4,enhanced5,enhanced6,enhanced7,enhanced8,enhanced9,enhanced10,enhanced11,enhanced12,enhanced13,enhanced14,enhanced15,enhanced16,enhancedwebb0,enhancedwebb3 -W -s -g10

# sorted 20% windows
# -b argument lists the bounds to be tested
# -W indicates to used 'optimal' window size for each dataset
#    (and skip datasets for which it is zero)
# -s indicates to search in sorted order
# -g20 indicates to use windows 20% of series length
# The output files will be generated in a directory with a random numeric name
# The output files will contain a row for each bound and a column for each dataset
# accuracy-*.csv will list the accuracy. This should be the same for every bound
# pruned-*.csv will list the number of series that were pruned
# times-*.csv will list the average time in milliseconds to classify the test set
# time-*.csv will ist the variance in the time over the ten runs
java -cp DTWBounds/src TSTester.TSTester -bkeogh,improved,petitjean,webb,enhanced1,enhanced2,enhanced3,enhanced4,enhanced5,enhanced6,enhanced7,enhanced8,enhanced9,enhanced10,enhanced11,enhanced12,enhanced13,enhanced14,enhanced15,enhanced16,enhancedwebb0,enhancedwebb3 -W -s -g20
