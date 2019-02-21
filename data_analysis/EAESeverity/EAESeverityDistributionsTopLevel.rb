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

# This ruby script will compile EAESeverityDistributions (from a whole load of runs) in each of the immediate subfolders of the current working directory
# that are identified as containing simulation output data. 
#
# Script takes the following arguments:
# '-end XXX', specifying the upper range of time for which EAE severity distributions graphs should be drawn. 

require 'fileutils'


observationEnd = ''
for i in (0..ARGV.length)
  arg = ARGV[i]
  puts arg #"arg " + i.to_s + " = " + arg.to_s
  if arg == '-end'  
    observationEnd = ARGV[i+1]
  end
end


singleRunIndicator = 'simOutputData_0.txt'                            # all directories that contain simulation output data must contain this file. 



path = Dir.pwd
k = path.rindex('Treg_2D')
headDir = path[0..k-1]                                                # dynamically find the location of this experimental set up. 

compileMediansScriptLoc = headDir + '/Treg_2D/data_analysis/EAESeverity/EAESeverityDistributions.m'
compileEAEScoresLoc = headDir + '/Treg_2D/data_analysis/EAESeverity/compileEAESeveritiesForRuns.m'
drawEAEScoresLoc = headDir + '/Treg_2D/data_analysis/EAESeverity/drawMedianEAEScore.m'


dirs = Dir.entries(".").reject!{|i| File.directory?(i) == false}      # retrieve all the directories within the current working directory. 
dirs.reject!{|i| File.exists?(i + "/" + singleRunIndicator) == false} # remove all directories that do not contain the singleRunIndicator. 

if File.exists?("EAESeverityAnalysis")
  system("rm -r EAESeverityAnalysis")
end
system("mkdir EAESeverityAnalysis")

cwd = Dir.pwd()                                                         # store the current working directory such that we can get back again. 
dirs.each do |dir|
  Dir.chdir dir
  FileUtils.ln_s(compileMediansScriptLoc, '.', :force => true)
  system("matlab -nosplash -nodesktop -r 'EAESeverityDistributions(" + observationEnd + ");quit'")
  system("cp EAESeverityDistributions-* ../EAESeverityAnalysis")  

  FileUtils.ln_s(compileEAEScoresLoc, '.', :force => true)
  FileUtils.ln_s(drawEAEScoresLoc, '.', :force => true)

  if File.exists?('EAESeverityScoresForRuns') == false
    system("matlab -nosplash -nodesktop -r 'compileEAESeveritiesForRuns;quit'")
  end
  #system("matlab -nosplash -nodesktop -r 'drawMedianEAEScore;quit'")
  #system("cp EAEScoreMedian* ../EAESeverityAnalysis")  
  #system("cp EAEScoreMean* ../EAESeverityAnalysis")  

  Dir.chdir cwd
end


