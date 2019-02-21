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

% This matlab script will open the EAE severity scores file (that stores the EAE severity experienced by each run for each sample point)
% and will plot the average results, saving to a file as it does. At the moment both parametric and non-parametric averages are plotted.
%
% It is designed to be run from within each of the subdirectories that contain (for example)
% parameter tweeks - all contained under an experimental directory.
%
% The 'endTime' argument lets the user specify the maixmum number of days over which graphs are drawn (on the x axis). 
function drawMedianEAEScore(endTime)


fontsize = 18;
linewidth = 2.0;



directory = pwd                                            % going to derive the name of the current experiment from the local directory such that we can name graph files accordingly. 
fslashes = find(directory == '/');                         % find the location of all forward slashes in the current directory name
fileNameTag = directory(fslashes(end)+1 : end) ;            % pull out the string from the end of the last slash to the end of the directory name. 
fileNameTag(find(fileNameTag == '.')) = '_';
if ~isinf(endTime)
  fileNameTag = [num2str(endTime) '_days__' fileNameTag];
end

%---------------------------------------------
% Reading arbritray sized files into matricies seems a bit tricky in matlab, so this will find out how big the file will be. 
% the EAE scores file has the following format. rows = time sample points. First column is time of sample point, and every additional column represents a run. 

dataPrefix = 'simOutputData_';
files = dir([dataPrefix '*']);                  % the '*' is a wild wild card. Specifying 'crude_analysis...' will stop '.' and '..' appearing in the list. 
numRuns = length(files)

fid = fopen('EAESeverityScoresForRuns');
example = fscanf(fid,'%f',[numRuns + 1, Inf]);
fclose(fid);
timeSamples = size(example,2);
%---------------------------------------------
if isinf(endTime)
 endTime = timeSamples;
end


% open the EAE scores file for all runs. Read in the data. 
% this will dynamically read the file into a matrix. THe matrix will have 'numRuns+1' rows, and 'timeSamples' columns. 
fid = fopen('EAESeverityScoresForRuns');
EAEScores = fscanf(fid, '%f', [numRuns + 1,endTime]);     % dynamic scan to fill the given matrix dimensions, note the single '%f'
EAEScores = EAEScores'; % this switches the data to the more familiar structure, time and then runs along the columns, and rows being time samples. EAEScores(<rows>,<cols>)

% am going to sort the EAE scores here. Note that the first column is the TIME, so that should be ignored. We only want to sort the EAE scores. 
for t = 1:endTime
  [unused,order] = sort(EAEScores(t,2:end));     % extract the order that is required for sorting. 
  o = order + 1;                                 % add 1 to the orders, because they were the relative orders ignoring the first column, since we now consider the first column we need to add 1 to everything. 
  EAEScores(t,2:end) = EAEScores(t, o);           % reassignment, based on the correct ordering. 
end


%totals_scores = zeros(timeSamples,6);
%for time = 1:timeSamples
%  for score  =0 : 6
%    total_scores(time,score) = length( find(EAEScores(:,time)) <= score ) / numRuns
%  end 
%end

%plot((1:timeSamples),total_scores(1)


EAEMedians = [];  
EAEMeans = [];
for t = 1:endTime
  y = EAEScores(t, 2:end);
  EAEMedians(t).median = median( y );
  EAEMedians(t).Qlow = y( ceil(0.25 * length(y)) );
  EAEMedians(t).Qhigh = y( ceil(0.75 * length(y)) );      % use ceil instead of round here, since we do not want to round down to zero, that is not a valid index in an array!

  EAEMeans(t).mean = mean(y);
  EAEMeans(t).std = std(y);
  EAEMeans(t).low = EAEMeans(t).mean - EAEMeans(t).std;
  EAEMeans(t).high = EAEMeans(t).mean + EAEMeans(t).std;
end

Xs = EAEScores(:,1);

clf;
hold on;
plot(Xs, [EAEMedians(:).median], 'r-', 'LineWidth',linewidth)
plot(Xs, [EAEMedians(:).Qlow], 'b:', 'LineWidth',linewidth)
plot(Xs, [EAEMedians(:).Qhigh], 'b:', 'LineWidth',linewidth)
hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth)
ylabel('EAE Score','FontSize',fontsize)
xlabel('Time (days)','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'mean', 'IQR'});
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.

B = [Xs(1), Xs(end), 0, 5];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
axis(B);                                            % replot with differen mins and maxes for axes
set(gca,'YTick',[0 1 2 3 4 5])


print('-dpng', '-r300', ['EAEScoreMedian_-_' fileNameTag '.png'])    % write the graph to the file system. 

clf;

hold on
plot(Xs, [EAEMeans(:).mean], 'r-','LineWidth',linewidth)
plot(Xs, [EAEMeans(:).low], 'b:','LineWidth',linewidth)
plot(Xs, [EAEMeans(:).high], 'b:','LineWidth',linewidth)
hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth)
ylabel('EAE Score','FontSize',fontsize)
xlabel('Time (days)','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'mean', '+/- std dev'});
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
B = [Xs(1), Xs(end), 0, 5.3];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
axis(B);                                            % replot with differen mins and maxes for axes
set(gca,'YTick',[0 1 2 3 4 5])

print('-dpng', '-r300', ['EAEScoreMean_-_' fileNameTag '.png'])    % write the graph to the file system. 


