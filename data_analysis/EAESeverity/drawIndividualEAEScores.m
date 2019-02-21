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

% This matlab script will open the 'EAESeverityScoresForRuns' file (which must have been compiled by another matlab script),
% and go through each run in turn, plotting its progression on a separate graph. The graphs are stored in a folder named
% 'EAE_individuals'.

LineWidth = 2.0;
FontSize = 18;



%----------------------------------
% this lets the user specify the maixmum number of days over which graphs are drawn (on the x axis). 
endTime = Inf;
%endTime = 40;
%----------------------------------


%-------------------------------------------------
% open this example and find out how long the table is (how many rows, representing samples in time), and how many columns it has. 
fid = fopen('simOutputData_0.txt');
l = fgetl(fid);                                 % read the first line, which in the case of single run data lines ALWAYS includes a comment line at the top. 
numCols = length(find(l == ' '));               % extact the number of columns from the number of spaces in the comment line
example = fscanf(fid, '%f ', [numCols,Inf]);    % read in the remainder of the file (header comment line already read) into an array. 
timeSamples = length(example(1,:));
fclose(fid);

dataPrefix = 'simOutputData_';
files = dir([dataPrefix '*']);                  % the '*' is a wild wild card. Specifying 'crude_analysis...' will stop '.' and '..' appearing in the list. 
numRuns = length(files)
%---------------------------------------------
fileNameTag = '';
if ~isinf(endTime)
  timeSamples = endTime;
  fileNameTag = [num2str(endTime) '_days__' fileNameTag];
end

if exist('EAE_individuals') ~= 7
  mkdir('EAE_individuals')
end



% open the EAE scores file for all runs. Read in the data. 
% this will dynamically read the file into a matrix. THe matrix will have 'numRuns+1' rows, and 'timeSamples' columns. 
fid = fopen('EAESeverityScoresForRuns');
EAEScores = fscanf(fid, '%f', [numRuns + 1,timeSamples]);     % dynamic scan to fill the given matrix dimensions, note the single '%f'
EAEScores = EAEScores'; % this switches the data to the more familiar structure, time and then runs along the columns, and rows being time samples. EAEScores(<rows>,<cols>)

Xs = EAEScores(:,1);                           % pull out the time in hours, and convert into days. 

runsToDraw = size(EAEScores,2);
runsToDraw = 50;
for col = 2:runsToDraw + 1                    % first column contains hours data, so ignore it. 
  dataRunNumber = col-2;                      % columns are ordered run numbers, ie, second column is simOutputData_0.txt. This converts column numbers back to runs. 
  clf;
  plot(Xs, EAEScores(:,col), 'b-','LineWidth',LineWidth)
  set(gca,'box','on');                                % draw a box around the plot. 
  set(gca,'LineWidth',LineWidth);
  set(gca,'FontSize',FontSize);
  B = [Xs(1), Xs(end), 0, 5];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                            % replot with differen mins and maxes for axes
  set(gca,'YTick',[0 1 2 3 4 5])
  ylabel('EAE severity','FontSize',FontSize);
  xlabel('Time (days)','FontSize',FontSize);
  print('-dpng', '-r300', ['EAE_individuals/EAEScoreIndividual_' num2str(dataRunNumber) '__' fileNameTag '.png'])    % write the graph to the file system. 
end
