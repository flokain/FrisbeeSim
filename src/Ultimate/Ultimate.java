package Ultimate;
import java.io.IOException;
import java.util.ArrayList;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.continuous.Continuous3D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Double3D;
import sim.util.MutableDouble3D;

public class Ultimate extends SimState{
	Continuous2D ultimateField2D;
	public Continuous3D ultimateField3D;
	double fieldWidth = 37.0; // size of an ultimate field in meters
	double fieldLength = 100.0; 
	double endzoneLength = 16.0;
	double stepTime = 0.0001; // simulated time that elapses between 2 steps (all forces are calculated in m/s^2)
	Bag positionsOffence;
	Bag positionsDefence;
	Frisbee frisbee;
	Double3D positionDisc;

	public Ultimate(long seed) throws IOException
	{		
		super(seed); 			
		positionsOffence = new Bag();
		positionsDefence = new Bag();
		
		for (int i = 0; i < 7; i++)
			positionsOffence.add(new Double2D((endzoneLength+(fieldLength-endzoneLength)*(i)/7 ), fieldWidth/2));
		//		for (int i = 0; i < 7; i++)
		//			positionsOffence.add(new Double2D(random.nextDouble()*fieldLength,random.nextDouble()*fieldWidth));
		//
		for (int i = 0; i < 7; i++)
			positionsDefence.add(new Double2D((endzoneLength+(fieldLength-endzoneLength)*(i)/7 ), fieldWidth/2+2));
		
		positionDisc = new Double3D( ((Double2D)positionsOffence.get(0)).x+1,((Double2D)positionsOffence.get(0)).y+1,1);
		frisbee = new Frisbee(positionDisc);
	}

	public Ultimate(long seed, Bag posiOffence)
	{
		super(seed);
		positionsOffence = posiOffence;
	}

	public Ultimate(long seed, Bag posiOffence, Bag posiDefence)
	{
		super(seed);
		positionsOffence = posiOffence;
		positionsDefence = posiDefence;
	}

	@Override
	public void start()
	{
		super.start();
		
		//init field;
		ultimateField2D = new Continuous2D(0.1, fieldLength,fieldWidth);
		ultimateField3D = new Continuous3D(0.001, fieldLength, fieldWidth, 40);
		ArrayList<FieldObject> lines = new ArrayList<FieldObject>(2);
		lines.add(new FieldObject(endzoneLength,0,0,fieldWidth));
		lines.add(new FieldObject(fieldLength - endzoneLength,0,0,fieldWidth));
		
		for ( int i = 0; i <lines.size();i++)
		{
			ultimateField2D.setObjectLocation(lines.get(i), lines.get(i).posi);
		}
			//init players
			ArrayList<PlayerOffence> offence = new ArrayList<PlayerOffence>(7);
			ArrayList<PlayerDefence> defence = new ArrayList<PlayerDefence>(7);

			for ( int i = 0; i <positionsOffence.size(); i++)
			{
				offence.add(new PlayerOffence((Double2D) positionsOffence.get(i)));
				ultimateField2D.setObjectLocation(offence.get(i),(Double2D)positionsOffence.get(i));
				schedule.scheduleRepeating(offence.get(i));
			}
			for ( int i = 0; i <positionsDefence.size(); i++)
			{
				defence.add(new PlayerDefence((Double2D) positionsDefence.get(i)));
				ultimateField2D.setObjectLocation(defence.get(i),(Double2D)positionsDefence.get(i));
				schedule.scheduleRepeating(defence.get(i));
			}
			//init Frisbee
			schedule.scheduleRepeating(frisbee);
			//frisbee.location.add(new Double3D(1,1,1));
			//frisbee.location.set(0,0,1);
//			Double3D velocity = new Double3D(13.42,-.41,0.001);
//			Double3D angles = new Double3D(-0.07,0.21,5.03);
//			Double3D angleVelocity = new Double3D(-14.94,-1.48,54.25);
			
			Double3D velocity = new Double3D(50,0,0);
			Double3D orientation = new Double3D(0,0.08,0.00);//rad
			Double3D omega = new Double3D(0,0,50);

			frisbee.throwDisc(velocity, orientation, omega);
			ultimateField2D.setObjectLocation(frisbee, new Double2D(frisbee.position.x,frisbee.position.y));
	}

	//setter and getter of this model
	public MutableDouble3D getDiscPosition() { return frisbee.position;}
	public void setDiscPosition(Double3D pos) {frisbee.position.setTo(pos);}
	
	public MutableDouble3D MutableDouble3D() { return frisbee.velocity;}
	public void setDiscVelocity(Double3D velocity) {frisbee.velocity.setTo(velocity);}
	
	public MutableDouble3D getDiscOrientation() { return frisbee.orientation;}
	public void setDiscOrientation(Double3D angles) {frisbee.orientation.setTo(angles);}
	
	public MutableDouble3D getDiscOmega() { return frisbee.omega;}
	public void setDiscOmega(Double3D omega) {frisbee.omega.setTo(omega);}
	
	public static void main(String[] args)
	{
		doLoop(Ultimate.class, args);
		System.exit(0);
	}   
}