package my;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import base.Agent;
import blocksworld.BlocksWorld;
import blocksworld.DynamicEnvironment;
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
	 * number of teams (may be single-agent teams); if only one team, sf.tyxt will be used; otherwise, sfi.txt will be
	 * used, with i the 1-based index of the team.
	 */
	protected static final int		TEAMS_NO						= 1;
	/**
	 * Number of cooperative agents in a team.
	 */
	protected static final int		TEAM_SIZE						= 1;
	/**
	 * The place to get the tests from.
	 */
	protected static final String	TEST_SUITE						= "tests/1-1/";
	/**
	 * To use test suites meant for multiple teams with only one team, modify this member with the desired suffix.
	 * <p>
	 * Example: if this suffix is set to "2", then "sf2.txt" will be used instead of "sf.txt".
	 */
	protected static final String	FORCE_SINGLE_FINAL_STATE_SUFFIX	= "";
	
	/**
	 * The probability for the environment to change dynamically at one step. Between 0 and 1.
	 */
	public static final float	DYNAMICITY	= 0.5f;
	/**
	 * The seed for the random generator, or -1 if to pick one from the time.
	 */
	public static final long	SEED		= -1;
	
	/**
	 * The name of the agent.
	 */
	protected static final String AGENT_NAME = "*A";
	
	/**
	 * Creates a new tester instance and begins testing.
	 * 
	 * @throws IOException
	 *             - see {@link Tester}.
	 */
	public MyTester() throws IOException
	{
		initializeEnvironment(TEST_SUITE);
		initializeAgents(TEAMS_NO, TEAM_SIZE, TEST_SUITE);
		makeSteps();
	}
	
	/**
	 * Main loop.
	 * 
	 * @param testSuite
	 *            - the path for test files.
	 * 
	 * @throws FileNotFoundException
	 *             - if world state file not found.
	 * @throws IOException
	 *             - if world state file is corrupted.
	 */
	protected void initializeEnvironment(String testSuite) throws IOException
	{
		try (InputStream input = new FileInputStream(testSuite + SI + EXT))
		{
			environment = new DynamicEnvironment(new BlocksWorld(input), SEED);
		}
	}
	
	/**
	 * @param teamsNumber
	 *            - the number of agent teams.
	 * @param teamSize
	 *            - the size of each team.
	 * @param testSuite
	 *            - the path for test files.
	 * @throws FileNotFoundException
	 *             - if desired state file not found.
	 * @throws IOException
	 *             - if desired state file is corrupted.
	 */
	protected void initializeAgents(int teamsNumber, int teamSize, String testSuite) throws IOException
	{
		agents = new LinkedList<>();
		Map<Agent, BlocksWorld> agentsStates = new HashMap<>();
		String name = AGENT_NAME;
		for(int team = 0; team < teamsNumber; team++)
		{
			String teamSuffix = (teamsNumber > 1 ? Integer.valueOf(team + 1).toString() : FORCE_SINGLE_FINAL_STATE_SUFFIX);
			try (InputStream input = new FileInputStream(testSuite + SF + teamSuffix + EXT))
			{
				BlocksWorld desires = new BlocksWorld(input);
				Agent leader = new MyAgent(desires, name);
				agentsStates.put(leader, desires);
				agents.add(leader);
				// larger team sizes unsupported
			}
			name = name.substring(0, 1) + (char)(name.charAt(name.length() - 1) + 1);
		}
		for(Agent agent : agentsStates.keySet())
			environment.addAgent(agent, agentsStates.get(agent), null);
		for(Agent agent : agents)
		{
			if(agentsStates.get(agent) != null)
			{
				System.out.println(agent.toString() + " desires:");
				System.out.println(agentsStates.get(agent).toString());
			}
		}
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
	 * @throws IOException
	 *             - see {@link Tester}.
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException
	{
		new MyTester();
	}
}
