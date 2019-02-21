package sim2d.dataCollection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim2d.TregSimulation;
import sim2d.cell.Cell;
import sim2d.cell.impl.APC_Impl;
import sim2d.cell.impl.CD4THelper;
import sim2d.cell.impl.CD4Treg;
import sim2d.cell.impl.CD8Treg;
import sim2d.cell.impl.Neuron;
import sim2d.cell.impl.DendriticCell;
import sim2d.cell.impl.DendriticCellMigrates;
import sim2d.cell.impl.TCell_Impl;
import sim2d.cell.impl.Th1Polarization;
import sim2d.cell.impl.Th2Polarization;
import sim2d.compartment.CNS2D;


/**
 * Class collects data from a simulation run. It is designed to be run as a Steppable, alongside the simulation. It must be scheduled. When its step method is called
 * the class will query the TregSimulation object for its data, and compile the relevant bits into its datastructures. This class is used in compiling the median
 * data for a series of runs.
 * 
 * A recent modification (2009.1.04) had the sensitivityAnalysis program driver write the contents of these single run data files to the hard drive, rather than storing
 * them an attempting to compile the median data whilst the sensitivity analysis's runs were ongoing. It was feared that storing all this data within the JVM might be
 * using a tremendous quantity of memory. The new system will only compile the median data once the sensitivity analysis has completed, reading all teh single run data
 * files from the hard drive. As such, there is a static method at the bottom that will compile a class of this type from a string/file that contains the data. 
 * 
 * 
 * @author mark
 *
 */
public class SingleRunDataStore implements Steppable
{
	DataColumnDouble colTime = new DataColumnDouble("time_hours");
	
	DataColumnInteger colTotalCD4Th = new DataColumnInteger("total_CD4Th");
	DataColumnInteger colTotalCD4ThNaive = new DataColumnInteger("total_CD4ThNaive");
	DataColumnInteger colTotalCD4ThPartial = new DataColumnInteger("total_CD4ThPartial");
	DataColumnInteger colTotalCD4ThProliferating = new DataColumnInteger("total_CD4ThProliferating");
	DataColumnInteger colTotalCD4Th1 = new DataColumnInteger("total_CD4Th1");
	DataColumnInteger colTotalCD4Th2 = new DataColumnInteger("total_CD4Th2");
	DataColumnInteger colTotalCD4ThApoptotic = new DataColumnInteger("total_CD4ThApoptotic");
		
	DataColumnInteger colTotalCD4Treg = new DataColumnInteger("total_CD4Treg");
	DataColumnInteger colTotalCD4TregNaive = new DataColumnInteger("total_CD4TregNaive");
	DataColumnInteger colTotalCD4TregPartial = new DataColumnInteger("total_CD4TregPartial");
	DataColumnInteger colTotalCD4TregProliferating = new DataColumnInteger("total_CD4TregProliferating");
	DataColumnInteger colTotalCD4TregActivated = new DataColumnInteger("total_CD4TregActivated");
	DataColumnInteger colTotalCD4TregApoptotic = new DataColumnInteger("total_CD4TregApoptotic");
	
	DataColumnInteger colTotalCD8Treg = new DataColumnInteger("total_CD8Treg");
	DataColumnInteger colTotalCD8TregNaive = new DataColumnInteger("total_CD8TregNaive");
	DataColumnInteger colTotalCD8TregPartial = new DataColumnInteger("total_CD8TregPartial");
	DataColumnInteger colTotalCD8TregProliferating = new DataColumnInteger("total_CD8TregProliferating");
	DataColumnInteger colTotalCD8TregActivated = new DataColumnInteger("total_CD8TregActivated");
	DataColumnInteger colTotalCD8TregApoptotic = new DataColumnInteger("total_CD8TregApoptotic");
	
	DataColumnInteger colCNSAPC = new DataColumnInteger("cns_APC");
	DataColumnInteger colCNSAPCImmature = new DataColumnInteger("cns_APCImmature");
	DataColumnInteger colCNSAPCTolerogenic = new DataColumnInteger("cns_APCTolerogenic");
	DataColumnInteger colCNSAPCImmunogenic = new DataColumnInteger("cns_APCImmunogenic");
	DataColumnInteger colCNSAPCApoptotic = new DataColumnInteger("cns_APCApoptotic");
	
	DataColumnInteger colCLNDC = new DataColumnInteger("CLN_DC");
	DataColumnInteger colCLNDCImmature = new DataColumnInteger("CLN_DCImmature");
	DataColumnInteger colCLNDCTolerogenic = new DataColumnInteger("CLN_DCTolerogenic");
	DataColumnInteger colCLNDCImmunogenic = new DataColumnInteger("CLN_DCImmunogenic");
	DataColumnInteger colCLNDCApoptotic = new DataColumnInteger("CLN_DCApoptotic");
	
	DataColumnInteger colSLODC = new DataColumnInteger("SLO_DC");
	DataColumnInteger colSLODCImmature = new DataColumnInteger("SLO_DCImmature");
	DataColumnInteger colSLODCTolerogenic = new DataColumnInteger("SLO_DCTolerogenic");
	DataColumnInteger colSLODCImmunogenic = new DataColumnInteger("SLO_DCImmunogenic");
	DataColumnInteger colSLODCApoptotic = new DataColumnInteger("SLO_DCApoptotic");

	DataColumnInteger colCNSCD4Th1 = new DataColumnInteger("CNS_CD4Th1");
	DataColumnInteger colCNSCD4Th2 = new DataColumnInteger("CNS_CD4Th2");
	
	DataColumnDouble colCD4ThMedianSpecificity = new DataColumnDouble("CD4Th_MedianSpecificity");
	DataColumnDouble colCD4Th1MedianSpecificity = new DataColumnDouble("CD4Th1_MedianSpecificity");
	DataColumnDouble colCD4Th2MedianSpecificity = new DataColumnDouble("CD4Th2_MedianSpecificity");

	DataColumnInteger colCumulativeTh1Killed = new DataColumnInteger("cumulative_Th1Killed");
	
	DataColumnInteger colCLNDCPolarizationType1 = new DataColumnInteger("CLN_DC_Type1_Polarization");
	DataColumnInteger colCLNDCPolarizationType2 = new DataColumnInteger("CLN_DC_Type2_Polarization");
	
	DataColumnInteger colCumulativeCNSDCType1 = new DataColumnInteger("cumulative_CNSDC_type1");
	DataColumnInteger colCumulativeCNSDCType2 = new DataColumnInteger("cumulative_CNSDC_type2");
	
	DataColumnInteger colSpleenDC = new DataColumnInteger("Spleen_DC");
	DataColumnInteger colSpleenDCImmature = new DataColumnInteger("Spleen_DCImmature");
	DataColumnInteger colSpleenDCTolerogenic = new DataColumnInteger("Spleen_DCTolerogenic");
	DataColumnInteger colSpleenDCImmunogenic = new DataColumnInteger("Spleen_DCImmunogenic");
	DataColumnInteger colSpleenDCApoptotic = new DataColumnInteger("Spleen_DCApoptotic");
	
	DataColumnInteger colSpleenCD4TregTotal = new DataColumnInteger("Spleen_CD4TregTotal");
	DataColumnInteger colSpleenCD4TregEffector = new DataColumnInteger("Spleen_CD4TregEffector");
	DataColumnInteger colSpleenCD8TregTotal = new DataColumnInteger("Spleen_CD8TregTotal");
	DataColumnInteger colSpleenCD8TregEffector = new DataColumnInteger("Spleen_CD8TregEffector");
	DataColumnInteger colSpleenCD4Th1 = new DataColumnInteger("Spleen_CD4Th1");
	DataColumnInteger colSpleenCD4Th2 = new DataColumnInteger("Spleen_CD4Th2");
	
	DataColumnInteger colSpleenTh1Prolif = new DataColumnInteger("SpleenTh1Prolif");
	DataColumnInteger colSpleenTh2Prolif = new DataColumnInteger("SpleenTh2Prolif");
	DataColumnInteger colSpleenCD4TregProlif = new DataColumnInteger("SpleenCD4TregProlif");
	DataColumnInteger colSpleenCD8TregProlif = new DataColumnInteger("SpleenCD8TregProlif");
	
	DataColumnInteger colCLNTh1Prolif = new DataColumnInteger("CLN_Th1Prolif");
	DataColumnInteger colCLNTh2Prolif = new DataColumnInteger("CLN_Th2Prolif");
	DataColumnInteger colCLNCD4TregProlif = new DataColumnInteger("CLN_CD4TregProlif");
	DataColumnInteger colCLNCD8TregProlif = new DataColumnInteger("CLN_CD8TregProlif");
	
	DataColumnInteger colCLNTh1Effector = new DataColumnInteger("CLN_Th1Effector");
	DataColumnInteger colCLNTh2Effector = new DataColumnInteger("CLN_Th2Effector");
	DataColumnInteger colCLNCD4TregEffector = new DataColumnInteger("CLN_CD4TregEffector");
	DataColumnInteger colCLNCD8TregEffector = new DataColumnInteger("CLN_CD8TregEffector");
	
	DataColumnInteger colCumulativeTh1KilledCirculatory = new DataColumnInteger("cumulativeTh1KilledCirculatory");
	DataColumnInteger colCumulativeTh1KilledCLN = new DataColumnInteger("cumulativeTh1KilledCLN");
	DataColumnInteger colCumulativeTh1KilledCNS = new DataColumnInteger("cumulativeTh1KilledCNS");	
	DataColumnInteger colCumulativeTh1KilledSLO = new DataColumnInteger("cumulativeTh1KilledCLN");
	DataColumnInteger colCumulativeTh1KilledSpleen = new DataColumnInteger("cumulativeTh1KilledSpleen");
	
