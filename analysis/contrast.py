"""
Contrasts the data contained in two directories.
"""
import os
import sys
import numpy
import matplotlib.pyplot as plt
from xml.etree.ElementTree import Element, SubElement
from xml.etree import ElementTree
from xml.dom import minidom
# imports that I have written.
import single_analysis
import stats
import scipy.stats as ss


dir1 = None
dir2 = None
out_dir = None


class ResponseContrast():
	def __init__(self, resp1, resp2):
		# KS test of differences
		self.max_Th1_ks, self.max_Th1_ks_p               = ss.ks_2samp(resp1.max_Th1, resp2.max_Th1)
		self.max_Th2_ks, self.max_Th2_ks_p               = ss.ks_2samp(resp1.max_Th2, resp2.max_Th2)
		self.max_4Treg_ks, self.max_4Treg_ks_p           = ss.ks_2samp(resp1.max_4Treg, resp2.max_4Treg)
		self.max_8Treg_ks, self.max_8Treg_ks_p           = ss.ks_2samp(resp1.max_8Treg, resp2.max_8Treg)
		self.max_Th1_time_ks, self.max_Th1_time_ks_p     = ss.ks_2samp(resp1.max_Th1_time, resp2.max_Th1_time)
		self.max_Th2_time_ks , self.max_Th2_time_ks_p    = ss.ks_2samp(resp1.max_Th2_time, resp2.max_Th2_time)
		self.max_4Treg_time_ks, self.max_4Treg_time_ks_p = ss.ks_2samp(resp1.max_4Treg_time, resp2.max_4Treg_time)
		self.max_8Treg_time_ks, self.max_8Treg_time_ks_p = ss.ks_2samp(resp1.max_8Treg_time, resp2.max_8Treg_time)
		self.Th1_40d_ks, self.Th1_40d_ks_p               = ss.ks_2samp(resp1.Th1_40d, resp2.Th1_40d)
		# A test of differences
		self.max_Th1_A         = stats.Atest(resp2.max_Th1, resp1.max_Th1)
		self.max_Th2_A         = stats.Atest(resp2.max_Th2, resp1.max_Th2)
		self.max_4Treg_A       = stats.Atest(resp2.max_4Treg, resp1.max_4Treg)
		self.max_8Treg_A       = stats.Atest(resp2.max_8Treg, resp1.max_8Treg)
		self.max_Th1_time_A    = stats.Atest(resp2.max_Th1_time, resp1.max_Th1_time)
		self.max_Th2_time_A    = stats.Atest(resp2.max_Th2_time, resp1.max_Th2_time)
		self.max_4Treg_time_A  = stats.Atest(resp2.max_4Treg_time, resp1.max_4Treg_time)
		self.max_8Treg_time_A  = stats.Atest(resp2.max_8Treg_time, resp1.max_8Treg_time)
		self.Th1_40d_A         = stats.Atest(resp2.Th1_40d, resp1.Th1_40d)

	def write(self, directory):
		""" write to file system """
		contrastXML = Element('contrast')
		child = SubElement(contrastXML, 'max_Th1_ks')
		child.text = '{:.2f}'.format(self.max_Th1_ks)
		child = SubElement(contrastXML, 'max_Th1_ks_p')
		child.text = '{:.2f}'.format(self.max_Th1_ks_p)

		child = SubElement(contrastXML, 'max_Th2_ks')
		child.text = '{:.2f}'.format(self.max_Th2_ks)
		child = SubElement(contrastXML, 'max_Th2_ks_p')
		child.text = '{:.2f}'.format(self.max_Th2_ks_p)

		child = SubElement(contrastXML, 'max_4Treg_ks')
		child.text = '{:.2f}'.format(self.max_4Treg_ks)
		child = SubElement(contrastXML, 'max_4Treg_ks_p')
		child.text = '{:.2f}'.format(self.max_4Treg_ks_p)

		child = SubElement(contrastXML, 'max_8Treg_ks')
		child.text = '{:.2f}'.format(self.max_8Treg_ks)
		child = SubElement(contrastXML, 'max_8Treg_ks_p')
		child.text = '{:.2f}'.format(self.max_8Treg_ks_p)

		child = SubElement(contrastXML, 'max_Th1_time_ks')
		child.text = '{:.2f}'.format(self.max_Th1_time_ks)
		child = SubElement(contrastXML, 'max_Th1_time_ks_p')
		child.text = '{:.2f}'.format(self.max_Th1_time_ks_p)

		child = SubElement(contrastXML, 'max_Th2_time_ks')
		child.text = '{:.2f}'.format(self.max_Th2_time_ks)
		child = SubElement(contrastXML, 'max_Th2_time_ks_p')
		child.text = '{:.2f}'.format(self.max_Th2_time_ks_p)

		child = SubElement(contrastXML, 'max_4Treg_time_ks')
		child.text = '{:.2f}'.format(self.max_4Treg_time_ks)
		child = SubElement(contrastXML, 'max_4Treg_time_ks_p')
		child.text = '{:.2f}'.format(self.max_4Treg_time_ks_p)

		child = SubElement(contrastXML, 'max_8Treg_time_ks')
		child.text = '{:.2f}'.format(self.max_8Treg_time_ks)
		child = SubElement(contrastXML, 'max_8Treg_time_ks_p')
		child.text = '{:.2f}'.format(self.max_8Treg_time_ks_p)

		child = SubElement(contrastXML, 'Th1_40d_ks')
		child.text = '{:.2f}'.format(self.Th1_40d_ks)
		child = SubElement(contrastXML, 'Th1_40d_ks_p')
		child.text = '{:.2f}'.format(self.Th1_40d_ks_p)

		child = SubElement(contrastXML, 'max_Th1_A')
		child.text = '{:.2f}'.format(self.max_Th1_A)
		child = SubElement(contrastXML, 'max_Th2_A')
		child.text = '{:.2f}'.format(self.max_Th2_A)
		child = SubElement(contrastXML, 'max_4Treg_A')
		child.text = '{:.2f}'.format(self.max_4Treg_A)
		child = SubElement(contrastXML, 'max_8Treg_A')
		child.text = '{:.2f}'.format(self.max_8Treg_A)

		child = SubElement(contrastXML, 'max_Th1_time_A')
		child.text = '{:.2f}'.format(self.max_Th1_time_A)
		child = SubElement(contrastXML, 'max_Th2_time_A')
		child.text = '{:.2f}'.format(self.max_Th2_time_A)
		child = SubElement(contrastXML, 'max_4Treg_time_A')
		child.text = '{:.2f}'.format(self.max_4Treg_time_A)
		child = SubElement(contrastXML, 'max_8Treg_time_A')
		child.text = '{:.2f}'.format(self.max_8Treg_time_A)

		child = SubElement(contrastXML, 'Th1_40d_A')
		child.text = '{:.2f}'.format(self.Th1_40d_A)

		with open(directory + '/contrast.xml', 'w') as file:
			print 'writing to directory {w}'.format(w=directory)
			file.write(single_analysis.prettify(contrastXML))


