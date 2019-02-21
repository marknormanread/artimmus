package sim2d.dataCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MultipleRunDataStore 
{
	private MedianDataColumnDouble colTime = new MedianDataColumnDouble("time_hours");
	
	private MedianDataColumnDouble colTotalCD4Th = new MedianDataColumnDouble("total_CD4Th");
	private MedianDataColumnDouble colTotalCD4ThNaive = new MedianDataColumnDouble("total_CD4ThNaive");
	private MedianDataColumnDouble colTotalCD4ThPartial = new MedianDataColumnDouble("total_CD4ThPartial");
	private MedianDataColumnDouble colTotalCD4ThProliferating = new MedianDataColumnDouble("total_CD4ThProliferating");
	private MedianDataColumnDouble colTotalCD4Th1 = new MedianDataColumnDouble("total_CD4Th1");
	private MedianDataColumnDouble colTotalCD4Th2 = new MedianDataColumnDouble("total_CD4Th2");
	private MedianDataColumnDouble colTotalCD4ThApoptotic = new MedianDataColumnDouble("total_CD4ThApoptotic");
	
	private MedianDataColumnDouble colTotalCD4Treg = new MedianDataColumnDouble("total_CD4Treg");
	private MedianDataColumnDouble colTotalCD4TregNaive = new MedianDataColumnDouble("total_CD4TregNaive");
	private MedianDataColumnDouble colTotalCD4TregPartial = new MedianDataColumnDouble("total_CD4TregPartial");
	private MedianDataColumnDouble colTotalCD4TregProliferating = new MedianDataColumnDouble("total_CD4TregProliferating");
	private MedianDataColumnDouble colTotalCD4TregActivated = new MedianDataColumnDouble("total_CD4TregActivated");
	private MedianDataColumnDouble colTotalCD4TregApoptotic = new MedianDataColumnDouble("total_CD4TregApoptotic");
	
	private MedianDataColumnDouble colTotalCD8Treg = new MedianDataColumnDouble("total_CD8Treg");
	private MedianDataColumnDouble colTotalCD8TregNaive = new MedianDataColumnDouble("total_CD8TregNaive");
	private MedianDataColumnDouble colTotalCD8TregPartial = new MedianDataColumnDouble("total_CD8TregPartial");
	private MedianDataColumnDouble colTotalCD8TregProliferating = new MedianDataColumnDouble("total_CD8TregProliferating");
	private MedianDataColumnDouble colTotalCD8TregActivated = new MedianDataColumnDouble("total_CD8TregActivated");
	private MedianDataColumnDouble colTotalCD8TregApoptotic = new MedianDataColumnDouble("total_CD8TregApoptotic");
	
	private MedianDataColumnDouble colCNSAPC = new MedianDataColumnDouble("cns_APC");
	private MedianDataColumnDouble colCNSAPCImmature = new MedianDataColumnDouble("cns_APCImmature");
	private MedianDataColumnDouble colCNSAPCTolerogenic = new MedianDataColumnDouble("cns_APCTolerogenic");
	private MedianDataColumnDouble colCNSAPCImmunogenic = new MedianDataColumnDouble("cns_APCImmunogenic");
	private MedianDataColumnDouble colCNSAPCApoptotic = new MedianDataColumnDouble("cns_APCApoptotic");
	
	private MedianDataColumnDouble colCLNDC = new MedianDataColumnDouble("CLN_DC");
	private MedianDataColumnDouble colCLNDCImmature = new MedianDataColumnDouble("CLN_DCImmature");
	private MedianDataColumnDouble colCLNDCTolerogenic = new MedianDataColumnDouble("CLN_DCTolerogenic");
	private MedianDataColumnDouble colCLNDCImmunogenic = new MedianDataColumnDouble("CLN_DCImmunogenic");
	private MedianDataColumnDouble colCLNDCApoptotic = new MedianDataColumnDouble("CLN_DCApoptotic");
	
	private MedianDataColumnDouble colSLODC = new MedianDataColumnDouble("SLO_DC");
	private MedianDataColumnDouble colSLODCImmature = new MedianDataColumnDouble("SLO_DCImmature");
	private MedianDataColumnDouble colSLODCTolerogenic = new MedianDataColumnDouble("SLO_DCTolerogenic");
	private MedianDataColumnDouble colSLODCImmunogenic = new MedianDataColumnDouble("SLO_DCImmunogenic");
	private MedianDataColumnDouble colSLODCApoptotic = new MedianDataColumnDouble("SLO_DCApoptotic");
	
	private MedianDataColumnDouble colCNSCD4Th1 = new MedianDataColumnDouble("CNS_CD4Th1");
	private MedianDataColumnDouble colCNSCD4Th2 = new MedianDataColumnDouble("CNS_CD4Th2");
	
	private MedianDataColumnDouble colCD4ThSpecificity = new MedianDataColumnDouble("CD4Th_Specificity");
	private MedianDataColumnDouble colCD4Th1Specificity = new MedianDataColumnDouble("CD4Th1_Specificity");
	private MedianDataColumnDouble colCD4Th2Specificity = new MedianDataColumnDouble("CD4Th2_Specificity");	
	
	private MedianDataColumnDouble colCumulativeTh1Killed = new MedianDataColumnDouble("cumulative_Th1Killed");
	
	private MedianDataColumnDouble colCLNDCPolarizationType1 = new MedianDataColumnDouble("CLN_DC_PolarizationType1");
	private MedianDataColumnDouble colCLNDCPolarizationType2 = new MedianDataColumnDouble("CLN_DC_PolarizationType2");
	
	private MedianDataColumnDouble colCumulativeCNSDCType1 = new MedianDataColumnDouble("cumulative_CNS_DC_PolarizationType1");
	private MedianDataColumnDouble colCumulativeCNSDCType2 = new MedianDataColumnDouble("cumulative_CNS_DC_PolarizationType2");
	
	private MedianDataColumnDouble colSpleenDC = new MedianDataColumnDouble("Spleen_DC");
	private MedianDataColumnDouble colSpleenDCImmature = new MedianDataColumnDouble("Spleen_DCImmature");
	private MedianDataColumnDouble colSpleenDCTolerogenic = new MedianDataColumnDouble("Spleen_DCTolerogenic");
	private MedianDataColumnDouble colSpleenDCImmunogenic = new MedianDataColumnDouble("Spleen_DCImmunogenic");
	private MedianDataColumnDouble colSpleenDCApoptotic = new MedianDataColumnDouble("Spleen_DCApoptotic");
	
	private MedianDataColumnDouble colSpleenCD4TregTotal = new MedianDataColumnDouble("Spleen_CD4TregTotal");
	private MedianDataColumnDouble colSpleenCD4TregEffector= new MedianDataColumnDouble("Spleen_CD4TregEffector");
	private MedianDataColumnDouble colSpleenCD8TregTotal = new MedianDataColumnDouble("Spleen_CD8TregTotal");
	private MedianDataColumnDouble colSpleenCD8TregEffector = new MedianDataColumnDouble("Spleen_CD8TregEffector");
	private MedianDataColumnDouble colSpleenCD4Th1 = new MedianDataColumnDouble("Spleen_CD4Th1");
	private MedianDataColumnDouble colSpleenCD4Th2 = new MedianDataColumnDouble("Spleen_CD4Th2");
	
	private MedianDataColumnDouble  colSpleenTh1Prolif = new MedianDataColumnDouble("Spleen_Th1Prolif");
	private MedianDataColumnDouble  colSpleenTh2Prolif = new MedianDataColumnDouble("Spleen_Th2Prolif");
	private MedianDataColumnDouble  colSpleenCD4TregProlif = new MedianDataColumnDouble("Spleen_CD4TregProlif");
	private MedianDataColumnDouble  colSpleenCD8TregProlif = new MedianDataColumnDouble("Spleen_CD8TregProlif");
	
	private MedianDataColumnDouble  colCLNTh1Prolif = new MedianDataColumnDouble("CLN_Th1Prolif");
	private MedianDataColumnDouble  colCLNTh2Prolif = new MedianDataColumnDouble("CLN_Th2Prolif");
	private MedianDataColumnDouble  colCLNCD4TregProlif = new MedianDataColumnDouble("CLN_CD4TregProlif");
	private MedianDataColumnDouble  colCLNCD8TregProlif = new MedianDataColumnDouble("CLN_CD8TregProlif");
	
	private MedianDataColumnDouble  colCLNTh1Effector = new MedianDataColumnDouble("CLN_Th1Effector");
	private MedianDataColumnDouble  colCLNTh2Effector = new MedianDataColumnDouble("CLN_Th2Effector");
	private MedianDataColumnDouble  colCLNCD4TregEffector = new MedianDataColumnDouble("CLN_CD4TregEffector");
	private MedianDataColumnDouble  colCLNCD8TregEffector = new MedianDataColumnDouble("CLN_CD8TregEffector");

	private MedianDataColumnDouble  colCumulativeCD4Th1KilledCirculatory = new MedianDataColumnDouble("cumulativeCD4Th1KilledCirculatory");
	private MedianDataColumnDouble  colCumulativeCD4Th1KilledCLN= new MedianDataColumnDouble("cumulativeCD4Th1KilledCLN");
	private MedianDataColumnDouble  colCumulativeCD4Th1KilledCNS= new MedianDataColumnDouble("cumulativeCD4Th1KilledCNS");	
	private MedianDataColumnDouble  colCumulativeCD4Th1KilledSLO= new MedianDataColumnDouble("cumulativeCD4Th1KilledSLO");
	private MedianDataColumnDouble  colCumulativeCD4Th1KilledSpleen = new MedianDataColumnDouble("cumulativeCD4Th1KilledSpleen");
	
	private MedianDataColumnDouble colCD4ThPrimedCirculatory = new MedianDataColumnDouble("CD4ThPrimedCirculatory");
	private MedianDataColumnDouble colCD4ThPrimedCLN = new MedianDataColumnDouble("CD4ThPrimedCLN");
	private MedianDataColumnDouble colCD4ThPrimedCNS = new MedianDataColumnDouble("CD4ThPrimedCNS");
	private MedianDataColumnDouble colCD4ThPrimedSLO = new MedianDataColumnDouble("CD4ThPrimedSLO");
	private MedianDataColumnDouble colCD4ThPrimedSpleen = new MedianDataColumnDouble("CD4ThPrimedSpleen");

	private MedianDataColumnDouble colCD4TregPrimedCirculatory = new MedianDataColumnDouble("CD4TregPrimedCirculatory");
	private MedianDataColumnDouble colCD4TregPrimedCLN = new MedianDataColumnDouble("CD4TregPrimedCLN");
	private MedianDataColumnDouble colCD4TregPrimedCNS = new MedianDataColumnDouble("CD4TregPrimedCNS");
	private MedianDataColumnDouble colCD4TregPrimedSLO = new MedianDataColumnDouble("CD4TregPrimedSLO");
	private MedianDataColumnDouble colCD4TregPrimedSpleen = new MedianDataColumnDouble("CD4TregPrimedSpleen");
	
	private MedianDataColumnDouble colCD8TregPrimedCirculatory = new MedianDataColumnDouble("CD8TregPrimedCirculatory");
	private MedianDataColumnDouble colCD8TregPrimedCLN = new MedianDataColumnDouble("CD8TregPrimedCLN");
	private MedianDataColumnDouble colCD8TregPrimedCNS = new MedianDataColumnDouble("CD8TregPrimedCNS");
	private MedianDataColumnDouble colCD8TregPrimedSLO = new MedianDataColumnDouble("CD8TregPrimedSLO");
	private MedianDataColumnDouble colCD8TregPrimedSpleen = new MedianDataColumnDouble("CD8TregPrimedSpleen");
	
	private MedianDataColumnDouble colNeuronsKilledCumulative = new MedianDataColumnDouble("NeuronsKilledCumulative");
	
	private MedianDataColumnDouble colDCMPeptidePresentationMBP = new MedianDataColumnDouble("DCMPeptidePresentationMBP");
	private MedianDataColumnDouble colDCMPeptidePresentationType1 = new MedianDataColumnDouble("DCMPeptidePresentationType1");
	private MedianDataColumnDouble colDCMPeptidePresentationBoth = new MedianDataColumnDouble("DCMPeptidePresentationBoth");
	private MedianDataColumnDouble colDCMPeptidePresentationNone = new MedianDataColumnDouble("DCMPeptidePresentationNone");
	private MedianDataColumnDouble colDCMPeptidePresentationTotal = new MedianDataColumnDouble("DCMPeptidePresentationTotal");
	
	private ArrayList<String> tableKey = new ArrayList<String>();
	
	public void logSingleRunResults(SingleRunDataStore store)
	{		
		this.colTime.logColumnValues(store.colTime);
		
		this.colTotalCD4Th.logColumnValues(store.colTotalCD4Th);
		this.colTotalCD4ThNaive.logColumnValues(store.colTotalCD4ThNaive);
		this.colTotalCD4ThPartial.logColumnValues(store.colTotalCD4ThPartial);
		this.colTotalCD4ThProliferating.logColumnValues(store.colTotalCD4ThProliferating);
		this.colTotalCD4Th1.logColumnValues(store.colTotalCD4Th1);
		this.colTotalCD4Th2.logColumnValues(store.colTotalCD4Th2);
		this.colTotalCD4ThApoptotic.logColumnValues(store.colTotalCD4ThApoptotic);
		
		this.colTotalCD4Treg.logColumnValues(store.colTotalCD4Treg);
		this.colTotalCD4TregNaive.logColumnValues(store.colTotalCD4TregNaive);
		this.colTotalCD4TregPartial.logColumnValues(store.colTotalCD4TregPartial);
		this.colTotalCD4TregProliferating.logColumnValues(store.colTotalCD4TregProliferating);
		this.colTotalCD4TregActivated.logColumnValues(store.colTotalCD4TregActivated);
		this.colTotalCD4TregApoptotic.logColumnValues(store.colTotalCD4TregApoptotic);
		
		this.colTotalCD8Treg.logColumnValues(store.colTotalCD8Treg);
		this.colTotalCD8TregNaive.logColumnValues(store.colTotalCD8TregNaive);
		this.colTotalCD8TregPartial.logColumnValues(store.colTotalCD8TregPartial);
		this.colTotalCD8TregProliferating.logColumnValues(store.colTotalCD8TregProliferating);
		this.colTotalCD8TregActivated.logColumnValues(store.colTotalCD8TregActivated);
		this.colTotalCD8TregApoptotic.logColumnValues(store.colTotalCD8TregApoptotic);
		
		this.colCNSAPC.logColumnValues(store.colCNSAPC);
		this.colCNSAPCImmature.logColumnValues(store.colCNSAPCImmature);
		this.colCNSAPCTolerogenic.logColumnValues(store.colCNSAPCTolerogenic);
		this.colCNSAPCImmunogenic.logColumnValues(store.colCNSAPCImmunogenic);
		this.colCNSAPCApoptotic.logColumnValues(store.colCNSAPCApoptotic);
		
		this.colCLNDC.logColumnValues(store.colCLNDC);
		this.colCLNDCImmature.logColumnValues(store.colCLNDCImmature);
		this.colCLNDCTolerogenic.logColumnValues(store.colCLNDCTolerogenic);
		this.colCLNDCImmunogenic.logColumnValues(store.colCLNDCImmunogenic);
		this.colCLNDCApoptotic.logColumnValues(store.colCLNDCApoptotic);
		
		this.colSLODC.logColumnValues(store.colSLODC);
		this.colSLODCImmature.logColumnValues(store.colSLODCImmature);
		this.colSLODCTolerogenic.logColumnValues(store.colSLODCTolerogenic);
		this.colSLODCImmunogenic.logColumnValues(store.colSLODCImmunogenic);
		this.colSLODCApoptotic.logColumnValues(store.colSLODCApoptotic);
		
		this.colCNSCD4Th1.logColumnValues(store.colCNSCD4Th1);
		this.colCNSCD4Th2.logColumnValues(store.colCNSCD4Th2);
		
		this.colCD4ThSpecificity.logColumnValues(store.colCD4ThMedianSpecificity);
		this.colCD4Th1Specificity.logColumnValues(store.colCD4Th1MedianSpecificity);
		this.colCD4Th2Specificity.logColumnValues(store.colCD4Th2MedianSpecificity);
		
		this.colCumulativeTh1Killed.logColumnValues(store.colCumulativeTh1Killed);
		
		this.colCLNDCPolarizationType1.logColumnValues(store.colCLNDCPolarizationType1);
		this.colCLNDCPolarizationType2.logColumnValues(store.colCLNDCPolarizationType2);
		
		this.colCumulativeCNSDCType1.logColumnValues(store.colCumulativeCNSDCType1);
		this.colCumulativeCNSDCType2.logColumnValues(store.colCumulativeCNSDCType2);
		
		this.colSpleenDC.logColumnValues(store.colSpleenDC);
		this.colSpleenDCImmature.logColumnValues(store.colSpleenDCImmature);
		this.colSpleenDCTolerogenic.logColumnValues(store.colSpleenDCTolerogenic);
		this.colSpleenDCImmunogenic.logColumnValues(store.colSpleenDCImmunogenic);
		this.colSpleenDCApoptotic.logColumnValues(store.colSpleenDCApoptotic);
		
		this.colSpleenCD4TregTotal.logColumnValues(store.colSpleenCD4TregTotal);
		this.colSpleenCD4TregEffector.logColumnValues(store.colSpleenCD4TregEffector);
		this.colSpleenCD8TregTotal.logColumnValues(store.colSpleenCD8TregTotal);
		this.colSpleenCD8TregEffector.logColumnValues(store.colSpleenCD8TregEffector);
		this.colSpleenCD4Th1.logColumnValues(store.colSpleenCD4Th1);
		this.colSpleenCD4Th2.logColumnValues(store.colSpleenCD4Th2);
		
		this.colSpleenTh1Prolif.logColumnValues(store.colSpleenTh1Prolif);
		this.colSpleenTh2Prolif.logColumnValues(store.colSpleenTh2Prolif);
		this.colSpleenCD4TregProlif.logColumnValues(store.colSpleenCD4TregProlif);
		this.colSpleenCD8TregProlif.logColumnValues(store.colSpleenCD8TregProlif);
		
		this.colCLNTh1Prolif.logColumnValues(store.colCLNTh1Prolif);
		this.colCLNTh2Prolif.logColumnValues(store.colCLNTh2Prolif);
		this.colCLNCD4TregProlif.logColumnValues(store.colCLNCD4TregProlif);
		this.colCLNCD8TregProlif.logColumnValues(store.colCLNCD8TregProlif);
		
		this.colCLNTh1Effector.logColumnValues(store.colCLNTh1Effector);
		this.colCLNTh2Effector.logColumnValues(store.colCLNTh2Effector);
		this.colCLNCD4TregEffector.logColumnValues(store.colCLNCD4TregEffector);
		this.colCLNCD8TregEffector.logColumnValues(store.colCLNCD8TregEffector);
		
		this.colCumulativeCD4Th1KilledCirculatory.logColumnValues(store.colCumulativeTh1KilledCirculatory);
		this.colCumulativeCD4Th1KilledCLN.logColumnValues(store.colCumulativeTh1KilledCLN);
		this.colCumulativeCD4Th1KilledCNS.logColumnValues(store.colCumulativeTh1KilledCNS);		
		this.colCumulativeCD4Th1KilledSLO.logColumnValues(store.colCumulativeTh1KilledSLO);
		this.colCumulativeCD4Th1KilledSpleen.logColumnValues(store.colCumulativeTh1KilledSpleen);		
		
		this.colCD4ThPrimedCirculatory.logColumnValues(store.colCD4ThPrimedCirculatory);
		this.colCD4ThPrimedCLN.logColumnValues(store.colCD4ThPrimedCLN);
		this.colCD4ThPrimedCNS.logColumnValues(store.colCD4ThPrimedCNS);
		this.colCD4ThPrimedSLO.logColumnValues(store.colCD4ThPrimedSLO);
		this.colCD4ThPrimedSpleen.logColumnValues(store.colCD4ThPrimedSpleen);
		
		this.colCD4TregPrimedCirculatory.logColumnValues(store.colCD4TregPrimedCirculatory);
		this.colCD4TregPrimedCLN.logColumnValues(store.colCD4TregPrimedCLN);
		this.colCD4TregPrimedCNS.logColumnValues(store.colCD4TregPrimedCNS);
		this.colCD4TregPrimedSLO.logColumnValues(store.colCD4TregPrimedSLO);
		this.colCD4TregPrimedSpleen.logColumnValues(store.colCD4TregPrimedSpleen);
		
		this.colCD8TregPrimedCirculatory.logColumnValues(store.colCD8TregPrimedCirculatory);
		this.colCD8TregPrimedCLN.logColumnValues(store.colCD8TregPrimedCLN);
		this.colCD8TregPrimedCNS.logColumnValues(store.colCD8TregPrimedCNS);
		this.colCD8TregPrimedSLO.logColumnValues(store.colCD8TregPrimedSLO);
		this.colCD8TregPrimedSpleen.logColumnValues(store.colCD8TregPrimedSpleen);
		
		this.colNeuronsKilledCumulative.logColumnValues(store.colNeuronsKilledCumulative);
		
		this.colDCMPeptidePresentationMBP.logColumnValues(store.colDCMPeptidePresentationMBP);
		this.colDCMPeptidePresentationType1.logColumnValues(store.colDCMPeptidePresentationType1);
		this.colDCMPeptidePresentationBoth.logColumnValues(store.colDCMPeptidePresentationBoth);
		this.colDCMPeptidePresentationNone.logColumnValues(store.colDCMPeptidePresentationNone);
		this.colDCMPeptidePresentationTotal.logColumnValues(store.colDCMPeptidePresentationTotal);
	}
	
	
	public String compileTableToString()
	{

		StringBuilder output = new StringBuilder();
		
		Iterator<Double> timeIter = colTime.getMediansIterator();										tableKey.add(colTime.getTitle());
		
		
		Iterator<Double> totalCD4ThIter = colTotalCD4Th.getMediansIterator();							tableKey.add(colTotalCD4Th.getTitle());
		Iterator<Double> totalCD4ThNaiveIter = colTotalCD4ThNaive.getMediansIterator();					tableKey.add(colTotalCD4ThNaive.getTitle());
		Iterator<Double> totalCD4ThPartialIter = colTotalCD4ThPartial.getMediansIterator();				tableKey.add(colTotalCD4ThPartial.getTitle());
		Iterator<Double> totalCD4ThProliferatingIter = colTotalCD4ThProliferating.getMediansIterator();	tableKey.add(colTotalCD4ThProliferating.getTitle());
		Iterator<Double> totalCD4Th1Iter = colTotalCD4Th1.getMediansIterator();							tableKey.add(colTotalCD4Th1.getTitle());
		Iterator<Double> totalCD4Th2Iter = colTotalCD4Th2.getMediansIterator();							tableKey.add(colTotalCD4Th2.getTitle());
		Iterator<Double> totalCD4ThApoptoticIter = colTotalCD4ThApoptotic.getMediansIterator();			tableKey.add(colTotalCD4ThApoptotic.getTitle());
		
		Iterator<Double> totalCD4TregIter = colTotalCD4Treg.getMediansIterator();						tableKey.add(colTotalCD4Treg.getTitle());
		Iterator<Double> totalCD4TregNaiveIter = colTotalCD4TregNaive.getMediansIterator();				tableKey.add(colTotalCD4TregNaive.getTitle());
		Iterator<Double> totalCD4TregPartialIter = colTotalCD4TregPartial.getMediansIterator();			tableKey.add(colTotalCD4TregPartial.getTitle());
		Iterator<Double> totalCD4TregProliferatingIter = colTotalCD4TregProliferating.getMediansIterator();	tableKey.add(colTotalCD4TregProliferating.getTitle());
		Iterator<Double> totalCD4TregActivatedIter = colTotalCD4TregActivated.getMediansIterator();		tableKey.add(colTotalCD4TregActivated.getTitle());
		Iterator<Double> totalCD4TregApoptoticIter = colTotalCD4TregApoptotic.getMediansIterator();		tableKey.add(colTotalCD4TregApoptotic.getTitle());
		
		Iterator<Double> totalCD8TregIter = colTotalCD8Treg.getMediansIterator();						tableKey.add(colTotalCD8Treg.getTitle());
		Iterator<Double> totalCD8TregNaiveIter = colTotalCD8TregNaive.getMediansIterator();				tableKey.add(colTotalCD8TregNaive.getTitle());
		Iterator<Double> totalCD8TregPartialIter = colTotalCD8TregPartial.getMediansIterator();			tableKey.add(colTotalCD8TregPartial.getTitle());
		Iterator<Double> totalCD8TregProliferatingIter = colTotalCD8TregProliferating.getMediansIterator();	tableKey.add(colTotalCD8TregProliferating.getTitle());
		Iterator<Double> totalCD8TregActivatedIter = colTotalCD8TregActivated.getMediansIterator();		tableKey.add(colTotalCD8TregActivated.getTitle());
		Iterator<Double> totalCD8TregApoptoticIter = colTotalCD8TregApoptotic.getMediansIterator();		tableKey.add(colTotalCD8TregApoptotic.getTitle());
			 
		Iterator<Double> cnsAPCIter = colCNSAPC.getMediansIterator();									tableKey.add(colCNSAPC.getTitle());
		Iterator<Double> cnsAPCImmatureIter = colCNSAPCImmature.getMediansIterator();					tableKey.add(colCNSAPCImmature.getTitle());
		Iterator<Double> cnsAPCTolerogenicIter = colCNSAPCTolerogenic.getMediansIterator();				tableKey.add(colCNSAPCTolerogenic.getTitle());
		Iterator<Double> cnsAPCImmunogenicIter = colCNSAPCImmunogenic.getMediansIterator();				tableKey.add(colCNSAPCImmunogenic.getTitle());
		Iterator<Double> cnsAPCApoptoticIter = colCNSAPCApoptotic.getMediansIterator();					tableKey.add(colCNSAPCApoptotic.getTitle());
		
		Iterator<Double> clnDCIter = colCLNDC.getMediansIterator();										tableKey.add(colCLNDC.getTitle());
		Iterator<Double> clnDCImmatureIter = colCLNDCImmature.getMediansIterator();						tableKey.add(colCLNDCImmature.getTitle());
		Iterator<Double> clnDCTolerogenicIter = colCLNDCTolerogenic.getMediansIterator();				tableKey.add(colCLNDCTolerogenic.getTitle());
		Iterator<Double> clnDCImmunogenicIter = colCLNDCImmunogenic.getMediansIterator();				tableKey.add(colCLNDCImmunogenic.getTitle());
		Iterator<Double> clnDCApoptoticIter = colCLNDCApoptotic.getMediansIterator();					tableKey.add(colCLNDCApoptotic.getTitle());
		
		Iterator<Double> sloDCIter = colSLODC.getMediansIterator();										tableKey.add(colSLODC.getTitle());
		Iterator<Double> sloDCImmatureIter = colSLODCImmature.getMediansIterator();						tableKey.add(colSLODCImmature.getTitle());
		Iterator<Double> sloDCTolerogenicIter = colSLODCTolerogenic.getMediansIterator();				tableKey.add(colSLODCTolerogenic.getTitle());
		Iterator<Double> sloDCImmunogenicIter = colSLODCImmunogenic.getMediansIterator();				tableKey.add(colSLODCImmunogenic.getTitle());
		Iterator<Double> sloDCApoptoticIter = colSLODCApoptotic.getMediansIterator();					tableKey.add(colSLODCApoptotic.getTitle());
		
		Iterator<Double> cnsCD4Th1Iter = colCNSCD4Th1.getMediansIterator();								tableKey.add(colCNSCD4Th1.getTitle());
		Iterator<Double> cnsCD4Th2Iter = colCNSCD4Th2.getMediansIterator();								tableKey.add(colCNSCD4Th2.getTitle());
		
		Iterator<Double> cd4ThSpecificityIter = colCD4ThSpecificity.getMediansIterator();				tableKey.add(colCD4ThSpecificity.getTitle());
		Iterator<Double> cd4Th1SpecificityIter = colCD4Th1Specificity.getMediansIterator();				tableKey.add(colCD4Th1Specificity.getTitle());
		Iterator<Double> cd4Th2SpecificityIter = colCD4Th2Specificity.getMediansIterator();				tableKey.add(colCD4Th2Specificity.getTitle());

		Iterator<Double> cumulativeTh1KilledIter = colCumulativeTh1Killed.getMediansIterator();			tableKey.add(colCumulativeTh1Killed.getTitle());
		
		Iterator<Double> clnDCPolarizationType1Iter = colCLNDCPolarizationType1.getMediansIterator();	tableKey.add(colCLNDCPolarizationType1.getTitle());
		Iterator<Double> clnDCPolarizationType2Iter = colCLNDCPolarizationType2.getMediansIterator();	tableKey.add(colCLNDCPolarizationType2.getTitle());
		
		Iterator<Double> colCumulativeCNSDCType1Iter = colCumulativeCNSDCType1.getMediansIterator(); 	tableKey.add(colCumulativeCNSDCType1.getTitle());
		Iterator<Double> colCumulativeCNSDCType2Iter = colCumulativeCNSDCType2.getMediansIterator(); 	tableKey.add(colCumulativeCNSDCType2.getTitle());
		
		Iterator<Double> spleenDCIter = colSpleenDC.getMediansIterator();								tableKey.add(colSpleenDC.getTitle());
		Iterator<Double> spleenDCImmatureIter = colSpleenDCImmature.getMediansIterator();				tableKey.add(colSpleenDCImmature.getTitle());
		Iterator<Double> spleenDCTolerogenicIter = colSpleenDCTolerogenic.getMediansIterator();			tableKey.add(colSpleenDCTolerogenic.getTitle());
		Iterator<Double> spleenDCImmunogenicIter = colSpleenDCImmunogenic.getMediansIterator();			tableKey.add(colSpleenDCImmunogenic.getTitle());
		Iterator<Double> spleenDCApoptoticIter = colSpleenDCApoptotic.getMediansIterator();				tableKey.add(colSpleenDCApoptotic.getTitle());
		
		Iterator<Double> spleenCD4TregTotalIter = colSpleenCD4TregTotal.getMediansIterator();			tableKey.add(colSpleenCD4TregTotal.getTitle());
		Iterator<Double> spleenCD4TregEffectorIter = colSpleenCD4TregEffector.getMediansIterator();		tableKey.add(colSpleenCD4TregEffector.getTitle());
		Iterator<Double> spleenCD8TregTotalIter = colSpleenCD8TregTotal.getMediansIterator();			tableKey.add(colSpleenCD8TregTotal.getTitle());
		Iterator<Double> spleenCD8TregEffectorIter = colSpleenCD8TregEffector.getMediansIterator();		tableKey.add(colSpleenCD8TregEffector.getTitle());
		Iterator<Double> spleenCD4Th1Iter = colSpleenCD4Th1.getMediansIterator();						tableKey.add(colSpleenCD4Th1.getTitle());
		Iterator<Double> spleenCD4Th2Iter = colSpleenCD4Th2.getMediansIterator();						tableKey.add(colSpleenCD4Th2.getTitle());
		
		Iterator<Double> spleenCD4TregProlifIter = colSpleenCD4TregProlif.getMediansIterator();			tableKey.add(colSpleenCD4TregProlif.getTitle() + " ");
		Iterator<Double> spleenCD8TregProlifIter = colSpleenCD8TregProlif.getMediansIterator();			tableKey.add(colSpleenCD8TregProlif.getTitle() + " ");
		Iterator<Double> spleenTh1ProlifIter = colSpleenTh1Prolif.getMediansIterator();					tableKey.add(colSpleenTh1Prolif.getTitle() + " ");
		Iterator<Double> spleenTh2ProlifIter = colSpleenTh2Prolif.getMediansIterator();					tableKey.add(colSpleenTh2Prolif.getTitle() + " ");
		
		Iterator<Double> clnCD4TregProlifIter = colCLNCD4TregProlif.getMediansIterator();				tableKey.add(colCLNCD4TregProlif.getTitle() + " ");
		Iterator<Double> clnCD8TregProlifIter = colCLNCD8TregProlif.getMediansIterator();				tableKey.add(colCLNCD8TregProlif.getTitle() + " ");
		Iterator<Double> clnTh1ProlifIter = colCLNTh1Prolif.getMediansIterator();						tableKey.add(colCLNTh1Prolif.getTitle() + " ");
		Iterator<Double> clnTh2ProlifIter = colCLNTh2Prolif.getMediansIterator();						tableKey.add(colCLNTh2Prolif.getTitle() + " ");
		
		Iterator<Double> clnCD4TregEffectorIter = colCLNCD4TregEffector.getMediansIterator();			tableKey.add(colCLNCD4TregEffector.getTitle() + " ");
		Iterator<Double> clnCD8TregEffectorIter = colCLNCD8TregEffector.getMediansIterator();			tableKey.add(colCLNCD8TregEffector.getTitle() + " ");
		Iterator<Double> clnTh1EffectorIter = colCLNTh1Effector.getMediansIterator();					tableKey.add(colCLNTh1Effector.getTitle() + " ");
		Iterator<Double> clnTh2EffectorIter = colCLNTh2Effector.getMediansIterator();					tableKey.add(colCLNTh2Effector.getTitle() + " ");
		
		Iterator<Double> cumulativeCD4Th1KilledCirculatoryIter = colCumulativeCD4Th1KilledCirculatory.getMediansIterator(); tableKey.add(colCumulativeCD4Th1KilledCirculatory.getTitle() + " ");
		Iterator<Double> cumulativeCD4Th1KilledCLNIter = colCumulativeCD4Th1KilledCirculatory.getMediansIterator(); tableKey.add(colCumulativeCD4Th1KilledCLN.getTitle() + " ");
		Iterator<Double> cumulativeCD4Th1KilledCNSIter = colCumulativeCD4Th1KilledCirculatory.getMediansIterator(); tableKey.add(colCumulativeCD4Th1KilledCNS.getTitle() + " ");		
		Iterator<Double> cumulativeCD4Th1KilledSLOIter = colCumulativeCD4Th1KilledCirculatory.getMediansIterator(); tableKey.add(colCumulativeCD4Th1KilledSLO.getTitle() + " ");
		Iterator<Double> cumulativeCD4Th1KilledSpleenIter = colCumulativeCD4Th1KilledCirculatory.getMediansIterator(); tableKey.add(colCumulativeCD4Th1KilledSpleen.getTitle() + " ");
		
		Iterator<Double> cd4ThPrimedCirculatoryIter = colCD4ThPrimedCirculatory.getMediansIterator(); 	tableKey.add(colCD4ThPrimedCirculatory.getTitle() + " ");
		Iterator<Double> cd4ThPrimedCLNIter = colCD4ThPrimedCLN.getMediansIterator(); 					tableKey.add(colCD4ThPrimedCLN.getTitle() + " ");
		Iterator<Double> cd4ThPrimedCNSIter = colCD4ThPrimedCNS.getMediansIterator(); 					tableKey.add(colCD4ThPrimedCNS.getTitle() + " ");
		Iterator<Double> cd4ThPrimedSLOIter = colCD4ThPrimedSLO.getMediansIterator(); 					tableKey.add(colCD4ThPrimedSLO.getTitle() + " ");
		Iterator<Double> cd4ThPrimedSpleenIter = colCD4ThPrimedSpleen.getMediansIterator(); 			tableKey.add(colCD4ThPrimedSpleen.getTitle() + " ");

		Iterator<Double> cd4TregPrimedCirculatoryIter = colCD4TregPrimedCirculatory.getMediansIterator(); tableKey.add(colCD4TregPrimedCirculatory.getTitle() + " ");
		Iterator<Double> cd4TregPrimedCLNIter = colCD4TregPrimedCLN.getMediansIterator(); 				tableKey.add(colCD4TregPrimedCLN.getTitle() + " ");
		Iterator<Double> cd4TregPrimedCNSIter = colCD4TregPrimedCNS.getMediansIterator(); 				tableKey.add(colCD4TregPrimedCNS.getTitle() + " ");
		Iterator<Double> cd4TregPrimedSLOIter = colCD4TregPrimedSLO.getMediansIterator(); 				tableKey.add(colCD4TregPrimedSLO.getTitle() + " ");
		Iterator<Double> cd4TregPrimedSpleenIter = colCD4TregPrimedSpleen.getMediansIterator(); 		tableKey.add(colCD4TregPrimedSpleen.getTitle() + " ");

		Iterator<Double> cd8TregPrimedCirculatoryIter = colCD8TregPrimedCirculatory.getMediansIterator(); tableKey.add(colCD8TregPrimedCirculatory.getTitle() + " ");
		Iterator<Double> cd8TregPrimedCLNIter = colCD8TregPrimedCLN.getMediansIterator(); 				tableKey.add(colCD8TregPrimedCLN.getTitle() + " ");
		Iterator<Double> cd8TregPrimedCNSIter = colCD8TregPrimedCNS.getMediansIterator(); 				tableKey.add(colCD8TregPrimedCNS.getTitle() + " ");
		Iterator<Double> cd8TregPrimedSLOIter = colCD8TregPrimedCNS.getMediansIterator(); 				tableKey.add(colCD8TregPrimedSLO.getTitle() + " ");
		Iterator<Double> cd8TregPrimedSpleenIter = colCD8TregPrimedSpleen.getMediansIterator(); 		tableKey.add(colCD8TregPrimedSpleen.getTitle() + " ");
		
		Iterator<Double> neuronsKilledCumulativeIter = colNeuronsKilledCumulative.getMediansIterator(); tableKey.add(colNeuronsKilledCumulative.getTitle() + " ");
		
		Iterator<Double> dcmPeptidePresentationMBPIter = colDCMPeptidePresentationMBP.getMediansIterator(); tableKey.add(colDCMPeptidePresentationMBP.getTitle() + " ");
		Iterator<Double> dcmPeptidePresentationType1Iter = colDCMPeptidePresentationType1.getMediansIterator(); tableKey.add(colDCMPeptidePresentationType1.getTitle() + " ");
		Iterator<Double> dcmPeptidePresentationBothIter = colDCMPeptidePresentationBoth.getMediansIterator(); tableKey.add(colDCMPeptidePresentationBoth.getTitle() + " ");
		Iterator<Double> dcmPeptidePresentationNoneIter = colDCMPeptidePresentationNone.getMediansIterator(); tableKey.add(colDCMPeptidePresentationNone.getTitle() + " ");
		Iterator<Double> dcmPeptidePresentationTotalIter = colDCMPeptidePresentationTotal.getMediansIterator(); tableKey.add(colDCMPeptidePresentationTotal.getTitle() + " ");
		
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
			
			output.append(cnsCD4Th1Iter.next());	output.append(" ");
			output.append(cnsCD4Th2Iter.next());	output.append(" ");
			
			output.append(cd4ThSpecificityIter.next());			output.append(" ");
			output.append(cd4Th1SpecificityIter.next());		output.append(" ");
			output.append(cd4Th2SpecificityIter.next());		output.append(" ");
			
			output.append(cumulativeTh1KilledIter.next()); output.append(" ");
			
			output.append(clnDCPolarizationType1Iter.next()); output.append(" ");
			output.append(clnDCPolarizationType2Iter.next()); output.append(" ");
			
			output.append(colCumulativeCNSDCType1Iter.next()); output.append(" ");
			output.append(colCumulativeCNSDCType2Iter.next()); output.append(" ");
			
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
	 * Returns a string that depicts the number of each column, and what that column contains. 
	 */
	public String getTableKey()
	{
		if(tableKey.size() == 0)
			throw new RuntimeException("cannot retrieive the key before the table has been generated");
		
		StringBuilder output = new StringBuilder();
		for(int i = 0; i < tableKey.size(); i++)
		{
			output.append(i + 1 + " - " + tableKey.get(i) + "\n");
		}
		
		return output.toString();
	}
	
	/**
	 * A wrapper class that forms a 'column' from a number of MedianDataDouble objects, which themselves can hold the various values that have occured for
	 * a variable and find the median. 
	 * @author mark
	 *
	 */
	public class MedianDataColumnDouble
	{
		private String title;
		private MedianDataDouble[] data;
		
		public MedianDataColumnDouble(String title)
		{	this.title = title; 		}
		
		public String getTitle()
		{	return title;	}
		
		/**
		 * Will add all the elements in source to the data structure. 
		 */
		public void logColumnValues(DataColumnDouble source)
		{
			if(data == null)
			{	// no data has been logged yet
				data = new MedianDataDouble[source.getLength()];
				for(int i = 0; i < data.length; i++)
					data[i] = new MedianDataDouble();
			}
				
			// retrieve all values in source and log them in data. 
			for(int i = 0; i < source.data.size(); i++)
			{
				data[i].logValue(source.data.get(i));
			}
		}
		
		public void logColumnValues(DataColumnInteger source)
		{
			if(data == null)
			{	// no data has been logged yet
				data = new MedianDataDouble[source.getLength()];
				for(int i = 0; i < data.length; i++)
					data[i] = new MedianDataDouble();
			}
				
			// retrieve all values in source and log them in data. 
			for(int i = 0; i < source.data.size(); i++)
			{
				data[i].logValue(source.data.get(i));
			}
		}
		
		public Double[] findMedians()
		{
			Double[] medians = new Double[data.length];		// will store median values in here
			
			for(int i = 0; i < data.length; i++)
			{	// cycle through each variable in 'data', get the median, and store it in the 'medians' array.
				medians[i] = data[i].findMedian();
			}
			
			return medians;
		}	
		
		public Iterator<Double> getMediansIterator()
		{
			Double[] medians = findMedians();
			List<Double> mediansList = Arrays.asList(medians);
			return mediansList.iterator();
		}
	}
}
