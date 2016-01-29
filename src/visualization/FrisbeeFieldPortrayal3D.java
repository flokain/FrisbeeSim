package visualization;

import java.awt.Color;

import sim.field.grid.DoubleGrid2D;
import sim.portrayal3d.grid.ValueGrid2DPortrayal3D;
import sim.portrayal3d.grid.quad.TilePortrayal;
import sim.util.gui.SimpleColorMap;

public class FrisbeeFieldPortrayal3D extends ValueGrid2DPortrayal3D {

	public FrisbeeFieldPortrayal3D()
	{
		this("images\\artificial-grass-field-top-view-texture.jpg");
	}
	FrisbeeFieldPortrayal3D(String imagePath)
	{
		super();
		setField(new DoubleGrid2D(100,37));
		setTransparency(0.8f);
		SimpleColorMap map = new SimpleColorMap(0.0,1.0, new Color(50,140,50), new Color(50,140,50));
        setPortrayalForAll(new TilePortrayal(map));     
        // Change the Z projection to display an image instead.  :-)
        //setImage((new ImageIcon(imagePath)).getImage());
	}
}
