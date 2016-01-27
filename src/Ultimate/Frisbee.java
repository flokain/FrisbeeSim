package Ultimate;

import java.io.IOException;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.*;

public class Frisbee extends UltimateEntity implements Steppable
{
	Vector3d rotForce;
	Vector3d rotAccel;
	Vector3d rotVelocity; // in relation to the frisbee Frame (x',y',z')
	Vector3d angles;
	Vector3d anglesd;
	Matrix3d rotation; //phi,theta,gamma

	double 	alpha;
	
	//Simulation Parameters
	private static double g;  		//The acceleration of gravity (m/s^2).
	private static double RHO; 		//The density of air in kg/m^3.
	private static double AREA;		//The area of a standard frisbee.
	private static double CL0;		//The lift coefficient at alpha = 0.
	private static double CLA;		//The lift coefficient dependent on alpha.
	private static double CD0;		//The drag coefficent at alpha = 0.
	private static double CDA;		//The drag coefficient dependent on alpha.
	private static double ALPHA0;	//Minimum drag and zero lift at ALPHA0 Hummel page 9
	private static double CRr;
	private static double CMO;
	private static double CMa;
	private static double CMq;
	private static double CRp;
	private static double CNr;
	private static double Ixy;
	private static double Iz;
	
	public Frisbee()	
	{
		this(new Vector3d(), new Vector3d());
	}	
	public Frisbee(Double2D posi)	
	{
		this(new Vector3d(posi.x,posi.y,0), new Vector3d());
	}	
	public Frisbee(Double3D posi)	
	{
		this(new Vector3d(posi.x,posi.y,posi.z), new Vector3d());
	}	
	public Frisbee(Vector3d posi, Vector3d velocity) 
	{
		this(posi,velocity,0);
	}
	public Frisbee(Vector3d posi, Vector3d velocity, int Coeff) 
	{
		super( posi,velocity,new Vector3d(), 0.175, 0.134698416889272);
		
		rotForce = new Vector3d();
		rotAccel = new Vector3d();
		rotVelocity = new Vector3d();
		angles = new Vector3d();
		rotation = new Matrix3d();
		rotation.setIdentity();	
		
		g = -9.7935; 	
	    RHO = 1.23;	
		AREA = Math.pow(radius,2)* Math.PI;	
		CL0 = 0.3331;	
		CLA = 1.9124;
		CD0 = 0.1769;
		CDA = 0.685;	
		ALPHA0 = -CL0/CLA;	
		CRr = 0.00171;
		CMO = -0.0821;
		CMa = 0.4338;
		Ixy = 0.001219;
		Iz = 0.002352;
		switch (Coeff){
			case 0:
				/* short flights*/
				CMq = -0.005;
				CRp = -0.0055;
				CNr = -0.0000071;
				break;
			case 1:
				/* long flights: */
				CMq = -0.0144; 
				CRp = -0.0125;
				CNr = -0.0000341;
				break;
		}
	}

	// set the initial conditions for a throw.
	// TODO maybe rename to init? or more abstract let the constructor do the init job
	public void throwDisc(Vector3d vel,Vector3d angles, Vector3d anglesVelocity)
	{
		velocity = vel;
		
		this.angles = angles;
		anglesd = anglesVelocity;
		
		rotVelocity = anglesVelocity;
		Vector3d tmpX = new Vector3d(velocity);
		tmpX.normalize();
		
		Vector3d tmpY = new Vector3d();
		tmpY.cross(new Vector3d(0,0,1),tmpX);
		tmpY.normalize();
		
		Vector3d tmpZ = new Vector3d();
		tmpZ.cross(tmpX, tmpY);
		// initial orientation of the disc
		
		rotation.setColumn(0, tmpX);	// x' points in velocity direction,
		rotation.setColumn(1, tmpY);	// y' is perpendicular to the velocity-z plane
		rotation.setColumn(2, tmpZ);	// z' is perpendicular to the x'-y' plane pointing "up";
		// add inital rotations to the disc plane
		
		rotation.setIdentity();
		rotate(rotation, angles.x, 0);
		rotate(rotation, angles.y, 1); // deines alpha at the beginning of the flight
		orientateDisc();
	}
	
