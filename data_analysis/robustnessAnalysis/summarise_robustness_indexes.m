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

% This script is designed to be executed from within a directory containing all the robustness indexes files. The extract_allPngs.rb script performs the extraction.
% The script summarises the response indexes in two formats, with each format being written in a file that is human-readable and another that is latex-readable. 
% 
% The first format prints out response indexes for all parameters with respect to a particular response. The second presents the parameters in the context of their
% ranked robustness in terms of all the individual responses, hence giving an overview of all parameter-response data. 
%


dataPrefix = 'robustness_indexes_-_';
files = dir([dataPrefix '*']);


indexes = [];

Responses = {'CD4Th1Max', 'CD4Th1MaxTime', 'CD4Th2Max', 'CD4Th2MaxTime', 'CD4TregMax', 'CD4TregMaxTime', 'CD8TregMax', 'CD8TregMaxTime', 'CD4Th1at40d','MaxEAEATest', 'EAEat40dATest','MaxEAE','EAEat40d'};

Responses_full = {'CD4Th1 Max', 'CD4Th1 Max Time', 'CD4Th2 Max', 'CD4Th2 Max Time', 'CD4Treg Max', 'CD4Treg Max Time', 'CD8Treg Max', 'CD8Treg Max Time', 'CD4Th1at40d','Max EAE A Test', 'EAE at 40d A Test','Max EAE','EAE at 40d'};

%---------------------------
% read in the data files
for f = 1:length(files)  
               
  fid = fopen(files(f).name,'r');
  temp = textscan(fid,'%s %f %f %f %f %f %s %f %f %s  %s', 'commentstyle','#');

  indexes(f).name = files(f).name(length(dataPrefix)+1 : end);          % store the name of the parameter
  indexes(f).name = strrep(indexes(f).name,'2D','');                    % remove the substring '2D' from the parameter name, should it exist.
  indexes(f).data = zeros(6,13);          % (<index> X <response>)
  indexes(f).data(1,:) = temp{2};         % closest percentage
  indexes(f).data(2,:) = temp{3};         % closest boundary
  indexes(f).data(3,:) = temp{4};         % lower percentage
  indexes(f).data(4,:) = temp{5};         % higher percentage
  indexes(f).data(5,:) = temp{6};         % lower boundary
  indexes(f).lowerDirections = temp{7};
  indexes(f).default = temp{8}(1);        % default value is always the same. 
  indexes(f).data(6,:) = temp{9};         % higher boundary
  indexes(f).higherDirections = temp{10}; 
  indexes(f).units = temp{11}{1};          % units are always the same! 
end
%---------------------------


