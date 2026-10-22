package ar.com.platea.service;

public class CardRejectedException extends RuntimeException {

	private final String field;

	public CardRejectedException(String field, String message) {
		super(message);
		this.field = field;
	}

	public String field() {
		return field;
	}
}
