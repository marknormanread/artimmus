import sys
import numpy
import glob
import matplotlib.pyplot as plt
import re
import os
from xml.etree.ElementTree import Element, SubElement
from xml.etree import ElementTree
from xml.dom import minidom


data_dir = '.'
end_time = 50.0


# columns from the simulation output data files indices of the responses we are using.
# 5 = total_CD4Th1
# 6 = total_CD4Th2
# 12 = total_CD4TregActivated
# 18 = total_CD8TregActivated


def prettify(elem):
	"""
	Return a pretty-printed XML string for the Element.

	Taken from: http://pymotw.com/2/xml/etree/ElementTree/create.html on 19/08/2014
	"""
	rough_string = ElementTree.tostring(elem, 'utf-8')
	reparsed = minidom.parseString(rough_string)
	return reparsed.toprettyxml(indent="  ")


def natural_sort(l):
	convert = lambda text: int(text) if text.isdigit() else text.lower()
	alphanum_key = lambda key: [ convert(c) for c in re.split('([0-9]+)', key) ]
	return sorted(l, key=alphanum_key)


def plot_t_cell_timeseries(data, output_path=None, fn_postfix='', sr_data=None, ymax=None):
	""" 
	Plots median time series data - ie, no distribution error bars shown. 'data' argument is a 2D numpy array.
	"""
	def error_bars(sr_data, sr_data_col, y_locs, stagger, period, color):
		error_lower = []
		error_upper = []
		error_locations = []
		for t in range(stagger, timed.shape[0], period):		
			distro = sr_data[:,t,sr_data_col]
			qUpper, qLower = numpy.percentile(distro, [90,10])

			eUpper = qUpper - y_locs[t]
			eLower = y_locs[t] - qLower
			error_upper.append(eUpper)
			error_lower.append(eLower)
			error_locations.append(t)
			
		plt.errorbar(timed[error_locations], y_locs[error_locations], yerr=[error_lower,error_upper], ecolor=color, linestyle='None')

	if output_path is None:
		output_path = data_dir
	# pull relevant data
	time = data[:,0]
	timed = time / 24			# time in days.
	cd4th1 = data[:,5]
	cd4th2 = data[:,6]
	cd4treg = data[:,12]
	cd8treg = data[:,18]

	lines = plt.plot(timed, cd4th1, 'r-', timed, cd4th2, 'c-', timed, cd4treg, 'b-', timed, cd8treg, 'g-')
	if sr_data is not None:
		error_bars(sr_data, 5, y_locs=cd4th1, stagger=0, period=48, color='r')		# cd4Th1
		error_bars(sr_data, 6, y_locs=cd4th2, stagger=12, period=48, color='c')		# cd4Th2
		error_bars(sr_data, 12, y_locs=cd4treg, stagger=24, period=48, color='b')		# cd4Treg
		error_bars(sr_data, 18, y_locs=cd8treg, stagger=36, period=48, color='g')		# cd8Treg

	plt.xlim((0.0, end_time))
	y_lims = list(plt.gca().get_ylim())
	if ymax:
		y_lims[1] = ymax
	plt.ylim((0.0, y_lims[1]))
	plt.xlabel('Time (days)')
	plt.ylabel('Cells')
	filePath = output_path + '/T-cell-dynamics' + fn_postfix + '.png'
	plt.rcParams.update({'font.size': 18})
	plt.savefig(filePath, bbox_inches='tight', dpi=300)   # save figure, with high resolution.
	plt.close()


def _find_peak(data, col):
	"""
	Finds the biggest value in a particular column of the simOutputData files.
	Columns here represent particular cell dynamics (such as CD4Th1 over time).
	:data :  numpy 2D array, representing all the simOutputFiles of the candidate being assessed. Dimensions as
			follows: [<Row>,<Column>]
	"""
	rowOfMax = numpy.argmax(data[:, col])
	return data[rowOfMax, col]


def _find_time_of_peak(data, col):
	"""
	Find the time at which the biggest value in a particular column of the simOutputData file occurred.

	:data :  numpy 2D array, representing all the simOutputFiles of the candidate being assessed. Dimensions as
	follows: [<Row>,<Column>]
	"""
	rowOfMax = numpy.argmax(data[:, col])
	return data[rowOfMax, 0]   # time is always in the first column.


def _find_num_at_time(data, col, row):
	"""
	Finds the value of a particular column and row of the simOutputData files.
	Note that many files may be supplied.

	:data :  numpy 2D array, representing all the simOutputFiles of the candidate being assessed. Dimensions as
			follows: [<Row>,<Column>]
	"""
	return data[row, col]


