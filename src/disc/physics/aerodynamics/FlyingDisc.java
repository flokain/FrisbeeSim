package disc.physics.aerodynamics;

import java.io.IOException;

import disc.physics.Disc;
import disc.physics.Disc.discType;
public class FlyingDisc extends Disc{
		
	public final double gamma0;		// minimum drag and zero lift at GAMMA0 Hummel page 9
	public final double[] cl;		// lift coefficients
	public final double[] cd;		// drag coefficients
	public final Matrix Ctau;		// Momenti coefficients of omega
	public final double[] ctau;		// Momenti coefficients of gamma
	
	public enum flightCoefficientsType 
	{
		HUMMEL_SHORT(Disc.discType.ULTRASTAR),
		HUMMEL_LONG(Disc.discType.ULTRASTAR);
		
		public final discType disc;
		
		flightCoefficientsType(discType disc)
		{
			this.disc = disc;
		}
	}
	
	public FlyingDisc(flightCoefficientsType co) throws IOException
	{
			super(co.disc);
			
			if (co == flightCoefficientsType.HUMMEL_SHORT || co == flightCoefficientsType.HUMMEL_LONG)
			{
				double CL0 = 0.3331;	                 //The lift coefficient at alpha = 0.       
				double CLA = 1.9124;                     //The lift coefficient dependent on alpha. 
				cl = new double[]{CL0,CLA};            
				
				double CD0 = 0.1769;                     //The drag coefficent at alpha = 0.       
				double CDA = 0.685;	                     //The drag coefficient dependent on alpha
				cd = new double[]{CD0,0,CDA};
				
				double CMO = -0.0821;					 // The pitch momentum coefficient at alpha = 0.
				double CMa = 0.4338;					 // The pitch momentum dependent on alpha.
				ctau = new double[]{CMO,CMa};
				
				gamma0 = -CL0/CLA;                       // minimum drag and zero lift at GAMMA0 Hummel page 9  
				
				double CRr = 0.00171;
				if(co == flightCoefficientsType.HUMMEL_SHORT)
				{
					double CMq = -0.005;
					double CRp = -0.0055;
					double CNr = -0.0000071;
					Ctau = new Matrix(CRp, 0.,   CRr,	 // Momenti coefficients dependent on omega
										0.,   CMq, 0.,  
										0.,   0.,   CNr);
				}
				else if(co == flightCoefficientsType.HUMMEL_LONG)
				{
					double CMq = -0.0144; 				 // Momenti coefficients dependent on omega
					double CRp = -0.0125;
					double CNr = -0.0000341;
					Ctau = new Matrix(CRp, 0.,   CRr,
										0.,   CMq, 0.,  
										0.,   0.,   CNr);
				}
				else
					throw new IOException("unknown flightCoefficients");
			}
			else
				throw new IOException("unknown flightCoefficients");
			
	}
}

		
