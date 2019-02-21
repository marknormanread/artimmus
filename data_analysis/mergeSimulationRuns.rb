# will copy all the simulation output data files from the source (first command line arg) to the dest (second command line arg). The files being copied
# are renamed such that the numbers in the names have no gaps. Note that this script does not delete files from the destination folder. 
# paths can be absolute, or relative. 
#
# The script will also attempt to find and move any seed number files that are associated with siomulation output data files. 

require 'find'
require 'fileutils'

puts "Reading command line arguments"
src = ARGV[0]
dst = ARGV[1]

#puts "source = " + src
#puts "dest = " + dst

$datafileNamePrefix = "simOutputData_"
$datafileNamePostfix = ".txt"
$seedFileNamePrefix = "simRunSeed_"

# a wrapper for the name of a file containing simulation output data. We do this so that we can extract the number from the name and perform 
# an array sort on that. The class also determines whether there is a seed file (containing the seed that the experiment ran with) for
# the experiment that it represents. If it is present, its name is calculated, since we wish to copy the seed across also. 
class FileData
  attr_reader :seedPresent, :seedFileName                     # allows reading of these attributes. 
  attr_reader :fname                                          # allows retrieval of the single run data file name
  attr_reader :num                                            # allows retrieval of the experimental run that this FileData object represents. 

  # standard constructor. The entire path to a singleRunData file should be provided, NOT just its name. 
  def initialize(name)
    @fname = name                                               # path to the singleRunData file. 
    path = name[0 .. name.rindex('/')]                          # separate the path from the file name, we will use this to search for the associated seed file. 
    fileName = name[name.rindex('/') + 1 .. -1]                 # separate the file name from the path.
                                        # extract the run number of the singleRunDataFile 
    @num = fileName[fileName.index($datafileNamePrefix)+$datafileNamePrefix.length .. fileName.index($datafileNamePostfix)-1].to_i
    if File.exists?(path + $seedFileNamePrefix + @num.to_s)     # attempt to find a seed file.
      @seedPresent = true                                       # store the fact that the file was found
      @seedFileName = path + $seedFileNamePrefix + @num.to_s    # and store its *path* (not just name). 
    end
  end

  # for use in sorting arrays of this object. Method returns -1 if the instance is smaller than the referenced object.
  # returns 1 if this instance is greater than the referenced object
  # returns 0 if they are the same. 
  def <=>(o)
    if @num < o.num
      return -1
    end
    if @num > o.num
      return 1
    end

    return 0
  end
end


srcFiles = []
dstFiles = []


# find simulation output data files in the source folder. 
files = Dir.entries(src)                                          # retrieve contents of src directory
files.reject!{|i| File.directory?(i) == true}                     # remove directories. 
files.reject!{|file| file.index($datafileNamePrefix) == nil }     # remove those files that do not contain the datafileNamePrefix string. 
files.each do |filename|
  srcFiles<< FileData.new(src + '/' + filename)
end

srcFiles.sort!                        # sort the file name wrapper objects in this array (according to the number in the name)

files = Dir.entries(dst).reject!{|i| File.directory?(i) == true}     # retrieve contents of the dst directory, ignoring directories. 
files.reject!{|filename| filename.index($datafileNamePrefix) == nil} # remove files that do not contain this string in their name (ignoring path). 
files.each do |filename|
  dstFiles<< FileData.new(dst + '/' + filename)
end

dstFiles.sort!                        # sort the array of files in the destination directory

puts dstFiles.size
if dstFiles.size == 0
  biggestNum = -1                      # in the event that there is nothing (yet) in the destination folder. 
else
  biggestNum = dstFiles[-1].num         # find the biggest number named file in the destination folder
end
puts "this many files in the destination folder = " + (biggestNum + 1).to_s

srcFiles.each do |f|                  # go through each of the source files, we're going to copy them across to the destination folder, but with a new name. 
  biggestNum = biggestNum + 1         # increment the number that will form part of the name

  newName = $datafileNamePrefix + biggestNum.to_s + $datafileNamePostfix      # compile the new name, the prefix, the unmber, and then the postfix
  FileUtils.cp(f.fname, dst + "/" + newName, :verbose => true)                # copy the file across
  if f.seedPresent                                                            # if there is a seed file...
    newName = $seedFileNamePrefix + biggestNum.to_s                           # then compile the name for its replacement file
    FileUtils.cp(f.seedFileName, dst + "/" + newName, :verbose => true)       # and copy it across. 
  end
end




