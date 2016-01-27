package Ultimate;
import java.awt.Color;
import java.util.ArrayList;

import javax.vecmath.Vector3d;

import sim.engine.SimState;
import sim.field.continuous.*;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Double3D;
import sim.util.MutableDouble2D;
import sim.engine.*;

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

	public Ultimate(long seed)
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
		
		String setup = "vertical";
		switch (setup) {

		case "vertical":
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
			Frisbee frisbee = new Frisbee(positionDisc);
			schedule.scheduleRepeating(frisbee);
			//frisbee.location.add(new Vector3d(1,1,1));
			//frisbee.location.set(0,0,1);
//			Vector3d velocity = new Vector3d(13.42,-.41,0.001);
//			Vector3d angles = new Vector3d(-0.07,0.21,5.03);
//			Vector3d angleVelocity = new Vector3d(-14.94,-1.48,54.25);
			
			Vector3d velocity = new Vector3d(50,0,0);
			Vector3d angles = new Vector3d(0,0.08,0.00);//rad
			Vector3d angleVelocity = new Vector3d(0,0,50);

			frisbee.throwDisc(velocity, angles, angleVelocity);
			ultimateField2D.setObjectLocation(frisbee, new Double2D(frisbee.location.x,frisbee.location.y));
			break;
//			
			case "throw":
////			Double2D posiP = new Double2D((endzoneLength+(fieldLength-endzoneLength)/7 ), fieldWidth/2);
////			ultimateField2D.setObjectLocation(new PlayerOffence(posiP),posiP);
//			Vector3d posiF = new Vector3d((endzoneLength+(fieldLength-endzoneLength)/7), fieldWidth/3,0);
//			Vector3d speedF = new Vector3d(14,0,0);
//			double angleOfAttack = 2;
//			
//			//frisbee = new Frisbee(posiF,speedF,angleOfAttack);
//			ultimateField2D.setObjectLocation(frisbee,new Double2D(posiF.x,posiF.y));
//			schedule.scheduleRepeating(frisbee);
		}


		//		for ( int i = 0; i < positionsDefence.size();i++)
		//			ultimateField2D.setObjectLocation(new PlayerDefence(), (Double2D)(positionsDefence.get(i)));
	}

	public static void main(String[] args)
	{
		doLoop(Ultimate.class, args);
		System.exit(0);
	}   
}