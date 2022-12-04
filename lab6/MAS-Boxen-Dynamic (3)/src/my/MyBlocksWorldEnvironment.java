package my;

import java.util.*;

import base.Agent;
import blocksworld.BlocksWorld;
import blocksworld.BlocksWorldAction;
import blocksworld.BlocksWorldEnvironment;
import blocksworld.Stack;

/**
 * Class implementing specific functionality for the environment.
 */
public class MyBlocksWorldEnvironment extends BlocksWorldEnvironment
{
	private AgentData token = null;

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
		int n;

		ArrayList<AgentData> agents = new ArrayList<>();
		for (AgentData ag : actionMap.keySet()) {
			agents.add(ag);
		}
		if (agents.size() == 2) {
			AgentData firstAgent = agents.get(0);
			AgentData secondAgent = agents.get(1);

			BlocksWorldAction firstAction = actionMap.get(firstAgent);
			BlocksWorldAction secondAction = actionMap.get(secondAgent);

			if(token == null) {
				token = firstAgent;
			}

			if(!firstAgent.equals(secondAgent) && firstAction.isConflicting(secondAction)) {
				if(this.hasToken(firstAgent)) {
					actionMap.remove(secondAgent);
					n = super.performActions(actionMap);
					firstAgent.setPreviousActionSuccessful();
					secondAgent.setPreviousActionFailed();
					token = secondAgent;
					return n;
				}
				else {
					n = super.performActions(actionMap);
					actionMap.remove(firstAgent);
					secondAgent.setPreviousActionSuccessful();
					firstAgent.setPreviousActionFailed();
					token = firstAgent;
					return n;
				}
			}

		}
		return super.performActions(actionMap);
	}
	
	@Override
	protected boolean hasToken(AgentData agent)
	{
		// TODO return if an agent has the token or not.
		return agent.equals(token);
	}

}
