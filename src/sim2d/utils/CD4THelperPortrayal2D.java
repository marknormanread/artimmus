package sim2d.utils;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim2d.cell.impl.CD4THelper;
import sim2d.cell.impl.TCell_Impl;
import sim2d.cell.impl.Th1Polarization;
import sim2d.cell.impl.Th2Polarization;

/**
 * Class is a modified copy of OvalPortrayal2D from the MASON toolkit. Modification is in relation to a specific class that this is intended to portray,
 * depending on the state of the object of that class it will be drawn in different colours. 
 * 
 * @author mark
 *
 */
public class CD4THelperPortrayal2D extends SimplePortrayal2D
    {
    public Paint paintTh1;
    public Paint paintTh2;
    public Paint paintOther;				// the other states that a T cell may exist in. 
    public double scale;
    //boolean drawSmaller = Display2D.isMacOSX && !Display2D.javaVersion.startsWith("1.3"); // fix a bug in OS X
    
    public CD4THelperPortrayal2D(Paint th1Col, Paint th2Col, Paint otherCol) {this(th1Col, th2Col, otherCol, 1.0);}
    
    
    public CD4THelperPortrayal2D(Paint th1Col, Paint th2Col, Paint otherCol, double scale)
    {
	    this.paintTh1 = th1Col;
	    this.paintTh2 = th2Col;
	    this.paintOther = otherCol;
	    this.scale = scale;
    } 
    
    // assumes the graphics already has its color set
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
        {
        final double width = info.draw.width*scale;
        final double height = info.draw.height*scale;

        CD4THelper tCell = (CD4THelper) object;
        
        if(tCell.getMaturity() == TCell_Impl.Maturity.Effector)
        {
        	if(tCell.getPolarization() instanceof Th1Polarization)
        		graphics.setPaint(paintTh1);
        	else if (tCell.getPolarization() instanceof Th2Polarization)
        		graphics.setPaint(paintTh2);
        }
        else
        	graphics.setPaint(paintOther);
            
        final int x = (int)(info.draw.x - width / 2.0);
        final int y = (int)(info.draw.y - height / 2.0);
        int w = (int)(width);
        int h = (int)(height);

        // draw centered on the origin
        graphics.fillOval(x,y,w, h);
        }

    /** If drawing area intersects selected area, add last portrayed object to the bag */
    public boolean hitObject(Object object, DrawInfo2D range)
        {
        final double SLOP = 1.0;  // need a little extra area to hit objects
        final double width = range.draw.width*scale;
        final double height = range.draw.height*scale;
        Ellipse2D.Double ellipse = new Ellipse2D.Double( range.draw.x-width/2-SLOP, range.draw.y-height/2-SLOP, width+SLOP*2,height+SLOP*2 );
        return ( ellipse.intersects( range.clip.x, range.clip.y, range.clip.width, range.clip.height ) );
        }

}
