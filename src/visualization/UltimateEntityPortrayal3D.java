package visualization;

import java.awt.Image;

import javax.media.j3d.Appearance;
import javax.media.j3d.Group;
import javax.media.j3d.TransformGroup;

import sim.portrayal3d.simple.PrimitivePortrayal3D;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;

public class UltimateEntityPortrayal3D extends PrimitivePortrayal3D 
{

    /** Constructs a UltimateEntityPortrayal3D with the given appearance and scale, plus whether or not to generate normals or texture coordinates.  Without texture coordiantes, a texture will not be displayed. */
    public UltimateEntityPortrayal3D(Appearance appearance, boolean generateNormals, boolean generateTextureCoordinates, double scale)
    {
    	//this.appearance = appearance;
	    setScale(null, scale);
	    setPickable(true);       
	    TransformGroup prev = new TransformGroup();
	   	prev.setCapability(Group.ALLOW_CHILDREN_READ);
	   	prev.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		try
		{	
	    	Scene s = null;
	       	ObjectFile f = new ObjectFile ();
	        f.setFlags (ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY);
	    	String s1 = "3dmodels\\frisbee.obj";
	    	s = f.load (s1);
	    
	    	prev.addChild(s.getSceneGroup());
	    	
		}
	    catch (java.io.FileNotFoundException ex){
	    }
	    group = prev;
    }
    
    @Override
	protected int numShapes() { return 1; }
    
}


