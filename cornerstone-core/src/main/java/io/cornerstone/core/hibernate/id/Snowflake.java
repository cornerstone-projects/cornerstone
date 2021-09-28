package io.cornerstone.core.hibernate.id;

import java.util.concurrent.ThreadLocalRandom;

import lombok.Value;

public class Snowflake {

	private static final long EPOCH = 1556150400000L;
	private final int workerId;
	private final int workerIdBits;
	private final int sequenceBits;
	private final long sequenceMask;
	private long sequence = 0L;
	private long lastTimestamp = -1L;

	public Snowflake(int workerId) {
		this(workerId, 8, 10);
	}

	public Snowflake(int workerId, int workerIdBits, int sequenceBits) {
		long maxWorkerId = -1L ^ (-1L << workerIdBits);
		if ((workerId > maxWorkerId) || (workerId < 0)) {
			throw new IllegalArgumentException(
					String.format("workerId can't be greater than %d or less than 0", maxWorkerId));
		}
		this.workerId = workerId;
		this.workerIdBits = workerIdBits;
		this.sequenceBits = sequenceBits;
		this.sequenceMask = -1L ^ (-1L << sequenceBits);
	}

	public synchronized long nextId() {
		long timestamp = System.currentTimeMillis();
		if (timestamp == this.lastTimestamp) {
			this.sequence = (this.sequence + 1) & this.sequenceMask;
			if (this.sequence == 0) {
				timestamp = System.currentTimeMillis();
				while (timestamp <= this.lastTimestamp) {
					timestamp = System.currentTimeMillis();
				}
			}
		} else if (timestamp > this.lastTimestamp) {
			this.sequence = ThreadLocalRandom.current().nextInt(2);
		} else {
			long offset = this.lastTimestamp - timestamp;
			if (offset < 5000) {
				try {
					this.wait(offset + 1);
					timestamp = System.currentTimeMillis();
					this.sequence = ThreadLocalRandom.current().nextInt(2);
				} catch (InterruptedException ex) {
					throw new IllegalStateException(ex);
				}
			} else {
				throw new IllegalStateException(
						String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", offset));
			}
		}
		this.lastTimestamp = timestamp;
		return ((timestamp - EPOCH) << (this.sequenceBits + this.workerIdBits)) | (this.workerId << this.sequenceBits)
				| this.sequence;
	}

	public Info parse(long id) {
		return new Info(id, this.workerIdBits, this.sequenceBits);
	}

	@Value
	public static class Info {
		private long timestamp;
		private int workerId;
		private long sequence;

		Info(long id, int workerIdBits, int sequenceBits) {
			long duration = id >> (sequenceBits + workerIdBits);
			this.timestamp = EPOCH + duration;
			this.workerId = (int) ((id - (duration << (sequenceBits + workerIdBits))) >> (sequenceBits));
			this.sequence = id - (duration << (sequenceBits + workerIdBits)) - (this.workerId << sequenceBits);
		}
	}

}
