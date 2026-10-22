package ar.com.platea.service;

import ar.com.platea.domain.Auditorium;
import java.util.ArrayList;
import java.util.List;

public final class SeatLayout {

	private SeatLayout() {
	}

	public static String rowLabel(int rowIndex) {
		return String.valueOf((char) ('A' + rowIndex));
	}

	public static boolean premium(int rowIndex, int seatNumber, int rows, int seatsPerRow) {
		if (rows < 5) {
			return false;
		}
		boolean middleRow = rowIndex >= 2 && rowIndex <= rows - 3;
		int half = Math.max(1, seatsPerRow / 2);
		int position = seatNumber <= half ? seatNumber : seatNumber - half;
		int blockSize = seatNumber <= half ? half : seatsPerRow - half;
		return middleRow && position > 1 && position < blockSize;
	}

	public static List<List<Integer>> blocks(int seatsPerRow) {
		int half = seatsPerRow / 2;
		return List.of(range(1, half), range(half + 1, seatsPerRow));
	}

	public static boolean exists(Auditorium auditorium, String code) {
		try {
			parse(auditorium, code);
			return true;
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	public static Position parse(Auditorium auditorium, String code) {
		if (code == null || !code.matches("^[A-L]\\d{1,2}$")) {
			throw new IllegalArgumentException(String.valueOf(code));
		}
		int rowIndex = code.charAt(0) - 'A';
		int number = Integer.parseInt(code.substring(1));
		if (rowIndex < 0 || rowIndex >= auditorium.getRowCount() || number < 1 || number > auditorium.getSeatsPerRow()) {
			throw new IllegalArgumentException(code);
		}
		return new Position(
				rowIndex,
				number,
				rowLabel(rowIndex),
				premium(rowIndex, number, auditorium.getRowCount(), auditorium.getSeatsPerRow()));
	}

	private static List<Integer> range(int from, int to) {
		List<Integer> values = new ArrayList<>();
		for (int number = from; number <= to; number++) {
			values.add(number);
		}
		return List.copyOf(values);
	}

	public record Position(int rowIndex, int number, String rowLabel, boolean premium) {
	}
}
