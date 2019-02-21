package sim2d.cell.molecule;

import sim2d.cell.Cell;

public interface CD200R
{
	
	/**
	 * Whether this APC is expressing sufficient CD200R to affect a signal
	 */
	public boolean getExpressing_CD200R();
	public void receiveCD200RNegativeSignal();
}
