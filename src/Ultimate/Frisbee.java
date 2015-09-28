package Ultimate;

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
	double alpha;
	
	//Simulation Parameters
	private double g;  		//The acceleration of gravity (m/s^2).
	private double RHO; 		//The density of air in kg/m^3.
	private double AREA;	//The area of a standard frisbee.
	private double CL0;	//The lift coefficient at alpha = 0.
	private double CLA;	//The lift coefficient dependent on alpha.
	private double CD0;	//The drag coefficent at alpha = 0.
	private double CDA;	//The drag coefficient dependent on alpha.
	private double ALPHA0;	// minimum drag and zero lift at ALPHA0 Hummel page 9
	private double CRr;
	private double CMO;
	private double CMa;
	private double CMq;
	private double CRp;
	private double CNr;
	private double Ixy;
	private double Iz;
	
	public Frisbee(Double2D posi)	
	{
		//		double radius = 0.155; 	//radius of an ultrastar disc ?
		//		double mass = 0.175; 	//mass of an ulstrastar disc
		this(new Vector3d(posi.x,posi.y,0), new Vector3d());
	}	
	public Frisbee(Double3D posi)	
	{
		this(new Vector3d(posi.x,posi.y,posi.z), new Vector3d());
	}	
	public Frisbee(Vector3d posi, Vector3d velocity) 
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
		/* short flights*/
		CMq = -0.005;
		CRp = -0.0055;
		CNr = -0.0000071;
		/* long flights: */
