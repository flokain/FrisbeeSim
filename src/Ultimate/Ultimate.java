package Ultimate;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;

import com.sun.accessibility.internal.resources.accessibility;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.continuous.Continuous3D;
import sim.portrayal.continuous.Continuous3DPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Double3D;
import sim.util.MutableDouble3D;

public class Ultimate extends SimState{
	public Continuous3D ultimateField3D;
	double fieldWidth = 37.0; // size of an ultimate field in meters
	double fieldLength = 100.0; 
	double endzoneLength = 16.0;
	double stepTime = 0.0001; // simulated time that elapses between 2 steps (all forces are calculated in m/s^2)
	
	Bag positionsOffence;
	Bag positionsDefence;
	ArrayList<PlayerOffence> offence;
	ArrayList<PlayerDefence> defence;
	public Frisbee frisbee;
	public Double3D positionDisc;

	public Ultimate(long seed) throws IOException
	{		
		super(seed); 	
		
		positionsOffence = new Bag();
		positionsDefence = new Bag();
		try {
			//frisbee = new Frisbee();
			frisbee = new Ball();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 7; i++)
			positionsOffence.add(new Double3D((endzoneLength+(fieldLength-endzoneLength)*(i)/7 ), fieldWidth/2,0));
		//		for (int i = 0; i < 7; i++)
		//			positionsOffence.add(new Double2D(random.nextDouble()*fieldLength,random.nextDouble()*fieldWidth));
		//
		for (int i = 0; i < 7; i++)
			positionsDefence.add(new Double3D((endzoneLength+(fieldLength-endzoneLength)*(i)/7 ), fieldWidth/2+2,0));
		positionDisc = new Double3D( ((Double3D)positionsOffence.get(0)).add(new Double3D(1,1,1)));
		frisbee.position.setTo(positionDisc);
		
		//init field;
		ultimateField3D = new Continuous3D(0.001, fieldLength, fieldWidth, 40);
		ArrayList<FieldObject> lines = new ArrayList<FieldObject>(2);
		lines.add(new FieldObject(endzoneLength,0,0,fieldWidth));
		lines.add(new FieldObject(fieldLength - endzoneLength,0,0,fieldWidth));
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
	
	public void stop()
	{
		
	}

	@Override
	public void start()
	{
		super.start();
		positionsOffence = new Bag();
		positionsDefence = new Bag();
		for (int i = 0; i < 7; i++)
			positionsOffence.add(new Double3D((endzoneLength+(fieldLength-endzoneLength)*(i)/7 ), fieldWidth/2,0));
		//		for (int i = 0; i < 7; i++)
		//			positionsOffence.add(new Double2D(random.nextDouble()*fieldLength,random.nextDouble()*fieldWidth));
		//
		for (int i = 0; i < 7; i++)
			positionsDefence.add(new Double3D((endzoneLength+(fieldLength-endzoneLength)*(i)/7 ), fieldWidth/2+2,0));
		positionDisc = new Double3D( ((Double3D)positionsOffence.get(0)).add(new Double3D(1,1,1)));
		frisbee.position.setTo(positionDisc);
		
		//init field;
		Continuous3DPortrayal2D ultimateField2D = new Continuous3DPortrayal2D(){{setField(ultimateField3D);}};
		ultimateField3D = new Continuous3D(0.001, fieldLength, fieldWidth, 40);
		ArrayList<FieldObject> lines = new ArrayList<FieldObject>(2);
		lines.add(new FieldObject(endzoneLength,0,0,fieldWidth));
		lines.add(new FieldObject(fieldLength - endzoneLength,0,0,fieldWidth));
		
//		for ( int i = 0; i <lines.size();i++)
//		{
//			ultimateField2D.setObjectLocation(lines.get(i), lines.get(i).posi);
//		}
			//init players
			offence = new ArrayList<PlayerOffence>(7);
			defence = new ArrayList<PlayerDefence>(7);

			for ( int i = 0; i <positionsOffence.size(); i++)
			{
				offence.add(new PlayerOffence((Double3D)positionsOffence.get(i)));
				ultimateField3D.setObjectLocation(offence.get(i),(Double3D)positionsOffence.get(i));
				schedule.scheduleRepeating(offence.get(i));
			}
			for ( int i = 0; i <positionsDefence.size(); i++)
			{
				defence.add(new PlayerDefence((Double3D) positionsDefence.get(i)));
				ultimateField3D.setObjectLocation(defence.get(i),(Double3D)positionsDefence.get(i));
				schedule.scheduleRepeating(defence.get(i));
			}
			//init Frisbee
			schedule.scheduleRepeating(frisbee);
			//frisbee.location.add(new Double3D(1,1,1));
			//frisbee.location.set(0,0,1);
//			Double3D velocity = new Double3D(13.42,-.41,0.001);
//			Double3D angles = new Double3D(-0.07,0.21,5.03);
//			Double3D angleVelocity = new Double3D(-14.94,-1.48,54.25);
			// Hummel f2302
			//Double3D position = new Double3D(-9.03E-01, -6.33E-01, -9.13E-01);
			Double3D orientation = new Double3D(-7.11E-02, 2.11E-01, 5.03E+00); //rad
			Double3D velocity = new Double3D(1.34E+01,  -4.11E-01, 1.12E-03);
			Double3D omega = new Double3D(-1.49E+01, -1.48E+00, 5.43E+01);		
			
//			Double3D velocity = new Double3D(50,0,0);
//			Double3D orientation = new Double3D(0,0.08,0.00);//rad
//			Double3D omega = new Double3D(0,0,50);

//			frisbee.position.setTo(position);
			//frisbee.throwDisc(velocity, orientation, omega);
			ultimateField3D.setObjectLocation(frisbee, frisbee.getPosition());
	}
	private class UltimateField extends Continuous3D
	{
		Double3D[] field;
		Double3D[] endzone1;
		Double3D[] endzone2;
		double border;
		Bag markers; 			//(Double3D) markers used in sport to visualize running paths
		
		public UltimateField(double discretization, double width,
				double height, double length, double endzoneDepth, double border, Bag markers) 
		{
			super(discretization, width + 2* border, height , length + 2*border);
			
			
			
			field = new Double3D[]{ };
		}
		
		
	}	
	

	//setter and getter of this model
	public MutableDouble3D getDiscPosition() { return frisbee.position;}
	public void setDiscPosition(Double3D pos) {frisbee.position.setTo(pos);}
	
	public MutableDouble3D getDiscOrientation() { return frisbee.orientation;}
	public void setDiscOrientation(Double3D orientation) {frisbee.orientation.setTo(orientation);}
	
	public MutableDouble3D getDiscVelocity() { return frisbee.velocity;}
	public void setDiscVelocity(Double3D velocity) {frisbee.velocity.setTo(velocity);}
	
	public MutableDouble3D getDiscOmega() { return frisbee.omega;}
	public void setDiscOmega(Double3D omega) {frisbee.omega.setTo(omega);}
	
	public MutableDouble3D getDiscAcceleration() { return frisbee.acceleration;}
	public void setDiscAcceleration(Double3D acceleration) {frisbee.acceleration.setTo(acceleration);}

	public MutableDouble3D getDiscAlpha() { return frisbee.alpha;}
	public void setDiscAlpha(Double3D alpha) {frisbee.alpha.setTo(alpha);}	
	
	public double getStepTime() {
		return stepTime;
	}
	
	public static void main(String[] args)
	{
		doLoop(Ultimate.class, args);
		System.exit(0);
	}   
}