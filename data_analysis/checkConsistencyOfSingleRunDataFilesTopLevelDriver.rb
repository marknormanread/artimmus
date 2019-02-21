# This is a driver for 'checkConsistencyOfSingleRunDataFiles.m'. This script is intended to be run from a directory containing subdirectories storing simulation experimental
# data. This script will identify all the sub directories which contain singleRunDataFiles, will copy the matlab script into each corresponding subdirectory, and will
# execute it - hence checking the consistency of data.
#
# This script MUST be given two command line arguments. "-numRuns XXX -timetimeSamples XXX" which identify the expected number of singleRunDataFiles in each subdirectory, 
# and the number of time timeSamples that each file should contain. Example argument: "-numRuns 500 -timeSamples 1300", meaning singleRunData files numbered from 0 to 499. 
#
# The matlab script, in addition to reporting failures to the stderror, will create a file called 'consistencyOfDataResult', which contains either the words 'FAIL' or 'PASS'. 
# This script will look through each directory that matlab operated in, and will find whether each 
#
#
require 'fileutils'

numRuns = '';
timeSamples = '';

if ARGV.length == 0 
  raise "you must supply the number of simulation runs and time samples!" 
end

for i in (0..ARGV.length)
  arg = ARGV[i]
  puts arg #"arg " + i.to_s + " = " + arg.to_s
  if arg == '-numRuns'  
    numRuns = ARGV[i+1]
  end
  if arg == '-timeSamples'
    timeSamples = ARGV[i+1]
  end
end



path = Dir.pwd
k = path.rindex('Treg_2D')
headDir = path[0..k-1]                                                # dynamically find the location of this experimental set up. 


singleRunIndicator = 'simOutputData_0.txt'                            # all directories that contain simulation output data must contain this file. 
checkConsistencyScriptLoc = headDir + '/Treg_2D/data_analysis/checkConsistencyOfSingleRunDataFiles.m'


dirs = Dir.entries('.').reject!{|i| File.directory?(i) == false}      # retrieve all the directories within the current working directory. 
dirs.reject! do |i|           # this code will reject any directory that contains not a single instance of a 'simOutputData' file. 
  reject = true
  files = Dir.entries(i)
  files.each do |f|
    if f.index("simOutputData_") != nil
      reject = false
      break
    end
  end
  reject
end

dirs.sort!

allDirs = dirs

# This section will remove any directories that contain the file 'consistencyOfDataResult', but only if that file does not contain the
# word 'FAIL'. 
#
if false
dirs.reject! do |dir|
  remove = false
  if File.exists?(dir + '/consistencyOfDataResult')
    remove = true
    File.open(dir + '/consistencyOfDataResult') do |file|
      while (line = file.gets)
        if line.index('FAIL')  != nil
          remove = false
          break
        end
      end
    end
  end
  remove
end
end


cwd = Dir.pwd()                                                       # store the current working directory such that we can get back again. 
dirs.each do |dir|
  puts "checking consistency of data in directory : " + dir
  Dir.chdir dir
  FileUtils.ln_s(checkConsistencyScriptLoc, '.', :force => true)
  system("matlab -nosplash -nodesktop -nodisplay -r \"checkConsistencyOfSingleRunDataFiles('-numRuns " + numRuns + " -timeSamples " + timeSamples+ "');quit\"")
  Dir.chdir cwd
end


failed = false
failedDirs = []
allDirs.each do |dir|
  File.open(dir + '/consistencyOfDataResult','r') do |file|
    while (line = file.gets)
      if line.index('FAIL') != nil
        failed = true 
        failedDirs<< dir
      end
    end 
  end
end

if dirs.length != 0
  File.open('consistencyOfDataResult','w') do |file|
    if failed == false
      file.write( "PASS\n" )
    else
      failedDirs.each do |dir|
        file.write( "FAIL - " + dir + " \n" )
        end
      end
    file.write("-numRuns " + numRuns + " -timeSamples " + timeSamples)
    end
  end
