package physics;

import javax.vecmath.Matrix3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
	/* 	physical simulation of the aerodynamics of a Frisbee 
	 * 	this does not store the position or orientation of the disc, because firstly 
	 *  they are not necessary for calculating the dynamics and secondly it keeps the mason out of it so it can be implemented anywhere else.
	 */
public class FrisbeePhysics {
	

	private Matrix3d inertiaTensor;
	private double mass;
	private Vector3d velocity; 			// translatoric speed in x,y,z direction
	private Vector3d angularVelocity; 	// angular speed around x,y,z axis (roll,pitch,spin/yaw)
	private Vector3d force;				// translatoric accelaration on the frisbee
	private Vector3d angularMomentum;	// angular force accting on the object
	
	/* initialize Disc with Ultrastar data from Hummel. */
	public FrisbeePhysics(Vector3d velocity, Vector3d angularVelocity, Vector3d force, Vector3d angularMomentum){
	
		inertiaTensor = new Matrix3d						// Frisbee is a symetrical object, the inertia around the x and y axis are the same
			(	0.001219,			0,			0, 			// 0,0: Inertia on the x axis
				0		,	 0.001219,			0,			// 1,1: Inertia on the y axis
				0		,			0,	 0.002352	);		// 2,2: Inertia on the z axis	
		
		this.velocity = velocity; 			
		this.angularVelocity = angularVelocity;
		this.force = force;
		this.angularMomentum = angularMomentum;
	}
	public FrisbeePhysics(Vector3d velocity, Vector3d angularVelocity){
		this(velocity, angularVelocity, new Vector3d(0,0,0), new Vector3d(0,0,0));	
	}
	public FrisbeePhysics(Vector3d velocity){
		this(velocity, new Vector3d(0,0,0), new Vector3d(0,0,0), new Vector3d(0,0,0));	
	}
	public FrisbeePhysics(){
		this(new Vector3d(0,0,0), new Vector3d(0,0,0), new Vector3d(0,0,0), new Vector3d(0,0,0));	
	}
	
	
	// add force  for wind or objects hitting the  Frisbee
	public FrisbeePhysics addForce(Vector3d force){
		this.force.add((Tuple3d)force);
		return this;
	}
	public FrisbeePhysics addAngularMomentum(Vector3d angularMomentum){
		this.angularMomentum.add((Tuple3d)angularMomentum);
		return this;
	}
	
	// solves the ode for the given timespan with the given Timesteps
	public FrisbeePhysics solve(double timespan,double timeStep)
	{
		// very simple forward euler solver
		int cycles = (int)(timespan/timeStep);
		
		
		for  (int i = 0; i < cycles; i++)
		{
			
		}
		return this;
		
	}

}
