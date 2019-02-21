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

#!/bin/ruby

#
# This script should be called from withing a particular sensitivity analysis directory, with teh actual parameter tweeks contained in immediate sub directories. 
# the script will find each sub directory that represents a tweek, which can be recognised by the present of the 'dataFile' indicated below. It will then
# create a symbolic link to a matlab script that can draw graphs of the median simulation runs, and call that script. The script itself will draw all the 
# relevant graphs, and will write a file to *this* directory (the experiment directory, rather than a tweek directory) that contains the upper bounds on
# each of the graphs generated in each parameter tweek. These will be different depending on the data that the graph was drawn with. However, for the purposes
# of comparison across graphs, we wish these ranges to be the same. Hence, a second pass of graph generation is performed, once the highest upper limit
# for each Y axis is found. That value is passed to the matlab graph generation script. At the very end, all the png files in each of the tweek directories
# are copied to one directory, such that they can be found and compared with ease. 
#

require 'fileutils'

topDir = FileUtils.getwd()
allPngFilesDir = topDir + "/simulationOutputPngs/"
FileUtils.makedirs(allPngFilesDir)														# create the directory where all the png graph plots are to be copied into
dataFile = "multipleDataOutput.txt"														# the data file that the script will compile graphs from

path = Dir.pwd
k = path.rindex('Treg_2D')
headDir = path[0..k-1]                                              # dynamically find the location of this experimental set up. 

observationEnd = ''
errorBars = false;
errorBarsInterval = 5;
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
  if arg == '-error'
    errorBars = true
    errorBarsInterval = ARGV[i+1]
  end
end



drawSimOutputGraphLocation = headDir + '/Treg_2D/data_analysis/robustnessAnalysis/drawSimOutputGraph.m'


contents = Dir.entries('.').reject!{|i| File.directory?(i) == false}    # get all the directories contained in the current directory. 
contents.reject!{|i| File.exists?(i + "/" + dataFile) == false}         # remove directories that do not contain the dataFile we are interested in.

if File.exists?('simoutputGraphAxesLimits_syswide') then FileUtils.rm('simoutputGraphAxesLimits_syswide') end
if File.exists?('simoutputGraphAxesLimits_cumulativeKilled') then FileUtils.rm('simoutputGraphAxesLimits_cumulativeKilled') end
if File.exists?('simoutputGraphAxesLimits_cumulativeCompartmentKilled') then FileUtils.rm('simoutputGraphAxesLimits_cumulativeCompartmentKilled') end
if File.exists?('simoutputGraphAxesLimits_primedCompTh') then FileUtils.rm('simoutputGraphAxesLimits_primedCompTh') end
if File.exists?('simoutputGraphAxesLimits_primedCompCD4Treg') then FileUtils.rm('simoutputGraphAxesLimits_primedCompCD4Treg') end
if File.exists?('simoutputGraphAxesLimits_primedCompCD8Treg') then FileUtils.rm('simoutputGraphAxesLimits_primedCompCD8Treg') end
if File.exists?('simoutputGraphAxesLimits_cumulativeNeuronsKilled') then FileUtils.rm('simoutputGraphAxesLimits_cumulativeNeuronsKilled') end
if File.exists?('simoutputGraphAxesLimits_neuronsKilled') then FileUtils.rm('simoutputGraphAxesLimits_neuronsKilled') end
if File.exists?('simoutputGraphAxesLimits_apcStatesCLN') then FileUtils.rm('simoutputGraphAxesLimits_apcStatesCLN') end
if File.exists?('simoutputGraphAxesLimits_apcStatesSpleen') then FileUtils.rm('simoutputGraphAxesLimits_apcStatesSpleen') end
if File.exists?('simoutputGraphAxesLimits_apcStatesCNS') then FileUtils.rm('simoutputGraphAxesLimits_apcStatesCNS') end
if File.exists?('simoutputGraphAxesLimits_apcStatesSLO') then FileUtils.rm('simoutputGraphAxesLimits_apcStatesSLO') end
if File.exists?('simoutputGraphAxesLimits_cd4ThStates') then FileUtils.rm('simoutputGraphAxesLimits_cd4ThStates') end
if File.exists?('simoutputGraphAxesLimits_cd4TregStates') then FileUtils.rm('simoutputGraphAxesLimits_cd4TregStates') end
if File.exists?('simoutputGraphAxesLimits_cd8TregStates') then FileUtils.rm('simoutputGraphAxesLimits_cd8TregStates') end
if File.exists?('simoutputGraphAxesLimits_thCNS') then FileUtils.rm('simoutputGraphAxesLimits_thCNS') end


