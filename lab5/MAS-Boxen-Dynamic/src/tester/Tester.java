package tester;

import java.util.List;

import base.Agent;
import base.Environment;

/**
 * Class to test the system.
 * 
 * @author Andrei Olaru
 */
public abstract class Tester
{
	/**
	 * Extension for test files.
	 */
	protected static final String	EXT				= ".txt";
	/**
	 * Name for the file containing the initial state of the world.
	 */
	protected static final String	SI				= "si";
	/**
	 * Name for the file containing the desired (goal) state of the world.
	 */
	protected static final String	SF				= "sf";
	
	/**
	 * The environment.
	 */
	protected Environment environment;
	
	/**
	 * The agents.
	 */
	protected List<Agent> agents;
	
	
	/**
	 * Main loop.
	 */
	protected void makeSteps()
	{
		System.out.println("\n\n================================================= INITIAL STATE:");
		
		System.out.println(environment.toString());
		System.out.println("\n\n=================================================");
		
		boolean complete = false;
		int nSteps = 0;
		while(!complete)
		{
			complete = environment.step();
			try
			{
				Thread.sleep(getDelay());
			} catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			System.out.println(environment.toString());
			nSteps++;
			System.out.println("\n\n================================================= STEP " + nSteps + " completed.");
		}
		System.out.println("\n\n================================================= ALL AGENTS COMPLETED.");
	}
	
	/**
	 * @return delay between successive steps (to be overridden).
	 */
	@SuppressWarnings("static-method")
	protected int getDelay()
	{
		return 0;
	}
}
