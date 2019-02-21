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

% Matlab 'M' file that will perform the robustness sensitivity analysis. Currently assumes that the current working directory is 'robustness_sensitivity_analysis'
%
% this command will add the data_analysis directory, and all its subdirectories (at time of writing, this includes xml) to the matlab search path. saves copying files around before use. 
%
% The script is a function, such that command line arguments may be provided. These should take the form 
% "robustnessAnalysis('-units probability')"

function robustnessAnalysis(ARGS)
%ARGS = '-xaxis Efficacy (percent)';
drawGraphs = true;

FontSize = 18;
LineWidth = 2.0;
MarkerSize = 12;

path = pwd;
k = findstr('Treg_2D',path);
headDir = path(1:k(end)-1);                     % dynamically locate where teh data analysis file is located, based in searching for 'Treg_2D' in the current working dir. 
addpath(genpath([headDir '/Treg_2D/data_analysis/matlab_helper_functions']))
addpath(genpath([headDir '/Treg_2D/data_analysis/robustnessAnalysis']))
addpath(genpath([headDir '/Treg_2D/data_analysis/stats']))
addpath(genpath([headDir '/Treg_2D/data_analysis/matlab_xml_toolbox']));

%
% Read arguments
%
args = split_str([' '], ARGS)        % split the arguments string acording to spaces. 

paramUnitsProvided = false;
paramUnits = '';
drawTemporal = true;
xAxisProvided = false;
xAxisLabel = '';
yAxisProvided = false;
yAxisLabel = '';
for i = 1 : length(args)             % go through each argument in turn
  if strcmp(args{i}, '-units')       % permits the provision of units for parameter that is subject to the current analysis. 
    paramUnits = args{i+1} 
    paramUnitsProvided = true        % this stops matlab attempting to automatically derive the units from the metadata parameters xml file. 
  end
  % THIS IS NOT CURRENTLY IMPLEMENTED.
  if strcmp(args{i}, '-notemporal')   % convenient way to switch off temporal (max time) responses on the A test graph. 
    drawTemporal = false;
  end
  if strcmp(args{i}, '-xaxis')       % this must be supplied last, since it allows for spaces in labels. 
    xAxisLabel = '';
    for j = i+1 : length(args)
      xAxisLabel = [xAxisLabel ' ' args{j}];
    end
    xAxisProvided = true;    
    paramUnitsProvided = true;       % presume that user has supplied units in the axis label. 
  end
  if strcmp(args{i}, '-yaxis')
    yAxisLabel = args{i+1};
    yAxisProvided = true;
  end
end
paramUnits

%---------------------------
% calculate the name of the current parameter. 
outputPostfix = pwd;                                % output postfix will appear on the end of the graphs generated in this script. 
temp = find( pwd == '/' );                          % find locations of all the forward slashes in the pwd.  
outputPostfix = outputPostfix( temp(end-1)+1 : temp(end)-1 );     % assign outputPostfix to everything following the last slash             
if ~isempty(strfind(outputPostfix, 'sensAnal_-_')); % if outputPostfix contains the specified string, then remove it. 
  outputPostfix = outputPostfix(length('sensAnal_-_')+1:end);
end
%---------------------------


dataPrefix = 'robustness_analysis_response_data';    % the prefix for files that contain response data
files = dir([dataPrefix '*']);                  % the '*' is a wild wild card. Specifying 'robustness_analysis...' will stop '.' and '..' appearing in the list. 
defaultFile = [];                               % we will store the default file in this variable.
data = [];                                      % where we will eventually store all the data. data[n].paramVal will give the parameter value. data[n].data will give the associated data. 
Responses = {'CD4Th1 Max', 'CD4Th1 Max Time', 'CD4Th2 Max', 'CD4Th2 Max Time', 'CD4Treg Max', 'CD4Treg Max Time', 'CD8Treg Max', 'CD8Treg Max Time', 'CD4Th1 at 40d',' Max EAE', 'EAE at 40d'};
%                  Th1     Th1MT    Th2      Th2MT   4Treg   4TregMT 8Treg    8TregMT Th1-40   M-EAE           EAE-40
ResponseLabels = {'Cells', 'Days', 'Cells', 'Days', 'Cells', 'Days', 'Cells', 'Days', 'Cells', 'EAE severity', 'EAE severity'};

