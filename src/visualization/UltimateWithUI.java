/*
  Copyright 2006 by Daniel Kuebrich
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

package visualization;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.lowagie.text.Jpeg;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.Cylinder;

import disc.physics.aerodynamics.FlightModel;
import disc.physics.aerodynamics.FlightModel_Kain;
import disc.physics.aerodynamics.FlightModel_HummelNew;
import disc.physics.aerodynamics.FlightModel_HummelOriginal;
import disc.physics.aerodynamics.FlightModel_HummelNewCorrected;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.display3d.Display3D;
import sim.engine.SimState;
import sim.field.continuous.Continuous3D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.continuous.Continuous3DPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.portrayal3d.continuous.ContinuousPortrayal3D;
import sim.portrayal3d.grid.quad.TilePortrayal;
import sim.portrayal3d.simple.BranchGroupPortrayal3D;
import sim.portrayal3d.simple.ConePortrayal3D;
import sim.portrayal3d.simple.CubePortrayal3D;
import sim.portrayal3d.simple.CylinderPortrayal3D;
import sim.portrayal3d.simple.Shape3DPortrayal3D;
import sim.util.Bag;
import sim.util.Double3D;
import sim.util.gui.SimpleColorMap;
import ultimate.FixedEntity;
import ultimate.Ultimate;
import ultimate.Ultimate.ExperimentSetup;
import ultimate.fixedEntity.Line;
import ultimate.steppableEntity.Frisbee;
import ultimate.steppableEntity.Ball;
import ultimate.steppableEntity.Player;
import ultimate.steppableEntity.PlayerDefence;
import ultimate.steppableEntity.PlayerOffence;

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
		setupCommandPanel();
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
		initialize(ExperimentSetup.VerticalStack);
	}
	public void start(ExperimentSetup setup)
	{
		super.start();
		initialize(setup);
	}
	public void initialize(ExperimentSetup setup)
	{
		((Ultimate)state).initialize(setup);
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
        //display3D.attach(field3D,"disc"); // show the grass field to the display
        display3D.attach(entityPortrayal3D,"disc");		
        
        //set the correct view angle so to look at the disc
		Matrix3d rotation = new Matrix3d();
		rotation.setIdentity();
		rotation.rotX(Math.PI/2);
		Vector3d translation = new Vector3d(3.5,-10,0);
		Vector3d discPosi = new Vector3d(((Ultimate)state).frisbee.getPositionX(),((Ultimate)state).frisbee.getPositionY(),((Ultimate)state).frisbee.getPositionZ());
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
		Box surface = 	new Box((float) field.getWidth()/2,(float)field.getHeight()/2, 0.001f, new Appearance()
							{	
								{
									setColoringAttributes(new ColoringAttributes(new Color3f(new Color(50,140,50)) ,ColoringAttributes.NICEST));
									setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.2f));
								}
							});
		field.setObjectLocation(surface, new Double3D(surface.getXdimension(),surface.getYdimension(),0));
		
		Bag objects = field.allObjects;
		entityPortrayal3D.setField(field);
		
		
		for (int i = 0; i < objects.size(); i++)
		{
			if(objects.objs[i] instanceof Box)
			{
				final Box b = (Box) objects.objs[i];
				entityPortrayal3D.setPortrayalForObject(objects.objs[i],  new BranchGroupPortrayal3D(new BranchGroup(){{addChild(b);}}));
			}
			if(objects.objs[i] instanceof Player)
			{
				Color col;
				if(objects.objs[i] instanceof PlayerOffence) col = Color.BLUE;
				else col = Color.RED;
				
				Transform3D trans = new Transform3D();
				trans.rotZ(-Math.PI/2);
				final TransformGroup t = new TransformGroup(trans);
					
				final Appearance app = new Appearance();
				app.setColoringAttributes(new ColoringAttributes(new Color3f(col) ,ColoringAttributes.NICEST));
				
				t.addChild(new Cone(0.3f, (float) 1,app));
				
				entityPortrayal3D.setPortrayalForObject(objects.objs[i],  new UltimateEntityPortrayal3D(new BranchGroup() {{addChild(t);}}));
			}
			/*if(objects.objs[i] instanceof PlayerOffence)
			{
				//entityPortrayal3D.setPortrayalForObject(objects.objs[i],  new UltimateEntityPortrayal3D(new BranchGroup() {{addChild(new Shape3D(new sim.app.crowd3d.GullCG()));}}));
			}
			else if(objects.objs[i] instanceof PlayerDefence)
			{
				entityPortrayal3D.setPortrayalForObject(objects.objs[i], new UltimateEntityPortrayal3D());
			}	*/		
			else if(objects.objs[i] instanceof Frisbee)
			{
				entityPortrayal3D.setPortrayalForObject(objects.objs[i], new FrisbeePortrayal3D());
			}
			else if(objects.objs[i] instanceof FixedEntity)
			{
				Double3D dir = ((FixedEntity)objects.objs[i]).getTop().subtract(((FixedEntity)objects.objs[i]).getPosition());
				Transform3D trans = new Transform3D();
				Vector3d y = new Vector3d(0,1,0);
				Vector3d direction = new Vector3d(dir.x,dir.y,dir.z);
				direction.normalize();
				double ang = y.angle(direction);
				Vector3d axis = new Vector3d();
				if (!y.equals(direction))
				{
					axis.cross(y, direction);
					axis.normalize();
					trans.set(new AxisAngle4d(axis, ang));
				}

				final TransformGroup t = new TransformGroup(trans);
				final Node c;
				Appearance app = new Appearance();
				
				if(objects.objs[i] instanceof ultimate.fixedEntity.Cone)
				{
					app.setColoringAttributes(new ColoringAttributes(new Color3f(Color.ORANGE) ,ColoringAttributes.NICEST));
					c= new Cone(0.1f, (float) dir.length(),app);
				}
				else if(objects.objs[i] instanceof ultimate.fixedEntity.Wall)
				{
					ultimate.fixedEntity.Wall wall = (ultimate.fixedEntity.Wall) objects.objs[i];
					app.setColoringAttributes(new ColoringAttributes(new Color3f(Color.YELLOW) ,ColoringAttributes.NICEST));
					c= new Box((float) wall.depth,(float) wall.width, (float) dir.length(),app);
				}
				else
				{
					c= new Cylinder(0.05f, (float) dir.length(),app);
				}
				
				t.addChild(c);
				entityPortrayal3D.setPortrayalForObject(objects.objs[i], new BranchGroupPortrayal3D(new BranchGroup() 
				{{			
					addChild(t);
				}}
				));
			}
			
		}
	}
	@SuppressWarnings("serial")
	public void setupFieldPortrayal2d()
	{
		// tell the portrayals what to portray and how to portray them 
		entityPortrayal2D.setField(((Ultimate)state).ultimateField3D);
		entityPortrayal2D.setField(((Ultimate)state).ultimateField3D);
		entityPortrayal2D.setPortrayalForClass(FixedEntity.class, new RectanglePortrayal2D(Color.white,0)
		{
				public void draw(Object object,  final Graphics2D g, final DrawInfo2D info )
			    {    
			    // draw our line as well
			    
			    final double width = info.draw.width * (((FixedEntity)object).getPosition().y-((FixedEntity)object).getTop().y) * 2;
			    final double height = info.draw.height * (((FixedEntity)object).getPosition().x-((FixedEntity)object).getTop().x)* 2;
			     
			    Double3D  p = ((FixedEntity)object).getPosition();
			    Double3D  t = ((FixedEntity)object).getTop();
			    g.setColor(Color.white);
			    g.drawLine( (int)(p.x* info.draw.height),(int)(p.y*info.draw.width),(int) (t.x*info.draw.height),(int) (t.y*info.draw.width));
			    g.drawLine((int)info.draw.x,
			        (int)info.draw.y,
			        (int)(info.draw.x) + (int)(width),
			        (int)(info.draw.y) + (int)(height));
			}
		});
		entityPortrayal2D.setPortrayalForClass(PlayerOffence.class, new UltimateEntityPortrayal2D(Color.blue,Color.green));
		entityPortrayal2D.setPortrayalForClass(PlayerDefence.class, new UltimateEntityPortrayal2D(Color.red,Color.green));
		entityPortrayal2D.setPortrayalForClass(Frisbee.class, new UltimateEntityPortrayal2D(Color.white,Color.green));
	}
	
	public static void setupCommandPanel()
	{
		
		// add new pnl as tab to console
		JPanel commandPnl = new JPanel();
		commandPnl.setLayout(new GridBagLayout());
		con.getTabPane().addTab("Commands",commandPnl);
	    GridBagConstraints c = new GridBagConstraints();		
	    c.weightx = 0.5;
	    c.weighty = 0.5;
	    c.anchor = GridBagConstraints.NORTH;
	    c.fill = GridBagConstraints.HORIZONTAL;

	    //setup a Pnl for interaction.
	    JPanel pnl = new JPanel();
	    pnl.setLayout(new GridBagLayout());
	    pnl.setBorder(BorderFactory.createTitledBorder("Flightmodels"));

	    c.gridx = 0;
	    c.gridy = 0;
	    commandPnl.add(pnl,c);	    
	    
		JButton button = new JButton("Hummel");
	    c.gridx = 0;
	    c.gridy = 0;
	    c.insets = new Insets(1,2,1,2);
	    pnl.add(button, c);
	    button.addActionListener( new ActionListener() 
	    {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Ultimate ultimate = (Ultimate)con.getSimulation().state;			
				try {
					ultimate.frisbee.setFlightModel(new FlightModel_HummelNew());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	    
	    button = new JButton("Hummel_Corrected");	
	    c.gridx = 0;
	    c.gridy = 1;
		pnl.add(button,c);
	    button.addActionListener( new ActionListener() 
	    {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Ultimate ultimate = (Ultimate)con.getSimulation().state;
				try {
					ultimate.frisbee.setFlightModel(new FlightModel_HummelNewCorrected());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	    
	    button = new JButton("Kain");	
	    c.gridx = 0;
	    c.gridy = 2;
		pnl.add(button,c);
	    button.addActionListener( new ActionListener() 
	    {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Ultimate ultimate = (Ultimate)con.getSimulation().state;
				try {
					ultimate.frisbee.setFlightModel(new FlightModel_Kain());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	    
	    button = new JButton("Ball");	
	    c.gridx = 0;
	    c.gridy = 3;
		pnl.add(button,c);
	    button.addActionListener( new ActionListener() 
	    {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Ultimate ultimate = (Ultimate)con.getSimulation().state;
				try {
					ultimate.frisbee = new Ball();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	    
	    //setup pnl for throw Setups.
	    pnl = new JPanel();
	    pnl.setLayout(new GridBagLayout());
	    pnl.setBorder(BorderFactory.createTitledBorder("Throws"));

	    c.weightx = 0.5;
	    c.weighty = 0.5;
	    c.anchor = GridBagConstraints.NORTH;
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridx = 0;
	    c.gridy = 1;
	    commandPnl.add(pnl,c);
	    
	    button = new JButton("Hummel-flight f2302");
	    c.gridx = 0;
	    c.gridy = 0;
	    c.insets = new Insets(1,2,1,2);
	    pnl.add(button, c);
	    
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
	    
	    button = new JButton("Hummel-flight mirrored f2302");
	    c.gridx = 0;
	    c.gridy = 1;
	    c.insets = new Insets(1,2,1,2);
	    pnl.add(button, c);
	    
	    button.addActionListener( new ActionListener() 
	    {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Ultimate ultimate = (Ultimate)con.getSimulation().state;			
				
				Double3D orientation = new Double3D(7.11E-02, -2.11E-01, -5.03E+00);//rad
				Double3D velocity = new Double3D(1.34E+01,  -4.11E-01, 1.12E-03);
				Double3D omega = new Double3D(1.49E+01, 1.48E+00, -5.43E+01);		
				
				ultimate.frisbee.throwDisc(velocity, orientation, omega);
			}
		});
	    
	    button = new JButton("v = (10,0,10)");	
	    c.gridx = 0;
	    c.gridy = 2;
		pnl.add(button,c);
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


	    //setup pnl for simulation Setups.
	    pnl = new JPanel();
	    pnl.setLayout(new GridBagLayout());
	    c.gridx = 1;
	    c.gridy = 0;
	    commandPnl.add(pnl,c);
	    pnl.setBorder(BorderFactory.createTitledBorder("Setups"));
	    commandPnl.add(pnl,c);
	    
	    button = new JButton("disc only!");
	    c.gridx = 0;
	    c.gridy = 0;
	    c.insets = new Insets(1,2,1,2);
	    pnl.add(button,c);
//	    
	    button.addActionListener( new ActionListener() 
	    {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				UltimateWithUI ultimateWUI = (UltimateWithUI)con.getSimulation();
				ultimateWUI.start(ExperimentSetup.Hummel);
			}
		});
	    button = new JButton("Vertical Stack!");
	    c.gridx = 0;
	    c.gridy = 1;
	    c.insets = new Insets(1,2,1,2);
	    pnl.add(button,c);
//	    
	    button.addActionListener( new ActionListener() 
	    {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				UltimateWithUI ultimateWUI = (UltimateWithUI)con.getSimulation();
				ultimateWUI.start(ExperimentSetup.VerticalStack);
			}
		});
	    button = new JButton("Read Experiment");
	    c.gridx = 0;
	    c.gridy = 2;
	    c.insets = new Insets(1,2,1,2);
	    pnl.add(button,c);
//	    
	    button.addActionListener( new ActionListener() 
	    {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				UltimateWithUI ultimateWUI = (UltimateWithUI)con.getSimulation();
				ultimateWUI.start(ExperimentSetup.ReadExperiment);
			}
		});
	}
}




