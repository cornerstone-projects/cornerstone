package io.cornerstone.core.coordination.support;

import java.util.List;

import io.cornerstone.core.Application;
import io.cornerstone.core.coordination.Membership;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AppMembership {

	private final String group = Application.current().map(Application::getName).orElse("application");

	private final Membership membership;

	public AppMembership(Membership membership) {
		this.membership = membership;

	}

	public boolean isLeader() {
		return this.membership.isLeader(this.group);
	}

	public String getLeader(String group) {
		return this.membership.getLeader(group);
	}

	public List<String> getMembers(String group) {
		return this.membership.getMembers(group);
	}

	@EventListener
	void onEvent(AvailabilityChangeEvent<ReadinessState> event) {
		switch (event.getState()) {
			case ACCEPTING_TRAFFIC:
				this.membership.join(this.group);
				break;
			case REFUSING_TRAFFIC:
				this.membership.leave(this.group);
				break;
		}
	}

}
