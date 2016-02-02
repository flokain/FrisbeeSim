package disc.physics;

import java.io.IOException;

// This class contains all the physical characteristics of a frisbee.
// As an example the Ultrastar has been implemented.


public class Disc 
{
	public enum discType 
	{
		ULTRASTAR;
	}
	public double inertia_XY;
	public double inertia_Z;
	public double mass;
	public double area;		// The area approximate by a circle.
	
	public Disc(discType discType) throws IOException
	{
		switch (discType)
		{
		case ULTRASTAR:
		{
			mass =  0.175;
			area = 0.057;		
			inertia_XY= 0.001219;
			inertia_Z = 0.002352;
			break;
		}
		default:
			throw new IOException("not a predefined disc");
		}
	}
}