aTestBoundsMin = 0.29; 
aTestBoundsMax = 0.71;

outputPrefix = [];                                 % will store the name of the parameter (path to it in sensitivity_analysis.xml) in here, so that subsequent output files can be associated with the name. 

pathToMetaData = [headDir '/Treg_2D/parameters_-_metaData.xml']  % this file contains meta data about the parameters being analysed, including their units. 
paramsMetaData = xml_load(pathToMetaData);         % open the xml file. 

% extract from the above meta data xml file the current parameter's units. 
if paramUnitsProvided == false                       % if the units have been provided at the command line, then we do not need to do this. 
  directory = pwd;                                   % get the current working directory, we will find the current parameter's units from this file. 
  slashLocations = find(directory == '/');           % returns an array of where the '/' locations are 
  directory = directory(slashLocations(end-1)+1 : slashLocations(end)-1);   % this will pull out only the "sens_Anal_-_....._Reg" part of the directory. 
  tags = split_str(['_'], directory);                % split the directory into separate strings based on underscores
%  tags(end) = [];                                    % delete 'Reg' (or 'EAE') from end. 
  tags([1,2]) = [];                                  % delete the first two items, which will be 'sensAnal' and '-' (this latter one because of '_-_').  
  command = 'paramUnits = paramsMetaData.';          % start to build a command to dynamically extract the parameter's units from the xml file. 
  for i = 1 : length(tags)                           % for each tag, add to the command. 
    command = [command tags{i} '.'];
  end
  command = [command 'units']                        % the final tag we need is 'units', this will contain the units we are interested in. 
  eval(command)                                      % this executes the command. The resulting variable 'paramUnits' will contain the units string. 
end

% remove the default file from the list, and store that separately. This assumes there is only one such file!
for p = 1:length(files)                         % arrays in matlab run from 1..size, not from zero.
  k = findstr(files(p).name, 'default');        % attempt to find the string 'default' in the file name. 
  if isempty(k) == 0                            % if the above succeeded, then 'k' will contain the index of where 'default' starts in the file name. 
    defaultFile = files(p);                     % save the default file.
    files(p) = [];                              % remove the default item from the 'files' array.
    break;
  end
end


% fetch the parameter value associated with each name.
for p = 1:length(files)                         % do this for all of the files found.
  name = files(p).name;                         % store the name of the file in a more convenient place.
  for r = length(name):-1:1                     % iterate 'm' backwards over the length of the name. The parameter value is the last thing to appear in the name after the last underscore.
    if name(r) == '_'                           % if 'm' indexes an underscore in the name, then everything following that will be the parameter value.
      data(p).paramVal = str2num(name(r+1 : length(name)));       % store the value.
      if isempty( strfind(name, 'default') )    % to avoid the word 'default' appearing in the outputPrefix
        outputPrefix = ['robustness_analysis' name(length(dataPrefix)+1 : r)];
      end
      break                                     % and stop looking.
    end
  end
end


% read each of the files found, and store their data in a struct array along with their parameter value.
for p = 1:length(files)
  fid = fopen(files(p).name, 'r');
  data(p).data = textscan(fid, '%d %f %f %f %f %f %f %f %f %f %f %f');    % read in 11 responses, the usual 9 and the 2 EAE related responses. 
  fclose(fid);
end

% 'unused' stores an array of the sorted ParamVals, but we want to sort the entire structure based on those values, not those values alone. So we are interested in 'order'
% This is necessary for plotting the data. Lines are drawn between consequtive items passed to the plot command. If they're not sorted according to domain then you 
% get strange artefacts. 
[unused, order] = sort([data(:).paramVal]);     
data = data(order);                              % reassign 'data' based on the correct ordering

% read the data for the default parameter value
fid = fopen(defaultFile.name, 'r');
  defaultData.data = textscan(fid, '%d %f %f %f %f %f %f %f %f %f %f %f');  % this is where the default data is to be stored
  underscoreLocations = find(defaultFile.name == '_');                            % returns an array containing the locations of all the underscores in the string 'name'
  defaultData.paramVal = defaultFile.name(underscoreLocations(end) + 1 : end);    % the parameter value is the last thing in the name string following directly from the last underscore. 
