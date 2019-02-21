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

#
# Very simple ruby script that iterates through each sub directory of the current working directory, 
# drawing EAE progressions of all the individual simulation executions contained therein.
#

require 'fileutils'


path = Dir.pwd
k = path.rindex('Treg_2D')
headDir = path[0..k-1]       

$singleRunDataName = "simOutputData_0.txt"

scriptLoc = headDir + '/Treg_2D/data_analysis/EAESeverity/drawIndividualEAEScores.m'

#---------------------------------------
# Read command line arguments. Currently, doesn't do anything. May add in functionality later. 
observationEnd = ''
for i in (0..ARGV.length)
  arg = ARGV[i]
  puts arg #"arg " + i.to_s + " = " + arg.to_s
  if arg == '-end'  
    observationEnd = ARGV[i+1]
  end
end
#---------------------------------------

dirs = Dir.entries('.').reject!{|i| File.directory?(i) == false}  # reject anything that isnt a directory. 
dirs.reject!{|i| File.exists?(i + "/" + $singleRunDataName) == false}
# dirs now contains all the sub-directories that contain single run data files. these are directories representing experimental setups. 

topdir = Dir.pwd

dirs.each do |dir|
  Dir.chdir dir

  FileUtils.ln_s(scriptLoc, '.', :force => true) 
  system("matlab -nosplash -nodesktop -r 'drawIndividualEAEScores;quit'")
  
  Dir.chdir topdir
end
