package tester;

import base.Environment;


/**
 * Class containing testing functionality.
 * 
 * @author Andrei Olaru
 */
public class Tester
{
	/**
	 * The environment to test, containing the agents.
	 */
	protected Environment env;
	
	/**
	 * Step counter.
	 */
	protected int stepCount = 0; 
	
	/**
	 * Calls the <code>step</code> method of the environment until the environment is clean.
	 */
	protected void makeSteps()
	{
		while(!env.goalsCompleted())
		{
			env.step();
			stepCount++;
			
			System.out.println(env.printToString());
			System.out.println("Num steps: " + stepCount);
			System.out.println();
			
			try
			{
				Thread.sleep(getDelay());
			} catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return delay between successive steps.
	 */
	@SuppressWarnings("static-method")
	protected int getDelay()
	{
		return 0;
	}
}
