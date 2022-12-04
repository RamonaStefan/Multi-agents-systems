package my;

import java.util.Map;

import blocksworld.BlocksWorld;
import blocksworld.BlocksWorldAction;
import blocksworld.BlocksWorldEnvironment;

/**
 * Class implementing specific functionality for the environment.
 */
public class MyBlocksWorldEnvironment extends BlocksWorldEnvironment
{

	/**
	 * @param world - the initial world.
	 */
	public MyBlocksWorldEnvironment(BlocksWorld world)
	{
		super(world);
	}
	
	@Override
	protected int performActions(Map<AgentData, BlocksWorldAction> actionMap)
	{
		// TODO solve conflicts if there are multiple agents.
		return super.performActions(actionMap);
	}
	
	@Override
	protected boolean hasToken(AgentData agent)
	{
		// TODO return if an agent has the token or not.
		return super.hasToken(agent);
	}
}
