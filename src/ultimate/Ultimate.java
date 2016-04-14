package ultimate;
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
import ultimate.fixedEntity.Cone;
import ultimate.fixedEntity.Line;
import ultimate.steppableEntity.Frisbee;
import ultimate.steppableEntity.Player;
import ultimate.steppableEntity.PlayerDefence;
import ultimate.steppableEntity.PlayerOffence;
import visualization.FieldObject;

public class Ultimate extends SimState{
	public Continuous3D ultimateField3D;
	double fieldWidth = 37.0; // size of an ultimate field in meters
	double fieldLength = 100.0; 
	double endzoneLength = 16.0;
	double stepTime = 0.001; // simulated time that elapses between 2 steps (all forces are calculated in m/s^2)
	
	Bag playerOffence;
	Bag playerDefence;
	Bag cones;
	Bag obstacles;
	Bag Frisbees;
	
	public enum ExperimentSetup 
	{
		Flight, Hummel, VerticalStack, Default, ReadExperiment;
	}
	
	public Frisbee frisbee;

	public Ultimate(long seed) throws IOException
	{		
		super(seed); 	
		
		try {
			frisbee = new Frisbee(new Double3D(endzoneLength+1,fieldWidth/2+1,1));
			//frisbee = new Ball();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		playerOffence = new Bag();
		playerDefence = new Bag();
		cones = new Bag();
		obstacles = new Bag();
		Frisbees = new Bag();
		ultimateField3D = new Continuous3D(0.001, fieldLength, fieldWidth, 40);
	}
	public void stop()
	{
	}

	

	public void initialize(ExperimentSetup setup){
			
		playerOffence.clear();
		playerDefence.clear();
		cones.clear();
		obstacles.clear();
		Frisbees.clear();
		ultimateField3D.clear();
		switch (setup)
		{
		case VerticalStack:
		{
			//init field;
			ultimateField3D = new Continuous3D(0.001, fieldLength, fieldWidth, 40);
			Bag lines = new Bag();
			lines.add(new Line(new Double3D(endzoneLength,0,0),new Double3D(endzoneLength,fieldWidth,0))); //endzone left
			lines.add(new Line(new Double3D(fieldLength - endzoneLength,0,0),new Double3D(fieldLength - endzoneLength,fieldWidth,0))); //endzone right
			lines.add(new Line(new Double3D(0,0,0),new Double3D(0,fieldWidth,0)));
			lines.add(new Line(new Double3D(fieldLength,0,0),new Double3D(fieldLength,fieldWidth,0)));
			lines.add(new Line(new Double3D(0,0,0),new Double3D(fieldLength,0,0)));
			lines.add(new Line(new Double3D(0,fieldWidth,0),new Double3D(fieldLength,fieldWidth,0)));
			
			for ( int i = 0; i <lines.size();i++)
			{
				Line line = ((Line)lines.get(i));
				ultimateField3D.setObjectLocation(line, line.getPosition().add(line.getTop()).multiply(0.5));
			}

			cones.add(new Cone(new Double3D(0,0,0))); 									//endzone 1 right back 
			cones.add(new Cone(new Double3D(0,fieldWidth,0)));  						//endzone 1 left back 
			cones.add(new Cone(new Double3D(endzoneLength,0,0)));						//endzone 1 right front
			cones.add(new Cone(new Double3D(endzoneLength,fieldWidth,0)));				//endzone 1 left front
			cones.add(new Cone(new Double3D(fieldLength,fieldWidth,0))); 				//endzone 2 right back 
			cones.add(new Cone(new Double3D(fieldLength,0,0)));  						//endzone 2 left back 
			cones.add(new Cone(new Double3D(fieldLength-endzoneLength,fieldWidth,0)));	//endzone 2 right front
			cones.add(new Cone(new Double3D(fieldLength-endzoneLength,0,0)));			//endzone 2 left front
			
			for ( int i = 0; i <cones.size();i++)
			{
				Cone cone = ((Cone)cones.get(i));
				ultimateField3D.setObjectLocation(cone, cone.getPosition().add(cone.getTop()).multiply(0.5));
			}
			
			//create objects, attach them to field, and to schedule
			for (int i = 0; i < 7; i++)
			{
				PlayerOffence pO = new PlayerOffence( new Double3D((endzoneLength+(fieldLength-endzoneLength)*(i)/7 ), fieldWidth/2,0));
				PlayerDefence pD = new PlayerDefence( new Double3D((endzoneLength+(fieldLength-endzoneLength)*(i)/7 ), fieldWidth/2+2,0));
				playerOffence.add(pO);
				playerDefence.add(pD);
				ultimateField3D.setObjectLocation(pO,pO.getPosition());
				schedule.scheduleRepeating(pO);
				ultimateField3D.setObjectLocation(pD,pD.getPosition());
				schedule.scheduleRepeating(pD);
			}
			frisbee.position.setTo( ((Player) playerOffence.get(0)).getPosition().add(new Double3D(1,1,1)));
			ultimateField3D.setObjectLocation(frisbee, frisbee.getPosition());
			schedule.scheduleRepeating(frisbee);
			break;
		}
		case Hummel:
		{
			try {
				frisbee = new Frisbee(new Double3D(endzoneLength+1,fieldWidth/2+1,1));
			} catch (IOException e) {
				e.printStackTrace();
			}
			ultimateField3D.setObjectLocation(frisbee, frisbee.getPosition());
			schedule.scheduleRepeating(frisbee);
			break;
		}
		case ReadExperiment:
		{
			//init field;
			ultimateField3D = new Continuous3D(0.001, fieldLength, fieldWidth, 40);
			Bag lines = new Bag();
			lines.add(new Line(new Double3D(endzoneLength,0,0),new Double3D(endzoneLength,fieldWidth,0))); //endzone left
			lines.add(new Line(new Double3D(fieldLength - endzoneLength,0,0),new Double3D(fieldLength - endzoneLength,fieldWidth,0))); //endzone right
			lines.add(new Line(new Double3D(0,0,0),new Double3D(0,fieldWidth,0)));
			lines.add(new Line(new Double3D(fieldLength,0,0),new Double3D(fieldLength,fieldWidth,0)));
			lines.add(new Line(new Double3D(0,0,0),new Double3D(fieldLength,0,0)));
			lines.add(new Line(new Double3D(0,fieldWidth,0),new Double3D(fieldLength,fieldWidth,0)));
			
			for ( int i = 0; i <lines.size();i++)
			{
				Line line = ((Line)lines.get(i));
				ultimateField3D.setObjectLocation(line, line.getPosition().add(line.getTop()).multiply(0.5));
			}

			cones.add(new Cone(new Double3D(0,0,0))); 									//endzone 1 right back 
			cones.add(new Cone(new Double3D(0,fieldWidth,0)));  						//endzone 1 left back 
			cones.add(new Cone(new Double3D(endzoneLength,0,0)));						//endzone 1 right front
			cones.add(new Cone(new Double3D(endzoneLength,fieldWidth,0)));				//endzone 1 left front
			cones.add(new Cone(new Double3D(fieldLength,fieldWidth,0))); 				//endzone 2 right back 
			cones.add(new Cone(new Double3D(fieldLength,0,0)));  						//endzone 2 left back 
			cones.add(new Cone(new Double3D(fieldLength-endzoneLength,fieldWidth,0)));	//endzone 2 right front
			cones.add(new Cone(new Double3D(fieldLength-endzoneLength,0,0)));			//endzone 2 left front
			
			for ( int i = 0; i <cones.size();i++)
			{
				Cone cone = ((Cone)cones.get(i));
				ultimateField3D.setObjectLocation(cone, cone.getPosition().add(cone.getTop()).multiply(0.5));
			}
			
			//create objects, attach them to field, and to schedule
			for (int i = 0; i < 1; i++)
			{
				PlayerOffence pO = new PlayerOffence( new Double3D((endzoneLength+(fieldLength-endzoneLength)/7 ), fieldWidth/2,0));
				PlayerDefence pD = new PlayerDefence( new Double3D((endzoneLength+(fieldLength-endzoneLength)/7 ), fieldWidth/2,0));
				playerOffence.add(pO);
				playerDefence.add(pD);
				ultimateField3D.setObjectLocation(pO,pO.getPosition());
				schedule.scheduleRepeating(pO);
				ultimateField3D.setObjectLocation(pD,pD.getPosition());
				schedule.scheduleRepeating(pD);
			}
			frisbee.position.setTo( new Double3D(endzoneLength,fieldWidth/2,0).add(new Double3D(1,1,1)));
			ultimateField3D.setObjectLocation(frisbee, frisbee.getPosition());
			schedule.scheduleRepeating(frisbee);
			break;
		}
		}
	}
	
	@Override
	
	public void start()
	{
		super.start();
		//initialize(ExpermintSetup.Default);
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