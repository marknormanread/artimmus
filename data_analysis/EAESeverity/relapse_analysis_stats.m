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


path = pwd;
k = findstr('Treg_2D',path);
headDir = path(1:k(end)-1);     % dynamically locate where the data analysis file is located, based in searching for 'Treg_2D' in the current working dir. 
addpath(genpath([headDir '/Treg_2D/data_analysis']))

%----------------------------------------------------
% find the default parameter value. This could be supplied as an argument when calling this
% script, but that functionality has not yet been added.
defaultParamValue = [];
if exist('defaultParameterValue') == 2
  fid = fopen('defaultParameterValue');
  temp = textscan(fid, '%f');
  defaultParamValue = temp{1}
  fclose(fid);
else
  error('please place the default parameter value in a file named defaultParameterValue');
end
%----------------------------------------------------

%----------------------------------------------------
% find all directories that contain relapsing autoimmunity data
dirs = dir;               % will extract all the files & directories listed in the cwd
dirs(find( [dirs(:).isdir] == 0)) = [];  % removes those files & dirs that are not directories

lose = [];                % will use this to remove direcetories that do not contain relapse data
for d = 1:length(dirs)    % go through each directory, looking for the data file
  fn = [dirs(d).name '/EAE_relapses_analysis_data']; 
  if exist(fn) ~= 2       % test whether such a file exists
    lose(end + 1) = d;    % if it does not, then this directory should be excluded
  end  
end

dirs(lose) = [];
% dirs now holds the directories that contain relapse data files. 
%----------------------------------------------------


%----------------------------------------------------
% extract data for each experiment from the directories identified above. 
experiment = [];          % this will store experimental data
defaultParamIndex = [];   % the index of control data in the experiment data structure

for d = 1:length(dirs)
  fid = fopen([dirs(d).name '/EAE_relapses_analysis_data']);
  unused = fgets(fid);     % throw away first line, it is a comment
  unused = fgets(fid);     % throw away second line, it is a comment
  
  % read single line of numRuns - reads something like : "numRuns 500"
  n = textscan(fid, '%s %u',1);
  numRuns = n{2};           % extract number of simulation executions

  % read single line of deaths - reads something like this: "mortalities 145 29.0"
  m = textscan(fid, '%s %u %f',1);
  experiment(d).deaths = m{2}; % extract mortalities from data file
  
  % format for this string is: # of relapses, 'EAE rela...', number of sims, proportion of sims
  % note that "EAE relapses" counts as two strings. 
  temp = textscan(fid, '%u %s %s %u %f');
  
  experiment(d).relapses = temp{4};  % store raw number of sims having each relapse
  fclose(fid);

  % extract the parameter value from the directory name
  k = find(dirs(d).name == '_');
  experiment(d).paramValS = dirs(d).name( k(end) + 1 : end);  % store param val as a string
  experiment(d).paramVal = str2num(experiment(d).paramValS);
end

for d = 1:length(dirs)
  fid = fopen([dirs(d).name '/clinical_episodes_per_run']);
  unused = fgets(fid);    % throw away first line, it is a comment
  
  experiment(d).clinicalEpisodes = fscanf(fid, '%f', [numRuns,1]); 
  fclose(fid);
end
%----------------------------------------------------

% 'unused' stores an array of the sorted paramVals, but we want to sort the entire structure 
% based on those values, not those values alone. So we are interested in 'order'
[unused, order] = sort([ experiment(:).paramVal ]);     
experiment = experiment(order);      % reassign 'data' based on the correct ordering
defaultParamIndex = find([experiment(:).paramVal] == defaultParamValue);

numRelapses = length(experiment(1).relapses); % this is how many possible relapses have been examined

for d = 1:length(experiment)
  for r = 1:numRelapses
    % zero means no relapse, 1 will mean that a simulation did have this number of relapses
    exp_data = zeros(1, numRuns);   
    % handle special case of no simulatoins having this number of relapses
    if experiment(d).relapses(r) ~= 0   
      exp_data(1:experiment(d).relapses(r)) = 1;
    end

    % this is same as above, for exp_data
    control_data = zeros(1, numRuns);
    if experiment(defaultParamIndex).relapses(r) ~= 0
      control_data(1:experiment(defaultParamIndex).relapses(r)) = 1;
    end

    observations = [control_data, exp_data];  % control observations first, then experiments
    exp_case = [zeros(1,numRuns),ones(1,numRuns)];  % indicate first lot are control, then experiment
    
    experiment(d).fisher(r) = fexact(observations',exp_case');
  end
  
  ctrl = experiment(defaultParamIndex).clinicalEpisodes;
  exp = experiment(d).clinicalEpisodes;
  experiment(d).a = Atest(ctrl,exp);
  experiment(d).u = ranksum(ctrl,exp);
end



% write the resultant data to the file system. 
fid = fopen('relapse_data_stats_analysis','w');
fprintf(fid, '#---------\n');
fprintf(fid, '# fishers exact test on pairwise episode bount number between exp and control\n');
fprintf(fid, '#---------\n');
fprintf(fid,'# %10s %6u %6u %6u %6u %6u %6u %6u %6u %6u %6u %6u %6u\n', 'paramVal', 1,2,3,4,5,6,7,8,9,10,11,12);
for d = 1:length(experiment)
  fprintf(fid,'%12f ', experiment(d).paramVal);
  for r = 1:numRelapses
    fprintf(fid,'%6.4f ', experiment(d).fisher(r));
  end
  fprintf(fid, '\n');
end
fprintf(fid,'\n\n');

fprintf(fid, '#---------\n');
fprintf(fid, '# Mann Whitney U p-values. These contrast distributions comprising number of\n');
fprintf(fid, '# clinical episodes experienced in each execution, between control and experiment.\n');
fprintf(fid, '#---------\n');
fprintf(fid, '# %10s %10s\n','paramVal','p-val');
for d = 1:length(experiment)
  fprintf(fid,'%12f %10.5f\n',experiment(d).paramVal,experiment(d).u);
end
fprintf(fid,'\n\n');

fprintf(fid, '#---------\n');
fprintf(fid, '# A-test scores. These contrast distributions comprising number of\n');
fprintf(fid, '# clinical episodes experienced in each execution, between control and experiment.\n');
fprintf(fid, '#---------\n');
fprintf(fid, '# %10s %10s\n','paramVal','a-score');
for d = 1:length(experiment)
  fprintf(fid,'%12f %10.5f\n',experiment(d).paramVal,experiment(d).a);
end

fclose(fid);
