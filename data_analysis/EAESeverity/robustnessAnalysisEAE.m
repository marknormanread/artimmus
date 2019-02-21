% 
% Copyright 2012 Mark Read
% 
% This file is part of ARTIMMUS.
% 
% ARTIMMUS is free software: you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation, either version 3 of the License, or
% (at your option) any later version.
% 
% ARTIMMUS is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
% 
% You should have received a copy of the GNU General Public License
% along with ARTIMMUS.  If not, see <http://www.gnu.org/licenses/>.
%

%
% The script is a function, such that command line arguments may be provided. These should take the form 
% "robustnessAnalysisEAE('-units probability')"
%
% Script will perform a robustness analysis on avilable data, but will also draw a graph of how proportions of runs reaching particular maximum scores
% changes with parameter values. hence, 6 line graph, one line for each EAE severity score, plotted against parameter. 
function robustnessAnalysisEAE(ARGS)
%ARGS = '-xaxis Drug efficacy (precentage)'
drawGraphs = true;


path = pwd;
k = findstr('Treg_2D',path);
headDir = path(1:k(end)-1);                     % dynamically locate where teh data analysis file is located, based in searching for 'Treg_2D' in the current working dir. 

addpath(genpath([headDir '/Treg_2D/data_analysis']))
pathToMetaData = [headDir '/Treg_2D/parameters_-_metaData.xml'];  % this file contains meta data about the parameters being analysed, including their units. 

dataPrefix = 'simOutputData_';
significanceThreshold = 1.0;

LineWidth = 2.0;
FontSize = 18;

%----------------------------------------------------
% Read arguments, and pull out the units of the parameter being tested here if they are not provided automatically. 
%
args = split_str([' '], ARGS)        % split the arguments string acording to spaces. 

paramUnitsProvided = false;
paramUnits = '';                      % the string identifying the units of the parameter being altered. 
defaultParamValue = [];
default_d_index = [];                     % directories referred to as 'd' in this script, this variable holds the index of the default param value as a 'd'. 
defaultParamValueProvided = false;
xAxisProvided = false;
xAxisLabel = '';
yAxisProvided = false;
yAxisLabel = '';
for i = 1 : length(args)             % go through each argument in turn
  if strcmp(args{i}, '-units')       % permits the provision of units for parameter that is subject to the current analysis. 
    paramUnits = args{i+1}; 
    paramUnitsProvided = true;        % this stops matlab attempting to automatically derive the units from the metadata parameters xml file. 
  end
  if strcmp(args{i}, '-xaxis')
    xAxisLabel = '';
    for j = i+1:length(args)
      xAxisLabel = [xAxisLabel ' ' args{j}];
    end
    xAxisProvided = true;    
    paramUnitsProvided = true;       % presume that user has supplied units in the axis label. 
  end
  if strcmp(args{i}, '-yaxis')
    yAxisLabel = args{i+1};
    yAxisProvided = true;
  end
  if strcmp(args{i}, '-default')
    defaultParamValue = str2num(args{i+1});
    defaultParamValueProvided = true;
  end
end

if defaultParamValueProvided == false
  if exist('defaultParameterValue') == 2
    fid = fopen('defaultParameterValue');
    temp = textscan(fid, '%f');
    defaultParamValue = temp{1}
    fclose(fid);
    defaultParamValueProvided = true;
  end
end

% last ditch effort to extract default parameter value, from the metadata file. 
if defaultParamValueProvided == 0
  paramsMetaData = xml_load(pathToMetaData);         % open the xml file.   
  directory = pwd                                   % get the current working directory, we will find the current parameter's units from this file. 
  slashLocations = find(directory == '/')           % returns an array of where the '/' locations are 
  directory = directory(slashLocations(end)+1 : end)   % this will pull out only the "sens_Anal_-_....._Reg" part of the directory. 
  tags = split_str(['_'], directory)                % split the directory into separate strings based on underscores
  tags(end) = []                                    % delete 'Reg' (or 'EAE') from end. 
  tags([1,2]) = []                                 % delete the first two items, which will be 'sensAnal' and '-' (this latter one because of '_-_').  
  command = 'temp = paramsMetaData.';               % start to build a command to dynamically extract the parameter's units from the xml file. 
  for i = 1 : length(tags)                           % for each tag, add to the command. 
    command = [command tags{i} '.'];
  end
  command = [command 'default'];                        % the final tag we need is 'units', this will contain the units we are interested in. 
  eval(command);                                      % this executes the command. The resulting variable 'paramUnits' will contain the units string. 
  defaultParamValue = str2num(temp);
  defaultParamValueProvided = true;
end


if paramUnitsProvided == false      % automatically extract the units from the metadata file, if they are not provided as arguments. 
  paramsMetaData = xml_load(pathToMetaData);         % open the xml file. 

  % extract from the above meta data xml file the current parameter's units. 
  directory = pwd;                                   % get the current working directory, we will find the current parameter's units from this file. 
  slashLocations = find(directory == '/');           % returns an array of where the '/' locations are 
  directory = directory(slashLocations(end)+1 : end);   % this will pull out only the "sens_Anal_-_....._Reg" part of the directory. 
  tags = split_str(['_'], directory);                % split the directory into separate strings based on underscores
%  tags(end) = []                                    % delete 'Reg' (or 'EAE') from end. 
  tags([1,2]) = [];                                  % delete the first two items, which will be 'sensAnal' and '-' (this latter one because of '_-_').  
  command = 'paramUnits = paramsMetaData.';          % start to build a command to dynamically extract the parameter's units from the xml file. 
  for i = 1 : length(tags);                           % for each tag, add to the command. 
    command = [command tags{i} '.'];
  end
  command = [command 'units']                        % the final tag we need is 'units', this will contain the units we are interested in. 
  eval(command);                                      % this executes the command. The resulting variable 'paramUnits' will contain the units string. 
end

%----------------------------------------------------

