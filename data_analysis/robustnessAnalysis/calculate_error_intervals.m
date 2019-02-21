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
% Function is designed to calculate error intervals (relative to median simulation data) for simulation
% data at specified times. This information is required for drawing error bars in sim output graphs. 
%
% INPUTS
% 'data' - the median simulation run data
% 'allData' - matrix representing all sim output data in directory <runNum X row X column (response)>
% 'response' - index to column in the sim output data files
% 'errorXs' - times at which error intervals are needed, in days. This is a vector. 
%
% OUTPUTS
% 'errorYs' - vector (same length as errorXs) containing values around which the error intervals
%             relate
% 'errorLs' - vector of lower error bounds, relative to values in errorYs
% 'errorUs' - vector of upper error boudns, relative to values in errorYs
%
function [errorYs, errorLs, errorUs] = calculate_error_intervals(data,allData,response,errorXs)

errorYs = [];
errorLs = [];
errorUs = [];

for i = 1:length(errorXs)
  row = (errorXs(i) * 24) + 1;        % convert from days into hours 
  values = sort(allData(:,response,row));
  errorYs(i) = data(response,row);

  percentiles = prctile(values,[2.5,97.5]);  % caluate abs values at 2.5 and 97.5th percentiles
  lower = percentiles(1);
  upper = percentiles(2);
  lower - data(response,row);
  relLower = abs(lower - errorYs(i));   % convert abs values into relative values around median
  relUpper = abs(upper - errorYs(i));
  
  errorLs(i) = relLower; 
  errorUs(i) = relUpper;
end