	//Setter and Getter
	public Vector3d getRotVelocity() 					{ return rotVelocity;				}
	public void setRotVelocity(Vector3d rotVelocity) 	{ this.rotVelocity = rotVelocity;	}
	public Vector3d getAngles()							{ return angles;					}
	public void setAngles(Vector3d angles) 				{ this.angles = angles;				}
	public double getAlpha() 							{ return alpha;						}
	public Matrix3d getRotation() 						{ return rotation;					}
	public void setRotation(Matrix3d rotation) 			{ this.rotation = rotation;			}
	
	//Variable Calculations
	private double calcAirRes()
	{
		return RHO*Math.pow(velocity.length(), 2)*AREA/2; //air resistance
	}
	private double calcAlpha()
	{
		// calculate alpha as the angle between velocity and x'
		Vector3d x = new Vector3d();
		Vector3d vel = new Vector3d(velocity);
		rotation.getColumn(0, x);
		double alpha = -vel.angle(x);
		
		rotation.getColumn(2, x);
		double vc3 = vel.dot(x);
		x.scale(vc3);
		vel.sub(x);
		
	// The angle of attack
		
		alpha = Math.atan(vc3/vel.length());
		// we need to know which of the vector is pointing more in z direction 
		// we project to the z-velocity plane. it is defined by the cross product of z and the velocity vector
		return alpha;
	}
	
