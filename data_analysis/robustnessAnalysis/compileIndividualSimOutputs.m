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

function compileIndividualSimOutputs(ARGS)

path = pwd;
k = findstr('Treg_2D',path);
headDir = path(1:k(end)-1);                     % dynamically locate where teh data analysis file is located, based in searching for 'Treg_2D' in the current working dir. 
addpath(genpath([headDir '/Treg_2D/data_analysis/matlab_helper_functions']))

%
% Read arguments
%
args = split_str([' '], ARGS)       % split the arguments string acording to spaces. 

upperRange = 10;
observationEnd = [];
selection = {};                     % user can supply a selection of specific run numbers that they wish to draw. 


for i = 1 : length(args)  
  if strcmp(args{i}, '-end'), observationEnd = args{i+1}, end
  if strcmp(args{i}, '-range'), upperRange = str2num(args{i+1}), end
  if strcmp(args{i}, '-selection')  % this must be the last of the arguments the user supplies
    for j = i+1:length(args)
      selection{end+1} = args{j};
    end
    selection
  end
end

if isempty(selection)
  for run = 0:upperRange
    drawSimOutputGraph(['-save 1 -data simOutputData_' num2str(run) '.txt -end ' observationEnd])
  end
else
  for i = 1:length(selection)
    drawSimOutputGraph(['-save 1 -data simOutputData_' selection{i} '.txt -end ' observationEnd])
  end
end
