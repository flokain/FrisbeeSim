package discPhysics;

import java.io.IOException;

import sim.util.Double3D;
import sim.util.MutableDouble3D;

public class FrisbeeDeKain {

	private final Matrix inertiaTensor;
	private final double mass;
	private final double radius;
	private final double g;  		//The acceleration of gravity (m/s^2).
	private double RHO; 	//The density of air in kg/m^3.
	private final double AREA;	//The area of a standard frisbee.
	private final double GAMMA0;	// minimum drag and zero lift at ALPHA0 Hummel page 9
	private final double[] cl;
	private final double[] cd;
	private final Matrix Ctau;
	private final double[] ctau;
	
	/* initialize Disc with Ultrastar data from Hummel. */
	public FrisbeeDeKain(){
		this(0);
	}

	public FrisbeeDeKain(int set){
	
		mass =  0.175;
		g = 9.7935; 	
	    RHO = 1.23;	
		AREA = 0.057;
		radius = Math.sqrt(AREA/Math.PI);
		
		double CL0 = 0.3331;	                 //The lift coefficient at alpha = 0.       
		double CLA = 1.9124;                     //The lift coefficient dependent on alpha. 
		cl = new double[]{CL0,CLA};            
		
		double CD0 = 0.1769;                     //The drag coefficent at alpha = 0.       
		double CDA = 0.685;	                     //The drag coefficient dependent on alpha
		cd = new double[]{CD0,0,CDA};
		GAMMA0 = -CL0/CLA;                       // minimum drag and zero lift at GAMMA0 Hummel page 9  
		
		double CRr = 0.00171;
		double CMO = -0.0821;
		double CMa = 0.4338;
		
		double CMq; 
		double CRp;
		double CNr;
		
		if(set == 0) // short flight Hummel
		{		
			CMq = -0.0144; 
		 	CRp = -0.0125;
		 	CNr = -0.0000341;
		}
		
		if(set == 1) // long flight hummel
		{
			CMq = -0.005;
			CRp = -0.0055;
			CNr = -0.0000071;
		}
		else // short flight is standard
		{
			CMq = -0.005;
			CRp = -0.0055;
			CNr = -0.0000071;
		}	
		
		Ctau = new Matrix(CRp, 0.,   CRr,
						  0.,   CMq, 0.,  
						  0.,   0.,   CNr);
		ctau = new double[]{CMO,CMa};
		
		double Ixy = 0.001219;
		double Iz = 0.002352;
		inertiaTensor = new Matrix(Ixy,  0,   0,
									0,	 Ixy, 0,  
									0,   0,   Iz);
	}
	
	public DeValueContainer calculate(Double3D r, Matrix R, Double3D l, Double3D p) throws IOException//describes the differendial equation  f(t,y) = y'
	{
		Double3D v; 		// 		v = r'
		Matrix omegaR;		// omegaR = R'
		Double3D f;         // 		f = p'
		Double3D tau;		// 	  tau = l'
		
		R = R.normalize();
		if(!R.isRotational() ) throw new IOException("R has to be a rotation Matrix");
		
		double gamma, air; 			// Help-variables, angle of attack, and airresistance
		Double3D omega, lift, drag; // Help-variables, angle of attack, and airresistance
		
		v = p.multiply(1/mass); 							// p = m*v -> v = p/m		
				
		omega = R											// l = R*I*RT*omega
			    .mul(inertiaTensor.InvertDiag()				// -> R*I^1*RT(l) = 
			    .mul(R.transpose()							// =  R*I^1*RT*R*I*RT*omega = omega;
			    .mul(l)));
		omegaR = R.cross(omega);    						// omegaR = [ omega x R1,: , omega x R2,: ,omega x R3,:]
		
		gamma = angle(R.getCol(0),R.getCol(1), v); 			//angle of attack
		air = RHO*AREA*v.lengthSq()/2; 						// Airresistance rho*A*v^2
		
		lift = new Double3D( cross( 						// lift direction direction is orthogonal to v and e2 = v x R3  
				               cross(v,R.getCol(2)),		// orthognal e2 = v x R3 
				                  	 v)						// and v 
				             .resize(poly(gamma,cl))  		// length of lift ist a polynom in gamma
							 .multiply(air));				// *air
		
		drag = new Double3D(v.negate()						// drag direction is negative v direction 
		                    .resize(poly(gamma-GAMMA0,cd))	// length of drag is a polynom in gamma-GAMMA0
		 					.multiply(air));				// *air
		 					
		f =    lift                							// f =   lift
			   .add(drag)   								//     + drag 
			   .add(new Double3D(0,0, -mass*g));			//	   + gravity
			 
		tau = Ctau.mul(omega)								// tau = [  Ctau * omega
		      .add(new Double3D(0, poly(gamma,ctau), 0)) 	//        + ptau(gamma)  ]
			  .multiply(air);								//       * RHO*AREA*v^2
		
		v = p.multiply(1/mass); 							// p = m*v -> v = p/m
		
		return new DeValueContainer(v,omegaR,f,tau);
	}
	public DeValueContainer calculate(DeValueContainer cont) throws IOException
	{
		return calculate(cont.r, cont.R, cont.l, cont.p);
	}
	public double[] calculate(double t, double[] y) throws IOException {
		return calculate(new DeValueContainer(y)).convert2Array();
	}
	