//		CMq = -0.0144; 
//		CRp = -0.0125;
//		CNr = -0.0000341;
	
		Ixy = 0.001219;
		Iz = 0.002352;
		
	}
	
	@Override
	public void step(final SimState state)
	{   
		force = calcForces();
		rotAccel = calcAngularAccelaration();
		super.step(state); //apply force
		Ultimate ultimate = (Ultimate) state;
		ultimate.space.setObjectLocation(this,new Double3D(location.x,location.y,location.z));
		stepRotation(state); //apply rotation
		
	}
	
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
	public void flightForces() {
			
			double  airRes = RHO*Math.pow(velocity.length(), 2)*AREA/2; //air resistance
			
			// calculate alpha as the angle between velocity and x'
			Vector3d x = new Vector3d();
			Vector3d vel = new Vector3d(velocity.x,velocity.y,velocity.z);
			rotation.getColumn(0, x);
			alpha = x.angle(vel);
			//is alpha positive or negative in the z-x plane?
			vel.normalize();
			x.sub(vel);
			alpha = alpha * Math.signum(x.z);
			
			// 0.3.1 L = liftVec = normalized crossproduct of y' and velocity
			Vector3d liftVec = new Vector3d();
			Vector3d pitchVec = new Vector3d(rotation.m01,rotation.m11,rotation.m21);
			liftVec.cross(pitchVec,velocity);
			liftVec.normalize();
			
			// 0.3.2 D = DragVec = negative normalized velocity
			Vector3d dragVec = new Vector3d(velocity);
			dragVec.negate();
			dragVec.normalize();		
			
			// 1. calculate the translational forces that are applied to the disc related to the velocity vector
			force.set(0, 0, 0); //reset the force vector
			
			// 1.1 Calculation of the drag coefficient (for Prantl’s relationship)
			// using the relationship given by S. A. Hummel.
			double drag = (CD0 + CDA*Math.pow((alpha-ALPHA0),2))*airRes;
			dragVec.scale(drag);
			force.add(dragVec); // drag is in negative direction of the velocity vector
			
			// 1.2 Calculation of the lift coefficient (for Prantl’s relationship)
			// 1.2.1 Stall: the Stall angle of the Frisbee is about 45° afterwards the Lift will be set to Zero, due to no more accurate information
			double lift;
			if (alpha <= Math.PI/2)
			{
				lift =  (CL0 + CLA*alpha)*airRes;
			}
			else if (alpha >= Math.PI/2*3)
			{				
				lift =  (CL0 + CLA*(alpha - Math.PI))*airRes;
			}
			else lift = (CL0 - CLA*(alpha + Math.PI))*airRes;
			lift =  (CL0 + CLA*alpha)*airRes;
			liftVec.scale(lift);
			force.add(liftVec);		
			
			// (1.3.) gravitational force which is constant and always pointing "downwards" m*g
			force.add(new Vector3d(0,0,mass*g));
			
			// 2. calculate the angular Forces 
			//Initial position x = 0.
			// momenti berechnen HUMMEL
			
			// 2.1 the roll vector is is orthogonal to velocity and orientation and therefore lies along orientVelocNormalVec.
			double roll = (CRr * rotVelocity.z + CRp * rotVelocity.x) * airRes * radius * 2;
			
			// 2.2 the pitch vector lies along the projection from the velocity to the frisbeeplane ( defined by orientantion)
			double pitch = (CMO + CMa * alpha + CMq * rotVelocity.y) * airRes * radius * 2;
			
			//2.3 the spin vector lies along the orientational vector
			double spin = (CNr * rotVelocity.z)* airRes * radius*2;
			
			//2.4 the angular accelaration is defined by rotAccel = Momentum / Moment of inertia
		
			
			// the equation Momenti (HUMMEL p.33) M = I*a + omega x I*omega
			// and therefore I^(-1) * (M- omega x I*omega) = a
			
			Matrix3d interia = new Matrix3d();
			interia.setM00(Ixy);
			interia.setM11(Ixy);
			interia.setM22(Iz);
			
			// tmpCross := omega x I*omega
			Vector3d tmpCross = new Vector3d(rotVelocity);
			interia.transform(tmpCross);
			tmpCross.cross(rotVelocity, tmpCross);
			
			// set a to M
			rotAccel.x = roll;
			rotAccel.y = pitch;
			rotAccel.z = spin;	
			
			// set a to M - omega x I*omega
			rotAccel.sub(tmpCross);
			
			// set a to I^(-1) (M - omega x I*omega)
			interia.invert();
			interia.transform(rotAccel);
			
			
	/*		// 0.1 calculate  coordinates-transformation of the rotated system y' = velocity proection on frisbee plane z' = orientation of the frisbeeplane x' = y cross z
	
			// 0.2 the rotation on the z axis is defined by the velocity vector, there for we still need calculate the y' and x' vectors after rotation.
			
			// 0.2.1 z' = spinVector = normal vector to frisbee plane;
			Vector3d spinVec = new Vector3d(rotation.m02,rotation.m12,rotation.m22).normalize();
			
			// 0.2.1 y' = pitchVector = normalized velocity projected to frisbee plane which is defined by the normal vector orientation z'
			Vector3d velocityOrientationComponend = new Vector3d(spinVec).resize(spinVec.dot(velocity)).negate();
			Vector3d pitchVec = new Vector3d(velocity).add(velocityOrientationComponend).normalize();
			
			// 0.2.2. x' = rollVector = normalized crossproduct of y' and z'
			Vector3d rollVec =  new Vector3d();
			rollVec.setToCrossproduct(spinVec, pitchVec);
			rollVec.normalize();
			
			// we can also now calculate the z rotation: by solving the equation rotation * rot_z * (0,1,0)' = y' <=> (-sin (gamma), cos(gamma), 0) = rotation^(-1) * y'
			Matrix3d rotationInverted = new Matrix3d(rotation);
			rotationInverted.invert();
			Vector3d y= new Vector3d(0,1,0);
			rotationInverted.transform(y);
			double gamma = Math.acos(y.y);
			rotation.rotZ(gamma);
			
			// 0.3.1 L = liftVec = normalized crossproduct of -velocity and y' or y' and velocity
			Vector3d liftVec = new Vector3d();		
			liftVec.setToCrossproduct(pitchVec,velocity);
			
			// 0.3.2 D = DragVec = negative normalized velocity
			Vector3d dragVec = new Vector3d(velocity).negate().normalize();		
			
			// 1. calculate the translational forces that are applied to the disc related to the velocity vector
			
			// 1.1 Calculation of the drag coefficient (for Prantl’s relationship)
			// using the relationship given by S. A. Hummel.
			double drag = (CD0 + CDA*Math.pow((alpha-ALPHA0)*Math.PI/180,2))*airRes;
			force.add(dragVec.scale(drag)); // drag is in negative direction of the velocity vector
			
			// 1.2 Calculation of the lift coefficient (for Prantl’s relationship)
			double lift = (CL0 + CLA*alpha*Math.PI/180)*airRes;
			force.add(liftVec.scale(lift));		
			
			// (1.3.) gravitational force which is constant and always pointing "downwards" m*g
			force.add(new Vector3d(0,0,mass*g));
			
			// 2. calculate the angular Forces 
			//Initial position x = 0.
			// momenti berechnen HUMMEL
			
			// 2.1.2 calculate momenti of roll, pitch and spin
			Vector3d omega = new Vector3d();
			
			// 2.1 the pitch vector lies along the projection from the velocity to the frisbeeplane ( defined by orientantion)
			double pitch = (CMO + CMa * alpha + CMq * rotVelocity.y) * airRes * radius * 2;
			pitchVec.scale(pitch);
			
			// 2.2 the roll vector is is orthogonal to velocity and orientation and therefore lies along orientVelocNormalVec.
			double roll = (CRr * rotVelocity.z + CRp * rotVelocity.x) * airRes * radius * 2;
			rollVec.scale(roll);
			
			//2.3 the spin vector lies along the orientational vector
			double spin = (CNr * rotVelocity.z)* airRes * radius*2;
			spinVec.scale(spin);
			
			//2.4 the angular accelaration is defined by rotAccel = Momentum / Moment of inertia
			rotAccel.x = roll / Ixy;
			rotAccel.y = roll / Ixy;
			rotAccel.z = roll / Iz;	*/	
		}
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
		
		
	// The angle of attack
		
		double alpha = -vel.angle(x);
		// we need to know which of the vector is pointing more in z direction 
		// we project to the z-velocity plane. it is defined by the cross product of z and the velocity vector
		Vector3d plane = new Vector3d();
		Vector3d globalZ =new Vector3d (0,0,1);
		plane.cross(vel, globalZ);
		plane.normalize();
		
		//now we project vel and x to that plane
		//vel = projection(vel,plane) stays the same
		x = projection(x,plane);
				
		// now we need to fin out if the disc is pointing lower than the velocity vector.
		// if x is pointing in negative velocity direction meaning that the dis is flipped
		// it will be handled as if it was not fliped meaning that x is set as -x.
		if ( x.dot(vel) <=0 ) x.scale(-1);
		
		// now we just look for the closer angle to z. if vel is closer, alpha is negative.
		if (x.angle(globalZ) > vel.angle(globalZ)) alpha = -alpha;
		
		// if x lies now above the line defined by z = vel.angle*(sqrtx^2+y^2) the angle is positive else it is negative.
		
		
