package ultimate.fixedEntity;

import sim.util.Double3D;
import ultimate.FixedEntity;

public class Cone extends FixedEntity {

	public Cone(Double3D position, Double3D top) {
		super(position, top);
		// TODO Auto-generated constructor stub
	}

	public Cone(Double3D position) {
		this(position, new Double3D(position.x,position.y,0.3));
		// TODO Auto-generated constructor stub
	}
}