	DataColumnInteger colCD4ThPrimedCirculatory = new DataColumnInteger("CD4ThPrimedCirculatory");
	DataColumnInteger colCD4ThPrimedCLN = new DataColumnInteger("CD4ThPrimedCLN");
	DataColumnInteger colCD4ThPrimedCNS = new DataColumnInteger("CD4ThPrimedCNS");
	DataColumnInteger colCD4ThPrimedSLO = new DataColumnInteger("CD4ThPrimedSLO");
	DataColumnInteger colCD4ThPrimedSpleen = new DataColumnInteger("CD4ThPrimedSpleen");

	DataColumnInteger colCD4TregPrimedCirculatory = new DataColumnInteger("CD4TregPrimedCirculatory");
	DataColumnInteger colCD4TregPrimedCLN = new DataColumnInteger("CD4TregPrimedCLN");
	DataColumnInteger colCD4TregPrimedCNS = new DataColumnInteger("CD4TregPrimedCNS");
	DataColumnInteger colCD4TregPrimedSLO = new DataColumnInteger("CD4TregPrimedSLO");
	DataColumnInteger colCD4TregPrimedSpleen = new DataColumnInteger("CD4TregPrimedSpleen");
	
	DataColumnInteger colCD8TregPrimedCirculatory = new DataColumnInteger("CD8TregPrimedCirculatory");
	DataColumnInteger colCD8TregPrimedCLN = new DataColumnInteger("CD8TregPrimedCLN");
	DataColumnInteger colCD8TregPrimedCNS = new DataColumnInteger("CD8TregPrimedCNS");
	DataColumnInteger colCD8TregPrimedSLO = new DataColumnInteger("CD8TregPrimedSLO");
	DataColumnInteger colCD8TregPrimedSpleen = new DataColumnInteger("CD8TregPrimedSpleen");
	
	DataColumnInteger colNeuronsKilledCumulative = new DataColumnInteger("NeuronsKilledCumulative");
	
	DataColumnInteger colDCMPeptidePresentationMBP = new DataColumnInteger("DCMPeptidePresentationMBP");
	DataColumnInteger colDCMPeptidePresentationType1 = new DataColumnInteger("DCMPeptidePresentationType1");
	DataColumnInteger colDCMPeptidePresentationBoth = new DataColumnInteger("DCMPeptidePresentationBoth");
	DataColumnInteger colDCMPeptidePresentationNone = new DataColumnInteger("DCMPeptidePresentationNone");
	DataColumnInteger colDCMPeptidePresentationTotal = new DataColumnInteger("DCMPeptidePresentationTotal");
	
	private double time = 0.0;
	
	private int totalCD4Th = 0;
	private int totalCD4ThNaive = 0;
	private int totalCD4ThPartial = 0;
	private int totalCD4ThProliferating = 0;	
	private int totalCD4Th1 = 0;
	private int totalCD4Th2 = 0;
	private int totalCD4ThApoptotic = 0;

	private int totalCD4Treg = 0;
	private int totalCD4TregNaive = 0;
	private int totalCD4TregPartial = 0;
	private int totalCD4TregProliferating = 0;
	private int totalCD4TregActivated = 0;
	private int totalCD4TregApoptotic = 0;
	
	private int totalCD8Treg = 0;
	private int totalCD8TregNaive = 0;
	private int totalCD8TregPartial = 0;
	private int totalCD8TregProliferating = 0;
	private int totalCD8TregActivated = 0;
	private int totalCD8TregApoptotic = 0;
	
	private int cumulativeTh1Killed = 0;
	
	private int cnsAPC = 0;
	private int cnsAPCImmature = 0;
	private int cnsAPCTolerogenic = 0;
	private int cnsAPCImmunogenic = 0;
	private int cnsAPCApoptotic = 0;
	
	private int clnDC = 0;
	private int clnDCImmature = 0;
	private int clnDCTolerogenic = 0;
	private int clnDCImmunogenic = 0;
	private int clnDCApoptotic = 0;
	
	private int sloDC = 0;
	private int sloDCImmature = 0;
	private int sloDCTolerogenic = 0;
	private int sloDCImmunogenic = 0;
	private int sloDCApoptotic = 0;
	
	private int cnsCD4Th1 = 0;
	private int cnsCD4Th2 = 0;
		  
	MedianDataDouble allCD4ThSpecificities = new MedianDataDouble();
	MedianDataDouble allCD4Th1Specificities = new MedianDataDouble();
	MedianDataDouble allCD4Th2Specificities = new MedianDataDouble();	
	private double cd4ThSpecificities;
	private double cd4Th1Specificities;
	private double cd4Th2Specificities;
	
	private int clnDCPolarizationType1 = 0;
	private int clnDCPolarizationType2 = 0;
	
	private int cumulativeCNSDCType1 = 0;
	private int cumulativeCNSDCType2 = 0;
	
	private int spleenDC = 0;
	private int spleenDCImmature = 0;
	private int spleenDCTolerogenic = 0;
	private int spleenDCImmunogenic = 0;
	private int spleenDCApoptotic = 0;
	
	private int spleenCD4TregTotal = 0;
	private int spleenCD4TregEffector = 0;
	private int spleenCD8TregTotal = 0;
	private int spleenCD8TregEffector = 0;
	private int spleenCD4Th1 = 0;
	private int spleenCD4Th2 = 0;
	
	private int spleenTh1Prolif = 0;
	private int spleenTh2Prolif = 0;
	private int spleenCD4TregProlif = 0;
	private int spleenCD8TregProlif = 0;
	
	private int clnTh1Prolif = 0;
	private int clnTh2Prolif = 0;
	private int clnCD4TregProlif = 0;
	private int clnCD8TregProlif = 0;
	
	private int clnTh1Effector = 0;
	private int clnTh2Effector = 0;
	private int clnCD4TregEffector = 0;
	private int clnCD8TregEffector = 0;
	
	private int cumulativeTh1KilledCirculatory = 0;
	private int cumulativeTh1KilledCLN = 0;
	private int cumulativeTh1KilledCNS = 0;	
	private int cumulativeTh1KilledSLO = 0;
	private int cumulativeTh1KilledSpleen = 0;
	
	private int cd4ThPrimedCirculatory = 0;
	private int cd4ThPrimedCLN = 0;
	private int cd4ThPrimedCNS = 0;
	private int cd4ThPrimedSLO = 0;
	private int cd4ThPrimedSpleen = 0;

	private int cd4TregPrimedCirculatory = 0;
	private int cd4TregPrimedCLN = 0;
	private int cd4TregPrimedCNS = 0;
	private int cd4TregPrimedSLO = 0;
	private int cd4TregPrimedSpleen = 0;
	
	private int cd8TregPrimedCirculatory = 0;
	private int cd8TregPrimedCLN = 0;
	private int cd8TregPrimedCNS = 0;
	private int cd8TregPrimedSLO = 0;
	private int cd8TregPrimedSpleen = 0;
	
	private int neuronsKilledCumulative = 0;	
	
	private int dcmPeptidePresentationMBP = 0;
	private int dcmPeptidePresentationType1 = 0;
	private int dcmPeptidePresentationBoth = 0;
	private int dcmPeptidePresentationNone = 0;
	private int dcmPeptidePresentationTotal = 0;
	
	private String tableKey = "";
	
	public SingleRunDataStore()
	{	}
	
