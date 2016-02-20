package ultimate.steppableEntity;

import java.io.IOException;

import disc.physics.aerodynamics.FlightModel_HummelNew;
import disc.physics.aerodynamics.FlyingDisc;
import disc.physics.aerodynamics.FlightModel_HummelNew.DeValueContainer;
import disc.physics.aerodynamics.FlyingDisc.flightCoefficientsType;
import sim.engine.Steppable;
import sim.util.Double3D;
import ultimate.Ultimate;
import ultimate.UltimateEntity;
import ultimate.UltimateEntity.AccelerationsContainer;

@SuppressWarnings("serial")
public class Ball extends Frisbee implements Steppable
{
	public Ball() throws IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public AccelerationsContainer calcAccelerations(Ultimate ultimate)
	{	
		if (position.z - radius <= 0)  //touching the ground
		{
			velocity.multiplyIn(0.8); // reflect and take some away due to friction. 
			velocity.z = Math.abs(velocity.z);
			omega.multiplyIn(-0.99);
			
		}
		acceleration.setTo(getVelocity().negate().resize(velocity.lengthSq()).multiply(Math.pow(radius,2)*Math.PI * 0.5).subtract(new Double3D(0,0,10))); // air friction on a ball
		alpha.setTo(getOmega().negate().multiply(0.8));
		return new AccelerationsContainer(new Double3D(acceleration),new Double3D(alpha));
	}
	
	public void getThrown(Double3D velocity, Double3D orientation, Double3D omega)
	{
		this.isFlying = true;
		this.velocity.setTo(velocity);
		this.orientation.setTo(orientation);
		this.omega.setTo(omega);
		// TODO Auto-generated method stub
		
	}
	
	public boolean isFlying()
	{
		return isFlying;
	}
}
