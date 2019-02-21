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

# This script is designed to run from a top level experiment directory (one that contains subdirectories of single run data, those subdirectories
# representing parameter tweeks or similar). It is meant to be the one file to call which will in turn perform all aspects of an EAE severity 
# based robustness analysis. 
#
#
# COMMAND LINE ARGUMENTS:
# '-end XXX' must be provided, it indicates the observation time for graph plotting and for EAE severity compilation. Recommended that this not be the entire length of the data
#         set, to avoid smoothing artifacts when generating EAE severity scores. 

require 'fileutils'

require 'find'
require 'rexml/document'

$singleRunDataName = "simOutputData_0.txt"


path = Dir.pwd
k = path.rindex('Treg_2D')
headDir = path[0..k-1]                                                # dynamically find the location of this experimental set up. 



#---------------------------------------
# Read command line arguments. 
observationEnd = ''
if ARGV.length != 2
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



compileEAEScoresLoc = headDir + '/Treg_2D/data_analysis/EAESeverity/compileEAESeveritiesForRuns.m'
drawEAEScoresLoc = headDir + '/Treg_2D/data_analysis/EAESeverity/drawMedianEAEScore.m'
robustnessAnalysisLoc = headDir + '/Treg_2D/data_analysis/EAESeverity/robustnessAnalysisEAE.m'
genEAESeverityResponsesLoc = headDir + '/Treg_2D/data_analysis/EAESeverity/generateEAESeverityResponses.m'
eaeDistributionsLoc = headDir + '/Treg_2D/data_analysis/EAESeverity/EAESeverityDistributions.m'
relapseLoc = headDir + '/Treg_2D/data_analysis/EAESeverity/relapseIncidenceAnalysis.m'

dirs = Dir.entries('.').reject!{|i| File.directory?(i) == false}
dirs.reject!{|i| File.exists?(i + "/" + $singleRunDataName) == false}
# dirs now contains all the sub-directories that contain single run data files. these are directories representing parameter changes. 

if File.exists?('EAESeverityAnalysis') == false
  system("rm -r EAESeverityAnalysis")
  Dir.mkdir('EAESeverityAnalysis')
end


topdir = Dir.pwd
if true                    # CHANGE THIS TO CUT IN OR OUT PROCESSING FUNCTIONALITY. 
  dirs.each do |dir| 
    puts "current dir = " + Dir.pwd
    puts "attempting to cd into " + dir

    Dir.chdir dir

    FileUtils.ln_s(compileEAEScoresLoc, '.', :force => true)
    FileUtils.ln_s(drawEAEScoresLoc, '.', :force => true)
    FileUtils.ln_s(genEAESeverityResponsesLoc, '.', :force => true)
    if File.exists?('EAESeverityScoresForRuns') == false                  # only compile severity scores if not already done so. 
      system("matlab -nosplash -nodesktop -r \"compileEAESeveritiesForRuns('-end " + observationEnd + "');quit\"")
    end

    files = Dir.entries('.')
    files.delete_if{|f| f.index('EAESeverity_response_data_-_') == nil}   # remove from list everything that is not response data
    if files.length == 0                                                  # and generate the responses if they are not present
      system("matlab -nosplash -nodesktop -r 'generateEAESeverityResponses(" + observationEnd + ");quit'")
    end

    FileUtils.ln_s(eaeDistributionsLoc, '.', :force => true)
    system("matlab -nosplash -nodesktop -r 'EAESeverityDistributions(" + observationEnd + ");quit'")
    system("cp EAESeverityDistributions* ../EAESeverityAnalysis")  

    system("matlab -nosplash -nodesktop -r 'drawMedianEAEScore( " + observationEnd + ");quit'")  
    system("cp EAEScoreMedian* ../EAESeverityAnalysis")  
    system("cp EAEScoreMean* ../EAESeverityAnalysis")  

    FileUtils.ln_s(relapseLoc, '.', :force => true) 
    system("matlab -nosplash -nodesktop -r 'relapseIncidenceAnalysis;quit'")
    system("cp RelapseIncidenceDurations_* ../EAESeverityAnalysis")
    system("cp RemissionIncidenceDurations_* ../EAESeverityAnalysis")

    Dir.chdir topdir
  end
end

FileUtils.ln_s(robustnessAnalysisLoc,'.',:force => true)
system("matlab -nosplash -nodesktop -r \"robustnessAnalysisEAE('');quit\"")
