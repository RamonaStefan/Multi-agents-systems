package votes;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * The result of a voting process, as a set of ballots (grouped into ballot groups with identical ballots).
 */
public class VotingData implements Serializable {
	/**
	 * The serial UID.
	 */
	private static final long	serialVersionUID	= 1L;
	/**
	 * The ballot groups.
	 */
	List<BallotGroup>			ballots;
	
	/**
	 * Constructor.
	 */
	public VotingData() {
		
	}
	
	/**
	 * @return the ballots
	 */
	public Collection<BallotGroup> getBallots() {
		return ballots;
	}
	
	/**
	 * @param ballots
	 *            the ballots to set
	 */
	public void setBallots(List<BallotGroup> ballots) {
		this.ballots = ballots;
	}
	
}
