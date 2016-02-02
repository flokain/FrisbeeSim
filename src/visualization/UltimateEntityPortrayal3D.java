package visualization;

import java.awt.Color;
import java.awt.Image;

import javax.media.j3d.Appearance;
import javax.media.j3d.Group;
import javax.media.j3d.TransformGroup;

import sim.portrayal3d.simple.PrimitivePortrayal3D;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;

public class UltimateEntityPortrayal3D extends PrimitivePortrayal3D 
{

	/** Constructs a UltimateEntityPortrayal3D with a default (flat opaque white) appearance and a scale of 1.0. */
    public UltimateEntityPortrayal3D()
        {
        this(1f);
        }
        
    /** Constructs a UltimateEntityPortrayal3D with a default (flat opaque white) appearance and the given scale. */
    public UltimateEntityPortrayal3D(double scale)
        {
        this(Color.white,scale);
        }
        
    /** Constructs a UltimateEntityPortrayal3D with a flat opaque appearance of the given color and a scale of 1.0. */
    public UltimateEntityPortrayal3D(Color color)
        {
        this(color,1f);
        }
        
    /** Constructs a UltimateEntityPortrayal3D with a flat opaque appearance of the given color and the given scale. */
    public UltimateEntityPortrayal3D(Color color, double scale)
        {
        this(appearanceForColor(color),true,false,scale);
        }

    /** Constructs a UltimateEntityPortrayal3D with the given (opaque) image and a scale of 1.0. */
    public UltimateEntityPortrayal3D(Image image)
        {
        this(image,1f);
        }

    /** Constructs a UltimateEntityPortrayal3D with the given (opaque) image and scale. */
    public UltimateEntityPortrayal3D(Image image, double scale)
        {
        this(appearanceForImage(image,true),false,true,scale);
        }


    /** Constructs a UltimateEntityPortrayal3D with the given appearance and scale, plus whether or not to generate normals or texture coordinates.  Without texture coordiantes, a texture will not be displayed. */
    public UltimateEntityPortrayal3D(Appearance appearance, boolean generateNormals, boolean generateTextureCoordinates, double scale)
    {
    	//this.appearance = appearance;
	    setScale(null, scale);
	    this.setPickable(true);       
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


