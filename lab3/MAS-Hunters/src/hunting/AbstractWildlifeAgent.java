package hunting;

import base.Agent;

/**
 * Parent class for agents in the predator-prey scenario.
 * 
 * @author andreiolaru
 */
public abstract class AbstractWildlifeAgent implements Agent
{
	/**
	 * Agent ID generator.
	 */
	public static int			counter	= 0;
	/**
	 * Agent ID.
	 */
	protected int				id		= counter++;
	/**
	 * The type of the agent.
	 */
	protected WildlifeAgentType	type;
	
	/**
	 * Default constructor.
	 * 
	 * @param agentType
	 *            - the type of the agent.
	 */
	public AbstractWildlifeAgent(WildlifeAgentType agentType)
	{
		type = agentType;
	}
	
	/**
	 * @return the type of the agent.
	 */
	public WildlifeAgentType getType()
	{
		return type;
	}
	
	/**
	 * @return the ID of the agent.
	 */
	public int getId()
	{
		return id;
	}
	
	@Override
	public int hashCode()
	{
		return id;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		AbstractWildlifeAgent other = (AbstractWildlifeAgent) obj;
		if(id != other.id)
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return type.toString() + Integer.toString(id);
	}
}
