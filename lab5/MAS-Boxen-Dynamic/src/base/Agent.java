package base;

/**
 * Interface to be implemented by agents.
 * 
 * @author Andrei Olaru
 */
public interface Agent
{
	/**
	 * Supplies the agent with perceptions and demands one action from the agent. The environment also specifies if the
	 * previous action of the agent has succeeded or not.
	 * 
	 * @param input
	 *            - the perceptions offered by the environment to the agent.
	 * 
	 * @return the agent output, containing the action to perform. Action should be of type
	 *         {@link blocksworld.BlocksWorldAction.Type#NONE} if the agent is not performing an action now, but may perform more
	 *         actions in the future. Action should be of type {@link blocksworld.BlocksWorldAction.Type#AGENT_COMPLETED} if the
	 *         agent will not perform any more actions ever in the future.
	 */
	public Action response(Perceptions input);
	
	/**
	 * @return the name of the agent.
	 */
	@Override
	public String toString();
	
	/**
	 * @return a string that is printed at every cycle to show the status of the agent.
	 */
	public String statusString();
}