fclose(fid);

% compile statistical information and tests based on the data read into 'data'. 
for p = 1:length(data)                                                                      % do for each parameter value
  fprintf(1,'compiling data for parameter %s\n', num2str(data(p).paramVal))                
  for r = 2:length(data(p).data)                                                            % we ignore the first response, since it is not a reponse, its the run number. 
    data(p).stats(r).defaultMedian = median(defaultData.data{r});                           % store the default value median 
    data(p).stats(r).median = median(data(p).data{r});                                      % store the parameter value median
    data(p).stats(r).A = Atest(data(p).data{r}, defaultData.data{r});                               % perform and store the value of the A test between default and paramete data .
          % calculate if the results of the A test are "biologically significant" according to the predefined boundaries. 
    if data(p).stats(r).A < aTestBoundsMin | data(p).stats(r).A > aTestBoundsMax
      data(p).stats(r).ATestSignificant = true;
    else      
      data(p).stats(r).ATestSignificant = false;
    end
    data(p).stats(r).IQR = iqr(data(p).data{r});                                            % calculate and store the interquartile range.   
  end
end


% pull data from the 'data' structure and place it in arrays that are more suitable for plotting graphs with. 
Ps = [];
As = [];
for p = 1:length(data)                              % iterate through each parameter value
  for r = 2:length(data(p).stats)                   % we start from 2, because the first column is not a reponse, it is the run number
    As(p, r-1) = data(p).stats(r).A;
    Ps(p) = data(p).paramVal;
  end
end
As                                                  % uncomment if you want to see the data being generated. 
Ps



if drawGraphs

  clf;                                                % clear the figure of anything that might previously have been displayed. 

  % plot a graph of parameter values against A test scores. 
  hold on;
  plot(Ps, As(:,1), 'r-s', 'LineWidth', LineWidth, 'MarkerSize', MarkerSize);
  plot(Ps, As(:,2), 'r-.s', 'LineWidth', LineWidth, 'MarkerSize', MarkerSize);
  plot(Ps, As(:,3), 'm-o', 'LineWidth', LineWidth, 'MarkerSize', MarkerSize);
  plot(Ps, As(:,4), 'm-.o' , 'LineWidth', LineWidth, 'MarkerSize', MarkerSize);
  plot(Ps, As(:,5), 'b-^', 'LineWidth', LineWidth, 'MarkerSize', MarkerSize);
  plot(Ps, As(:,6), 'b-.^', 'LineWidth', LineWidth, 'MarkerSize', MarkerSize);
  plot(Ps, As(:,7), 'g-v', 'LineWidth', LineWidth, 'MarkerSize', MarkerSize);
  plot(Ps, As(:,8), 'g-.v', 'LineWidth', LineWidth, 'MarkerSize', MarkerSize);
  plot(Ps, As(:,9), 'k-*', 'LineWidth', LineWidth, 'MarkerSize', MarkerSize);
  plot(Ps, As(:,10), 'k--<', 'LineWidth', LineWidth, 'MarkerSize', MarkerSize);
  plot(Ps, As(:,11), 'k-->', 'LineWidth', LineWidth, 'MarkerSize', MarkerSize);
  set(gca,'box','on');
  set(gca,'LineWidth',LineWidth);                           % draw a thicker box around the plot. 
  hold off;


   
  line([Ps(1), Ps(length(Ps))], [0.71, 0.71], 'color', 'k', 'LineStyle', ':','LineWidth',LineWidth)   % draw the 0.71 effect magnitude line
  line([Ps(1), Ps(length(Ps))], [0.29, 0.29], 'color', 'k', 'LineStyle', ':','LineWidth',LineWidth)   % draw the 0.29 effect magnitude line

  B = [Ps(1), Ps(end), 0.0, 1.0];                     % axis ranges, [xmin, xmax, ymin, ymax].
  axis(B);                                            % replot with differen mins and maxes for axes
  %legend(Responses,'Location','NorthEastOutside','FontSize',FontSize)                                   % write the legend, which lines are which responses.

  set(gca,'FontSize',FontSize);
  if xAxisProvided == false
    xAxisLabel = ['Parameter value (' paramUnits ')'];
  end
 

  ylabel('A test score','FontSize',FontSize);
  xlabel(xAxisLabel,'FontSize',FontSize);


  print('-dpng', '-r300', [pwd '/' outputPrefix 'Atest'])    % write the graph to the file system. 

  %-------------------------------
  % Convery temporal-related responses from hours into days. REMEMBER that the first column of data(p).data contains time of sample. Hence, CD4Th1Max = second column.
  for p = 1:length(Ps)
    data(p).data{3} = data(p).data{3} / 24  % divide only the temporal response data by 24, converting hours into days. 
    data(p).data{5} = data(p).data{5} / 24;  % divide only the temporal response data by 24, converting hours into days. 
    data(p).data{7} = data(p).data{7} / 24;  % divide only the temporal response data by 24, converting hours into days. 
    data(p).data{9} = data(p).data{9} / 24;  % divide only the temporal response data by 24, converting hours into days. 
  end
  %-------------------------------

  %-------------------------------
  % for each response, plot the medians in terms of the actual response data, showing medians, IRQs, extereme values, and outliers. 
  for r = 1:length(Responses)
    name = Responses{r};
    name(ismember(name,' ')) = []                                 % the function around 'name' here removes all the spaces from the string

    y_Label = ResponseLabels{r};

    robustnessAnalysis_Responses_plotAndStore(data, Ps, defaultData, r+1, name, outputPrefix, xAxisLabel, y_Label, 0)
  end
  
 r = 9
    name = Responses{r};
    name(ismember(name,' ')) = []                                 % the function around 'name' here removes all the spaces from the string

    y_Label = ResponseLabels{r};

    robustnessAnalysis_Responses_plotAndStore(data, Ps, defaultData, r+1, name, outputPrefix, xAxisLabel, y_Label, 600)
 
  
  %-------------------------------

  %-------------------------------
  % Draw graph of how T cell peak numbers change with each parameter, as a bar graph.
  % Note that 'hold on/off' cannot be used with bar, it messes things up (?). 
  clf;
  for p = 1:length(Ps)  
    bardata(p,1) = median(data(p).data{2});
    bardata(p,2) = median(data(p).data{4});
    bardata(p,3) = median(data(p).data{6});
    bardata(p,4) = median(data(p).data{8});
  end 
  h = bar(bardata);%,'BarLayout','grouped');

