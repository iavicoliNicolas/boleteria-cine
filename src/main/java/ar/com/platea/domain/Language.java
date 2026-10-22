package ar.com.platea.domain;

public enum Language {
	SUB("Subtitulada", "SUB"),
	DUB("Doblada", "DOB"),
	OV("Idioma original", "VO");

	private final String label;
	private final String chip;

	Language(String label, String chip) {
		this.label = label;
		this.chip = chip;
	}

	public String label() {
		return label;
	}

	public String chip() {
		return chip;
	}
}
