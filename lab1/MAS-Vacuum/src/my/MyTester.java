package my;

import gridworld.AbstractGridEnvironment;
import gridworld.AbstractGridEnvironment.GridAgentData;
import gridworld.GridOrientation;
import tester.DummyAgent;
import tester.Tester;

/**
 * Main class for testing.
 * 
 * @author Andrei Olaru
 */
public class MyTester extends Tester
{
	/**
	 * Delay between two agent steps. In milliseconds.
	 */
	protected static final int STEP_DELAY = 500;
	
	/**
	 * Creates a new tester instance and begins testing.
	 */
	public MyTester()
	{
		env = new MyEnvironment();
//		env.addAgent(new GridAgentData(new DummyAgent(), ((AbstractGridEnvironment) env).getBottomLeft(),
//				GridOrientation.NORTH));
		env.addAgent(new GridAgentData(new MyAgent(), ((AbstractGridEnvironment) env).getBottomLeft(),
				GridOrientation.NORTH));
		
		
		System.out.println(env.printToString());
		
		makeSteps();
	}
	
	@Override
	protected int getDelay()
	{
		return STEP_DELAY;
	}
	
	/**
	 * Main.
	 * 
	 * @param args
	 *            - not used
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		new MyTester();
	}
}