	public class DeValueContainer
	{
		public Double3D r;
		public Matrix R;
		public Double3D l;
		public Double3D p;
		
		public DeValueContainer(Double3D r, Matrix R, Double3D l, Double3D p)
		{
			this.r = new Double3D(r);
			this.R = new Matrix(R);
			this.l = new Double3D(l);
			this.p = new Double3D(p);
		}
		public DeValueContainer()
		{
			this.r = new Double3D();
			this.R = new Matrix();
			this.l = new Double3D();
			this.p = new Double3D();
		}
		public DeValueContainer(double[] y)
		{
			r = new Double3D(y[0],y[1],y[2]);
			R = new Matrix(y[3],y[4],y[5],y[6],y[7],y[8],y[9],y[10],y[11]); 		// angular velocity around x,y,z axis of the frisbee Frame (roll,pitch,spin/yaw)
			l = new Double3D(y[12],y[13],y[14]);
			p = new Double3D(y[15],y[16],y[17]);
		}
		public double[] convert2Array()
		{
			return new double[]{r.x,r.y,r.z,
					            R.el[0].x, R.el[0].y, R.el[0].z, R.el[1].x, R.el[1].y, R.el[1].z, R.el[2].x, R.el[2].y, R.el[2].z,
					            l.x, l.y, l.z,
					            p.x, p.y, p.z};
		}

	}
	
	class Matrix
	{
		Double3D[] el =new Double3D[3];
		
		public Matrix(Matrix m)
		{ 
			for(int i= 0; i<3;i++) el[i] = new Double3D(m.el[i]);	
		}
		
		public Matrix()
		{ 
			for(int i= 0; i<3;i++) el[i] = new Double3D();	
		}
		public Matrix(Double3D a, Double3D b, Double3D c)
		{ 
			el[0] = a;
			el[1] = b;
			el[2] = c;
		}
		public Matrix(double y0,double y1, double y2,double y3,double y4,double y5,double y6,double y7,double y8) //zeilen weise
		{
			el[0] = new Double3D(y0,y1,y2);
			el[1] = new Double3D(y3,y4,y5);
			el[2] = new Double3D(y6,y7,y8);
		}
		
		public Matrix(double [] y) //zeilen weise
		{
			for(int i= 0; i<3;i++) el[i] = new Double3D(y[i],y[i+3],y[i+6]);
		}
		
		public Matrix setRow(int i, double[] y)
		{	
			setRow(i, new Double3D(y[0],y[1],y[2]));;
			return this;
		}
		public Matrix setRow(int i, Double3D y)
		{	
			el[i] = new Double3D(y);
			return this;
		}
		public Double3D getCol(int j) 
		{
			if (j == 0)	return new Double3D(el[0].x,el[1].x,el[2].x);
			if (j == 1)	return new Double3D(el[0].y,el[1].y,el[2].y);
			if (j == 2)	return new Double3D(el[0].z,el[1].z,el[2].z);
			return null;
		}
		public double[] convert2Array()
		{
			return new double[]{el[0].x, el[0].y, el[0].z, el[1].x, el[1].y, el[1].z, el[2].x, el[2].y, el[2].z};
		}
		
		public Matrix setIdentity()
		{
			el[0] = new Double3D(1,0,0);
			el[1] = new Double3D(0,1,0);
			el[2] = new Double3D(0,0,1);
			
			return this;
		}
		
		public Double3D mul(Double3D y)
		{
			MutableDouble3D re = new MutableDouble3D();
		
			re.x = el[0].dot(y);
			re.y = el[1].dot(y);
			re.z = el[2].dot(y);
			return new Double3D(re);
		}
		
