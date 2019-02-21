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

% Function takes as arguments the names of files, separated by spaces, to plot the EAE severity score of. More than one file may be provided. 
% No other arguments should be taken. 
%
%
% THIS SCRIPT DOES NOT WORK USING THE (NEW) EAE SEVERITY MECHANISM - DIFFERENTIATION ON THE NUMBER OF NEURONS KILLED. 
% IT IS THEREFORE CONSIDERED TEMPORARILY OBSOLETE. 

function EAESeverityScores_individualRuns(ARGS)


%
% The following variables correspond to the data held in each column of the data file. 
%
time_hours = 1;
total_CD4Th = 2;
total_CD4ThNaive = 3;
total_CD4ThPartial = 4 ;
total_CD4ThProliferating = 5;
total_CD4Th1 = 6;
total_CD4Th2 = 7;
total_CD4ThApoptotic = 8;
total_CD4Treg = 9;
total_CD4TregNaive = 10;
total_CD4TregPartial = 11;
total_CD4TregProliferating = 12;
total_CD4TregActivated = 13;
total_CD4TregApoptotic = 14;
total_CD8Treg = 15;
total_CD8TregNaive = 16;
total_CD8TregPartial = 17;
total_CD8TregProliferating = 18;
total_CD8TregActivated = 19;
total_CD8TregApoptotic = 20;
cns_APC = 21;
cns_APCImmature = 22;
cns_APCTolerogenic = 23;
cns_APCImmunogenic = 24;
cns_APCApoptotic = 25;
CLN_DC = 26;
CLN_DCImmature = 27;
CLN_DCTolerogenic = 28;
CLN_DCImmunogenic = 29;
CLN_DCApoptotic = 30;
SLO_DC = 31;
SLO_DCImmature = 32;
SLO_DCTolerogenic = 33;
SLO_DCImmunogenic = 34;
SLO_DCApoptotic = 35;
CNS_CD4Th1 = 36;
CNS_CD4Th2 = 37;
CD4Th_Specificity = 38;
CD4Th1_Specificity = 39;
CD4Th2_Specificity = 40;
cumulative_Th1Killed = 41;
CLN_DC_PolarizationType1 = 42;
CLN_DC_PolarizationType2 = 43;
cumulative_CNS_DC_PolarizationType1 = 44;
cumulative_CNS_DC_PolarizationType2 = 45;
Spleen_DC = 46;
Spleen_DCImmature = 47;
Spleen_DCTolerogenic = 48;
Spleen_DCImmunogenic = 49;
Spleen_DCApoptotic = 50;
Spleen_CD4TregTotal = 51;
Spleen_CD4TregEffector = 52;
Spleen_CD8TregTotal = 53;
Spleen_CD8TregEffector = 54 ;
Spleen_CD4Th1 = 55;
Spleen_CD4Th2 = 56;
Spleen_CD4TregProlif  = 57;
Spleen_CD8TregProlif  = 58;
Spleen_Th1Prolif  = 59;
Spleen_Th2Prolif  = 60;
CLN_CD4TregProlif  = 61;
CLN_CD8TregProlif  = 62 ;
CLN_Th1Prolif  = 63;
CLN_Th2Prolif  = 64;
CLN_CD4TregEffector  = 65;
CLN_CD8TregEffector  = 66;
CLN_Th1Effector = 67;
CLN_Th2Effector = 68;
cumulativeCD4Th1KilledCirculatory = 69;
cumulativeCD4Th1KilledCLN = 70;
cumulativeCD4Th1KilledCNS = 71; 
cumulativeCD4Th1KilledSLO = 72;
cumulativeCD4Th1KilledSpleen = 73;
CD4ThPrimedCirculatory = 74;
CD4ThPrimedCLN = 75;
CD4ThPrimedCNS = 76;
CD4ThPrimedSLO = 77;
CD4ThPrimedSpleen = 78;
CD4TregPrimedCirculatory = 79;
CD4TregPrimedCLN = 80;
CD4TregPrimedCNS = 81;
CD4TregPrimedSLO = 82;
CD4TregPrimedSpleen = 83;
CD8TregPrimedCirculatory = 84;
CD8TregPrimedCLN = 85;
CD8TregPrimedCNS = 86;
CD8TregPrimedSLO = 87;
CD8TregPrimedSpleen = 88;%simOutputData_163.txt

addpath '/n/staffstore/markread/java_workspace_compute_ss/Treg_2D/data_analysis'

Th1Thresholds = [0, 400, 600, 800, 1000, 1200]              % these are the threshold levels of Th1 cells at 1000h that we are intersted in, and that we should draw on the graphs. 

