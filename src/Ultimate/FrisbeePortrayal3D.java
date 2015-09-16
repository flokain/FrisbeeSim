package Ultimate;

import java.awt.Color;
import java.awt.Image;

import javax.media.j3d.Appearance;
import javax.media.j3d.Group;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;

import sim.portrayal3d.simple.PrimitivePortrayal3D;

public class FrisbeePortrayal3D extends PrimitivePortrayal3D 
{

	/** Constructs a FrisbeePortrayal3D with a default (flat opaque white) appearance and a scale of 1.0. */
    public FrisbeePortrayal3D()
        {
        this(1f);
        }
        
    /** Constructs a FrisbeePortrayal3D with a default (flat opaque white) appearance and the given scale. */
    public FrisbeePortrayal3D(double scale)
        {
        this(Color.white,scale);
        }
        
    /** Constructs a FrisbeePortrayal3D with a flat opaque appearance of the given color and a scale of 1.0. */
    public FrisbeePortrayal3D(Color color)
        {
        this(color,1f);
        }
        
    /** Constructs a FrisbeePortrayal3D with a flat opaque appearance of the given color and the given scale. */
    public FrisbeePortrayal3D(Color color, double scale)
        {
        this(appearanceForColor(color),true,false,scale);
        }

    /** Constructs a FrisbeePortrayal3D with the given (opaque) image and a scale of 1.0. */
    public FrisbeePortrayal3D(Image image)
        {
        this(image,1f);
        }

    /** Constructs a FrisbeePortrayal3D with the given (opaque) image and scale. */
    public FrisbeePortrayal3D(Image image, double scale)
        {
        this(appearanceForImage(image,true),false,true,scale);
        }


    /** Constructs a FrisbeePortrayal3D with the given appearance and scale, plus whether or not to generate normals or texture coordinates.  Without texture coordiantes, a texture will not be displayed. */
    public FrisbeePortrayal3D(Appearance appearance, boolean generateNormals, boolean generateTextureCoordinates, double scale)
    {
//    	//this.appearance = appearance;
//	    setScale(null, scale);
//	    this.setPickable(true);       
//	    TransformGroup prev = new TransformGroup();
//	   	prev.setCapability(Group.ALLOW_CHILDREN_READ);
//	   	prev.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
//		try
//		{	
//	    	Scene s = null;
//	       	ObjectFile f = new ObjectFile ();
//	        f.setFlags (ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY);
//	    	String s1 = "3dmodels\\frisbee.obj";
//	    	s = f.load (s1);
//	    	prev.addChild(s.getSceneGroup());
//	    	
//		}
//	    catch (java.io.FileNotFoundException ex){
//	    }
//	    group = prev;
    }
    
	public TransformGroup getModel(Object object, TransformGroup prev)
	{
		Frisbee frisbee = (Frisbee)object;
		if (prev == null) // scene graph is being created
		{
			
			prev = new TransformGroup();
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
		}
//		   	Matrix3d rot = new Matrix3d();
//		   	rot.m00 = 1;
//		   	rot.m21 = 1;
//		   	rot.m12 = 1;
//		   	rot.mul(frisbee.rotation);
//		   	
		   	prev.setTransform(new Transform3D(frisbee.rotation, frisbee.location,1));
		return prev;
	}
    protected int numShapes() { return 1; }
    
}