		public Matrix mul(Matrix m)
		{
			MutableDouble3D re = new MutableDouble3D();
			Matrix ans = new Matrix();
			for(int i= 0; i<3;i++)
			{
				ans.el[i] = this.mul(m.getCol(i));
			}
			return ans.transpose();
		}
		
		public Matrix rotX(double ang)
		{
			double c = Math.cos(ang);
			double s = Math.sin(ang);
			return this.mul(new Matrix( 1, 0, 0,
										0, c, -s,
										0, s, c ) );
		}
		public Matrix rotY(double ang)
		{
			double c = Math.cos(ang);
			double s = Math.sin(ang);
			return this.mul(new Matrix( c,  0, s,
										0,  1, 0,
										-s, 0, c ) );
		}
		public Matrix rotZ(double ang)
		{
			double c = Math.cos(ang);
			double s = Math.sin(ang);
			return this.mul(new Matrix( c,  -s, 0,
										s,  c,  0,
										0,  0,  1 ) );
		}
		
		public Matrix transpose()
		{
			Matrix ans = new Matrix();
			for(int i= 0; i<3;i++) ans.el[i]=this.getCol(i);
			return ans;
		}
		public Matrix InvertDiag()
		{
			Matrix tmp = new Matrix();
			
			tmp.el[0] = new Double3D (1/el[0].x, 0,0);
			tmp.el[1] = new Double3D (0, 1/el[1].y ,0);
			tmp.el[2] = new Double3D (0, 0, 1/el[2].z);
			
			return tmp;
		}
		public Matrix cross(Double3D b)
		{
			return new Matrix( FrisbeeDeKain.cross(b,getCol(0)), 
							   FrisbeeDeKain.cross(b,getCol(1)), 
							   FrisbeeDeKain.cross(b,getCol(2)));
		}
		public double det()
		{
			return  el[0].x*(el[1].y * el[2].z - el[1].z*el[2].y) -
					el[0].y*(el[1].x * el[2].z - el[1].z * el[2].x) +
					el[0].z*(el[1].x * el[2].y - el[1].y*el[2].x);
		}
		public Matrix normalize()
		{
			return new Matrix( getCol(0).normalize(),
							   getCol(1).normalize(),
							   getCol(2).normalize())
					   .transpose();
		}
		public boolean isRotational()
		{
			double tmp = Math.abs(det()-1);
			return true;//(tmp < 1e-14);
		}
	}
	static Double3D cross(Double3D a, Double3D b)
	{
		MutableDouble3D tmp = new MutableDouble3D();
		tmp.x = a.y*b.z - a.z*b.y;
		tmp.y = a.z*b.x - a.x*b.z;
		tmp.z = a.x*b.y - a.y*b.x;
		return new Double3D(tmp);
	}

	private Double3D project(Double3D s0, Double3D s1, Double3D x)
	{
		return project(cross(s0,s1),x); 
	}
	private Double3D project(Double3D n, Double3D x)
	{
		n = n.normalize();
		return x.subtract(n.multiply(x.dot(n))); // p_n(x) = x-n*(x.n)  
	}
	
	private double angle(Double3D s0, Double3D s1, Double3D x)
	{
			Double3D n = cross(s0,s1);
			double sin = n.dot(x)/ Math.sqrt(( n.dot(n) * x.dot(x)));
			return Math.asin(sin);
	}
	
	private double poly(double x, double[] y)
	{
		double tmp = 0;
		for (int i = 0; i<y.length ; i++) 
			tmp = tmp + y[i]* Math.pow(x,(i));
		return tmp;
	}
	public DeValueContainer convertHummelToKainIvp( double[] y)
	{
		Double3D r;
		Matrix R;
		Double3D l;
		Double3D p;
		
		r = new Double3D(y[0],y[1], y[2]);	//r -> r									
		p = new Double3D(y[3], y[4], y[5])	// v -> p = v*m
				.multiply(mass);
		R = new Matrix().setIdentity()	//an -> R 
				.rotX(y[6])
				.rotY(-y[7])
				.rotZ(-y[8])
				.transpose();
		l = R								// omega -> l = R*I*RT(omega)
			.mul(inertiaTensor)
			.mul(R.transpose())
			.mul(new Double3D(y[9],y[10],y[11]));
		
		return new DeValueContainer(r,R,l,p);

	}
}


