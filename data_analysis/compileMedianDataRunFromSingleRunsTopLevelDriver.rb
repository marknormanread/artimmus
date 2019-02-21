# This script is a driver for the 'compileMedianDataRunFromSingleRuns' script. 'compileMedianDataRunFromSingleRuns' will work from within a directory
# that contains a lot of single run data files. This script can identify those directores (for example, in a sensitivityAnalysis directory', copy
# in the compileMedianDataRunFromSingleRuns script, and execute it. 
#



require 'fileutils'


path = Dir.pwd
k = path.rindex('Treg_2D')
headDir = path[0..k-1]                                              # dynamically find the location of this experimental set up. 

singleRunIndicator = 'simOutputData_0.txt'                            # all directories that contain simulation output data must contain this file. 
compileMediansScriptLoc = headDir + '/Treg_2D/data_analysis/compileMedianDataRunFromSingleRuns.m'

dirs = Dir.entries(".").reject!{|i| File.directory?(i) == false}      # retrieve all the directories within the current working directory. 
dirs.reject!{|i| File.exists?(i + "/" + singleRunIndicator) == false} # remove all directories that do not contain the singleRunIndicator. 

cwd = Dir.pwd()                                                         # store the current working directory such that we can get back again. 
dirs.each do |dir|
  Dir.chdir dir
  FileUtils.ln_s(compileMediansScriptLoc, '.', :force => true)
  if File.exists?('multipleDataOutput.txt') == false
    system("matlab -nosplash -nodesktop -r 'compileMedianDataRunFromSingleRuns;quit'")
  end
  Dir.chdir cwd
end