	private Vector3d calcForces( double alpha, double airRes, Vector3d velocity, Matrix3d rotation) 
	{
		Vector3d force = new Vector3d();
		
	//calculate  Lift
		
		//liftVec = normalized crossproduct of pitch vector' and velocity

		Vector3d liftVec = new Vector3d();
		Vector3d pitchVec = new Vector3d(rotation.m01,rotation.m11,rotation.m21);
		liftVec.cross(pitchVec,velocity);
		liftVec.normalize();
		
		// 1.2 Calculation of the lift coefficient (for Prantl’s relationship)
		// 1.2.1 Stall: the Stall angle of the Frisbee is about 45° afterwards the Lift will be set to Zero, due to no more accurate information
		// the lift as a function of alpha is extendend from a linear function on [-pi/4,pi/4] to linear splines on [0,pi]
		// the function looks like this: in [0,pi/4] and [3pi/4 pi] it is linear rising in [pi/4, 3pi/4] it is falling
		// the function is odd meaning f(-x) = -f(x) i call it the zigzag function
		
		double lift = (CLA*(alpha) + CL0)*airRes; // add in the alpha0 shift and multiply by air ressistance
		liftVec.scale(lift); // resize by lift
		force.set(liftVec);	 // set the force
	
		
	//calculate  Drag	
		
		// DragVec = negative normalized velocity
		Vector3d dragVec = new Vector3d(velocity);
		dragVec.negate();
		dragVec.normalize();		
		
		// Calculation of the drag coefficient (for Prantl’s relationship)
		// using the relationship given by S. A. Hummel.
		
		// drag is quadratic in angles [-pi/2 +ALPHA0, pi/2+ALPHA0] and Pi periodic meaning f(y+k*pi -pi/2) = f(y) 
		double drag;
		drag = (CD0 + CDA*Math.pow((alpha-ALPHA0),2))*airRes;
		dragVec.scale(drag);
		force.add(dragVec); // drag is in negative direction of the velocity vector
		
	// add gravity
		
		// gravitational force is constant and always pointing in negative z direction
		force.add(new Vector3d(0,0,mass*g));
		return force;
	}
	private Vector3d calcAngularAccelaration( double alpha, double airRes, Vector3d angles, Vector3d rotVelocity)
	{
		
		
	// calculate the angular Momentum
		
		// momenti berechnen HUMMEL
		// the roll vector is is orthogonal to velocity and orientation and therefore lies along orientVelocNormalVec.
		double roll = (CRr * rotVelocity.z + CRp * rotVelocity.x) * airRes * radius * 2;
		
		// the pitch vector lies along the projection from the velocity to the frisbeeplane ( defined by orientantion)
		double pitch = (CMO + CMa * alpha + CMq * rotVelocity.y) * airRes * radius * 2;
		
		// the spin vector lies along the orientational vector
		double spin = (CNr * rotVelocity.z)* airRes * radius*2;
		
	// calculate angular acceleration
		
		// the equation Momenti (HUMMEL p.33) M = I*a + omega x I*omega
		// and therefore I^(-1) * (M- omega x I*omega) = a
		
		Vector3d omega = new Vector3d (Math.cos(angles.y)*rotVelocity.x,rotVelocity.y,Math.sin(angles.y)*rotVelocity.x + rotVelocity.z);
		Vector3d omegaF = new Vector3d (Math.cos(angles.y)*rotVelocity.x,rotVelocity.y,Math.sin(angles.y)*rotVelocity.x);
		
		Matrix3d interia = new Matrix3d();
		interia.setM00(Ixy);
		interia.setM11(Ixy);
		interia.setM22(Iz);
		
		// tmpCross := omegaF x I*omega :precission
		Vector3d precission = new Vector3d();
		interia.transform(omega); // I*omega
		precission.cross(omegaF, omega); //omegaF x I*omega
		
		// set a to M
		rotAccel = new Vector3d(roll,pitch,spin);
		
		// set a to M - omegaF x I*omega
		rotAccel.sub(precission); //M - omegaF x I*omega
		
		// set a to I^(-1) (M - omegaF x I*omega)
		interia.invert();
		interia.transform(rotAccel);
		
		// since [Hummel, p.34] a = omega', a = phi'' cos(theta) -phi' sin(theta), theta'',  - phi'' sin(theta) + phi' cos(theta)*theta' +gamma''
		// we have to transform the coordinates to get (Phi'',theta'',gamma'')
		
		rotAccel.x = (rotAccel.x + rotVelocity.x * Math.sin(angles.y))/Math.cos(angles.y);  // phi'' = (a[1] + phi' sin(theta))/cos(theta), since   a[1] = phi'' cos(theta) -phi' sin(theta)
		rotAccel.z = rotAccel.z - rotVelocity.x * Math.cos(angles.y)*rotVelocity.y - rotAccel.x * Math.sin(angles.y);  // gamma'' = (a[3] - phi' cos(theta)- phi'' sin(theta)*theta', since   a[1] = phi'' cos(theta) -phi' sin(theta)
		
		return rotAccel;
	}
	//Simulation stepper
	@Override
	public void step(final SimState state)
	{   
		alpha = calcAlpha();
		double airRes = calcAirRes();
		force = calcForces(alpha,airRes,velocity,rotation);
		rotAccel = calcAngularAccelaration(alpha,airRes,angles, rotVelocity);
		Ultimate ultimate = (Ultimate) state;
		
		//flightForces();
		stepRotation(ultimate); //apply rotation
		super.step(state); //apply force and set location in the enviroment

	}
	private void stepRotation(Ultimate state)
	{
		
//		Vector3d deltaRotVelocity = new Vector3d(rotVelocity);
//		deltaRotVelocity.scale(state.stepTime); 	// calc delta rotation
//		
//		angles.add(deltaRotVelocity);
//		
//		// dR/dt = [ omega x Rx,  omega x Ry, omega x Rz ]
//		Matrix3d deltaRotation = new Matrix3d();	//write rotation in Matrix form	
//		
//		
//		Vector3d Rx = new Vector3d(rotVelocity);
//		Vector3d Ry = new Vector3d(rotVelocity);
//		Vector3d Rz = new Vector3d(rotVelocity);
//		
//		//oder doch row?
//		rotation.getColumn(0, Rx);
//		rotation.getColumn(1, Ry);
//		rotation.getColumn(2, Rz);
//		
//		Rx.cross(rotVelocity, Rx);
//		Ry.cross(rotVelocity, Ry);
//		Rz.cross(rotVelocity, Rz);
//		
//		deltaRotation.setColumn(0, Rx);
//		deltaRotation.setColumn(1, Ry);
//		deltaRotation.setColumn(2, Rz);
//		
//		deltaRotation.mul(state.stepTime);
//		
//		rotate(deltaRotation, deltaRotVelocity.x, 0); // apply x rotation
//		rotate(deltaRotation, deltaRotVelocity.y, 1); // apply y rotation
//		rotate(deltaRotation, deltaRotVelocity.z, 2); // apply z rotation
//
//		rotation.mul(deltaRotation);		// // rotate by delta rotation 	
//			
//		rotation.setIdentity();
//		rotate(rotation, angles.x, 0);
//		rotate(rotation, angles.y, 1);
//		rotate(rotation, angles.z, 2);
//		rotAccel.scale(state.stepTime); // one step is a thousand of a second
//		rotVelocity.add(rotAccel); //add delta accelaration to velocity rotation
		
		//reorientate the disc
		
		orientateDisc();
		Vector3d deltaRotVelocity = new Vector3d(rotVelocity);
		deltaRotVelocity.scale(state.stepTime); //calc delta rotation
		angles.add(deltaRotVelocity);	
		Matrix3d deltaRotation = new Matrix3d();	//write rotation in Matrix form	
		deltaRotation.setIdentity();
		rotate(deltaRotation, deltaRotVelocity.x, 0); //apply x rotation
		rotate(deltaRotation, deltaRotVelocity.y, 1); // apply y rotation
		rotate(deltaRotation, deltaRotVelocity.z, 2); // apply z rotation

		rotation.mul(deltaRotation);		// // rotate by delta rotation 	
		
		//reorientate the disc
		orientateDisc();
		rotAccel.scale(state.stepTime); // one step is a thousand of a second
		rotVelocity.add(rotAccel); //add delta accelaration to velocity rotation
	}
	
