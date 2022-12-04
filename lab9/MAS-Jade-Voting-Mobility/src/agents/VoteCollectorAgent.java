package agents;

import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.wrapper.ControllerException;
import platform.Log;
import votes.VotingData;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

/**
 * The Vote Collector Agent.
 */
public class VoteCollectorAgent extends Agent {
	/**
	 * The serial UID.
	 */
	private static final long serialVersionUID = -4316893632718883072L;

	@Override
	public void setup() {
		Log.log(this, "Hello");
		
		// TODO add behaviors

		System.out.println("Agent "+getLocalName()+" waiting for requests...");
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );

		addBehaviour(new AchieveREResponder(this, template) {
			protected ACLMessage prepareResponse(ACLMessage request) throws RefuseException {
				System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());
				if (checkAction(request.getContent())) {
					String content = request.getContent();
					String[] infoRequest = content.split("--");
					String containerName = infoRequest[0].trim();
					String regionAgent = infoRequest[1].trim();
					String receiver = request.getSender().getLocalName();

					this.myAgent.doMove(new ContainerID(containerName, null));

					ACLMessage requestPreferenceMsg = new ACLMessage(ACLMessage.REQUEST);
					requestPreferenceMsg.addReceiver(getAID(regionAgent));
					requestPreferenceMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
					requestPreferenceMsg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
					try {
						requestPreferenceMsg.setContent("send votes - " + getContainerController().getContainerName());
					} catch (ControllerException e) {
						e.printStackTrace();
					}

					addBehaviour(new AchieveREInitiator(this.myAgent, requestPreferenceMsg) {
						protected void handleAllResultNotifications(Vector notifications) {
							if (notifications.size() < 1) {
								System.out.println("Asking Region to send votes...Timeout expired: missing 1 response");
								return;
							}

							// receive votes
							ACLMessage responseMessage = (ACLMessage) notifications.get(0);
							if (responseMessage.getPerformative() == ACLMessage.INFORM) {
								System.out.println("Agent " + this.myAgent.getName() + " received votes from Region");
								try {
									VotingData votes = (VotingData) responseMessage.getContentObject();
//									inform.setContentObject(votes);
									ACLMessage requestPreferenceMsg = request.createReply();
									requestPreferenceMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
									requestPreferenceMsg.setPerformative(ACLMessage.INFORM);

									requestPreferenceMsg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
									requestPreferenceMsg.setContentObject(votes);

									addBehaviour(new AchieveREInitiator(this.myAgent, requestPreferenceMsg) {
										protected void handleAllResultNotifications(Vector notifications) {
											System.out.println("Votes sent");
											doDelete();
										}
									});
								} catch (UnreadableException | IOException e) {
									e.printStackTrace();
								}
							}
						}
					});

					return null;
				}
				else {
					System.out.println("Agent "+getLocalName()+": Refuse");
					throw new RefuseException("check-failed");
				}
			}
		} );
	}

	private boolean checkAction(String content) {
		String[] infoRequest = content.split("--");
		return infoRequest.length == 2;
	}

	/**
	 * This method is executed just before moving the agent to another
	 * location. It is automatically called by the JADE framework.
	 * It disposes the GUI and prints a bye message on the standard output.
	 */
	protected void beforeMove()
	{
		System.out.println(getLocalName()+" is now moving elsewhere.");
	}

	/**
	 * This method is executed as soon as the agent arrives to the new
	 * destination.
	 * It creates a new GUI and sets the list of visited locations and
	 * the list of available locations (via the behaviour) in the GUI.
	 */
	protected void afterMove() {
		try {
			System.out.println(getLocalName()+" is just arrived to this location." + getContainerController().getContainerName());
		} catch (ControllerException e) {
			e.printStackTrace();
		}
		// Register again SL0 content language and JADE mobility ontology,
		// since they don't migrate.
		getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);
		getContentManager().registerOntology(MobilityOntology.getInstance());
		// get the list of available locations from the AMS.
	}

	@Override
	protected void takeDown() {
		Log.log(this, "terminating.");
	}
}
