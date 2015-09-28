/*
  Copyright 2006 by Daniel Kuebrich
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */
//test

package Ultimate;
import sim.engine.*;
import sim.field.grid.DoubleGrid2D;
import sim.display.*;
import sim.display3d.Display3D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.LocationWrapper;
import sim.portrayal.continuous.*;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal3d.continuous.*;
import sim.portrayal3d.grid.ValueGrid2DPortrayal3D;
import sim.portrayal3d.grid.quad.TilePortrayal;
import sim.portrayal3d.simple.CylinderPortrayal3D;
import sim.portrayal3d.simple.WireFrameBoxPortrayal3D;
import sim.util.gui.SimpleColorMap;

import java.awt.*;

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.sun.j3d.loaders.*;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.geometry.Cylinder;

public class UltimateWithUI extends GUIState
{
	public Display2D display2D;
	public JFrame display2DFrame;
	static Console con;
	ContinuousPortrayal2D entityPortrayal2D = new ContinuousPortrayal2D();
	
	
	public Display3D display3D;
	public JFrame display3DFrame;
	ContinuousPortrayal3D entityPortrayal3D = new ContinuousPortrayal3D();
    ValueGrid2DPortrayal3D FieldPortrayal3D = new ValueGrid2DPortrayal3D("Field Projection");
    
    
	public static void main(String[] args)
	{
		UltimateWithUI vid = new UltimateWithUI();
		con = new Console(vid);
		con.setVisible(true);
	}

	public UltimateWithUI() { super(new Ultimate(System.currentTimeMillis())); }
	public UltimateWithUI(SimState state) { super(state); }
	public static String getName() { return "Ultimate"; }

	public void start()
	{
		super.start();
		// set up our portrayals
		setupPortrayals();
	}

	public void load(SimState state)
	{
		super.load(state);
		// we now have new grids.  Set up the portrayals to reflect that
		setupPortrayals();
	}


	public void setupPortrayals()
	{
		setupFieldPortrayal2d();
		setupFrisbeePortrayal3d();
		
	}
	public void setupFrisbeePortrayal3d()
	{
		entityPortrayal3D.setField(((Ultimate)state).space);
		entityPortrayal3D.setPortrayalForClass(Frisbee.class, new FrisbeePortrayal3D());
        display3D.reset();
        display3D.createSceneGraph();
       
	}
	public void setupFieldPortrayal2d()
	{
		// tell the portrayals what to portray and how to portray them
		entityPortrayal2D.setField(((Ultimate)state).ultimateField);
		entityPortrayal2D.setPortrayalForClass(FieldObject.class, new sim.portrayal.simple.RectanglePortrayal2D(Color.white));
		entityPortrayal2D.setPortrayalForClass(PlayerOffence.class, new sim.portrayal.simple.MovablePortrayal2D(
				 												  new sim.portrayal.simple.CircledPortrayal2D(
				 												  new sim.portrayal.simple.OvalPortrayal2D() 
				 												  {
				 													  public void draw(Object object, Graphics2D graphics,DrawInfo2D info) 
				 													  {
				 														  PlayerOffence player = (PlayerOffence)object;
				 														  scale = player.getRadius();
				 														  paint = Color.blue;
				 														  super.draw(object, graphics, info);
				 													  }
				 												  }
				 												  ,0,5.0,Color.green, true)));
		
		entityPortrayal2D.setPortrayalForClass(PlayerDefence.class, new sim.portrayal.simple.MovablePortrayal2D(
						  										  new sim.portrayal.simple.CircledPortrayal2D(
						  										  new sim.portrayal.simple.OvalPortrayal2D()
						  										  {
						  											  public void draw(Object object, Graphics2D graphics,DrawInfo2D info) 
						  											  {
						  												  PlayerDefence player = (PlayerDefence)object;
						  												  scale = player.getRadius();
						  												  paint = Color.red;
						  												  super.draw(object, graphics, info);
						  											  }
						  										  } 
						  										  ,0,5.0,Color.green, true)));
		
		entityPortrayal2D.setPortrayalForClass(Frisbee.class, new sim.portrayal.simple.MovablePortrayal2D(
														    new sim.portrayal.simple.CircledPortrayal2D(
														    new sim.portrayal.simple.OvalPortrayal2D() 
												   		    {
													  		    public void draw(Object object, Graphics2D graphics,DrawInfo2D info) 
															    {
																    Frisbee frisbee = (Frisbee)object;
																    scale = frisbee.getRadius();
																    paint = Color.white;
																   super.draw(object, graphics, info);
															    }
												   		    }
														    ,0,5.0,Color.green, true)));
		

		// reschedule the displayer
		display2D.reset();

		// redraw the display
		display2D.repaint();
	}

