package visualization;

import java.awt.Color;
import java.awt.Image;
import java.io.FileNotFoundException;
import java.util.Enumeration;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import sim.portrayal.LocationWrapper;
import sim.portrayal3d.SimplePortrayal3D;
import sim.portrayal3d.simple.BranchGroupPortrayal3D;
import sim.portrayal3d.simple.CubePortrayal3D;
import sim.portrayal3d.simple.PrimitivePortrayal3D;
import sim.util.Double3D;
import ultimate.UltimateEntity;
import ultimate.steppableEntity.Frisbee;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.geometry.ColorCube;

public class UltimateEntityPortrayal3D extends PrimitivePortrayal3D 
{
	BranchGroup bgTop;
		TransformGroup tgEntity;
			TransformGroup tgInitialTransform;
				BranchGroup bgEntityModel;
				
		BranchGroup bgPhysics;
			BranchGroup bgArrows;
				Arrow velocityArrow;
				Arrow omegaArrow;
				Arrow accelerationArrow;
				Arrow alphaArrow;
				Arrow orientationXArrow;
				Arrow orientationZArrow;
		BranchGroup bgTrail;
		    TransformGroup tgTrail;
				Shape3D shapeTrail;
	

	/** Constructs a UltimateEntityPortrayal3D with a default (flat opaque white) appearance and a scale of 1.0. */
	
    public UltimateEntityPortrayal3D(BranchGroup entityModel,Transform3D transform)
    {
    		bgTop = new BranchGroup();
    		bgTop.setCapability(BranchGroup.ALLOW_DETACH);
    		bgTop.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
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
						orientationXArrow = new Arrow("localX", Color.RED);
						orientationXArrow.setCapability(BranchGroup.ALLOW_DETACH);
						orientationZArrow = new Arrow("localZ", Color.RED);
						orientationZArrow.setCapability(BranchGroup.ALLOW_DETACH);
				bgTrail = new BranchGroup();
				 	tgTrail = new TransformGroup();
				 	tgTrail.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
						shapeTrail = new Shape3D();
						shapeTrail.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
						shapeTrail.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
			
			bgTop.addChild(tgEntity);
				tgEntity.addChild(tgInitialTransform);
				tgInitialTransform.addChild(bgEntityModel);
			bgTop.addChild(bgPhysics);
				bgPhysics.addChild(bgArrows);
					bgArrows.addChild(velocityArrow);
					bgArrows.addChild(omegaArrow);
					bgArrows.addChild(accelerationArrow);
					bgArrows.addChild(alphaArrow);
					bgArrows.addChild(orientationXArrow);
					bgArrows.addChild(orientationZArrow);
			bgTop.addChild(bgTrail);		
				bgTrail.addChild(tgTrail);
					tgTrail.addChild(shapeTrail);
			
			group = bgTop;	
    }
    public UltimateEntityPortrayal3D(BranchGroup entityModel)
    {
    	this(entityModel, new Transform3D());
    }
    public UltimateEntityPortrayal3D()
    {
    	this("3dmodels/monkey.obj",new Transform3D() {{setIdentity();}});
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
			prev.addChild(bgTop);
			
			QuadArray q= new QuadArray(4, QuadArray.COORDINATES);
			Double3D p  = entity.getPosition();
			q.setCoordinate(0,new double[]{p.x,p.y,0});
			q.setCoordinate(1,new double[]{p.x,p.y,p.z});
			q.setCoordinate(2,new double[]{p.x,p.y,p.z});
			q.setCoordinate(3,new double[]{p.x,p.y,0});
			shapeTrail.addGeometry(q);
			shapeTrail.setAppearance( new Appearance()
			{	
				{
					setColoringAttributes(new ColoringAttributes(new Color3f(Color.WHITE) ,ColoringAttributes.NICEST));
					setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.4f));
				}
			});
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
		
		Matrix3d o = entity.getOrientationAsMatrix();
		
		Transform3D rotationalTrans = new Transform3D(entity.getOrientationAsMatrix(), new Vector3d(),1);
		tgEntity.setTransform(rotationalTrans);		
		if (entity.getArrowsVisible())
		{
			if (bgArrows.getParent() == null) bgPhysics.addChild(bgArrows);
			
			if (entity.getVelocity().length() != 0)
				velocityArrow.setHeadRelative(entity.getVelocity().normalize());
			
			if (entity.getOmega().length() != 0)
			{
				omegaArrow.setHeadRelative(entity.getOmega().normalize());
				omegaArrow.mulTransform(rotationalTrans);
			}
			if (entity.getAcceleration().length() != 0)
				accelerationArrow.setHeadRelative(entity.getAcceleration().normalize());
			
			if (entity.getAlpha().length() != 0)
			{
				alphaArrow.setHeadRelative(entity.getAlpha().normalize());
				alphaArrow.mulTransform(rotationalTrans);
			}			
			orientationXArrow.setHeadRelative(new Double3D(o.getM00(),o.getM10(),o.getM20()));
			orientationZArrow.setHeadRelative(new Double3D(o.getM02(),o.getM12(),o.getM22()));
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
		// if ( entity.getTrailVisible)

		Transform3D t1 = new Transform3D();
		prev.getTransform(t1);
		t1.invert();
		tgTrail.setTransform(t1);
		QuadArray lastTrail =  (QuadArray)shapeTrail.getGeometry(shapeTrail.numGeometries()-1);
		
		double[] coordinate0 = new double[]{0,0,0};
		double[] coordinate1 = new double[]{0,0,0};
		lastTrail.getCoordinate(3, coordinate0);
		lastTrail.getCoordinate(2, coordinate1);
		
		Double3D tmp = new Double3D(coordinate1[0],coordinate1[1],coordinate1[2]);
		Double3D pos = entity.getPosition();
		Double3D dir = tmp.subtract(pos);
		if (dir.length() > 0.1)
		{
			QuadArray trail = new QuadArray(8, QuadArray.COORDINATES | QuadArray.NORMALS);
			trail.setCapability(QuadArray.ALLOW_NORMAL_WRITE);
			trail.setCoordinate(0, coordinate0);
			trail.setCoordinate(1,coordinate1);
			trail.setCoordinate(2,new double[]{pos.x,pos.y,pos.z});
			trail.setCoordinate(3,new double[]{pos.x,pos.y,0});
			trail.setCoordinate(7, coordinate0);
			trail.setCoordinate(6,coordinate1);
			trail.setCoordinate(5,new double[]{pos.x,pos.y,pos.z});
			trail.setCoordinate(4,new double[]{pos.x,pos.y,0});
			
			shapeTrail.addGeometry(trail);
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

		public void setHeadRelative(Double3D pa) 
		{
			if(pa.length() != 0)
			{
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