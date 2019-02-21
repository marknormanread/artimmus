package sim2d;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.Schedule;
import sim.portrayal.Inspector;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim2d.cell.impl.CD4THelper;
import sim2d.cell.impl.CD4Treg;
import sim2d.cell.impl.CD8Treg;
import sim2d.cell.impl.Neuron;
import sim2d.cell.impl.CNSMacrophage;
import sim2d.cell.impl.DendriticCell;
import sim2d.cell.impl.DendriticCellMigrates;
import sim2d.compartment.Compartment_Impl2D;
import sim2d.compartment.Spleen2D;
import sim2d.filesystem.FileSystemIO;
import sim2d.utils.APCPolarizationDataLogger;
import sim2d.utils.APCStateDataLogger;
import sim2d.utils.CD4THelperPortrayal2D;
import sim2d.utils.CD4TregStateDataLogger;
import sim2d.utils.CD8TregStateDataLogger;
import sim2d.utils.SpleenTCellDataLogger;
import sim2d.utils.SystemWideDataLogger;
import sim2d.utils.THelperCellStateDataLogger;


public class Treg2DSim_GUI extends GUIState
{
	public static boolean showCellPopulationDynamics = true;
	
	public Console console;					// we keep a reference to the console so that we can attach JFrames to it.
	
    public Display2D cnsDisplay;
    public JFrame cnsDisplayFrame;
	public SparseGridPortrayal2D cnsCellsGridPortrayal = new SparseGridPortrayal2D();
	public FastValueGridPortrayal2D cnsSDAPortrayal = new FastValueGridPortrayal2D("SDA");
	public FastValueGridPortrayal2D cnsType1Portrayal = new FastValueGridPortrayal2D("Type1");
	public FastValueGridPortrayal2D cnsType2Portrayal = new FastValueGridPortrayal2D("Type2");
	
    public Display2D clnDisplay;
    public JFrame clnDisplayFrame;
	public SparseGridPortrayal2D clnCellsGridPortrayal = new SparseGridPortrayal2D();
	public FastValueGridPortrayal2D clnSDAPortrayal = new FastValueGridPortrayal2D("SDA");
	public FastValueGridPortrayal2D clnType1Portrayal = new FastValueGridPortrayal2D("Type1");
	public FastValueGridPortrayal2D clnType2Portrayal = new FastValueGridPortrayal2D("Type2");
	
    public Display2D circulationDisplay;
    public JFrame circulationDisplayFrame;
	public SparseGridPortrayal2D circulationCellsGridPortrayal = new SparseGridPortrayal2D();
	public FastValueGridPortrayal2D circulationSDAPortrayal = new FastValueGridPortrayal2D("SDA");
	public FastValueGridPortrayal2D circulationType1Portrayal = new FastValueGridPortrayal2D("Type1");
	public FastValueGridPortrayal2D circulationType2Portrayal = new FastValueGridPortrayal2D("Type2");
	
    public Display2D sloDisplay;
    public JFrame sloDisplayFrame;
	public SparseGridPortrayal2D sloCellsGridPortrayal = new SparseGridPortrayal2D();
	public FastValueGridPortrayal2D sloSDAPortrayal = new FastValueGridPortrayal2D("SDA");
	public FastValueGridPortrayal2D sloType1Portrayal = new FastValueGridPortrayal2D("Type1");
	public FastValueGridPortrayal2D sloType2Portrayal = new FastValueGridPortrayal2D("Type2");
	
    public Display2D spleenDisplay;
    public JFrame spleenDisplayFrame;
	public SparseGridPortrayal2D spleenCellsGridPortrayal = new SparseGridPortrayal2D();
	public FastValueGridPortrayal2D spleenSDAPortrayal = new FastValueGridPortrayal2D("SDA");
	public FastValueGridPortrayal2D spleenType1Portrayal = new FastValueGridPortrayal2D("Type1");
	public FastValueGridPortrayal2D spleenType2Portrayal = new FastValueGridPortrayal2D("Type2");
	
	private Color sdaColour = Color.blue;
	private Color type1Colour = Color.red;
	private Color type2Colour = Color.white;
	
	private double sdaMax = 15.0;
	private double type1Max = 15.0;
	private double type2Max = 15.0;
	
