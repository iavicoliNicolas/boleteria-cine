package ar.com.platea.view;

import java.util.List;

public record TicketView(
		String code,
		String buyer,
		String email,
		String phone,
		String title,
		String cinema,
		String address,
		String city,
		String auditorium,
		String when,
		String format,
		String language,
		String seats,
		int count,
		String total,
		String card,
		String purchased,
		List<Bar> barcode) {

	public record Bar(int width, boolean ink) {
	}
}
