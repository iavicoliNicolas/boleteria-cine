package ar.com.platea.domain;

public enum Format {
	DOS_D("2D", "dos-d"),
	TRES_D("3D", "tres-d"),
	IMAX("IMAX", "imax"),
	CUATRO_DX("4DX", "cuatro-dx");

	private final String label;
	private final String key;

	Format(String label, String key) {
		this.label = label;
		this.key = key;
	}

	public String label() {
		return label;
	}

	public String key() {
		return key;
	}
}
