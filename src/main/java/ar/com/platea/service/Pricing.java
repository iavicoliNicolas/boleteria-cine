package ar.com.platea.service;

import ar.com.platea.domain.Format;
import java.time.DayOfWeek;

public final class Pricing {

	public static final int PREMIUM_SURCHARGE = 1800;

	private Pricing() {
	}

	public static int base(Format format, DayOfWeek day) {
		boolean weekend = day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
		return switch (format) {
			case DOS_D -> weekend ? 8900 : 7900;
			case TRES_D -> weekend ? 10900 : 9900;
			case IMAX -> weekend ? 15200 : 13900;
			case CUATRO_DX -> weekend ? 17200 : 15900;
		};
	}
}
