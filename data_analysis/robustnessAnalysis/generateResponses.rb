# Copyright 2012 Mark Read
# 
# This file is part of ARTIMMUS.
# 
# ARTIMMUS is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# ARTIMMUS is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with ARTIMMUS.  If not, see <http://www.gnu.org/licenses/>.
#

# This file should be called from within the root of a 'sensitivity analysis' experiment, which will be a directory containing other directories, each of which will hold the results of a particular parameter setting. 
# Within any particular subdirectory, the script will examine each run in turn and calculate the 'response' data. These responses will then be compiled into a table, representing each run's values for the
# various responses. 
# Once this file of run-responses has been created for a particular parameter setting, it will be copied to a 'robustnessAnalysis' directory that lies under the root. From this directory the 
# robustness sensitivity analysis will be  performed. 
#
# COMMAND LINE ARGUMENTS:
# '-end XXX' must be provided, it indicates the observation time IN DAYS for graph plotting and for EAE severity compilation. Recommended that this not be the entire length of the data
#         set, to avoid smoothing artifacts when generating EAE severity scores. 
require 'fileutils'

require 'find'
require 'rexml/document'

include REXML

# The following will dynamically calculate the absolute path to the current Treg_2D experimental set up, used in copying files across to places where they are later executed. 
path = Dir.pwd
k = path.rindex('Treg_2D')
headDir = path[0..k-1]       


#---------------------------------------
# Read command line arguments. 
observationEnd = ''
if ARGV.length == 0
  puts "must specify end time"         
  return
end
for i in (0..ARGV.length)
  arg = ARGV[i]
  puts arg #"arg " + i.to_s + " = " + arg.to_s
  if arg == '-end'  
    observationEnd = ARGV[i+1]
  end
end
#---------------------------------------


          # by default, the default value must be found by examining the name of the current working directory to find the tags into the xml file that will
          # permit the default value to be extracted. 
          # however, it is possible to provide the default value through the command line. This is useful because we may want to name the directory something more meaningful
          # than a bunch of xml tags. 
defaultProvided = false                                       
defaultValue = -1000                                          # some silly value that will hopefully be overwritten, else cause a fast fail. 

(0 .. (ARGV.size-1)).each do |i|
  if ARGV[i] == '-default' 
    defaultValue = ARGV[i+1]
    puts "default value provided as " + defaultValue
    defaultProvided = true
  end 
end


$singleRunDataName = "simOutputData_"
singleRunDataNameFirst = "simOutputData_0.txt"                # if the directory contains any single run data, it will contain a file of this name since this is the first file that will be created (the first simulation run). 
pathArray = []																								# array will contain absolute paths of directories that contain singleRunDataNameFirst. 
$pathToParam = []																							# 'findDefaultValue' will place in this array the tags in sensitivity_analysis.xml that indicate the parameter for which this script is being run. 

$defaultParameterValueFileName = "defaultParameterValue"      # the name of the file (should it exist) that contains the default parameter value. 

# This method was obtained from http://www.devdaily.com/blog/post/ruby/ruby-method-read-in-entire-file-as-string on 25/11/09
#
# retrieves an entire file's data as a string, 'data', which is then returned. 
def get_file_as_string(filename)
  data = ''
  f = File.open(filename, "r") 
  f.each_line do |line|
    data += line
  end
  return data
end


# Identify which parameter setting was the default from the sensitivity_parameters.xml file. 
# must be called from within the root test directory (the one that contains subdirectories for each parameter value variation)
def findDefaultValue()
  workingDir = FileUtils.pwd()

  testDir = workingDir[workingDir.rindex("/")+1..-1]                  # retrieve the name of this test dir, without all the other paths before it. 

  if(testDir.rindex("_Reg") != nil)                                   # remove '_Reg', if it exists, from the end of the string
    testDir = testDir[0..testDir.rindex("_Reg")-1]
  end

  if(testDir.rindex("_EAE") != nil)
    testDir = testDir[0..testDir.rindex("_EAE")-1]                    # remove '_EAE', if it exists, from the end of the string
  end

  testDir = testDir["sensAnal_-_".length..-1]                         # remove 'sensAnal_-_' from the front of the string

                                        # we are going to separate out, and store, each substring separated by a '_', 
  i = testDir.index("_")                # we are doing this so that we can retrieve the path to the parameter in the sensitivityAnalysis parameters file. That info is used to create unique names for directories containing data.
  while(i != nil)
    substring = testDir[0..i-1]                                       # pull out the substring, from beginning to (but not including) the first "_"

    if substring.index("_") == nil                                     # safety, in case we have separators containing more than one "_" in a row.
      $pathToParam<< substring                                         # add the substring to the array
    end
    testDir = testDir[i+1..-1]                                        # remove the current substring from the fronnt of 'testDir' so that we can work on the next substring
    i = testDir.index("_")                                            # find the next '_', if it exists. 
  end 
  $pathToParam<< testDir                                              # all that will remain of 'testDir' here is the last substring, so we add that too. 
	
                       # At this point, 'pathToParam' contains each of the substrings representing xml tags in the sensitivity_parameters.xml file.
                       # We can use this information to search through the xml file and find the default parameter value 

  sensitivityParameters = REXML::Document.new(File.new("sensitivity_parameters.xml"))   # read in the sensitivity_parameters.xml file
  root = sensitivityParameters.root()                                 # grab the root of the xml file (corresponding to 'input' tag)

  searchString = ""                                                   # in order to drill straight down to the 'default' value, we are going to construct a search string that will navigate there in the xml file. 
                                                                      # this was based on the following page http://www.xml.com/pub/a/2005/11/09/rexml-processing-xml-in-ruby.html?page=2
  $pathToParam.each do |p|                                            # iterate through each tag that defines which parameter from the xml file is being investigated. 
    searchString += p                                                 # add that tag name to the search string
    searchString += "[1]/"                                            # '[1]' because (for some reason) the standard being used here starts naming things from 1, not zero. '/' is the separator between tags
  end
  searchString += "default"                                           # add the last 'default' tag to the string. 

  defaultVal = root.elements[searchString].get_text                   # find and store the default value. Using the search string we just constructed we can retrieve this value in one go. 

  return defaultVal.to_s                                              # return as string (as opposed to REXML::Text)
