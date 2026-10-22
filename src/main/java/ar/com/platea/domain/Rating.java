package ar.com.platea.domain;

public enum Rating {
	ATP("ATP", "atp", "Apta para todo público"),
	PLUS_13("+13", "plus-13", "Apta para mayores de 13 años"),
	PLUS_16("+16", "plus-16", "Apta para mayores de 16 años"),
	PLUS_18("+18", "plus-18", "Apta para mayores de 18 años");

	private final String label;
	private final String key;
	private final String hint;

	Rating(String label, String key, String hint) {
		this.label = label;
		this.key = key;
		this.hint = hint;
	}

	public String label() {
		return label;
	}

	public String key() {
		return key;
	}

	public String hint() {
		return hint;
	}
}
