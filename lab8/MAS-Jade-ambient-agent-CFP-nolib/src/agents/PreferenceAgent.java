package agents;

import FIPA.DateTime;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import platform.Log;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;

import java.time.LocalTime;
import java.util.Random;


/**
 * Preference agent.
 */
public class PreferenceAgent extends Agent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3397689918969697329L;
	private static final String[] wakeUpModes = { "soft", "super-soft", "hard"};

	@Override
	public void setup()
	{
		Log.log(this, "Hello");
		
		// Register the preference-agent service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		
		ServiceDescription sd = new ServiceDescription();
		sd.setType(ServiceType.PREFERENCE_AGENT);
		sd.setName("ambient-wake-up-call");
		dfd.addServices(sd);
		try
		{
			DFService.register(this, dfd);
		} catch(FIPAException fe)
		{
			fe.printStackTrace();
		}


		// TODO add behaviors
		System.out.println("Agent "+getLocalName()+" waiting for requests...");
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );

		addBehaviour(new AchieveREResponder(this, template) {
			protected ACLMessage prepareResponse(ACLMessage request) throws RefuseException {
				System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());
				if (checkAction(request.getContent())) {
					System.out.println("Agent "+getLocalName()+": Action successfully performed");
					ACLMessage inform = request.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					String content = request.getContent();
					String[] infoRequest = content.split(" ");
					String day = infoRequest[0];
					LocalTime time = LocalTime.parse(infoRequest[1]);
					inform.setContent(getPreferenceMode(day, time));
					return inform;
				}
				else {
					System.out.println("Agent "+getLocalName()+": Refuse");
					throw new RefuseException("check-failed");
				}
			}
		} );

	}

	private boolean checkAction(String content) {
		String[] infoRequest = content.split(" ");
		return infoRequest.length == 2;
	}

	private String getPreferenceMode(String day, LocalTime timeOfDay) {
		switch(day) {
			case "Monday":
			case "Tuesday":
			case "Wednesday":
			case "Thursday":
			case "Friday": {
				if (timeOfDay.compareTo(LocalTime.parse("09:00:00")) > 0) { // trecut de 9
					return "hard";
				}
				else if (timeOfDay.compareTo(LocalTime.parse("08:00:00")) > 0) {
					return "soft";
				}
				else {
					return "super-soft";
				}
			}
			case "Saturday":
			case "Sunday": {
				if (timeOfDay.compareTo(LocalTime.parse("10:00:00")) > 0) { // trecut de 10
					return "hard";
				}
				else if (timeOfDay.compareTo(LocalTime.parse("09:00:00")) > 0) {
					return "soft";
				}
				else {
					return "super-soft";
				}
			}
		}
		return "super-soft";
	}
	
	@Override
	protected void takeDown()
	{
		// De-register from the yellow pages
		try
		{
			DFService.deregister(this);
		} catch(FIPAException fe)
		{
			fe.printStackTrace();
		}
		
		// Printout a dismissal message
		Log.log(this, "terminating.");
	}
}
