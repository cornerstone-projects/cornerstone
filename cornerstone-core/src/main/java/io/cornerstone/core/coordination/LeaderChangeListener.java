package io.cornerstone.core.coordination;

public interface LeaderChangeListener {

	boolean supports(String group);

	void notLeader();

	void isLeader();

}
