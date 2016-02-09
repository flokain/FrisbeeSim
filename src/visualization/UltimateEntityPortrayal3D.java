package visualization;

import java.awt.Color;
import java.awt.Image;
import java.io.FileNotFoundException;
import java.util.Enumeration;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import sim.portrayal.LocationWrapper;
import sim.portrayal3d.SimplePortrayal3D;
import sim.portrayal3d.simple.PrimitivePortrayal3D;
import sim.util.Double3D;
import Ultimate.Frisbee;
import Ultimate.UltimateEntity;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;

public class UltimateEntityPortrayal3D extends PrimitivePortrayal3D 
{
	TransformGroup tgTop;
		TransformGroup tgEntity;
			TransformGroup tgInitialTransform;
				BranchGroup bgEntityModel;
		BranchGroup bgPhysics;
			BranchGroup bgArrows;
				Arrow velocityArrow;
				Arrow omegaArrow;
				Arrow accelerationArrow;
				Arrow alphaArrow;
	

	/** Constructs a UltimateEntityPortrayal3D with a default (flat opaque white) appearance and a scale of 1.0. */
	
    public UltimateEntityPortrayal3D(BranchGroup entityModel,Transform3D transform)
    {
    		tgTop = new TransformGroup();
    		tgTop.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
    			tgEntity = new TransformGroup();
    			tgEntity.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    				tgInitialTransform = new TransformGroup(transform);
    					bgEntityModel = entityModel;
				bgPhysics = new BranchGroup();
				bgPhysics.setCapability(BranchGroup.ALLOW_DETACH);
				bgPhysics.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
				bgPhysics.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
					bgArrows = new BranchGroup();
					bgArrows.setCapability(BranchGroup.ALLOW_DETACH);
					bgArrows.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
					bgArrows.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
						velocityArrow = new Arrow ("Velocity",Color.YELLOW);
						velocityArrow.setCapability(BranchGroup.ALLOW_DETACH);
						omegaArrow = new Arrow("Omega", Color.YELLOW);
						omegaArrow.setCapability(BranchGroup.ALLOW_DETACH);
						accelerationArrow = new Arrow("Acceleration", Color.BLUE);
						accelerationArrow.setCapability(BranchGroup.ALLOW_DETACH);
						alphaArrow = new Arrow("Alpha", Color.BLUE);
						alphaArrow.setCapability(BranchGroup.ALLOW_DETACH);
					
			
			tgTop.addChild(tgEntity);
				tgEntity.addChild(tgInitialTransform);
				tgInitialTransform.addChild(bgEntityModel);
			tgTop.addChild(bgPhysics);
				bgPhysics.addChild(bgArrows);
					bgArrows.addChild(velocityArrow);
					bgArrows.addChild(omegaArrow);
					bgArrows.addChild(accelerationArrow);
					bgArrows.addChild(alphaArrow);
				
			
			group = tgTop;	
    }
    public UltimateEntityPortrayal3D(BranchGroup entityModel)
    {
    	this(entityModel, null);
    }
    
    public UltimateEntityPortrayal3D(String fileLocation, Transform3D transform)
    {
    	this(loadModel(fileLocation),transform);
    }
    
	@Override
	public TransformGroup getModel(Object object, TransformGroup prev)
	{
		UltimateEntity entity = (UltimateEntity)object;
		
		if (prev == null)
		{
			prev = new TransformGroup();
			prev.setCapability(Group.ALLOW_CHILDREN_READ);
			prev.addChild(tgTop);
			//set facets of the disc pickable.
			setPickable(entity, bgEntityModel);
		}
		
		Matrix3d rot1 = new Matrix3d();
		rot1.rotX(-entity.getOrientation().x);
		Matrix3d rot2 = new Matrix3d();
		rot2.rotY(-entity.getOrientation().y);
		
		Matrix3d orientationAsMatrix = new Matrix3d();
		orientationAsMatrix.setIdentity();
		orientationAsMatrix.mul(rot2);
		orientationAsMatrix.mul(rot1);
		orientationAsMatrix.transpose();
		
		Transform3D rotationalTrans = new Transform3D(orientationAsMatrix, new Vector3d(),1);
		tgEntity.setTransform(rotationalTrans);		
		if (entity.getArrowsVisible())
		{
			if (bgArrows.getParent() == null) bgPhysics.addChild(bgArrows);

			velocityArrow.setHeadRelative(entity.getVelocity().normalize());
			
			omegaArrow.setHeadRelative(entity.getOmega().normalize());
			omegaArrow.mulTransform(rotationalTrans);
			accelerationArrow.setHeadRelative(entity.getAcceleration().normalize());
			alphaArrow.setHeadRelative(entity.getAlpha().normalize());
			alphaArrow.mulTransform(rotationalTrans);
		}
		else
		{
			try 
			{
				bgArrows.detach();
			}
			catch (Exception e)
			{
				
			}
		}
		
			
		return prev;
	}
	
	private static BranchGroup loadModel(String fileLocation)
	{
			Scene s = null;
		   	ObjectFile f = new ObjectFile ();
		    f.setFlags(ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY);
			try {
				s = f.load(fileLocation);
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
	private class Arrow extends BranchGroup
	{
		public TransformGroup t;
		
		public Arrow(Double3D top, String label,final Color color)
		{
			super();
			t = new TransformGroup();
			t.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			t.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
			
			addChild(t);
			t.addChild( new sim.portrayal3d.simple.Arrow(0.01, new Double3D(0,0,0), top, null, label,
					SimplePortrayal3D.appearanceForColors(color, null, color, Color.WHITE, 0.3f, 1f)));	
		}

		public Arrow(String label,final Color color) {

			this(new Double3D(1,0,0),label,color);
		}
		 public void mulTransform(Transform3D trans)
		 {
			 Transform3D t1 =  new Transform3D(trans);
			 Transform3D t2 = new Transform3D();
			 t.getTransform(t2);
			 t1.mul(t2);
			 t.setTransform(t1);
			 
		 }

		public void setHeadRelative(Double3D pa) {
			// TODO Auto-generated method stub
			Matrix3d rotScale = new Matrix3d();
			Vector3d p = new Vector3d(pa.x,pa.y,pa.z);
			double length = p.length();
			p.normalize();
			Vector3d p1 = new Vector3d();
			Vector3d p2 = new Vector3d();
			Vector3d pTmp1 = new Vector3d(1,0,0);
			Vector3d pTmp2 = new Vector3d(-1,0,0);
			if (p.equals(pTmp1)||p.equals(pTmp2) ) { pTmp1.set(0, 1, 0);}
			p1.cross(p, pTmp1);
			p2.cross(p, p1);
			
			
			rotScale.setColumn(0, p);
			rotScale.setColumn(1, p1);
			rotScale.setColumn(2, p2);
			
			Vector3d origin = new Vector3d();
			
			Transform3D oriTrans = new Transform3D();
			((TransformGroup)this.getChild(0)).getTransform(oriTrans);
			oriTrans.get(origin);
			
			Transform3D trans = new Transform3D();
			trans.setTranslation(origin);
			trans.setRotation(rotScale);
			trans.setScale(p.length());
			t.setTransform(trans);
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