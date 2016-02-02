package Ultimate;

import java.awt.Color;

import javax.vecmath.Vector3d;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import sim.util.Double3D;
import sim.util.MutableDouble2D;

public abstract class Player extends UltimateEntity implements Steppable {

	double energy; 	// energy left in the object that makes it possible to accelarte itself
	double legPower; 	// maximum amount of energy applied per second to run. (max accelaration)
	double armPower;	// maximum amount of energy applied per second to throw. (max accelaration)
	public MutableDouble2D tempVector = new MutableDouble2D();
	
	public Player( Double2D posi)
	{
		super(posi,0.5);
	}
	
	@Override
	public AccelerationsContainer calcAccelerations(Ultimate ultimate)
	{	
		double x= ultimate.random.nextDouble()-0.5;
		double y= ultimate.random.nextDouble()-0.5;
		double z= ultimate.random.nextDouble()-0.5;
		Double3D acceleration = new Double3D(x,y,z);

		double theta = ultimate.random.nextDouble()-0.5;
		double phi   = ultimate.random.nextDouble()-0.5;
		double gamma = ultimate.random.nextDouble()-0.5;
		Double3D alpha = new Double3D(theta,phi,gamma);
		
		return new AccelerationsContainer(acceleration,alpha);
	}
}