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


% This matlab function will search for 'multipleDataOutput.txt' file in the local directory, and will draw graphs of the median 
% data from simulation runs. Graphs will be stored on the file system.
%
% The function will however take an argument '-data', that specifies the name of a file to draw graphs from instead. This can be used to 
% draw graphs of individual simulation executions rather than the median (Which is the default). 
%
% The function takes a single string as an argument. The string will specify optional arguments followed by their value. The arguments must start with
% a '-', and all elements of the string must be separated by single spaces. For example '-syswideY 1400' would be accepted. 
%
% the '-end XXX' argument allows the user to specify the period of data that is drawn on graphs. The default is all available data. This argument is expressed
% in days. 
%
% Error bars can be drawn on some graphs, if '-error' is specified in ARGS (operand = interval of bars). 
% This can be very time consuming as the script reads every simulation output data file in the current 
% directory.
function drawSimOutputGraph(ARGS)
%ARGS = '-save 1 -error 5'

dataFile = 'multipleDataOutput.txt';            % by default (unless otherwise specified in the function arguments) the graphs will be drawn based on median data. 

path = pwd;
k = findstr('Treg_2D',path);
headDir = path(1:k(end)-1);                     % dynamically locate where teh data analysis file is located, based in searching for 'Treg_2D' in the current working dir. 
addpath(genpath([headDir '/Treg_2D/data_analysis']))
addpath(genpath([headDir '/Treg_2D/data_analysis/EAESeverity/']))



drawingIndividualRun = false;
fontsize = 18;
linewidth = 2.0;