%---------------------------
for response = 1:length(Responses)
  for p = 1:length(indexes)
    data(p) = indexes(p).data(1,response);
  end
  [sorted,order] = sort(data);

  indexes = indexes(order);
  
  %---------------------------
  % calculate the ranks of the parameters with respect to the robustness indexes for the current response. 
  % items that are tied in terms of rankable data share the lower rank (ie, ranks for the top three might be 1, 3 and 3).
  currentP = 1;
  currentRank = 1;
  while(currentP <= length(indexes))
    currentVal = indexes(currentP).data(1,response);        % rank of current, and perhaps subsequent, item(s) depends on how many items match this data. 

    searching = true;                                       % will search for subsequent items that share the same data and currentVal. 
    tempP = currentP + 1;                                   % this is used to scan ahead through the parameters searching for matches on data. 
    while(searching & tempP <= length(indexes))             % continue searching forward until either an inequivalent data is found, or the end of the array is reached. 
      
      % have to separate out teh NaN case, since all operations return false when fed a NaN number, even equality. 
      if isnan(indexes(tempP).data(1,response)) & isnan(currentVal)
        currentRank = currentRank + 1;                      % current item is the same, keep searching. The current rank is increased to reflect the fact that it is shared. 
        tempP = tempP + 1;
      elseif indexes(tempP).data(1,response) ~= currentVal
        searching = false;                                  % current item is different, stop searching
      else
        currentRank = currentRank + 1;                      % current item is the same, keep searching. The current rank is increased to reflect the fact that it is shared. 
        tempP = tempP + 1;
      end
    end
    
    for p = currentP : (tempP-1)                            % assign rank to all the currently tied parameters. 
      indexes(p).ranks(response) = currentRank;
    end
    currentP = tempP;                                       % preparation for the next items
    currentRank = currentRank + 1;
  end
  %---------------------------

  %---------------------------
  % write human-readable table to the filesystem. 
  fid = fopen(['summarise_robustness_indexes_for_response_-_'  Responses{response}],'w');
  fprintf(fid,'#rank, closest_percent, closest_boundary, lower_percent, higher_percent, lower_boundary, higher_boundary, param_name\n');
  for p = 1:length(indexes)
    fprintf(fid, '%u '   , indexes(p).ranks(response));
    fprintf(fid, '%8.4G ', indexes(p).data(1,response));
    fprintf(fid, '%8.4G ', indexes(p).data(2,response));
    fprintf(fid, '%8.4G ', indexes(p).data(3,response));
    fprintf(fid, '%8.4G ', indexes(p).data(4,response));
    fprintf(fid, '%8.4G ', indexes(p).data(5,response));
    fprintf(fid, '%8s '  , indexes(p).lowerDirections{response});
    fprintf(fid, '%8.4G ', indexes(p).default);
    fprintf(fid, '%8.4G ', indexes(p).data(6,response));
    fprintf(fid, '%8s '  , indexes(p).higherDirections{response});
    fprintf(fid, '%s ', indexes(p).name);
    fprintf(fid, '\n');
  end
  fclose(fid);
  %---------------------------


  %---------------------------
  % write latex-readable table to the filesystem. 
  fid = fopen(['latex_summarise_robustness_indexes_for_response_-_'  Responses{response} '.tex'],'w');

  fprintf(fid, '\\begin{landscape}\n');
  fprintf(fid, '\\begin{center}\n');
  fprintf(fid, '\\tiny{ \n');
  fprintf(fid, '\\begin{longtable}{|l||c|cc|ccc|c|}\n');
	fprintf(fid, '%%Here is the caption, the stuff in [] is the table of contents entry,\n');
	fprintf(fid, '%%the stuff in {} is the title that will appear on the first page of the\n');
	fprintf(fid, '%%table.\n');
	fprintf(fid, '\\caption[Robustness indexes for \\emph{%s} response]\n', Responses_full{response});
  fprintf(fid, '{Robustness indexes for parameters with respect to the \\emph{%s} response. RI, robustness index; LI, lower index; UI, upper index; LB, lower boundary; DV, default value; UB, upper boundary. Results indicating no significant deviation in behaviour are marked with a period.}\n', Responses_full{response});
  fprintf(fid, '\\label{tab:A3-RSASummary%s}\\\\ \n\n', Responses{response});

	fprintf(fid, '    %%This is the header for the first page of the table...\n');
	fprintf(fid, '    \\hline\\hline\n');
	fprintf(fid, '    \\multicolumn{1}{|c||}{\\textbf{Parameter Name}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c|}{\\textbf{RI (\\%%)}}  & \n\n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{LI (\\%%)}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c|}{\\textbf{UI (\\%%)}} & \n\n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{LB}} & \n');
	fprintf(fid, '      \\multicolumn{1}{c}{\\textbf{DV}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c|}{\\textbf{UB}} & \n\n');

	fprintf(fid, '    \\multicolumn{1}{c|}{\\textbf{Rank}} \n');
	fprintf(fid, '    \\\\\\hline\\hline\n');
	fprintf(fid, '	\\endfirsthead\n\n');

	fprintf(fid, '  %%This is the header for the remaining page(s) of the table...\n');
	fprintf(fid, '		\\multicolumn{7}{c}{{\\tablename} \\thetable{} -- Continued} \\\\[0.5ex]\n');
	fprintf(fid, ' 		  \\hline\\hline %%\\\\[-2ex]\n');
	fprintf(fid, '		  \\multicolumn{1}{|c||}{\\textbf{Parameter Name}} & \n');
	fprintf(fid, '      \\multicolumn{1}{c|}{\\textbf{RI} (\\%%)} & \n\n');

	fprintf(fid, '      \\multicolumn{1}{c}{\\textbf{LI} (\\%%)} & \n');
	fprintf(fid, '      \\multicolumn{1}{c|}{\\textbf{UI} (\\%%)} & \n\n');

	fprintf(fid, '      \\multicolumn{1}{c}{\\textbf{LB}} & \n');
	fprintf(fid, '      \\multicolumn{1}{c}{\\textbf{DV}} & \n');
	fprintf(fid, '      \\multicolumn{1}{c|}{\\textbf{UB}} & \n\n');

	fprintf(fid, '      \\multicolumn{1}{c|}{\\textbf{Rank}} \n');
	fprintf(fid, '		  \\\\\\hline\\hline%%[-1.8ex]\n');
	fprintf(fid, ' 		\\endhead\n\n');

	fprintf(fid, '		  %%This is the footer for all pages except the last page of the table...\n');
	fprintf(fid, '	    \\multicolumn{7}{l}{{Continued on Next Page\\ldots}} \\\\ \n');
	fprintf(fid, '	  \\endfoot\n\n');
  
	fprintf(fid, '    	%%This is the footer for the last page of the table...\n');
	fprintf(fid, '	    \\hline\n');
	fprintf(fid, '		\\endlastfoot\n\n');

  for p = 1:length(indexes)
    underscoreLoc = find(indexes(p).name == '_');
    fprintf(fid, '\\textsl{%s} & ', [indexes(p).name(1 : underscoreLoc(1)-1) '\' indexes(p).name(underscoreLoc(1) : end)]);

    if isnan(indexes(p).data(1,response)); fprintf(fid, '. & '); else; fprintf(fid, '%3.4G & ', indexes(p).data(1,response)); end
    
  
    if isnan(indexes(p).data(3,response)); fprintf(fid, '. & '); else; fprintf(fid, '%3.4G & ', indexes(p).data(3,response)); end
    if isnan(indexes(p).data(4,response)); fprintf(fid, '. & '); else; fprintf(fid, '%3.4G & ', indexes(p).data(4,response)); end

    if strcmp(indexes(p).lowerDirections{response},'.') == 1
      fprintf(fid, ' . & ');
    else
      fprintf(fid, '%3.4G$(%s)$ & ', indexes(p).data(5,response), indexes(p).lowerDirections{response});
    end

    fprintf(fid, '%3.4G & ', indexes(p).default);

    if strcmp(indexes(p).higherDirections{response},'.') == 1  
      fprintf(fid, ' . & ');
    else
      fprintf(fid, '%3.4G$(%s)$ & ', indexes(p).data(6,response), indexes(p).higherDirections{response});
    end

    fprintf(fid, '%u '   , indexes(p).ranks(response));
    fprintf(fid, '\\\\ \n');
    if mod(p, 4) == 0 & p ~= length(indexes)
      fprintf(fid, '\\hline \n');
    end
  end


	fprintf(fid, '\n\n  \\end{longtable}\n');  
  fprintf(fid, '  } \n');
  fprintf(fid, '  \\end{center}\n');
	fprintf(fid, '  \\end{landscape}\n');
  fclose(fid);
  %---------------------------
end



%---------------------------
% calculate the total rank score
for p = 1:length(indexes)
  indexes(p).sumOfRanks = sum(indexes(p).ranks(:));
end
[sorted, order] = sort([indexes(:).sumOfRanks]);
indexes = indexes(order);
%---------------------------


%---------------------------
% calculate the ranks of the parameters with respect to the robustness indexes for the current response. 
% items that are tied in terms of rankable data share the lower rank (ie, ranks for the top three might be 1, 3 and 3).
currentP = 1;
currentRank = 1;
while(currentP <= length(indexes))
  currentVal = indexes(currentP).sumOfRanks;        % rank of current, and perhaps subsequent, item(s) depends on how many items match this data. 

  searching = true;                                       % will search for subsequent items that share the same data and currentVal. 
  tempP = currentP + 1;                                   % this is used to scan ahead through the parameters searching for matches on data. 
  while(searching & tempP <= length(indexes))             % continue searching forward until either an inequivalent data is found, or the end of the array is reached. 
    if isnan(indexes(tempP).data(1,response)) & isnan(currentVal)
      currentRank = currentRank + 1;                      % current item is the same, keep searching. The current rank is increased to reflect the fact that it is shared. 
      tempP = tempP + 1;
    elseif indexes(tempP).sumOfRanks ~= currentVal            % test this subsequent parameter against the current data item. 
      searching = false;                                  % current item is different, stop searching
    else
      currentRank = currentRank + 1;                      % current item is the same, keep searching. The current rank is increased to reflect the fact that it is shared. 
      tempP = tempP + 1;
    end
  end
  
  for p = currentP : (tempP-1)                            % assign rank to all the currently tied parameters. 
    indexes(p).globalRank = currentRank;
  end
  currentP = tempP;                                       % preparation for the next items
  currentRank = currentRank + 1;
end
%---------------------------

% sort indexes according to global rank
[sorted,order] = sort([indexes(:).globalRank]);
indexes = indexes(order);

%---------------------------
% Create human-readable table of total rank data. 
fid = fopen('summarise_robutness_indexes_total_ranks','w');
fprintf(fid, '#SumOfRanks, Responses(Th1M Th1MT Th2M Th2MT 4M 4MT 8M 8MT Th140 MEAEA EAE40A MEAE EAE40) paramName, globalRank\n');
for p = 1:length(indexes)
  fprintf(fid, '%4u ', indexes(p).sumOfRanks);
  for r = 1:length(Responses)
    fprintf(fid, '%4u ', indexes(p).ranks(r));
  end
  fprintf(fid, '%s ', indexes(p).name);
  fprintf(fid, '%u \n', indexes(p).globalRank);
end
fclose(fid);
%---------------------------

%---------------------------
% create a latex-readable table of total rank data. 
  fid = fopen('latex_summarise_robustness_indexes_total_ranks.tex','w');

  fprintf(fid, '\\begin{landscape}\n');
  fprintf(fid, '\\begin{center}\n');
  fprintf(fid, 'TODO: CHECK THE FORMAT AND CAPTION OF THIS TABLE. CURRENTLY SHOWS PERCENTAGES (ROBUSTNESS INDEXES), ORDERED BY SUM OF RANKS\n\n');
  fprintf(fid, '\\tiny{ \n');
  fprintf(fid, '\\begin{longtable}{|cl||cc cc cc cc c cc cc|c|}\n');
	fprintf(fid, '%%Here is the caption, the stuff in [] is the table of contents entry,\n');
	fprintf(fid, '%%the stuff in {} is the title that will appear on the first page of the\n');
	fprintf(fid, '%%table.\n');
	fprintf(fid, '\\caption[Rank summaries of robustness indexes]\n');
  fprintf(fid, '{Rank summaries of robustness indexes for all responses. Responses are indicated as follows: 1M, \\emph{CD4Th1 Max}; 1MT, \\emph{CD4Th1 Max Time}; 2M, \\emph{CD4Th2 Max}; 2MT, \\emph{CD4Th2 Max Time}; 4M, \\emph{CD4Treg Max}; 4MT, \\emph{CD4Treg Max Time}; 8M, \\emph{CD8Treg Max}; 8MT, \\emph{CD8Treg Max Time}; Th40, \\emph{CD4Th1 at 40 Days}; MEA, \\emph{Max EAE}; E40A, \\emph{EAE at 40 Days}. Significant deviation is indicated through the \\emph{A} test. ME and E40 represent \\emph{Max EAE} and \\emph{EAE at 40 Days}, with significant deviations in response behaviour defined as a change of at least 1.0 in the mean EAE score. Not-a-number values, representing no significant deviation in behaviour, are marked with a period for clarity.}\n');
  fprintf(fid, '\\label{tab:7-RSASummaryRanks}\\\\ \n\n');

	fprintf(fid, '    %%This is the header for the first page of the table...\n');
	fprintf(fid, '    \\hline\\hline\n');
	fprintf(fid, '    \\multicolumn{1}{|c}{\\textbf{Rank}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c||}{\\textbf{Parameter Name}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{1M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{1MT}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{2M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{2MT}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{4M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{4MT}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{8M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{8MT}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{Th40}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{MEA}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{E40A}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{ME}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c|}{\\textbf{E40}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c|}{\\textbf{Total}} \n');
	fprintf(fid, '    \\\\\\hline\\hline\n');
	fprintf(fid, '	\\endfirsthead\n\n');

	fprintf(fid, '  %%This is the header for the remaining page(s) of the table...\n');
	fprintf(fid, '		\\multicolumn{7}{c}{{\\tablename} \\thetable{} -- Continued} \\\\[0.5ex]\n');
	fprintf(fid, ' 		  \\hline\\hline %%\\\\[-2ex]\n');
	fprintf(fid, '    \\multicolumn{1}{|c}{\\textbf{Rank}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c||}{\\textbf{Parameter Name}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{1M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{1MT}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{2M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{2MT}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{4M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{4MT}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{8M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{8MT}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{Th40}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{MEA}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{E40A}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{ME}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c|}{\\textbf{E40}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c|}{\\textbf{Total}} \n');
	fprintf(fid, '		  \\\\\\hline\\hline%%[-1.8ex]\n');
	fprintf(fid, ' 		\\endhead\n\n');

	fprintf(fid, '		  %%This is the footer for all pages except the last page of the table...\n');
	fprintf(fid, '	    \\multicolumn{7}{l}{{Continued on Next Page\\ldots}} \\\\ \n');
	fprintf(fid, '	  \\endfoot\n\n');
  
	fprintf(fid, '    	%%This is the footer for the last page of the table...\n');
	fprintf(fid, '	    \\hline\n');
	fprintf(fid, '		\\endlastfoot\n\n');

  for p = 1:length(indexes)
    fprintf(fid, '%u & ', indexes(p).globalRank);
    underscoreLoc = find(indexes(p).name == '_');
    fprintf(fid, '\\textsl{%s} & ', [indexes(p).name(1 : underscoreLoc(1)-1) '\' indexes(p).name(underscoreLoc(1) : end)]);

    for r = 1:length(Responses)
      rank = indexes(p).ranks(r);
      if rank == 73                                             % for clarity, write '.' instead of '73' for parameters ranked last. 
        fprintf(fid, '. & ');
      else
        %fprintf(fid, '%3.4G(%1.0f) & ', rank, indexes(p).data(1,r));
        if ~isnan(indexes(p).data(1,r))
          fprintf(fid, '%3.2f & ', indexes(p).data(1,r));
        else
          fprintf(fid, '. & ', indexes(p).data(1,r));
        end
      end
    end
    fprintf(fid, '%u '   , indexes(p).sumOfRanks);
    fprintf(fid, '\\\\ \n');
    if mod(p, 4) == 0 & p ~= length(indexes)
      fprintf(fid, '\\hline \n');
    end
  end


	fprintf(fid, '\n\n  \\end{longtable}\n');  
  fprintf(fid, '  } \n');
  fprintf(fid, '  \\end{center}\n');
	fprintf(fid, '  \\end{landscape}\n');
  fclose(fid);
  %---------------------------


[unused,order] = sort({indexes(:).name});
indexes = indexes(order);

%---------------------------
% create a latex-readable table of total rank data. 
  fid = fopen('latex_summarise_robustness_indexes_allRIs.tex','w');

  fprintf(fid, '\\begin{landscape}\n');
  fprintf(fid, '\\begin{center}\n');
  fprintf(fid, 'TODO: CHECK THE FORMAT AND CAPTION OF THIS TABLE. CURRENTLY SHOWS PERCENTAGES (ROBUSTNESS INDEXES), ORDERED BY SUM OF RANKS\n\n');
  fprintf(fid, '\\tiny{ \n');
  fprintf(fid, '\\begin{longtable}{|l||cc cc cc cc c cc cc|}\n');
	fprintf(fid, '%%Here is the caption, the stuff in [] is the table of contents entry,\n');
	fprintf(fid, '%%the stuff in {} is the title that will appear on the first page of the\n');
	fprintf(fid, '%%table.\n');
	fprintf(fid, '\\caption[Rank summaries of robustness indexes]\n');
  fprintf(fid, '{Rank summaries of robustness indexes for all responses. Responses are indicated as follows: 1M, \\emph{CD4Th1 Max}; 1MT, \\emph{CD4Th1 Max Time}; 2M, \\emph{CD4Th2 Max}; 2MT, \\emph{CD4Th2 Max Time}; 4M, \\emph{CD4Treg Max}; 4MT, \\emph{CD4Treg Max Time}; 8M, \\emph{CD8Treg Max}; 8MT, \\emph{CD8Treg Max Time}; Th40, \\emph{CD4Th1 at 40 Days}; MEA, \\emph{Max EAE}; E40A, \\emph{EAE at 40 Days}. Significant deviation is indicated through the \\emph{A} test. ME and E40 represent \\emph{Max EAE} and \\emph{EAE at 40 Days}, with significant deviations in response behaviour defined as a change of at least 1.0 in the mean EAE score. Ranks of 73, representing no significant deviation in behaviour are marked with a period for clarity.}\n');
  fprintf(fid, '\\label{tab:7-RSASummaryRanks}\\\\ \n\n');

	fprintf(fid, '    %%This is the header for the first page of the table...\n');
	fprintf(fid, '    \\hline\\hline\n');
	fprintf(fid, '    \\multicolumn{1}{|c||}{\\textbf{Parameter Name}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{1M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{1MT}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{2M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{2MT}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{4M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{4MT}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{8M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{8MT}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{Th40}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{MEA}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{E40A}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{ME}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c|}{\\textbf{E40}}s \n');

	fprintf(fid, '    \\\\\\hline\\hline\n');
	fprintf(fid, '	\\endfirsthead\n\n');

	fprintf(fid, '  %%This is the header for the remaining page(s) of the table...\n');
	fprintf(fid, '		\\multicolumn{7}{c}{{\\tablename} \\thetable{} -- Continued} \\\\[0.5ex]\n');
	fprintf(fid, ' 		  \\hline\\hline %%\\\\[-2ex]\n');
	fprintf(fid, '    \\multicolumn{1}{|c||}{\\textbf{Parameter Name}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{1M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{1MT}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{2M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{2MT}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{4M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{4MT}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{8M}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{8MT}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{Th40}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{MEA}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{E40A}} & \n');

	fprintf(fid, '    \\multicolumn{1}{c}{\\textbf{ME}} & \n');
	fprintf(fid, '    \\multicolumn{1}{c|}{\\textbf{E40}} \n');

	fprintf(fid, '		  \\\\\\hline\\hline%%[-1.8ex]\n');
	fprintf(fid, ' 		\\endhead\n\n');

	fprintf(fid, '		  %%This is the footer for all pages except the last page of the table...\n');
	fprintf(fid, '	    \\multicolumn{7}{l}{{Continued on Next Page\\ldots}} \\\\ \n');
	fprintf(fid, '	  \\endfoot\n\n');
  
	fprintf(fid, '    	%%This is the footer for the last page of the table...\n');
	fprintf(fid, '	    \\hline\n');
	fprintf(fid, '		\\endlastfoot\n\n');

  for p = 1:length(indexes)
    underscoreLoc = find(indexes(p).name == '_');
    fprintf(fid, '\\textsl{%s} & ', [indexes(p).name(1 : underscoreLoc(1)-1) '\' indexes(p).name(underscoreLoc(1) : end)]);

    for r = 1:length(Responses)
      rank = indexes(p).ranks(r);
      if rank == 73                                             % for clarity, write '.' instead of '73' for parameters ranked last. 
        fprintf(fid, '. & ');
      else
        %fprintf(fid, '%3.4G(%1.0f) & ', rank, indexes(p).data(1,r));
        fprintf(fid, '%3.2f & ', indexes(p).data(1,r));
      end
    end
    fprintf(fid, '\\\\ \n');
    if mod(p, 4) == 0 & p ~= length(indexes)
      fprintf(fid, '\\hline \n');
    end
  end


	fprintf(fid, '\n\n  \\end{longtable}\n');  
  fprintf(fid, '  } \n');
  fprintf(fid, '  \\end{center}\n');
	fprintf(fid, '  \\end{landscape}\n');
  fclose(fid);
  %---------------------------



