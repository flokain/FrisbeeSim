package discPhysics;

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
	private double radius;


	private Vector3d x; 				// position in global frame
	private Vector3d v; 				// velocity in x,y,z direction
	private Vector3d a; 				// accelaration in x,y,z directi
	private Vector3d o; 				// orientation global frameprivate Vector3d dv; 				// accelaration in x,y,z direction
	private Vector3d omega; 			// angular velocity around x,y,z axis of the frisbee Frame (roll,pitch,spin/yaw)
	private Vector3d domega; 			// angular acceleration around x,y,z axis of the frisbee Frame (roll,pitch,spin/yaw)
	private Vector3d fo;				// force on the frisbee
	private Vector3d mo;				// Momentum acting on the object
	

	public FrisbeePhysics(){
		this(new Vector3d(0,0,0));	
	}
	public FrisbeePhysics(Vector3d v){
		this(v, new Vector3d(0,0,0));	
	}
	public FrisbeePhysics(Vector3d v, Vector3d omega){
		this(new Vector3d(0,0,0),new Vector3d(0,0,0),v, omega, new Vector3d(0,0,0), new Vector3d(0,0,0));	
	}
	
	/* initialize Disc with Ultrastar data from Hummel. */
	public FrisbeePhysics(Vector3d x,Vector3d o,Vector3d v, Vector3d omega, Vector3d fo, Vector3d mo){
	
		inertiaTensor = new Matrix3d						// Frisbee is a symetrical object, the inertia around the x and y axis are the same
			(	0.001219,			0,			0, 			// 0,0: Inertia on the x axis
				0		,	 0.001219,			0,			// 1,1: Inertia on the y axis
				0		,			0,	 0.002352	);		// 2,2: Inertia on the z axis		
	}
	
	
	public FrisbeePhysics addAngularMomentum(Vector3d mo){
		this.mo.add((Tuple3d)mo);
		return this;
	}
	
	// add force  for wind or objects hitting the Frisbee
	public FrisbeePhysics addForce(Vector3d force){
		this.fo.add((Tuple3d)force);
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
