# The automation of analysis and response generation has required that the automation scripts read the 'parameters.xml' and 'sensitivity_parameters.xml' files
# to pull out things like default values. I imagine that there will be more uses yet for having automation scripts read these files. Often directories are 
# annotated using the tags of the parameters in those xml files, and those tags are separated in teh directorty names by underscores. This creates a problem if the
# tag names themselves contain underscores. The programs cannot know whehter the underscore is part of the tag name, or is separating two tags. 
#
# To rectify this problem, I have removed the underscores from all the variables in the the simulation's java code. I needed to remove the underscores from
# all the parameter files too. That's what this script does. It will recurse through all files within the working directory, and if it finds either of the
# two parameter files, it will remove all the underscores from the file. 

require 'fileutils'
require 'find'

#rootDir = "'" + ARGV[0] + "/'"
name1 = "sensitivity_parameters.xml"
name2 = "parameters.xml"


Find.find(FileUtils.pwd()) do |path|
  if (path[path.rindex('/')+1..-1] == name1 || path[path.rindex('/')+1..-1] == name2)
    puts "operating on file " + path

    File.open(path, 'r+') do |f|  # open file for update
      lines = f.readlines           # read into array of lines
      lines.each do |it|            # for each line...
        it.delete! '_'              # remove all the underscrores
      end

      f.pos = 0                     # back to start
      f.print lines                 # write out modified lines
      f.truncate(f.pos)             # truncate to new length
    end
  end                               # file is automatically closed
end
