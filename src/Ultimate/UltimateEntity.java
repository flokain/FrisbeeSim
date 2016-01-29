package Ultimate;

import java.awt.Color;

import javax.vecmath.Vector3d;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import sim.util.Double3D;

public abstract class UltimateEntity implements Steppable{

	double mass; 
	public Vector3d location;
	public Vector3d orientation; 
	public Vector3d velocity;
	public Vector3d force = new Vector3d();
	public Vector3d accel = new Vector3d();
	public double radius; // all elements are considerd to be balls or tubes for visualization
	
	// Getter and Setter, important for inspector in Mason
	public Vector3d getLocation() 						{ return location; 			}
	public void setLocation( Vector3d location ) 		{ this.location = location; }
	public Vector3d getVelocity() 						{ return velocity; 			}
	public void setVelocity( Vector3d velocity ) 		{ this.velocity = velocity; }
	public Vector3d getAccelaration() 					{ return accel; 			}
	public void setAccelaration( Vector3d accel ) 		{ this.accel = accel; 		}
	public double getRadius() 							{ return radius; 			}
	public void setRadius( double radius )				{ this.radius = radius; 	}
	public double getMass() 							{ return mass; 				}
	public void setMass( double mass ) 					{ this.mass = mass; 		} 

	// Constructors
	public UltimateEntity( Vector3d posi, Vector3d orientation, Vector3d velocity,Vector3d accel, double mass, double radius)
	{
		location 		 = 	new Vector3d(posi); 
		this.orientation = 	new Vector3d(orientation);
		this.velocity 	 = 	new Vector3d(velocity);
		this.accel 		 =	new Vector3d(accel);
		this.radius 	 =	radius;
		this.mass 		 =	mass;
	}
	public UltimateEntity( Vector3d posi, Vector3d velocity,Vector3d accel, double mass, double radius)
	{
		this(posi, new Vector3d(0,0,1), velocity, accel, mass, radius);
	}
	public UltimateEntity( Double2D posi, Double2D orientation, Double2D velocity, Double2D accel, double mass, double radius)
	{
		this( new Vector3d(posi.x,posi.y,0), new Vector3d(orientation.x,orientation.y,0), new Vector3d(velocity.x,velocity.y,0), new Vector3d(accel.x,accel.y,0), mass, radius);
	}
	public UltimateEntity( Double2D posi, Double2D velocity, Double2D accel, double mass, double radius)
	{
		this( new Vector3d(posi.x,posi.y,0), new Vector3d(1,0,0), new Vector3d(velocity.x,velocity.y,0), new Vector3d(accel.x,accel.y,0), mass, radius);
	}
	public UltimateEntity( Double2D posi, double mass, double radius){
		this(  posi,new Double2D(0,0),new Double2D(0,0), mass, radius);	
	}
	public UltimateEntity( Double2D posi, double radius, Color c )
	{
		this( posi,new Double2D(0,0),new Double2D(0,0),1.0, radius);
	}
	public UltimateEntity( Double2D posi, Color c)
	{
		this(posi, 1.0, c);
	}

	@Override
	public void step( final SimState state )
	{
		Ultimate ultimate = (Ultimate)state;
		
		accel.scale(1./mass,force);
		accel.scale(ultimate.stepTime); // one step is a thousand of a second
		Vector3d deltaVelocity = new Vector3d(velocity);
		deltaVelocity.scale(ultimate.stepTime);
		location.add(deltaVelocity);  // resets newLoc
		velocity.add(accel);
		ultimate.ultimateField2D.setObjectLocation(this,new Double2D(location.x, location.y)); //set the location in 2d portrayal
		ultimate.ultimateField3D.setObjectLocation(this,new Double3D(location.x,location.y,location.z)); //set the location in 3d portrayal
	}
}