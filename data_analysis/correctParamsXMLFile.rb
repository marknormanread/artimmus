# This script corrects a mistake made by the matlab code that generates LHC parameter files. Those files that matlab creates have as their root an element named "root", 
# whereas it should be "input". This seems not to bother the java simulator, but the scripts that operate on data and read the parameters files require the root element
# to be called "input". 
#
# This script corrects the problem, and renames that element in all relevant tags. There  is some code commented out below, which selects files from different places to 
# operate on. I've left it in as an example. 

require 'rexml/document'

include REXML


#
# Here we find the xml files that we are interested in
#
files = []

##---------------------
## this part was used to pull the "lhc_1_parameters_xxx.xml" files from single_run directory and alter them. It does not operate recursively through LHC1_run_xxx dirs. 
##---------------------
#interestIndicator = "lhc1_parameters_"
#files = Dir.entries(Dir.pwd()).reject{|i| i.index(interestIndicator) == nil} # this gets the items in the current working directory (non-recursive)
#																																						 # and rejects those that do not contain the text indicated by 'interestIndicator'/ 
#files.reject!{|f| f.rindex(".xml") == nil } # 'reject!' keeps items for which the expression evaluates to false. We will reject those which do not end in ".xml"
	
##---------------------
## this part will go through each of the LHC1_run_xxx dirs and select the parameters xml files within them for modification. 
## Note that this selection method is fully recursive. 
##---------------------
interestIndicator = "LHC1_run_"                                               # the prefix for selecting directories in which to search for paramters files. 
dirs = Dir.entries(Dir.pwd()).reject{|i| i.index(interestIndicator) == nil}		# get the directories that contain LHC1 runs (rejecting everything in the 
																																							# current directory that doesn't start with "LHC1_run_"
dirs.each do |d|																															# cycle through each directory
	puts "compiling files of interest from directory " + d
	temp = Dir.entries(d).reject{|i| i.index("lhc1_parameters_") == nil}				# store contents of directory in temp, rejecting all but those starting with...
	temp.reject!{|i| i.rindex(".xml") == nil}																		# reject all that do not contain ".xml"
	temp.each {|f| files<< d + "/" + f}																					# for all that remains, add the path (including directory, since we have not changed
																																							# changed the current working directory) to the 'files' of interest
  files<< d + "/parameters.xml"																								# add parameters.xml too
end

	
#
# Here we cycle through the files that we are interested in, open the xml file, modify it, delete the orginal, and re-write the modified version under
# the same name. 
#
files.each do |fileName|
	puts "modifying/correcting file " + fileName
	params = REXML::Document.new(File.new(fileName))   # read in the parameters.xml file
	params.root.name = "input"												 # change the root tag from "root" to "input"

	output = ""																					# where we will store the xml file text before writing to filesystem
	output = params.to_s																# generate string text of xml file
	#puts output

	system("rm " + fileName)														# delete the original file. 

	File.open(fileName, 'w') do |f|						  				# write to the file. 
		f.write(output)																	
	end
end