%  xlim([min(Ps)-(0.05*max(Ps)) , 1.05*max(Ps)]);
  set(h(1),'facecolor','r');   % set Th1 cells to red
  set(h(2),'facecolor','m');   % set Th2 cells to magneta
  set(h(3),'facecolor','b');   % set CD4Treg cells to blue
  set(h(4),'facecolor','g');   % set CD8Treg cells to green
  set(gca,'XTickLabel',Ps);
  set(gca,'box','on');
  set(gca,'LineWidth',LineWidth);
  set(gca,'FontSize',FontSize);
  xlabel(xAxisLabel,'FontSize',FontSize);
  ylabel('Cells','FontSize',FontSize);
  legend('CD4Th1','CD4Th2','CD4Treg','CD8Treg');
%  hold off;

  print('-dpng', '-r300', [pwd '/' outputPrefix 'TCellPeaks'])
  %----------------------------

  %----------------------------
  % Print the responses for each parameter value to the file system. 
  fid = fopen('robustness_analysis_-_ScoresATest','w');
  fprintf(fid,'#param value, CD4Th1Max, CD4Th1MaxTime, CD4Th2Max, CD4Th2MaxTime, CD4TregMax, CD4TregMaxTime, CD8TregMax, CD8TregMaxTime, Th1@40d, Max EAE, EAE @ 40d\n');
  for p = 1:length(data)
    fprintf(fid,'%u ', data(p).paramVal);
    for r = 2:length(data(p).data)                                                            % we ignore the first response, since it is not a reponse, its the run number.
      fprintf(fid,'%g ', data(p).stats(r).A);
    end
    fprintf(fid,'\n');
  end
  fclose(fid);
  %----------------------------
end

%=======================================
%=======================================
% Calculate robustness indexes, based on exactly where the response A test values cross the 0.71 and 0.29 significance boundaries
robustnessIndexes = [];

