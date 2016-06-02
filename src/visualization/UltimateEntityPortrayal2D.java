package visualization;

import java.awt.Color;
import java.awt.Graphics2D;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.CircledPortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import ultimate.UltimateEntity;

public class UltimateEntityPortrayal2D extends MovablePortrayal2D{
	
	//draws selectable and moveable circles with radius fill color, border color (when selected) 
	public UltimateEntityPortrayal2D(final Color colorFill, Color colorSelected)
	{
		super(
				new CircledPortrayal2D(
				  	new OvalPortrayal2D() 
				  	{
					  @Override
					  public void draw(Object object, Graphics2D graphics,DrawInfo2D info) 
					  {
						  UltimateEntity ent = (UltimateEntity)object;
						  scale = ent.getRadius()*3;
						  paint = colorFill;
						  super.draw(object, graphics, info);
					  }
				  	}
				  ,0,5.0,colorSelected, true));
	}
}
