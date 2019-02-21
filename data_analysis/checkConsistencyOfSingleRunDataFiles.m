% Matlab script will check the consistency of all singleRun simulation data within a directory. 
%
% Matlab script is designed to be run from within the same directory as a collection of singleRunData files. It will check that the correct
% number of singleRunData files are present, and will check that they follow a continuous ordering from zero to the number that there are (minus 1)
% It will open each file in turn and ensure that the corret number of time samples are present, and that the correct number of columns are printed
% on each line - thus ensuring that all data points are correctly recorded.
%
% The script runs quietly, printing only a message at the end concerning the failure or not of the data in the directory to conform to the
% expected format. If any failures are found, then these are output to stdout, meaning that this script can be called from the terminal
% as part of a batch job and have any failings found redirected and compiled into a file for overview of a series of tests. 

function checkConsistencyOfSingleRunDataFiles(ARGS)

ARGS

path = pwd;
k = findstr('Treg_2D',path);
headDir = path(1:k(end)-1);                     % dynamically locate where teh data analysis file is located, based in searching for 'Treg_2D' in the current working dir. 

addpath(genpath([headDir '/Treg_2D/data_analysis/matlab_helper_functions']))


numRuns = 500;
timeSamples = 1300;


% ------
% read command line arguments
args = split_str([' '], ARGS);        % split the arguments string acording to spaces. 

for i = 1 : length(args)             % go through each argument in turn
  if strcmp(args{i}, '-numRuns')           % input the number of runs expected. This corresponds with the number of single run data files expected to be found. 
    numRuns = str2num(args{i+1}); 
  end
  if strcmp(args{i}, '-timeSamples')       % input the number of rows expected in each file.
    timeSamples = str2num(args{i+1}) ;
  end
end
% ------

dataPrefix = 'simOutputData_';                  % used to find all items in the directory that start with this text. 
dataPostfix = '.txt';                           
files = dir([dataPrefix '*']);                  % the '*' is a wild wild card. Specifying 'crude_analysis...' will stop '.' and '..' appearing in the list. 
failureFound = false;                           % at the end of script execution a pass or fail message is displayed according to how this variable has been manipulated.

fid_res = fopen('consistencyOfDataResult','w');

tmp = split_str(['/'], pwd);                    % extract the last part of the directory name, used in locating the source of errors when outputting. 
dirName = tmp{end};                 

% -------
% first test, are the expected number of simulation runs' data present?
if length(files) ~= numRuns         
  failureFound = true;
  fprintf(2, 'FAIL: %s - found only %u singleRunDataFiles, was expecting %u\n', dirName, length(files), numRuns);
  fprintf(fid_res, 'FAIL: %s - found only %u singleRunDataFiles, was expecting %u\n', dirName, length(files), numRuns);
end
% -------

% -------
% second test, are the single run data files well ordered? No missing files names. 
[unused,order] = sortn({files(:).name});        % sortn will sort strings and treat number characters as numbers, so 'c10' will come before 'c101'. 
files(:) = files(order);                        % reorder files such that they are sorted in ascending order. 


for i=0:numRuns-1                               % i will iterate through all the integer numbers that we expect to see singleRunData files named after. 
  expectedName = [dataPrefix num2str(i) dataPostfix];   % this is the expected file name, check that it exists. 
  if exist(expectedName,'file') ~= 2
    fprintf(2, 'FAIL: %s - this file is missing from directory %s\n', expectedName, dirName);
    fprintf(fid_res, 'file %s is missing from directory %s\n', expectedName, dirName);
    failureFound = true;
  end
end
% -------


for f = 1:length(files)
  fid = fopen(files(f).name, 'r');                                      % open each file in turn. 
        % first dimension (of which there is one item) is just the cell container for everyhting else. Second dimension corresponds to rows. And the third dimension corresponds to the characeters comprising each line/row. 
  data = textscan(fid, '%s', 'delimiter', '\n', 'CommentStyle', '#');   % reads the entire file into a cell array. Each cell in that array contains one line of the file (delimiter set to newline). Each line is itself a cell array. 
  fclose(fid);

  % -------
  % third test, check that the correct number of rows exist in each file.   
  rowsFound = length(data{1});
  if rowsFound ~= timeSamples + 1
    failureFound = true;
    fprintf(2, 'FAIL: %s - file %s contains the wrong number of lines (rows). Found %u, should be %u.\n', dirName, files(f).name, rowsFound, timeSamples + 1);
    fprintf(fid_res, 'FAIL: %s - file %s contains the wrong number of lines (rows). Found %u, should be %u.\n', dirName, files(f).name, rowsFound, timeSamples + 1);
  end
  % -------

  % -------
  % fourth test, check that the same number of columns exist in each row of each file. 
  % 
  if isempty(data{1}) == false      % this test cannot be performed if there is absolutely no data, which will also be thrown up by test three, so these cases may be skipped.
    colsExpected = length(strfind(data{1}{1}, ' '));
    for row = 2:rowsFound
      dataLine = data{1}{row};
      colsFound = length(strfind(dataLine, ' '));
      if colsFound ~= colsExpected
        failureFound = true;
        fprintf(2, 'FAIL: %s - the file %s, line %u contained the wrong number of items. Expecting %u, found %u.\n', dirName, files(f).name, row, colsExpected, colsFound);
        fprintf(fid_res, 'FAIL: %s - the file %s, line %u contained the wrong number of items. Expecting %u, found %u.\n', dirName, files(f).name, row, colsExpected, colsFound);
      end
    end
  end
  % -------
end


if failureFound == false
  fprintf(1, 'PASS for directory %s\n', dirName);
  fprintf(fid_res, 'PASS for directory %s\n', dirName);
else
  fprintf(1, 'FAIL for directory %s\n', dirName);
end
fclose(fid_res);


