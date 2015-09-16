package Ultimate;

import java.awt.Color;
import javax.vecmath.Vector3d;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

public abstract class Player extends UltimateEntity implements Steppable {

	double energy; 	// energy left in the object that makes it possible to accelarte itself
	double legPower; 	// maximum amount of energy applied per second to run. (max accelaration)
	double armPower;	// maximum amount of energy applied per second to throw. (max accelaration)
	public MutableDouble2D tempVector = new MutableDouble2D();
	
	public Player( Double2D posi, Color c)
	{
		super(posi,0.5,c);
	}

	public Vector3d getForces( final Ultimate ultimate)
	{	
		double x= ultimate.random.nextDouble()-0.5;
		double y= ultimate.random.nextDouble()-0.5;
		double z= ultimate.random.nextDouble()-0.5;
		Vector3d force = new Vector3d(x,y,z);
		return force;
	}
	
	public void step( final SimState state )
	{
		Ultimate ultimate = (Ultimate)state;
		// get force
		force = getForces(ultimate);
		super.step(state);
//		ultimate.ultimateField.setObjectLocation(this, new Double2D(loc));
	}

}