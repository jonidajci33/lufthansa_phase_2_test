package com.planningpoker.identity.application.port.out;

import com.planningpoker.identity.domain.User;

/**
 * Secondary (driven) port for publishing user-related domain events.
 * The infrastructure adapter translates these calls into Kafka messages
 * on the {@code identity.user.events} topic.
 */
public interface UserEventPublisherPort {

    void publishUserRegistered(User user);

    void publishUserUpdated(User user);

    void publishUserDeactivated(User user);
}
