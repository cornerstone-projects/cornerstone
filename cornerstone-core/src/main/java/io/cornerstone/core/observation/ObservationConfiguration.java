package io.cornerstone.core.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;
import net.ttddyy.observation.tracing.DataSourceBaseContext;

import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.observability.LettuceObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.ServerHttpObservationFilter;

@Configuration
@ConditionalOnEnabledTracing
public class ObservationConfiguration {

	@Bean
	ObserveTransactionalAspect observeTransactionalAspect(ObservationRegistry observationRegistry) {
		return new ObserveTransactionalAspect(observationRegistry);
	}

	@Bean
	RepositoryObservationAspect repositoryObservationAspect(ObservationRegistry observationRegistry) {
		return new RepositoryObservationAspect(observationRegistry);
	}

	@Bean
	ControllerObservationAspect controllerObservationAspect(ObservationRegistry observationRegistry) {
		return new ControllerObservationAspect(observationRegistry);
	}

	@Bean
	ObservationPredicate noSpringSecurityObservations() {
		return (name, context) -> !name.startsWith("spring.security.");
	}

	@Bean
	ObservationPredicate noActuatorServerObservations() {
		return (name, context) -> {
			if (name.equals("http.server.requests")
					&& context instanceof ServerRequestObservationContext serverContext) {
				return !serverContext.getCarrier().getRequestURI().startsWith("/actuator");
			}
			else {
				return true;
			}
		};
	}

	@Bean
	ObservationPredicate noRootlessHttpObservations() {
		return (name, context) -> {
			RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
			if (requestAttributes instanceof ServletRequestAttributes) {
				Observation observation = (Observation) requestAttributes.getAttribute(
						ServerHttpObservationFilter.class.getName() + ".observation", RequestAttributes.SCOPE_REQUEST);
				return observation == null || !observation.isNoop();
			}
			else {
				return true;
			}
		};
	}

	@Bean
	ObservationPredicate noParentlessDatabaseObservations() {
		return (name, context) -> {
			if (context instanceof LettuceObservationContext || context instanceof DataSourceBaseContext) {
				return context.getParentObservation() != null;
			}
			return true;
		};
	}

}
