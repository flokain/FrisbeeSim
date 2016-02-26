package visualization;

import java.awt.Color;
import java.awt.Graphics2D;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.Double2D;

public class FieldObject extends RectanglePortrayal2D{

	private static final long serialVersionUID = 2371402856632016025L;
	public Double2D posi;
	public Double2D size;

	public FieldObject(Double2D posi, Double2D size) 
	{
		this.posi = posi;
		this.size = size;
	}
	
	public FieldObject(double x, double y, double w, double h) 
	{
		this.posi = new Double2D(x,y);
		this.size = new Double2D(w,h);
	}

	@Override
	public void draw(Object object,  final Graphics2D g, final DrawInfo2D info )
    {    
    // draw our line as well
    
    final double width = info.draw.width * size.x * 2;
    final double height = info.draw.height * size.y * 2;
        
    g.setColor(Color.white);
    g.drawLine((int)info.draw.x,
        (int)info.draw.y,
        (int)(info.draw.x) + (int)(width),
        (int)(info.draw.y) + (int)(height));
    }
}