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

% This script will find all single run data files within the current working directory. It will deduce their EAE scores over time, and will write those
% scores, for each run, to a file named 'EAESeverityScoresForRuns'. The columns of this file represent sigle run data file scores, whilst the rows represent
% sample points in time. The very first column is just the time at which the sample was taken. 
%
% EAE severity scores are calculated as follows. 
% A cumulative count of the number of neurons killed over time are extracted as a vector from the singleRunDataFile. 
% This is differentiated, that is, for each time point, the difference between neurons killed at that time point, and the number of neurons killed 12 hours ago
% This data is then smoothed, based on averages. The average is calculated from a range of one day either side of the current time being smoothed. 
% Previous calibration activities derived a model that associates this smoothed-differentiated-figure with an EAE score. That function is appled to the data to find EAE scores over time
% Those EAE scores are very precise, so they are rounded to the nearest EAE number. 
% Those EAE numbers are then written to the file system. 
%
% Note that smoothing has been found to produce strange artifacts near the extremes of the data (when the data is non-zero, or non-stable, such as is the case for 
% prolonged autoimmunity data sets). 
% The artifact has the effect of providing sharp rises or falls in smoothed data near the end, since the influence of the last data items in the data series is exaggerated. 
% This can create problems for scoring based on neuronal apoptosis data, since the number of neurons apoptosed at the end of the data series can suddenly vary wildly.
% As a result, there is now a parameter to this script provided as an argument, which dictates how much data to compile into EAE
% scores. It is hihgly recommended that the end of the data be ignored to avoid these artifacts. 
%
% Current arguments:
% -end XXX    ... this dicates when to stop compiling EAE data from smoothed neuronal apoptosis data. It is expressed in days. If not provided, the full range of neuronal 
 %                apoptosis data will be used. 
function compileEAESeveritiesForRuns(ARGS)

path = pwd;
k = findstr('Treg_2D',path);
headDir = path(1:k(end)-1);                     % dynamically locate where teh data analysis file is located, based in searching for 'Treg_2D' in the current working dir. 
addpath(genpath([headDir '/Treg_2D/data_analysis/EAESeverity/']))
addpath(genpath([headDir '/Treg_2D/data_analysis/matlab_helper_functions/']))
 

%
% Read arguments
%
args = split_str([' '], ARGS)        % split the arguments string acording to spaces. 
observationEnd = Inf
for i = 1 : length(args)  
  if strcmp(args{i}, '-end'), observationEnd = str2num(args{i+1}) +1 , end    % 1 is added because day zero is on index 1. 
end



dataPrefix = 'simOutputData_';
files = dir([dataPrefix '*']);                  % the '*' is a wild wild card. Specifying 'crude_analysis...' will stop '.' and '..' appearing in the list. 
%-------------------------------------------------
% This piece of code will re-order the contents of the files data structure according to the run number (extracted from each file name and converted into a number). 
for f = 1:length(files);
  name = files(f).name;
  runNums(f) = str2num(name(length('simOutputData_')+1 : length(name) - length('.txt')));
end
[unused,order] = sort(runNums);                 % runs are sorted, such that the first items in the final table of EAE scores correspond to the lowest numbered runs. 
files = files(order);
%-------------------------------------------------


%-------------------------------------------------
% open this example and find out how long the table is (how many rows, representing samples in time), and how many columns it has. 
if exist('simOutputData_0.txt','file') == 0     % safety, in case the script has been run on a directory that does not contain all the necessary data. This stops automation crashing. 
  quit;
end
fid = fopen('simOutputData_0.txt');
l = fgetl(fid);                                 % read the first line, which in the case of single run data lines ALWAYS includes a comment line at the top. 
numCols = length(find(l == ' '));               % extact the number of columns from the number of spaces in the comment line
example = fscanf(fid, '%f ', [numCols,Inf]);    % read in the remainder of the file (header comment line already read) into an array. 
numRows = length(example(1,:));
fclose(fid);
%-------------------------------------------------

if isinf(observationEnd)
  observationEnd = ceil(24*numRows);
end
EAEScores = zeros(length(files), observationEnd);  % stores the EAE score for each run, over each time sample point. 

%-------------------------------------------------
% These values were obtained using the framework for analysing the effect of different smoothing windows. They represent threshold values that map neurons killed onto 
% EAE severity scores. The framework may be considered 'version 2', operating on the proportion of in vivo runs that reached a particular score at any point 
% in time. 
modelOfEAE = [ 56.66,   69.32,   71.65,   74.96,  78.64];   % for period=1, window=157


period = 1.0;                                 % period of time over which differtials are taken. 
window = 157;
%-------------------------------------------------


LineWidth = 1.0;
FontSize = 12;

