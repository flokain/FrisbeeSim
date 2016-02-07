package Ultimate;

import java.awt.Color;
import java.lang.annotation.Inherited;

import javafx.geometry.Orientation;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.inspector.TabbableAndGroupable;
import sim.util.Double2D;
import sim.util.Double3D;
import sim.util.MutableDouble3D;

public abstract class UltimateEntity implements Steppable, TabbableAndGroupable {

	protected double mass; 
	protected double radius; // all elements are considerd to be balls or tubes for visualization
	
	protected MutableDouble3D position;
	protected MutableDouble3D orientation; 
	protected MutableDouble3D omega;			// angular velocity
	protected MutableDouble3D velocity;
	protected MutableDouble3D acceleration;
	protected MutableDouble3D alpha;

	// Constructors
	public UltimateEntity( Double3D posi, Double3D orientation, Double3D velocity,Double3D omega, double mass, double radius)
	{
		position 		 = 	new MutableDouble3D(posi); 
		this.orientation = 	new MutableDouble3D(orientation);
		this.velocity 	 = 	new MutableDouble3D(velocity);
		this.omega 		 =  new MutableDouble3D(omega);
		this.radius 	 =	radius;
		this.mass 		 =	mass;
		this.acceleration= new MutableDouble3D(0,0,0);
		this.alpha		 = new MutableDouble3D(0,0,0);
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
	//overrides tabbedandGrouped Interface
	@Override
	public String[] provideTabNames() 
	{
		return new String[]{"Translation", "Rotation"};
	}
    public String[][] provideTabGroups()
    { return new String[][] {{"Position","Velocity","Acceleration"},
                             {"Orientation","Omega","Alpha"}};
    }
	@Override
	public String[][][] provideTabGroupProperties() {
		return new String[][][]
		{{{ "PositionX","PositionY","PositionZ"},
		  {"VelocityX","VelocityY","VelocityZ"},
		  {"AccelerationX","AccelerationY","AccelerationZ"}
		 },	
		 {{"OrientationX","OrientationY","OrientationZ"},
		  {"OmegaX","OmegaY","OmegaZ"},
		  {"AlphaX","AlphaY","AlphaZ"}
		 }
		};
	}

                    
public String provideExtraTab()
    { return "Misc"; }

// seter and Setter, important for inspector in Mason
public double getMass() {
	return mass;
}
public double getRadius() {
	return radius;
}
public double getPositionX() {
	return position.x;
}
public double getPositionY() {
	return position.y;
}
public double getPositionZ() {
	return position.z;
}
public double getOrientationX() {
	return orientation.x;
}	
public double getOrientationY() {
	return orientation.y;
}
public double getOrientationZ() {
	return orientation.z;
}
public double getVelocityX() {
	return velocity.x;
}	
public double getVelocityY() {
	return velocity.y;
}
public double getVelocityZ() {
	return velocity.z;
}
public double getOmegaX() {
	return omega.x;
}
public double getOmegaY() {
	return omega.y;
}
public double getOmegaZ() {
	return omega.z;
}
public double getAccelerationX() {
	return acceleration.x;
}
public double getAccelerationY() {
	return acceleration.y;
}
public double getAccelerationZ() {
	return acceleration.z;
}
public double getAlphaX() {
	return alpha.x;
}
public double getAlphaY() {
	return alpha.y;
}
public double getAlphaZ() {
	return alpha.z;
}
public Double3D setPosition() {
	return new Double3D(position);
}
public Double3D setOrientation() {
	return new Double3D(orientation);
}
public Double3D setVelocity() {
	return new Double3D(velocity);
}
public Double3D setOmega() {
	return new Double3D(omega);
}
public Double3D setAcceleration() {
	return new Double3D(acceleration);
}
public Double3D setAlpha() {
	return new Double3D(alpha);
}

public double setPositionX(double x) {
	return position.x = x;
}
public double setPositionY(double y) {
	return position.y = y;
}
public double setPositionZ(double z) {
	return position.z = z;
}
public double setOrientationX(double x) {
	return orientation.x;
}	
public double setOrientationY(double y) {
	return orientation.y = y;
}
public double setOrientationZ(double z) {
	return orientation.z = z;
}
public double setVelocityX(double  x) {
	return velocity.x = x;
}	
public double setVelocityY(double y) {
	return velocity.y = y;
}
public double setVelocityZ(double z) {
	return velocity.z = z;
}
public double setOmegaX(double x) {
	return omega.x = x;
}
public double setOmegaY(double y) {
	return omega.y = y;
}
public double setOmegaZ(double z) {
	return omega.z = z;
}
public double setAccelerationX(double x) {
	return acceleration.x = x;
}
public double setAccelerationY(double y) {
	return acceleration.y = y;
}
public double setAccelerationZ(double z) {
	return acceleration.z = z;
}
public double setAlphaX(double x) {
	return alpha.x = x;
}
public double setAlphaY(double y) {
	return alpha.y = y;
}
public double setAlphaZ(double z) {
	return alpha.z = z;
}
public Double3D getPosition() {
	return new Double3D(position);
}
public Double3D getOrientation() {
	return new Double3D(orientation);
}
public Double3D getVelocity() {
	return new Double3D(velocity);
}
public Double3D getOmega() {
	return new Double3D(omega);
}
public Double3D getAcceleration() {
	return new Double3D(acceleration);
}
public Double3D getAlpha() {
	return new Double3D(alpha);
}
}