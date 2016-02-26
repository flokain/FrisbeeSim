package visualization;

import java.awt.Color;
import java.awt.Image;
import java.io.FileNotFoundException;
import java.util.Enumeration;










import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.Group;
import javax.media.j3d.Leaf;
import javax.media.j3d.Material;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import sim.portrayal.LocationWrapper;
import sim.portrayal3d.SimplePortrayal3D;
import sim.portrayal3d.simple.Arrow;
import sim.portrayal3d.simple.BranchGroupPortrayal3D;
import sim.portrayal3d.simple.PrimitivePortrayal3D;
import sim.util.Double3D;
import ultimate.UltimateEntity;
import ultimate.steppableEntity.Frisbee;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.javafx.sg.prism.NGNode;

public class FrisbeePortrayal3D extends UltimateEntityPortrayal3D
{
    public FrisbeePortrayal3D()
    {
    	super("3dmodels\\frisbee.obj",new Transform3D()
    	{
    		{
    			rotX(Math.PI);
    		}
    	});
    }
    public TransformGroup getModel(Object object, TransformGroup prev)
	{
    	UltimateEntity entity = (UltimateEntity)object;
    	
    	if (prev == null) entity.setArrowsVisible(true);
    	return super.getModel(object, prev);
	}
}