	// Matlab copy of  Hummels frisbee eqautions for evaluation purposes
	public void flightForces() {
			
			
			Matrix3d T_c_N = new Matrix3d();
			T_c_N.setIdentity();
			rotate(T_c_N,angles.x,0);
			rotate(T_c_N,angles.y,1);
			
			
			// calculate aerodynamic forces and moments 
			// everyvector is expressed in the N frame
			Vector3d vel = new Vector3d(velocity.x,velocity.y,velocity.z);
			Vector3d c3 = new Vector3d();
			T_c_N.getColumn(2, c3);
			
			double vc3 = vel.dot(c3);
			Vector3d vp = new Vector3d(vel);
			Vector3d uvp = new Vector3d();
			Vector3d uvel = new Vector3d();
			Vector3d ulat = new Vector3d();
			Vector3d vc3c3 = new Vector3d(c3);
			vc3c3.scale(vc3);
			
			vp.sub(vc3c3);  //vp= [vel-vc3*c3]; 
			alpha = Math.atan(vc3/vp.length());
			
			double  Adp = RHO*Math.pow(velocity.length(), 2)*AREA/2; //air resistance
			
			uvel = vel;
			uvel.normalize();
			
			uvp = vp;
			uvp.normalize();
			
			ulat.cross(c3,uvp);
			
			Vector3d omega_C = new Vector3d (Math.cos(angles.y)*rotVelocity.x,rotVelocity.y,Math.sin(angles.y)*rotVelocity.x + rotVelocity.z);
			Vector3d omega_N = new Vector3d(omega_C);
			T_c_N.transform(omega_N);
			
			double omegavp = omega_N.dot(uvp);
			double omegalat = omega_N.dot(ulat);
			double omegaspin = omega_N.dot(c3);
			
			double AdvR = radius*2*omegaspin/2/vel.length() ;
			
			double CL = CL0 + CLA*alpha; 
			double alphaeq = -CL0/CLA;  // this is angle of attack at zero lift 
			double CD = CD0 + CDA*(alpha-alphaeq)*(alpha-alphaeq); 
			double CM=CMO + CMa*alpha; 
		    
		    double lift = CL*Adp; 
		    double drag = CD*Adp; 
		    Vector3d ulift = new Vector3d();       //ulift always has - d3 component 
		    Vector3d udrag = new Vector3d();
		    Vector3d uearth = new Vector3d();
		    
		    ulift.cross(uvel,ulat);
		    ulift.negate();
		    udrag = uvel;
		    udrag.negate();
		    uearth.set(0, 0, 1);
		    
		    ulift.scale(lift);
		    udrag.scale(drag);
		    uearth.scale(mass*g);

		    force.set(0,0,0);
			force.add(ulift);
			force.add(udrag);
			force.add(uearth);
			
			Vector3d mvp = new Vector3d(uvp);
			Vector3d mlat = new Vector3d(ulat);
			Vector3d mspin = new Vector3d(c3);
			
			double roll = Adp*radius*2* (CRr*omegaspin + CRp*omegavp);
			double pitch = Adp*radius*2* (CM + CMq*omegalat);
			double spin = Adp*radius*2*CNr*(omegaspin);
			
			mvp.scale(roll);
			mlat.scale(pitch);
			mspin.scale(spin);
			
			T_c_N.transpose();
			T_c_N.transform(mvp);
			T_c_N.transform(mlat);
			T_c_N.transform(mspin);
			
			Vector3d m = new Vector3d();
			m.add(mvp);
			m.add(mlat);
			m.add(mspin);
			
			accel = new Vector3d(force);
			accel.scale(1/mass);
			
			double phi = angles.x;
			double th = angles.y;
			double gam = angles.z;
			double fd = rotVelocity.x;
			double thd = rotVelocity.y;
			double gd = rotVelocity.z;
			double Id = Ixy;
			double Ia = Iz;
			double st = Math.sin(th);
			double ct = Math.cos(th);
			
			rotAccel.x = (m.x + Id*thd*fd*st - Ia*thd*(fd*st+gd) +Id*thd*fd*st)/Id/ct;
			rotAccel.y = (m.y + Ia*fd*ct*(fd*st +gd) - Id*fd*fd*ct*st)/Id; 
			rotAccel.z = (m.z - Ia*(rotAccel.x*st + thd*fd*ct))/Ia;
		}
	
