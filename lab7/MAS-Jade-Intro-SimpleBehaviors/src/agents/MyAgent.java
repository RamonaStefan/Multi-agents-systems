package agents;

import java.util.*;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * The Agent.
 */
public class MyAgent extends Agent {
	/**
	 * The serial UID.
	 */
	private static final long	serialVersionUID			= 2081456560111009192L;
	/**
	 * The name of the registration protocol.
	 */
	static final String			REGISTRATION_PROTOCOL		= "register-child";
	static final String			UP_PROTOCOL					= "send-child-value";
	static final String			DOWN_PROTOCOL				= "send-global-max-value";
	static final String			REPLY_ID					= "conv-value";


	/**
	 * Time between checking for messages.
	 */
	static final int			TICK_PERIOD					= 100;
	/**
	 * Number of ticks to wait for registration messages.
	 */
	static final int			MAX_TICKS					= 50;
	/**
	 * Template for registration messages.
	 * <p>
	 * Alternative: <code>
	 * new MatchExpression() {
	 *  &#64;Override
	 *  public boolean match(ACLMessage msg) {
	 *  	return (msg.getPerformative() == ACLMessage.INFORM && msg.getProtocol().equals("register-child"));
	 *  }}
	 * </code>
	 */
	static MessageTemplate		registrationReceiptTemplate	= MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchProtocol(REGISTRATION_PROTOCOL));
	static MessageTemplate		upwardReceiptTemplate	= MessageTemplate.and(
			MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchProtocol(UP_PROTOCOL)),
			MessageTemplate.MatchReplyWith(REPLY_ID));
	static MessageTemplate		downwardReceiptTemplate	= MessageTemplate.and(
			MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchProtocol(DOWN_PROTOCOL)),
			MessageTemplate.MatchInReplyTo(REPLY_ID));
	/**
	 * Known child agents.
	 */
	List<AID>					childAgents					= new LinkedList<>();
	/**
	 * The ID of the parent.
	 */
	AID							parentAID					= null;
	/**
	 * The value associated to the agent.
	 */
	int							agentValue;
	int 						globalMaxValue;
	Map<AID, Integer> childValues = new HashMap<>();
	
	/**
	 * @param childAID
	 *            the ID of the child to add.
	 */
	public void addChildAgent(AID childAID) {
		childAgents.add(childAID);
	}
	
	/**
	 * @return the list of IDs of child agents.
	 */
	public List<AID> getChildAgents() {
		return childAgents;
	}
	
	@SuppressWarnings("serial")
	@Override
	protected void setup() {
		parentAID = (AID) getArguments()[0];
		agentValue = ((Integer) getArguments()[1]).intValue();
		
		System.out.println("Hello from agent: " + getAID().getName() + " with parentAID: " + parentAID);
		
		// add the behavior that sends the registration message to the parent
		if(parentAID != null) {
			System.out.println("Registration sender behavior for this agent starts in 1 second");
			addBehaviour(new WakerBehaviour(this, 1000) {
				@Override
				protected void onWake() {
					// Create the registration message as a simple INFORM message
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setProtocol(REGISTRATION_PROTOCOL);
					msg.setConversationId("registration-" + myAgent.getName());
					msg.addReceiver(parentAID);
					
					myAgent.send(msg);
				}
				
				@Override
				public int onEnd() {
					System.out.println("Agent " + myAgent.getAID().getLocalName() + " has sent registration message to "
							+ parentAID.getLocalName());
					return super.onEnd();
				}
			});
		}
		else
			System.out.println("Registration sender behavior need not start for agent " + getAID().getName());
		
		// add the RegistrationReceiveBehavior
		addBehaviour(new TickerBehaviour(this, TICK_PERIOD) {
			@Override
			protected void onTick() {
				ACLMessage receivedMsg = myAgent.receive(registrationReceiptTemplate);
				// register the agent if message received
				if(receivedMsg != null) {
					AID childAID = receivedMsg.getSender();
					((MyAgent) myAgent).addChildAgent(childAID);
				}
				// if number of ticks surpassed, take down the agent
				if(getTickCount() >= MAX_TICKS) {
					stop();
					// TODO: comment this out once you add the other behaviors as well
//					myAgent.doDelete();
					//UP
					if(childAgents.size() == 0) {
						//leaf send value
						addBehaviour(new OneShotBehaviour(myAgent) {
							@Override
							public void action() {
								sendUpMessage(myAgent, agentValue);
								addBehaviour(new SimpleBehaviour(myAgent) {
									@Override
									public void action() {
										ACLMessage receivedMsg = myAgent.receive(downwardReceiptTemplate);
										if(receivedMsg != null) {
											globalMaxValue = Integer.parseInt(receivedMsg.getContent());
											myAgent.doDelete();
										}
										else {
											block();
										}
									}

									@Override
									public boolean done() {
										return false;
									}
								});
							}

//							@Override
//							public int onEnd() {
//								System.out.println("Leaf agent " + myAgent.getAID().getLocalName() + " has sent its value " + agentValue + " to "
//										+ parentAID.getLocalName());
//								return super.onEnd();
//							}
						});
					}
					else {
						// node waits for childs value -> send value (if not root)
						addBehaviour(new SimpleBehaviour(myAgent) {
							@Override
							public void action() {
								ACLMessage receivedMsg = myAgent.receive(upwardReceiptTemplate);
								if(receivedMsg != null) {
									int childValue = Integer.parseInt(receivedMsg.getContent());
									AID childAID = receivedMsg.getSender();
									childValues.put(childAID, childValue);
									if(childValues.size() == childAgents.size()) {
										globalMaxValue = Collections.max(childValues.values());
										globalMaxValue = Math.max(globalMaxValue, agentValue);

										if(parentAID == null) {
											addBehaviour(new OneShotBehaviour(myAgent) {
												@Override
												public void action() {
													for(AID childAID : childAgents) {
														sendDownMessage(myAgent, childAID);
													}
													myAgent.doDelete();
												}

//												@Override
//												public int onEnd() {
//													System.out.println("Root agent " + myAgent.getAID().getLocalName() + " has sent global max value " + globalMaxValue + "  to childs "
//															+ childAgents);
//													return super.onEnd();
//												}
											});
										}
										else {
											// send value to parent
											addBehaviour(new OneShotBehaviour(myAgent) {
												@Override
												public void action() {
													sendUpMessage(myAgent, globalMaxValue);
													// DOWN
													addBehaviour(new SimpleBehaviour(myAgent) {
														@Override
														public void action() {
															ACLMessage receivedMsg = myAgent.receive(downwardReceiptTemplate);
															if(receivedMsg != null) {
																globalMaxValue = Integer.parseInt(receivedMsg.getContent());
																addBehaviour(new OneShotBehaviour(myAgent) {
																	@Override
																	public void action() {
																		for (AID childAID : childAgents) {
																			sendDownMessage(myAgent, childAID);
																		}
																		myAgent.doDelete();
																	}

//																	@Override
//																	public int onEnd() {
//																		System.out.println("Agent " + myAgent.getAID().getLocalName() + " has sent  global max value " + globalMaxValue + "  to "
//																				+ childAgents);
//																		return super.onEnd();
//																	}
																});
															}
															else {
																block();
															}
														}

														@Override
														public boolean done() {
															return false;
														}

													});
												}

//												@Override
//												public int onEnd() {
//													System.out.println("Agent " + myAgent.getAID().getLocalName() + " has sent  max value (until now)" + globalMaxValue + " value message to "
//															+ parentAID.getLocalName());
//													return super.onEnd();
//												}
											});
										}
									}
								}
								else {
									block();
								}
							}

							@Override
							public boolean done() {
								return false;
							}
						});
					}

				}
			}
		});
	}

	private void sendUpMessage(Agent myAgent, int value) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol(UP_PROTOCOL);
		msg.setConversationId("send local value -" + myAgent.getName());
		msg.addReceiver(parentAID);
		msg.setContent(String.valueOf(value));
		msg.setReplyWith(REPLY_ID);
		myAgent.send(msg);
	}

	private void sendDownMessage(Agent myAgent, AID childAID) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol(DOWN_PROTOCOL);
		msg.setConversationId("send global value -" + myAgent.getName());
		msg.addReceiver(childAID);
		msg.setContent(String.valueOf(globalMaxValue));
		msg.setInReplyTo(REPLY_ID);
		myAgent.send(msg);
	}
	
	@Override
	protected void takeDown() {
		String out = "Agent " + getAID().getLocalName() + " has the following children: ";
		for(AID childAID : childAgents)
			out += childAID.getLocalName() + "  ";
		out += ". The max value is: " + globalMaxValue + "\n";
		System.out.println(out);
	}
}