outputPostfix = pwd;                                % output postfix will appear on the end of the graphs generated in this script. 
temp = find( pwd == '/' );                          % find locations of all the forward slashes in the pwd.  
outputPostfix = outputPostfix(temp(end)+1:end);     % assign outputPostfix to everything following the last slash             
if ~isempty(strfind(outputPostfix, 'sensAnal_-_')); % if outputPostfix contains the specified string, then remove it. 
  outputPostfix = outputPostfix(length('sensAnal_-_')+1:end);
end

%----------------------------------------------------
% find all directories that contain single run data, and EAE score data. 
dirs = dir;                                         % will extract all the files & directories listed in the cwd. 
dirs(find( [dirs(:).isdir] == 0)) = [];             % removes those files & dirs that are not directories. 

lose = [];                                          % will use this to remove direcetories that do not contain EAE score data. 
for d = 1:length(dirs)                              % go through each directory, looking for the EAE scores data file.
  fn = [dirs(d).name '/EAESeverityScoresForRuns'];  % dynamically calculate where the EAE scores data file ought to be. 
  if exist(fn) ~= 2                                 % test whether such a file exists. 
    lose(end + 1) = d;                              % if it does not, then this directory should be excluded. 
  end  
end

dirs(lose) = [];
% dirs now holds the directories that contain EAESeverity scores files. 
%----------------------------------------------------

EAEScores = [];                                   % this matric will hold EAE scores for all runs, in all directories. EAEScores(<dir>,<rows/samples>,<cols/runs>). first column is time.
for d = 1:length(dirs)
  %---------------------------------------------
  % Reading arbritray sized files into matricies seems a bit tricky in matlab, so this will find out how big the file will be. 
  % the EAE scores file has the following format. rows = time sample points. First column is time of sample point, and every additional column represents a run. 

  files = dir([dirs(d).name '/' dataPrefix '*']); % the '*' is a wild wild card. Specifying 'dataPrefix' will return any matches. 
  dirs(d).name
  numRuns = length(files);                                      
  %---------------------------------------------

  fid = fopen([dirs(d).name '/EAESeverityScoresForRuns']);
  temp = fscanf(fid, '%f', [numRuns + 1,Inf]);    % dynamic scan to fill the given matrix dimensions, note the single '%f'
  EAEScores(d,:,:) = temp';                       % this switches the data to the more familiar structure, time and then runs along the columns, and rows being time samples. EAEScores(<dir>,<rows>,<cols>)
end

%----------------------------------------------------
% These loops sort the data in EAE scores, along the runs. This is necessary such that pulling out particular quartile values is possible. 
for d = 1:length(dirs)
  for t = 1:size(EAEScores,2)
    EAEScores(d,t,2:end) = sort(EAEScores(d,t,2:end));
  end
end
%----------------------------------------------------

%----------------------------------------------------
% Extract the maximum scores achieved at any point during excution, for each run in each directory (experiment). 
MaxEAEScores = zeros([length(dirs),numRuns]);               % stores the maximum EAE score in each run, for each directory [<dir>,<run>]
for d = 1:length(dirs)
  for r = 1:numRuns                               % scan across all runs. First item is time, so ignore that. 
    MaxEAEScores(d,r) = max(EAEScores(d,:,r+1));  % remember that first items of EAEScores row is the time, hence r+1
  end
end
%----------------------------------------------------

%----------------------------------------------------
% these two matricies are going to hold the summary EAE score data. EAEScores holds the raw data of EAE progression for each run. Here this data is summarised into 
% median and mean EAE scores covering all the runs, for each sample point in time. 
EAEMedians = [];  
EAEMeans = [];
for d = 1:length(dirs)                            % analysis performed for each directory. 
  for t = 1:size(EAEScores,2)                     % and for each time sample point. 
    y = EAEScores(d, t, 2:end);                   % contenience, refer to this as 'y. The first column of EAEScores contains time data, time at which sample occurred, so we start from 2. 
    EAEMedians(d, t).median = median( y );
    EAEMedians(d, t).Qlow = y( ceil(0.25 * length(y)) );   % 25 and 75 here represent the upper and lower quartiles. They may be changed. 
    EAEMedians(d, t).Qhigh = y( ceil(0.75 * length(y)) );  % obtain the ceil here (rather than round) to avoid rounding down to zero, since that is not a valid matlab array index. 

    EAEMeans(d, t).mean = mean(y);
    EAEMeans(d, t).std = std(y);
    EAEMeans(d, t).low = EAEMeans(d, t).mean - EAEMeans(d, t).std;    % collect data for a standard deviation below and above the mean. 
    EAEMeans(d, t).high = EAEMeans(d, t).mean + EAEMeans(d, t).std;
  end
end
%----------------------------------------------------

