package Ultimate;

import java.awt.Color;
import java.lang.annotation.Inherited;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import sim.util.Double3D;
import sim.util.MutableDouble3D;

public abstract class UltimateEntity implements Steppable{

	protected double mass; 
	protected double radius; // all elements are considerd to be balls or tubes for visualization
	
	protected MutableDouble3D position;
	protected MutableDouble3D orientation; 
	protected MutableDouble3D omega;			// angular velocity
	protected MutableDouble3D velocity;
	protected MutableDouble3D acceleration;
	protected MutableDouble3D alpha;
	
	// Getter and Setter, important for inspector in Mason
	public double getMass() {
		return mass;
	}
	public double getRadius() {
		return radius;
	}
	
	public MutableDouble3D getPosition() {
		return position;
	}
	public MutableDouble3D getOrientation() {
		return orientation;
	}
	public MutableDouble3D getOmega() {
		return omega;
	}
	public MutableDouble3D getVelocity() {
		return velocity;
	}
	public MutableDouble3D getAcceleration() {
		return acceleration;
	}
	public MutableDouble3D getAlpha() {
		return alpha;
	}
	// Constructors
	public UltimateEntity( Double3D posi, Double3D orientation, Double3D velocity,Double3D omega, double mass, double radius)
	{
		position 		 = 	new MutableDouble3D(posi); 
		this.orientation = 	new MutableDouble3D(orientation);
		this.velocity 	 = 	new MutableDouble3D(velocity);
		this.omega 		 =  new MutableDouble3D(omega);
		this.radius 	 =	radius;
		this.mass 		 =	mass;
		this.acceleration= new MutableDouble3D();
		this.alpha		 = new MutableDouble3D();
	}
	public UltimateEntity( Double3D posi, Double3D velocity, double mass, double radius)
	{
		this(posi, new Double3D(0,0,0), velocity, new Double3D(0,0,0), mass, radius);
	}
	public UltimateEntity( Double2D posi, Double2D velocity, double mass, double radius)
	{
		this(posi,new Double2D(0,0), velocity, mass, radius);
	}
	public UltimateEntity( Double2D posi, Double2D orientation, Double2D velocity, double mass, double radius)
	{
		this( new Double3D(posi.x,posi.y,0), new Double3D(orientation.x,orientation.y,0), new Double3D(velocity.x,velocity.y,0), new Double3D(0,0,0), mass, radius);
	}
	public UltimateEntity( Double2D posi, double mass, double radius){
		this(  posi,new Double2D(0,0),new Double2D(0,0), mass, radius);	
	}
	public UltimateEntity( Double2D posi, double radius)
	{
		this( posi,new Double2D(0,0),new Double2D(0,0),1.0, radius);
	}
	public UltimateEntity( Double2D posi) 				
	{ 
		this(posi, 1.0); 
		}

	
	// override these functions!
	
	public UltimateEntity(Double3D position) {
		// TODO Auto-generated constructor stub
	}
	public AccelerationsContainer calcAccelerations(Ultimate ultimate) // calculation of acceleration
	{
		return null; 
	}
	@Override
	public void step( final SimState state )
	{
		Ultimate ultimate = (Ultimate)state;
		
		double h = ultimate.stepTime;
		
		AccelerationsContainer container = calcAccelerations(ultimate);
		acceleration.setTo(container.acceleration);
		alpha.setTo(container.alpha);
				
		position.addIn(new Double3D(velocity).multiply(h));
		velocity.addIn(new Double3D(container.acceleration).multiply(h));
		
		orientation.addIn(new Double3D(omega).multiply(h));
		omega.addIn(new Double3D(container.alpha).multiply(h));
		
		ultimate.ultimateField2D.setObjectLocation(this,new Double2D(position.x, position.y)); 				//set the location in 2d portrayal
		ultimate.ultimateField3D.setObjectLocation(this,new Double3D(position.x,position.y,position.z)); 	//set the location in 3d portrayal
	}
	
	protected class AccelerationsContainer
	{
		Double3D acceleration;
		Double3D alpha;
		
		public AccelerationsContainer(Double3D acceleration, Double3D alpha) 
		{
			this.acceleration = acceleration;
			this.alpha = alpha;
		}
	}
}