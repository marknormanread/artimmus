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
% This script analizes the relapses of EAE, and their durations, for a series of experimental data. It requires that the EAESeverityScoresForRuns file has been compiled. 
% From this file, the number of simulation runs that have a single bout of EAE, versus a single relapse, two relapses, etc, are extracted and written to a table.
%
% The scrip goes on to calculate the duration of each of these relapses, and plots a cumulative distribution function for each. Ie, say that 10 out of 50 simulations have
% a bout of EAE and then a relapse. The cumulative distribution plot wil show the cumulative proportion of runs that experience each possible duration of EAE, for the first bout, and again for
% the second bout. 
%
% The script has been augmented to now provide information on the duration of remissions from EAE. Similar presentation in the form of cumulative distribution plots are used. 
%
clear;              % in case this script is called directly after another. 

maxEAEBouts = 10;
maxPlotTime = 50;   % the maximum number of days that the plot of EAE incidence durations will display. 

fontsize = 18;
linewidth = 2.0;

directory = pwd;
fslashes = find(directory == '/');                         % find the location of all forward slashes in the current directory name
fileNameTag = directory(fslashes(end)+1 : end)             % pull out the string from the end of the last slash to the end of the directory name. 
fileNameTag(find(fileNameTag == '.')) = '_';


%---------------------------------------------
% Reading arbritray sized files into matricies seems a bit tricky in matlab, so this will find out how big the file will be. 
% the EAE scores file has the following format. rows = time sample points. First column is time of sample point, and every additional column represents a run. 

dataPrefix = 'simOutputData_';
files = dir([dataPrefix '*']);                  % the '*' is a wild wild card. Specifying 'crude_analysis...' will stop '.' and '..' appearing in the list. 
numRuns = length(files)

fid = fopen('EAESeverityScoresForRuns');
example = fscanf(fid,'%f',[numRuns + 1, Inf]);
fclose(fid);
timeSamples = size(example,2)
%---------------------------------------------




fid = fopen('EAESeverityScoresForRuns','r');
    % dynamic scan to fill the given matrix dimensions, note the single '%f'
  EAEScores = fscanf(fid, '%f', [numRuns + 1,Inf]); 
    % this switches the data to the more familiar structure, time and then
    % runs along the columns, and rows being time samples. EAEScores(<rows>,<cols>)
  EAEScores = EAEScores';          
fclose(fid);

clinicalEpisodes = zeros(1,numRuns);     % will store the number of clinical episodes per run

propRelapses = zeros(maxEAEBouts+1, 1); % produces a vector of zeros. The first index represents 
                                        % no bouts of EAE, the second mono-phasic, 
                                        % the third dual-phasic, and so on. The variable stores 
                                        % the proportion of all simulation runs experiencing
                                        % particular numbers of EAE episodes. 
propDead = 0;

durationRelapses = cell([6,1]);       % will store the durations of EAE bouts here. durationRelapses{<bout number>}[<sample>]. 
                                      % No indicence of EAE not considered here, hence the first
                                      % index relates to the durations of the first bout, and so on.
                                      % Length of second dimension may change, according to how 
                                      % many simulation runs hit that number of bouts. 

durationRemissions = cell([6,1]);     % COMMMENT

