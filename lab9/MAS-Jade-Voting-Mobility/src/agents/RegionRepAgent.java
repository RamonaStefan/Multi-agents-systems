package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetInitiator;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;
import jade.wrapper.ControllerException;
import platform.Log;
import votes.VoteReader;
import votes.VotingData;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/**
 * The Region Representative Agent.
 */
public class RegionRepAgent extends Agent
{
	/**
	 * The serial UID.
	 */
	private static final long	serialVersionUID	= 2081456560111009192L;
	/**
	 * Known election manager agent
	 */
	AID							electionManagerAgent;
	/**
	 * key name for the region vote results from json input file
	 */
	String 						regionVoteKey;
	/**
	 * Vote result for this region.
	 */
	VotingData 					voteResult;
	
	@Override
	protected void setup()
	{
		Log.log(this, "Hello from RegionRepresantative: " + this.getLocalName());
		Log.log(this, "Adding DF subscribe behaviors");
		
		regionVoteKey = (String)getArguments()[0];
		
		AID dfAgent = getDefaultDF();
		Log.log(this, "Default DF Agent: " + dfAgent);
		
		// Create election service discovery behavior
		// Build the DFAgentDescription which holds the service descriptions for the the ambient-agent service
		
		VoteReader voteReader = new VoteReader();
		voteResult = voteReader.getVoteResult(regionVoteKey); 
		
		
		DFAgentDescription DFDesc = new DFAgentDescription();
		ServiceDescription serviceDesc = new ServiceDescription();
		serviceDesc.setType(ServiceType.ELECTION_MANAGEMENT);
		DFDesc.addServices(serviceDesc);
		
		SearchConstraints cons = new SearchConstraints();
		cons.setMaxResults(Long.valueOf(1));
		
		// add sub behavior for ambient-agent service discovery
		this.addBehaviour(new SubscriptionInitiator(this,
				DFService.createSubscriptionMessage(this, dfAgent, DFDesc, cons)) {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void handleInform(ACLMessage inform)
			{
				Log.log(myAgent, "Notification received from DF");
				
				try
				{
					DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
					if(results.length > 0)
						for(DFAgentDescription dfd : results)
						{
							AID provider = dfd.getName();
							// The same agent may provide several services; we are interested
							// in the election-management one
							for(Iterator it = dfd.getAllServices(); it.hasNext();)
							{
								ServiceDescription sd = (ServiceDescription) it.next();
								if(sd.getType().equals(ServiceType.ELECTION_MANAGEMENT))
								{
									Log.log(myAgent, ServiceType.ELECTION_MANAGEMENT, "service found: Service \"", sd.getName(),
											"\" provided by agent ", provider.getName());
									addServiceAgent(ServiceType.ELECTION_MANAGEMENT, provider);
									
									// if we found the ElectionManager we can cancel the subscription
									cancel(inform.getSender(), true);
								}
							}
						}
				} 
				catch(FIPAException | ControllerException fe)
				{
					fe.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * This method will be called when all the needed agents have been discovered.
	 */
	protected void onDiscoveryCompleted() throws ControllerException {
		// TODO: add the RequestInitiator behavior for asking the VoteCollectorAgent to come collect the results
		ACLMessage requestPreferenceMsg = new ACLMessage(ACLMessage.REQUEST);
		requestPreferenceMsg.addReceiver(electionManagerAgent);
		requestPreferenceMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		requestPreferenceMsg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
		requestPreferenceMsg.setContent("send the vote collectors - " + getContainerController().getContainerName());
		addBehaviour(new AchieveREInitiator(this, requestPreferenceMsg) {
			protected void handleAllResultNotifications(Vector notifications) {
				if (notifications.size() < 1) {
					System.out.println("Asking ElectionManager to send the vote collectors...Timeout expired: missing 1 response");
					return;
				}

				ACLMessage responseMessage = (ACLMessage) notifications.get(0);
				if (responseMessage.getPerformative() == ACLMessage.INFORM) {
					System.out.println("Agent " + this.myAgent.getName() + " received confirmation from ElectionManager that VoteCollector will come");
					MessageTemplate template = MessageTemplate.and(
							MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
							MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );


					addBehaviour(new AchieveREResponder(this.myAgent, template) {
						protected ACLMessage prepareResponse(ACLMessage request) throws RefuseException {
							System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());
							if (checkAction(request.getContent())) {
								ACLMessage inform = request.createReply();
								inform.setPerformative(ACLMessage.INFORM);
								try {
									inform.setContentObject(voteResult);
								} catch (IOException e) {
									e.printStackTrace();
								}
								String content = request.getContent();
								String[] infoRequest = content.split("-");
								String containerName = infoRequest[1].trim();
								this.myAgent.doMove(new ContainerID(containerName, null));

								return inform;
							}
							else {
								System.out.println("Agent "+getLocalName()+": Refuse");
								throw new RefuseException("check-failed");
							}
						}
					} );
				}
				else if (responseMessage.getPerformative() == ACLMessage.REFUSE) {
					System.out.println("Agent " + this.myAgent.getName() + " received refusal from ElectionManager");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					ACLMessage requestPreferenceMsg = new ACLMessage(ACLMessage.REQUEST);
					requestPreferenceMsg.addReceiver(electionManagerAgent);
					requestPreferenceMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
					requestPreferenceMsg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
					try {
						requestPreferenceMsg.setContent("send the vote collectors - " + this.myAgent.getContainerController().getContainerName());
					} catch (ControllerException e) {
						e.printStackTrace();
					}
					addBehaviour(new AchieveREInitiator(this.myAgent, requestPreferenceMsg) {
						protected void handleAllResultNotifications(Vector notifications) {
							if (notifications.size() < 1) {
								System.out.println("Asking ElectionManager to send the vote collectors...Timeout expired: missing 1 response");
								return;
							}
							else if (responseMessage.getPerformative() == ACLMessage.REFUSE) {
								System.out.println("Agent " + this.myAgent.getName() + " received refusal from ElectionManager");
							}
							doDelete();
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
	public void addServiceAgent(String serviceType, AID agent) throws ControllerException {
		
		if(serviceType.equals(ServiceType.ELECTION_MANAGEMENT))
		{
			if(electionManagerAgent != null)
				Log.log(this, "Warning: a second election manager agent found.");
			electionManagerAgent = agent;
		}
		
		if(electionManagerAgent != null)
			onDiscoveryCompleted();
	}

	private boolean checkAction(String content) {
		String[] infoRequest = content.split("-");
		return infoRequest.length == 2;
	}

	@Override
	protected void takeDown()
	{
		// Printout a dismissal message
		Log.log(this, "terminating.");
	}

}
