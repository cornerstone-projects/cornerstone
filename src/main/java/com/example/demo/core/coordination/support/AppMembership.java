package com.example.demo.core.coordination.support;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.core.Application;
import com.example.demo.core.coordination.Membership;

@Component
public class AppMembership {

	private final String group;

	private final Membership membership;

	public AppMembership(Application application, Membership membership) {
		this.membership = membership;
		this.group = application.getName();
		this.membership.join(group);
	}

	public boolean isLeader() {
		return membership.isLeader(group);
	}

	public String getLeader(String group) {
		return membership.getLeader(group);
	}

	public List<String> getMembers(String group) {
		return membership.getMembers(group);
	}

}