def plotCDF(dist1, dist2, d1name, d2name, responseName, file_path):
	"""
	Calculates cumulative distribution functions for the two supplied empirical distributions, and plots the two CDFs
	atop one another.
	"""
	d, p = ss.ks_2samp(dist1, dist2)
	plt.clf()
	cdf1 = stats.ecdf(dist1)
	cdf2 = stats.ecdf(dist2)
	D1X = [record[0] for record in cdf1]
	D1Y = [record[1] for record in cdf1]
	D2X = [record[0] for record in cdf2]
	D2Y = [record[1] for record in cdf2]
	if D1X and D1Y and D2X and D2Y:		# if the lists are not empty or None
		# create a record for where 0 proportion of the population sits. Otherwise ECDF plots don't touch the x axis.
		D1X[0:0] = [D1X[0]]
		D1Y[0:0] = [0]
		D2X[0:0] = [D2X[0]]
		D2Y[0:0] = [0]
	p1, = plt.plot(D1X, D1Y)
	p2, = plt.plot(D2X, D2Y)
	if d1name is not None and d2name is not None:
		plt.legend([p1, p2], [d1name, d2name],loc=4)
	plt.xlabel('Response value')
	plt.ylabel('Proportion')
	plt.title('{r} ; KS={ks:.2f}, p={p:.2f}'.format(r=responseName, ks=d, p=p))
	plt.gca().grid(True);   # turn on grid lines.
	plt.rcParams.update({'font.size': 18})
	plt.savefig(file_path, dpi=300)
	plt.close()