%---------------------------------------------
% data preparation nearly completed: each directory (parameter value) has been summarised. 
% The data prepared/extracted here is what is to be drawn on the robustness analysis graph. 
% currently there are two points in EAE severity that are examined: the maximum EAE score experienced (based on both median and mean summary data)
% the level of EAE experienced at 40 days (1000h) is also examined. 
robustnessData = [];                                                % this is where data for each directory/parameter-value is to be collected. 
for d = 1:length(dirs)  
  index = find(EAEScores(d,:,1) == 40);                             % index of 40th day dynamically found, this is case the resolution of data collection ever changes. 
  [val, ind] = max([EAEMeans(d,:).mean]);                           % find the maximum EAE score found, and also its index (needed because interquartile ranges are also extracted).
  robustnessData(d).meanMaxEAE = val(1);                            % score the max value.
  robustnessData(d).meanMaxLow = EAEMeans(d,ind(1)).low;            % find the upper and lower quartiles (whatever they might have been set to, above).
  robustnessData(d).meanMaxHigh = EAEMeans(d,ind(1)).high;

  [val, ind] = max([EAEMedians(d,:).median]);
  robustnessData(d).medianMaxEAE = val(1);
  robustnessData(d).medianMaxLow = EAEMedians(d,ind(1)).Qlow;
  robustnessData(d).medianMaxHigh = EAEMedians(d,ind(1)).Qhigh;

  robustnessData(d).day40MeanEAE = EAEMeans(d,index).mean;
  robustnessData(d).day40MeanLow = EAEMeans(d,index).low;
  robustnessData(d).day40MeanHigh = EAEMeans(d,index).high;

  robustnessData(d).day40MedianEAE = EAEMedians(d,index).median;
  robustnessData(d).day40MedianLow = EAEMedians(d,index).Qlow;
  robustnessData(d).day40MedianHigh = EAEMedians(d,index).Qhigh;

  robustnessData(d).proportionMaxLevel5 = (length(find(MaxEAEScores(d,:) == 5)) / numRuns) * 100;
  robustnessData(d).proportionMaxLevel4 = (length(find(MaxEAEScores(d,:) == 4)) / numRuns) * 100;
  robustnessData(d).proportionMaxLevel3 = (length(find(MaxEAEScores(d,:) == 3)) / numRuns) * 100;
  robustnessData(d).proportionMaxLevel2 = (length(find(MaxEAEScores(d,:) == 2)) / numRuns) * 100;
  robustnessData(d).proportionMaxLevel1 = (length(find(MaxEAEScores(d,:) == 1)) / numRuns) * 100;
  robustnessData(d).proportionMaxLevel0 = (length(find(MaxEAEScores(d,:) == 0)) / numRuns) * 100;
 
  robustnessData(d).dead =  length(find(MaxEAEScores(d,:) == 5));  % the number of simulations that died for this parameter
  robustnessData(d).alive = length(find(MaxEAEScores(d,:) ~= 5)); % the number of simulations that survived for thsi parameter

  underscoreLocations = find(dirs(d).name == '_');                            % returns an array containing the locations of all the underscores in the string 'name'
  robustnessData(d).paramVal = str2num(dirs(d).name(underscoreLocations(end) + 1 : end));    % the parameter value is the last thing in the name string following directly from the last underscore. 
end

% reorder the robustness analysis data in order of increasing parameter value represented by each directory. 
[unused, order] = sort([robustnessData(:).paramVal]);
robustnessData(:) = robustnessData(order);

if defaultParamValueProvided == true
  temp = find( [robustnessData(:).paramVal] == defaultParamValue );
  default_d_index = temp;
  defaultParamMaxScoreMean = robustnessData(temp(1)).meanMaxEAE;
  defaultParamMaxScoreMedian = robustnessData(temp(1)).medianMaxEAE;

  defaultParam40ScoreMean = robustnessData(temp(1)).day40MeanEAE;
  defaultParam40ScoreMedian = robustnessData(temp(1)).day40MedianEAE;
end



%---------------------------------------------
% graphs will need to write to the file system. Create the directory to hold graphs, if it does not already exist. 
if exist('robustness_sensitivity_analysis') == 0
  mkdir('robustness_sensitivity_analysis');
end



robustnessIndexes = [];
robustnessIndexes(1).name = 'MaxEAE_raw';
robustnessIndexes(2).name = 'EAEat40d_raw';
robustnessIndexes(1).lowerBoundary = NaN;
robustnessIndexes(1).upperBoundary = NaN;
robustnessIndexes(1).lowerDirection = '.';
robustnessIndexes(1).upperDirection = '.';
robustnessIndexes(2).lowerBoundary = NaN;
robustnessIndexes(2).upperBoundary = NaN;
robustnessIndexes(2).lowerDirection = '.';
robustnessIndexes(2).upperDirection = '.';


defaultIndex = find([robustnessData(:).paramVal] == defaultParamValue)
% -------------------------------------
% calculate higher end robustness index. 
for p = defaultIndex + 1 : length(robustnessData)  
  % move out from the default to the extreme, operating over each range of response data values
  x_lo = robustnessData(p-1).paramVal;
  x_hi = robustnessData(p).paramVal;
  y_lo = robustnessData(p-1).meanMaxEAE;
  y_hi = robustnessData(p).meanMaxEAE;

  u_lo = robustnessData(1).paramVal;
  u_hi = robustnessData(end).paramVal;
  v_lo = defaultParamMaxScoreMean + significanceThreshold;
  v_hi = defaultParamMaxScoreMean + significanceThreshold;

  % perform the line cross test
  [flag, x_cross, y_cross] = IntersectionOfLines(x_lo, y_lo, x_hi, y_hi, u_lo, v_lo, u_hi, v_hi);
  if flag == 1
    % if the point at which the lines cross is within the range of response data values, then stop, the point is found  
    if x_cross >= x_lo & x_cross <= x_hi
      robustnessIndexes(1).upperBoundary = x_cross;
      robustnessIndexes(1).upperDirection = '+';
      break
    end
  end
  
  v_lo = defaultParamMaxScoreMean - significanceThreshold;
  v_hi = defaultParamMaxScoreMean - significanceThreshold;
  [flag, x_cross, y_cross] = IntersectionOfLines(x_lo, y_lo, x_hi, y_hi, u_lo, v_lo, u_hi, v_hi);
  if flag == 1
    % if the point at which the lines cross is within the range of response data values, then stop, the point is found
    if x_cross >= x_lo & x_cross <= x_hi
      robustnessIndexes(1).upperBoundary = x_cross;
      robustnessIndexes(1).upperDirection = '-';
      break
    end
  end