	public JFrame populationsFrame;
	public SystemWideDataLogger populationsDL;
	
	public JFrame cd4ThFrame;
	public THelperCellStateDataLogger cd4ThDL;
	
	public JFrame cd4TregFrame;
	public CD4TregStateDataLogger cd4TregDL;
	
	public JFrame cd8TregFrame;
	public JFrame th1KilledFrame;
	public CD8TregStateDataLogger cd8TregDL;
		
	public JFrame dcSLOFrame;
	public APCStateDataLogger dcSLODL;
	
	public JFrame dcCLNFrame;
	public APCStateDataLogger dcCLNDL;
	
	public JFrame cnsmCNSFrame;
	public APCStateDataLogger cnsmCNSDL;
	
	public JFrame dcCNSFrame;
	public APCStateDataLogger dcCNSDL;
	
	public JFrame dcSpleenFrame;
	public APCStateDataLogger dcSpleenDL;
	
	public JFrame dcCLNPolarizationFrame;
	public APCPolarizationDataLogger dcCLNPolarizationDL;
	
	public JFrame dcSpleenTCellsFrame;
	public SpleenTCellDataLogger dcSpleenTCellsDL;
	
	public static Color CD4ThCol = Color.cyan;
    public static Color CD4Th1Col = Color.red;
    public static Color CD4Th2Col = Color.white;
    public static Color CD4TregCol = Color.yellow;
    public static Color CD8TregCol = Color.green;
    public static Color DCCol = Color.magenta;
    public static Color DCMigratesCol = Color.MAGENTA;
    public static Color CNSMCol = Color.pink;
    public static Color CNS = Color.darkGray;
    private Map<String, Color> colsMap = new HashMap<String, Color>();
	
	public Treg2DSim_GUI(long seed)
	{	
		super(new TregSimulation(seed, 
								TregSimulation.Dimension.TwoD, 
								FileSystemIO.openXMLFile("parameters-testEfficiency.xml"),
								1000000
								));
		
//		super(new TregSimulation(seed, 
//		TregSimulation.Dimension.TwoD, 
//		FileSystemIO.openXMLFile("parameters.xml"),
//		1000000, 
//		1000.0
//		));
		
		
		TregSimulation.sim.setupSimulationParameters();						// essential that we do this here.
		colsMap.put("CD4Th", CD4ThCol);
		colsMap.put("CD4Th1", CD4Th1Col);
		colsMap.put("CD4Th2", CD4Th2Col);
		colsMap.put("CD4Treg", CD4TregCol);
		colsMap.put("CD8Treg", CD8TregCol);
		colsMap.put("DC", DCCol);
		colsMap.put("CNSM", CNSMCol);
		colsMap.put("CNS", CNS);
	}	

	public static String getName()
	{ 	return "The EAE and Treg Simulation.";		}
	
	public static Object getInfo()
	{	return "No information at this time.";		}
	
    public void start()
    {

    	// this forces the simulation to re-read the parameters file, such that any changes made by the user can be reflected immediately
    	// without having to restart the GUI. 
    	//((TregSimulation)this.state).setParametersDocument(FileSystemIO.openXMLFile("parameters.xml"));
    	((TregSimulation)this.state).setParametersDocument(FileSystemIO.openXMLFile("parameters-testEfficiency.xml"));
    	super.start();
    	

    	setupPortrayals();
    	setupGraphs();
    }
    