def main():
	global dir1
	global dir2
	global out_dir

	d1_label = None
	d2_label = None
	if '-d1' in sys.argv:
		i = sys.argv.index('-d1')
		dir1 = sys.argv[i + 1]
	if '-d2' in sys.argv:
		i = sys.argv.index('-d2')
		dir2 = sys.argv[i + 1]
	if dir1 is None or dir2 is None:
		raise Exception('Cannot proceed without specifying both data directories.')
	if '-o' in sys.argv:
		i = sys.argv.index('-o')
		out_dir = sys.argv[i + 1]
		if not os.path.exists(out_dir):
			os.makedirs(out_dir)
	if '-l1' in sys.argv:
		i = sys.argv.index('-l1')
		d1_label = sys.argv[i + 1]
	if '-l2' in sys.argv:
		i = sys.argv.index('-l2')
		d2_label = sys.argv[i + 1]

	print 'checking dir 1 {dir}'.format(dir=dir1)
	single_analysis.check_unique_seeds(dir1)
	print 'checking dir 2 {dir}'.format(dir=dir2)
	single_analysis.check_unique_seeds(dir2)

	responses1 = single_analysis.calculate_responses(dir1)
	responses2 = single_analysis.calculate_responses(dir2)

	plotCDF(responses1.max_Th1, responses2.max_Th1, d1_label, d2_label, 'Max Th1', out_dir + '/CDF_max_th1.png')
	plotCDF(responses1.max_Th2, responses2.max_Th2, d1_label, d2_label, 'Max Th2', out_dir + '/CDF_max_th2.png')
	plotCDF(responses1.max_4Treg, responses2.max_4Treg, d1_label, d2_label, 'Max CD4Treg', out_dir + '/CDF_max_4Treg.png')
	plotCDF(responses1.max_8Treg, responses2.max_8Treg, d1_label, d2_label, 'Max CD8Treg', out_dir + '/CDF_max_8Treg.png')

	plotCDF(responses1.max_Th1_time, responses2.max_Th1_time, d1_label, d2_label, 'Max Th1 time', out_dir + '/CDF_max_th1_time.png')
	plotCDF(responses1.max_Th2_time, responses2.max_Th2_time, d1_label, d2_label, 'Max Th2 time', out_dir + '/CDF_max_th2_time.png')
	plotCDF(responses1.max_4Treg_time, responses2.max_4Treg_time, d1_label, d2_label, 'Max CD4Treg Time', out_dir + '/CDF_max_4Treg_time.png')
	plotCDF(responses1.max_8Treg_time, responses2.max_8Treg_time, d1_label, d2_label, 'Max CD8Treg Time ', out_dir + '/CDF_max_8Treg_time.png')

	plotCDF(responses1.Th1_40d, responses2.Th1_40d, d1_label, d2_label, 'Th1 @ 40 days', out_dir + '/CDF_th1_40d.png')

	contrast = ResponseContrast(responses1, responses2)
	contrast.write(out_dir)




if  __name__ =='__main__': main()