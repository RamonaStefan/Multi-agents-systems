package base;

/**
 * Interface to be implemented by environment implementations.
 * 
 * @author Andrei Olaru
 */
public interface Environment
{
	/**
	 * The class contains data that characterizes the external state of an agent, from the point of view of the
	 * environment. For instance, the agent's position.
	 *
	 * @author andreiolaru
	 */
	public static class AgentData
	{
		/**
		 * The agent's implementation.
		 */
		protected Agent agent;
		
		/**
		 * @param linkedAgent
		 *            - the internal implementation of the agent about which this instance contains environment data.
		 */
		public AgentData(Agent linkedAgent)
		{
			agent = linkedAgent;
		}
	}
	
	/**
	 * @return <code>true</code> if the goals of all agents have completed.
	 */
	boolean goalsCompleted();
	
	/**
	 * Adds an agent to the environment. The environment places the agent in it, in the specified state.
	 * 
	 * @param agentData
	 *            - all the data the environment needs about an agent, containing extrinsic state and a reference to the
	 *            agent's implementation.
	 */
	void addAgent(AgentData agentData);
	
	/**
	 * When the method is invoked, all agents should receive a perception of the environment and decide on an action to
	 * perform.
	 */
	void step();
	
	/**
	 * Renders the environment into a easy readable String.
	 * 
	 * @return the {@link String} representation.
	 */
	String printToString();
}
