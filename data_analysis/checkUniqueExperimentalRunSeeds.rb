# This script should be called within a directory that contains singleRunData files (either a SingleRun experment, or a SensitivityAnalysis exp). 
# It will print to the command line whether all the seeds within the directory are unique. This is important, since experiments with the same
# seed will give the same results. We want all the simulation runs to have unique seeds. 
#
# It works by reopening the enumerable class and adding a method that allows duplicate entires in an array to be returned as a separate array. 


$seedFileIndicator = 'simRunSeed_'

# Taken from http://snippets.dzone.com/posts/show/3838
# Will return the duplicate entries in an array. (it provides an array.dups method)
#
module Enumerable
  def dups
    inject({}) {|h,v| h[v]=h[v].to_i+1; h}.reject{|k,v| v==1}.keys
  end
end



contents = Dir.entries(".")                                               # retrieve names (but not paths) of all the current working directory's contents. 
contents.reject!{|i| i.index($seedFileIndicator) == nil}                  # reject all contents that do not contain this search string. 

seeds = []                                                                # seed numbers will be stored in here
seedsHash = Hash.new                                                      # here we will store seed numbers (values) against the files in which they are contained (key, since they're unique)
contents.each do |seedFile|                                               # go through each seed file, 
  File.open(seedFile, 'r') do |file|                                          
    num = file.gets.to_i                                                  # read the contents, and store them
    seeds<< num
    seedsHash[seedFile] = num
  end
end

duplicates = seeds.dups                                                   # find duplicate seeds
  
if duplicates.size == 0 
  puts "PASSSED, all " + contents.size.to_s + " seeds unique in directory " + Dir.pwd
else
  puts "FAILED, seeds are not all unique in directory " + Dir.pwd

  seedsHash.reject!{|key,val| duplicates.include?(val) == false}          # delete all enties for which the value is not a duplicate
  arr = seedsHash.to_a
  arr.sort!{|a,b| a[1] <=> b[1]}                                          # sort the array of [key, value] pairs according to values. 
  arr.each{|a| puts "filename = " + a[0] + " seed = " + a[1].to_s }       # this will print which files contain duplicate seed values. 
end

  
