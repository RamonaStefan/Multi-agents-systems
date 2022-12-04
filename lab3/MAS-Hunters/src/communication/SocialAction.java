package communication;

import java.util.HashSet;
import java.util.Set;

import base.Action;
import base.Agent;
import my.MyEnvironment.MyAction;

/**
 * An implementation for the result of an agent response that contains both a physical action and a series of social
 * actions (messages).
 * 
 * @author andreiolaru
 */
public class SocialAction implements Action
{
	/**
	 * The physical action the agent wants to perform.
	 */
	MyAction			action;
	/**
	 * The messages the agent wants to send.
	 */
	Set<AgentMessage>	outgoingMessages	= new HashSet<>();
	
	/**
	 * @param physicalAction
	 *            - the action to perform.
	 */
	public SocialAction(MyAction physicalAction)
	{
		action = physicalAction;
	}
	
	/**
	 * @return the action the agents wants to perform.
	 */
	public MyAction getPhysicalAction()
	{
		return action;
	}
	
	/**
	 * @return the messages the agent wants to send.
	 */
	public Set<AgentMessage> getOutgoingMessages()
	{
		return outgoingMessages;
	}
	
	/**
	 * Simplified method to add a new message to the list of outgoing messages.
	 * 
	 * @param sender
	 *            - the sending {@link Agent} (normally <code>this</code>).
	 * @param destination
	 *            - the ID of the destination agent.
	 * @param content
	 *            - the content of the message.
	 */
	public void addOutgoingMessage(Agent sender, AgentID destination, Object content)
	{
		addOutgoingMessage(new AgentMessage(AgentID.getAgentID(sender), destination, content));
	}
	
	/**
	 * Adds a new message to the list of outgoing messages.
	 * 
	 * @param message
	 *            - the message to send.
	 */
	public void addOutgoingMessage(AgentMessage message)
	{
		outgoingMessages.add(message);
	}
	
	@Override
	public String toString()
	{
		return "{" + action + (outgoingMessages.isEmpty() ? "" : ":" + outgoingMessages) + "}";
	}
}