end
% -------------------------------------
% -------------------------------------
% calculate the lower end robustness index
for p = defaultIndex -1 :-1: 1 % cycle backwards, from default downwards. 
  x_lo = robustnessData(p).paramVal;
  x_hi = robustnessData(p + 1).paramVal;
  y_lo = robustnessData(p).meanMaxEAE;
  y_hi = robustnessData(p + 1).meanMaxEAE;

  u_lo = robustnessData(1).paramVal;
  u_hi = robustnessData(end).paramVal;
  v_lo = defaultParamMaxScoreMean + significanceThreshold;
  v_hi = defaultParamMaxScoreMean + significanceThreshold;

  % perform the line cross test
  [flag, x_cross, y_cross] = IntersectionOfLines(x_lo, y_lo, x_hi, y_hi, u_lo, v_lo, u_hi, v_hi);
  if flag == 1
    % if the point at which the lines cross is within the range of response data values, then stop, the point is found  
    if x_cross >= x_lo & x_cross <= x_hi
      robustnessIndexes(1).lowerBoundary = x_cross;
      robustnessIndexes(1).lowerDirection = '+';
      break
    end
  end
  
  v_lo = defaultParamMaxScoreMean - significanceThreshold;
  v_hi = defaultParamMaxScoreMean - significanceThreshold;
  [flag, x_cross, y_cross] = IntersectionOfLines(x_lo, y_lo, x_hi, y_hi, u_lo, v_lo, u_hi, v_hi);
  if flag == 1
    % if the point at which the lines cross is within the range of response data values, then stop, the point is found
    if x_cross >= x_lo & x_cross <= x_hi
      robustnessIndexes(1).lowerBoundary = x_cross;
      robustnessIndexes(1).lowerDirection = '-';
      break
    end
  end
end
% -------------------------------------
% -------------------------------------
% calculate higher end robustness index. 
for p = defaultIndex + 1 : length(robustnessData)  
  % move out from the default to the extreme, operating over each range of response data values
  x_lo = robustnessData(p-1).paramVal;
  x_hi = robustnessData(p).paramVal;
  y_lo = robustnessData(p-1).day40MeanEAE;
  y_hi = robustnessData(p).day40MeanEAE;

  u_lo = robustnessData(1).paramVal;
  u_hi = robustnessData(end).paramVal;
  v_lo = defaultParam40ScoreMean + significanceThreshold;
  v_hi = defaultParam40ScoreMean + significanceThreshold;

  % perform the line cross test
  [flag, x_cross, y_cross] = IntersectionOfLines(x_lo, y_lo, x_hi, y_hi, u_lo, v_lo, u_hi, v_hi);
  if flag == 1
    % if the point at which the lines cross is within the range of response data values, then stop, the point is found  
    if x_cross >= x_lo & x_cross <= x_hi
      robustnessIndexes(2).upperBoundary = x_cross;
      robustnessIndexes(2).upperDirection = '+';
      break
    end
  end
  
  v_lo = defaultParam40ScoreMean - significanceThreshold;
  v_hi = defaultParam40ScoreMean - significanceThreshold;
  [flag, x_cross, y_cross] = IntersectionOfLines(x_lo, y_lo, x_hi, y_hi, u_lo, v_lo, u_hi, v_hi);
  if flag == 1
    % if the point at which the lines cross is within the range of response data values, then stop, the point is found
    if x_cross >= x_lo & x_cross <= x_hi
      robustnessIndexes(2).upperBoundary = x_cross;
      robustnessIndexes(2).upperDirection = '-';
      break
    end
  end
end
% -------------------------------------
% -------------------------------------
% calculate the lower end robustness index
for p = defaultIndex -1 :-1: 1 % cycle backwards, from default downwards. 
  x_lo = robustnessData(p).paramVal;
  x_hi = robustnessData(p + 1).paramVal;
  y_lo = robustnessData(p).day40MeanEAE;
  y_hi = robustnessData(p + 1).day40MeanEAE;

  u_lo = robustnessData(1).paramVal;
  u_hi = robustnessData(end).paramVal;
  v_lo = defaultParam40ScoreMean + significanceThreshold;
  v_hi = defaultParam40ScoreMean + significanceThreshold;

  % perform the line cross test
  [flag, x_cross, y_cross] = IntersectionOfLines(x_lo, y_lo, x_hi, y_hi, u_lo, v_lo, u_hi, v_hi);
  if flag == 1
    % if the point at which the lines cross is within the range of response data values, then stop, the point is found  
    if x_cross >= x_lo & x_cross <= x_hi
      robustnessIndexes(2).lowerBoundary = x_cross;
      robustnessIndexes(2).lowerDirection = '+';
      break
    end
  end
  
  v_lo = defaultParam40ScoreMean - significanceThreshold;
  v_hi = defaultParam40ScoreMean - significanceThreshold;
  [flag, x_cross, y_cross] = IntersectionOfLines(x_lo, y_lo, x_hi, y_hi, u_lo, v_lo, u_hi, v_hi);
  if flag == 1
    % if the point at which the lines cross is within the range of response data values, then stop, the point is found
    if x_cross >= x_lo & x_cross <= x_hi
      robustnessIndexes(2).lowerBoundary = x_cross;
      robustnessIndexes(2).lowerDirection = '-';
      break
    end
  end
end
% -------------------------------------


%-------------------------------
% This code will calculate percentage boundaries for parameter perturbations resulting in significant behavioural deviations. 
default = robustnessData(defaultIndex).paramVal;
for r = 1:length(robustnessIndexes)

  robustnessIndexes(r).upperPercent = abs(abs(default - robustnessIndexes(r).upperBoundary) / default) * 100;
  robustnessIndexes(r).lowerPercent = abs(abs(default - robustnessIndexes(r).lowerBoundary) / default) * 100;

  % boundaries can have values of either a number, Inf, or NaN. I wish for Inf to rank in front of NaN (which it does by default). However, NaN in these
  % calculations does not compute properly, as such we have to do these explicit checks first. 
  if isnan(robustnessIndexes(r).upperBoundary) & ~isnan(robustnessIndexes(r).lowerBoundary)
    robustnessIndexes(r).closestBoundary = robustnessIndexes(r).lowerBoundary;
    robustnessIndexes(r).closestPercent = robustnessIndexes(r).lowerPercent;  

  elseif ~isnan(robustnessIndexes(r).upperBoundary) & isnan(robustnessIndexes(r).lowerBoundary)
    robustnessIndexes(r).closestBoundary = robustnessIndexes(r).upperBoundary;
    robustnessIndexes(r).closestPercent = robustnessIndexes(r).upperPercent;

  else                % if both upperBoundary and lowerBoundary are NaN, this still works. If neither of them are,then this works too. 
    if abs(default - robustnessIndexes(r).upperBoundary) < abs(default - robustnessIndexes(r).lowerBoundary)
      robustnessIndexes(r).closestBoundary = robustnessIndexes(r).upperBoundary;
      robustnessIndexes(r).closestPercent = robustnessIndexes(r).upperPercent;
    else
      robustnessIndexes(r).closestBoundary = robustnessIndexes(r).lowerBoundary;
      robustnessIndexes(r).closestPercent = robustnessIndexes(r).lowerPercent;
    end
  end
