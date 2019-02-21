package sim2d.cell.impl;

import sim2d.TregSimulation;

/**
 * This is an abstract class from which different T cell polarizations inherit.
 * 
 * Once T cells become proliferative they adopt a polarization. The concrete polarization classes handle all polarization related functions for a T cell, such a molecule expressions, and cytokine secretions. 
 * @author mark
 *
 */
public abstract class Polarization 
{
	/**
	 * Monitors the current time of the simulation in relation to timers dictating the up- and dow-regulation of molecules, and handles the events that may transpire as a result of timers
	 * expiring. 
	 * @param simulation
	 */
	public abstract void updateMoleculeExpression(TregSimulation simulation);
	
	/**
	 *  Handles the secretion of cytokines relating to a particular polarization. 
	 */
	public abstract void secreteCytokines();
	
	protected CD4THelper Th;												// the CD4Thelper with which this polarization instance is associated. 
	
	/**
	 * Constructor permits the association of a polarization object with its owning CD4Thelper
	 * @param cd4Th
	 */
	protected Polarization(CD4THelper cd4Th)
	{
		Th = cd4Th;
	}
	
	/**
	 * Th cell adopts its polarization when it becomes proliferative, however this allows polarization
	 * to perform tasks when an effector state is adopted. 
	 */
	public abstract void becomeEffector();
}
