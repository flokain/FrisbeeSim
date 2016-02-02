package disc.physics.aerodynamics;

import java.io.IOException;

import disc.physics.Disc;
import disc.physics.aerodynamics.FlyingDisc.flightCoefficientsType;
import sim.util.Double3D;
import sim.util.MutableDouble3D;

public class FlightModel_Kain {

	private final FlyingDisc disc;
	private final double g;  	//The acceleration of gravity (m/s^2).
	private double RHO; 		//The density of air in kg/m^3.
	
	/* Parameters for the calculation
	 * they only need to be calculated once and are intended to save computation time and
	 * to make the code more readable.
	 */
	Matrix inverseInertia;
	
	/* initialize Disc with Ultrastar data from Hummel. */
	public FlightModel_Kain() throws IOException
	{
		this(new FlyingDisc(flightCoefficientsType.HUMMEL_SHORT));
	}

	public FlightModel_Kain(FlyingDisc disc){
	
	    RHO = 1.23;	
		g = 9.7935;
		this.disc = disc;
		inverseInertia = new Matrix(1/disc.inertia_XY, 1/disc.inertia_XY, 1/disc.inertia_Z);
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
		
		v = p.multiply(1/disc.mass); 									// p = m*v -> v = p/m		
		omega = R														// l = R*I*RT*omega
			    .mul(inverseInertia)									// -> R*I^1*RT(l) = 
			    .mul(R.transpose()										// =  R*I^1*RT*R*I*RT*omega = omega;
			    .mul(l));
		omegaR = R.cross(omega);    									// omegaR = [ omega x R1,: , omega x R2,: ,omega x R3,:]
		
		gamma = angle(R.getCol(0),R.getCol(1), v); 						//angle of attack
		air = RHO*disc.area*v.lengthSq()/2; 							// Airresistance rho*A*v^2
		
		if ( gamma == 0)
			lift = new Double3D(0,0,0);
		else
		{
			lift = new Double3D( cross( 								// lift direction direction is orthogonal to v and e2 = v x R3  
					               cross(v,R.getCol(2)),				// orthognal e2 = v x R3 
					                  	 v)								// and v 
					             .resize(poly(gamma,disc.cl))  			// length of lift ist a polynom in gamma
								 .multiply(air));						// *air
		}
		
		if ( gamma -disc.gamma0 == 0)
			drag = new Double3D(0,0,0);
		
		else
		{
			drag = new Double3D(v.negate()								// drag direction is negative v direction 
		                    .resize(poly(gamma-disc.gamma0,disc.cd))	// length of drag is a polynom in gamma-GAMMA0
		 					.multiply(air));							// *air
		}					
		f =    lift                							            // f =   lift
			   .add(drag)   								            //     + drag 
			   .add(new Double3D(0,0, -disc.mass*g));			        //	   + gravity
			                                                            
		tau = disc.Ctau.mul(omega)								        // tau = [  Ctau * omega
		      .add(new Double3D(0, poly(gamma,disc.ctau), 0)) 	        //        + ptau(gamma)  ]
			  .multiply(air);								            //       * RHO*AREA*v^2
		                                                                
		v = p.multiply(1/disc.mass); 							        // p = m*v -> v = p/m
		
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
	
	static Double3D cross(Double3D a, Double3D b)
	{
		MutableDouble3D tmp = new MutableDouble3D();
		tmp.x = a.y*b.z - a.z*b.y;
		tmp.y = a.z*b.x - a.x*b.z;
		tmp.z = a.x*b.y - a.y*b.x;
		return new Double3D(tmp);
	}

	@SuppressWarnings("unused")
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
				.multiply(disc.mass);
		R = new Matrix().setIdentity()	//an -> R 
				.rotY(-y[7])
				.rotX(-y[6])
				.rotZ(-y[8])
				.transpose();
		
		l = R								// omega -> l = R*I*RT(omega)
			.mul(inverseInertia)
			.mul(R.transpose())
			.mul(new Double3D(y[9],y[10],y[11]));
		
		return new DeValueContainer(r,R,l,p);

	}

	
}


