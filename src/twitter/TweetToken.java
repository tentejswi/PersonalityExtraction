package twitter;

import java.util.ArrayList;

/*
 * Represents a part of the tweet text
 */
public class TweetToken {
	String token = null;
	ArrayList<String> entities = null;
	
	public TweetToken(String token, ArrayList<String> entities) {
		this.token = token;
		this.entities = entities;
	}
}
