package disc.physics.examples;

import java.io.IOException;
import java.util.ArrayList;

import rungeKutta.EmbeddedRungeKutta;
import rungeKutta.RungeKutta;
import rungeKutta.Solver;

import com.xeiam.xchart.Chart;
import com.xeiam.xchart.QuickChart;
import com.xeiam.xchart.SwingWrapper;

import differentialEquations.DifferentialEquation;
import differentialEquations.InitialValueProblem;
import disc.physics.aerodynamics.FlightModel_HummelNew;
import disc.physics.aerodynamics.FlightModel_HummelOriginal;
import disc.physics.aerodynamics.FlightModel_Kain;
import disc.physics.aerodynamics.FlyingDisc;
import disc.physics.aerodynamics.FlyingDisc.flightCoefficientsType;
public class testWithRungeKuttaSolver {
	
	public static void main(String[] args) throws IOException
	{
		double t_0 = 0;
		double[] yHummelOriginal_0 = new double[] {-9.03E-01, -6.33E-01, -9.13E-01,
									 1.34E+01,  -4.11E-01, 1.12E-03,
									 -7.11E-02, 2.11E-01, 5.03E+00,
									 -1.49E+01, -1.48E+00, 5.43E+01};
		
		double[] yHummelNew_0 = new double[] {-9.03E-01, -6.33E-01, -9.13E-01,
										 -7.11E-02, 2.11E-01, 5.03E+00,
										 1.34E+01,  -4.11E-01, 1.12E-03,
										 -1.49E+01, -1.48E+00, 5.43E+01};
		double stepSize = 0.00001;
		double t_end = 1;
		
		FlyingDisc disc = new FlyingDisc(flightCoefficientsType.HUMMEL_SHORT);
		
		
		//instance DiscDeKain
		FlightModel_Kain eqKain = new frisbeeDeKainWrapper(disc);
		double[] yK_0 = eqKain.convertHummelToKainIvp(yHummelOriginal_0).convert2Array();
		DifferentialEquation equationKain   = (DifferentialEquation) eqKain;
		
		//instance DiscDeHummelOriginal
		DifferentialEquation equationHummelOriginal = new DiscDeHummelOriginalWraper();
		
		//instance DiscDeHummelOriginal
		DifferentialEquation equationHummelNew = new DiscDeHummelNewWraper(disc);
		
		//instance embeddedRungeKutta
		EmbeddedRungeKutta ode45 = new EmbeddedRungeKutta(EmbeddedRungeKutta.Methods.DORMAND_PRINCE);
		
		//instance forward Euler
		double[][] A = new double[][]{{0}};
		double[] b = new double[]{1};
		double[] c = new double[]{1};
		RungeKutta ode1 = new RungeKutta(A, b, c);
		
		
		// Listify equations and solver
		
		ArrayList<InitialValueProblem> ivpList = new ArrayList<InitialValueProblem>();
		

		ivpList.add(new InitialValueProblem(equationHummelOriginal,yHummelOriginal_0,t_0));
		//ivpList.add(new InitialValueProblem(equationKain,yK_0,t_0));
		ivpList.add(new InitialValueProblem(equationHummelNew,yHummelNew_0,t_0));
		
		ArrayList<Solver> solverList = new ArrayList<Solver>();
		solverList.add(ode45);
		solverList.add(ode1);
		
		for( InitialValueProblem ivp : ivpList)
		{
			ode45.setEquation(ivp.getEquation());
			
			double time = System.currentTimeMillis();
			ode45.run(ivp.getY_0(), t_0, t_end,1e-16,1e-3,0.8,2);
			double completedIn = System.currentTimeMillis() - time;
			
			System.out.println( "Computation Time for ode45 on " + ivp.getEquation().getClass().toString()
							   +": "+ completedIn +" ms");
			ode1.setEquation(ivp.getEquation());
			time = System.currentTimeMillis();
			ode1.run(ivp.getY_0(), t_0, t_end, stepSize);
			completedIn = System.currentTimeMillis() - time;
			
			System.out.println( "Computation Time for ode1 on " + ivp.getEquation().getClass().toString()
							   +": "+ completedIn +" ms");
			
			double[][] values1 = ode1.getY_values();
			double lastV1 = values1[values1.length-1][0];//values1[0].length-2];
			double[][] values45 = ode45.getY_values();
			double lastV45 = values45[values45.length-1][0];//values1[0].length-2];
			
			System.out.println( "solutiondifference " + (lastV1-lastV45));
			
			for (Solver solver : solverList)
			{
				time = System.currentTimeMillis();
				
				 Chart a = QuickChart.getChart("position", "t", "m",new String[]{"x","y","z"},solver.getT_values(),solver.getY_values(new int[]{0,1,2}));
				 Chart a2 = QuickChart.getChart("velocity", "t", "m",new String[]{"x","y","z"},solver.getT_values(),solver.getY_values(new int[]{3,4,5}));
				 Chart a3 = QuickChart.getChart("orientation", "t", "m",new String[]{"x","y"},solver.getT_values(),solver.getY_values(new int[]{6,7}));
				 Chart a4 = QuickChart.getChart("angular Acceleration", "t", "m",new String[]{"x","y","z"},solver.getT_values(),solver.getY_values(new int[]{9,10,11}));
				 
				 ArrayList<Chart> charts = new ArrayList<>();
				 charts.add(a);
				 charts.add(a2);
				 charts.add(a3);
				 charts.add(a4);
				 
				new SwingWrapper(charts).displayChartMatrix("Solver1");
				 
				completedIn = System.currentTimeMillis() - time;
				System.out.println("Quickchart Charts creation Time: " + completedIn);
			}
		}
			

	}
	private static class DiscDeHummelOriginalWraper extends FlightModel_HummelOriginal implements DifferentialEquation
	{
		@Override
		public double[] calculate(double t, double[] y){
			return super.calculate(t,y);
		}
		
	}
	private static class DiscDeHummelNewWraper extends FlightModel_HummelNew implements DifferentialEquation
	{
		DiscDeHummelNewWraper(FlyingDisc disc) throws IOException
		{
			
		}
		@Override
		public double[] calculate(double t, double[] y){
			return super.calculate(t,y);
		}
		
	}
	private static class frisbeeDeKainWrapper extends FlightModel_Kain implements DifferentialEquation
	{
		frisbeeDeKainWrapper(FlyingDisc disc)throws IOException
		{
			
		}
		
		public double[] calculate(double t, double[] y) throws IOException{
			return super.calculate(t,y);
		}
		
	}
}