max_neurons_killed = [];                        % maximum number of (smoothed!) neurons killed in an hour (at daily samples) for each run will be stored in here. 
% read the data into a matrix
for run = 1:length(files)  
  run
  fid = fopen(files(run).name);
    fgetl(fid);                                 % throw away the first line, it contains only a comment.
    temp = fscanf(fid,'%f ',[numCols,Inf]);
  fclose(fid);  

  neuronsKilledCumulative = temp(89,:);         % this column represents the cumulative count of neurons killed over time. 
  diffs = differentiate_neurons_killed(neuronsKilledCumulative,(1:numRows),period); % calculate the differentials for each hour. 
  smoothdiffs = smooth(diffs,window);           % smooth this data

  dailySmoothDiffs = zeros(observationEnd,1);
  dailySmoothDiffs(:) = smoothdiffs(1:24:(24*observationEnd));

  max_neurons_killed(run) = max(dailySmoothDiffs);


  %-------------------------------------------------
  % this code will assign EAE scores for an entire day (24 hours, 24 sample points) based on sampling the number of neurons killed just once in
  % a 24 hour period. 
    scores_24 = zeros(1,observationEnd);
    for day = 1:observationEnd      % 24:(numRows)
       
      neurons_killed = dailySmoothDiffs(day);   
      if neurons_killed >= modelOfEAE(5)
        scores_24(day) = 5;
      elseif neurons_killed >= modelOfEAE(4)
        scores_24(day) = 4;
      elseif neurons_killed >= modelOfEAE(3)
        scores_24(day) = 3;
      elseif neurons_killed >= modelOfEAE(2)
        scores_24(day) = 2;
      elseif neurons_killed >= modelOfEAE(1)
        scores_24(day) = 1;
      else
        scores_24(day) = 0;
      end
    end
  %-------------------------------------------------

  EAEScores(run,:) = scores_24;

  k = find(EAEScores(run,:) >= 5.0);
  if ~isempty(k)
    EAEScores(run,k(1):end) = 5.0;
  end

end

clf;
[prop,vals] = ecdf(max_neurons_killed);
hold on;
plot(vals,prop,'LineWidth',LineWidth);
set(gca,'box','on');
set(gca,'FontSize',FontSize);
set(gca,'LineWidth',LineWidth);
B = axis();

  % plot vertical lines at the threshold neurons killed values, for each of the 5 EAE severity scores. 
line([modelOfEAE(1) modelOfEAE(1)],[B(3) B(4)],'color','k','LineStyle',':','LineWidth',LineWidth);
line([modelOfEAE(2) modelOfEAE(2)],[B(3) B(4)],'color','k','LineStyle',':','LineWidth',LineWidth);
line([modelOfEAE(3) modelOfEAE(3)],[B(3) B(4)],'color','k','LineStyle',':','LineWidth',LineWidth);
line([modelOfEAE(4) modelOfEAE(4)],[B(3) B(4)],'color','k','LineStyle',':','LineWidth',LineWidth);
line([modelOfEAE(5) modelOfEAE(5)],[B(3) B(4)],'color','k','LineStyle',':','LineWidth',LineWidth);

  % plot horrizontal black dotted lines every 10% of population. 
B = axis();
line([B(1) B(2)],[0.1 0.1],'color','k','LineStyle',':','LineWidth',LineWidth);
line([B(1) B(2)],[0.2 0.2],'color','k','LineStyle',':','LineWidth',LineWidth);
line([B(1) B(2)],[0.3 0.3],'color','k','LineStyle',':','LineWidth',LineWidth);
line([B(1) B(2)],[0.4 0.4],'color','k','LineStyle',':','LineWidth',LineWidth);
line([B(1) B(2)],[0.5 0.5],'color','k','LineStyle',':','LineWidth',LineWidth);
line([B(1) B(2)],[0.6 0.6],'color','k','LineStyle',':','LineWidth',LineWidth);
line([B(1) B(2)],[0.7 0.7],'color','k','LineStyle',':','LineWidth',LineWidth);
line([B(1) B(2)],[0.8 0.8],'color','k','LineStyle',':','LineWidth',LineWidth);
line([B(1) B(2)],[0.9 0.9],'color','k','LineStyle',':','LineWidth',LineWidth);

hold off;
print('-dpng','-r300',['cdf_max_neurons_killed___period=' num2str(period) '___window=' num2str(window)])

% write EAE scores to a file. 
fid = fopen('EAESeverityScoresForRuns', 'w');
for row = 1:size(EAEScores,2)
  fprintf(fid, '%g ', row-1);               % minus one because the first day of observation is day zero.
  for f = 1:length(files)
    fprintf(fid, '%g ', EAEScores(f,row));
  end
  fprintf(fid, '\n');
end
fclose(fid);





