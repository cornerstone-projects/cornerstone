package com.example.demo.core.coordination;

public interface LeaderChangeListener {

	boolean supports(String group);

	void notLeader();

	void isLeader();

}
