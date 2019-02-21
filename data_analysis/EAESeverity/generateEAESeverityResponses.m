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

% This file is intended to be run from a directory containing single run data files, the EAE severities for these simulation runs must already
% have been calculated. This script will compile, from those EAE scores for runs, two responses: the maximum EAE score attained durinf simulation
% execution for all the runs, and the EAE score at day 40. This information will then be output to a directory. 
%
%
function generateEAESeverityResponses(observationEnd)

path = pwd;
k = findstr('Treg_2D',path);
headDir = path(1:k(end)-1);                     % dynamically locate where teh data analysis file is located, based in searching for 'Treg_2D' in the current working dir. 
addpath(genpath([headDir '/Treg_2D/data_analysis/EAESeverity']))

% read in the EAE Severities for runs file. 
if exist('EAESeverityScoresForRuns','file') == 0     % safety, in case the script has been run on a directory that does not contain all the necessary data. This stops automation crashing. 
  compileEAESeveritiesForRuns(['-end ' num2str(observationEnd)])
end

fid = fopen('EAESeverityScoresForRuns','r');    % note that the first column is the time in days. Sample rate is daily, not hourly like other data 
  l = fgetl(fid);                               % read the first line, which in the case of single run data lines ALWAYS includes a comment line at the top. 
  numCols = length(find(l == ' '))   ;          % extact the number of columns from the number of spaces in the comment line
fclose(fid);
  
fid = fopen('EAESeverityScoresForRuns','r');    % note that the first column is the time in days. Sample rate is daily, not hourly like other data  
  scores = fscanf(fid,'%f ',[numCols,Inf]);
  numRows = size(scores,2);
fclose(fid);


respMax = [];
resp40 = [];
for col = 2:numCols         % note that the first column is just the time at which the sample was taken (in days). 
  run = col - 1;
  respMax(run) = max(scores(col,:));          % extract the peak EAE score achieved across all runs, this is one of the responses.
  resp40(run) = scores(col,40);               % extract the EAE scores remaining at 1000 hours, the second response
end

fileTag = pwd;
index = findstr(fileTag,'_-_');
if isempty(index)
  underscores = find(fileTag == '/');
  fileTag = fileTag(underscores(end)+1 : end);
else
  fileTag = fileTag(index(end)+3 : end);
end

% write the response information to the filesystem.
fid = fopen(['EAESeverity_response_data_-_' fileTag],'w');
  fprintf(fid,'# run, maxEAEScores, EAEScore@40days\n');
  for run = 1:numCols - 1    % numCols is calculated based on EAESeveritySCoresForRuns, and includes an additional column for the time of sampling, this is removed from respMax and resp40.
    fprintf(fid,'%u %4.4f %4.4f\n',run,respMax(run),resp40(run));
  end
fclose(fid);