for run = 1:numRuns
  runCol = run+1;                         % the first column is time. 

  % a bit of hypothetical futureproofing. Possible that no EAE developed at all! 
  if ~isempty(find(EAEScores(:,runCol) >= 1))      

    if any(EAEScores(:,runCol) == 5)
      propDead = propDead + 1;
    end

    % there is some elegance here. Ismember returns 1 for all items in the first array that equal 
    % the value of the second. 
    % This is then bit-flipped to give 1s for EAE scores >= 1, and 0 otherwise.
    % returns 1 for each day that score greater than or equal to 1 was found, 
    % and zero for all symptom free days. 
    clinicalSymptoms = ~ismember(EAEScores(:,runCol), 0);    
                                                              
    
    clinicalPeriods = 0;
    for d = 2:length(clinicalSymptoms)
      if clinicalSymptoms(d-1) == 0 && clinicalSymptoms(d) == 1
        clinicalPeriods = clinicalPeriods + 1;
      end
    end   

    % store the number of clinical periods that this simulation run experienced
    clinicalEpisodes(run) = clinicalPeriods;

    %-------------------------------------------
    % Calculate the duration of relapses into EAE symptoms. This operates by counting the length of such periods, and storing the length depending on how many relapses have happened before
    % the one currently being examined. 
    bout = 0;
    i = 1;            % i is the index of the start of a clinical bout
    while i < size(clinicalSymptoms,1)      
      if clinicalSymptoms(i) == 1
        % found start of clinical bout
        bout = bout + 1;
        j = i + 1;    % j will search for the end of a clinical bout. Start 1 ahead of i. 
          % will count the length of the clinical bout. J will finish the loop as the first 0 following the last 1 in the bout. j - i will give the length of the bout. 
        while j <= size(clinicalSymptoms,1) && clinicalSymptoms(j) == 1 % note use of short-circuitry. Second term could cause index out of bounds error. It is not evaluated unless first term passes.
          j = j + 1;                    
        end 
        duration = j-i;

        durationRelapses{ min(bout,length(durationRelapses)) }(end+1) = duration;      % assign towhichever is smaller, the length of cell array or the bout number. 

        i = j;        % start search for the next relapse from here. 
      else  
        % did not find a clinical bout on this day, so increment to the next day. 
        i = i + 1;     
      end
    end   
    %-------------------------------------------


    %-------------------------------------------
    % Calculate the duration of remissions out of EAE symptoms. This operates as above (for remissions)
    remission = 0;
    i = find(clinicalSymptoms == 1);               % start looking at the first onset of autoimmunity. 
    i = i(1);                                      % 'i' will store the start of a symptom free remission. 'j' counts the length of time till the next relapse. 
    while i < size(clinicalSymptoms,1)             % stop searching when i exceeds the period of observation.
      if clinicalSymptoms(i) == 0                  % looking for the start of a symptom free period.
        remission = remission + 1;                 % record this remission number
        j = i + 1;                                 % 'j' is incremented until the next bout of EAE symptoms. 
        while j <= size(clinicalSymptoms,1) && clinicalSymptoms(j) == 0 % note use of short-circuitry. 
          j = j + 1;
        end
          % this if statement ensures that a 'remission' is only so called if EAE symptoms follow. remissions should not be included if their length is dictated by the
          % end of obsevation, since it skews the data. I'm interested in how long it takes before another bout of EAE happens. Hence, if the above loop exited because
          % j exceeded the period of observation, then it should not be included.        
        if j<= size(clinicalSymptoms,1)       
          duration = j - i; 
          durationRemissions{ min(remission, length(durationRemissions)) }(end + 1) = duration;
        end
        i = j;
      else
        i = i + 1;
      end
    end
    %-------------------------------------------


    if clinicalPeriods >= length(propRelapses -1)    % first index in propRelapses is 1, represents no EAE. So if propRelapses measures up to 10 (index 11),
                                                    % and clinicalPeriods is 11 (as in, actualy bouts of EAE), then this should get flagged.
      propRelapses(end) = propRelapses(end) + 1;
    else
        % the first index in propRelapses is for no EAE incidence. But that index is 1 (matlab labelling), hence we add 1. 
      propRelapses(clinicalPeriods+1) = propRelapses(clinicalPeriods+1) + 1;
    end
  else            % if there was no episode of autoimmune symptoms for this run (the if statement is way above).
    propRelapses(1) = propRelapses(1) + 1;          % record that this run had no clinical symptoms.
  end
end

