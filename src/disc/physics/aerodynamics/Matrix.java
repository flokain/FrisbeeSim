package disc.physics.aerodynamics;

import sim.util.Double3D;
import sim.util.MutableDouble3D;

class Matrix
{
	Double3D[] el =new Double3D[3];
	
	public Matrix(Matrix m)
	{ 
		for(int i= 0; i<3;i++) el[i] = new Double3D(m.el[i]);	
	}
	
	public Matrix()
	{ 
		for(int i= 0; i<3;i++) el[i] = new Double3D(0,0,0);	
	}
	public Matrix(Double3D a, Double3D b, Double3D c)
	{ 
		el[0] = a;
		el[1] = b;
		el[2] = c;
	}
	public Matrix(double y0,double y1, double y2) //diag
	{
		el[0] = new Double3D(y0,0,0);
		el[1] = new Double3D(0,y1,0);
		el[2] = new Double3D(0,0,y2);
	}
	public Matrix(double y0,double y1, double y2,double y3,double y4,double y5,double y6,double y7,double y8) //zeilen weise
	{
		el[0] = new Double3D(y0,y1,y2);
		el[1] = new Double3D(y3,y4,y5);
		el[2] = new Double3D(y6,y7,y8);
	}
	
	public Matrix(double [] y) //zeilen weise
	{
		for(int i= 0; i<3;i++) el[i] = new Double3D(y[i],y[i+3],y[i+6]);
	}
	
	public Matrix setRow(int i, double[] y)
	{	
		setRow(i, new Double3D(y[0],y[1],y[2]));;
		return this;
	}
	public Matrix setRow(int i, Double3D y)
	{	
		el[i] = new Double3D(y);
		return this;
	}
	public double get(int i, int j){
		
		if (j == 0)	return el[i].x;
		if (j == 1)	return el[i].y;
		if (j == 2)	return el[i].z;
		return 0;
	}
	
	public Double3D getCol(int j) 
	{
		if (j == 0)	return new Double3D(el[0].x,el[1].x,el[2].x);
		if (j == 1)	return new Double3D(el[0].y,el[1].y,el[2].y);
		if (j == 2)	return new Double3D(el[0].z,el[1].z,el[2].z);
		return null;
	}
	public double[] convert2Array()
	{
		return new double[]{el[0].x, el[0].y, el[0].z, el[1].x, el[1].y, el[1].z, el[2].x, el[2].y, el[2].z};
	}
	
	public Matrix setIdentity()
	{
		el[0] = new Double3D(1,0,0);
		el[1] = new Double3D(0,1,0);
		el[2] = new Double3D(0,0,1);
		
		return this;
	}
	
	public Double3D mul(Double3D y)
	{
		MutableDouble3D re = new MutableDouble3D();
	
		re.x = el[0].dot(y);
		re.y = el[1].dot(y);
		re.z = el[2].dot(y);
		return new Double3D(re);
	}
	
	public Matrix mul(Matrix m)
	{
		Matrix ans = new Matrix();
		for(int i= 0; i<3;i++)
		{
			ans.el[i] = this.mul(m.getCol(i));
		}
		return ans.transpose();
	}
	
	public Matrix rotX(double ang)
	{
		double c = Math.cos(ang);
		double s = Math.sin(ang);
		return this.mul(new Matrix( 1, 0, 0,
									0, c, -s,
									0, s, c ) );
	}
	public Matrix rotY(double ang)
	{
		double c = Math.cos(ang);
		double s = Math.sin(ang);
		return this.mul(new Matrix( c,  0, s,
									0,  1, 0,
									-s, 0, c ) );
	}
	public Matrix rotZ(double ang)
	{
		double c = Math.cos(ang);
		double s = Math.sin(ang);
		return this.mul(new Matrix( c,  -s, 0,
									s,  c,  0,
									0,  0,  1 ) );
	}
	
	public Matrix transpose()
	{
		Matrix ans = new Matrix();
		for(int i= 0; i<3;i++) ans.el[i]=this.getCol(i);
		return ans;
	}
	public Matrix InvertDiag()
	{
		Matrix tmp = new Matrix();
		
		tmp.el[0] = new Double3D (1/el[0].x, 0,0);
		tmp.el[1] = new Double3D (0, 1/el[1].y ,0);
		tmp.el[2] = new Double3D (0, 0, 1/el[2].z);
		
		return tmp;
	}
	public Matrix cross(Double3D b)
	{
		return new Matrix( FlightModel_Kain.cross(b,getCol(0)), 
						   FlightModel_Kain.cross(b,getCol(1)), 
						   FlightModel_Kain.cross(b,getCol(2)));
	}
	public double det()
	{
		return  el[0].x*(el[1].y * el[2].z - el[1].z*el[2].y) -
				el[0].y*(el[1].x * el[2].z - el[1].z * el[2].x) +
				el[0].z*(el[1].x * el[2].y - el[1].y*el[2].x);
	}
	public Matrix normalize()
	{
		return new Matrix( getCol(0).normalize(),
						   getCol(1).normalize(),
						   getCol(2).normalize())
				   .transpose();
	}
	public boolean isRotational()
	{
		double tmp = Math.abs(det()-1);
		return true; //(tmp < 1e-14);
	}
}