	public void step(SimState state)
	{
		resetValues();
		
		TregSimulation sim = (TregSimulation) state;
		
		time = sim.schedule.getTime();						// the time in hours.
		
		Collection<Cell> circulationCells = sim.circulation.getAllCells();
		Collection<Cell> cnsCells = sim.cns.getAllCells();
		Collection<Cell> clnCells = sim.cln.getAllCells();
		Collection<Cell> sloCells = sim.slo.getAllCells();
		Collection<Cell> spleenCells = sim.spleen.getAllCells();
					
		
		Collection<Cell> allCells = new ArrayList<Cell>();
		allCells.addAll(circulationCells);
		allCells.addAll(cnsCells);
		allCells.addAll(clnCells);
		allCells.addAll(sloCells);
		allCells.addAll(spleenCells);
		
		// log the values for this timestep
		countTotalCells(allCells);
		
		// counting cells pertaining to a specific compartment
		countCLN(clnCells);
		countCNS(cnsCells);
		countSLO(sloCells);
		countSpleen(spleenCells);
		
		// log median specificity of all cells in the simulation
		countCD4ThSpecificities(allCells);							// log the specificities of all CD4Th cells in the system.
		
		cumulativeTh1Killed = CD8Treg.cd4Th1ApopDL.getApoptosisedTotal();
		cumulativeTh1KilledCirculatory = CD8Treg.cd4Th1ApopDL.getApoptosisedCirculatory();
		cumulativeTh1KilledCLN = CD8Treg.cd4Th1ApopDL.getApoptosisedCLN();
		cumulativeTh1KilledCNS = CD8Treg.cd4Th1ApopDL.getApoptosisedCNS();
		cumulativeTh1KilledSLO = CD8Treg.cd4Th1ApopDL.getApoptosisedSLO();
		cumulativeTh1KilledSpleen = CD8Treg.cd4Th1ApopDL.getApoptosisedSpleen();
		
		cumulativeCNSDCType1 = CNS2D.cumulativeCNSDCType1Polarized;
		cumulativeCNSDCType2 = CNS2D.cumulativeCNSDCType2Polarized;
		
		cd4ThPrimedCirculatory = TCell_Impl.primingDL.getCD4ThCirculation();
		cd4ThPrimedCLN = TCell_Impl.primingDL.getCD4ThCLN();
		cd4ThPrimedCNS = TCell_Impl.primingDL.getCD4ThCNS();
		cd4ThPrimedSLO = TCell_Impl.primingDL.getCD4ThSLO();
		cd4ThPrimedSpleen = TCell_Impl.primingDL.getCD4ThSpleen();

		cd4TregPrimedCirculatory = TCell_Impl.primingDL.getCD4TregCirculation();
		cd4TregPrimedCLN = TCell_Impl.primingDL.getCD4TregCLN();
		cd4TregPrimedCNS = TCell_Impl.primingDL.getCD4TregCNS();
		cd4TregPrimedSLO = TCell_Impl.primingDL.getCD4TregSLO();
		cd4TregPrimedSpleen = TCell_Impl.primingDL.getCD4TregSpleen();
		
		cd8TregPrimedCirculatory = TCell_Impl.primingDL.getCD8TregCirculation();
		cd8TregPrimedCLN = TCell_Impl.primingDL.getCD8TregCLN();
		cd8TregPrimedCNS = TCell_Impl.primingDL.getCD8TregCNS();
		cd8TregPrimedSLO = TCell_Impl.primingDL.getCD8TregSLO();
		cd8TregPrimedSpleen = TCell_Impl.primingDL.getCD8TregSpleen();	
		
		neuronsKilledCumulative = Neuron.neuronsKilledDL.getCumulativeCountNeuronsApoptosised();
		
		dcmPeptidePresentationMBP = DendriticCellMigrates.peptidePresentationDL.getApoptosedMBPTotal();
		dcmPeptidePresentationType1 = DendriticCellMigrates.peptidePresentationDL.getApoptosedType1Total();
		dcmPeptidePresentationBoth = DendriticCellMigrates.peptidePresentationDL.getApoptosedBothTotal();
		dcmPeptidePresentationNone = DendriticCellMigrates.peptidePresentationDL.getApoptosedNullTotal();
		dcmPeptidePresentationTotal = DendriticCellMigrates.peptidePresentationDL.getApoptosedDCTotal();
		
		storeData();												// log the values into the data stores. 
	}
	
	/**
	 * Handles the median specificity values for CD4Th, CD4Th1, and CD4Th2 cells in the simulation (across all compartments)
	 */
	private void countCD4ThSpecificities(Collection<Cell> cells)
	{
	                                       
		for(Cell cell : cells)
		{
			if(cell instanceof CD4THelper)
			{
				CD4THelper cd4Th = (CD4THelper) cell;
				
				if(cd4Th.getPolarization() instanceof Th1Polarization)
				{
					allCD4Th1Specificities.logValue(cd4Th.getSpecificity());		// log value for all CD4Th1 cells, only those that are effectors.
				} else if(cd4Th.getPolarization() instanceof Th2Polarization)
				{
					allCD4Th2Specificities.logValue(cd4Th.getSpecificity());		// log value for all CD4Th2 cell, only those that are effectors.
				} else if( (cd4Th.getMaturity() != TCell_Impl.Maturity.Effector)
							&& cd4Th.getMaturity() != TCell_Impl.Maturity.Apoptotic
						) 
				{
						// if the CD4Th cell is not mature or apoptotic
					allCD4ThSpecificities.logValue(cd4Th.getSpecificity());			// log value for all CD4Th cells that are not mature or apoptotic
				}
			}
		}
		
		// safety, it is possible that there were no CD4Th1 or Th2 cells, so we add some '0' values to prevent crashes elsewhere. 
		if(allCD4ThSpecificities.getQuantityValuesLogged() == 0)
			allCD4ThSpecificities.logValue(0.0);
		if(allCD4Th1Specificities.getQuantityValuesLogged() == 0)
			allCD4Th1Specificities.logValue(0.0);
		if(allCD4Th2Specificities.getQuantityValuesLogged() == 0)
			allCD4Th2Specificities.logValue(0.0);
		
		cd4ThSpecificities = allCD4ThSpecificities.findMedian();
		cd4Th1Specificities = allCD4Th1Specificities.findMedian();
		cd4Th2Specificities = allCD4Th2Specificities.findMedian();
	}
	
	
	
	private void countCLN(Collection<Cell> compartmentCells)
	{
		for(Cell cell : compartmentCells)
		{
			if(cell instanceof DendriticCell)
			{
				DendriticCell dc = (DendriticCell) cell;
				clnDC ++;
				
				if(dc.isImmature())
					clnDCImmature ++;
				else if (dc.isExpressing_MHCPeptide() && dc.isTolerogenic())
					clnDCTolerogenic ++;
				else if (dc.isExpressing_MHCPeptide() && dc.isImmunogenic())
					clnDCImmunogenic ++;
				else if (dc.isApoptotic())
					clnDCApoptotic ++;
				
				if(dc.isExpressing_MHCPeptide())
				{
					if(dc.getPolarization() == DendriticCell.Polarization.Type1)
						clnDCPolarizationType1 ++;
					else if (dc.getPolarization() == DendriticCell.Polarization.Type2)
						clnDCPolarizationType2 ++;
				}
			}
			
			if(cell instanceof TCell_Impl)
			{
				if(cell instanceof CD8Treg)
				{
					final CD8Treg treg = (CD8Treg) cell;
					
					if(treg.getMaturity() == TCell_Impl.Maturity.Proliferating)
						clnCD8TregProlif++;
					
					if(treg.getMaturity() == TCell_Impl.Maturity.Effector)
						clnCD8TregEffector ++;
				} else if(cell instanceof CD4Treg) 
				{
					final CD4Treg treg = (CD4Treg) cell;
					
					if(treg.getMaturity() == TCell_Impl.Maturity.Proliferating)
						clnCD4TregProlif++;
					
					if(treg.getMaturity() == TCell_Impl.Maturity.Effector)
						clnCD4TregEffector ++;					
				} else if (cell instanceof CD4THelper)
				{
					final CD4THelper th = (CD4THelper) cell;
					
					if(th.getMaturity() == TCell_Impl.Maturity.Proliferating)
					{
						if(th.getPolarization() instanceof Th1Polarization)
							clnTh1Prolif ++;
						else if(th.getPolarization() instanceof Th2Polarization)
							clnTh2Prolif ++;
					}
					if(th.getMaturity() == TCell_Impl.Maturity.Effector)
					{
						if(th.getPolarization() instanceof Th1Polarization)
							clnTh1Effector ++;
						else if(th.getPolarization() instanceof Th2Polarization)
							clnTh2Effector ++;
					}
				}
			}
		}
	}
	
	/**
	 * Count cells and their states that are specific to the CNS compartment. 
	 */
	private void countCNS(Collection<Cell> compartmentCells)
	{
		for(Cell cell : compartmentCells)
		{
			if(cell instanceof APC_Impl)
			{
				APC_Impl apc = (APC_Impl) cell;
				cnsAPC ++;
				
				if(apc.isImmature())
					cnsAPCImmature ++;
				else if(apc.isTolerogenic() && apc.isExpressing_MHCPeptide())
					cnsAPCTolerogenic ++;
				else if(apc.isImmunogenic() && apc.isExpressing_MHCPeptide())
					cnsAPCImmunogenic ++;
				else if(apc.isApoptotic())
					cnsAPCApoptotic ++;
			}
			else if(cell instanceof CD4THelper)
			{
				CD4THelper tCell = (CD4THelper) cell;
				if(tCell.getMaturity() == TCell_Impl.Maturity.Effector)
				{
					if (tCell.getPolarization() instanceof Th1Polarization)
						cnsCD4Th1++;
					else if (tCell.getPolarization() instanceof Th2Polarization)
						cnsCD4Th2++;
				}
			}
		}
	}
	
