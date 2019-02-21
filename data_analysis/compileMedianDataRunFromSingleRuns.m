% Script reads all the single run data files in the current directory, and calculates the median values for each column (data marker from simulation) and
% time sample, across all runs. It then writes this data out to a median data file on the FS. 
%

% find all the relevant files in the directory
dataPrefix = 'simOutputData_';    % the prefix for files that contain response data
files = dir([dataPrefix '*']);                  % the '*' is a wild wild card. Specifying 'lhc1_analysis...' will stop '.' and '..' appearing in the list. 

% read the contents of those files into a large datastructure. This will have to have dimensions : run X column X time
%-------------------------------------------------
% open this example and find out how long the table is (how many rows, representing samples in time), and how many columns it has. 
fid = fopen('simOutputData_0.txt');
firstLine = fgetl(fid);                              % read the first line, which in the case of single run data lines ALWAYS includes a comment line at the top. In doing so, we drop it from consideration. 
numCols = length(find(firstLine == ' '));            % extact the number of columns from the number of spaces in the comment line
fclose(fid);

fid = fopen('simOutputData_0.txt');
firstLine = fgetl(fid);                              % read the first line, since it is only comments. 
example = fscanf(fid, '%f ', [numCols,Inf]);    % read in the remainder of the file (header comment line already read) into an array. 
numRows = length(example(1,:));
fclose(fid);
%-------------------------------------------------

singleRunData = zeros(length(files), numCols, numRows);
for run = 1:length(files)
  files(run).name
  fid = fopen(files(run).name);
    fgetl(fid);                                 % throw away the first line, it contains only a comment.
    singleRunData(run,:,:) = fscanf(fid,'%f ',[numCols,Inf]);
  fclose(fid);
end


% create a separate data structure for the median stuff to be stored in
medians = zeros(numCols, numRows);

% compile medians across the first DS, and place them in the new DS. 
for col = 1:numCols
  for row = 1:numRows
    medians(col,row) = median(singleRunData(:,col,row));
  end
end

% write the new DS to the filesystem under the correct name
medianDataFileName = 'multipleDataOutput.txt';
fid = fopen(medianDataFileName, 'w');
for row = 1:numRows
  for col = 1:numCols
    fprintf(fid, '%g ', medians(col,row));
  end
  fprintf(fid, '\n');
end
fclose(fid);
