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
% A script designed to collect successive groups of 5 simulation runs, and plot the EAE severity of those 5 runs on a graph. 
%


% find all the relevant files in the directory
dataPrefix = 'simOutputData_';    % the prefix for files that contain response data
files = dir([dataPrefix '*']);                  % the '*' is a wild wild card. Specifying 'lhc1_analysis...' will stop '.' and '..' appearing in the list. 

runsPerPlot = 5;

for run = 1:6:length(files)-runsPerPlot
  arg = '';
  for i = run:run + runsPerPlot -1
    arg = [arg files(i).name ' '];
  end
  arg = arg(1:end-1);
  EAESeverityScores_individualRuns(arg);
end
