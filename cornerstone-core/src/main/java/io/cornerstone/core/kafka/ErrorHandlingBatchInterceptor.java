package io.cornerstone.core.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.log.LogAccessor;
import org.springframework.kafka.listener.BatchInterceptor;
import org.springframework.kafka.support.KafkaUtils;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.SerializationUtils;

public class ErrorHandlingBatchInterceptor<K, V> implements BatchInterceptor<K, V> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public ConsumerRecords<K, V> intercept(ConsumerRecords<K, V> records, Consumer<K, V> consumer) {
		for (TopicPartition partition : records.partitions()) {
			for (ConsumerRecord<K, V> record : records.records(partition)) {
				if (record.value() == null) {
					DeserializationException exception = SerializationUtils.getExceptionFromHeader(record,
							KafkaUtils.VALUE_DESERIALIZER_EXCEPTION_HEADER, new LogAccessor(getClass()));
					if (exception != null) {
						handleError(record, exception);
					}
				}
			}
		}
		return records;
	}

	void handleError(ConsumerRecord<K, V> record, Exception exception) {
		this.logger
			.error(String.format("failed to deserialize value of ConsumerRecord(topic = %s, partition = %d, key = %s)",
					record.topic(), record.partition(), record.key()), exception);
	}

}
