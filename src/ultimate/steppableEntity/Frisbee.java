package ultimate.steppableEntity;

import java.io.IOException;

import disc.physics.aerodynamics.FlightModel;
import disc.physics.aerodynamics.FlightModel.DeValueContainer;
import disc.physics.aerodynamics.FlightModel_HummelNew;
import disc.physics.aerodynamics.FlyingDisc;
import disc.physics.aerodynamics.FlyingDisc.flightCoefficientsType;
import sim.engine.Steppable;
import sim.util.Double3D;
import ultimate.Ultimate;
import ultimate.UltimateEntity;
import ultimate.UltimateEntity.AccelerationsContainer;

public class Frisbee extends UltimateEntity implements Steppable
{
	//override arrowsFlag
	
	private FlightModel flightModel;
	protected boolean isFlying = false;

	Frisbee(Double3D position, Double3D orientation, Double3D velocity,Double3D omega, double mass, double radius, FlightModel flightModel) throws IOException
	{
		super(position,orientation,velocity,omega,mass,radius);
		FlyingDisc physicalModelofDisc = new FlyingDisc(flightCoefficientsType.HUMMEL_SHORT);
		this.flightModel = flightModel;
		arrowsFlag = true;
	}
	
	Frisbee(Double3D position, Double3D orientation, Double3D velocity,Double3D omega, double mass, double radius) throws IOException
	{
		super(position,orientation,velocity,omega,mass,radius);
		FlyingDisc physicalModelofDisc = new FlyingDisc(flightCoefficientsType.HUMMEL_SHORT);
		flightModel = new FlightModel_HummelNew(physicalModelofDisc);
		arrowsFlag = true;
	}
	public Frisbee(Double3D position) throws IOException
	{
		super(position, new Double3D(0,0,0),new Double3D(0,0,0),new Double3D(0,0,0),0.175,0.135);
		FlyingDisc physicalModelofDisc = new FlyingDisc(flightCoefficientsType.HUMMEL_SHORT);
		flightModel = new FlightModel_HummelNew(physicalModelofDisc);
		arrowsFlag = true;
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

	public FlightModel getFlightModel() {
		return flightModel;
	}

	public void setFlightModel(FlightModel flightModel) {
		this.flightModel = flightModel;
	}
	
}
