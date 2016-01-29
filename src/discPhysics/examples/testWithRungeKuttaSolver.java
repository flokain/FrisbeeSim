package discPhysics.examples;

import java.io.IOException;
import java.util.ArrayList;

import rungeKutta.EmbeddedRungeKutta;
import rungeKutta.RungeKutta;

import com.xeiam.xchart.Chart;
import com.xeiam.xchart.QuickChart;
import com.xeiam.xchart.SwingWrapper;

import differentialEquations.DifferentialEquation;
import discPhysics.FrisbeeDE;
import discPhysics.FrisbeeDeKain;
public class testWithRungeKuttaSolver {
	
	public static void main(String[] args) throws IOException
	{
		double t_0 = 0;
		double[] y_0 = new double[] {-9.03E-01, -6.33E-01, -9.13E-01,
									 1.34E+01,  -4.11E-01, 1.12E-03,
									 -7.11E-02, 2.11E-01, 5.03E+00,
									 -1.49E+01, -1.48E+00, 5.43E+01};
		double stepSize = 0.00001;
		double t_end = 1.46;
		FrisbeeDeKain eqKain = new frisbeeDeKainWrapper();
		double[] yK_0 = eqKain.convertHummelToKainIvp(y_0).convert2Array();
				
		 DifferentialEquation equationHummel = new frisbeeDeWrapper();
		 DifferentialEquation equationKain   = (DifferentialEquation) eqKain;
		 
		 //forward euler
		 double[][] A = new double[][]{{0}};
		 double[] b = new double[]{1};
		 double[] c = new double[]{1};
		 RungeKutta solver = new RungeKutta(A, b, c);
		 solver.setEquation(equationHummel);
		 double time = System.currentTimeMillis();
		 
		 solver.run(y_0, t_0, t_end, stepSize);
		//do something that takes some time...
		double completedIn = System.currentTimeMillis() - time;
		System.out.println("Computation Time: " + completedIn);
		
		time = System.currentTimeMillis();
		EmbeddedRungeKutta solver2 = new EmbeddedRungeKutta(EmbeddedRungeKutta.Methods.DORMAND_PRINCE);
		solver2.setEquation(equationHummel);
		solver2.run(y_0, t_0, t_end);
		System.out.println("Computation Time: " + completedIn);
		
		time = System.currentTimeMillis();
		 Chart a = QuickChart.getChart("position", "t", "m",new String[]{"x","y","z"},solver2.getT_values(),solver2.getY_values(new int[]{0,1,2}));
		 Chart a2 = QuickChart.getChart("velocity", "t", "m",new String[]{"x","y","z"},solver2.getT_values(),solver2.getY_values(new int[]{3,4,5}));
		 Chart a3 = QuickChart.getChart("orientation", "t", "m",new String[]{"x","y"},solver2.getT_values(),solver2.getY_values(new int[]{6,7}));
		 Chart a4 = QuickChart.getChart("angular Acceleration", "t", "m",new String[]{"x","y","z"},solver2.getT_values(),solver2.getY_values(new int[]{9,10,11}));
		 
		 ArrayList<Chart> charts = new ArrayList<>();
		 charts.add(a);
		 charts.add(a2);
		 charts.add(a3);
		 charts.add(a4);
		 
		 new SwingWrapper(charts).displayChartMatrix();
		 
		completedIn = System.currentTimeMillis() - time;
		System.out.println("Quickchart Charts creation Time: " + completedIn);
		
		time = System.currentTimeMillis();
		  a = QuickChart.getChart("position", "t", "m",new String[]{"x","y","z"},solver.getT_values(),solver.getY_values(new int[]{0,1,2}));
		  a2 = QuickChart.getChart("velocity", "t", "m",new String[]{"x","y","z"},solver.getT_values(),solver.getY_values(new int[]{3,4,5}));
		  a3 = QuickChart.getChart("orientation", "t", "m",new String[]{"x","y"},solver.getT_values(),solver.getY_values(new int[]{6,7}));
		  a4 = QuickChart.getChart("angular Acceleration", "t", "m",new String[]{"x","y","z"},solver.getT_values(),solver.getY_values(new int[]{9,10,11}));
		 
		 ArrayList<Chart> charts2 = new ArrayList<>();
		 charts2.add(a);
		 charts2.add(a2);
		 charts2.add(a3);
		 charts2.add(a4);
		 
		 new SwingWrapper(charts2).displayChartMatrix();
		 
		completedIn = System.currentTimeMillis() - time;
		System.out.println("Quickchart Charts creation Time: " + completedIn);
		 //new Plot(solver, equationKain, yK_0, t_0);

	}
	private static class frisbeeDeWrapper extends FrisbeeDE implements DifferentialEquation
	{
		@Override
		public double[] calculate(double t, double[] y){
			return super.calculate(t,y);
		}
		
	}
	private static class frisbeeDeKainWrapper extends FrisbeeDeKain implements DifferentialEquation
	{
		@Override
		public double[] calculate(double t, double[] y) throws IOException{
			return super.calculate(t,y);
		}
		
	}
}
