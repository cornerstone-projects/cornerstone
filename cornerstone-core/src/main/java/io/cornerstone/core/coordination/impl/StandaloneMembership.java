package io.cornerstone.core.coordination.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.cornerstone.core.Application;
import io.cornerstone.core.coordination.Membership;

public class StandaloneMembership implements Membership {

	private final Map<String, List<String>> groups = new HashMap<>();

	private final String self = Application.current().map(Application::getInstanceId).orElse("");

	@Override
	public void join(String group) {
		List<String> members = this.groups.computeIfAbsent(group, k -> new ArrayList<>());
		if (!members.contains(this.self)) {
			members.add(this.self);
		}
	}

	@Override
	public void leave(String group) {
		List<String> members = this.groups.get(group);
		if (members != null) {
			members.remove(this.self);
		}
	}

	@Override
	public boolean isLeader(String group) {
		return this.self.equals(getLeader(group));
	}

	@Override
	public String getLeader(String group) {
		List<String> members = getMembers(group);
		if ((members == null) || members.isEmpty()) {
			return null;
		}
		else {
			return members.getFirst();
		}
	}

	@Override
	public List<String> getMembers(String group) {
		return this.groups.get(group);
	}

}
