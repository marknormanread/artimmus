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

% Matlab script that plots a graph depicting the proportion of simulation executions that experience at least each degree of EAE severity, over time.
% The script requires that the 'EAESeverityScoresForRuns' file be generated beforehand. 
%
%
%
% The 'endTime' argument lets the user specify the maixmum number of days over which graphs are drawn (on the x axis). 
function EAESeverityDistributions(endTime)

path = pwd;
k = findstr('Treg_2D',path);
headDir = path(1:k(end)-1);                     % dynamically locate where teh data analysis file is located, based in searching for 'Treg_2D' in the current working dir. 
addpath(genpath([headDir '/Treg_2D/data_analysis/EAESeverity/']))

LineWidth = 2.0;
FontSize = 18;

directory = pwd                                            % going to derive the name of the current experiment from the local directory such that we can name graph files accordingly. 
fslashes = find(directory == '/');                         % find the location of all forward slashes in the current directory name
fileNameTag = directory(fslashes(end)+1 : end) ;            % pull out the string from the end of the last slash to the end of the directory name. 
fileNameTag(find(fileNameTag == '.')) = '_';
if ~isinf(endTime)
  fileNameTag = [num2str(endTime) '_days__' fileNameTag];
end

%-------------------------------------------------
% open this example and find out how long the table is (how many rows, representing samples in time), and how many columns it has. 
fid = fopen('EAESeverityScoresForRuns','r');
  l = fgetl(fid);                                 % read the first line, which in the case of single run data lines ALWAYS includes a comment line at the top. 
  numCols = length(find(l == ' '))                % extact the number of columns from the number of spaces in the comment line
  numRuns = numCols - 1;                          % the first column is the time in days, the rest are runs. 
fclose(fid);

fid = fopen('EAESeverityScoresForRuns','r');
EAEScores = fscanf(fid, '%f ', [numCols,endTime]);  % read in the data file.
numRows = length(EAEScores(1,:))                  % equivalent to the number of time samples. 
fclose(fid);
%-------------------------------------------------

scores = [0,1,2,3,4,5];                           % EAE scores that exist. 

proportionsScoresCumulative = zeros(numRows,length(scores));  % this will hold the proportion of simulation runs that experience *at least* each EAE severity over time. (<time>,<score>). 
proportionsScores = zeros(numRows,length(scores));            % this will hold the proportion of simulation runs that experience each EAE severity over tme (<time>,<score>).


% this works by cycling through each time sample, and each score in turn. It finds the indexes of all simulation runs that are at least the current score. It counts
% how many of these there are, and calculates the proportion of the total number of runs. 
for time = 1:numRows  
  for score_i = 1:length(scores)
    score = scores(score_i);
    proportionsScoresCumulative(time,score_i) = length(find(EAEScores(2:end,time) >= score)) / numRuns;
    proportionsScores(time,score_i) = length(find(EAEScores(2:end,time) == score)) / numRuns;
  end
end

%-------------------------------------------------
% calculate the proportion of runs that reach a maximum of each score.
maximumScores = [];
proportionMaximumScores = zeros(6,1);             % this will store the proportion of runs reaching each maximum score. First index is score zero. 
for run = 2:numCols
  maximumScores(end+1) = max(EAEScores(run,:));
end
for score = 0:5
  proportionMaximumScores(score+1) = (length(find(maximumScores == score)) * 100) / numRuns;
end
%-------------------------------------------------

%-------------------------------------------------
% write the proportion of runs hitting each maximum score during EAE progression to a file
fid = fopen('Maximum_Scores_As_Proportions','w');   
  for score = 5:-1:0
    fprintf(1,'score = %u, proportion = %4.1f\n', score, proportionMaximumScores(score+1));    % print to terminal
    fprintf(fid,'score = %u, proportion = %4.1f\n', score, proportionMaximumScores(score+1));  % print to file. 
  end
fclose(fid);
%-------------------------------------------------

Xs = EAEScores(1,:);
clf;
hold on;
%plot(Xs,proportionsScoresCumulative(:,1),'g-','LineWidth',LineWidth);
plot(Xs,proportionsScoresCumulative(:,2),'c-','LineWidth',LineWidth);
plot(Xs,proportionsScoresCumulative(:,3),'b-','LineWidth',LineWidth);
plot(Xs,proportionsScoresCumulative(:,4),'m-','LineWidth',LineWidth);
plot(Xs,proportionsScoresCumulative(:,5),'r-','LineWidth',LineWidth);
plot(Xs,proportionsScoresCumulative(:,6),'k-','LineWidth',LineWidth);
hold off;
B = axis;
B(3) = 0.0;
B(4) = 1.0;
axis(B);
set(gca,'box','on');
set(gca,'LineWidth',LineWidth);
set(gca,'FontSize',FontSize);
xlabel('Time (days)','FontSize',FontSize);
ylabel('Proportion of simulation runs','FontSize',FontSize);
[legh,objh,outh,outm] = legend({'1', '2', '3', '4', '5'});  
set(legh,'LineWidth',0.5*LineWidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',LineWidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.  
%title('The proportion of simulation runs experiencing at least a particular degree of EAE over time')
print('-dpng', '-r300', ['EAESeverityDistributions-cumulative_-_' fileNameTag '.png'])    % write the graph to the file system. 

clf;
hold on;
plot(Xs,proportionsScores(:,1),'g-','LineWidth',LineWidth);
plot(Xs,proportionsScores(:,2),'c-','LineWidth',LineWidth);
plot(Xs,proportionsScores(:,3),'b-','LineWidth',LineWidth);
plot(Xs,proportionsScores(:,4),'m-','LineWidth',LineWidth);
plot(Xs,proportionsScores(:,5),'r-','LineWidth',LineWidth);
plot(Xs,proportionsScores(:,6),'k-','LineWidth',LineWidth);
hold off;
B = axis;
B(3) = 0.0;                                               % set the Y axis limits
B(4) = 1.0;
axis(B);
set(gca,'box','on');
set(gca,'LineWidth',LineWidth);
set(gca,'FontSize',FontSize);
xlabel('Time (days)','FontSize',FontSize);
ylabel('Proportion of simulation runs','FontSize',FontSize);
[legh,objh,outh,outm] = legend({'0', '1', '2', '3', '4', '5'});     
set(legh,'LineWidth',0.5*LineWidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',LineWidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.  
%title('The proportion of simulation runs experiencing each degree of EAE over time')
print('-dpng', '-r300', ['EAESeverityDistributions-individual_-_' fileNameTag '.png'])    % write the graph to the file system. 