class Responses:
	def __init__(self, sr_data):
		"""
		"""
		self.max_Th1 = []
		self.max_Th1_time = []
		self.max_Th2 = []
		self.max_Th2_time = []
		self.max_4Treg = []
		self.max_4Treg_time = []
		self.max_8Treg = []
		self.max_8Treg_time = []
		self.Th1_40d = []
		self.CD4Th2_30d = []
		self.num_reps = sr_data.shape[0]
		for r in range(sr_data.shape[0]):
			data = sr_data[r,:,:]
			# peaks
			self.max_Th1.append(_find_peak(data, col=5))
			self.max_Th2.append(_find_peak(data, col=6))
			self.max_4Treg.append(_find_peak(data, col=12))
			self.max_8Treg.append(_find_peak(data, col=18))
			# times
			self.max_Th1_time.append(_find_time_of_peak(data, col=5))
			self.max_Th2_time.append(_find_time_of_peak(data, col=6))
			self.max_4Treg_time.append(_find_time_of_peak(data, col=12))
			self.max_8Treg_time.append(_find_time_of_peak(data, col=18))
			# other
			self.Th1_40d.append(_find_num_at_time(data, col=5, row=(24 * 40)))
			self.CD4Th2_30d.append(_find_num_at_time(data, col=6, row=(24 * 30)))

	def write_responses(self, directory):
		"""
		Write the responses to the file system as an xml file.
		"""
		def attachDistribution(name, parent, items):
			"""
			Helper function, adds values stored in 'items' list to the supplied XML parent tag.
			Output looks like this:
			...
			<parent-name>
				<name>
					<item>items[0]</item>
					<item>items[1]</item>
					.....
					<item>items[n]</item>
				</name>
			</parent-name>
			...
			"""
			response = SubElement(parent, name)
			for i in items:
				child = SubElement(response, 'item')
				child.text = str(i)

		# prepare to write the distribution of response values to the file system.
		responsesXML = Element('Responses')
		attachDistribution(parent=responsesXML, name='max_Th1', items=self.max_Th1)
		attachDistribution(parent=responsesXML, name='max_Th2', items=self.max_Th2)
		attachDistribution(parent=responsesXML, name='max_4Treg', items=self.max_4Treg)
		attachDistribution(parent=responsesXML, name='max_8Treg', items=self.max_8Treg)
		attachDistribution(parent=responsesXML, name='max_Th1_time', items=self.max_Th1_time)
		attachDistribution(parent=responsesXML, name='max_Th2_time', items=self.max_Th2_time)
		attachDistribution(parent=responsesXML, name='max_4Treg_time', items=self.max_4Treg_time)
		attachDistribution(parent=responsesXML, name='max_8Treg_time', items=self.max_8Treg_time)
		attachDistribution(parent=responsesXML, name='Th1_40d', items=self.Th1_40d)
		attachDistribution(parent=responsesXML, name='CD4Th2_30d_ks', items=self.CD4Th2_30d)
		with open(directory + '/responses.xml','w') as file:
			file.write(prettify(responsesXML))


def load_data(data_dir):
	"""
	read in the single-run data files contained within specified directory. 
	Returns a 3D numpy array; dimensions <run number, time sample, column (response)> .
	"""
	single_run_files = glob.glob(data_dir + '/simOutputData_*.txt')     # find files
	single_run_files = natural_sort(single_run_files)
	print 'found {num} single run data files.'.format(num=len(single_run_files))
	sr_data = None    # import file data into one large data structure.
	for i, n in enumerate(single_run_files):
		print 'processing file ' + n
		with open(n, 'r') as file:
			# numpy does all the hard work and strips out the first row of comments.
			# returns a 2D array, dimension <time sample, response>
			data = numpy.loadtxt(file)
		if sr_data is None:
			# initialise data structure on first visitation.
			# dimensions <run number, time sample, column (response)>
			sr_data = numpy.zeros((len(single_run_files), data.shape[0], data.shape[1]))
		sr_data[i, :, :] = data
	return sr_data


def check_unique_seeds(data_dir):
	seed_files = glob.glob(data_dir + '/simRunSeed_*')     # find files
	seed_files = natural_sort(seed_files)
	print 'found {num} seed files.'.format(num=len(seed_files))
	encountered_seeds = []
	for f in seed_files:
		with open(f, 'r') as file:
			seed = numpy.loadtxt(file)
			if seed in encountered_seeds:
				print '!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!'
				print 'WARNING!!!! NON-UNIQUE SEED ENCOUNTERED!'
				print '!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!'
				break
				return
			encountered_seeds.append(seed)
	print 'all seeds in dir {dir} found to be unique.'.format(dir=data_dir)

def calculate_responses(data_dir, sr_data=None):
	"""
	Analyses single run data files the given directory, extracts the responses, and writes them to the filesystem.
	Response object is returned.
	"""
	if sr_data is None:
		check_unique_seeds(data_dir)
		sr_data = load_data(data_dir)
	responses = Responses(sr_data)
	responses.write_responses(directory=data_dir)
	return responses


def main():
	global data_dir
	global end_time
	ymax = None   # max range of y axis on time series graph. 
	if '-d' in sys.argv:
		i = sys.argv.index('-d')
		data_dir = sys.argv[i + 1]
	check_unique_seeds(data_dir)
	sr_data = load_data(data_dir)

	if '-end' in sys.argv:
		i = sys.argv.index('-end')
		end_time = float(sys.argv[i + 1])

	if '-ymax' in sys.argv:
		i = sys.argv.index('-ymax')
		ymax = float(sys.argv[i + 1])

	if '-avg' in sys.argv:
		# calculate and save the median data.
		median_data = numpy.median(sr_data, axis=0)
		median_data_path = data_dir + '/multipleDataOutput.txt'
		with open(median_data_path, 'w') as output:
			numpy.savetxt(output, median_data, fmt='%g')
		# plot averaged dynamics.
		plot_t_cell_timeseries(median_data, sr_data=sr_data, ymax=ymax)		

	if '-ind' in sys.argv:
		# plot dynamics of individual simulation executions.
		ind_dir = data_dir + '/individuals'
		if not os.path.exists(ind_dir):
			os.makedirs(ind_dir)
		for n in range(len(sr_data)):
			fn_postfix = 'ind-' + str(n)
			plot_t_cell_timeseries(sr_data[n,:,:], output_path=ind_dir, fn_postfix=fn_postfix, ymax=ymax)

	if '-resp' in sys.argv:
		calculate_responses(data_dir=data_dir, sr_data=sr_data)

if  __name__ =='__main__': main()