%
% Read arguments
%
args = split_str([' '], ARGS)       % split the arguments string acording to spaces. 
savePlot = 0;                       % by default, do not save the plot. This bust be expicitly set to 1 in order to save. 
observationEnd = Inf;               % by default, draw the entire dataset. 
calculateYs = 0;                    % by default, do not write output files detailing the Y axis limits of graphs produced (this is useful for sensitivity analysis 
                                    % simulation executions, less so for SingleRun executions, for which the parent directory gets cluttered with junk. s
errorBars = 0;                % set to 1, this indicates that error bars should be drawn
errorInterval = 5;            % interval at which bars are drawn, in days. 
saveFilePrefix = '';
syswideY = [];
cumulativeY = [];
cumulativeCompY = [];
primedCompThY = [];
primedCompCD4TregY = [];
primedCompCD8TregY = [];
cumulativeNeuronsKilledY = [];
neuronsKilledY = [];
apcStatesCLNY = [];
apcStatesSpleenY = [];
apcStatesCNSY = [];
apcStatesSLOY = [];
clnAPCPolarizationsY = [];
cd4ThStatesY = [];
cd4TregStatesY = [];
cd8TregStatesY = [];
thCNSY = [];
for i = 1 : length(args)  
  if strcmp(args{i}, '-save'), savePlot = str2num(args{i+1}), end
  if strcmp(args{i}, '-end'),  observationEnd = (str2num(args{i+1})*24) + 1, end         % we add one, since hour/day zero appears in index 1.

  if strcmp(args{i}, '-calculateYs'), calculateYs = str2num(args{i+1}), end
  if strcmp(args{i}, '-data')
    dataFile = args{i+1}
    drawingIndividualRun = true;
    saveFilePrefix = '/individual_sim_graphs/'
    if exist('individual_sim_graphs') ~= 7
      mkdir('individual_sim_graphs')
    end
  end 
  if strcmp(args{i}, '-error')
    errorBars = 1
    errorInterval = str2num(args{i+1});
  end
  if strcmp(args{i}, '-syswideY'), syswideY = str2num(args{i+1}), end 
  if strcmp(args{i}, '-cumkillY'), cumulativeY = str2num(args{i+1}), end 
  if strcmp(args{i}, '-cumkillCompY'), cumulativeCompY = str2num(args{i+1}), end 
  if strcmp(args{i}, '-primedCompThY'), primedCompThY = str2num(args{i+1}), end 
  if strcmp(args{i}, '-primedCompCD4TregY'), primedCompCD4TregY = str2num(args{i+1}), end 
  if strcmp(args{i}, '-primedCompCD8TregY'), primedCompCD8TregY = str2num(args{i+1}), end 
  if strcmp(args{i}, '-cumNeuronsKilledY'), cumulativeNeuronsKilledY = str2num(args{i+1}), end
  if strcmp(args{i}, '-neuronsKilledY'), neuronsKilledY = str2num(args{i+1}), end
  if strcmp(args{i}, '-apcStatesCLNY'), apcStatesCLNY = str2num(args{i+1}), end
  if strcmp(args{i}, '-apcStatesSpleenY'), apcStatesSpleenY = str2num(args{i+1}), end
  if strcmp(args{i}, '-apcStatesCNSY'), apcStatesSpleenY = str2num(args{i+1}), end
  if strcmp(args{i}, '-apcStatesSLOY'), apcStatesSpleenY = str2num(args{i+1}), end
  if strcmp(args{i}, '-clnAPCPolarizationsY'), clnAPCPolarizationsY = str2num(args{i+1}), end
  if strcmp(args{i}, '-cd4ThStatesY'), cd4ThStatesY = str2num(args{i+1}), end
  if strcmp(args{i}, '-cd4TregStatesY'), cd4TregStatesY = str2num(args{i+1}), end
  if strcmp(args{i}, '-cd8TregStatesY'), cd8TregStatesY = str2num(args{i+1}), end
  if strcmp(args{i}, '-thCNSY'), thCNSY = str2num(args{i+1}), end
end



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
CD8TregPrimedSpleen = 88;
NeuronsKilledCumulative = 89;

%-------------------------------------------------
% open this example and find out how long the table is (how many rows, representing samples in time), and how many columns it has. 
firstLineIsComment = false;                     % not all files to be read in start with a comment, but some do. This variable helps handle both cases. 
fid = fopen(dataFile,'r');
firstLine = fgetl(fid);                         % read the first line, which in the case of single run data lines ALWAYS includes a comment line at the top. 
numCols = length(find(firstLine == ' '))      % extact the number of columns from the number of spaces in the comment line
if firstLine(1) == '#'
  firstLineIsComment = true;
end
fclose(fid);

fid = fopen(dataFile,'r');
if firstLineIsComment
  firstLine = fgetl(fid);
end
data = fscanf(fid, '%f ', [numCols,Inf]);  % read in the data file.
numRows = length(data)                % equivalent to the number of time samples. 
fclose(fid);
%-------------------------------------------------



if savePlot                                                  % fileNameTag, derived here, is also the same for all graphs. 
  directory = pwd                                            % going to derive the name of the current experiment from the local directory such that we can name graph files accordingly. 
  fslashes = find(directory == '/');                         % find the location of all forward slashes in the current directory name
  fileNameTag = directory(fslashes(end)+1 : end)             % pull out the string from the end of the last slash to the end of the directory name. 
  fileNameTag(find(fileNameTag == '.')) = '_';
  if ~isinf(observationEnd)
    fileNameTag = [num2str((observationEnd-1)/24) '_days__' fileNameTag];
  end
  fileNameStart = '';
  
  if drawingIndividualRun == true
    underscoreLocations = find(dataFile == '_');
    dotLocations = find(dataFile == '.');
    runNum = dataFile(underscoreLocations(end) + 1 : dotLocations(end) - 1);
    fileNameTag = [fileNameTag '_' runNum];
    fileNameStart = 'Run_';
  end
end

if isinf(observationEnd)
  observationEnd = numRows;
end
Xs = data(1,1:observationEnd) / 24;                                          % divide by 24 to get days. This is the same for all graphs plotted against time. 


%====================================================================
% pre-processing required for drawing of error bars on graphs
%====================================================================
if errorBars
  % read in all the available simulation data files. 
  dataPrefix = 'simOutputData_';
  files = dir([dataPrefix '*']); 
  numSamples = length(files);
  allData = zeros(numSamples,numCols,numRows);
  fid = fopen('simOutputData_0.txt');
  l = fgetl(fid); % read the first line, identify if it is a comment 
  firstLineComment = false;
  if l(1) == '#'
    firstLineComment = true;
  end
  for f = 1:length(files)
    fid = fopen(files(f).name,'r');
    if firstLineComment
      l = fgetl(fid);                         % throw away the first line, it is a comment. 
    end
    filedata = fscanf(fid,'%f ',[numCols,Inf]);
    filedata(1:end,1) = data(1:end,1) / 24;	      % convert hours into days. 
    fclose(fid);
    allData(f,:,:) = filedata;
  end
end
 

%====================================================================
% Plot system wide graph. 
%====================================================================
Ys = zeros(4, length(data(1,1:observationEnd)));
Ys(1,:) = data(total_CD4Th1,1:observationEnd);
Ys(2,:) = data(total_CD4Th2,1:observationEnd);
Ys(3,:) = data(total_CD4TregActivated,1:observationEnd);
Ys(4,:) = data(total_CD8TregActivated,1:observationEnd);
clf;
hold on;
leg1 = plot(Xs, Ys(1,:), 'r-', 'LineWidth', linewidth);
leg2 = plot(Xs, Ys(2,:), 'm-', 'LineWidth', linewidth);
leg3 = plot(Xs, Ys(3,:), 'b-', 'LineWidth', linewidth);
leg4 = plot(Xs, Ys(4,:), 'g-', 'LineWidth', linewidth);
if errorBars
  xlimits = xlim;   % error bar plotting interfers with axis limits, store now to reset after. 
  errorIntervalStep = errorInterval / 4;  % 4 sets of error bars to draw, this is step between them

  errorXs = (0*errorIntervalStep:errorInterval:Xs(end));
  [errorYs, errorLs, errorUs] = calculate_error_intervals(data,allData,total_CD4Th2,errorXs);
  errorbar(errorXs, errorYs, errorLs,errorUs,'.m','LineWidth',0.5*linewidth);

  errorXs = (1*errorIntervalStep:errorInterval:Xs(end));
  [errorYs, errorLs, errorUs] = calculate_error_intervals(data,allData,total_CD4Th1,errorXs);
  errorbar(errorXs, errorYs, errorLs,errorUs,'.r','LineWidth',0.5*linewidth);

  errorXs = (2*errorIntervalStep:errorInterval:Xs(end));
  [errorYs, errorLs, errorUs] = calculate_error_intervals(data,allData,total_CD4TregActivated,errorXs);
  errorbar(errorXs, errorYs, errorLs,errorUs,'.b','LineWidth',0.5*linewidth);

  errorXs = (3*errorIntervalStep:errorInterval:Xs(end));
  [errorYs, errorLs, errorUs] = calculate_error_intervals(data,allData,total_CD8TregActivated,errorXs);
  errorbar(errorXs, errorYs, errorLs,errorUs,'.g','LineWidth',0.5*linewidth);

  xlim(xlimits);    % reset axis limits
end

hold off
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend([leg1,leg2,leg3,leg4],'CD4Th1', 'CD4Th2', 'CD4Treg', 'CD8Treg');
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth); % sets the line width of the lines in the legend, ie, the lines on the plot. 

