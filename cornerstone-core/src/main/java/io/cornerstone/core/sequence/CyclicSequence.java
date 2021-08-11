package io.cornerstone.core.sequence;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public interface CyclicSequence extends Sequence {

	@Override
	default int nextIntValue() {
		return Integer.parseInt(nextStringValue().substring(getCycleType().getPattern().length()));
	}

	CycleType getCycleType();

	static enum CycleType {

		MINUTE("yyyyMMddHHmm") {
			@Override
			protected boolean isSameCycle(Calendar lastCal, Calendar nowCal) {
				return HOUR.isSameCycle(lastCal, nowCal) && nowCal.get(Calendar.MINUTE) == lastCal.get(Calendar.MINUTE);

			}

			@Override
			protected void skipCycles(Calendar cal, int cycles) {
				cal.add(Calendar.MINUTE, cycles);
			}

			@Override
			protected void skipToCycleStart(Calendar cal) {
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
			}

		},
		HOUR("yyyyMMddHH") {

			@Override
			protected boolean isSameCycle(Calendar lastCal, Calendar nowCal) {
				return DAY.isSameCycle(lastCal, nowCal)
						&& nowCal.get(Calendar.HOUR_OF_DAY) == lastCal.get(Calendar.HOUR_OF_DAY);
			}

			@Override
			protected void skipCycles(Calendar cal, int cycles) {
				cal.add(Calendar.HOUR_OF_DAY, cycles);
			}

			@Override
			protected void skipToCycleStart(Calendar cal) {
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
			}

		},
		DAY("yyyyMMdd") {
			@Override
			protected boolean isSameCycle(Calendar lastCal, Calendar nowCal) {
				return MONTH.isSameCycle(lastCal, nowCal)
						&& nowCal.get(Calendar.DAY_OF_YEAR) == lastCal.get(Calendar.DAY_OF_YEAR);
			}

			@Override
			protected void skipCycles(Calendar cal, int cycles) {
				cal.add(Calendar.DAY_OF_MONTH, cycles);
			}

			@Override
			protected void skipToCycleStart(Calendar cal) {
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
			}

		},
		MONTH("yyyyMM") {
			@Override
			protected boolean isSameCycle(Calendar lastCal, Calendar nowCal) {
				return YEAR.isSameCycle(lastCal, nowCal) && nowCal.get(Calendar.MONTH) == lastCal.get(Calendar.MONTH);
			}

			@Override
			protected void skipCycles(Calendar cal, int cycles) {
				cal.add(Calendar.MONTH, cycles);
			}

			@Override
			protected void skipToCycleStart(Calendar cal) {
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
			}

		},
		YEAR("yyyy") {
			@Override
			protected boolean isSameCycle(Calendar lastCal, Calendar nowCal) {
				return (nowCal.get(Calendar.YEAR) == lastCal.get(Calendar.YEAR));
			}

			@Override
			protected void skipCycles(Calendar cal, int cycles) {
				cal.add(Calendar.YEAR, cycles);
			}

			@Override
			protected void skipToCycleStart(Calendar cal) {
				cal.set(Calendar.MONTH, 0);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
			}

		};

		private final String pattern;

		private CycleType(String pattern) {
			this.pattern = pattern;
		}

		public String getPattern() {
			return pattern;
		}

		public String format(Date date) {
			return new SimpleDateFormat(pattern).format(date);
		}

		public boolean isSameCycle(Date last, Date now) {
			if (last == null)
				return true;
			Calendar lastCalendar = Calendar.getInstance();
			lastCalendar.setTime(last);
			Calendar nowCalendar = Calendar.getInstance();
			nowCalendar.setTime(now);
			return isSameCycle(lastCalendar, nowCalendar);
		}

		public Date getCycleStart(Date date) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			skipToCycleStart(cal);
			return cal.getTime();
		}

		public Date getCycleEnd(Date date) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			skipToCycleEnd(cal);
			return cal.getTime();
		}

		public Date skipCycles(Date date, int cycles) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			skipCycles(cal, cycles);
			return cal.getTime();
		}

		private void skipToCycleEnd(Calendar cal) {
			skipCycles(cal, 1);
			skipToCycleStart(cal);
			cal.add(Calendar.MILLISECOND, -1);
		}

		protected abstract void skipToCycleStart(Calendar cal);

		protected abstract void skipCycles(Calendar cal, int cycles);

		protected abstract boolean isSameCycle(Calendar lastCal, Calendar nowCal);

	}

}