package ultimate;

import sim.util.Double3D;

// Object doesn't change during simulation and therefore doesnot implement "stepable".
// this objects are lines on the ground, cones, trees...
public class FixedEntity {

	Double3D position;
	Double3D top;
	
	// Transform3d 
	
	public FixedEntity(Double3D position, Double3D top) 
	{
		this.position = position;
		this.top = top;
	}

	public Double3D getPosition() {
		return position;
	}

	public void setPosition(Double3D position) {
		this.position = position;
	}

	public Double3D getTop() {
		return top;
	}

	public void setTop(Double3D top) {
		this.top = top;
	}
	
}
