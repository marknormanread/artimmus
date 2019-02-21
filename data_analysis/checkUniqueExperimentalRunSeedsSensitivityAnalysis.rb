# This is a top level driver for the checkUniqueExperimentalRunSeeds script, which runs on individial dictories that contain single run data files. 
# This script can operate at a level above, which may contain many such directories. It will identify all the directories that contain single run 
# data files, copy the checkUniqueExperimentalRunSeeds script into those directories, and call matlab to run it. 

require 'fileutils'


path = Dir.pwd
k = path.rindex('Treg_2D')
headDir = path[0..k-1]                                                            # dynamically find the location of this experimental set up. 


singleRunIndicator = 'simOutputData_0.txt'                            # all directories that contain simulation output data must contain this file. 
uniqScriptLocation = headDir + '/Treg_2D/data_analysis/checkUniqueExperimentalRunSeeds.rb'

dirs = Dir.entries(".").reject!{|i| File.directory?(i) == false}
dirs.reject!{|i| File.exists?(i + "/" + singleRunIndicator) == false} # remove all directories that do not contain the singleRunIndicator. 

cwd = Dir.pwd                                                         # store the current working directory such that we can get back again. 
dirs.each do |dir|
  Dir.chdir dir
  FileUtils.ln_s(uniqScriptLocation, '.', :force => true)
  system("ruby checkUniqueExperimentalRunSeeds.rb")
  Dir.chdir cwd
end
