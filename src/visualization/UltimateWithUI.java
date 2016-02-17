/*
  Copyright 2006 by Daniel Kuebrich
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

package visualization;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import Ultimate.FieldObject;
import Ultimate.Frisbee;
import Ultimate.PlayerDefence;
import Ultimate.PlayerOffence;
import Ultimate.Ultimate;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.display3d.Display3D;
import sim.engine.SimState;
import sim.field.continuous.Continuous3D;
import sim.portrayal.Inspector;
import sim.portrayal.continuous.Continuous3DPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.portrayal3d.continuous.ContinuousPortrayal3D;
import sim.portrayal3d.grid.quad.TilePortrayal;
import sim.portrayal3d.simple.BranchGroupPortrayal3D;
import sim.portrayal3d.simple.CubePortrayal3D;
import sim.portrayal3d.simple.Shape3DPortrayal3D;
import sim.util.Bag;
import sim.util.Double3D;
import sim.util.gui.SimpleColorMap;

public class UltimateWithUI extends GUIState
{
	public Display2D display2D;
	public JFrame display2DFrame;
	static Console con;
	Continuous3DPortrayal2D entityPortrayal2D = new Continuous3DPortrayal2D();
	
	public Display3D display3D;
	public JFrame display3DFrame;
	ContinuousPortrayal3D entityPortrayal3D = new ContinuousPortrayal3D();
    //ValueGrid2DPortrayal3D FieldPortrayal3D = new ValueGrid2DPortrayal3D("Field Projection");
    
	public static void main(String[] args) throws IOException
	{
		UltimateWithUI vid = new UltimateWithUI();
		con = new Console(vid);
		//vid.scheduleRepeatingImmediatelyBefore(new sim.display.RateAdjuster(60.0));
		con.setVisible(true);
		
//	    // Buttons
		JPanel pnl = new JPanel();
	    JButton button = new JButton("Fly!");
//	    
	    pnl.add(button);
//	    
	    button.addActionListener( new ActionListener() 
	    {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Ultimate ultimate = (Ultimate)con.getSimulation().state;			
				
				Double3D orientation = new Double3D(-7.11E-02, 2.11E-01, 5.03E+00);//rad
				Double3D velocity = new Double3D(1.34E+01,  -4.11E-01, 1.12E-03);
				Double3D omega = new Double3D(-1.49E+01, -1.48E+00, 5.43E+01);		
				
				ultimate.frisbee.throwDisc(velocity, orientation, omega);
			}
		});
	    button = new JButton("Fly Ball_x!");
//	    
	    pnl.add(button);
//	    
	    button.addActionListener( new ActionListener() 
	    {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Ultimate ultimate = (Ultimate)con.getSimulation().state;			
				
				Double3D orientation = new Double3D(0,0,0);//rad
				Double3D velocity = new Double3D(10,0,10);
				Double3D omega = new Double3D(0,0,0);		
				
				ultimate.frisbee.throwDisc(velocity, orientation, omega);
			}
		});
//	    
	    con.getTabPane().addTab("Commands",pnl);
	}

	public UltimateWithUI() throws IOException { super(new Ultimate(System.currentTimeMillis())); }
	public UltimateWithUI(SimState state) { super(state); }
	
	//adds a model tab in the simulation for inspectors int the simState subclass Ultimate
	public Object getSimulationInspectedObject() { return state; }
	public Inspector getInspector()
	{
	Inspector i = super.getInspector();
	i.setVolatile(true);
	return i;
	}
	
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
		display2D.setBackdrop(new Color(50,140,50)); // a dark green
		// attach the portrayals
		display2D.attach(entityPortrayal2D,"Players and disc");
		
		// Make the Display3D
		display3D = new Display3D(900,500,this)
		{
			{
				updateRule = Display2D.UPDATE_RULE_WALLCLOCK_TIME;
				wallInterval = 40; // 40 ms = 25frames/s  20ms = 50 frames/s
			}
		};
		display3D.setBackdrop((new ImageIcon("images/horizon.jpg")).getImage(), true);
		
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
		Continuous3D field = ((Ultimate)state).ultimateField3D;
		Bag objects = field.allObjects;
		entityPortrayal3D.setField(field);
		for (int i = 0; i < objects.size(); i++)
		{
			if(objects.objs[i] instanceof PlayerOffence)
			{
				entityPortrayal3D.setPortrayalForObject(objects.objs[i],  new UltimateEntityPortrayal3D(new BranchGroup() {{addChild(new Shape3D(new sim.app.crowd3d.GullCG()));}}));
			}
			else if(objects.objs[i] instanceof PlayerDefence)
			{
				entityPortrayal3D.setPortrayalForObject(objects.objs[i], new UltimateEntityPortrayal3D());
			}			
			else if(objects.objs[i] instanceof Frisbee)
			{
				entityPortrayal3D.setPortrayalForObject(objects.objs[i], new FrisbeePortrayal3D());
			}
			
			
		}
//		entityPortrayal3D.setField(field);
//		entityPortrayal3D.setPortrayalForClass(Frisbee.class, new FrisbeePortrayal3D());
//		entityPortrayal3D.setPortrayalForClass(PlayerOffence.class, new UltimateEntityPortrayal3D());
//		try {
//			entityPortrayal3D.setPortrayalForClass(PlayerOffence.class, new BranchGroupPortrayal3D(BranchGroupPortrayal3D.getBranchGroupForFile("3dmodels\\ea.obj")));
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//entityPortrayal3D.setPortrayalForClass(PlayerDefence.class, new Shape3DPortrayal3D(new Shape3D(new sim.app.crowd3d.GullCG()),Color.red));
		//entityPortrayal3D.setPortrayalForClass(PlayerOffence.class, new CubePortrayal3D(Color.blue));
	}
	@SuppressWarnings("serial")
	public void setupFieldPortrayal2d()
	{
		// tell the portrayals what to portray and how to portray them 
		entityPortrayal2D.setField(((Ultimate)state).ultimateField3D);
		entityPortrayal2D.setPortrayalForClass(FieldObject.class, new RectanglePortrayal2D(Color.white));
		entityPortrayal2D.setPortrayalForClass(PlayerOffence.class, new UltimateEntityPortrayal2D(Color.blue,Color.green));
		entityPortrayal2D.setPortrayalForClass(PlayerDefence.class, new UltimateEntityPortrayal2D(Color.red,Color.green));
		entityPortrayal2D.setPortrayalForClass(Frisbee.class, new UltimateEntityPortrayal2D(Color.white,Color.green));
	}
}




