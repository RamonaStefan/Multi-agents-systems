package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.proto.AchieveREResponder;
import platform.Log;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;

import java.util.Arrays;

/**
 * The PhoneAlarmAgent.
 */
public class PhoneAlarmAgent extends Agent {
	/**
	 * The serial UID.
	 */
	private static final long serialVersionUID = -4316893632718883072L;
	private static final String[] wakeUpModes = { "soft", "hard"};

	@Override
	public void setup() {
		Log.log(this, "Hello");

		// Register the ambient-agent service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());

		ServiceDescription sd = new ServiceDescription();
		sd.setType(ServiceType.AMBIENT_AGENT);
		sd.setName("ambient-wake-up-call");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		// TODO add behaviors
		System.out.println("Agent "+getLocalName()+" waiting for CFP...");
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP) );

		addBehaviour(new ContractNetResponder(this, template) {
			@Override
			protected ACLMessage handleCfp(ACLMessage cfp) {
				System.out.println("Agent "+getLocalName()+": CFP received from "+cfp.getSender().getName()+". Action is "+cfp.getContent());
				if (evaluateAction(cfp.getContent())) {
					System.out.println("Agent "+getLocalName()+": Proposing "+ cfp.getContent());
					ACLMessage propose = cfp.createReply();
					propose.setPerformative(ACLMessage.PROPOSE);
					propose.setContent(cfp.getContent());
					return propose;
				}
				else {
					System.out.println("Agent "+getLocalName()+": Refuse");
					ACLMessage propose = cfp.createReply();
					propose.setPerformative(ACLMessage.REFUSE);
					propose.setContent(cfp.getContent());
					return propose;
				}
			}

			@Override
			protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) {
				System.out.println("Agent "+getLocalName()+": Proposal accepted");
				ACLMessage inform = accept.createReply();
				inform.setPerformative(ACLMessage.INFORM);
				return null;
			}

			protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
				System.out.println("Agent "+getLocalName()+": Proposal rejected");
			}
		} );

		template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );

		addBehaviour(new AchieveREResponder(this, template) {
			protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
				System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());
				if (checkAction(request.getContent())) {
					System.out.println("Agent "+getLocalName()+": Action wake up successfully performed");
					ACLMessage inform = request.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					inform.setContent("done");
					return inform;
				}
				else {
					System.out.println("Agent "+getLocalName()+": Refuse");
					throw new RefuseException("check-failed");
				}
			}
		} );
	}

	private boolean evaluateAction(String content) {
		return Arrays.asList(wakeUpModes).contains(content);
	}

	private boolean checkAction(String content) {
		return content.equals("wake-up");
	}


	@Override
	protected void takeDown() {
		// Unregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Printout a dismissal message
		Log.log(this, "terminating.");
	}
}
