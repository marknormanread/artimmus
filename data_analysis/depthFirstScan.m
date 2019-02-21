function [params] = depthFirstScan(xml, indicator)
% entry point for the below function. It hides irrelevant details from the caller. 
  [params, unused] = depthFirstScanHelp([], xml, [], indicator);
end


function [params, result] = depthFirstScanHelp(path, xml, params, indicator)
% This function is recursive. 
% this function will scan through one of my simulation's parameter.xml structured files in a depth first manner. It attempts
% to locate fields with names matching the 'indicator'. If a parameter contains that field, then it saves all the data for that parameter. 
% This data is used to construct a latin hypercube sample. Returned, params is a one dimensional cell array. Each element
% contains: a cell array of strings indicating the path to the variable in the parameters.xml file; and a structure containing
% the default value, type, min and max values for use in constructing a LHC sample. The returned value 'Result' indicates to a calling level of
% recursion whether the current field needs to be logged in params. 
%
% indicator - this is the string that we are matching against at each recursion through the tree, indicating a record worth recording. 
    result = 0; 
    Fields = fieldnames(xml);                                 % get the field names of this level of the structure. 
    for i=1:length(Fields);                                   % going to look at each field name in turn
      if strcmp(Fields{i}, indicator)                       % if the field name corresponds to this special case (which is LHC specific)
        % record some stuff
        result = 1;      
        
      else                                                    % otherwise we should recurse down a level in the structure
        if isstruct(xml.(Fields{i}))                          % but first check that the next level down is actually a structure, not just data. 
          newpath = path;                                     % the path to the field,
          newpath{length(path)+1} = Fields{i};
          [params, record] = depthFirstScanHelp( newpath, xml.(Fields{i}), params, indicator );             % recurse down a level. 
          if record == 1                                      % if the last call returned indicating that this field needs to be recorded (because lhs_SA_min was found). 
            temp = {newpath, xml.(Fields{i}) };               % what will be the new item in the params cellarray
            params{end+1} = temp;                             % add the new field to the cellarray
          end 
        end
      end
    end 
end
