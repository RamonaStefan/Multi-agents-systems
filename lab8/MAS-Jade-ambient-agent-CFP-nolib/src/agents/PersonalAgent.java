package agents;

import java.time.LocalTime;
import java.util.*;

import agents.behaviors.AmbientServiceDiscoveryBehavior;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import platform.Log;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.domain.FIPANames;
import jade.proto.AchieveREInitiator;

/**
 * The PersonalAgent.
 */
public class PersonalAgent extends Agent
{
	/**
	 * The serial UID.
	 */
	private static final long	serialVersionUID	= 2081456560111009192L;
	
	/**
	 * Known ambient agents.
	 */
	List<AID>					ambientAgents		= new LinkedList<>();
	
	/**
	 * Known preference agent
	 */
	AID							preferenceAgent;

	private static final String[] dayWeek = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	private String wakeUpModePreference = null;

	@Override
	protected void setup()
	{
		Log.log(this, "Hello from PersonalAgent");
		Log.log(this, "Adding DF subscribe behaviors");
		
		// Create a parallel behavior to handle the two DF subscriptions: one for the two ambient-agent and one for the
		// preference-agent services
		addBehaviour(new AmbientServiceDiscoveryBehavior(this, ParallelBehaviour.WHEN_ALL));
	}
	
	/**
	 * This method will be called when all the needed agents have been discovered.
	 */
	protected void onDiscoveryCompleted()
	{
		// TODO: add the RequestInitiator behavior for asking the PreferenceAgent about preferred wake up mode
		// request preference from PreferenceAgent
		ACLMessage requestPreferenceMsg = new ACLMessage(ACLMessage.REQUEST);
		requestPreferenceMsg.addReceiver(preferenceAgent);
		requestPreferenceMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		requestPreferenceMsg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
		int day = new Random().nextInt(7);
		requestPreferenceMsg.setContent("Monday " + LocalTime.parse("08:30:00"));

		addBehaviour(new AchieveREInitiator(this, requestPreferenceMsg) {
			protected void handleInform(ACLMessage inform) {
				System.out.println("Agent "+inform.getSender().getName()+" successfully answered with preferred wake up mode");
			}
			protected void handleRefuse(ACLMessage refuse) {
				System.out.println("Agent "+refuse.getSender().getName()+" refused to answer  with preferences");
			}
			protected void handleFailure(ACLMessage failure) {
				if (failure.getSender().equals(myAgent.getAMS())) {
					System.out.println("Responder does not exist");
				}
				else {
					System.out.println("Agent "+failure.getSender().getName()+" failed to answer with preferences");
				}
			}
			protected void handleAllResultNotifications(Vector notifications) {
				if (notifications.size() < 1) {
					System.out.println("Asking for preferences...Timeout expired: missing 1 response");
					return;
				}

				// receive preference
				ACLMessage responseMessage = (ACLMessage) notifications.get(0);
				if (responseMessage.getPerformative() == ACLMessage.INFORM) {
					wakeUpModePreference = responseMessage.getContent();
					System.out.println("Agent " + this.myAgent.getName() + " received wake up mode preference received: " + wakeUpModePreference);

					// cfp
					ACLMessage cfpMessage = new ACLMessage(ACLMessage.CFP);
					for (AID agent : ambientAgents) {
						cfpMessage.addReceiver(agent);
					}
					cfpMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
					cfpMessage.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
					cfpMessage.setContent(wakeUpModePreference);

					addBehaviour(new ContractNetInitiator(this.myAgent, cfpMessage) {

						protected void handlePropose(ACLMessage propose, Vector v) {
							System.out.println("Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
						}
						protected void handleRefuse(ACLMessage refuse) {
							System.out.println("Agent " + refuse.getSender().getName() + " refused");
						}
						protected void handleFailure(ACLMessage failure) {
							if (failure.getSender().equals(myAgent.getAMS())) {
								System.out.println("Responder does not exist");
							} else {
								System.out.println("Agent " + failure.getSender().getName() + " failed");
							}
						}
						protected void handleAllResponses(Vector responses, Vector acceptances) {
							if (responses.size() < 2) {
								System.out.println("CFP: Timeout expired: missing " + (2 - responses.size()) + " responses");
							}
							// Evaluate proposals
							AID bestProposer = null;
							Enumeration e = responses.elements();
							while (e.hasMoreElements()) {
								ACLMessage msg = (ACLMessage) e.nextElement();
								if (msg.getPerformative() == ACLMessage.PROPOSE) {
									ACLMessage reply = msg.createReply();
									if (bestProposer == null) {
										bestProposer = msg.getSender();
										System.out.println("Agent " + this.myAgent.getName() + " accepting proposal from responder " + bestProposer.getName());
										reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
									} else {
										reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
										System.out.println("Agent " + this.myAgent.getName() + " rejecting proposal from responder " + msg.getSender().getName());
									}
									acceptances.addElement(reply);
								}
							}

							// request to do wake up action
							ACLMessage wakeUpMessage = new ACLMessage(ACLMessage.REQUEST);
							wakeUpMessage.addReceiver(bestProposer);
							wakeUpMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
							wakeUpMessage.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
							wakeUpMessage.setContent("wake-up");

							addBehaviour(new AchieveREInitiator(this.myAgent, wakeUpMessage) {
								protected void handleInform(ACLMessage inform) {
									System.out.println("Agent " + inform.getSender().getName() + " successfully received the wake up call");
								}
								protected void handleRefuse(ACLMessage refuse) {
									System.out.println("Agent " + refuse.getSender().getName() + " refused to wake up");
								}
								protected void handleFailure(ACLMessage failure) {
									if (failure.getSender().equals(myAgent.getAMS())) {
										System.out.println("Responder does not exist");
									} else {
										System.out.println("Agent " + failure.getSender().getName() + " failed to wake up");
									}
								}
								protected void handleAllResultNotifications(Vector notifications) {
									if (notifications.size() < 1) {
										System.out.println("Wake up call: Timeout expired: missing 1 responses");
										return;
									}
									ACLMessage responseMessage = (ACLMessage) notifications.get(0);
									if (responseMessage.getPerformative() == ACLMessage.INFORM) {
										System.out.println("Agent " + responseMessage.getSender().getName() + " succesfully woke up");
									}
									else {
										System.out.println("Agent " + responseMessage.getSender().getName() + " didn't succesfully wake up");
									}
								}
							});
						}
					});
				}
			}
		});
	}

	/**
	 * Retains an agent provided a service.
	 * 
	 * @param serviceType
	 *            - the service type.
	 * @param agent
	 *            - the agent providing a service.
	 */
	public void addServiceAgent(String serviceType, AID agent)
	{
		if(serviceType.equals(ServiceType.AMBIENT_AGENT))
			ambientAgents.add(agent);
		if(serviceType.equals(ServiceType.PREFERENCE_AGENT))
		{
			if(preferenceAgent != null)
				Log.log(this, "Warning: a second preference agent found.");
			preferenceAgent = agent;
		}
		if(preferenceAgent != null && ambientAgents.size() >= 2)
			onDiscoveryCompleted();
	}
	
	@Override
	protected void takeDown()
	{
		// Printout a dismissal message
		Log.log(this, "terminating.");
	}
}
