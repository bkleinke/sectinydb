// $Id: GraphDisplayPanel.java,v 1.2 2003/10/07 21:46:08 idgay Exp $

/*									tab:4
 * "Copyright (c) 2000-2003 The Regents of the University  of California.  
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice, the following
 * two paragraphs and the author appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
 *
 * Copyright (c) 2002-2003 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */


/**
 * @author Wei Hong
 */

//*******************************************************
//*******************************************************
//This class is essentially a JPanel with the paint() function overridden
//IT cycles through the nodes, edges, nodePainters, edgePainters, and screenPainters
//and tells them all to draw on its graphics object
//Then I add some code for scaling and zooming the screen
//and converting from node coordinates to screen coordinates
//*******************************************************
//*******************************************************

package net.tinyos.tinydb.topology;

import net.tinyos.tinydb.topology.util.*;
import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;

public class GraphDisplayPanel extends javax.swing.JPanel
{
    //the following are member variables needed only for painting
    protected Image doubleBufferImage;
	protected Dimension doubleBufferImageSize;
	protected Graphics doubleBufferGraphic;
	protected boolean fitToScreenAutomatically = false;
	
	//the following are needed for scaling and zooming
	protected double xScale=534;
	protected double xScaleIntercept = -101;
	protected double yScale = 534;
	protected double yScaleIntercept=-65;
	protected double xMargin=10, yMargin=10;//value of 10 -> margin width of 10% of the screensize
	protected boolean screenIsEmpty=true;


	public GraphDisplayPanel()
	{
		//{{INIT_CONTROLS
		setLayout(null);
		Insets ins = getInsets();
		setSize(ins.left + ins.right + 430,ins.top + ins.bottom + 270);
		//}}

		//{{REGISTER_LISTENERS
		//}}
	}

              //----------------------------------------------------------------
              //PAINT
              //recall that paint() is called the first time the window
              //needs to be drawn, or if something damages the window,
              //and in this case by RefreshScreenNow()
     public void paint(Graphics g)
     {
    	super.paint(g);//first paint the panel normally
     
		//the following block of code is used
		//if this is the first time being drawn or if the window was resized
		//Otherwise we don't creat a new buffer
		Dimension d = getSize();
		
		if ((doubleBufferImage == null) || (d.width != doubleBufferImageSize.width) || (d.height != doubleBufferImageSize.height))//if this is the first time being drawn or if the window was resized
		{
			doubleBufferImage = createImage(d.width, d.height);
			doubleBufferImageSize = d;
			if (doubleBufferGraphic != null) {
				doubleBufferGraphic.dispose();
			}
			doubleBufferGraphic = doubleBufferImage.getGraphics();
			doubleBufferGraphic.setFont(getFont());
		}

		doubleBufferGraphic.setColor(Color.white);
		doubleBufferGraphic.fillRect(0, 0, d.width, d.height);
	
		if( (fitToScreenAutomatically) || (screenIsEmpty)) 
		{     //if the user wants all the nodes on the screen, or if these are the first nodes on the screen, fit all nodes to the visible area
			//FitToScreen();
			;
		}
		
		      //draw things on the screen before any nodes or edges appear
		MainClass.displayManager.PaintUnderScreen(doubleBufferGraphic);

		      //draw all the nodes
		DisplayManager.NodeInfo nodeDisplayInfo;
		int xCoord, yCoord, imageWidth, imageHeight;
		for(Enumeration nodes = MainClass.displayManager.GetNodeInfo(); nodes.hasMoreElements();) 
		{
			nodeDisplayInfo = (DisplayManager.NodeInfo)nodes.nextElement();
			  //figure out where to put the node on the screen
			xCoord = ScaleNodeXCoordToScreenCoord(MainClass.locationAnalyzer.GetX(nodeDisplayInfo.GetNodeNumber()));
			yCoord = ScaleNodeYCoordToScreenCoord(MainClass.locationAnalyzer.GetY(nodeDisplayInfo.GetNodeNumber()));
	              //if that spot is not on the visible area, don't draw it at all
			if(	(xCoord > this.getSize().getWidth()) ||
				(yCoord > this.getSize().getHeight()))
			{
				continue;
			}
			imageWidth = (int)Math.max(20,xScale*nodeDisplayInfo.GetImageWidth()/100);
			imageHeight = (int)Math.max(20,yScale*nodeDisplayInfo.GetImageHeight()/100);
			MainClass.displayManager.PaintAllNodes(nodeDisplayInfo.GetNodeNumber(), xCoord-imageWidth/2, yCoord-imageHeight/2, xCoord+imageWidth/2, yCoord+imageHeight/2, doubleBufferGraphic);



		}
		
		      //draw all the edges
		DisplayManager.EdgeInfo edgeDisplayInfo;
		for(Enumeration edges = MainClass.displayManager.GetEdgeInfo(); edges.hasMoreElements();) 
		{
			edgeDisplayInfo = (DisplayManager.EdgeInfo)edges.nextElement();
			  //figure out the coordinates of the endpoints of the edge
			int x1 = ScaleNodeXCoordToScreenCoord(MainClass.locationAnalyzer.GetX(edgeDisplayInfo.GetSourceNodeNumber()));
			int y1 = ScaleNodeYCoordToScreenCoord(MainClass.locationAnalyzer.GetY(edgeDisplayInfo.GetSourceNodeNumber()));
			int x2 = ScaleNodeXCoordToScreenCoord(MainClass.locationAnalyzer.GetX(edgeDisplayInfo.GetDestinationNodeNumber()));
			int y2 = ScaleNodeYCoordToScreenCoord(MainClass.locationAnalyzer.GetY(edgeDisplayInfo.GetDestinationNodeNumber()));

	//		edgeDisplayInfo.paint(doubleBufferGraphic);
			MainClass.displayManager.PaintAllEdges(edgeDisplayInfo.GetSourceNodeNumber(), edgeDisplayInfo.GetDestinationNodeNumber(), x1, y1, x2, y2, doubleBufferGraphic);
		}

              //draw things over the entire display
		MainClass.displayManager.PaintOverScreen(doubleBufferGraphic);

		      //Make everything that was drawn visible
		g.drawImage(doubleBufferImage, 0, 0, null);

     }
              //END OF PAINT
              //----------------------------------------------------------------
     