    private void setupGraphs()
    {	    
    	populationsDL = new SystemWideDataLogger("System wide", colsMap);
	    state.schedule.scheduleRepeating(Schedule.EPOCH, 2, populationsDL);
	    
	    if(showCellPopulationDynamics)
	    {
	    	cd4ThDL = new THelperCellStateDataLogger("CD4Th Population Breakdown");
		    state.schedule.scheduleRepeating(Schedule.EPOCH, 2, cd4ThDL);
		    
	    	cd4TregDL = new CD4TregStateDataLogger();
		    state.schedule.scheduleRepeating(Schedule.EPOCH, 2, cd4TregDL);
		    
	    	cd8TregDL = new CD8TregStateDataLogger();
		    state.schedule.scheduleRepeating(Schedule.EPOCH, 2, cd8TregDL);
		    
		    dcSLODL = new APCStateDataLogger("DC SLO Cells", DendriticCell.class, ((TregSimulation)state).slo);
		    state.schedule.scheduleRepeating(Schedule.EPOCH, 2, dcSLODL);
		    
		    dcCLNDL = new APCStateDataLogger("DC CLN Cells", DendriticCell.class, ((TregSimulation)state).cln);
		    state.schedule.scheduleRepeating(Schedule.EPOCH, 2, dcCLNDL);
		    
		    cnsmCNSDL = new APCStateDataLogger("CNSM Cells", CNSMacrophage.class, ((TregSimulation)state).cns);
		    state.schedule.scheduleRepeating(Schedule.EPOCH, 2, cnsmCNSDL);
		    
		    dcCNSDL = new APCStateDataLogger("DC CNS Cells", DendriticCell.class, ((TregSimulation)state).cns);
		    state.schedule.scheduleRepeating(Schedule.EPOCH, 2, dcCNSDL);
		    
		    dcSpleenDL = new APCStateDataLogger("DC Spleen Cells", DendriticCell.class, ((TregSimulation)state).spleen);
		    state.schedule.scheduleRepeating(Schedule.EPOCH, 2, dcSpleenDL);
		    
		    dcSpleenTCellsDL = new SpleenTCellDataLogger("Spleen T Cells");
		    state.schedule.scheduleRepeating(Schedule.EPOCH, 2, dcSpleenTCellsDL);
	    	
	    	dcCLNPolarizationDL = new APCPolarizationDataLogger("DC Polarization Numbers in the CLN", ((TregSimulation)state).cln);
		    state.schedule.scheduleRepeating(Schedule.EPOCH, 2, dcCLNPolarizationDL);
	    }
	    
	    if(populationsFrame != null)
	    {
	    	console.unregisterFrame(populationsFrame);
	    	populationsFrame.dispose();
	    	populationsFrame.setVisible(false);   
	    	populationsFrame = null;
	    }
	    if(cd4ThFrame != null)
	    {
	    	console.unregisterFrame(cd4ThFrame);
	    	cd4ThFrame.dispose();
	    	cd4ThFrame.setVisible(false);   
	    	cd4ThFrame = null;
	    }
	    if(cd4TregFrame != null)
	    {
	    	console.unregisterFrame(cd4TregFrame);
	    	cd4TregFrame.dispose();
	    	cd4TregFrame.setVisible(false);   
	    	cd4TregFrame = null;
	    }
	    if(cd8TregFrame != null)
	    {
	    	console.unregisterFrame(cd8TregFrame);
	    	cd8TregFrame.dispose();
	    	cd8TregFrame.setVisible(false);   
	    	cd8TregFrame = null;
	    }
	    if(th1KilledFrame != null)
	    {
	    	console.unregisterFrame(th1KilledFrame);
	    	th1KilledFrame.dispose();
	    	th1KilledFrame.setVisible(false);   
	    	th1KilledFrame = null;
	    }	
	    if(dcSLOFrame != null)
	    {
	    	console.unregisterFrame(dcSLOFrame);
	    	dcSLOFrame.dispose();
	    	dcSLOFrame.setVisible(false);   
	    	dcSLOFrame = null;
	    }
	    if(dcCLNFrame != null)
	    {
	    	console.unregisterFrame(dcCLNFrame);
	    	dcCLNFrame.dispose();
	    	dcCLNFrame.setVisible(false);   
	    	dcCLNFrame = null;
	    }
	    if(cnsmCNSFrame != null)
	    {
	    	console.unregisterFrame(cnsmCNSFrame);
	    	cnsmCNSFrame.dispose();
	    	cnsmCNSFrame.setVisible(false);   
	    	cnsmCNSFrame = null;
	    }
	    if(dcCNSFrame != null)
	    {
	    	console.unregisterFrame(dcCNSFrame);
	    	dcCNSFrame.dispose();
	    	dcCNSFrame.setVisible(false);   
	    	dcCNSFrame = null;
	    }
	    if(dcSpleenFrame != null)
	    {
	    	console.unregisterFrame(dcSpleenFrame);
	    	dcSpleenFrame.dispose();
	    	dcSpleenFrame.setVisible(false);   
	    	dcSpleenFrame = null;
	    	
	    	console.unregisterFrame(dcSpleenTCellsFrame);
	    	dcSpleenTCellsFrame.dispose();
	    	dcSpleenTCellsFrame.setVisible(false);   
	    	dcSpleenTCellsFrame = null;
	    }	
	    if(dcCLNPolarizationFrame != null)
	    {
	    	console.unregisterFrame(dcCLNPolarizationFrame);
	    	dcCLNPolarizationFrame.dispose();
	    	dcCLNPolarizationFrame.setVisible(false);   
	    	dcCLNPolarizationFrame = null;
	    }
    }
    
    
    private void setupPortrayals()
    {	// tell the portrayals what to portray and how to portray them

    	
    	// set the portrayals to portray the cellsGrid in their respective compartments.
    	cnsCellsGridPortrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).cns).cellsGrid );
    	clnCellsGridPortrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).cln).cellsGrid );
    	circulationCellsGridPortrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).circulation).cellsGrid );
    	sloCellsGridPortrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).slo).cellsGrid );	
    	spleenCellsGridPortrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).spleen).cellsGrid );
    	
    	/* molecule field portrayals for the CNS */
    	cnsSDAPortrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).cns).sdaGrid );
    	cnsSDAPortrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 , sdaMax, Color.black, sdaColour) );
    	cnsType1Portrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).cns).type1Grid );
    	cnsType1Portrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 , type1Max, Color.black, type1Colour) );
    	cnsType2Portrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).cns).type2Grid );
    	cnsType2Portrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 , type2Max, Color.black, type2Colour) );
    	
    	/* molecule field portrayals for the CLN */
    	clnSDAPortrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).cln).sdaGrid );
    	clnSDAPortrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 , sdaMax, Color.black, sdaColour) );
    	clnType1Portrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).cln).type1Grid );
    	clnType1Portrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 , type1Max, Color.black, type1Colour) );
    	clnType2Portrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).cln).type2Grid );
    	clnType2Portrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 , type2Max, Color.black, type2Colour) );
    	
    	/* molecule field portrayals for the circulation */
    	circulationSDAPortrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).circulation).sdaGrid );
    	circulationSDAPortrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 , sdaMax, Color.black, sdaColour) );
    	circulationType1Portrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).circulation).type1Grid );
    	circulationType1Portrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 , type1Max, Color.black, type1Colour) );
    	circulationType2Portrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).circulation).type2Grid );
    	circulationType2Portrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 , type2Max, Color.black, type2Colour) );
    	
    	/* molecule field portrayals for the SLO */
    	sloSDAPortrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).slo).sdaGrid );
    	sloSDAPortrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 ,sdaMax, Color.black, sdaColour) );
    	sloType1Portrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).slo).type1Grid );
    	sloType1Portrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 , type1Max, Color.black, type1Colour) );
    	sloType2Portrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).slo).type2Grid );
    	sloType2Portrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 , type2Max, Color.black, type2Colour) );
    	
    	/* molecule field portrayals for the Spleen */
    	spleenSDAPortrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).spleen).sdaGrid );
    	spleenSDAPortrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 ,sdaMax, Color.black, sdaColour) );
    	spleenType1Portrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).spleen).type1Grid );
    	spleenType1Portrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 , type1Max, Color.black, type1Colour) );
    	spleenType2Portrayal.setField( ((Compartment_Impl2D)((TregSimulation)state).spleen).type2Grid );
    	spleenType2Portrayal.setMap( new sim.util.gui.SimpleColorMap( 0.0 , type2Max, Color.black, type2Colour) );
    	
    	/* set the colour to portray cells in */
    	setupCellsPortrayal(cnsCellsGridPortrayal);
    	setupCellsPortrayal(clnCellsGridPortrayal);
    	setupCellsPortrayal(circulationCellsGridPortrayal);
    	setupCellsPortrayal(sloCellsGridPortrayal);
    	
    	setupCellsPortrayal(spleenCellsGridPortrayal);
    		
	    // reschedule the displayers
    	cnsDisplay.reset();
    	clnDisplay.reset();
    	circulationDisplay.reset();
    	sloDisplay.reset();
    	spleenDisplay.reset();
	    
	    cnsDisplay.setBackdrop(Color.BLACK);
	    clnDisplay.setBackdrop(Color.BLACK);
	    circulationDisplay.setBackdrop(Color.BLACK);
	    sloDisplay.setBackdrop(Color.BLACK);
    	spleenDisplay.setBackdrop(Color.BLACK);
	    
	
	    // redraw the displays
	    cnsDisplay.repaint();
	    clnDisplay.repaint();
	    circulationDisplay.repaint();
	    sloDisplay.repaint();
    	spleenDisplay.repaint();
    }
    

    
    /**
     * Sets up the specified portrayal to draw cells in the correct colours.
     */
    private void setupCellsPortrayal(SparseGridPortrayal2D portrayal)
    {
    	portrayal.setPortrayalForClass(CD4THelper.class, new CD4THelperPortrayal2D(CD4Th1Col, CD4Th2Col, CD4ThCol) );
    	portrayal.setPortrayalForClass(CD4Treg.class, new sim.portrayal.simple.OvalPortrayal2D(CD4TregCol) );
    	portrayal.setPortrayalForClass(CD8Treg.class, new sim.portrayal.simple.OvalPortrayal2D(CD8TregCol) );
    	portrayal.setPortrayalForClass(DendriticCell.class, new sim.portrayal.simple.OvalPortrayal2D(DCCol) );
    	portrayal.setPortrayalForClass(DendriticCellMigrates.class, new sim.portrayal.simple.OvalPortrayal2D(DCMigratesCol) );
    	portrayal.setPortrayalForClass(CNSMacrophage.class, new sim.portrayal.simple.OvalPortrayal2D(CNSMCol) );
    	portrayal.setPortrayalForClass(Neuron.class, new sim.portrayal.simple.OvalPortrayal2D(CNS) );
    	portrayal.setPortrayalForNull(new sim.portrayal.simple.OvalPortrayal2D(Color.black));
    	portrayal.setPortrayalForRemainder(new sim.portrayal.simple.OvalPortrayal2D(Color.black));
    }
    
    /*
     * Called when display windows and the such need initialization.  
     */
    public void init(Controller c)
    {
    	int width = 300;
    	int height = 300;
    	int height_extra = 27;
	    super.init(c);
		
	    
	    /* make the displayers */
	    cnsDisplay = 			new Display2D(width, height, this, 1);
	    clnDisplay = 			new Display2D(width, height, this, 1);
	    circulationDisplay = 	new Display2D(width, height, this, 1);
	    sloDisplay = 			new Display2D(width,height,this, 1);
    	spleenDisplay = 		new Display2D(width, height, this, 1);
	    
		/* turn off clipping */
	    cnsDisplay.setClipping(false);
	    clnDisplay.setClipping(false);
	    circulationDisplay.setClipping(false);
	    sloDisplay.setClipping(false);
    	spleenDisplay.setClipping(false);

	    /* set up the compartment display frames */
	    cnsDisplayFrame = cnsDisplay.createFrame();
	    clnDisplayFrame = clnDisplay.createFrame();
	    circulationDisplayFrame = circulationDisplay.createFrame();
	    sloDisplayFrame = sloDisplay.createFrame();
    	spleenDisplayFrame = spleenDisplay.createFrame();
	    
	    cnsDisplayFrame.setTitle("CNS Compartment");
	    clnDisplayFrame.setTitle("CLN Compartment");
	    circulationDisplayFrame.setTitle("Circulation Compartment");
	    sloDisplayFrame.setTitle("SLO Compartment");
    	spleenDisplayFrame.setTitle("Spleen Compartment");
	    
	    c.registerFrame(cnsDisplayFrame);   					// register the frame so it appears in the "Display" list
	    c.registerFrame(clnDisplayFrame);   					// register the frame so it appears in the "Display" list
	    c.registerFrame(circulationDisplayFrame);   			// register the frame so it appears in the "Display" list
	    c.registerFrame(sloDisplayFrame);   					// register the frame so it appears in the "Display" list
    	c.registerFrame(spleenDisplayFrame);   					// register the frame so it appears in the "Display" list
	    
	    cnsDisplayFrame.setVisible(true);
	    clnDisplayFrame.setVisible(true);
	    circulationDisplayFrame.setVisible(true);
	    sloDisplayFrame.setVisible(true);
    	spleenDisplayFrame.setVisible(true);
	    
	    circulationDisplay.attach( circulationSDAPortrayal, "SDA", false );
	    circulationDisplay.attach( circulationType1Portrayal, "Type1", false );
	    circulationDisplay.attach( circulationType2Portrayal, "Type2", false );
	    circulationDisplay.attach( circulationCellsGridPortrayal, "Cells", true );
	    
	    cnsDisplay.attach( cnsSDAPortrayal, "SDA", false );
	    cnsDisplay.attach( cnsType1Portrayal, "Type1", false );
	    cnsDisplay.attach( cnsType2Portrayal, "Type2", false );
	    cnsDisplay.attach( cnsCellsGridPortrayal, "Cells", true );
	    
	    clnDisplay.attach( clnSDAPortrayal, "SDA", false );
	    clnDisplay.attach( clnType1Portrayal, "Type1", false );
	    clnDisplay.attach( clnType2Portrayal, "Type2", false );
	    clnDisplay.attach( clnCellsGridPortrayal, "Cells", true );
	    	    
	    sloDisplay.attach( sloSDAPortrayal, "SDA", false );
	    sloDisplay.attach( sloType1Portrayal, "Type1", false );
	    sloDisplay.attach( sloType2Portrayal, "Type2", false );
	    sloDisplay.attach( sloCellsGridPortrayal, "Cells", true );
	    
    	{
		    spleenDisplay.attach( spleenSDAPortrayal, "SDA", false );
		    spleenDisplay.attach( spleenType1Portrayal, "Type1", false );
		    spleenDisplay.attach( spleenType2Portrayal, "Type2", false );
		    spleenDisplay.attach( spleenCellsGridPortrayal, "Cells", true );
    	}
	    
	    /* place the display frames at the correct place on the screen */
	    int bleed = 80;			// TODO remove bleed.
	    cnsDisplayFrame.setBounds(0, 0, width+bleed, height + bleed);											// params: (topleft x, topleft y, width, height).
	    clnDisplayFrame.setBounds(0, height + height_extra + bleed, width+bleed, height+bleed);				// params: (topleft x, topleft y, width, height).
	    circulationDisplayFrame.setBounds(width+bleed, 0, width + bleed, height+bleed);						// params: (topleft x, topleft y, width, height).
	    sloDisplayFrame.setBounds(width+bleed, height + height_extra + bleed, width+bleed, height+bleed);		// params: (topleft x, topleft y, width, height).
    	spleenDisplayFrame.setBounds(2*(width+bleed), height + height_extra + bleed, width+bleed, height+bleed); // params: (topleft x, topleft y, width, height).
    }
    
    /**
     * Method called when the simulation terminates. At this point we need to draw the graphs. 
     */
    public void finish()
    {  	
    	super.finish();
    	populationsFrame = populationsDL.compileGraph();
		populationsFrame.setBounds(620,0, 620, 380);
		populationsFrame.setVisible(true);
		
		if(showCellPopulationDynamics)
		{
			cd4ThFrame =cd4ThDL.graph.compileGraph();
			cd4ThFrame.setBounds(620,0, 620, 380);
			cd4ThFrame.setVisible(true);
						
			cd4TregFrame =cd4TregDL.graph.compileGraph();
			cd4TregFrame.setBounds(620,0, 620, 380);
			cd4TregFrame.setVisible(true);
			
			cd8TregFrame =cd8TregDL.popnGraph.compileGraph();
			cd8TregFrame.setBounds(620,0, 620, 380);
			cd8TregFrame.setVisible(true);
			
			th1KilledFrame =cd8TregDL.killedGraph.compileGraph();
			th1KilledFrame.setBounds(620,0, 620, 380);
			th1KilledFrame.setVisible(true);
			
			dcSLOFrame = dcSLODL.graph.compileGraph();
			dcSLOFrame.setBounds(620,0, 620, 380);
			dcSLOFrame.setVisible(true);
			
			dcCLNFrame = dcCLNDL.graph.compileGraph();
			dcCLNFrame.setBounds(620,0, 620, 380);
			dcCLNFrame.setVisible(true);
						
			cnsmCNSFrame = cnsmCNSDL.graph.compileGraph();
			cnsmCNSFrame.setBounds(620,0, 620, 380);
			cnsmCNSFrame.setVisible(true);
			
			dcCNSFrame = dcCNSDL.graph.compileGraph();
			dcCNSFrame.setBounds(620,0, 620, 380);
			dcCNSFrame.setVisible(true);
			
			dcSpleenFrame = dcSpleenDL.graph.compileGraph();
			dcSpleenFrame.setBounds(620,0, 620, 380);
			dcSpleenFrame.setVisible(true);
			
			dcSpleenTCellsFrame = dcSpleenTCellsDL.graph.compileGraph();
			dcSpleenTCellsFrame.setBounds(620,0, 620, 380);
			dcSpleenTCellsFrame.setVisible(true);
	    	
			dcCLNPolarizationFrame = dcCLNPolarizationDL.graph.compileGraph();
			dcCLNPolarizationFrame.setBounds(620,0, 620, 380);
			dcCLNPolarizationFrame.setVisible(true);
		}
    }
    
    public Object getSimulationInspectedObject()
    {
    	return state;		// stored in the GUIState class, which we are a subclass of. It corresponds with a TregSim object.
    }

    /*
     * Used to set the inspector state to ****NOT*** 'volatile'. We do not require the inspector to update in real time, once the model parameters are set, the run
     * continues until it is stopped. 
     */
    public Inspector getInspector()
    {
	    Inspector i = super.getInspector();
	    i.setVolatile(false);				// set this to true if you want the inspector to update. 
	    return i;
    }
    

    public void quit()
    {
    	super.quit();

    	if (sloDisplayFrame!=null) sloDisplayFrame.dispose();
    	sloDisplayFrame = null;
    	sloDisplay = null;
    	
    	if (cnsDisplayFrame!=null) cnsDisplayFrame.dispose();
    	cnsDisplayFrame = null;
    	cnsDisplay = null;
    	
    	if (clnDisplayFrame!=null) clnDisplayFrame.dispose();
    	clnDisplayFrame = null;
    	clnDisplay = null;
    	
    	if (circulationDisplayFrame!=null) circulationDisplayFrame.dispose();
    	circulationDisplayFrame = null;
    	circulationDisplay = null;
    	
    	if (populationsFrame != null) populationsFrame.dispose();
    	populationsFrame = null;
    	
    	if (cd4ThFrame != null) cd4ThFrame.dispose();
    	cd4ThFrame = null;
    	
    	if (cd4TregFrame != null) cd4TregFrame.dispose();
    	cd4TregFrame = null;
    	
    	if (cd8TregFrame != null) cd8TregFrame.dispose();
    	cd8TregFrame = null;
    	
    	if(th1KilledFrame != null) th1KilledFrame.dispose();
    	th1KilledFrame = null;
    	
    	if (dcSLOFrame != null) dcSLOFrame.dispose();
    	dcSLOFrame = null;
    	
    	if (dcCLNFrame != null) dcCLNFrame.dispose();
    	dcCLNFrame = null;
    	
    	if (cnsmCNSFrame != null) cnsmCNSFrame.dispose();
    	cnsmCNSFrame = null;
    	
    	if (dcCNSFrame != null) dcCNSFrame.dispose();
    	dcCNSFrame = null;
    	
    	
    	if (dcSpleenFrame != null) dcSpleenFrame.dispose();
    	dcSpleenFrame = null;
    	
    	if (dcSpleenTCellsFrame != null) dcSpleenTCellsFrame.dispose();
    	dcSpleenTCellsFrame = null;
    	
    	if (dcCLNPolarizationFrame != null) dcCLNPolarizationFrame.dispose();
    	dcCLNPolarizationFrame = null;
    }
    
    
    /**
     * The main method that starts a TregSimulation with the GUI.
     */
    public static void main(String[] args)
    {
	    Treg2DSim_GUI sim = new Treg2DSim_GUI(100);
        
	    Console c = new Console(sim);
	    sim.console = c;
	    c.setVisible(true);
    }
}
