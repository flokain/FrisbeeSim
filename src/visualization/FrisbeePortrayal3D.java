package visualization;

import java.awt.Color;
import java.awt.Image;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import Ultimate.Frisbee;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;

import sim.app.woims3d.Vector3D;
import sim.portrayal.LocationWrapper;
import sim.portrayal3d.SimplePortrayal3D;
import sim.portrayal3d.simple.Arrow;
import sim.portrayal3d.simple.CubePortrayal3D;
import sim.portrayal3d.simple.PrimitivePortrayal3D;
import sim.util.Double3D;

public class FrisbeePortrayal3D extends SimplePortrayal3D
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
    	//this.appearance = appearance;
	    //setScale(null, scale);       
	    //TransformGroup prev = new TransformGroup();
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
    
	public TransformGroup getModel(Object object, TransformGroup j3dModel)
	{
		Frisbee frisbee = (Frisbee)object;

		if (j3dModel == null) // scene graph is being created
		{
			j3dModel = new TransformGroup();
		   	j3dModel.setCapability(Group.ALLOW_CHILDREN_READ);
		   	j3dModel.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			
			try
			{	
				//tgFrisbee = loadModel(frisbee);
				j3dModel.addChild(loadModel(frisbee));
				//tgPhysics = createPhysicsArrows(frisbee);
				//j3dModel.addChild(tgPhysics);
				//j3dModel.addChild(createAxes());
			}
			catch (java.io.FileNotFoundException ex){}
			//CubePortrayal3D e34 = new CubePortrayal3D();
			//j3dModel.addChild(e34.getModel(object, null).cloneTree());
		}
		TransformGroup tgFrisbee = new TransformGroup();
		tgFrisbee.setCapability(Group.ALLOW_CHILDREN_READ);
		tgFrisbee.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tgFrisbee = (TransformGroup) j3dModel.getChild(0);
		//tgPhysics = createPhysicsArrows(frisbee);
		tgFrisbee.setTransform(new Transform3D(frisbee.getRotation(),new Vector3d(0,0,0),1));
		j3dModel.setTransform(new Transform3D(new Matrix3d(1,0,0,0,1,0,0,0,1),frisbee.getLocation(),1));
		return j3dModel;
	}
	
	TransformGroup loadModel(Frisbee frisbee) throws java.io.FileNotFoundException
	{
			TransformGroup tg = new TransformGroup();
			tg.setCapability(Group.ALLOW_CHILDREN_READ);
			tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			Scene s = null;
		   	ObjectFile f = new ObjectFile ();
		    f.setFlags(ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY);
			String s1 = "3dmodels\\frisbee.obj";
			s = f.load(s1);
			tg.addChild(s.getSceneGroup());
			return tg;
	}
	BranchGroup createAxes()
    {
    BranchGroup temp = new BranchGroup();
    temp.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
    double arrowRadius = 0.01;
    temp.addChild(new Arrow(arrowRadius, new Double3D(0, 0, 0), new Double3D(1,0,0),"O"  , "X", null));
    temp.addChild(new Arrow(arrowRadius, new Double3D(0, 0, 0), new Double3D(0,1,0), null, "Y", null));
    temp.addChild(new Arrow(arrowRadius, new Double3D(0, 0, 0), new Double3D(0,0,1), null, "Z", null));
    return temp;
    }
	BranchGroup createPhysicsArrows(Frisbee frisbee)
	{
		BranchGroup temp = new BranchGroup();
	    temp.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
	    double arrowRadius = 0.01;
	    temp.addChild(new Arrow(arrowRadius, new Double3D(0, 0, 0), vToD3d(frisbee.getVelocity()), null  , "vel", null));
	    temp.addChild(new Arrow(arrowRadius, new Double3D(0, 0, 0), vToD3d(frisbee.getAccelaration()), null, "accel", null));
	    return temp;
    }
	
	Double3D vToD3d(Vector3d v)
	{
		return new Double3D(v.x,v.y,v.z);
	}
}