	//An old relict, find out what it is...
	public void flightForces2() {
		final double g = 	-9.81; 		//The acceleration of gravity (m/s^2).
		final double m = 	 0.175; 	//The mass of a standard frisbee in kilograms.
	    final double RHO = 	 1.23;		//The density of air in kg/m^3.
		final double AREA =  0.0568;	//The area of a standard frisbee.
		final double CL0 = 	 0.133;	//The lift coefficient at alpha = 0.
		final double CLA = 	 1.9124;	//The lift coefficient dependent on alpha.
		final double CD0 = 	 0.1769;	//The drag coefficent at alpha = 0.
		final double CDA = 	 0.685;	//The drag coefficient dependent on alpha.
		final double ALPHA0 = -CL0/CLA;	// minimum drag and zero lift at ALPHA0 Hummel page 9
		final double CRr = 	 0.00171;
		final double CMO = 	-0.0821;
		final double CMa = 	 0.4338;
		final double CMq = 	-0.005;
		final double CRp = 	-0.0055;
		final double CNr = 	-0.0000071;
		/* long flights:
		final double CMq = -0.0144; 
		final double CRp = -0.0125;
		final double CNr = -0.0000341;
		*/
		final double Ixy = 0.00122;
		final double Iz = 0.00235;
		
		// 0 calculate Hilfsvariablen
		double ct = Math.cos(angles.y);
		double st = Math.sin(angles.y);
		double sf = Math.sin(angles.x);
		double cf = Math.cos(angles.x);
		
		double thd = anglesd.y;
		double fd = anglesd.x;
		double gd = anglesd.z;
		
		Matrix3d T_c_N = new Matrix3d();
		T_c_N.rotX(angles.x);
		rotate(T_c_N,angles.y,1);
		T_c_N.transpose();
		
		Vector3d c3 = new Vector3d(T_c_N.m02,T_c_N.m12,T_c_N.m22);
		double length = c3.dot(velocity);
		Vector3d vp = new Vector3d(c3);
		vp.normalize();
		vp.scale(length);
		vp.sub(velocity);
		vp.negate();
		
		double alpha = Math.atan(length/vp.length());
		double  airRes = RHO*velocity.lengthSquared()*AREA/2; //air resistance
		
		Vector3d uvel = new Vector3d(velocity);
		uvel.normalize();
		
		Vector3d uvp = new Vector3d(vp);
		uvp.normalize();
		
		Vector3d ulat = new Vector3d();
		ulat.cross(c3,uvp);
		
		Vector3d omegaD_N_inC = new Vector3d(fd * ct, thd, fd*st+gd);
		Vector3d omegaD_N_inN = new Vector3d(omegaD_N_inC);
		
		T_c_N.transpose();
		T_c_N.transform(omegaD_N_inN);
		T_c_N.transpose();
		
		double omegavp = omegaD_N_inN.dot(uvp); 
		double omegalat = omegaD_N_inN.dot(ulat); 
		double omegaspin = omegaD_N_inN.dot(c3); 
		
		double lift = (CL0 + CLA*alpha)*airRes;
		double drag = (CD0 + CDA*Math.pow((alpha-ALPHA0),2))*airRes;
		Vector3d ulift = new Vector3d();
		ulift.cross(uvel,ulat);
		ulift.negate();
		
		Vector3d udrag = new Vector3d(uvel);
		udrag.negate();
		
		ulift.scale(lift);
		udrag.scale(drag);
		force.set(ulift);
		force.add(udrag);
		force.add(new Vector3d(0,0,mass*g));
		
		double roll = (CRr * omegaspin + CRp * omegavp) * airRes * radius * 2;
		
		// 2.2 the pitch vector lies along the projection from the velocity to the frisbeeplane ( defined by orientantion)
		double pitch = (CMO + CMa * alpha + CMq * omegalat) * airRes * radius * 2;
		
		//2.3 the spin vector lies along the orientational vector
		double spin = (CNr * omegaspin)*  radius*2;
		ulat.scale(pitch);
		uvp.scale(roll);
		Vector3d uspin = new Vector3d(0,0,spin);
		
		T_c_N.transform(ulat);
		T_c_N.transform(uvp);
		Vector3d M = new Vector3d();
		M.add(ulat);
		M.add(uvp);
		M.add(uspin);
		
		double Id = Ixy;
		double Ia = Iz;
		
		rotAccel.x = (M.x + Id*thd*fd*st - Ia*thd*(fd*st+gd) +Id*thd*fd*st)/Id/ct;
		rotAccel.y = (M.y + Ia*fd*ct*(fd*st +gd) - Id*fd*fd*ct*st)/Id;
		rotAccel.z = (M.z - Ia*(rotAccel.x*st + thd*fd*ct))/Ia;
	}

