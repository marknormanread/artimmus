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

% PLEASE NOTE THAT THIS SCRIPT ALSO GENERATES RESPONSES FOR THE ATEST ROBUSTNESS SENSITIVITY ANALYSIS FRAMEWORK. The outputFileName variable is changed, but otherwise the
% response generation is the same in both sensitivity analyses. 
%
%
% This script will generate all the responses in an LHC sample folder (containing, say, 1000 runs of the simulation for a particular set of parameters drawn from parameter
% space through a latin hypercube design). It will compile, for each simulation execution, 11 responses: the maxiumum number of Th1, Th2, CD4Treg and CD8Treg cells reached
% at any point during simulation execution, and the times at which those maximums are reached (this makes 8 responses), the number of Th1 cells remaining at 40 days, the
% maximum EAE score reached over time, and the EAE score at time 40 days. 
% These responses, for each simulation execution, are written to the filesystem. File name is dictated by the 'outputFileName' argument.
%
% The observationEnd argument is used when generating EAE severity scores. It dictates the length of time (in days) that must be compiled into severity scores.Please note
% that it is strongly recommended that the entire neuronal apoptosis data set not be used, since smoothing artifacts appear at the ends. Recommend that at least 4 days
% worth of neuronal data be discarded from the end of the data set to avoid this. 
function generate_LHCx_response(outputFileName,observationEnd)
outputFileName
observationEnd

path = pwd;
k = findstr('Treg_2D',path);
headDir = path(1:k(end)-1);                     % dynamically locate where teh data analysis file is located, based in searching for 'Treg_2D' in the current working dir. 
addpath(genpath([headDir '/Treg_2D/data_analysis/EAESeverity']))

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



% find and open all the singleRun data files
dataPrefix = 'simOutputData_';
files = dir([dataPrefix '*']);                  % the '*' is a wild wild card. Specifying 'crude_analysis...' will stop '.' and '..' appearing in the list. 

%-------------------------------------------------
% This piece of code will re-order the contents of the files data structure according to the run number (extracted from each file name and converted into a number). 
for f = 1:length(files);
  name = files(f).name;
  runNums(f) = str2num(name(length('simOutputData_')+1 : length(name) - length('.txt')));
end
[unused,order] = sort(runNums);
files = files(order);
%-------------------------------------------------

%-------------------------------------------------
% open this example and find out how long the table is (how many rows, representing samples in time), and how many columns it has. 
if exist('simOutputData_0.txt','file') == 0   % safety, in case this file does not exist, then just exit. Otherwise automation crashes and stops. 
  quit
end

fid = fopen('simOutputData_0.txt');
l = fgetl(fid);                              % read the first line, which in the case of single run data lines ALWAYS includes a comment line at the top. 
if l(1) == '#'
  firstLineComment = true;
end
numCols = length(find(l == ' '));            % extact the number of columns from the number of spaces in the comment line
example = fscanf(fid, '%f ', [numCols,Inf]);    % read in the remainder of the file (header comment line already read) into an array. 
numRows = length(example(1,:));
fclose(fid);
%-------------------------------------------------

responses = [];

% for each single run data file
for f = 1:length(files)
  fid = fopen(files(f).name,'r');
  if firstLineComment
    l = fgetl(fid);                         % throw away the first line, it is a comment. 
  end
  data = fscanf(fid,'%f ',[numCols,Inf]);
  fclose(fid);

  % extract the response data.   
  [maximum,times] = max(data(total_CD4Th1,:));
  responses(f).cd4Th1Max = maximum;
  responses(f).cd4Th1MaxTime = times(1);

  [maximum,times] = max(data(total_CD4Th2,:));
  responses(f).cd4Th2Max = maximum;
  responses(f).cd4Th2MaxTime = times(1);

  [maximum,times] = max(data(total_CD4TregActivated,:));
  responses(f).cd4TregMax = maximum;
  responses(f).cd4TregMaxTime = times(1);

  [maximum,times] = max(data(total_CD8TregActivated,:));
  responses(f).cd8TregMax = maximum;
  responses(f).cd8TregMaxTime = times(1);

  responses(f).cd4Th1_40d = data(total_CD4Th1,40*24);
end

% The two EAE responses must come from the EAE severity files. Assuming that this exists, the following line will generate responses from them. 
generateEAESeverityResponses(observationEnd)

EAEResponseFile = dir('EAESeverity_response_data*');       % there should only be one of these anyway. 
fid = fopen(EAEResponseFile(1).name,'r');     % open the EAE severities file
  l = fgetl(fid);                             % throw away the first line, it is a comment. 
  scores = fscanf(fid,'%f ', [3,Inf]);        % 3 columns are run number, max EAE and EAE at 40 days. 

  for f = 1:length(files)                     % store the EAE responses. 
    responses(f).maxEAE = scores(2,f);
    responses(f).eae_40d = scores(3,f); 
  end
fclose(fid);


% write the data to the filesystem. 
% responses are written thus: run#,CD4Th1Max, CD4Th1MaxTime,CD4Th2Max, CD4Th2MaxTime,CD4TregMax,Cd4TregMaxTime,CD8TregMax,CD8TregMaxTime,Th1@40d,EAEMax,EAE@40d
fid = fopen(outputFileName,'w');
  for run = 1:length(files)
    fprintf(fid,'%u ',run);
    fprintf(fid,'%4.4f ',responses(run).cd4Th1Max);
    fprintf(fid,'%4.4f ',responses(run).cd4Th1MaxTime);
    fprintf(fid,'%4.4f ',responses(run).cd4Th2Max);
    fprintf(fid,'%4.4f ',responses(run).cd4Th2MaxTime);
    fprintf(fid,'%4.4f ',responses(run).cd4TregMax);
    fprintf(fid,'%4.4f ',responses(run).cd4TregMaxTime);
    fprintf(fid,'%4.4f ',responses(run).cd8TregMax);
    fprintf(fid,'%4.4f ',responses(run).cd8TregMaxTime);
    fprintf(fid,'%4.4f ',responses(run).cd4Th1_40d);
    fprintf(fid,'%4.4f ',responses(run).maxEAE);
    fprintf(fid,'%4.4f ',responses(run).eae_40d);
    fprintf(fid,'\n');
  end
fclose(fid);
