package Ultimate;

import java.awt.Color;

import javax.vecmath.Matrix3d;

import sim.app.keepaway.Entity;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Double3D;
import sim.util.MutableDouble2D;

import javax.vecmath.Vector3d;

public abstract class UltimateEntity implements Steppable{

	double mass; 	// mass of the object
	public Vector3d location;
	public Vector3d orientation; 
	public Vector3d velocity;
	public Vector3d bump;
	
	public Vector3d force = new Vector3d();
	public Vector3d accel = new Vector3d();
	public Vector3d newLoc = new Vector3d();
	public Vector3d sumVector = new Vector3d();
	public double radius; // for visualization
	public double cap;
	
	// Accessors for inspector
	public double getX() { return location.x; }
	public void setX( double newX ) { location.x = newX; }

	public double getY() { return location.y; }
	public void setY( double newY ) { location.y = newY; }
	public double getZ() { return location.z; }
	public void setZ( double newZ ) { location.z = newZ; }

	public double getVelocityX() { return velocity.x; }
	public void setVelocityX( double newX ) { velocity.x = newX; }

	public double getVelocityY() { return velocity.y; }
	public void setVelocityY( double newY ) { velocity.y = newY; }

	public double getVelocityZ() { return velocity.z; }
	public void setVelocityZ( double newZ ) { velocity.z = newZ; }
	
	public double getAccelarationX() { return accel.x; }
	public void setAccelarationX( double newX ) { accel.x = newX; }
	
	public double getAccelarationZ() { return accel.z; }
	public void setAccelarationZ( double newZ ) { accel.z = newZ; }

	public double getAccelarationY() { return accel.y; }
	public void setAccelarationY( double newY ) { accel.y = newY; }	

	public double getRadius() { return radius; }
	
	public double getZigZag(){return radius;}
	
	public void setRadius( double newRadius ) 
	{
		radius = newRadius;
	} 

	public double getMass() { return mass; }
	public void setMass( double newMass ) { mass = newMass; } 

	// Constructor
	public UltimateEntity( Vector3d posi, Vector3d orientation, Vector3d velocity,Vector3d accel, double mass, double radius)
	{
		location = new Vector3d(posi); 
		this.orientation = new Vector3d(orientation);
		this.velocity = new Vector3d(velocity);
		bump = new Vector3d();
		this.accel =  new Vector3d(accel);
		this.radius = radius;
		this.mass = mass;
		cap = 1.0;
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
	public UltimateEntity(Double2D posi, double mass, double radius){
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

	public boolean isValidMove( final Ultimate ultimate, final Vector3d newLoc)
	{
		Bag objs = ultimate.ultimateField.getObjectsWithinDistance(new Double2D(location.x, location.y), 10);

		double dist = 0;

		// check objects
		for(int x=0; x<objs.numObjs; x++)
		{
			if(objs.objs[x] != this)
			{
				Vector3d tmp = new Vector3d(((UltimateEntity)objs.objs[x]).location);
				tmp.sub(newLoc);
				dist = tmp.length();

				if((((UltimateEntity)objs.objs[x]).radius + radius) > dist)  // collision!
					return false;
			}
		}

		// check walls
		if(newLoc.x > ultimate.fieldLength)
		{
			if (velocity.x > 0) velocity.x = -velocity.x;
			return false;
		}
		else if(newLoc.x < 0)
		{
			if (velocity.x < 0) velocity.x = -velocity.x;
			return false;
		}
		else if(newLoc.y > ultimate.fieldWidth)
		{
			if (velocity.y > 0) velocity.y = -velocity.y;
			return false;
		}
		else if(newLoc.y < 0)
		{
			if (velocity.y < 0) velocity.y = -velocity.y;
			return false;
		}

		// no collisions: return, fool
		return true;
	}

	public void capVelocity()
	{
		if(velocity.length() > cap)
			velocity.normalize();
			velocity.scale(cap);
	}
	// one step is a thousand of a second
	
	public void step( final SimState state )
	{
		Ultimate ultimate = (Ultimate)state;
		
		accel.scale(1/mass,force);
		accel.scale(ultimate.stepTime); // one step is a thousand of a second
		
		velocity.add(accel);
		Vector3d deltaVelocity = new Vector3d(velocity);
		deltaVelocity.scale(ultimate.stepTime);
		location.add(deltaVelocity);  // resets newLoc

		ultimate.ultimateField.setObjectLocation(this,new Double2D(location.x, location.y)); //set the loaction
	}
}