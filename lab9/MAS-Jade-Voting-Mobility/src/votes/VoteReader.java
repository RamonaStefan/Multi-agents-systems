package votes;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads votes from a file.
 */
public class VoteReader {
	/**
	 * The voting results, indexed by region.
	 */
	Map<String, VotingData> voteResults = new HashMap<>();
	
	/**
	 * Reads the voting results.
	 */
	public VoteReader() {
		readFile("data/region_votes.json");
	}
	
	/**
	 * @param filename the file to read the results from.
	 */
	private void readFile(String filename) {
		try (InputStream in = new FileInputStream(new File(filename))) {
            ObjectMapper mapper =  new ObjectMapper();

            final JsonNode regionVotesNode = mapper.readTree(in);
            
            int id = 1;
            for (JsonNode regionVoteNode : regionVotesNode) {
                String regionName = "region" + (id++);
                VotingData voteResult = mapper.treeToValue(regionVoteNode, VotingData.class);
                
                voteResults.put(regionName, voteResult);
            }

        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	
	/**
	 * @param regionName the region to get results for.
	 * @return the voting data.
	 */
	public VotingData getVoteResult(String regionName) {
		return voteResults.get(regionName);
	}
}