%
% Read arguments
%
files = split_str([' '], ARGS)        % split the arguments string acording to spaces. The arguments represent files names of simulation runs to plot EAE scores of. 

runs = {}
for f = 1:length(files)
  dataFile = files{f};
  underscoreLocations = find(dataFile == '_');
  dotLocations = find(dataFile == '.');
  runNum = dataFile(underscoreLocations(end) + 1 : dotLocations(end) - 1);
  runs{f} = [runNum '_'];
end


data = [];                             % this will be a two dimensional matrix, a two column table holding time and Total Th1, with another dim for the runs. Runs X Time X TotalTh1
numRows = [];                          % sores how many time sample points are represnted in the data, and is determined below. 
Xs = [];
for f = 1:length(files)               % scan through each file in turn.              
  fid = fopen(files{f});              % open the file for read, and read it (textscan below) into a temporary data structure. 
  temp = textscan(fid, '%f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f', 'CommentStyle', '#');  
  fclose(fid);

  if isempty(data)                       % if data has not yet been assigned anything, then upon reading the first file, we create the structure to store all the data. 
    numRows = length(temp{1});         % determine how many time sample points are represented in the data
    data = zeros(length(files), 2, numRows);  % Runs X Time X TotalTh1
  end

  data(f,1,:) = temp{time_hours};      % extract from temp the hours
  Xs = temp{time_hours}' / 24;
  data(f,2,:) = temp{total_CD4Th1};    % extract from temp the Th1. 
end

% In vivo, when a mouse reaches severity 5, it dies. In the simulation, this level of Th1 cells may be reached, and the mouse may recover again. We do not wish to represent this on the
% graph, so this loop will go through each file in turn and look for any point that severity 5 is reached. From that point onwards, till the end of the run, the number of Th1 cells is
% set artificially high to that the run continues to record as level 5 on the graph rather than recovering. 
for run = 1:length(files)
  for time = 1:numRows
    if data(run,2,time) >= Th1Thresholds(end)     
      data(run,2,time:numRows) = 1000000; break;
    end
  end
end

severities = zeros(length(files), numRows);    % here we will store severity for each run over time. 
              
for run = 1:length(files)
  for time = 1:numRows
    numTh1 = data(run,2,time);
    if numTh1 >= Th1Thresholds(1) && numTh1 < Th1Thresholds(2)
      severities(run,time) = 0 + (0.03 * run);
    elseif numTh1 >= Th1Thresholds(2) && numTh1 < Th1Thresholds(3)
      severities(run,time) = 1 + (0.03 * run);
    elseif numTh1 >= Th1Thresholds(3) && numTh1 < Th1Thresholds(4)
      severities(run,time) = 2 + (0.03 * run);
    elseif numTh1 >= Th1Thresholds(4) && numTh1 < Th1Thresholds(5)
      severities(run,time) = 3 + (0.03 * run);
    elseif numTh1 >= Th1Thresholds(5) && numTh1 < Th1Thresholds(6)
      severities(run,time) = 4 + (0.03 * run);
    elseif data(run,2,time) >= Th1Thresholds(6)
      severities(run,time) = 5 + (0.03 * run);
    end
  end
end


%
% This code can be commented out or adjusted to draw only a subset of the time that the simulation was run for. 
%
maxTime = 960;
Xs = Xs(1:maxTime);
severities = severities(:,1:maxTime);


%
% Plot each run in terms of its severities. 
%
styles = {'r-','g-','b-','k-','c-','m-','y-','r:','g:','b:','k:','c:','m:','y:'};
widths = [5.0, 4.5, 4.0, 3.5, 3.0, 2.5, 2.0, 1.5, 1.0, 0.5] 
cla;
hold on;
for run = 1:length(files)
%  plot(Xs, severities(run,:), styles{run}, 'LineWidth', widths(run));
  plot(Xs, severities(run,:), styles{run}, 'LineWidth', 2.0);
end
hold off;

xlabel('Time (days)');
ylabel('EAE Score');
legend('run1', 'run2', 'run3', 'run4', 'run5', 'run6');
title('The EAE severity score of simulation execution(s).');

B = [Xs(1), Xs(end), 0, 5.3];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
axis(B);                                            % replot with differen mins and maxes for axes
set(gca,'YTick',[0 1 2 3 4 5])


tag = '';
for r = 1:length(runs)
  tag = [tag runs{r}];
end

print('-dpng', '-r300', [pwd '/EAESeverityShortTerm/' 'EAESeverityPlot_' tag '.png'])    % write the graph to the file system. 