end
%-------------------------------

%-------------------------------
% write response robustness indexes to the filesystem, copying existing data in the file over, if the file exists, since this data will have been generated by RobustnessAnalysis.m.
if exist(['robustness_sensitivity_analysis/robustness_indexes_-_' outputPostfix])
  fid = fopen(['robustness_sensitivity_analysis/robustness_indexes_-_' outputPostfix],'r'); % read the existing data. 
    existing = textscan(fid,'%s %s %s %s %s %s %s %s %s %s %s\n');
  fclose(fid); 

  delete(['robustness_sensitivity_analysis/robustness_indexes_-_' outputPostfix]);          % remove the existing file, and re-write the whole lot. 

  fid = fopen(['robustness_sensitivity_analysis/robustness_indexes_-_' outputPostfix],'w'); % write the existing data
    for row = 1:12
      fprintf(fid,'%40s ',existing{1}{row});
      fprintf(fid,'%10s ',existing{2}{row});
      fprintf(fid,'%10s ',existing{3}{row});
      fprintf(fid,'%10s ',existing{4}{row});
      fprintf(fid,'%10s ',existing{5}{row});
      fprintf(fid,'%10s ',existing{6}{row});
      fprintf(fid,'%10s ',existing{7}{row});
      fprintf(fid,'%10s ',existing{8}{row});
      fprintf(fid,'%10s ',existing{9}{row});
      fprintf(fid,'%10s ',existing{10}{row});
      fprintf(fid,'%10s ',existing{11}{row});
      fprintf(fid,'\n');
    end  
else
  fid = fopen(['robustness_sensitivity_analysis/robustness_indexes_-_' outputPostfix],'w'); % if the file does not already exist, then place the following header information in. 
  fprintf(fid,'%40s %10s %10s %10s %10s %10s %10s %10s %10s %10s %10s\n','#Response', 'close_P', 'close_B', 'low_P', 'up_P', 'low_B', 'low_dir', 'default', 'up_B', 'up_dir', 'units');
end

for r = 1:length(robustnessIndexes)
  fprintf(fid, '%40s ', robustnessIndexes(r).name);
  fprintf(fid, '%10.4G ', robustnessIndexes(r).closestPercent);
  fprintf(fid, '%10.4G ', robustnessIndexes(r).closestBoundary);
  fprintf(fid, '%10.4G ', robustnessIndexes(r).lowerPercent);
  fprintf(fid, '%10.4G ', robustnessIndexes(r).upperPercent);
  fprintf(fid, '%10.4G ', robustnessIndexes(r).lowerBoundary);
  fprintf(fid, '%10s '  , robustnessIndexes(r).lowerDirection);
  fprintf(fid, '%10.4G ', default);
  fprintf(fid, '%10.4G ', robustnessIndexes(r).upperBoundary);
  fprintf(fid, '%10s '  , robustnessIndexes(r).upperDirection);
  fprintf(fid, '%10s '   , paramUnits);
  fprintf(fid, '\n');
end
fclose(fid);


if drawGraphs
  %---------------------------------------------
  % Two graphs are drawn, based on median summary data, and on mean data too. 

  if xAxisProvided == false
    xAxisLabel = ['parameter value (' paramUnits ')'];
  end
 

  Xs = [robustnessData(:).paramVal];                          % the x-axis data to be plotted. 
