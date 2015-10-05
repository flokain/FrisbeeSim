package visualization;

import java.awt.Color;
import java.awt.Image;

import javax.media.j3d.Appearance;
import javax.media.j3d.Group;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;

import sim.portrayal3d.grid.quad.TilePortrayal;
import sim.portrayal3d.simple.PrimitivePortrayal3D;
import sim.util.gui.ColorMap;

public class TrajectoryPortrayal3D extends TilePortrayal 
{

	public TrajectoryPortrayal3D(ColorMap colorDispenser) {
		super(colorDispenser);
		// TODO Auto-generated constructor stub
	}

}
