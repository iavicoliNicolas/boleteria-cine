package ar.com.platea.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CheckoutForm {

	@NotBlank(message = "Ingresá tu nombre y apellido")
	@Size(min = 3, max = 80, message = "El nombre tiene que tener al menos 3 caracteres")
	private String nombre = "";

	@NotBlank(message = "Ingresá tu email")
	@Email(message = "El email no es válido")
	@Size(max = 120, message = "El email es demasiado largo")
	private String email = "";

	@Pattern(regexp = "^$|^[+0-9 ()-]{6,30}$", message = "Revisá el teléfono")
	private String telefono = "";

	@NotBlank(message = "Ingresá el número de la tarjeta")
	@Size(max = 23, message = "Revisá el número de la tarjeta")
	private String tarjeta = "";

	@NotBlank(message = "Ingresá el vencimiento")
	private String vencimiento = "";

	private String cvv = "";

	@NotBlank(message = "Ingresá el nombre del titular")
	@Size(max = 80, message = "El nombre del titular es demasiado largo")
	private String titular = "";

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre == null ? "" : nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email == null ? "" : email;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono == null ? "" : telefono;
	}

	public String getTarjeta() {
		return tarjeta;
	}

	public void setTarjeta(String tarjeta) {
		this.tarjeta = tarjeta == null ? "" : tarjeta;
	}

	public String getVencimiento() {
		return vencimiento;
	}

	public void setVencimiento(String vencimiento) {
		this.vencimiento = vencimiento == null ? "" : vencimiento;
	}

	public String getCvv() {
		return cvv;
	}

	public void setCvv(String cvv) {
		this.cvv = cvv == null ? "" : cvv.replaceAll("\\D", "");
	}

	public String getTitular() {
		return titular;
	}

	public void setTitular(String titular) {
		this.titular = titular == null ? "" : titular;
	}
}
