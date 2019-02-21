# compiles all the singleRunData files in a given directory (first argument) into one medians' file 
# (ie, for each column and row, the median value across all xxx files is found). 
#
# Then plots the effector T cell responses. 

import sys
import numpy
import matplotlib.pyplot as plt
import glob
import os


def myround(x, base=5):
	"""
	Rounds 'x' to the nearest 'base'. 
	Code taken from here, on the 16/02/2015:
	http://stackoverflow.com/questions/2272149/round-to-5-or-other-number-in-python
	"""
	return int(base * round(float(x)/base))


def plot_performance(savePath, dataPath, savePostfix=''):
	"""
	Plots graphs of simulation performance for this corresponding candidate.

	arguments:
	  savePath - the location where the graph is to be saved
	  savePostFix - text appended to file name, to make it distinctive. e.g. "_ind1" would give files called
	  				"path/T-cell-dynamics_ind1.png"
	  dataPath - the location of the data file to be graphed
	"""
	with open(dataPath, 'r') as f:
		data = numpy.loadtxt(f)

	time = data[:,0]
	timed = time / 24			# time in days.
	cd4th1 = data[:,5]
	cd4th2 = data[:,6]
	cd4treg = data[:,12]
	cd8treg = data[:,18]

	plt.figure(dataPath)

	lines = plt.plot(timed, cd4th1, 'r-', timed, cd4th2, 'c-', timed, cd4treg, 'b-', timed, cd8treg, 'g-')
	try:
		endTime = myround(timed[-1] - 1)
		plt.xlim((0, endTime))		# This is causing an intermittent error, I'm not sure why.
	except ValueError as e:
		print "ERROR: plot_performance: something failed in plt.xlim([0, 50])"
		print "ERROR: directory = " + dataPath
		print "ERROR: timed: " + str(timed)
		print "ERROR: data.size = " + str(data.shape)
		print "ERROR: baseline.CD4Th1 = " + str(calib_baseline.data.shape)
		print "ERROR: original exception message = " + str(e)

	plt.xlabel('Time (days)')
	plt.ylabel('Cells')

	filePath = savePath + '/T-cell-dynamics' + savePostfix + '.png'
	plt.rcParams.update({'font.size': 18})
	plt.savefig(filePath, bbox_inches='tight', dpi=300)	# save figure, with high resolution.
	plt.close()


# ---------------- Program starts here ------------------

print sys.argv
directory = sys.argv[1]
runFiles  = glob.glob(os.path.join(directory, 'simOutputData_*.txt'))
seedFiles = glob.glob(os.path.join(directory, 'simRunSeed_*'))

fileArrays = None
for i, n in enumerate(runFiles):
	with open(n, 'r') as f:
		# numpy does all the f work and strips out the first row of comments.
		data = numpy.loadtxt(f)
	if fileArrays is None:		# initialise data structure on first visitation.
		fileArrays = numpy.zeros((len(runFiles), data.shape[0], data.shape[1]))
	fileArrays[i, :, :] = data

# prepare to write the distribution of response values to the file system.
# responses = calculate_write_objective_responses(directory=self.candDir, fileArrays=fileArrays)
# write median data to file system (for downstream analysis or graphing).
medianData = numpy.median(fileArrays, axis=0)
medianFilePath = os.path.join(directory, 'multipleDataOutput.txt')
with open(medianFilePath, 'w') as output:
	numpy.savetxt(output, medianData, fmt='%g')

plot_performance(savePath=directory, dataPath=medianFilePath)
