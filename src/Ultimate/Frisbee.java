package Ultimate;

import java.io.IOException;

import com.sun.prism.paint.Color;

import disc.physics.aerodynamics.FlightModel_HummelNew;
import disc.physics.aerodynamics.FlyingDisc;
import disc.physics.aerodynamics.FlightModel_HummelNew.DeValueContainer;
import disc.physics.aerodynamics.FlyingDisc.flightCoefficientsType;
import sim.engine.Steppable;
import sim.util.Double3D;

public class Frisbee extends UltimateEntity implements Steppable
{

	private static final long serialVersionUID = -277584315287176445L;
	
	private FlightModel_HummelNew flightModel;

	Frisbee(Double3D position, Double3D orientation, Double3D velocity,Double3D omega, double mass, double radius) throws IOException
	{
		super(position,orientation,velocity,omega,mass,radius);
		FlyingDisc physicalModelofDisc = new FlyingDisc(flightCoefficientsType.HUMMEL_SHORT);
		flightModel = new FlightModel_HummelNew(physicalModelofDisc);
	}
	Frisbee(Double3D position) throws IOException
	{
		super(position, new Double3D(0,0,0),new Double3D(0,0,0),new Double3D(0,0,0),0.175,0.135);
		FlyingDisc physicalModelofDisc = new FlyingDisc(flightCoefficientsType.HUMMEL_SHORT);
		flightModel = new FlightModel_HummelNew(physicalModelofDisc);
	}
	
	public Frisbee() throws IOException {
		this(new Double3D());
	}
	@Override
	public AccelerationsContainer calcAccelerations(Ultimate ultimate)
	{	
		DeValueContainer derivativeOf = flightModel.calculate( new Double3D(position),new Double3D(orientation),new Double3D(velocity),new Double3D(omega));
		return new AccelerationsContainer(derivativeOf.v, derivativeOf.omega);
	}
	
	public void throwDisc(Double3D velocity, Double3D orientation, Double3D omega)
	{
		this.velocity.setTo(velocity);
		this.orientation.setTo(orientation);
		this.omega.setTo(omega);
		// TODO Auto-generated method stub
		
	}
}