	/**
	 * Count cells and their states that are specific to the SLO compartment. 
	 */
	private void countSLO(Collection<Cell> compartmentCells)
	{
		for(Cell cell : compartmentCells)
		{
			if(cell instanceof DendriticCell)
			{
				DendriticCell dc = (DendriticCell) cell;
				sloDC ++;
				
				if(dc.isImmature())
					sloDCImmature ++;
				else if(dc.isTolerogenic() && dc.isExpressing_MHCPeptide())
					sloDCTolerogenic ++;
				else if(dc.isImmunogenic() && dc.isExpressing_MHCPeptide())
					sloDCImmunogenic ++;
				else if(dc.isApoptotic())
					sloDCApoptotic ++;
			}
		}
	}
	/**
	 * Count cells and their states that are specific to the Spleen compartment. 
	 */
	private void countSpleen(Collection<Cell> compartmentCells)
	{
		for(Cell cell : compartmentCells)
		{
			if(cell instanceof DendriticCell)
			{
				DendriticCell dc = (DendriticCell) cell;
				spleenDC ++;
				
				if(dc.isImmature())
					spleenDCImmature ++;
				else if(dc.isTolerogenic() && dc.isExpressing_MHCPeptide())
					spleenDCTolerogenic ++;
				else if(dc.isImmunogenic() && dc.isExpressing_MHCPeptide())
					spleenDCImmunogenic ++;
				else if(dc.isApoptotic())
					spleenDCApoptotic ++;
			}
			
			if(cell instanceof TCell_Impl)
			{
				if(cell instanceof CD8Treg)
				{
					final CD8Treg treg = (CD8Treg) cell;
					spleenCD8TregTotal ++;
					
					if(treg.getMaturity() == TCell_Impl.Maturity.Proliferating)
						spleenCD8TregProlif++;
					
					if(treg.getMaturity() == TCell_Impl.Maturity.Effector)
						spleenCD8TregEffector ++;
				} else if(cell instanceof CD4Treg) 
				{
					final CD4Treg treg = (CD4Treg) cell;
					spleenCD4TregTotal ++;
					
					if(treg.getMaturity() == TCell_Impl.Maturity.Proliferating)
						spleenCD4TregProlif++;
					
					if(treg.getMaturity() == TCell_Impl.Maturity.Effector)
						spleenCD4TregEffector ++;					
				} else if (cell instanceof CD4THelper)
				{
					final CD4THelper th = (CD4THelper) cell;
					
					if(th.getMaturity() == TCell_Impl.Maturity.Proliferating)
					{
						if(th.getPolarization() instanceof Th1Polarization)
							spleenTh1Prolif ++;
						else if(th.getPolarization() instanceof Th2Polarization)
							spleenTh2Prolif ++;
					}
					if(th.getMaturity() == TCell_Impl.Maturity.Effector)
					{
						if(th.getPolarization() instanceof Th1Polarization)
							spleenCD4Th1 ++;
						else if(th.getPolarization() instanceof Th2Polarization)
							spleenCD4Th2 ++;
					}
				}
			}
		}
	}
	
	
	/**
	 * Method takes all the cells in *THE SIMULATION* and tallies up their total numbers, states, ect, across all compartments (and this is done in one go).
	 */
	private void countTotalCells(Collection<Cell> allCells)
	{
		for (Cell cell : allCells)
		{
			if(cell instanceof CD4THelper)
			{				
				CD4THelper tcell = (CD4THelper)cell;
				totalCD4Th ++;
				switch(tcell.getMaturity())								// log the cell's state of activation at a population level.
				{
					case Naive:
						totalCD4ThNaive ++;
						break;
					case Partial:
						totalCD4ThPartial ++;
						break;
					case Proliferating:
						totalCD4ThProliferating ++;
						break;
					case Effector:
						if(tcell.getPolarization() instanceof Th1Polarization)						
							totalCD4Th1++;
						else if (tcell.getPolarization() instanceof Th2Polarization ) 
							totalCD4Th2++;						
						break;
					case Apoptotic:
						totalCD4ThApoptotic ++;
						break;
				}					
			}
			
			if(cell instanceof CD4Treg)
			{
				totalCD4Treg ++;										// log the presence of this cell in the population totals
				
				TCell_Impl tcell = (TCell_Impl)cell;
				switch(tcell.getMaturity())								// log the cell's state of activation at a population level.
				{
					case Naive:
						totalCD4TregNaive ++;
						break;
					case Partial:
						totalCD4TregPartial ++;
						break;
					case Proliferating:
						totalCD4TregProliferating ++;
						break;
					case Effector:
						totalCD4TregActivated ++;
						break;
					case Apoptotic:
						totalCD4TregApoptotic ++;
						break;
				}					
			}
			
			if(cell instanceof CD8Treg)
			{
				totalCD8Treg ++;										// log the presence of this cell in the population totals
				
				TCell_Impl tcell = (TCell_Impl)cell;
				switch(tcell.getMaturity())								// log the cell's state of activation at a population level.
				{
					case Naive:
						totalCD8TregNaive ++;
						break;
					case Partial:
						totalCD8TregPartial ++;
						break;
					case Proliferating:
						totalCD8TregProliferating ++;
						break;
					case Effector:
						totalCD8TregActivated ++;
						break;
					case Apoptotic:
						totalCD8TregApoptotic ++;
						break;
				}					
			}
		}
	}
	
