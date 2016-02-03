package visualization;

import java.awt.Color;
import java.awt.Image;
import java.util.Enumeration;


import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.Group;
import javax.media.j3d.Leaf;
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
import sim.util.Double3D;
import Ultimate.Frisbee;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.javafx.sg.prism.NGNode;

public class FrisbeePortrayal3D extends SimplePortrayal3D
{
	BranchGroup bgTop;
		TransformGroup tgFrisbee;
			BranchGroup bgFrisbeeModel;
		BranchGroup bgPhysics;
			Arrow velocityArrow;
			Arrow omegaArrow;
			Arrow accelerationArrow;
			Arrow alphaArrow;
			


	/** Constructs a FrisbeePortrayal3D with a default (flat opaque white) appearance and a scale of 1.0. */
    public FrisbeePortrayal3D()
        {
	    	super();
    		bgTop = new BranchGroup();
    		bgTop.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    			tgFrisbee = new TransformGroup();
    			tgFrisbee.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
    			tgFrisbee.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    			 	bgFrisbeeModel = new BranchGroup();
    			 	bgFrisbeeModel.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
				bgPhysics = new BranchGroup();
				//bgPhysics.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
				
			bgTop.addChild(tgFrisbee);
				tgFrisbee.addChild(bgFrisbeeModel);
			bgTop.addChild(bgPhysics);
				bgPhysics.addChild(velocityArrow);
				bgPhysics.addChild(omegaArrow);
				bgPhysics.addChild(accelerationArrow);
				bgPhysics.addChild(alphaArrow);
        }
    
	@Override
	public TransformGroup getModel(Object object, TransformGroup prev)
	{
		Frisbee frisbee = (Frisbee)object;

		if (prev == null)
		{
			try
			{	
				tgFrisbee = loadModel(frisbee);
			}
			catch (java.io.FileNotFoundException ex){}
			prev = new TransformGroup();
			prev.setCapability(Group.ALLOW_CHILDREN_READ);
			prev.addChild(bgTop);
			bgFrisbeeModel.addChild(new ColorCube(0.01));
			
			//set facets of the disc pickable.
			LocationWrapper wrapper = new LocationWrapper(object, null, getCurrentFieldPortrayal());
			bgFrisbeeModel.setUserData(wrapper);
			setPickable(bgFrisbeeModel);
			
		}
		
		Matrix3d rot1 = new Matrix3d();
		rot1.rotX(-frisbee.getOrientation().x);
		Matrix3d rot2 = new Matrix3d();
		rot2.rotY(-frisbee.getOrientation().y);
		
		Matrix3d orientationAsMatrix = new Matrix3d();
		orientationAsMatrix.setIdentity();
		orientationAsMatrix.mul(rot2);
		orientationAsMatrix.mul(rot1);
		orientationAsMatrix.transpose();
		
		//Vector3d locationAsVector = new Vector3d(frisbee.getPosition().x,frisbee.getPosition().y,frisbee.getPosition().z);
		
		tgFrisbee.setTransform(new Transform3D(orientationAsMatrix, new Vector3d(),1));
		velocityArrow = new Arrow( frisbee.getVelocity(), "Velocity", Color.YELLOW);
		omegaArrow = new Arrow( frisbee.getOmega(), "Omega", Color.YELLOW);
		accelerationArrow = new Arrow( frisbee.getAcceleration(), "Acceleration", Color.BLUE);
		alphaArrow = new Arrow( frisbee.getAlpha(), "Alpha", Color.BLUE);
		
		return prev;
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
    temp.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    return temp;
    }
	BranchGroup createPhysicsArrows(Frisbee frisbee,BranchGroup bg)
	{
		BranchGroup temp = new BranchGroup();
	    temp.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	    temp.setCapability(BranchGroup.ALLOW_DETACH);
	    bg.addChild(temp);
	    return bg;
    }
	
	Double3D vToD3d(Vector3d v,double length)
	{
		Double3D re= new Double3D(v.x,v.y,v.z);
		return re.resize(length);
		
	}
	private class Arrow extends sim.portrayal3d.simple.Arrow
	{
		public Arrow(Double3D top, String label,final Color color)
		{
			super(0.3, new Double3D(), top, null, label, new Appearance()
			{
				{
					setColoringAttributes(new ColoringAttributes(new Color3f(color),ColoringAttributes.SHADE_GOURAUD));
				}
				{
					
				}
			});
		}
		
	}
	public void setPickable(Node node)
	{
		if (node instanceof Group)
		{
			for (Enumeration<Node> childNodes = ((Group)node).getAllChildren(); childNodes.hasMoreElements();)
			{
				setPickable(childNodes.nextElement());
			}
		}
		else if (node instanceof Shape3D)
		{
			setPickableFlags((Shape3D)node);
		}
	}
}