	public void init(Controller c)
	{
		super.init(c);
		// Make the Display2D.  We'll have it display stuff later.
		double scale = 1;
		display2D = new Display2D(100*scale,37*scale,this); 
		display2DFrame = display2D.createFrame();
		c.registerFrame(display2DFrame);   // register the frame so it appears in the "display2D" list
		display2DFrame.setVisible(true);
		display2DFrame.setTitle("Field");
		display2DFrame.setSize(1000,500);
		display2D.setScale(10);
		
		// attach the portrayals
		// specify the backdrop color  -- what gets painted behind the displays
		display2D.setBackdrop(new Color(50,140,50));  // a dark green
		display2D.attach(entityPortrayal2D,"Players and disc");
		
		// Make the Display3D
		display3D = new Display3D(900,500,this);
        //display3D.attach(new WireFrameBoxPortrayal3D(-0.3, -0.3, -0.0, 0.3, 0.3, 10), "Bounds");
		FrisbeeFieldPortrayal3D field3D= new FrisbeeFieldPortrayal3D();
		
		// trajectory drawing
		TilePortrayal trajectory = new TilePortrayal(new SimpleColorMap(0.0,100, Color.WHITE, Color.RED));
		// trajectory drawing
		display3D.attach(new FrisbeeFieldPortrayal3D(), "Field in 3D");
        display3D.attach(field3D,"disc");
        display3D.attach(entityPortrayal3D,"disc");
        
        //set the correct view angle
		Matrix3d rotation = new Matrix3d();
		rotation.setIdentity();
		rotation.rotX(Math.PI/2);
		Vector3d translation = new Vector3d(0,-10,0);//new Vector3d(1,-5,1);
		translation.add(((Ultimate)state).frisbee.location);
		Transform3D lookAt  = new Transform3D(rotation,translation,3);
		display3D.universe.getViewingPlatform().getViewPlatformTransform().setTransform(lookAt); // this sets the viewpoint!
		
		display3D.canvas.getView().setBackClipDistance(200);
		display3D.setShowsAxes(true);
		display3DFrame = display3D.createFrame();
		c.registerFrame(display3DFrame);   // register the frame so it appears in the "display2D" list
		display3DFrame.setVisible(true);
		display3DFrame.setTitle("Disc");
		
		//allign all windows/frames;
		Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
		Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		int taskBarHeight = screen.height - winSize.height;
		
		
		Rectangle bounds2D = new Rectangle();
		Rectangle bounds3D = new Rectangle();
		Rectangle boundsCon = new Rectangle(screen);
		
		int widthCon = 500;
		boundsCon.x = boundsCon.width -widthCon;
		boundsCon.width = widthCon;
		
		double heightRelative2D = 1./2;
		bounds2D.setBounds(0, 0, boundsCon.x, (int)(screen.height*heightRelative2D));
		bounds3D.setBounds(0, (int)(screen.height*heightRelative2D),boundsCon.x , (int)(screen.height*(1-heightRelative2D)-taskBarHeight));
		
		display2DFrame.setBounds(bounds2D);
		display3DFrame.setBounds(bounds3D);
		
		// attach the portrayals
		display2D.setBackdrop(new Color(50,140,50));  // dark green
	}

	public void quit()
	{
		super.quit();

		if (display2DFrame!=null) display2DFrame.dispose();
		display2DFrame = null;  // let gc
		display2D = null;       // let gc
		
		if (display3DFrame!=null) display3DFrame.dispose();
		display3DFrame = null;  // let gc
		display3D = null;       // let gc
	}
}




