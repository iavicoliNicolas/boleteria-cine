package ar.com.platea.domain;

public enum Genre {
	DRAMA("Drama"),
	THRILLER("Thriller"),
	CIENCIA_FICCION("Ciencia ficción"),
	COMEDIA("Comedia"),
	ROMANCE("Romance"),
	ACCION("Acción"),
	ANIMACION("Animación"),
	TERROR("Terror"),
	DOCUMENTAL("Documental"),
	AVENTURA("Aventura");

	private final String label;

	Genre(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}