contents.each do |dir|
  Dir.chdir(topDir + '/' + dir)
  FileUtils.ln_s(drawSimOutputGraphLocation, '.', :force => true) 
  args = '-save 0 -calculateYs 1 '
  if observationEnd != ''
    args += '-end ' + observationEnd + ' '
  end
  if errorBars == true
    args += '-error ' + errorBarsInterval.to_s + ' '
  end
  system('matlab -nosplash -nodesktop -r "drawSimOutputGraph(\''+ args + '\');quit"')
end

Dir.chdir(topDir)
syswideAxes = []                                                # these arrays will store the y-axis height of each of the graphs generated by drawSimOutputGraph script. 
cumulativeKilledAxes = []
cumulativeKilledCompartmentsAxes = []
primedCompThAxes = []
primedCompCD4TregAxes = []
primedCompCD8TregAxes = []
cumulativeNeuronsKilledAxes = []
neuronsKilledAxes = []
apcStatesCLNAxes = []
apcStatesSpleenAxes = []
apcStatesCNSAxes = []
apcStatesSLOAxes = []
clnAPCPolarizationsAxes = []
cd4ThStatesAxes = []
cd4TregStatesAxes = []
cd8TregStatesAxes = []
thCNSAxes = []
File.open('simoutputGraphAxesLimits_syswide', 'r') do |f|       # read the file that contains these y-axis figures, put those numbers into the arrays. 
  while (line = f.gets)
    syswideAxes << line.to_f
  end
end
File.open('simoutputGraphAxesLimits_cumulativeKilled', 'r') do |f|
  while (line = f.gets)
    cumulativeKilledAxes << line.to_f
  end
end
File.open('simoutputGraphAxesLimits_cumulativeCompartmentKilled') do |f|
  while(line = f.gets)
    cumulativeKilledCompartmentsAxes<< line.to_f
  end
end
File.open('simoutputGraphAxesLimits_primedCompTh') do |f|
  while(line = f.gets)
    primedCompThAxes<< line.to_f
  end
end
File.open('simoutputGraphAxesLimits_primedCompCD4Treg') do |f|
  while(line = f.gets)
    primedCompCD4TregAxes<< line.to_f
  end
end
File.open('simoutputGraphAxesLimits_primedCompCD8Treg') do |f|
  while(line = f.gets)
    primedCompCD8TregAxes<< line.to_f
  end
end
File.open('simoutputGraphAxesLimits_cumulativeNeuronsKilled') do |f|
  while(line = f.gets)
    cumulativeNeuronsKilledAxes<< line.to_f
  end
end
File.open('simoutputGraphAxesLimits_neuronsKilled') do |f|
  while(line = f.gets)
    neuronsKilledAxes<< line.to_f
  end
end
File.open('simoutputGraphAxesLimits_apcStatesCLN') do |f|
  while(line = f.gets)
    apcStatesCLNAxes<< line.to_f
  end  
end
File.open('simoutputGraphAxesLimits_apcStatesSpleen') do |f|
  while(line = f.gets)
    apcStatesSpleenAxes<< line.to_f
  end  
end
File.open('simoutputGraphAxesLimits_apcStatesCNS') do |f|
  while(line = f.gets)
    apcStatesCNSAxes<< line.to_f
  end  
end
File.open('simoutputGraphAxesLimits_apcStatesSLO') do |f|
  while(line = f.gets)
    apcStatesSLOAxes<< line.to_f
  end  
end
File.open('simoutputGraphAxesLimits_clnAPCPolarizations') do |f|
  while(line = f.gets)
    clnAPCPolarizationsAxes<< line.to_f
  end  
end

File.open('simoutputGraphAxesLimits_cd4ThStates') do |f|
  while(line = f.gets)
    cd4ThStatesAxes<< line.to_f
  end  
end
File.open('simoutputGraphAxesLimits_cd4TregStates') do |f|
  while(line = f.gets)
    cd4TregStatesAxes<< line.to_f
  end  
end
File.open('simoutputGraphAxesLimits_cd8TregStates') do |f|
  while(line = f.gets)
    cd8TregStatesAxes<< line.to_f
  end  
end
File.open('simoutputGraphAxesLimits_thCNS') do |f|
  while(line = f.gets)
    thCNSAxes<< line.to_f
  end  
end


args = '-save 1 -calculateYs 0 '                         # setting arguments for the next execution of the drawSimOutputGraph scripts. 
  if observationEnd != ''
    args += '-end ' + observationEnd + ' '
  end
  if errorBars == true
    args += '-error ' + errorBarsInterval.to_s + ' '
  end

