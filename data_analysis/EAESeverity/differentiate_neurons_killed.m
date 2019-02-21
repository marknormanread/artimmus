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


function Diffs = differentiate_neurons_killed(Data, SamplePoints, DiffHours)
% This function is intended for use with 'compileEAESeveritiesForRuns' (or some other EAE score calculation
% script). The EAE scoring mechanism requires the differential of the number of neurons killed in recent
% hours to be calculated. 

% This function will take:
% Data - a vector that represents the cumulative count of neurons killed over time, 
% SamplePoints - the times at which the samples for neurons killed over time were obtained at 
%     in practice this will just be every hour. 
% DiffHours - the period of time over which the differentiation is to be taken. 
%
% The function is simple, for each hour it takes the difference between the cumulative count of neurons killed
% at current time (since this is applied  over all times) from the cumulative count DiffHours ago. 
% 
% This can clearly cause problems for time = 0, and other low values, because the data at time -1 does not exist. 
% To accommodate this, the data is fleshed out with leading zeros. These zeros are deleted again later. 



% add some leading zeros to the start of these arrays. 
D = zeros(1,length(Data)+DiffHours);
D(DiffHours+1:end) = Data(:);
S = zeros(1,length(SamplePoints) + DiffHours);
S(DiffHours+1:end) = SamplePoints(:);

Diffs =zeros(1,length(D));
for i = DiffHours+1:length(D)
  difference = diff([ D(i-DiffHours) , D(i) ]);
  Diffs(i) = difference;
end

Diffs = Diffs (DiffHours+1:end);

