package communication;

import base.Agent;

/**
 * Simulates an opaque agent ID generation process.
 * <p>
 * In practice it is implemented using the hash code of the {@link Agent} instance.
 * 
 * @author andreiolaru
 */
public class AgentID
{
	/**
	 * The hash code.
	 */
	int id;
	
	/**
	 * @param internalID
	 *            - the hash code.
	 */
	protected AgentID(int internalID)
	{
		id = internalID;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return ((AgentID) obj).id == this.id;
	}
	
	@Override
	public int hashCode()
	{
		return id;
	}
	
	/**
	 * Gets the agent ID for a given agent.
	 * 
	 * @param agent
	 *            - the agent.
	 * @return - the id.
	 */
	public static AgentID getAgentID(Agent agent)
	{
		return new AgentID(agent.hashCode());
	}
	
	@Override
	public String toString()
	{
		return "#" + id;
	}
}
