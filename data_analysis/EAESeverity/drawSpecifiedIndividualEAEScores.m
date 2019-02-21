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
% Script will take an argument specifying a number of individual run numbers (literally, just their numbers- 1, 37, etc - not their file names),
% and will plot their EAE progressions on the same graph.
%
% example call from matlab  -> drawSpecifiedIndividualEAEScores('2 3 54 6 78')
function drawSpecifiedIndividualEAEScores(ARGS)

LineWidth = 2.0;
FontSize = 18;


times = 1:51;                                  % THIS CAN BE USED TO SPECIFY THE RANGE OF TIME (IN DAYS) THAT IS TO BE PLOTTED ALONG THE X AXIS. Day zero counts as 1. IE, 51 = days 0 - 50

%------------------------
% dynamically build paths to important helper functions. 
path = pwd;
k = findstr('Treg_2D',path);
headDir = path(1:k(end)-1);                     % dynamically locate where the data analysis file is located, based in searching for 'Treg_2D' in the current working dir. 
addpath(genpath([headDir '/Treg_2D/data_analysis/matlab_helper_functions']))
%------------------------


%
% Read arguments
%
files = split_str([' '], ARGS)        % split the arguments string acording to spaces. The arguments represent files names of simulation runs to plot EAE scores of. 

runs = [];                             % will extract from the file names supplied the run numbers specified. 
for f = 1:length(files)
  runs(f) = str2num(files{f});
end

%-------------------------------------------------
% open this example and find out how long the table is (how many rows, representing samples in time), and how many columns it has. 
fid = fopen('simOutputData_0.txt');
line = fgetl(fid);                              % read the first line, which in the case of single run data lines ALWAYS includes a comment line at the top. 
numCols = length(find(line == ' '));            % extact the number of columns from the number of spaces in the comment line
example = fscanf(fid, '%f ', [numCols,Inf]);    % read in the remainder of the file (header comment line already read) into an array. 
timeSamples = length(example(1,:));
fclose(fid);

dataPrefix = 'simOutputData_';
files = dir([dataPrefix '*']);                  % the '*' is a wild wild card. Specifying 'crude_analysis...' will stop '.' and '..' appearing in the list. 
numRuns = length(files)
%---------------------------------------------


% open the EAE scores file for all runs. Read in the data. 
% this will dynamically read the file into a matrix. THe matrix will have 'numRuns+1' rows, and 'timeSamples' columns. 
fid = fopen('EAESeverityScoresForRuns');
EAEScores = fscanf(fid, '%f', [numRuns + 1,timeSamples]);     % dynamic scan to fill the given matrix dimensions, note the single '%f'
EAEScores = EAEScores'; % this switches the data to the more familiar structure, time and then runs along the columns, and rows being time samples. EAEScores(<rows>,<cols>).

clf;                                              
hold on;
styles = {'r-','g-','b-','k-','c-','m-','y-','r:','g:','b:','k:','c:','m:','y:'};
widths = [5.0, 4.5, 4.0, 3.5, 3.0, 2.5, 2.0, 1.5, 1.0, 0.5];

size(EAEScores)
Xs = EAEScores(times,1);                           % extract days. 
for r = 1:length(runs)                            % go through each of the specified run EAE scores, and plot each in turn. 
  runScores = EAEScores(times,runs(r)+2);               % extract EAE scores
  runScores = runScores + (0.03 * r);             % add a little height onto each score, to stop them being plotted over the top of one another. 
  plot(Xs, runScores, styles{r}, 'LineWidth', LineWidth); % plot, and choose a line style dynamically. 
end

set(gca,'box','on');                              % draw a box around the plotting area. 
%xlabel('Time (days)');
%ylabel('EAE Score');
%legend('run1', 'run2', 'run3', 'run4', 'run5', 'run6');
%title('The EAE severity score of simulation execution(s).');

B = [Xs(1), Xs(end), 0, 5.3];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
axis(B);                                          % replot with differen mins and maxes for axes
set(gca,'YTick',[0 1 2 3 4 5])                    % where the unit markings should appear on the y axis. 
set(gca,'linewidth',LineWidth);
set(gca,'fontsize',FontSize);
xlabel('Days','FontSize',FontSize);
ylabel('EAE severity','FontSize',FontSize);

tag = '';                                         % compile a tag to label the graph when written to the file system with. The tag is derived from the run numbers specified. 
for r = 1:length(runs)
  tag = [tag '_' num2str(runs(r))];
end

print('-dpng', '-r300', [pwd '/EAESeverityPlot_' tag '.png'])    % write the graph to the file system. 

