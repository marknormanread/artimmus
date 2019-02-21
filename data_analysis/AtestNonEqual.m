function A = AtestNonEqual(X,Y)
% Calculates the Vargha-Delaney A test, but from the Cliff Delta instead
% of matlab's ranksum functinos. 
% This calculation may not be the most efficient, but it is intuitive 
% (relatively speaking) and does not constrain X and Y to have the same
% number of samples. 

d = CliffDelta(X,Y);
A = (d + 1) / 2;