     public void update(Graphics g)
     {
    	paint(g);
     }
     
     public void repaint(Graphics g)
     {
    	paint(g);
     }

              
              //*******************************************************
              //*******************************************************
              //the following code sets X and Y scaling factors for 
              //the X and Y coordinates of the nodes 
              //*******************************************************
              //the norm is to set scaling factors to keep everything
              //on the screen.  However, if the user attempts to zoom
              //the scaling factors can be larger and will be held 
              //constant
              //*******************************************************
              //*******************************************************

              
              
              //----------------------------------------------------------------
              //FIT TO SCREEN
	          //this function will set the scaling factors above such
	          //that all nodes are scaled to within the screen viewing area
	public void FitToScreen() //do not synchronize
	{
		double largestXCoord = Double.MIN_VALUE;
		double smallestXCoord = Double.MAX_VALUE;
		double largestYCoord = Double.MIN_VALUE;
		double smallestYCoord = Double.MAX_VALUE;
		double x, y;
              //find the largest and smallest coords for all nodes
		DisplayManager.NodeInfo currentDisplayInfo;
		for(Enumeration nodes = MainClass.displayManager.GetNodeInfo(); nodes.hasMoreElements();) 
		{
			currentDisplayInfo = (DisplayManager.NodeInfo)nodes.nextElement();
			if(screenIsEmpty){currentDisplayInfo.SetFitOnScreen(true);}
			if( ((currentDisplayInfo.GetDisplayThisNode() == true) && (currentDisplayInfo.GetFitOnScreen() == true) ))//If the current node is displayed and old enough, or if they are the first nodes to be drawn
			{
				x = MainClass.locationAnalyzer.GetX(currentDisplayInfo.GetNodeNumber());
				y = MainClass.locationAnalyzer.GetY(currentDisplayInfo.GetNodeNumber());
				if(x > largestXCoord) {largestXCoord = x;}
				if(x < smallestXCoord){smallestXCoord = x;}
				if(y > largestYCoord){largestYCoord = y;}
				if(y < smallestYCoord){smallestYCoord = y;}
			}
		}	
		      //here we use the following equations to set the scaling factors:
		      // xScale*SmallestXCoord + XIntercept = 0
		      // xScale*LargestXCoord  + XIntercept = window.width();
		      //
		      //And the same for the y scaling factors.  
		      //Note that I want a border of <Margin>% of the screen on both sides
		     
		Dimension d = getSize();
		
		xScale = (d.width-2*(d.width/xMargin))/(largestXCoord - smallestXCoord);
		yScale = (d.height-2*(d.height/yMargin))/(largestYCoord - smallestYCoord);

		xScale = Math.min(xScale, yScale);//this is to make sure that the x and y coordinates are not warped
		yScale = xScale;

		xScaleIntercept = -xScale*smallestXCoord + d.width/xMargin;
		yScaleIntercept = -yScale*smallestYCoord + d.height/yMargin;
		screenIsEmpty = false;
		if(MainClass.displayManager.proprietaryNodeInfo.isEmpty())//if there are no nodes and none of this function was executed
		{
			screenIsEmpty = true;
			xScale = 1;
			yScale = 1;
			xScaleIntercept = 0;
			yScaleIntercept = 0;
		}
		System.out.println(xScale + " " + yScale);
		System.out.println(xScaleIntercept + " " + yScaleIntercept);
	}
	
	public int ScaleNodeXCoordToScreenCoord(double pXCoord)//do not synchronize
	{         //take the local coordinate system of the nodes and show it as a graphical coordinate system
		Double xCoord = new Double(xScale*pXCoord+xScaleIntercept);
		return xCoord.intValue();
	}
	
	public int ScaleNodeYCoordToScreenCoord(double pYCoord)       //do not synchronize
	{         //take the local coordinate system of the nodes and show it as a graphical coordinate system
		Double yCoord = new Double(yScale*pYCoord+yScaleIntercept);
		return yCoord.intValue();
	}

	public Double ScaleScreenXCoordToNodeCoord(double pXCoord)//do not synchronize
	{         //take a graphical coordinate system and show it as the local coordinate system of the nodes 
		return new Double((pXCoord-xScaleIntercept)/xScale);
	}
	
	public Double ScaleScreenYCoordToNodeCoord(double pYCoord)       //do not synchronize
	{         //take a graphical coordinate system and show it as the local coordinate system of the nodes 
		return new Double((pYCoord-yScaleIntercept)/yScale);
	}
	
	          //------------------------------------------------------------------
	          //GET/SET
	public boolean GetFitToScreenAutomatically(){return fitToScreenAutomatically;}
	public void SetFitToScreenAutomatically(boolean p){fitToScreenAutomatically=p;}
	public double GetXScale(){return xScale;}
	public double GetYScale(){return yScale;}
	          //GET/SET
	          //------------------------------------------------------------------

	//{{DECLARE_CONTROLS
	//}}

}