args += '-syswideY ' + syswideAxes.max.to_s + ' '
args += '-cumkillY ' + cumulativeKilledAxes.max.to_s + ' '
args += '-cumkillCompY ' + cumulativeKilledCompartmentsAxes.max.to_s + ' '
args += '-primedCompThY ' + primedCompThAxes.max.to_s + ' '
args += '-primedCompCD4TregY ' + primedCompCD4TregAxes.max.to_s + ' '
args += '-primedCompCD8TregY ' + primedCompCD8TregAxes.max.to_s + ' '
args += '-cumNeuronsKilledY ' +  cumulativeNeuronsKilledAxes.max.to_s + ' '
args += '-neuronsKilledY ' +  neuronsKilledAxes.max.to_s + ' '
args += '-apcStatesCLNY ' + apcStatesCLNAxes.max.to_s + ' '
args += '-apcStatesSpleenY ' + apcStatesSpleenAxes.max.to_s + ' '
args += '-apcStatesCNSY ' + apcStatesCNSAxes.max.to_s + ' '
args += '-apcStatesSLOY ' + apcStatesSLOAxes.max.to_s + ' '
args += '-clnAPCPolarizationsY ' + clnAPCPolarizationsAxes.max.to_s + ' '
args += '-cd4ThStatesY ' + cd4ThStatesAxes.max.to_s + ' '
args += '-cd4TregStatesY ' + cd4TregStatesAxes.max.to_s + ' '
args += '-cd8TregStatesY ' + cd8TregStatesAxes.max.to_s + ' '
args += '-thCNSY ' + thCNSAxes.max.to_s + ' '

puts "args = " + args

contents.each do |dir|
  Dir.chdir(topDir + '/' + dir)
  # second pass, this time adjusting all graphs of the same type to have the same height. 
  system('matlab -nosplash -nodesktop -r "drawSimOutputGraph(\'' + args + '\');quit"')

  pngFiles = Dir.entries('.').reject!{|i| i.rindex('.png') == nil}
  pngFiles.each do |f|
    FileUtils.cp(f, allPngFilesDir)
  end
end

Dir.chdir(topDir)
if File.exists?('simoutputGraphAxesLimits_syswide') then FileUtils.rm('simoutputGraphAxesLimits_syswide') end
if File.exists?('simoutputGraphAxesLimits_cumulativeKilled') then FileUtils.rm('simoutputGraphAxesLimits_cumulativeKilled') end
if File.exists?('simoutputGraphAxesLimits_cumulativeCompartmentKilled') then FileUtils.rm('simoutputGraphAxesLimits_cumulativeCompartmentKilled') end
if File.exists?('simoutputGraphAxesLimits_primedCompTh') then FileUtils.rm('simoutputGraphAxesLimits_primedCompTh') end
if File.exists?('simoutputGraphAxesLimits_primedCompCD4Treg') then FileUtils.rm('simoutputGraphAxesLimits_primedCompCD4Treg') end
if File.exists?('simoutputGraphAxesLimits_primedCompCD8Treg') then FileUtils.rm('simoutputGraphAxesLimits_primedCompCD8Treg') end
if File.exists?('simoutputGraphAxesLimits_cumulativeNeuronsKilled') then FileUtils.rm('simoutputGraphAxesLimits_cumulativeNeuronsKilled') end
if File.exists?('simoutputGraphAxesLimits_neuronsKilled') then FileUtils.rm('simoutputGraphAxesLimits_neuronsKilled') end
if File.exists?('simoutputGraphAxesLimits_apcStatesCLN') then FileUtils.rm('simoutputGraphAxesLimits_apcStatesCLN') end
if File.exists?('simoutputGraphAxesLimits_apcStatesSpleen') then FileUtils.rm('simoutputGraphAxesLimits_apcStatesSpleen') end
if File.exists?('simoutputGraphAxesLimits_apcStatesCNS') then FileUtils.rm('simoutputGraphAxesLimits_apcStatesCNS') end
if File.exists?('simoutputGraphAxesLimits_apcStatesSLO') then FileUtils.rm('simoutputGraphAxesLimits_apcStatesSLO') end
if File.exists?('simoutputGraphAxesLimits_clnAPCPolarizations') then FileUtils.rm('simoutputGraphAxesLimits_clnAPCPolarizations') end
if File.exists?('simoutputGraphAxesLimits_cd4ThStates') then FileUtils.rm('simoutputGraphAxesLimits_cd4ThStates') end
if File.exists?('simoutputGraphAxesLimits_cd4TregStates') then FileUtils.rm('simoutputGraphAxesLimits_cd4TregStates') end
if File.exists?('simoutputGraphAxesLimits_cd8TregStates') then FileUtils.rm('simoutputGraphAxesLimits_cd8TregStates') end
if File.exists?('simoutputGraphAxesLimits_thCNS') then FileUtils.rm('simoutputGraphAxesLimits_thCNS') end

