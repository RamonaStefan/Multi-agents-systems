package my;

import tester.Tester;

/**
 * Main class for testing.
 * 
 * @author Alexandru Sorici
 */
public class MyTester extends Tester
{
	/**
	 * Delay between two agent steps. In milliseconds.
	 */
	protected static final int	STEP_DELAY		= 500;
	
	/**
	 * Number of predators
	 */
	protected static final int	NUM_PREDATORS	= 4;
//	protected static final int	NUM_PREDATORS	= 10;

	/**
	 * Number of prey agents
	 */
	protected static final int	NUM_PREY		= 4;
//	protected static final int	NUM_PREY		= 8;

	/**
	 * Range of vision for prey agents.
	 */
	public static final int		PREY_RANGE		= 2;
	/**
	 * Range of vision for predator agents.
	 */
	public static final int		PREDATOR_RANGE	= 5;
	
	/**
	 * Map width
	 */
//	protected static final int	MAP_WIDTH		= 10;
	protected static final int	MAP_WIDTH		= 30;


	/**
	 * Map height
	 */
	protected static final int	MAP_HEIGHT		= 7;
//	protected static final int	MAP_HEIGHT		= 20;

	/**
	 * Creates a new tester instance and begins testing.
	 */
	public MyTester()
	{
		// create environment instance
		env = new MyEnvironment(MAP_WIDTH, MAP_HEIGHT, NUM_PREDATORS, NUM_PREY);
		
		System.out.println(env.printToString());
		
		makeSteps();
		
		System.out.println("[Environment] Goal completed. All prey is dead.");
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
