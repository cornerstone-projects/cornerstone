package io.cornerstone.core.event;

import org.springframework.context.ApplicationEvent;

import io.cornerstone.core.message.Topic;

public interface ApplicationEventTopic extends Topic<ApplicationEvent> {

}
