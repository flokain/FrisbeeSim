package Ultimate;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import sim.engine.Steppable;
import sim.util.Double3D;
import sim.util.MutableDouble2D;

public abstract class Player extends UltimateEntity implements Steppable {

	double energy; 	// energy left in the object that makes it possible to accelarte itself
	double legPower; 	// maximum amount of energy applied per second to run. (max accelaration)
	double armPower;	// maximum amount of energy applied per second to throw. (max accelaration)
	public MutableDouble2D tempVector = new MutableDouble2D();
	
	public Player( Double3D posi)
	{
		super(posi,0.5);
	}
	
	@Override
	public AccelerationsContainer calcAccelerations(Ultimate ultimate)
	{	
		Frisbee disc = ultimate.frisbee;
		double stepTime = ultimate.getStepTime();
		
		if(disc.isFlying())
		{
			moveTo(disc);
		}
		
		return new AccelerationsContainer(new Double3D(acceleration),new Double3D(alpha));
	}
	
	protected void moveTo(UltimateEntity en)
	{
		Double3D direction = en.getPosition().subtract(this.getPosition());
		//setAcceleration(direction.normalize(),stepTime); //stupid
		setAcceleration(getContactPointDirection(en).normalize());
		setAlpha();	
	}
	
	public void setAcceleration(Double3D direction)
	{
		//acceleration  biophysiological models can be realized with transferfunction, siehe ergometermodel bio medizinische regelungsmathe
		//those result in  a function that solves x'= 1/(T)*(-x + K*b(t)) T is called timeconstant and Amplifier K and b inputfunction.
		//In our case b is a uniform direction vector, T is approximative half the time to reach 95% v_max which is K.
		Double3D dir = direction;
		double T = 1; //time that would pass until reach of velocity_max if there was no drag by -x.
		double K = 10; //v_max
		Double3D velocity = getVelocity();
		super.setAcceleration(velocity.negate().add(dir.multiply(K)).multiply(1./T)); // x'= 1/(T)*(-x + K*b(t))
	}
	
	public void setAlpha()
	{
		//assume that orientation always wants to point the local (1,0,0) in to the direction of accel.
		// it means that the player wants to look at the direction he is running. 
		// mach einen koordinaten wechsel bezüglich orientation für acceleration auf a.
		// berechne die angestrebte lokale Drehachse  o1 x a. sie hat die länge sin(alpha)
		// die stärkste rotation soll stattfinden wenn der winkel zwischen o1, a pi beträgt.
		// deshalb berechnen wir die länge der achse als (-cos(winkel)+1)/2 = (- o1 . a + 1)/2
		// diese ist bei winkel = 0 ebenfalls null und bei pi maximal und gleich 1.
		
		Vector3d a = new Vector3d(velocity.x,velocity.y,velocity.z);
		if(a.length() == 0) return; // there is no velocity, nothing to do therefore
		a.normalize();
		
		Vector3d o1 = new Vector3d();
		
		Matrix3d realToBodyTrans = getOrientationAsMatrix();
		getOrientationAsMatrix().getColumn(0, o1);
		realToBodyTrans.transpose();
		
		realToBodyTrans.transform(a);

		// berechne die angestrebte lokale Drehachse  o1 x a. sie hat die länge sin(alpha)
		// das ist aber nicht praktisch weil wir dadurch nur den spitzen winkel der aufgespannten
		//geraden bekommen. Für Abhilfe sorgt dann der nächste schritt.
		
		Vector3d axis = new Vector3d();
		axis.cross(o1,a);
		axis.normalize();
		
		// die stärkste rotation soll stattfinden wenn der winkel zwischen o1 und a pi ist.
		// deshalb berechnen wir die länge der achse als (-cos(winkel)+1)/2 = (- o1 . a + 1)/2
		// diese ist bei winkel = 0 ebenfalls null und bei pi maximal und gleich 1.
		// double scale = (-o1.dot(a) +1)/2
		Vector3d o2 = new Vector3d();
		getOrientationAsMatrix().getColumn(1, o2);
		// also we want to look upstraight. this means as secondary constraint
		// we want the local y axis o2 to be planar in world coordinates (z=0) therefore we rotate arount the o1
		
		Double3D ax = new Double3D(axis.x,axis.y,axis.z);
		double T = 0.01; //time that would pass until reach of velocity_max if there was no drag by -x.
		double K = 10; //v_max
		Double3D omega = getOmega();
		super.setAlpha(omega.negate().add(ax.multiply(K)).multiply(1./T)); // x'= 1/(T)*(-x + K*b(t))
		super.setAlpha(ax.multiply(K));
	}
	
	
	protected Double3D getContactPointDirection(UltimateEntity en)
	{
		Double3D distanceVec = en.getPosition().subtract(this.getPosition());
		double timeUntilIntersection = en.getVelocity().dot(distanceVec) / en.getVelocity().length();
		// if the time has allready past this means the disc flies away from the player.
		// he will then take the positive time and predict to intercept it at this point.
		timeUntilIntersection = Math.abs(timeUntilIntersection);
		
		return en.getPosition().add(distanceVec.multiply(timeUntilIntersection)).subtract(getPosition());
	}

}