package votes;

import java.io.Serializable;
import java.util.List;

/**
 * An instance of this class represents a group of identical ballots.
 */
public class BallotGroup implements Serializable {
	/**
	 * The serial UID.
	 */
	private static final long	serialVersionUID	= 1L;
	/**
	 * The number of identical ballots in this group.
	 */
	int							count;
	/**
	 * The candidates, in the order chosen on the ballot.
	 */
	List<String>				candidates;
	
	/**
	 * Constructor.
	 */
	public BallotGroup() {
	}
	
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}
	
	/**
	 * @param count
	 *            the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}
	
	/**
	 * @return the candidates
	 */
	public List<String> getCandidates() {
		return candidates;
	}
	
	/**
	 * @param candidates
	 *            the candidates to set
	 */
	public void setCandidates(List<String> candidates) {
		this.candidates = candidates;
	}
	
}