rawDead = propDead;                           % store raw data, before conversion to proportions
rawRelapses = propRelapses;                   % store raw data, before conversion to proportions
propDead = (propDead / numRuns) * 100         % convert totals into proportions, and then into percentages. 
propRelapses = (propRelapses / numRuns) * 100       % convert totals into proportions, and then into percentages. 


fid = fopen('EAE_relapses_analysis_data','w')
  fprintf(fid,'# indicated are total bouts of EAE, not just relapses. IE, it is possible to have no EAE, and the first onset counts as 1. Actual is number of simulations, proportion is percentage of total runs.\n');
  fprintf(fid,'#%19s %10s %10s\n', 'Relapses', 'Actual', 'Proportion');
  fprintf(fid, '%20s %10u\n', 'numRuns', numRuns);
  fprintf(fid,'%20s %10u %10.1f\n', 'mortalities', rawDead, propDead);
  for r = 1:length(propRelapses)
    fprintf(fid,'%2u %17s %10u %10.1f\n', r-1 , 'EAE incidents', rawRelapses(r), propRelapses(r));
  end
fclose(fid);


fid = fopen('clinical_episodes_per_run','w')
  fprintf(fid,'# each column in this file represents a simulation run. Values are number of clinical episodes that simulation had\n');
  for i = 1:length(clinicalEpisodes)
    fprintf(fid,'%u ', clinicalEpisodes(i));
  end
fclose(fid);




clf;
colours =     {'k','b','g','c','r','m','k' ,'b' ,'g' ,'c' ,'r' ,'m' };
linestyles =  {'-','-','-','-','-','-','--','--','--','--','--','--'};

%-------------------------------------------
% plot graph of duration of RELAPSES. 
hold on
legendText = {};
for bout = 1:length(durationRelapses)
  durations = durationRelapses{bout};
  if ~isempty(durations)
    h = cdfplot(durations);
    set(h,'color',colours{ bout });
    set(h,'linestyle',linestyles{  bout });
    set(h,'linewidth',linewidth);
    legendText{end+1} = num2str(bout);
  end
end
if length(legendText) > 1
  legendText{end} = ['>= ' legendText{end}];
end
set(gca,'box','on');
set(gca,'FontSize',fontsize);
set(gca,'LineWidth',linewidth);

xlim([0, maxPlotTime]);
xlabel('Duration (days)','FontSize',fontsize);
ylabel('Proportion of simulations','FontSize',fontsize);

title('');
[legh,objh,outh,outm] = legend(legendText,'location','SouthEast');
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
hold off

print('-dpng', '-r300', ['RelapseIncidenceDurations__' fileNameTag '.png'])    % write the graph to the file system. 
%-------------------------------------------


%-------------------------------------------
% plot graph of duration of REMISSIONS. 
clf;
hold on
legendText = {};
for bout = 1:length(durationRemissions)
  durations = durationRemissions{bout};
  if ~isempty(durations)
    h = cdfplot(durations);
    set(h,'color',colours{ bout });
    set(h,'linestyle',linestyles{  bout });
    set(h,'linewidth',linewidth);
    legendText{end+1} = num2str(bout);
  end
end
if length(legendText) > 1
  legendText{end} = ['>= ' legendText{end}];
end
set(gca,'box','on');
set(gca,'FontSize',fontsize);
set(gca,'LineWidth',linewidth);


xlim([0, 80]);
xlabel('Duration (days)','FontSize',fontsize);
ylabel('Proportion of simulations','FontSize',fontsize);

title('');
[legh,objh,outh,outm] = legend(legendText,'location','SouthEast');
set(legh,'LineWidth',0.5*linewidth);                      % sets the line width of the legend axis (the box around the legend)
set(objh,'LineWidth',linewidth);                          % sets the line width of the lines in the legend, ie, the lines on the plot.
hold off

print('-dpng', '-r300', ['RemissionIncidenceDurations__' fileNameTag '.png'])    % write the graph to the file system. 
%-------------------------------------------