%title('Total number of effector T cells throughout the system')

if ~isempty(syswideY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, syswideY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                            % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_syswide', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%10.2f\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'SystemWide__' fileNameTag '.png'])    % write the graph to the file system. 
end


%====================================================================
% Plot graph of cumulative count of CD4Th1 cells killed by CD8Tregs. 
%====================================================================
clf;
plot(Xs, data(cumulative_Th1Killed,1:observationEnd), 'b-', 'LineWidth', linewidth)
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
%title('Cumulative count of Th1 cells apoptosised by CD8Tregs')

if ~isempty(cumulativeY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, cumulativeY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                            % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_cumulativeKilled', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%10.2f\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'Cumulative_Th1_Killed__' fileNameTag '.png'])    % write the graph to the file system. 
end


%====================================================================
% Plot graph of where Th1 killing by CD8Treg cells is taking place, in terms of bodily compartments. 
%====================================================================

clf;
hold on;
plot(Xs, data(cumulativeCD4Th1KilledCirculatory,1:observationEnd)', 'r-', 'LineWidth',linewidth)
plot(Xs, data(cumulativeCD4Th1KilledCLN,1:observationEnd)', 'g-', 'LineWidth',linewidth)
plot(Xs, data(cumulativeCD4Th1KilledSLO,1:observationEnd)', 'c-', 'LineWidth',linewidth)
plot(Xs, data(cumulativeCD4Th1KilledSpleen,1:observationEnd)', 'b-', 'LineWidth',linewidth)
hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 

xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'Circulatory', 'CLN', 'SLO', 'Spleen'},'Location','NorthWest');
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
%title('Cumulative count of Th1 cells apoptosised by CD8Tregs in each compartment')

if ~isempty(cumulativeCompY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, cumulativeCompY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                            % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_cumulativeCompartmentKilled', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%10.2f\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' fileNameStart 'Cumulative_Th1_Killed_Compartments__' fileNameTag '.png'])    % write the graph to the file system. 
end

%====================================================================
% Plot graph of where Priming of THelper cells is taking place. 
%====================================================================

clf;
hold on;
plot(Xs, data(CD4ThPrimedSLO,1:observationEnd)', 'c-',  'LineWidth',linewidth)
plot(Xs, data(CD4ThPrimedCLN,1:observationEnd)', 'g-',  'LineWidth',linewidth)
plot(Xs, data(CD4ThPrimedSpleen,1:observationEnd)', 'b-', 'LineWidth',linewidth)
hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'SLO', 'CLN', 'Spleen'}, 'Location','NorthWest');
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
%title('Cumulative count of which compartment CD4Th cells are being primed in.')

if ~isempty(primedCompThY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, primedCompThY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                            % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_primedCompTh', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%4.2G\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'Primed_Compartments_CD4Th__' fileNameTag '.png'])    % write the graph to the file system. 
end


%====================================================================
% Plot graph of where Priming of CD4Treg cells is taking place. 
%====================================================================

clf;
hold on;
plot(Xs, data(CD4TregPrimedSLO,1:observationEnd)', 'c-',  'LineWidth',linewidth)
plot(Xs, data(CD4TregPrimedCLN,1:observationEnd)', 'g-',  'LineWidth',linewidth)
plot(Xs, data(CD4TregPrimedSpleen,1:observationEnd)', 'b-', 'LineWidth',linewidth)
if errorBars
  xlimits = xlim;   % error bar plotting interfers with axis limits, store now to reset after. 
  errorIntervalStep = errorInterval / 3;  % 3 sets of error bars to draw, this is step between them

  errorXs = (0*errorIntervalStep:errorInterval:Xs(end));
  [errorYs, errorLs, errorUs] = calculate_error_intervals(data,allData,CD4TregPrimedSLO,errorXs);
  errorbar(errorXs, errorYs, errorLs,errorUs,'.c','LineWidth',0.5*linewidth);

  errorXs = (1*errorIntervalStep:errorInterval:Xs(end));
  [errorYs, errorLs, errorUs] = calculate_error_intervals(data,allData,CD4TregPrimedCLN,errorXs);
  errorbar(errorXs, errorYs, errorLs,errorUs,'.g','LineWidth',0.5*linewidth);

  errorXs = (2*errorIntervalStep:errorInterval:Xs(end));
  [errorYs, errorLs, errorUs] = calculate_error_intervals(data,allData,CD4TregPrimedSpleen,errorXs);
  errorbar(errorXs, errorYs, errorLs,errorUs,'.b','LineWidth',0.5*linewidth);

  xlim(xlimits);    % reset axis limits
end
hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'SLO', 'CLN', 'Spleen'},'Location','NorthWest');
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
%title('Cumulative count of which compartment CD4Treg cells are being primed in.')

if ~isempty(primedCompCD4TregY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, primedCompCD4TregY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                            % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_primedCompCD4Treg', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%10.2f\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'Primed_Compartments_CD4Treg__' fileNameTag '.png'])    % write the graph to the file system. 
end


%====================================================================
% Plot graph of where Priming of CD8Treg cells is taking place. 
%====================================================================

clf;
hold on;
plot(Xs, data(CD8TregPrimedSLO,1:observationEnd)', 'c-',  'LineWidth',linewidth)
plot(Xs, data(CD8TregPrimedCLN,1:observationEnd)', 'g-',  'LineWidth',linewidth)
plot(Xs, data(CD8TregPrimedSpleen,1:observationEnd)', 'b-', 'LineWidth',linewidth)
if errorBars
  xlimits = xlim;   % error bar plotting interfers with axis limits, store now to reset after. 
  errorIntervalStep = errorInterval / 3;  % 3 sets of error bars to draw, this is step between them

  errorXs = (0*errorIntervalStep:errorInterval:Xs(end));
  [errorYs, errorLs, errorUs] = calculate_error_intervals(data,allData,CD8TregPrimedSLO,errorXs);
  errorbar(errorXs, errorYs, errorLs,errorUs,'.c','LineWidth',0.5*linewidth);

  errorXs = (1*errorIntervalStep:errorInterval:Xs(end));
  [errorYs, errorLs, errorUs] = calculate_error_intervals(data,allData,CD8TregPrimedCLN,errorXs);
  errorbar(errorXs, errorYs, errorLs,errorUs,'.g','LineWidth',0.5*linewidth);

  errorXs = (2*errorIntervalStep:errorInterval:Xs(end));
  [errorYs, errorLs, errorUs] = calculate_error_intervals(data,allData,CD8TregPrimedSpleen,errorXs);
  errorbar(errorXs, errorYs, errorLs,errorUs,'.b','LineWidth',0.5*linewidth);

  xlim(xlimits);    % reset axis limits
end
hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'SLO', 'CLN', 'Spleen'},'Location','NorthWest');
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
%title('Cumulative count of which compartment CD8Treg cells are being primed in.')

if ~isempty(primedCompCD8TregY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, primedCompCD8TregY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                            % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_primedCompCD8Treg', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%4.2G\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'Primed_Compartments_CD8Treg__' fileNameTag '.png'])    % write the graph to the file system. 
end




%====================================================================
% Plot graph of cumulative count of neurons apoptosed.
%====================================================================

clf;
hold on;
plot(Xs, data(NeuronsKilledCumulative,1:observationEnd)', 'r-',  'LineWidth',linewidth)

hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)


if ~isempty(cumulativeNeuronsKilledY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, cumulativeNeuronsKilledY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                            % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height.
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_cumulativeNeuronsKilled', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%10.2f\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'Cumulative_Neurons_Killed__' fileNameTag '.png'])    % write the graph to the file system. 
end



%====================================================================
% Plot graph of neurons apoptosed per hour.
%====================================================================

clf;
neuronsKilledPerHour = differentiate_neurons_killed(data(NeuronsKilledCumulative,:), (1:numRows), 1);
window = 157;
smoothedNeuronsKilled = smooth(neuronsKilledPerHour,window);         % smooth this data
hold on;
plot(Xs, neuronsKilledPerHour(1:observationEnd)', 'r-', 'LineWidth',linewidth)
plot(Xs, smoothedNeuronsKilled(1:observationEnd)', 'b-', 'LineWidth',linewidth)



hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'raw', 'smoothed'});
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.

if ~isempty(neuronsKilledY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, neuronsKilledY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                                       % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_neuronsKilled', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%10.2f\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'Neurons_Killed_per_Hour__' fileNameTag '.png'])    % write the graph to the file system. 
end



%====================================================================
% Plot graph of the different states of APCs in the CLN compartment. (ie, immunogenic, tolerogenic, naive, etc).
%====================================================================

clf;
hold on;


plot(Xs, data(CLN_DC,1:observationEnd)', 'm-',  'LineWidth',linewidth)
plot(Xs, data(CLN_DCImmature,1:observationEnd)', 'g-',  'LineWidth',linewidth)
plot(Xs, data(CLN_DCTolerogenic,1:observationEnd)', 'b-', 'LineWidth',linewidth)
plot(Xs, data(CLN_DCImmunogenic,1:observationEnd)', 'r-', 'LineWidth',linewidth)
hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'Total', 'Immature', 'Tolerogenic', 'Immunogenic'});
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
%title('States of APCs in the CLN compartment')

if ~isempty(apcStatesCLNY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, apcStatesCLNY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                                    % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_apcStatesCLN', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%4.2G\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'APC_States_CLN__' fileNameTag '.png'])    % write the graph to the file system. 
end



%====================================================================
% Plot graph of the different states of APCs in the Spleen compartment. (ie, immunogenic, tolerogenic, naive, etc).
%====================================================================

clf;
hold on;
set(gca,'box','on');

plot(Xs, data(Spleen_DC,1:observationEnd)', 'm-',  'LineWidth',linewidth)
plot(Xs, data(Spleen_DCImmature,1:observationEnd)', 'g-',  'LineWidth',linewidth)
plot(Xs, data(Spleen_DCTolerogenic,1:observationEnd)', 'b-', 'LineWidth',linewidth)
plot(Xs, data(Spleen_DCImmunogenic,1:observationEnd)', 'r-', 'LineWidth',linewidth)
hold off;
set(gca,'FontSize',fontsize)

set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'Total', 'Immature', 'Tolerogenic', 'Immunogenic'});
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
%title('States of APCs in the CLN compartment')

if ~isempty(apcStatesSpleenY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, apcStatesSpleenY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                                    % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_apcStatesSpleen', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%4.2G\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'APC_States_Spleen__' fileNameTag '.png'])    % write the graph to the file system. 
end



%====================================================================
% Plot graph of the different states of APCs in the CNS compartment. (ie, immunogenic, tolerogenic, naive, etc).
%====================================================================

clf;
hold on;


plot(Xs, data(cns_APC,1:observationEnd)', 'm-',  'LineWidth',linewidth)
plot(Xs, data(cns_APCImmature,1:observationEnd)', 'g-',  'LineWidth',linewidth)
plot(Xs, data(cns_APCTolerogenic,1:observationEnd)', 'b-', 'LineWidth',linewidth)
plot(Xs, data(cns_APCImmunogenic,1:observationEnd)', 'r-', 'LineWidth',linewidth)
hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'Total', 'Immature', 'Tolerogenic', 'Immunogenic'});
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
%title('States of APCs in the CLN compartment')

if ~isempty(apcStatesCNSY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, apcStatesCNSY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                                    % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_apcStatesCNS', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%4.2G\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'APC_States_CNS__' fileNameTag '.png'])    % write the graph to the file system. 
end



%====================================================================
% Plot graph of the different states of APCs in the SLO compartment. (ie, immunogenic, tolerogenic, naive, etc).
%====================================================================

clf;
hold on;


plot(Xs, data(SLO_DC,1:observationEnd)', 'm-',  'LineWidth',linewidth)
plot(Xs, data(SLO_DCImmature,1:observationEnd)', 'g-',  'LineWidth',linewidth)
plot(Xs, data(SLO_DCTolerogenic,1:observationEnd)', 'b-', 'LineWidth',linewidth)
plot(Xs, data(SLO_DCImmunogenic,1:observationEnd)', 'r-', 'LineWidth',linewidth)
hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'Total', 'Immature', 'Tolerogenic', 'Immunogenic'});
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
%title('States of APCs in the CLN compartment')

if ~isempty(apcStatesSLOY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, apcStatesSLOY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                                    % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_apcStatesSLO', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%4.2G\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'APC_States_SLO__' fileNameTag '.png'])    % write the graph to the file system. 
end




%====================================================================
% Plot graph of Type1 or Type2 DCs in the CLN over time. 
%====================================================================

clf;
hold on;


plot(Xs, data(CLN_DC_PolarizationType1,1:observationEnd)', 'r-',  'LineWidth',linewidth)
plot(Xs, data(CLN_DC_PolarizationType2,1:observationEnd)', 'g-',  'LineWidth',linewidth)
hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'Type 1', 'Type 2'});
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
%title('States of APCs in the CLN compartment')

if ~isempty(clnAPCPolarizationsY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, clnAPCPolarizationsY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                                    % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_clnAPCPolarizations', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%4.2G\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'CLN_APC_Polarizations__' fileNameTag '.png'])    % write the graph to the file system. 
end



%====================================================================
% Plot graph of CD4Th states of activation.
%====================================================================

clf;
hold on;

plot(Xs, data(total_CD4ThNaive,1:observationEnd)', 'g-',  'LineWidth',linewidth)
plot(Xs, data(total_CD4ThPartial,1:observationEnd)', 'c-',  'LineWidth',linewidth)
plot(Xs, data(total_CD4ThProliferating,1:observationEnd)', 'b-',  'LineWidth',linewidth)
plot(Xs, data(total_CD4Th1,1:observationEnd)', 'r-',  'LineWidth',linewidth)
plot(Xs, data(total_CD4Th2,1:observationEnd)', 'm-',  'LineWidth',linewidth)
plot(Xs, data(total_CD4ThApoptotic,1:observationEnd)', 'k-',  'LineWidth',linewidth)
hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'Naive', 'Partial', 'Proliferating', 'Th1', 'Th2', 'Apoptotic'});
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
%title('States of APCs in the CLN compartment')

if ~isempty(cd4ThStatesY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, cd4ThStatesY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                                    % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_cd4ThStates', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%4.2G\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'CD4Th_States__' fileNameTag '.png'])    % write the graph to the file system. 
end



%====================================================================
% Plot graph of CD4Treg states of activation.
%====================================================================

clf;
hold on;

plot(Xs, data(total_CD4TregNaive,1:observationEnd)', 'g-',  'LineWidth',linewidth)
plot(Xs, data(total_CD4TregPartial,1:observationEnd)', 'c-',  'LineWidth',linewidth)
plot(Xs, data(total_CD4TregProliferating,1:observationEnd)', 'b-',  'LineWidth',linewidth)
plot(Xs, data(total_CD4TregActivated,1:observationEnd)', 'r-',  'LineWidth',linewidth)

hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'Naive', 'Partial', 'Proliferating', 'Effector'});
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
%title('States of APCs in the CLN compartment')

if ~isempty(cd4TregStatesY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, cd4TregStatesY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                                    % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_cd4TregStates', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%4.2G\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'CD4Treg_States__' fileNameTag '.png'])    % write the graph to the file system. 
end




%====================================================================
% Plot graph of CD8Treg states of activation.
%====================================================================

clf;
hold on;

plot(Xs, data(total_CD8TregNaive,1:observationEnd)', 'g-',  'LineWidth',linewidth)
plot(Xs, data(total_CD8TregPartial,1:observationEnd)', 'c-',  'LineWidth',linewidth)
plot(Xs, data(total_CD8TregProliferating,1:observationEnd)', 'b-',  'LineWidth',linewidth)
plot(Xs, data(total_CD8TregActivated,1:observationEnd)', 'r-',  'LineWidth',linewidth)

hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'Naive', 'Partial', 'Proliferating', 'Effector'});
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
%title('States of APCs in the CLN compartment')

if ~isempty(cd8TregStatesY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, cd8TregStatesY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                                    % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_cd8TregStates', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%4.2G\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'CD8Treg_States__' fileNameTag '.png'])    % write the graph to the file system. 
end



%====================================================================
% Plot graph of CD4Th1 and CD4Th2 numbers in the CNS
%====================================================================

clf;
hold on;

plot(Xs, data(CNS_CD4Th1,1:observationEnd)', 'r-',  'LineWidth',linewidth)
plot(Xs, data(CNS_CD4Th2,1:observationEnd)', 'm-',  'LineWidth',linewidth)

hold off;
set(gca,'FontSize',fontsize)
set(gca,'box','on');
set(gca,'LineWidth',linewidth);                           % draw a thicker box around the plot. 
xlabel('Time (days)','FontSize',fontsize)
ylabel('Cells','FontSize',fontsize)
[legh,objh,outh,outm] = legend({'Th1', 'Th2'});
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
%title('States of APCs in the CLN compartment')

if ~isempty(thCNSY)                                      % if a value for the top of the y axis was specified, then set the axes accordingly. 
  B = [Xs(1), Xs(end), 0.0, thCNSY];                     % axis ranges, [xmin, xmax, ymin, ymax]. 
  axis(B);                                                    % replot with differen mins and maxes for axes
end

% write the upper limits of the various graphs drawn by this script to the a file in the parent directory. This is used
% for a second pass of graph drawing to ensure that all graphs of a particular belonging to this experiment are the same height. 
if calculateYs
  upperY = get(gca, 'YLim')                                  % get the limit for the y axis, returned as [lowerLimit, upperLimit].
  fid = fopen('../simoutputGraphAxesLimits_thCNS', 'a');    % open a file to which the upper limit is to be written
  fprintf(fid, '%4.2G\n', upperY(end));                      % write the value. This format can be read again by matlab. 
  fclose(fid);
end

if savePlot
  print('-dpng', '-r300', [pwd '/' saveFilePrefix fileNameStart 'Th_cells_CNS__' fileNameTag '.png'])    % write the graph to the file system. 
end



