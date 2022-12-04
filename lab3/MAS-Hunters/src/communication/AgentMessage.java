package communication;

import java.util.HashSet;
import java.util.Set;

import base.Agent;

/**
 * An agent sent between two agents.
 * 
 * @author andreiolaru
 */
public class AgentMessage
{
	/**
	 * The sender.
	 */
	protected AgentID	sender;
	/**
	 * The intended receiver.
	 */
	protected AgentID	destination;
	/**
	 * The content.
	 */
	protected Object	content;
	
	/**
	 * @param sender
	 *            - the ID of the sender.
	 * @param destination
	 *            - the ID of the destination.
	 * @param content
	 *            - the content.
	 */
	public AgentMessage(AgentID sender, AgentID destination, Object content)
	{
		this.sender = sender;
		this.destination = destination;
		this.content = content;
	}
	
	/**
	 * @return the sender.
	 */
	public AgentID getSender()
	{
		return sender;
	}
	
	/**
	 * @return the destination.
	 */
	public AgentID getDestination()
	{
		return destination;
	}
	
	/**
	 * @return the content.
	 */
	public Object getContent()
	{
		return content;
	}
	
	@Override
	public String toString()
	{
		return "[" + sender + "@" + destination + ":" + content + "]";
	}
	
	/**
	 * helper method to filter from a set of messages only those for a specified agent.
	 * 
	 * @param allMessages
	 *            - the messages to filter.
	 * @param destinationID
	 *            - the ID of the destination agent.
	 * @return messages for the specified destination.
	 */
	public static Set<AgentMessage> filterMessagesFor(Set<AgentMessage> allMessages, AgentID destinationID)
	{
		Set<AgentMessage> result = new HashSet<>();
		for(AgentMessage m : allMessages)
			if(m.destination.equals(destinationID))
				result.add(m);
		return result;
	}
	
	/**
	 * helper method to filter from a set of messages only those for a specified agent.
	 * 
	 * @param allMessages
	 *            - the messages to filter.
	 * @param destinationAgent
	 *            - the destination agent.
	 * @return messages for the specified destination.
	 */
	public static Set<AgentMessage> filterMessagesFor(Set<AgentMessage> allMessages, Agent destinationAgent)
	{
		return filterMessagesFor(allMessages, AgentID.getAgentID(destinationAgent));
	}
}