defaultIndex = find([data(:).paramVal] == str2num(defaultData.paramVal));      % this must exist, and only once. 

for r = 1:length(Responses)
  name = Responses{r};
  name(find(name == ' ')) = [];                             % remove all the spaces from the name.
  robustnessIndexes(r).name = name;
  robustnessIndexes(r).lowerBoundary = NaN;
  robustnessIndexes(r).upperBoundary = NaN;
  robustnessIndexes(r).lowerDirection = '.';
  robustnessIndexes(r).upperDirection = '.';

  % -------------------------------------
  % calculate higher end robustness index. 
  for p = defaultIndex + 1 : length(data)  
    % move out from the default to the extreme, operating over each range of response data values
    x_lo = data(p-1).paramVal;
    x_hi = data(p).paramVal;
    y_lo = data(p-1).stats(r + 1).A;
    y_hi = data(p).stats(r + 1).A;

    u_lo = data(1).paramVal;
    u_hi = data(end).paramVal;
    v_lo = 0.71;     v_hi = 0.71;

    % perform the line cross test
    [flag, x_cross, y_cross] = IntersectionOfLines(x_lo, y_lo, x_hi, y_hi, u_lo, v_lo, u_hi, v_hi);
    if flag == 1
      % if the point at which the lines cross is within the range of response data values, then stop, the point is found  
      if x_cross >= x_lo & x_cross <= x_hi
        robustnessIndexes(r).upperBoundary = x_cross;
        robustnessIndexes(r).upperDirection = '+';
        break
      end
    end
    
    v_lo = 0.29;     v_hi = 0.29;
    [flag, x_cross, y_cross] = IntersectionOfLines(x_lo, y_lo, x_hi, y_hi, u_lo, v_lo, u_hi, v_hi);
    if flag == 1
      % if the point at which the lines cross is within the range of response data values, then stop, the point is found
      if x_cross >= x_lo & x_cross <= x_hi
        robustnessIndexes(r).upperBoundary = x_cross;
        robustnessIndexes(r).upperDirection = '-';
        break
      end
    end
  end
  % -------------------------------------

  % -------------------------------------
  % calculate the lower end robustness index
  for p = defaultIndex -1 :-1: 1 % cycle backwards, from default downwards. 
    x_lo = data(p).paramVal;
    x_hi = data(p + 1).paramVal;
    y_lo = data(p).stats(r + 1).A;
    y_hi = data(p + 1).stats(r + 1).A;

    u_lo = data(1).paramVal;
    u_hi = data(end).paramVal;
    v_lo = 0.71;     v_hi = 0.71;

    % perform the line cross test
    [flag, x_cross, y_cross] = IntersectionOfLines(x_lo, y_lo, x_hi, y_hi, u_lo, v_lo, u_hi, v_hi);
    if flag == 1
      % if the point at which the lines cross is within the range of response data values, then stop, the point is found  
      if x_cross >= x_lo & x_cross <= x_hi
        robustnessIndexes(r).lowerBoundary = x_cross;
        robustnessIndexes(r).lowerDirection = '+';
        break
      end
    end
    
    v_lo = 0.29;     v_hi = 0.29;
    [flag, x_cross, y_cross] = IntersectionOfLines(x_lo, y_lo, x_hi, y_hi, u_lo, v_lo, u_hi, v_hi);
    if flag == 1
      % if the point at which the lines cross is within the range of response data values, then stop, the point is found
      if x_cross >= x_lo & x_cross <= x_hi
        robustnessIndexes(r).lowerBoundary = x_cross;
        robustnessIndexes(r).lowerDirection = '-';
        break
      end
    end
  end
  % -------------------------------------
end

%-------------------------------
% This code will calculate percentage boundaries for parameter perturbations resulting in significant behavioural deviations. 
default = data(defaultIndex).paramVal;
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


fid = fopen(['robustness_indexes_-_' outputPostfix],'w');
fprintf(fid,'%40s %10s %10s %10s %10s %10s %10s %10s %10s %10s %10s\n','#Response', 'close_P', 'close_B', 'low_P', 'up_P', 'low_B', 'low_dir', 'default', 'up_B', 'up_dir', 'units');
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



