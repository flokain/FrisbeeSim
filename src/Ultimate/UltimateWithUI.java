/*
  Copyright 2006 by Daniel Kuebrich
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

package Ultimate;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.swing.JFrame;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.display3d.Display3D;
import sim.engine.SimState;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.portrayal3d.continuous.ContinuousPortrayal3D;
import sim.portrayal3d.grid.quad.TilePortrayal;
import sim.portrayal3d.simple.CubePortrayal3D;
import sim.portrayal3d.simple.Shape3DPortrayal3D;
import sim.util.gui.SimpleColorMap;
import visualization.FrisbeeFieldPortrayal3D;
import visualization.FrisbeePortrayal3D;
import visualization.UltimateEntityPortrayal2D;

public class UltimateWithUI extends GUIState
{
	public Display2D display2D;
	public JFrame display2DFrame;
	static Console con;
	ContinuousPortrayal2D entityPortrayal2D = new ContinuousPortrayal2D();
	
	public Display3D display3D;
	public JFrame display3DFrame;
	ContinuousPortrayal3D entityPortrayal3D = new ContinuousPortrayal3D();
    //ValueGrid2DPortrayal3D FieldPortrayal3D = new ValueGrid2DPortrayal3D("Field Projection");
    
	public static void main(String[] args)
	{
		UltimateWithUI vid = new UltimateWithUI();
		con = new Console(vid);
		con.setVisible(true);
	}

	public UltimateWithUI() { super(new Ultimate(System.currentTimeMillis())); }
	public UltimateWithUI(SimState state) { super(state); }
	public static String getName() { return "Ultimate"; }

	//overriding the GuiStates standard functions
	@Override
	public void start()
	{
		super.start();
		// set up our portrayals
		setupPortrayals();
		
		// reschedule the displayer just to be shure
		display2D.reset();
		display3D.reset();
		// redraw the display
		display2D.repaint();
        display3D.createSceneGraph();

	}
	@Override
	public void load(SimState state)
	{
		super.load(state);
		// we now have new grids.  Set up the portrayals to reflect that
		setupPortrayals();
	}
	@SuppressWarnings("unused")
	@Override
	public void init(Controller c)
	{
		super.init(c);
		
		// Make the Display2D
		double scale = 1;
		display2D = new Display2D(100*scale,37*scale,this); 
		display2D.setScale(10);
		display2DFrame = display2D.createFrame();
		display2DFrame.setVisible(true);
		display2DFrame.setTitle("Field");
		display2DFrame.setSize(1000,500);
		
		// specify the backdrop color  -- what gets painted behind the displays
		display2D.setBackdrop(new Color(50,140,50));  // a dark green
		// attach the portrayals
		display2D.attach(entityPortrayal2D,"Players and disc");
		
		// Make the Display3D
		display3D = new Display3D(900,500,this);
		
		// add the portrayal3ds of players disc and the playingfield to the display
        //display3D.attach(new WireFrameBoxPortrayal3D(-0.3, -0.3, -0.0, 0.3, 0.3, 10), "Bounds");
		FrisbeeFieldPortrayal3D field3D = new FrisbeeFieldPortrayal3D();
        display3D.attach(field3D,"disc"); // show the grass field to the display
        display3D.attach(entityPortrayal3D,"disc");		
        
        //set the correct view angle so to look at the disc
		Matrix3d rotation = new Matrix3d();
		rotation.setIdentity();
		rotation.rotX(Math.PI/2);
		Vector3d translation = new Vector3d(3.5,-10,0);
		Vector3d discPosi = new Vector3d(((Ultimate)state).positionDisc.x,((Ultimate)state).positionDisc.y,((Ultimate)state).positionDisc.z);
		translation.add(discPosi);
		Transform3D lookAt  = new Transform3D(rotation,translation,1);
		display3D.universe.getViewingPlatform().getViewPlatformTransform().setTransform(lookAt); // this sets the viewpoint!
		
		display3D.canvas.getView().setBackClipDistance(200);
		display3D.setShowsAxes(true);
		display3DFrame = display3D.createFrame();
		display3DFrame.setVisible(true);
		display3DFrame.setTitle("Disc");
		
		// trajectory drawing
		TilePortrayal trajectory = new TilePortrayal(new SimpleColorMap(0.0,100, Color.WHITE, Color.RED));
		
		//align all windows/frames
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
		
		c.registerFrame(display2DFrame);   // register the frame so it appears in the "display2D" list
		c.registerFrame(display3DFrame);   // register the frame so it appears in the list in the controller
	}
	
	@Override
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

	// Portrayal setups
	public void setupPortrayals()
	{
		setupFieldPortrayal2d();
		setupFrisbeePortrayal3d();	
	}
	public void setupFrisbeePortrayal3d()
	{
		entityPortrayal3D.setField(((Ultimate)state).ultimateField3D);
		entityPortrayal3D.setPortrayalForClass(Frisbee.class, new FrisbeePortrayal3D());
		//entityPortrayal3D.setPortrayalForClass(PlayerOffence.class, new UltimateEntityPortrayal3D());
		entityPortrayal3D.setPortrayalForClass(PlayerOffence.class, new CubePortrayal3D(Color.blue));
		entityPortrayal3D.setPortrayalForClass(PlayerDefence.class, new Shape3DPortrayal3D(new Shape3D(new sim.app.crowd3d.GullCG())));
       
	}
	@SuppressWarnings("serial")
	public void setupFieldPortrayal2d()
	{
		// tell the portrayals what to portray and how to portray them 
		entityPortrayal2D.setField(((Ultimate)state).ultimateField2D);
		entityPortrayal2D.setPortrayalForClass(FieldObject.class, new RectanglePortrayal2D(Color.white));
		entityPortrayal2D.setPortrayalForClass(PlayerOffence.class, new UltimateEntityPortrayal2D(Color.blue,Color.green));
		entityPortrayal2D.setPortrayalForClass(PlayerDefence.class, new UltimateEntityPortrayal2D(Color.red,Color.green));
		entityPortrayal2D.setPortrayalForClass(Frisbee.class, new UltimateEntityPortrayal2D(Color.white,Color.green));
	}
}




