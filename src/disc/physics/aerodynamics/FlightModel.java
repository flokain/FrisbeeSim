package disc.physics.aerodynamics;

import sim.util.Double3D;

public interface FlightModel {

	DeValueContainer calculate( Double3D position, Double3D orientation, Double3D velocity, Double3D omega);
	
	public class DeValueContainer
	{
		public Double3D r;
		public Double3D orientation;
		public Double3D v;
		public Double3D omega;
		
		public DeValueContainer(Double3D r, Double3D orientation, Double3D v, Double3D omega)
		{
			this.r = new Double3D(r);
			this.orientation = new Double3D(orientation);
			this.v = new Double3D(v);
			this.omega = new Double3D(omega);
		}
		public DeValueContainer()
		{
			this.r = new Double3D();
			this.orientation = new Double3D();
			this.v = new Double3D();
			this.omega = new Double3D();
		}
		public DeValueContainer(double[] y)
		{
			r = new Double3D(y[0],y[1],y[2]);
			orientation = new Double3D(y[3],y[4],y[5]); 		// angular velocity around x,y,z axis of the frisbee Frame (roll,pitch,spin/yaw)
			v = new Double3D(y[6],y[7],y[8]);
			omega = new Double3D(y[9],y[10],y[11]);
		}
		public double[] convert2Array()
		{
			return new double[]{r.x,r.y,r.z,
					            orientation.x, orientation.y, orientation.z,
					            v.x, v.y, v.z,
					            omega.x, omega.y, omega.z};
		}

	}	
}
