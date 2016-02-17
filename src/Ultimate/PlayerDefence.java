package Ultimate;

import sim.util.Double2D;
import sim.util.Double3D;

public class PlayerDefence extends Player
{
	public PlayerDefence( Double3D posi)
	{
		super(posi);
	}
	protected void moveTo(UltimateEntity en)
	{
		Double3D direction = en.getPosition().subtract(this.getPosition()).normalize();
		setAcceleration(direction); //stupid
	}
}
