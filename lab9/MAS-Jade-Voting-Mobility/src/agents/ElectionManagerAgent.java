package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import platform.Log;
import votes.BallotGroup;
import votes.VotingData;

import javax.swing.*;

/**
 * ElectionManager agent.
 */
public class ElectionManagerAgent extends Agent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3397689918969697329L;
	
	/**
	 * The AID of the VoteCollector agent used by this ElectionManager
	 */
	AID					voteCollector;
	
	/**
	 * The names of the regions from which the ElectionManager awaits vote results collected by the VoteCollector. 
	 */
	List<String>		regionVoteNames;
	private static int numberVotes = 0;
	
	@SuppressWarnings("unchecked")
	@Override
	public void setup()
	{
		Log.log(this, "Hello");
		
		// get the AID of the vote collector agent
		voteCollector = (AID)getArguments()[0];
		Log.log(this, "My vote collector agent is: " + voteCollector);
		
		// get the list of region names (keys in the json file) from which the ElectionManager awaits votes
		regionVoteNames = (List<String>)getArguments()[1];
		Log.log(this, "Awaiting votes from the following regions: " + regionVoteNames);
		
		// Register the election manager agent service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(ServiceType.ELECTION_MANAGEMENT);
		sd.setName("election-management");
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
				ACLMessage inform = request.createReply();
				inform.setPerformative(ACLMessage.INFORM);
				if (checkAction(request.getContent())) {
					String content = request.getContent();
					String[] infoRequest = content.split("-");
					String containerName = infoRequest[1].trim();

					ACLMessage requestPreferenceMsg = new ACLMessage(ACLMessage.REQUEST);
					requestPreferenceMsg.addReceiver(voteCollector);
					requestPreferenceMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
					requestPreferenceMsg.setReplyByDate(new Date(System.currentTimeMillis() + 60000));
					requestPreferenceMsg.setContent(containerName + "--" + request.getSender().getLocalName());
					addBehaviour(new AchieveREInitiator(this.myAgent, requestPreferenceMsg) {
						protected void handleAllResultNotifications(Vector notifications) {
							if (notifications.size() < 1) {
								System.out.println("Asking VoteCollector to collect votes...Timeout expired: missing 1 response");
								return;
							}

							System.out.println("Asking VoteCollector to collect votes done");
							ACLMessage responseMessage = (ACLMessage) notifications.get(0);
							try {
								if (responseMessage.getContentObject() != null) {
									System.out.println("Agent " + this.myAgent.getName() + " received votes from VoteCollector");
									VotingData votes = null;
									try {
										votes = (VotingData) responseMessage.getContentObject();
									} catch (UnreadableException e) {
										e.printStackTrace();
									}
									System.out.println(votes);
									SVT(votes);
									if (numberVotes == 4) {
										System.out.println("DONE");
										doDelete();
									}
								}
							} catch (UnreadableException e) {
								e.printStackTrace();
							}
						}
					});
				}
				else {
						System.out.println("Agent "+getLocalName()+": Refuse");
						throw new RefuseException("check-failed");
				}
				return inform;
			}

		} );



	}

	private boolean checkAction(String content) {
		String[] infoRequest = content.split("-");
		return infoRequest.length == 2;
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
	public void SVT(VotingData votes) {
		double drop = Math.floor(250.0/(5 + 1)) + 1;
		List<String> winners = new ArrayList<>();
		Map<String, Integer> totalVotesFirstChoice;
		int pos = 0;
		totalVotesFirstChoice = getVotesForCandidatesOnPosition((List<BallotGroup>) votes.getBallots(), 0);
		do {
			String maxBal;
			if(totalVotesFirstChoice.keySet().size() > 1) {
				maxBal = totalVotesFirstChoice.entrySet().stream().filter(r -> !winners.contains(r.getKey())).max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
			}
			else if(totalVotesFirstChoice.keySet().size() == 1) {
				maxBal = new ArrayList<>(totalVotesFirstChoice.keySet()).get(0);
				if(winners.contains(maxBal)) {
					break;
				}
			}
			else {
				List<String> candidates = new ArrayList<>();
				for(BallotGroup b : votes.getBallots()) {
					candidates.addAll(b.getCandidates());
					break;
				}
				candidates.removeAll(winners);
				if (candidates.size() == 0) {
					break;
				}
				maxBal = candidates.get(0);
			}
			int max = totalVotesFirstChoice.get(maxBal);
			java.util.List<BallotGroup> bg = (List<BallotGroup>) votes.getBallots();
			if (max < drop) {
				String candName = getMinimumCandidate(totalVotesFirstChoice);
				BallotGroup ballotToRemove = null;
				for (BallotGroup ballot : bg) {
					if (ballot.getCandidates().get(0).equals(candName)) {
						ballotToRemove = ballot;
						transferUnused(ballot, totalVotesFirstChoice, 1);
					}
				}
				bg.remove(ballotToRemove);
				totalVotesFirstChoice.remove(max);
			}

			double surplus = totalVotesFirstChoice.get(maxBal) - drop;
			winners.add(maxBal);
			transferSurplus(bg, maxBal, surplus, totalVotesFirstChoice);
			pos++;
		}while (winners.size() < 5);
		System.out.println(winners);
	}

	public void transferSurplus(List<BallotGroup> bg, String max, double surplus, Map<String, Integer> totalVotesFirstChoice) {
		Map<String, Integer> newVotes = getVotesForCandidatesOnPosition(bg, 1);
		List<BallotGroup> ballotToRedis = new ArrayList<>();

		for (BallotGroup ballot : bg) {
			if (ballot.getCandidates().get(0).equals(max)) {
				ballotToRedis.add(ballot);
			}
		}

		for (BallotGroup ballot : ballotToRedis) {
			String name = ballot.getCandidates().get(1);
			int newVote = (int) (newVotes.get(name) / totalVotesFirstChoice.get(max) * surplus);
			if (totalVotesFirstChoice.containsKey(name)) {
				int value = totalVotesFirstChoice.get(name);
				value += newVote;
				totalVotesFirstChoice.put(name, value);
			}
			else {
				totalVotesFirstChoice.put(name, newVote);
			}
		}
		totalVotesFirstChoice.remove(max);
		bg.removeAll(ballotToRedis);
	}

	public void transferUnused(BallotGroup ballot, Map<String, Integer> totalVotesFirstChoice, int nextCandidate) {
		String cand = ballot.getCandidates().get(nextCandidate);
		if (totalVotesFirstChoice.containsKey(cand)) {
			int value = totalVotesFirstChoice.get(cand);
			value += ballot.getCount();
			totalVotesFirstChoice.put(cand, value);
		}
		else {
			totalVotesFirstChoice.put(cand, ballot.getCount());
		}
	}

	public Map<String, Integer> getVotesForCandidatesOnPosition(List<BallotGroup> votes, int position) {
		Map<String, Integer> totalVotesFirstChoice = new HashMap<>();
		for(BallotGroup ballot:votes) {
			String key = ballot.getCandidates().get(position);
			Integer sum = ballot.getCount();
			if (totalVotesFirstChoice.containsKey(key)) {
				int value = totalVotesFirstChoice.get(key);
				value += sum;
				totalVotesFirstChoice.put(key, value);
			}
			else {
				totalVotesFirstChoice.put(key, sum);
			}
		}
		return totalVotesFirstChoice;
	}

	public String getMinimumCandidate(Map<String, Integer> votes) {
		return votes.entrySet().stream().min((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
	}

}