	// Helpfull functions for Matrix calculus
	private void orientateDisc()
	{
		//  the x coordinate of the disc is always the of the projected velocity vector therefore rotation must be adjusted each step after forces have been applied.
		//	also the rotVelocity vector changes because the local coordinates of the disc change. We can get the rotation by solving the following equation:
		//  rotation_old * orientation = rotation_old^(-1) * rotation_new. 
		//TODO 
		//Since orientation is a rotation around the local z axis of the disc this may be simplified to beta = arccos( rotation_old.col1 * rotation_new.col1).
		Vector3d spinVec = new Vector3d();
		rotation.getColumn(2, spinVec);
		
		// 1 rollvec is the projection of velocity to the frisbee plane
		Vector3d rollVec  = projection(velocity, spinVec);
		rollVec.normalize();
		
		// 2 pitchVec is perpedicular to spin rollvec 
		Vector3d pitchVec = new Vector3d();
		pitchVec.cross(spinVec,rollVec);
		pitchVec.normalize();
				
		// 3. spin stays the same
		
		rotation.setColumn(0, rollVec);
		rotation.setColumn(1, pitchVec);
		
//		rotation.setIdentity();
//		rotate(rotation,angles.x,0);
//		rotate(rotation,angles.y,1);
//		//rotate(rotation,angles.z,2);
	}
	private void rotate(Matrix3d mat,double angle,int axis) 
	{
		Matrix3d rot = new Matrix3d();
		if (axis == 0) rot.rotX(angle);
		else if (axis == 1) rot.rotY(angle);
		else if (axis == 2) rot.rotZ(angle);
		else 
		{
			throw new Error("illegal value for axis must be 0,1 or 2");
		}
		mat.mul(rot);
	}
	private Vector3d projection(Vector3d vec,Vector3d direc)
	{
		double length = vec.dot(direc);
		direc.normalize();
		direc.scale(length);		
		Vector3d proj = new Vector3d(vec);
		proj.sub(direc);
		return proj;
	}

}