%  Xs = Xs .* 100;

  clf;                                                       % clears the figure. Not a problem if script is called in a batch mode, but for testing we need to clear. 
  hold on
  h = []                                                     % h contains handles to the lines plotted, used to create legends for only some of the lines plotted. 
  h(1) = plot(Xs, [robustnessData(:).meanMaxEAE], 'bo-','LineWidth',LineWidth)
  h(2) = plot(Xs, [robustnessData(:).day40MeanEAE,], 'rs-','LineWidth',LineWidth)

  set(gca,'box','on');
  set(gca,'LineWidth',1.0);                           % draw a thicker box around the plot. 

  if defaultParamValueProvided == false
    plot(Xs, [robustnessData(:).meanMaxLow], 'b:', 'LineWidth', LineWidth)
    plot(Xs, [robustnessData(:).meanMaxHigh], 'b:', 'LineWidth', LineWidth)

    plot(Xs, [robustnessData(:).day40MeanLow,], 'r:', 'LineWidth', LineWidth)
    plot(Xs, [robustnessData(:).day40MeanHigh,], 'r:', 'LineWidth', LineWidth)
  end
  hold off

  B = [Xs(1), Xs(end), 0, 5];                               % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                                  % replot with different mins and maxes for axes
  set(gca,'YTick',[0 1 2 3 4 5]);
  set(gca,'FontSize',FontSize);
  xlabel(xAxisLabel,'FontSize',FontSize);
  ylabel('EAE severity score','FontSize',FontSize);

  if defaultParamValueProvided  == true
    h(3) = line([defaultParamValue, defaultParamValue], [0, 5], 'color', 'k', 'LineStyle', '-.','LineWidth',LineWidth)     % draws a vertical line at the default parameter value. 
    line([Xs(1), Xs(end)], [defaultParamMaxScoreMean + significanceThreshold, defaultParamMaxScoreMean + significanceThreshold], 'color', 'b', 'LineStyle', ':', 'LineWidth', LineWidth);  
    line([Xs(1), Xs(end)], [defaultParamMaxScoreMean - significanceThreshold, defaultParamMaxScoreMean - significanceThreshold], 'color', 'b', 'LineStyle', ':', 'LineWidth', LineWidth);

    line([Xs(1), Xs(end)], [defaultParam40ScoreMean + significanceThreshold, defaultParam40ScoreMean + significanceThreshold], 'color', 'r', 'LineStyle', ':', 'LineWidth', LineWidth);
    line([Xs(1), Xs(end)], [defaultParam40ScoreMean - significanceThreshold, defaultParam40ScoreMean - significanceThreshold], 'color', 'r', 'LineStyle', ':', 'LineWidth', LineWidth);
    [legh,objh,outh,outm] = legend(h,{'Max EAE score'; 'EAE score @ 40d'; 'default param val'})
    set(legh,'LineWidth',0.5*LineWidth);                      % sets the line width of the legend axis (the box around the legend)
    set(objh,'LineWidth',LineWidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.  
  else
    [legh,objh,outh,outm] = legend(h,{'Max EAE score'; 'EAE score @ 40d'}) ;
    set(legh,'LineWidth',0.5*LineWidth);                      % sets the line width of the legend axis (the box around the legend)
    set(objh,'LineWidth',LineWidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.  
  end

  print('-dpng', '-r300', ['robustness_sensitivity_analysis/EAERobustnessAnalysisMean_-_' outputPostfix '.png'])    % write the graph to the file system. 



  clf;



  hold on
  h = []                                                     % h contains handles to the lines plotted, used to create legends for only some of the lines plotted. 
  h(1) = plot(Xs, [robustnessData(:).medianMaxEAE], 'bo-','LineWidth',LineWidth)
  h(2) = plot(Xs, [robustnessData(:).day40MedianEAE,], 'rs-','LineWidth',LineWidth)

  set(gca,'box','on');
  set(gca,'LineWidth',LineWidth);                           % draw a thicker box around the plot. 

  if defaultParamValueProvided == false
    plot(Xs, [robustnessData(:).medianMaxLow], 'b:', 'LineWidth', LineWidth)
    plot(Xs, [robustnessData(:).medianMaxHigh], 'b:', 'LineWidth',LineWidth)

    plot(Xs, [robustnessData(:).day40MedianLow,], 'r:', 'LineWidth',LineWidth)
    plot(Xs, [robustnessData(:).day40MedianHigh,], 'r:', 'LineWidth', LineWidth)
  end
  hold off

  B = [Xs(1), Xs(end), 0, 5];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                          % replot with differen mins and maxes for axes
  set(gca,'YTick',[0 1 2 3 4 5])                    % what points on the y-axis to draw tick lines at. 
  set(gca,'FontSize',12);
  xlabel(xAxisLabel,'FontSize',FontSize)
  ylabel('EAE severity score','FontSize',FontSize)

  if defaultParamValueProvided  == true
    h(3) = line([defaultParamValue, defaultParamValue], [0, 5], 'color', 'k', 'LineStyle', '-.','LineWidth',1.0)     % draws a vertical line at the default parameter value. 
    line([Xs(1), Xs(end)], [defaultParamMaxScoreMedian + significanceThreshold, defaultParamMaxScoreMedian + significanceThreshold], 'color', 'b', 'LineStyle', ':', 'LineWidth',LineWidth) ;
    line([Xs(1), Xs(end)], [defaultParamMaxScoreMedian - significanceThreshold, defaultParamMaxScoreMedian - significanceThreshold], 'color', 'b', 'LineStyle', ':', 'LineWidth',LineWidth) ;

    line([Xs(1), Xs(end)], [defaultParam40ScoreMedian + significanceThreshold, defaultParam40ScoreMedian + significanceThreshold], 'color', 'r', 'LineStyle', ':', 'LineWidth',LineWidth) ;
    line([Xs(1), Xs(end)], [defaultParam40ScoreMedian - significanceThreshold, defaultParam40ScoreMedian - significanceThreshold], 'color', 'r', 'LineStyle', ':', 'LineWidth',LineWidth) ;
    [legh,objh,outh,outm] = legend(h,{'Max EAE score'; 'EAE score @ 40d'; 'default param val'}) 
    set(legh,'LineWidth',0.5*LineWidth);                      % sets the line width of the legend axis (the box around the legend)
    set(objh,'LineWidth',LineWidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.  
  else
    [legh,objh,outh,outm] = legend(h,{'Max EAE score'; 'EAE score @ 40d'}) 
    set(legh,'LineWidth',0.5*LineWidth);                      % sets the line width of the legend axis (the box around the legend)
    set(objh,'LineWidth',LineWidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.  
  end

  print('-dpng', '-r300', ['robustness_sensitivity_analysis/EAERobustnessAnalysisMedian_-_' outputPostfix '.png'])    % write the graph to the file system. 





  %=======================================================
  %=======================================================
  % DRAW A-TEST GRAPHS FOR EAE RESPONSES.
  %=======================================================
  %=======================================================
  % Start by finding all the EAE response data files, and copying them into the robustness sensitivity analysis directory. 
  for d = 1:length(dirs)
    cd(dirs(d).name);
    generateEAESeverityResponses
    cd ..
    copyfile([dirs(d).name '/EAESeverity_response_data*'],'robustness_sensitivity_analysis/.')
  end

  % everything from here on needs to be computed within the robustness sensitivity analysis directory, so may as well cd there now.
  cd 'robustness_sensitivity_analysis'


  files = dir('EAESeverity_response_data*');                  % the '*' is a wild wild card. Specifying 'robustness_analysis...' will stop '.' and '..' appearing in the list. 
  Responses = {'Max EAE', 'EAE @ 40 days'};

  % load the response data for each parameter value into a data strcuture, and note the parameter value as we go.
  for f = 1:length(files)
    fid = fopen(files(f).name,'r');
    l = fgetl(fid);                                           % throw away the first line, its just a comment. 
    EAEResponses(f).data = fscanf(fid,'%f',[3,Inf]);
    underscoreLocations = find(files(f).name == '_');
    EAEResponses(f).paramVal = str2num(files(f).name(underscoreLocations(end) + 1 : end));    % the parameter value is the last thing in the name string following directly from the last
  end  

  % the default parameter file will be in here somewhere, this will identify it, and explicitly hold another reference to it. 
  for f = 1:length(files)
    if EAEResponses(f).paramVal == defaultParamValue
      defaultEAEResponse = EAEResponses(f);
      break;
    end
  end

  % 'unused' stores an array of the sorted ParamVals, but we want to sort the entire structure based on those values, not those values alone. So we are interested in 'order'
  % This is necessary for plotting the data. Lines are drawn between consequtive items passed to the plot command. If they're not sorted according to domain then you 
  % get strange artefacts. 
  [unused, order] = sort([EAEResponses(:).paramVal]);     
  EAEResponses = EAEResponses(order);                              % reassign 'data' based on the correct ordering


  %  This will calculate the A test scores for the two responses. 
  for f = 1:length(EAEResponses)
    EAEResponses(f).ATestMax = Atest(EAEResponses(f).data(2,:)', defaultEAEResponse.data(2,:)');      % first column is just the time of the sample (in days).
    if (abs(EAEResponses(f).ATestMax - 0.5) + 0.5) >= 0.71
      EAEResponses(f).AtestMaxSignificant = 1;
    else
      EAEResponses(f).AtestMaxSignificant = 0;
    end

    EAEResponses(f).ATest40 = Atest(EAEResponses(f).data(3,:)', defaultEAEResponse.data(3,:)');      % first column is just the time of the sample (in days).
    if (abs(EAEResponses(f).ATest40 - 0.5) + 0.5) >= 0.71
      EAEResponses(f).Atest40Significant = 1;
    else
      EAEResponses(f).Atest40Significant = 0;
    end

  end


  % pull EAE response data and place it in arrays that are more suitable for plotting graphs with. 
  Ps = [];
  AsMax = [];
  As40 = [];
  for p = 1:length(EAEResponses)                              % iterate through each parameter value
    AsMax(p) = EAEResponses(p).ATestMax;
    As40(p) = EAEResponses(p).ATest40; 
  end
  AsMax                                                  % uncomment if you want to see the data being generated. 
  As40
  Ps = [EAEResponses(:).paramVal]
  if islogical(Ps)                                    % some of the plotting scripts don't work with logical (boolean) data types, so convert to double if necessary.
    Ps = double(Ps);
  end

  clf;                                                % clear the figure of anything that might previously have been displayed. 

  % plot a graph of parameter values against A test scores. 
  hold on;
  h(1) = plot(Ps, AsMax, 'b-o', 'LineWidth', LineWidth);
  h(2) = plot(Ps, As40, 'r-+', 'LineWidth', LineWidth);

  set(gca,'box','on');
  set(gca,'LineWidth',LineWidth);                           % draw a thicker box around the plot. 
  set(gca,'FontSize',FontSize);
  hold off;
   
  line([Ps(1), Ps(length(Ps))], [0.71, 0.71], 'color', 'k', 'LineStyle', ':','LineWidth',LineWidth);   % draw the 0.71 effect magnitude line
  line([Ps(1), Ps(length(Ps))], [0.29, 0.29], 'color', 'k', 'LineStyle', ':','LineWidth',LineWidth);   % draw the 0.29 effect magnitude line


  B = [Ps(1), Ps(end), 0.0, 1.0];                     % axis ranges, [xmin, xmax, ymin, ymax].
  axis(B);                                            % replot with differen mins and maxes for axes


  if defaultParamValueProvided  == true
    h(3) = line([defaultParamValue, defaultParamValue], [B(3), B(4)], 'color', 'k', 'LineStyle', '-.','LineWidth',1.0);     % draws a vertical line at the default parameter value. 
    Responses{end + 1} = 'default param val';
    [legh,objh,outh,outm] = legend(h,{'EAE max';'EAE @ 40 days';'default param val'},'Location','NorthEast','FontSize',FontSize);   % write the legend, which lines are which responses.  
    set(legh,'LineWidth',0.5*LineWidth);                      % sets the line width of the legend axis (the box around the legend)
    set(objh,'LineWidth',LineWidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
  else
    [legh,objh,outh,outm] = legend(Responses,'Location','NorthEast','FontSize',FontSize);                                          % write the legend, which lines are which responses.
    set(legh,'LineWidth',0.5*LineWidth);                      % sets the line width of the legend axis (the box around the legend)
    set(objh,'LineWidth',LineWidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
  end

  ylabel('A test score','FontSize',FontSize);
  xlabel(xAxisLabel,'FontSize',FontSize);

  print('-dpng', '-r300', [pwd '/EAE_robustness_analysis_ATestScores_-_' outputPostfix]);    % write the graph to the file system. 

  yLabel = 'EAE severity score';
  robustnessAnalysisEAE_Responses_plotAndStore(EAEResponses, Ps, defaultEAEResponse, 'max', 'MaxEAEScore', outputPostfix, xAxisLabel, yLabel)
  robustnessAnalysisEAE_Responses_plotAndStore(EAEResponses, Ps, defaultEAEResponse, '40', 'EAEScore40days', outputPostfix, xAxisLabel, yLabel)

  fid = fopen('robustness_analysis_EAE_-_ScoresATest','w');
    fprintf(fid,'#param value, Max EAE Score A test value, EAE Score @ 40 days A test value\n');
    for p = 1:length(EAEResponses)
      fprintf(fid,'%u %4.4f %4.4f\n', EAEResponses(p).paramVal, EAEResponses(p).ATestMax, EAEResponses(p).ATest40);
    end
  fclose(fid);

  %-----------------------------------------------------
  % draw graph of how proportion of simulations reaching each maximum severity score changes with parameter values (this is not a robustnes analysis, but
  % is performed by this script because all the informatino needed to do it is present here).
  clf;
  hold on;
  h = [];                                                     % reset handle for lines.
  Ps
  [robustnessData(:).proportionMaxLevel0]  
  h(1) = plot(Ps,[robustnessData(:).proportionMaxLevel0],'g-o','LineWidth',LineWidth);
  h(2) = plot(Ps,[robustnessData(:).proportionMaxLevel1],'c-o','LineWidth',LineWidth);
  h(3) = plot(Ps,[robustnessData(:).proportionMaxLevel2],'b-o','LineWidth',LineWidth);
  h(4) = plot(Ps,[robustnessData(:).proportionMaxLevel3],'m-o','LineWidth',LineWidth);
  h(5) = plot(Ps,[robustnessData(:).proportionMaxLevel4],'r-o','LineWidth',LineWidth);
  h(6) = plot(Ps,[robustnessData(:).proportionMaxLevel5],'k-o','LineWidth',LineWidth);

  set(gca,'box','on');
  set(gca,'LineWidth',LineWidth);                           % draw a thicker box around the plot. 
  set(gca,'FontSize',FontSize);

  xlabel(xAxisLabel,'FontSize',FontSize);
  ylabel('Proportion of simulations (percentage)','FontSize',FontSize); 

  B = axis;
  B(1) = Ps(1);
  B(2) = Ps(end);                               % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);
  [legh,objh,outh,outm] = legend({'0','1','2','3','4','5'},'Location','NorthEast','FontSize',FontSize)    % write the legend, which lines are which responses.
  set(legh,'LineWidth',0.5*LineWidth);                      % sets the line width of the legend axis (the box around the legend)
  set(objh,'LineWidth',LineWidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.

  print('-dpng', '-r300', [pwd '/ProportionOfRuns_MaxEAESeverities_parameters_graph_-_' outputPostfix])    % write the graph to the file system. 
  %-----------------------------------------------------

end

%-----------------------------------------------------
% This section performs statistical analyses on the death rates under each experiment (parameter
% value). This is performed in the context of a control (the default parameter value). 
% The analyses gage statistical signficance and effect magnitude of the change in death rate
% under each parameter value. 
%
% This experimental data is binary - simulation dies or it does not. Each contrast of parameter
% value with default (control) case constitutes a 'contingency table', as follows:
%
%       | control | experiment
% -------------------------------
% dead  |    a    |    b
% alive |    c    |    d
%
% where a b c and d represent the number of simulations in each case. 
%
% Pearson's correlation co-efficient is calculated, as this is valid for binary data such as this. 
% The correlation is a measure of effect, however there is a caveat to using this data. The biggest
% coefficients (1 or -1) can only be attained if the control data has all dead and the experiment
% contains only alive simulations (or vice versa). Any spread of data across cells a and c in the 
% table above will reduce the maximum possible correlation. Having run this through ARTIMMUS
% data, an experiment that doubles the control death rate from 15% to 30% achieves a correlation
% of only 0.16, which is classed as 'small'. However, I would consider a doubling of the death
% rate as a fairly significant experiment. The point is that caution should be used when 
% using this measure: context dependent (experiment-aware) indications of significance are better
% than using generic guidelines. 
%
% Fisher's exact test is used to obtain the P-value associated with generating this spread of data
% assuming that the null hypothesis is true (the null hypothesis here is that the experiment is of
% no consequence, and that the spread of dead/alive in control should equal the experiment). 
% Fisher's exact test establishes this p-value absolutely, rather than using estimations of 
% distribution as used in Chi-squared. Again, a cautionary note. P-values only give the probability
% of obtaining such data under a hypothesis we know to pretty much always be false. It says nothing
% of the effect magnitude (how important the effect is), it only establishes than an effect is 
% likely, and that the sample size used is sufficient to show it. 
%
% Note that matlab's Pearson coefficient calculation also provides p values - though I believe
% these are estimated. They can be compared to Fisher's, but it is best to use Fishers as it is
% more accurate. 

% compile a vector of 0s and 1s for dead and alive. This represents the control case
default_dead = ones(1,robustnessData(default_d_index).dead);
default_alive = zeros(1,robustnessData(default_d_index).alive);
default_mortality = [default_alive,default_dead];

pearsons_effect = [];   % used for storing values
fishers_exact = [];

for d = 1:length(dirs) 
  dead  = ones(1, robustnessData(d).dead);   % compile similar vectors for the experiment
  alive = zeros(1, robustnessData(d).alive);

  % format of data to go into corr is as follows. Corr compares two columns of values, 
  % treating each row as a co-ordinate. Hence, the first column contains both control and 
  % experiment observations, and the second column indicates whether the observation
  % was control, or experiment. 
  mortality = [alive,dead];
  observations = [default_mortality,mortality];
  experiment = [zeros(1,length(default_mortality)),ones(1,length(mortality))];
  [rho,pval] = corr(observations',experiment'); % rho is correlation coefficient. 

  pearsons_effect(d).paramVal = robustnessData(d).paramVal;
  pearsons_effect(d).rho = rho;
  pearsons_effect(d).pval = pval;

  fishers_exact(d) = fexact(observations',experiment') % input format as with 'corr' above. 
end

% the following will write the data to the filesystem:
% if the file does not already exist, then place the following header information in. 
fid = fopen(['mortality_rate_stats_-_' outputPostfix],'w'); 
fprintf(fid,'%10s %10s %10s %10s\n','#paramVal', 'pearson-r', 'pearson-p', 'fishers-p');

for d = 1:length(dirs)
  fprintf(fid, '%10.4G ', pearsons_effect(d).paramVal);
  fprintf(fid, '%10.4G ', pearsons_effect(d).rho);
  fprintf(fid, '%10.4G ', pearsons_effect(d).pval);
  fprintf(fid, '%10.4G ', fishers_exact(d));
  fprintf(fid, '\n');
end
fclose(fid);
%-----------------------------------------------------



