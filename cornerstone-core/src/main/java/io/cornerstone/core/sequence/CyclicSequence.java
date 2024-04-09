package io.cornerstone.core.sequence;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface CyclicSequence extends Sequence {

	@Override
	default int nextIntValue() {
		return Integer.parseInt(nextStringValue().substring(getCycleType().getPattern().length()));
	}

	CycleType getCycleType();

	enum CycleType {

		MINUTE("yyyyMMddHHmm") {
			@Override
			public boolean isSameCycle(LocalDateTime last, LocalDateTime now) {
				return HOUR.isSameCycle(last, now) && (now.getMinute() == last.getMinute());

			}

			@Override
			public LocalDateTime skipCycles(LocalDateTime datetime, int cycles) {
				return datetime.plusMinutes(cycles);
			}

			@Override
			public LocalDateTime getCycleStart(LocalDateTime datetime) {
				return datetime.withSecond(0).withNano(0);
			}

		},
		HOUR("yyyyMMddHH") {

			@Override
			public boolean isSameCycle(LocalDateTime last, LocalDateTime now) {
				return DAY.isSameCycle(last, now) && (now.getHour() == last.getHour());
			}

			@Override
			public LocalDateTime skipCycles(LocalDateTime datetime, int cycles) {
				return datetime.plusHours(cycles);
			}

			@Override
			public LocalDateTime getCycleStart(LocalDateTime datetime) {
				return MINUTE.getCycleStart(datetime).withMinute(0);
			}

		},
		DAY("yyyyMMdd") {
			@Override
			public boolean isSameCycle(LocalDateTime last, LocalDateTime now) {
				return MONTH.isSameCycle(last, now) && (now.getDayOfYear() == last.getDayOfYear());
			}

			@Override
			public LocalDateTime skipCycles(LocalDateTime datetime, int cycles) {
				return datetime.plusDays(cycles);
			}

			@Override
			public LocalDateTime getCycleStart(LocalDateTime datetime) {
				return HOUR.getCycleStart(datetime).withHour(0);
			}

		},
		MONTH("yyyyMM") {
			@Override
			public boolean isSameCycle(LocalDateTime last, LocalDateTime now) {
				return YEAR.isSameCycle(last, now) && (now.getMonth() == last.getMonth());
			}

			@Override
			public LocalDateTime skipCycles(LocalDateTime datetime, int cycles) {
				return datetime.plusMonths(cycles);
			}

			@Override
			public LocalDateTime getCycleStart(LocalDateTime datetime) {
				return DAY.getCycleStart(datetime).withDayOfMonth(1);
			}

		},
		YEAR("yyyy") {
			@Override
			public boolean isSameCycle(LocalDateTime last, LocalDateTime now) {
				return (now.getYear() == last.getYear());
			}

			@Override
			public LocalDateTime skipCycles(LocalDateTime datetime, int cycles) {
				return datetime.plusYears(cycles);
			}

			@Override
			public LocalDateTime getCycleStart(LocalDateTime datetime) {
				return MONTH.getCycleStart(datetime).withMonth(1);
			}

		};

		private final String pattern;

		private final DateTimeFormatter formatter;

		CycleType(String pattern) {
			this.pattern = pattern;
			this.formatter = DateTimeFormatter.ofPattern(pattern);
		}

		public String getPattern() {
			return this.pattern;
		}

		public String format(LocalDateTime datetime) {
			return this.formatter.format(datetime);
		}

		public LocalDateTime getCycleEnd(LocalDateTime datetime) {
			return skipCycles(getCycleStart(datetime), 1).minusNanos(1);
		}

		public abstract LocalDateTime getCycleStart(LocalDateTime datetime);

		public abstract LocalDateTime skipCycles(LocalDateTime datetime, int cycles);

		public abstract boolean isSameCycle(LocalDateTime last, LocalDateTime now);

	}

}
