
require 'find'

$dirPrefix = "LHC1_run_"

# a wrapper for the name of a file containing simulation output data. We do this so that we can extract the number from the name and perform 
# an array sort on that. 
class FileData
  # standard constructor. Pass in the file name. Does not have to have the path preceeding the name removed. 
  def initialize(name, prefix)
    @fname = name
    temp = name[name.index(prefix)+prefix.length..-1].to_i
    puts "temp =  " + temp.to_s
    @num = temp
  end

  # for use in sorting arrays of this object. Method returns -1 if the instance is smaller than the referenced object.
  # returns 1 if this instance is greater than the referenced object
  # returns 0 if they are the same. 
  def <=>(o)
    if @num < o.getNum
      return -1
    end
    if @num > o.getNum
      return 1
    end

    return 0
  end

  # standard getters.
  def getName
    return @fname
  end
  def getNum
    return @num
  end

  # for testing mostly. But if you do want to knwo what the name of the file contained within this object is, then use this.
  def to_s
    return @fname
  end


end


exp = []
# find all the LHC1 output folders, and compile their paths into 'exp'
Find.find(Dir.pwd) do |path|
	if FileTest.directory? path
		folderName = path[path.index("/")+1..-1]
		if folderName.index($dirPrefix) != nil
			puts "found " + folderName
			exp<< FileData.new(path, $dirPrefix)
		end
	end
end

exp.sort!
exp.each do |e|
	puts "e = " + e.getNum.to_s
end

outputFile = "progress"
File.open(outputFile, 'w') do |f|
	exp.each do |path|
		f.write("\n\n" + path.getName + "\n")
		files = Dir.entries(path.getName).reject{|i| i.index("simOutputData_") == nil}
		dataFiles = []
		if files.length != 0
			files.each{|x| dataFiles<< FileData.new(x, "simOutputData_")}
			dataFiles.sort!		
			f.write(dataFiles[-1].getName)		
		end
#		Find.find(path.getName)	do |k|
#		 	fileName = k[k.rindex("/") + 1 .. -1]
#		 	if fileName.index("simOutputData_") != nil
#		 		f.write(fileName + "\n")
#		 	end
#		end
	end
end

#system("date > " + outputFile)
#	exp.each do |path|
#		system("echo '' > ../" + outputFile + " ; echo '' > ../" + outputFile)
#		system("echo " + path)
#		system("cd " + path)
#		system("ls > ../" + outputFile)
#	end
#end