end



#
# Entry point of the program. 
#

# create the directory where all the crude sensitivity analysis response data is to be copied. 
robustnessDir = "robustness_sensitivity_analysis"
if File.exists?(robustnessDir)
  FileUtils.rm_rf(robustnessDir)
end
if File.exists?(robustnessDir) == false																	# safety, don't try to make the directory if it already exists. 
	FileUtils.mkdir(robustnessDir)
end
FileUtils.ln_s(headDir + '/Treg_2D/data_analysis/robustnessAnalysis/robustnessAnalysis.m', 'robustness_sensitivity_analysis/.', :force => true)
FileUtils.ln_s(headDir + '/Treg_2D/data_analysis/compileMedianDataRunFromSingleRunsTopLevelDriver.rb', '.', :force => true)
FileUtils.ln_s(headDir + '/Treg_2D/data_analysis/checkUniqueExperimentalRunSeedsSensitivityAnalysis.rb', '.', :force => true)
FileUtils.ln_s(headDir + '/Treg_2D/data_analysis/robustnessAnalysis/compileSimOutputGraphs.rb', '.', :force => true)


# find the default value that the parameter would take.
if defaultProvided == false
  if File.exists?($defaultParameterValueFileName)
    defaultValue = File.open($defaultParameterValueFileName,"r"){|f| defaultValue = f.gets}
  else
    defaultValue = findDefaultValue()
  end
end
puts "default value = " + defaultValue

pathArray = Dir.entries(".").reject!{|i| File.directory?(i) == false}     # keep only directories. 
pathArray.sort!                                                           # operate on directories in a slightly more sensible manner.  
pathArray.reject!{|i| File.exists?(i + "/" + singleRunDataNameFirst) == false} # remove all directories that do not contain the singleRunIndicator. 
pathArray.each{|i| i.insert(0, FileUtils.pwd() + "/")}                    # turn the relative address into the absolute address. This is important because
                                                                          # there is information encoded in the directory names. 
# output to the terminal which files have been found.
puts("\n\nthe following directories were identified\n\n")
pathArray.each do |item|
  puts(item)
end


# look through each directory that contains single run data, and calculate for each run the responses. 
pathArray.each do |path|                                            # search through each path containing single run data files in turn. These paths represent individual parameter tweeks
  puts "compiling single run data from directory : " + path         # keep the human informed (and midly entertained, this script could take a while)

	paramTestValue = path[path.rindex("_")+1..-1]											# retrieves the value of the parameter during this set of runs, as derived from 'path'. 
  outputDataFileName = "robustness_analysis_response_data_-_"       # begin creating the output file name. 
  $pathToParam.each { |p| outputDataFileName += p + "_-_" }         # add the path to the parameter to the output file name.
  outputDataFileName += paramTestValue		# compile the name of the file to which response data is to be written.

  oldDir = Dir.pwd
  Dir.chdir path
    FileUtils.ln_s(headDir + '/Treg_2D/data_analysis/LHC_sensitivity_analysis/generate_LHCx_response.m', '.', :force => true)
    #if File.exists?(outputDataFileName) == false    
      system("matlab -nosplash -nodesktop -r \"generate_LHCx_response('" + outputDataFileName + "','" + observationEnd + "');quit\"")  
    #end
  Dir.chdir oldDir

  FileUtils.cp(path + '/' + outputDataFileName, robustnessDir + '/' + outputDataFileName);

      # if this parameter happens to be the default parameter, then recompile and save as a name corresponding to the 'default'. 
  if (paramTestValue.to_f == defaultValue.to_f)
    fileName = robustnessDir + "/" + "robustness_analysis_response_data_-_"   # start creating the file name for the default response data.
    $pathToParam.each { |p| fileName += p + "_-_" }                 # add the tags that uniquely identify this parameter. 
    fileName += "default_-_" + defaultValue                         # add the "default" keyword, and store the actual default value too. This is used in the matlab analysis. 
      FileUtils.cp(path + '/' + outputDataFileName, fileName);
  end
end

system("ruby compileMedianDataRunFromSingleRunsTopLevelDriver.rb")
system("rm -r simulationOutputPngs")
system("ruby compileSimOutputGraphs.rb -end " + observationEnd)

Dir.chdir('robustness_sensitivity_analysis')
system('matlab -nodesktop -nosplash -r "robustnessAnalysis(\'\');quit"')
Dir.chdir('..')




