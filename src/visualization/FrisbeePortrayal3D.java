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
import Ultimate.Frisbee;
import Ultimate.UltimateEntity;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.javafx.sg.prism.NGNode;

public class FrisbeePortrayal3D extends PrimitivePortrayal3D
{
	TransformGroup tgTop;
		TransformGroup tgFrisbee;
			BranchGroup bgFrisbeeModel;
		BranchGroup bgPhysics;
			BranchGroup bgArrows;
				Arrow velocityArrow;
				Arrow omegaArrow;
				Arrow accelerationArrow;
				Arrow alphaArrow;

	/** Constructs a FrisbeePortrayal3D with a default (flat opaque white) appearance and a scale of 1.0. */
    public FrisbeePortrayal3D()
        {
    		tgTop = new TransformGroup();
    		tgTop.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
    			tgFrisbee = new TransformGroup();
    			tgFrisbee.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    				bgFrisbeeModel = loadModel();
				bgPhysics = new BranchGroup();
				bgPhysics.setCapability(BranchGroup.ALLOW_DETACH);
				bgPhysics.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
				bgPhysics.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
					bgArrows = new BranchGroup();
			
			tgTop.addChild(tgFrisbee);
				tgFrisbee.addChild(bgFrisbeeModel);
			tgTop.addChild(bgPhysics);
				bgPhysics.addChild(bgArrows);
			
			group = tgTop;	
        }
    
	@Override
	public TransformGroup getModel(Object object, TransformGroup prev)
	{
		Frisbee frisbee = (Frisbee)object;
		
		if (prev == null)
		{
			prev = new TransformGroup();
			prev.setCapability(Group.ALLOW_CHILDREN_READ);
			prev.addChild(tgTop);
			//set facets of the disc pickable.
			setPickable(frisbee, bgFrisbeeModel);
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
		
		tgFrisbee.setTransform(new Transform3D(orientationAsMatrix, new Vector3d(),1));
		
		bgArrows = new BranchGroup();
		
		velocityArrow = new Arrow (frisbee.getVelocity(),"Velocity",Color.YELLOW);
		omegaArrow = new Arrow( frisbee.getOmega(), "Omega", Color.YELLOW);
		accelerationArrow = new Arrow( frisbee.getAcceleration(), "Acceleration", Color.BLUE);
		alphaArrow = new Arrow( frisbee.getAlpha(), "Alpha", Color.BLUE);
		
		bgArrows.addChild(velocityArrow);
		bgArrows.addChild(omegaArrow);
		bgArrows.addChild(accelerationArrow);
		bgArrows.addChild(alphaArrow);
		
		bgPhysics.setChild(bgArrows,0);
		return prev;
	}
	
	private BranchGroup loadModel()
	{
			Scene s = null;
		   	ObjectFile f = new ObjectFile ();
		    f.setFlags(ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY);
			String s1 = "3dmodels\\frisbee.obj";
			try {
				s = f.load(s1);
			} catch (FileNotFoundException | IncorrectFormatException
					| ParsingErrorException e) {
				e.printStackTrace();
			}
			return s.getSceneGroup();
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
			super(0.05, new Double3D(0,0,0), top, null, label, new Appearance()
			{
				{
					setColoringAttributes(new ColoringAttributes(new Color3f(color),ColoringAttributes.SHADE_GOURAUD));
				}
			});
				
		}
		
		
		public void setHeadRelative(Double3D pa) {
			// TODO Auto-generated method stub
			Vector3d p = new Vector3d(pa.x,pa.y,pa.z);
			Matrix3d rotScale = new Matrix3d();
			rotScale.setColumn(0, p);
			Vector3d origin = new Vector3d();
			
			Transform3D oriTrans = new Transform3D();
			this.getTransform(oriTrans);
			oriTrans.get(origin);
			
			Transform3D trans = new Transform3D();
			trans.setTranslation(origin);
			trans.setRotationScale(rotScale);
			super.setTransform(trans);
		}
			
	}
	public void setPickable(Object object,Node node)
	{
		if (node instanceof Group)
		{
			for (Enumeration<Node> childNodes = ((Group)node).getAllChildren(); childNodes.hasMoreElements();)
			{
				setPickable(object, childNodes.nextElement());
			}
		}
		else if (node instanceof Shape3D)
		{
			LocationWrapper wrapper = new LocationWrapper(object, null, getCurrentFieldPortrayal());
			node.setUserData(wrapper);
			setPickableFlags((Shape3D)node);
		}
	}

	@Override
	protected int numShapes() {
		// TODO Auto-generated method stub
		return 0;
	}
}

