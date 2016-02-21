package disc.physics.aerodynamics;

import java.io.IOException;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import sim.util.Double3D;
import disc.physics.Disc;
import disc.physics.aerodynamics.FlyingDisc.flightCoefficientsType;

public class FlightModel_HummelNewCorrected extends FlightModel_HummelNew{
	

	public FlightModel_HummelNewCorrected() throws IOException {
		super();
	}
	
	public FlightModel_HummelNewCorrected(FlyingDisc disc)
	{
		super(disc);
	}

	public double[] calculate(double[] y) // the diffrential equation for y''= f(t,y,y')
	{
		Vector3d x = new Vector3d(y[0],y[1],y[2]); 				// position in global frame
		Vector3d an = new Vector3d(y[3],y[4],y[5]); 				// velocity in x,y,z direction
		Vector3d v = new Vector3d(y[6],y[7],y[8]); 			// angles orientation global frameprivate Vector3d dv; 				// accelaration in x,y,z direction
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
		double Id = disc.inertia_XY;
		double Ia = disc.inertia_Z;
		double st = Math.sin(th);
		double ct = Math.cos(th);
		 
		
		Matrix3d rot1 = new Matrix3d();
		rot1.rotX(an.x);
		Matrix3d rot2 = new Matrix3d();
		rot2.rotY(an.y);
		
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
		
		double alpha = (vp.length() == 0) ? 0:Math.atan(vc3/vp.length());

		double  Adp = RHO*Math.pow(v.length(), 2)*disc.area/2; //air resistance
		Vector3d uvel = new Vector3d(vel);
		if (uvel.length() > 0) uvel.normalize();
		
		Vector3d uvp = new Vector3d(vp);
		if(uvp.length() > 0) uvp.normalize();
		
		Vector3d ulat = new Vector3d();
		ulat.cross(c3,uvp);
		if(ulat.length() > 0) ulat.normalize();
		
		Vector3d omega_C = new Vector3d(fd*ct,thd,fd*st+gd);
		Vector3d omega_N = new Vector3d(omega_C);
		T_c_N.transform(omega_N);
		
		double omegavp = omega_N.dot(uvp);
		double omegalat = omega_N.dot(ulat);
		double omegaspin = omega_N.dot(c3);
		
		// double AdvR = radius*2*omegaspin/2/vel.length() ; not used
		
		double CL = disc.cl[0] + disc.cl[1]*alpha; 
		double alphaeq = -disc.cl[0]/disc.cl[1];  // this is angle of attack at zero lift 
		double CD = disc.cd[0] + disc.cd[2]*(alpha-alphaeq)*(alpha-alphaeq); 
		double CM=disc.ctau[0] + disc.ctau[1]*alpha; 
		Vector3d mvp = new Vector3d(uvp);
		
		double radius = Math.sqrt(disc.area / Math.PI);
		mvp.scale(Adp*radius*2*(Math.sqrt(radius*2/g)*disc.Ctau.el[0].z*omegaspin +disc.Ctau.el[0].x*omegavp));
		
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
	   
	   uearth.set(0, 0, -1);
	   uearth.scale(disc.mass*g);

		fo.add(ulift);
		fo.add(udrag);
		fo.add(uearth);
		
		Vector3d mlat = new Vector3d(ulat);
		double pitch = Adp*radius*2* (CM + disc.Ctau.el[1].y*omegalat);
		mlat.scale(pitch);
		
		Vector3d mspin = new Vector3d(0,0,-1);
		double spin = disc.Ctau.el[2].z*(omegaspin);
		mspin.scale(spin);
		
		T_c_N.transpose();
		T_c_N.transform(mvp);
		T_c_N.transform(mlat);
		T_c_N.transform(mspin);
		
		Vector3d m = new Vector3d(mvp);
		m.add(mlat);
		m.add(mspin);
		
		Vector3d accel = new Vector3d(fo);
		accel.scale(1/disc.mass);
		
		omegaD.x = (m.x + Id*thd*fd*st - Ia*thd*(fd*st+gd) +Id*thd*fd*st)/Id/ct +0.55e-16;
		omegaD.y = (m.y + Ia*fd*ct*(fd*st +gd) - Id*fd*fd*ct*st)/Id;  
		omegaD.z = (m.z - Ia*(omegaD.x*st + thd*fd*ct))/Ia;
		
		double[] ans = new double[]{v.x,v.y,v.z, -omega.x,-omega.y,-omega.z, accel.x, accel.y,accel.z, -omegaD.x,-omegaD.y,-omegaD.z};
		return ans;
	}

}