/*		double velAngle = Math.signum(vel.z) * x.angle(new Vector3d(vel.x,vel.y,0)); // at positiv z it points upward
		
		double line = velAngle * ( (new Vector3d(x.x, x.y,0)).length());
		if(line >= x.z)
		{
			alpha = -x.angle(vel);
		}
		else
		{
			alpha = x.angle(vel);
		}*/
		return alpha;
	}
	private Vector3d calcForces() 
	{
		double airRes = calcAirRes();
		alpha = calcAlpha();
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
	private Vector3d calcAngularAccelaration()
	{
		double airRes = calcAirRes();
		double alpha = calcAlpha();
		Vector3d angAccel;
		
		
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
	
	private void stepRotation(SimState state)
	{
		Ultimate ultimate = (Ultimate)state;
		rotAccel.scale(ultimate.stepTime); // one step is a thousand of a second
		
		rotVelocity.add(rotAccel); //add delta accelaration to velocity rotation
		Vector3d deltaRotVelocity = new Vector3d(rotVelocity);
		deltaRotVelocity.scale(ultimate.stepTime); //calc delta rotation
			
		Matrix3d deltaRotation = new Matrix3d();	//write rotation in Matrix form	
		deltaRotation.setIdentity();
		rotate(deltaRotation, deltaRotVelocity.x, 0); //apply x rotation
		rotate(deltaRotation, deltaRotVelocity.y, 1); // apply y rotation
		rotate(deltaRotation, deltaRotVelocity.z, 2); // apply z rotation

		rotation.mul(deltaRotation);		// // rotate by delta rotation 	
		
		//reorientate the disc
		Vector3d rotationZ = new Vector3d();
		Vector3d rotationX = new Vector3d();
		Vector3d rotationY = new Vector3d();
		rotation.getColumn(2,rotationZ);
		
		rotationY.cross(rotationZ, velocity);
		rotationY.normalize();
		rotationX.cross(rotationY,rotationZ);
		
		rotation.setColumn(0, rotationX);
		rotation.setColumn(1, rotationY);

		ultimate.space.setObjectLocation(this,new Double3D(location.x,location.y,location.z));
	}
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

	private void orientateDisc()
	{
		//  the x coordinate of the disc is always the of the projected velocity vector therefore rotation must be adjusted each step after forces have been applied.
		//	also the rotVelocity vector changes because the local coordinates of the disc change. We can get the rotation by solving the following equation:
		//  rotation_old * orientation = rotation_old^(-1) * rotation_new. 
		//TODO 
		//Since orientation is a rotation around the local z axis of the disc this may be simplified to beta = arccos( rotation_old.col1 * rotation_new.col1).
		Vector3d spinVec = new Vector3d(rotation.m02,rotation.m12,rotation.m22);
		
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
	public double getAlpha() {return alpha;}
	public double zigZag(double alpha)
	{
		double abs = Math.abs(alpha);
		if (abs <= Math.PI/4)
		{
			 // alpha stays the same
		}
		else if (abs >= Math.PI/2*3)
		{				
			alpha =  alpha- Math.PI; // also going up
		}
		else alpha = Math.PI/2 - alpha; // going down
		return alpha;
	}
	
}