	private void storeData()
	{
		colTime.logValue(time);
		
		colTotalCD4Th.logValue(totalCD4Th);
		colTotalCD4ThNaive.logValue(totalCD4ThNaive);
		colTotalCD4ThPartial.logValue(totalCD4ThPartial);
		colTotalCD4ThProliferating.logValue(totalCD4ThProliferating);
		colTotalCD4Th1.logValue(totalCD4Th1);
		colTotalCD4Th2.logValue(totalCD4Th2);
		colTotalCD4ThApoptotic.logValue(totalCD4ThApoptotic);
		
		colTotalCD4Treg.logValue(totalCD4Treg);
		colTotalCD4TregNaive.logValue(totalCD4TregNaive);
		colTotalCD4TregPartial.logValue(totalCD4TregPartial);
		colTotalCD4TregProliferating.logValue(totalCD4TregProliferating);
		colTotalCD4TregActivated.logValue(totalCD4TregActivated);
		colTotalCD4TregApoptotic.logValue(totalCD4TregApoptotic);
		
		colTotalCD8Treg.logValue(totalCD8Treg);
		colTotalCD8TregNaive.logValue(totalCD8TregNaive);
		colTotalCD8TregPartial.logValue(totalCD8TregPartial);
		colTotalCD8TregProliferating.logValue(totalCD8TregProliferating);
		colTotalCD8TregActivated.logValue(totalCD8TregActivated);
		colTotalCD8TregApoptotic.logValue(totalCD8TregApoptotic);
		
		colCNSAPC.logValue(cnsAPC);
		colCNSAPCImmature.logValue(cnsAPCImmature);
		colCNSAPCTolerogenic.logValue(cnsAPCTolerogenic);
		colCNSAPCImmunogenic.logValue(cnsAPCImmunogenic);
		colCNSAPCApoptotic.logValue(cnsAPCApoptotic);
		
		colCLNDC.logValue(clnDC);
		colCLNDCImmature.logValue(clnDCImmature);
		colCLNDCTolerogenic.logValue(clnDCTolerogenic);
		colCLNDCImmunogenic.logValue(clnDCImmunogenic);
		colCLNDCApoptotic.logValue(clnDCApoptotic);
		
		colSLODC.logValue(sloDC);
		colSLODCImmature.logValue(sloDCImmature);
		colSLODCTolerogenic.logValue(sloDCTolerogenic);
		colSLODCImmunogenic.logValue(sloDCImmunogenic);
		colSLODCApoptotic.logValue(sloDCApoptotic);
		
		colCNSCD4Th1.logValue(cnsCD4Th1);
		colCNSCD4Th2.logValue(cnsCD4Th2);
		
		colCD4ThMedianSpecificity.logValue(cd4ThSpecificities);
		colCD4Th1MedianSpecificity.logValue(cd4Th1Specificities);
		colCD4Th2MedianSpecificity.logValue(cd4Th2Specificities);
		
		colCumulativeTh1Killed.logValue(cumulativeTh1Killed);
		
		colCLNDCPolarizationType1.logValue(clnDCPolarizationType1);
		colCLNDCPolarizationType2.logValue(clnDCPolarizationType2);
		
		colCumulativeCNSDCType1.logValue(cumulativeCNSDCType1);
		colCumulativeCNSDCType2.logValue(cumulativeCNSDCType2);
		
		colSpleenDC.logValue(spleenDC);
		colSpleenDCImmature.logValue(spleenDCImmature);
		colSpleenDCTolerogenic.logValue(spleenDCTolerogenic);
		colSpleenDCImmunogenic.logValue(spleenDCImmunogenic);
		colSpleenDCApoptotic.logValue(spleenDCApoptotic);
		
		colSpleenCD4TregTotal.logValue(spleenCD4TregTotal);
		colSpleenCD4TregEffector.logValue(spleenCD4TregEffector);
		colSpleenCD8TregTotal.logValue(spleenCD8TregTotal);
		colSpleenCD8TregEffector.logValue(spleenCD8TregEffector);
		colSpleenCD4Th1.logValue(spleenCD4Th1);
		colSpleenCD4Th2.logValue(spleenCD4Th2);
	
		colSpleenCD4TregProlif.logValue(spleenCD4TregProlif);
		colSpleenCD8TregProlif.logValue(spleenCD8TregProlif);
		colSpleenTh1Prolif.logValue(spleenTh1Prolif);
		colSpleenTh2Prolif.logValue(spleenTh2Prolif);
		
		colCLNCD4TregProlif.logValue(clnCD4TregProlif);
		colCLNCD8TregProlif.logValue(clnCD8TregProlif);
		colCLNTh1Prolif.logValue(clnTh1Prolif);
		colCLNTh2Prolif.logValue(clnTh2Prolif);
		
		colCLNCD4TregEffector.logValue(clnCD4TregEffector);
		colCLNCD8TregEffector.logValue(clnCD8TregEffector);
		colCLNTh1Effector.logValue(clnTh1Effector);
		colCLNTh2Effector.logValue(clnTh2Effector);
		
		colCumulativeTh1KilledCirculatory.logValue(cumulativeTh1KilledCirculatory);
		colCumulativeTh1KilledCLN.logValue(cumulativeTh1KilledCLN);
		colCumulativeTh1KilledCNS.logValue(cumulativeTh1KilledCNS);		
		colCumulativeTh1KilledSLO.logValue(cumulativeTh1KilledSLO);
		colCumulativeTh1KilledSpleen.logValue(cumulativeTh1KilledSpleen);
		
		colCD4ThPrimedCirculatory.logValue(cd4ThPrimedCirculatory);
		colCD4ThPrimedCLN.logValue(cd4ThPrimedCLN);
		colCD4ThPrimedCNS.logValue(cd4ThPrimedCNS);
		colCD4ThPrimedSLO.logValue(cd4ThPrimedSLO);
		colCD4ThPrimedSpleen.logValue(cd4ThPrimedSpleen);
		
		colCD4TregPrimedCirculatory.logValue(cd4TregPrimedCirculatory);
		colCD4TregPrimedCLN.logValue(cd4TregPrimedCLN);
		colCD4TregPrimedCNS.logValue(cd4TregPrimedCNS);
		colCD4TregPrimedSLO.logValue(cd4TregPrimedSLO);
		colCD4TregPrimedSpleen.logValue(cd4TregPrimedSpleen);

		colCD8TregPrimedCirculatory.logValue(cd8TregPrimedCirculatory);
		colCD8TregPrimedCLN.logValue(cd8TregPrimedCLN);
		colCD8TregPrimedCNS.logValue(cd8TregPrimedCNS);
		colCD8TregPrimedSLO.logValue(cd8TregPrimedSLO);
		colCD8TregPrimedSpleen.logValue(cd8TregPrimedSpleen);
		
		colNeuronsKilledCumulative.logValue(neuronsKilledCumulative);
		
		colDCMPeptidePresentationMBP.logValue(dcmPeptidePresentationMBP);
		colDCMPeptidePresentationType1.logValue(dcmPeptidePresentationType1);
		colDCMPeptidePresentationBoth.logValue(dcmPeptidePresentationBoth);
		colDCMPeptidePresentationNone.logValue(dcmPeptidePresentationNone);
		colDCMPeptidePresentationTotal.logValue(dcmPeptidePresentationTotal);
	}
	
	
	/**
	 * Set all the values back to zero in preparation for the next logging of simulation data.
	 */
	private void resetValues()
	{
		time = 0.0;
		
		totalCD4Th = 0;
		totalCD4ThNaive = 0;
		totalCD4ThPartial = 0;
		totalCD4ThProliferating = 0;
		totalCD4Th1 = 0;
		totalCD4Th2 = 0;
		totalCD4ThApoptotic = 0;

		totalCD4Treg = 0;
		totalCD4TregNaive = 0;
		totalCD4TregPartial = 0;
		totalCD4TregProliferating = 0;
		totalCD4TregActivated = 0;
		totalCD4TregApoptotic = 0;
		
		totalCD8Treg = 0;
		totalCD8TregNaive = 0;
		totalCD8TregPartial = 0;
		totalCD8TregProliferating = 0;
		totalCD8TregActivated = 0;
		totalCD8TregApoptotic = 0;
		
		cumulativeTh1Killed = 0;
		
		cnsAPC = 0;
		cnsAPCImmature = 0;
		cnsAPCTolerogenic = 0;
		cnsAPCImmunogenic = 0;
		cnsAPCApoptotic = 0;
		
		clnDC = 0;
		clnDCImmature = 0;
		clnDCTolerogenic = 0;
		clnDCImmunogenic = 0;
		clnDCApoptotic = 0;
		
		sloDC = 0;
		sloDCImmature = 0;
		sloDCTolerogenic = 0;
		sloDCImmunogenic = 0;
		sloDCApoptotic = 0;
		
		cnsCD4Th1 = 0;
		cnsCD4Th2 = 0;
		
		allCD4ThSpecificities = new MedianDataDouble();
		allCD4Th1Specificities = new MedianDataDouble();
		allCD4Th2Specificities = new MedianDataDouble();
		cd4ThSpecificities = 0.0;
		cd4Th1Specificities = 0.0;
		cd4Th2Specificities = 0.0;
		
		clnDCPolarizationType1 = 0;
		clnDCPolarizationType2 = 0;
		
		cumulativeCNSDCType1 = 0;
		cumulativeCNSDCType2 = 0;
		
		spleenDC = 0;
		spleenDCImmature = 0;
		spleenDCTolerogenic = 0;
		spleenDCImmunogenic = 0;
		spleenDCApoptotic = 0;
		
		spleenCD4TregTotal = 0;
		spleenCD4TregEffector = 0;
		spleenCD8TregTotal = 0;
		spleenCD8TregEffector = 0;
		spleenCD4Th1 = 0;
		spleenCD4Th2 = 0;
		
		spleenTh1Prolif = 0;
		spleenTh2Prolif = 0;
		spleenCD4TregProlif = 0;
		spleenCD8TregProlif = 0;
		
		clnTh1Prolif = 0;
		clnTh2Prolif = 0;
		clnCD4TregProlif = 0;
		clnCD8TregProlif = 0;
		
		clnTh1Effector = 0;
		clnTh2Effector = 0;
		clnCD4TregEffector = 0;
		clnCD8TregEffector = 0;
		
		cumulativeTh1KilledCirculatory = 0;
		cumulativeTh1KilledCLN = 0;
		cumulativeTh1KilledCNS = 0;		
		cumulativeTh1KilledSLO = 0;
		cumulativeTh1KilledSpleen = 0;
		
		cd4ThPrimedCirculatory = 0;
		cd4ThPrimedCLN = 0;
		cd4ThPrimedCNS = 0;
		cd4ThPrimedSLO = 0;
		cd4ThPrimedSpleen = 0;

		cd4TregPrimedCirculatory = 0;
		cd4TregPrimedCLN = 0;
		cd4TregPrimedCNS = 0;
		cd4TregPrimedSLO = 0;
		cd4TregPrimedSpleen = 0;
		
		cd8TregPrimedCirculatory = 0;
		cd8TregPrimedCLN = 0;
		cd8TregPrimedCNS = 0;
		cd8TregPrimedSLO = 0;
		cd8TregPrimedSpleen = 0;	
		
		neuronsKilledCumulative = 0;
		
		dcmPeptidePresentationMBP = 0;
		dcmPeptidePresentationType1 = 0;
		dcmPeptidePresentationBoth = 0;
		dcmPeptidePresentationNone = 0;
		dcmPeptidePresentationTotal = 0;
	}
	
	
	public String compileTableToString()
	{
		StringBuilder output = new StringBuilder();
		StringBuilder key = new StringBuilder();
		
		Iterator<Double> timeIter = colTime.getIterator();											key.append(colTime.getTitle() + " ");
		
		Iterator<Integer> totalCD4ThIter = colTotalCD4Th.getIterator();								key.append(colTotalCD4Th.getTitle() + " ");
		Iterator<Integer> totalCD4ThNaiveIter = colTotalCD4ThNaive.getIterator();					key.append(colTotalCD4ThNaive.getTitle() + " ");
		Iterator<Integer> totalCD4ThPartialIter = colTotalCD4ThPartial.getIterator();				key.append(colTotalCD4ThPartial.getTitle() + " ");
		Iterator<Integer> totalCD4ThProliferatingIter = colTotalCD4ThProliferating.getIterator();	key.append(colTotalCD4ThProliferating.getTitle() + " ");
		Iterator<Integer> totalCD4Th1Iter = colTotalCD4Th1.getIterator();							key.append(colTotalCD4Th1.getTitle() + " ");
		Iterator<Integer> totalCD4Th2Iter = colTotalCD4Th2.getIterator();							key.append(colTotalCD4Th2.getTitle() + " ");
		Iterator<Integer> totalCD4ThApoptoticIter = colTotalCD4ThApoptotic.getIterator();			key.append(colTotalCD4ThApoptotic.getTitle() + " ");
		
		Iterator<Integer> totalCD4TregIter = colTotalCD4Treg.getIterator();							key.append(colTotalCD4Treg.getTitle() + " ");
		Iterator<Integer> totalCD4TregNaiveIter = colTotalCD4TregNaive.getIterator();				key.append(colTotalCD4TregNaive.getTitle() + " ");
		Iterator<Integer> totalCD4TregPartialIter = colTotalCD4TregPartial.getIterator();			key.append(colTotalCD4TregPartial.getTitle() + " ");
		Iterator<Integer> totalCD4TregProliferatingIter = colTotalCD4TregProliferating.getIterator();key.append(colTotalCD4TregProliferating.getTitle() + " ");
		Iterator<Integer> totalCD4TregActivatedIter = colTotalCD4TregActivated.getIterator();		key.append(colTotalCD4TregActivated.getTitle() + " ");
		Iterator<Integer> totalCD4TregApoptoticIter = colTotalCD4TregApoptotic.getIterator();		key.append(colTotalCD4TregApoptotic.getTitle() + " ");
		
		Iterator<Integer> totalCD8TregIter = colTotalCD8Treg.getIterator();							key.append(colTotalCD8Treg.getTitle() + " ");
		Iterator<Integer> totalCD8TregNaiveIter = colTotalCD8TregNaive.getIterator();				key.append(colTotalCD8TregNaive.getTitle() + " ");
		Iterator<Integer> totalCD8TregPartialIter = colTotalCD8TregPartial.getIterator();			key.append(colTotalCD8TregPartial.getTitle() + " ");
		Iterator<Integer> totalCD8TregProliferatingIter = colTotalCD8TregProliferating.getIterator();key.append(colTotalCD8TregProliferating.getTitle() + " ");
		Iterator<Integer> totalCD8TregActivatedIter = colTotalCD8TregActivated.getIterator();		key.append(colTotalCD8TregActivated.getTitle() + " ");
		Iterator<Integer> totalCD8TregApoptoticIter = colTotalCD8TregApoptotic.getIterator();		key.append(colTotalCD8TregApoptotic.getTitle() + " ");
		
		Iterator<Integer> cnsAPCIter = colCNSAPC.getIterator();										key.append(colCNSAPC.getTitle() + " ");
		Iterator<Integer> cnsAPCImmatureIter = colCNSAPCImmature.getIterator();						key.append(colCNSAPCImmature.getTitle() + " ");
		Iterator<Integer> cnsAPCTolerogenicIter = colCNSAPCTolerogenic.getIterator();				key.append(colCNSAPCTolerogenic.getTitle() + " ");
		Iterator<Integer> cnsAPCImmunogenicIter = colCNSAPCImmunogenic.getIterator();				key.append(colCNSAPCImmunogenic.getTitle() + " ");
		Iterator<Integer> cnsAPCApoptoticIter = colCNSAPCApoptotic.getIterator();					key.append(colCNSAPCApoptotic.getTitle() + " ");
		
		Iterator<Integer> clnDCIter = colCLNDC.getIterator();										key.append(colCLNDC.getTitle() + " ");
		Iterator<Integer> clnDCImmatureIter = colCLNDCImmature.getIterator();						key.append(colCLNDCImmature.getTitle() + " ");
		Iterator<Integer> clnDCTolerogenicIter = colCLNDCTolerogenic.getIterator();					key.append(colCLNDCTolerogenic.getTitle() + " ");
		Iterator<Integer> clnDCImmunogenicIter = colCLNDCImmunogenic.getIterator();					key.append(colCLNDCImmunogenic.getTitle() + " ");
		Iterator<Integer> clnDCApoptoticIter = colCLNDCApoptotic.getIterator();						key.append(colCLNDCApoptotic.getTitle() + " ");
		
		Iterator<Integer> sloDCIter = colSLODC.getIterator();										key.append(colSLODC.getTitle() + " ");
		Iterator<Integer> sloDCImmatureIter = colSLODCImmature.getIterator();						key.append(colSLODCImmature.getTitle() + " ");
		Iterator<Integer> sloDCTolerogenicIter = colSLODCTolerogenic.getIterator();					key.append(colSLODCTolerogenic.getTitle() + " ");
		Iterator<Integer> sloDCImmunogenicIter = colSLODCImmunogenic.getIterator();					key.append(colSLODCImmunogenic.getTitle() + " ");
		Iterator<Integer> sloDCApoptoticIter = colSLODCApoptotic.getIterator();						key.append(colSLODCApoptotic.getTitle() + " ");
		
		Iterator<Integer> cnsCD4Th1Iter = colCNSCD4Th1.getIterator();								key.append(colCNSCD4Th1.getTitle() + " ");								
		Iterator<Integer> cnsCD4Th2Iter = colCNSCD4Th2.getIterator();								key.append(colCNSCD4Th2.getTitle() + " ");
		
		Iterator<Double> allCD4ThMedianSpecificityIter = colCD4ThMedianSpecificity.getIterator();	key.append(colCD4ThMedianSpecificity.getTitle() + " ");
		Iterator<Double> allCD4Th1MedianSpecificityIter = colCD4Th1MedianSpecificity.getIterator();	key.append(colCD4Th1MedianSpecificity.getTitle() + " ");
		Iterator<Double> allCD4Th2MedianSpecificityIter = colCD4Th2MedianSpecificity.getIterator();	key.append(colCD4Th2MedianSpecificity.getTitle() + " ");
		
		Iterator<Integer> cumulativeTh1KilledIter = colCumulativeTh1Killed.getIterator();			key.append(colCumulativeTh1Killed.getTitle() + " ");
	
		Iterator<Integer> clnDCPolarizationType1Iter = colCLNDCPolarizationType1.getIterator();		key.append(colCLNDCPolarizationType1.getTitle() + " ");
		Iterator<Integer>clnDCPolarizationType2Iter = colCLNDCPolarizationType2.getIterator();		key.append(colCLNDCPolarizationType2.getTitle() + " ");
		
		Iterator<Integer> cumulativeCNSDCType1Iter = colCumulativeCNSDCType1.getIterator();			key.append(colCumulativeCNSDCType1.getTitle() + " ");
		Iterator<Integer> cumulativeCNSDCType2Iter = colCumulativeCNSDCType2.getIterator();			key.append(colCumulativeCNSDCType2.getTitle() + " ");
			
		Iterator<Integer> spleenDCIter = colSpleenDC.getIterator();									key.append(colSpleenDC.getTitle() + " ");
		Iterator<Integer> spleenDCImmatureIter = colSpleenDCImmature.getIterator();					key.append(colSpleenDCImmature.getTitle() + " ");
		Iterator<Integer> spleenDCTolerogenicIter = colSpleenDCTolerogenic.getIterator();			key.append(colSpleenDCTolerogenic.getTitle() + " ");
		Iterator<Integer> spleenDCImmunogenicIter = colSpleenDCImmunogenic.getIterator();			key.append(colSpleenDCImmunogenic.getTitle() + " ");
		Iterator<Integer> spleenDCApoptoticIter = colSpleenDCApoptotic.getIterator();				key.append(colSpleenDCApoptotic.getTitle() + " ");
		
		Iterator<Integer> spleenCD4TregTotalIter = colSpleenCD4TregTotal.getIterator();				key.append(colSpleenCD4TregTotal.getTitle() + " ");
		Iterator<Integer> spleenCD4TregEffectorIter = colSpleenCD4TregEffector.getIterator();		key.append(colSpleenCD4TregEffector.getTitle() + " ");
		Iterator<Integer> spleenCD8TregTotalIter = colSpleenCD8TregTotal.getIterator();				key.append(colSpleenCD8TregTotal.getTitle() + " ");
		Iterator<Integer> spleenCD8TregEffectorIter = colSpleenCD8TregEffector.getIterator();		key.append(colSpleenCD8TregEffector.getTitle() + " ");
		Iterator<Integer> spleenCD4Th1Iter = colSpleenCD4Th1.getIterator();							key.append(colSpleenCD4Th1.getTitle() + " ");
		Iterator<Integer> spleenCD4Th2Iter = colSpleenCD4Th2.getIterator();							key.append(colSpleenCD4Th2.getTitle() + " ");
				
		Iterator<Integer> spleenCD4TregProlifIter = colSpleenCD4TregProlif.getIterator();			key.append(colSpleenCD4TregProlif.getTitle() + " ");
		Iterator<Integer> spleenCD8TregProlifIter = colSpleenCD8TregProlif.getIterator();			key.append(colSpleenCD8TregProlif.getTitle() + " ");
		Iterator<Integer> spleenTh1ProlifIter = colSpleenTh1Prolif.getIterator();					key.append(colSpleenTh1Prolif.getTitle() + " ");
		Iterator<Integer> spleenTh2ProlifIter = colSpleenTh2Prolif.getIterator();					key.append(colSpleenTh2Prolif.getTitle() + " ");
		
		Iterator<Integer> clnCD4TregProlifIter = colCLNCD4TregProlif.getIterator();					key.append(colCLNCD4TregProlif.getTitle() + " ");
		Iterator<Integer> clnCD8TregProlifIter = colCLNCD8TregProlif.getIterator();					key.append(colCLNCD8TregProlif.getTitle() + " ");
		Iterator<Integer> clnTh1ProlifIter = colCLNTh1Prolif.getIterator();							key.append(colCLNTh1Prolif.getTitle() + " ");
		Iterator<Integer> clnTh2ProlifIter = colCLNTh2Prolif.getIterator();							key.append(colCLNTh2Prolif.getTitle() + " ");
		
		Iterator<Integer> clnCD4TregEffectorIter = colCLNCD4TregEffector.getIterator();				key.append(colCLNCD4TregEffector.getTitle() + " ");
		Iterator<Integer> clnCD8TregEffectorIter = colCLNCD8TregEffector.getIterator();				key.append(colCLNCD8TregEffector.getTitle() + " ");
		Iterator<Integer> clnTh1EffectorIter = colCLNTh1Effector.getIterator();						key.append(colCLNTh1Effector.getTitle() + " ");
		Iterator<Integer> clnTh2EffectorIter = colCLNTh2Effector.getIterator();						key.append(colCLNTh2Effector.getTitle() + " ");
		
		Iterator<Integer> cumulativeCD4Th1KilledCirculatoryIter = colCumulativeTh1KilledCirculatory.getIterator(); key.append(colCumulativeTh1KilledCirculatory.getTitle() + " ");
		Iterator<Integer> cumulativeCD4Th1KilledCLNIter = colCumulativeTh1KilledCLN.getIterator(); 	key.append(colCumulativeTh1KilledCLN.getTitle() + " ");
		Iterator<Integer> cumulativeCD4Th1KilledCNSIter = colCumulativeTh1KilledCNS.getIterator(); 	key.append(colCumulativeTh1KilledCNS.getTitle() + " ");		
		Iterator<Integer> cumulativeCD4Th1KilledSLOIter = colCumulativeTh1KilledSLO.getIterator(); 	key.append(colCumulativeTh1KilledSLO.getTitle() + " ");
		Iterator<Integer> cumulativeCD4Th1KilledSpleenIter = colCumulativeTh1KilledSpleen.getIterator(); key.append(colCumulativeTh1KilledSpleen.getTitle() + " ");
		
		Iterator<Integer> cd4ThPrimedCirculatoryIter = colCD4ThPrimedCirculatory.getIterator(); 		key.append(colCD4ThPrimedCirculatory.getTitle() + " ");
		Iterator<Integer> cd4ThPrimedCLNIter = colCD4ThPrimedCLN.getIterator(); 						key.append(colCD4ThPrimedCLN.getTitle() + " ");
		Iterator<Integer> cd4ThPrimedCNSIter = colCD4ThPrimedCNS.getIterator(); 						key.append(colCD4ThPrimedCNS.getTitle() + " ");
		Iterator<Integer> cd4ThPrimedSLOIter = colCD4ThPrimedSLO.getIterator(); 						key.append(colCD4ThPrimedSLO.getTitle() + " ");
		Iterator<Integer> cd4ThPrimedSpleenIter = colCD4ThPrimedSpleen.getIterator(); 				key.append(colCD4ThPrimedSpleen.getTitle() + " ");
		
		Iterator<Integer> cd4TregPrimedCirculatoryIter = colCD4TregPrimedCirculatory.getIterator(); 	key.append(colCD4TregPrimedCirculatory.getTitle() + " ");
		Iterator<Integer> cd4TregPrimedCLNIter = colCD4TregPrimedCLN.getIterator(); 					key.append(colCD4TregPrimedCLN.getTitle() + " ");
		Iterator<Integer> cd4TregPrimedCNSIter = colCD4TregPrimedCNS.getIterator(); 					key.append(colCD4TregPrimedCNS.getTitle() + " ");
		Iterator<Integer> cd4TregPrimedSLOIter = colCD4TregPrimedSLO.getIterator(); 					key.append(colCD4TregPrimedSLO.getTitle() + " ");
		Iterator<Integer> cd4TregPrimedSpleenIter = colCD4TregPrimedSpleen.getIterator(); 			key.append(colCD4TregPrimedSpleen.getTitle() + " ");
		
		Iterator<Integer> cd8TregPrimedCirculatoryIter = colCD8TregPrimedCirculatory.getIterator(); 	key.append(colCD8TregPrimedCirculatory.getTitle() + " ");
		Iterator<Integer> cd8TregPrimedCLNIter = colCD8TregPrimedCLN.getIterator(); 					key.append(colCD8TregPrimedCLN.getTitle() + " ");
		Iterator<Integer> cd8TregPrimedCNSIter = colCD8TregPrimedCLN.getIterator(); 					key.append(colCD8TregPrimedCNS.getTitle() + " ");
		Iterator<Integer> cd8TregPrimedSLOIter = colCD8TregPrimedSLO.getIterator(); 					key.append(colCD8TregPrimedSLO.getTitle() + " ");
		Iterator<Integer> cd8TregPrimedSpleenIter = colCD8TregPrimedSpleen.getIterator(); 			key.append(colCD8TregPrimedSpleen.getTitle() + " ");
		
		Iterator<Integer> neuronsKilledCumulativeIter = colNeuronsKilledCumulative.getIterator(); 	key.append(colNeuronsKilledCumulative.getTitle() + " ");

		Iterator<Integer> dcmPeptidePresentationMBPIter = colDCMPeptidePresentationMBP.getIterator();		key.append(colDCMPeptidePresentationMBP.getTitle() + " ");
		Iterator<Integer> dcmPeptidePresentationType1Iter = colDCMPeptidePresentationType1.getIterator();	key.append(colDCMPeptidePresentationType1.getTitle() + " ");
		Iterator<Integer> dcmPeptidePresentationBothIter = colDCMPeptidePresentationBoth.getIterator();		key.append(colDCMPeptidePresentationBoth.getTitle() + " ");
		Iterator<Integer> dcmPeptidePresentationNoneIter = colDCMPeptidePresentationNone.getIterator();		key.append(colDCMPeptidePresentationNone.getTitle() + " ");
		Iterator<Integer> dcmPeptidePresentationTotalIter = colDCMPeptidePresentationTotal.getIterator();	key.append(colDCMPeptidePresentationTotal.getTitle() + " ");
		
		tableKey = key.toString();
		output.append("#" + tableKey + "\n");
		while(timeIter.hasNext())
		{
			output.append(timeIter.next()); output.append(" ");
			
			output.append(totalCD4ThIter.next()); output.append(" ");
			
			output.append(totalCD4ThNaiveIter.next()); output.append(" ");
			output.append(totalCD4ThPartialIter.next()); output.append(" ");
			output.append(totalCD4ThProliferatingIter.next()); output.append(" ");
			output.append(totalCD4Th1Iter.next()); output.append(" ");
			output.append(totalCD4Th2Iter.next()); output.append(" ");
			output.append(totalCD4ThApoptoticIter.next()); output.append(" ");
			
			output.append(totalCD4TregIter.next()); output.append(" ");
			output.append(totalCD4TregNaiveIter.next()); output.append(" ");
			output.append(totalCD4TregPartialIter.next()); output.append(" ");
			output.append(totalCD4TregProliferatingIter.next()); output.append(" ");
			output.append(totalCD4TregActivatedIter.next()); output.append(" ");
			output.append(totalCD4TregApoptoticIter.next()); output.append(" ");
			
			output.append(totalCD8TregIter.next()); output.append(" ");
			output.append(totalCD8TregNaiveIter.next()); output.append(" ");
			output.append(totalCD8TregPartialIter.next()); output.append(" ");
			output.append(totalCD8TregProliferatingIter.next()); output.append(" ");
			output.append(totalCD8TregActivatedIter.next()); output.append(" ");
			output.append(totalCD8TregApoptoticIter.next()); output.append(" ");
			
			output.append(cnsAPCIter.next()); output.append(" ");
			output.append(cnsAPCImmatureIter.next()); output.append(" ");
			output.append(cnsAPCTolerogenicIter.next()); output.append(" ");
			output.append(cnsAPCImmunogenicIter.next()); output.append(" ");
			output.append(cnsAPCApoptoticIter.next()); output.append(" ");
			
			output.append(clnDCIter.next()); output.append(" ");
			output.append(clnDCImmatureIter.next()); output.append(" ");
			output.append(clnDCTolerogenicIter.next()); output.append(" ");
			output.append(clnDCImmunogenicIter.next()); output.append(" ");
			output.append(clnDCApoptoticIter.next()); output.append(" ");
			
			output.append(sloDCIter.next()); output.append(" ");
			output.append(sloDCImmatureIter.next()); output.append(" ");
			output.append(sloDCTolerogenicIter.next()); output.append(" ");
			output.append(sloDCImmunogenicIter.next()); output.append(" ");
			output.append(sloDCApoptoticIter.next()); output.append(" ");
			
			output.append(cnsCD4Th1Iter.next()); output.append(" ");
			output.append(cnsCD4Th2Iter.next()); output.append(" ");
			
			output.append(allCD4ThMedianSpecificityIter.next()); output.append(" ");
			output.append(allCD4Th1MedianSpecificityIter.next()); output.append(" ");
			output.append(allCD4Th2MedianSpecificityIter.next()); output.append(" ");
			
			output.append(cumulativeTh1KilledIter.next()); output.append(" ");
			
			output.append(clnDCPolarizationType1Iter.next()); output.append(" ");
			output.append(clnDCPolarizationType2Iter.next()); output.append(" ");
			
			output.append(cumulativeCNSDCType1Iter.next()); output.append(" ");
			output.append(cumulativeCNSDCType2Iter.next()); output.append(" ");
			
			output.append(spleenDCIter.next()); output.append(" ");
			output.append(spleenDCImmatureIter.next()); output.append(" ");
			output.append(spleenDCTolerogenicIter.next()); output.append(" ");
			output.append(spleenDCImmunogenicIter.next()); output.append(" ");
			output.append(spleenDCApoptoticIter.next()); output.append(" ");
			
			output.append(spleenCD4TregTotalIter.next()); output.append(" ");
			output.append(spleenCD4TregEffectorIter.next()); output.append(" ");
			output.append(spleenCD8TregTotalIter.next()); output.append(" ");
			output.append(spleenCD8TregEffectorIter.next()); output.append(" ");
			output.append(spleenCD4Th1Iter.next()); output.append(" ");
			output.append(spleenCD4Th2Iter.next()); output.append(" ");
			
			output.append(spleenCD4TregProlifIter.next()); output.append(" ");
			output.append(spleenCD8TregProlifIter.next()); output.append(" ");
			output.append(spleenTh1ProlifIter.next()); output.append(" ");
			output.append(spleenTh2ProlifIter.next()); output.append(" ");
			
			output.append(clnCD4TregProlifIter.next()); output.append(" ");
			output.append(clnCD8TregProlifIter.next()); output.append(" ");
			output.append(clnTh1ProlifIter.next()); output.append(" ");
			output.append(clnTh2ProlifIter.next()); output.append(" ");
			
			output.append(clnCD4TregEffectorIter.next()); output.append(" ");
			output.append(clnCD8TregEffectorIter.next()); output.append(" ");
			output.append(clnTh1EffectorIter.next()); output.append(" ");
			output.append(clnTh2EffectorIter.next()); output.append(" ");
			
			output.append(cumulativeCD4Th1KilledCirculatoryIter.next()); output.append(" ");
			output.append(cumulativeCD4Th1KilledCLNIter.next()); output.append(" ");
			output.append(cumulativeCD4Th1KilledCNSIter.next()); output.append(" ");			
			output.append(cumulativeCD4Th1KilledSLOIter.next()); output.append(" ");
			output.append(cumulativeCD4Th1KilledSpleenIter.next()); output.append(" ");
			
			output.append(cd4ThPrimedCirculatoryIter.next()); output.append(" ");
			output.append(cd4ThPrimedCLNIter.next()); output.append(" ");
			output.append(cd4ThPrimedCNSIter.next()); output.append(" ");
			output.append(cd4ThPrimedSLOIter.next()); output.append(" ");
			output.append(cd4ThPrimedSpleenIter.next()); output.append(" ");

			output.append(cd4TregPrimedCirculatoryIter.next()); output.append(" ");
			output.append(cd4TregPrimedCLNIter.next()); output.append(" ");
			output.append(cd4TregPrimedCNSIter.next()); output.append(" ");
			output.append(cd4TregPrimedSLOIter.next()); output.append(" ");
			output.append(cd4TregPrimedSpleenIter.next()); output.append(" ");
			
			output.append(cd8TregPrimedCirculatoryIter.next()); output.append(" ");
			output.append(cd8TregPrimedCLNIter.next()); output.append(" ");
			output.append(cd8TregPrimedCNSIter.next()); output.append(" ");
			output.append(cd8TregPrimedSLOIter.next()); output.append(" ");
			output.append(cd8TregPrimedSpleenIter.next()); output.append(" ");
			
			output.append(neuronsKilledCumulativeIter.next()); output.append(" ");
			
			output.append(dcmPeptidePresentationMBPIter.next()); output.append(" ");
			output.append(dcmPeptidePresentationType1Iter.next()); output.append(" ");
			output.append(dcmPeptidePresentationBothIter.next()); output.append(" ");
			output.append(dcmPeptidePresentationNoneIter.next()); output.append(" ");
			output.append(dcmPeptidePresentationTotalIter.next()); output.append(" ");
			
			output.append("\n");
		}
		
		return output.toString();
	}
	
