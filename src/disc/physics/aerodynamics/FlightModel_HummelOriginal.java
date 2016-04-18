package disc.physics.aerodynamics;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

public class FlightModel_HummelOriginal{
	
	private double g;  		//The acceleration of gravity (m/s^2).
	private double RHO; 	//The density of air in kg/m^3.
	
	private double mass;
	private double radius;
	private double AREA;	//The area of a standard frisbee.
		
	private double CL0;		//The lift coefficient at alpha = 0.
	private double CLA;		//The lift coefficient dependent on alpha.
	private double CD0;		//The drag coefficent at alpha = 0.
	private double CDA;		//The drag coefficient dependent on alpha.
	private double CRr;
	private double CMO;
	private double CMa;
	private double CMq;
	private double CRp;
	private double CNr;
	private double Ixy;
	private double Iz;
	
	public FlightModel_HummelOriginal(){
	
		mass =  0.175;
		
		g = 9.7935; 	
	    RHO = 1.23;	
		AREA = 0.057;
		radius = Math.sqrt(AREA/Math.PI);
		CL0 = 0.3331;	
		CLA = 1.9124;
		CD0 = 0.1769;
		CDA = 0.685;	
		CRr = 0.00171;
		CMO = -0.0821;
		CMa = 0.4338;
		CMq = -0.005;
		CRp = -0.0055;
		CNr = 0.0000071;
		//long flights:
		//CMq = -0.0144; 
		//CRp = -0.0125;
		//CNr = -0.0000341;
		Ixy = 0.001219;
		Iz = 0.002352;
	}
	
	//hummels version
	public Double[] calculate(double t,Double[] y)
	{
		double[] x = calculate(t,new double[]{ y[0].doubleValue(), y[1].doubleValue(), y[2].doubleValue(),
												 y[3].doubleValue(), y[4].doubleValue(), y[5].doubleValue(),
												 y[6].doubleValue(), y[7].doubleValue(), y[8].doubleValue(),
												 y[9].doubleValue(), y[10].doubleValue(), y[11].doubleValue()}
							   );
		return new Double[] {x[0],x[1],x[2],x[3],x[4],x[5],x[6],x[7],x[8],x[9],x[10], x[11]};
	}
										// force on the frisbee
	@SuppressWarnings("unused")
	public double[] calculate(double t,double[] y) // the diffrential equation for y''= f(t,y,y')
	{
		Vector3d x = new Vector3d(y[0],y[1],y[2]); 				// position in global frame
		Vector3d v = new Vector3d(y[3],y[4],y[5]); 				// velocity in x,y,z direction
		Vector3d an = new Vector3d(y[6],y[7],y[8]); 			// angles orientation global frameprivate Vector3d dv; 				// accelaration in x,y,z direction
		Vector3d omega = new Vector3d(y[9],y[10],y[11]); 		// angular velocity around x,y,z axis of the frisbee Frame (roll,pitch,spin/yaw)
		Vector3d omegaD = new Vector3d();											// angular acceleration around x,y,z axis of the frisbee Frame (roll,pitch,spin/yaw)
		Vector3d fo = new Vector3d();												// force on the frisbee
		Vector3d mo;		// Momentum acting on the object
		
		double phi = an.x;
		double th = an.y;
		double gam = an.z;
		double fd = omega.x;
		double thd = omega.y;
		double gd = omega.z;
		double Id = Ixy;
		double Ia = Iz;
		double st = Math.sin(th);
		double ct = Math.cos(th);
		 
		
		Matrix3d rot1 = new Matrix3d();
		rot1.rotX(-an.x);
		Matrix3d rot2 = new Matrix3d();
		rot2.rotY(-an.y);
		
		Matrix3d T_c_N = new Matrix3d();
		T_c_N.setIdentity();
		T_c_N.mul(rot2);
		T_c_N.mul(rot1);
		T_c_N.transpose();
		
		// calculate aerodynamic forces and moments 
		// every Vector is expressed in the N frame
		Vector3d vel = new Vector3d(v.x,v.y,v.z);
		Vector3d c3 = new Vector3d();
		T_c_N.getColumn(2, c3);
		
		double vc3 = vel.dot(c3);
		Vector3d vp = new Vector3d(vel);
		Vector3d vc3c3 = new Vector3d(c3);
		vc3c3.scale(vc3);
		vp.sub(vc3c3);  //vp= [vel-vc3*c3]; Vector3d uvp = new Vector3d();
		
		double alpha = Math.atan(vc3/vp.length());
		double  Adp = RHO*Math.pow(v.length(), 2)*AREA/2; //air resistance
		Vector3d uvel = new Vector3d(vel);
		uvel.normalize();
		
		Vector3d uvp = new Vector3d(vp);
		uvp.normalize();
		
		Vector3d ulat = new Vector3d();
		ulat.cross(c3,uvp);
		ulat.normalize();
		
		Vector3d omega_C = new Vector3d(fd*ct,thd,fd*st+gd);
		Vector3d omega_N = new Vector3d(omega_C);
		T_c_N.transform(omega_N);
		
		double omegavp = omega_N.dot(uvp);
		double omegalat = omega_N.dot(ulat);
		double omegaspin = omega_N.dot(c3);
		
		// double AdvR = radius*2*omegaspin/2/vel.length() ; not used
		
		double CL = CL0 + CLA*alpha; 
		double alphaeq = -CL0/CLA;  // this is angle of attack at zero lift 
		double CD = CD0 + CDA*(alpha-alphaeq)*(alpha-alphaeq); 
		double CM=CMO + CMa*alpha; 
		Vector3d mvp = new Vector3d(uvp);
		mvp.scale(Adp*radius*2*(Math.sqrt(radius*2/g)*CRr*omegaspin +CRp*omegavp));
		
	    double lift = CL*Adp; 
	    double drag = CD*Adp; 
	    
	    Vector3d ulift = new Vector3d();       //ulift always has - d3 component 
	    Vector3d udrag = new Vector3d();
	    Vector3d uearth = new Vector3d();
	    
	    ulift.cross(uvel,ulat);
	    ulift.negate();
	    ulift.scale(lift);
	    
	    udrag = new Vector3d(uvel);
	    udrag.negate();
	   udrag.scale(drag);
	   
	   uearth.set(0, 0, 1);
	   uearth.scale(mass*g);

		fo.add(ulift);
		fo.add(udrag);
		fo.add(uearth);
		
		Vector3d mlat = new Vector3d(ulat);
		double pitch = Adp*radius*2* (CM + CMq*omegalat);
		mlat.scale(pitch);
		
		Vector3d mspin = new Vector3d(0,0,1);
		double spin = CNr*(omegaspin);
		mspin.scale(spin);
		
		T_c_N.transpose();
		T_c_N.transform(mvp);
		T_c_N.transform(mlat);
		//T_c_N.transform(mspin);
		
		Vector3d m = new Vector3d(mvp);
		m.add(mlat);
		m.add(mspin);
		
		Vector3d accel = new Vector3d(fo);
		accel.scale(1/mass);
		
		omegaD.x = (m.x + Id*thd*fd*st - Ia*thd*(fd*st+gd) +Id*thd*fd*st)/Id/ct +0.55e-16;
		omegaD.y = (m.y + Ia*fd*ct*(fd*st +gd) - Id*fd*fd*ct*st)/Id;  
		omegaD.z = (m.z - Ia*(omegaD.x*st + thd*fd*ct))/Ia;
		
		double[] ans = new double[]{v.x,v.y,v.z, accel.x, accel.y,accel.z, omega.x,omega.y,omega.z,omegaD.x,omegaD.y,omegaD.z};
		return ans;
	}
}