	/**

	 * 
	 * @param file
	 * @return
	 */
	public static SingleRunDataStore compileFromFile(File file)
	{
		SingleRunDataStore store = new SingleRunDataStore();
		
		ArrayList<String> lines = new ArrayList<String>();
		
		/* Read the file, placing each line into a string stored within an ArrayList
		 * 
		 * File reading code based on that that found online, on 2009.11.04 at
		 * http://www.javapractices.com/topic/TopicAction.do?Id=42
		 */
		try 
		{
		    //use buffering, reading one line at a time		      
			BufferedReader input =  new BufferedReader(new FileReader(file));
		    try 
		    {
		    	String line = null; //not declared within while loop
		        /*
		        * readLine is a bit quirky :
		        * it returns the content of a line MINUS the newline.
		        * it returns null only for the END of the stream.
		        * it returns an empty String if two newlines appear in a row.
		        */
		        while( (line = input.readLine()) != null )
		        {
		        	lines.add(line);
		        }
		    }
		    finally {
		    	input.close();
		    }
		}
		catch (IOException ex){
			ex.printStackTrace();
		}

		/*
		 * At this point 'lines' contains the file's data. We need to read it in order and place that data into this class.
		 * Each row in the file will represent a data sampling point in the simulation, the columns represent data items.
		 * 
		 * Note that we make allowances for lines that start with a '#', these are comments. 
		 */
		
		for(String line : lines)
		{
			if(line.contains("#") == false)								// ignore comments in the input data file. 
			{
				compileSamplePointData(store, line);
			}
		}
		
		return store;
	}
	
	
	private static void compileSamplePointData(SingleRunDataStore store, String line)
	{
		String[] data = line.split(" ");	// splits the line into words separated by " " and compiles each line into an array, called 'data'. 
		
		store.time = Double.parseDouble(data[0]);
		
		store.totalCD4Th = Integer.parseInt(data[1]);
		store.totalCD4ThNaive = Integer.parseInt(data[2]);
		store.totalCD4ThPartial = Integer.parseInt(data[3]);
		store.totalCD4ThProliferating = Integer.parseInt(data[4]);
		store.totalCD4Th1 = Integer.parseInt(data[5]);
		store.totalCD4Th2 = Integer.parseInt(data[6]);
		store.totalCD4ThApoptotic = Integer.parseInt(data[7]);
		
		store.totalCD4Treg = Integer.parseInt(data[8]);
		store.totalCD4TregNaive = Integer.parseInt(data[9]);
		store.totalCD4TregPartial = Integer.parseInt(data[10]);
		store.totalCD4TregProliferating = Integer.parseInt(data[11]);
		store.totalCD4TregActivated = Integer.parseInt(data[12]);
		store.totalCD4TregApoptotic = Integer.parseInt(data[13]);
		
		store.totalCD8Treg = Integer.parseInt(data[14]);
		store.totalCD8TregNaive = Integer.parseInt(data[15]);
		store.totalCD8TregPartial = Integer.parseInt(data[16]);
		store.totalCD8TregProliferating = Integer.parseInt(data[17]);
		store.totalCD8TregActivated = Integer.parseInt(data[18]);
		store.totalCD8TregApoptotic = Integer.parseInt(data[19]);
		
		store.cnsAPC = Integer.parseInt(data[20]);
		store.cnsAPCImmature = Integer.parseInt(data[21]);
		store.cnsAPCTolerogenic = Integer.parseInt(data[22]);
		store.cnsAPCImmunogenic = Integer.parseInt(data[23]);
		store.cnsAPCApoptotic = Integer.parseInt(data[24]);
		
		store.clnDC = Integer.parseInt(data[25]);
		store.clnDCImmature = Integer.parseInt(data[26]);
		store.clnDCTolerogenic = Integer.parseInt(data[27]);
		store.clnDCImmunogenic = Integer.parseInt(data[28]);
		store.clnDCApoptotic = Integer.parseInt(data[29]);
		
		store.sloDC = Integer.parseInt(data[30]);
		store.sloDCImmature = Integer.parseInt(data[31]);
		store.sloDCTolerogenic = Integer.parseInt(data[32]);
		store.sloDCImmunogenic = Integer.parseInt(data[33]);
		store.sloDCApoptotic = Integer.parseInt(data[34]);
		
		store.cnsCD4Th1 = Integer.parseInt(data[35]);
		store.cnsCD4Th2 = Integer.parseInt(data[36]);
		
		store.cd4ThSpecificities = Double.parseDouble(data[37]);
		store.cd4Th1Specificities = Double.parseDouble(data[38]);
		store.cd4Th2Specificities = Double.parseDouble(data[39]);
		
		store.cumulativeTh1Killed = Integer.parseInt(data[40]);
		
		store.clnDCPolarizationType1 = Integer.parseInt(data[41]);
		store.clnDCPolarizationType2 = Integer.parseInt(data[42]);
		
		store.cumulativeCNSDCType1 = Integer.parseInt(data[43]);
		store.cumulativeCNSDCType2 = Integer.parseInt(data[44]);
		
		store.spleenDC = Integer.parseInt(data[45]);
		store.spleenDCImmature = Integer.parseInt(data[46]);
		store.spleenDCTolerogenic = Integer.parseInt(data[47]);
		store.spleenDCImmunogenic = Integer.parseInt(data[48]);
		store.spleenDCApoptotic = Integer.parseInt(data[49]);
		
		store.spleenCD4TregTotal = Integer.parseInt(data[50]);
		store.spleenCD4TregEffector = Integer.parseInt(data[51]);
		store.spleenCD8TregTotal = Integer.parseInt(data[52]);
		store.spleenCD8TregEffector = Integer.parseInt(data[53]);
		store.spleenCD4Th1 = Integer.parseInt(data[54]);
		store.spleenCD4Th2 = Integer.parseInt(data[55]);
	
		store.spleenCD4TregProlif = Integer.parseInt(data[56]);
		store.spleenCD8TregProlif = Integer.parseInt(data[57]);
		store.spleenTh1Prolif = Integer.parseInt(data[58]);
		store.spleenTh2Prolif = Integer.parseInt(data[59]);
		
		store.clnCD4TregProlif = Integer.parseInt(data[60]);
		store.clnCD8TregProlif = Integer.parseInt(data[61]);
		store.clnTh1Prolif = Integer.parseInt(data[62]);
		store.clnTh2Prolif = Integer.parseInt(data[63]);
		
		store.clnCD4TregEffector = Integer.parseInt(data[64]);
		store.clnCD8TregEffector = Integer.parseInt(data[65]);
		store.clnTh1Effector = Integer.parseInt(data[66]);
		store.clnTh2Effector = Integer.parseInt(data[67]);
		
		store.cumulativeTh1KilledCirculatory = Integer.parseInt(data[68]);
		store.cumulativeTh1KilledCLN= Integer.parseInt(data[69]);
		store.cumulativeTh1KilledCNS= Integer.parseInt(data[70]);
		store.cumulativeTh1KilledSLO= Integer.parseInt(data[71]);
		store.cumulativeTh1KilledSpleen= Integer.parseInt(data[72]);
		
		store.cd4ThPrimedCirculatory = Integer.parseInt(data[73]);
		store.cd4ThPrimedCLN = Integer.parseInt(data[74]);
		store.cd4ThPrimedCNS = Integer.parseInt(data[75]);
		store.cd4ThPrimedSLO = Integer.parseInt(data[76]);
		store.cd4ThPrimedSpleen = Integer.parseInt(data[77]);

		store.cd4TregPrimedCirculatory = Integer.parseInt(data[78]);
		store.cd4TregPrimedCLN = Integer.parseInt(data[79]);
		store.cd4TregPrimedCNS = Integer.parseInt(data[80]);
		store.cd4TregPrimedSLO = Integer.parseInt(data[81]);
		store.cd4TregPrimedSpleen = Integer.parseInt(data[82]);
		
		store.cd8TregPrimedCirculatory = Integer.parseInt(data[83]);
		store.cd8TregPrimedCLN = Integer.parseInt(data[84]);
		store.cd8TregPrimedCNS = Integer.parseInt(data[85]);
		store.cd8TregPrimedSLO = Integer.parseInt(data[86]);
		store.cd8TregPrimedSpleen = Integer.parseInt(data[87]);	
		
		store.neuronsKilledCumulative = Integer.parseInt(data[88]);
		
		store.dcmPeptidePresentationMBP = Integer.parseInt(data[89]);
		store.dcmPeptidePresentationType1 = Integer.parseInt(data[90]);
		store.dcmPeptidePresentationBoth = Integer.parseInt(data[91]);
		store.dcmPeptidePresentationNone = Integer.parseInt(data[92]);
		store.dcmPeptidePresentationTotal = Integer.parseInt(data[93]);
		
		store.storeData();
	}

}