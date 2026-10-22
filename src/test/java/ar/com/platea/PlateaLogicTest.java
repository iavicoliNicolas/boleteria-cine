package ar.com.platea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.com.platea.domain.Format;
import ar.com.platea.service.CardRejectedException;
import ar.com.platea.service.CardValidator;
import ar.com.platea.service.Money;
import ar.com.platea.service.Pricing;
import ar.com.platea.service.SeatLayout;
import ar.com.platea.service.Slugs;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PlateaLogicTest {

	@Test
	void formateaPesosConSeparadorDeMiles() {
		assertThat(Money.pesos(7900)).isEqualTo("$ 7.900");
		assertThat(Money.pesos(15200)).isEqualTo("$ 15.200");
	}

	@Test
	void armaSlugsSinAcentos() {
		assertThat(Slugs.slug("Órbitas")).isEqualTo("orbitas");
		assertThat(Slugs.slug("El faro de Quequén")).isEqualTo("el-faro-de-quequen");
		assertThat(Slugs.slug("Platea Nueva Córdoba")).isEqualTo("platea-nueva-cordoba");
	}

	@Test
	void cobraMasElFinDeSemanaYLasPreferenciales() {
		assertThat(Pricing.base(Format.DOS_D, DayOfWeek.MONDAY)).isEqualTo(7900);
		assertThat(Pricing.base(Format.DOS_D, DayOfWeek.FRIDAY)).isEqualTo(8900);
		assertThat(Pricing.base(Format.IMAX, DayOfWeek.SATURDAY)).isEqualTo(15200);
		assertThat(SeatLayout.premium(0, 3, 8, 12)).isFalse();
		assertThat(SeatLayout.premium(2, 1, 8, 12)).isFalse();
		assertThat(SeatLayout.premium(2, 3, 8, 12)).isTrue();
		assertThat(SeatLayout.premium(7, 4, 8, 12)).isFalse();
	}

	@Test
	void aceptaVisaYRechazaTarjetaVencidaOTerminadaEnCeros() {
		LocalDate today = LocalDate.of(2026, 10, 22);
		CardValidator.Card card = CardValidator.check("4242 4242 4242 4242", "12/30", "123", "ANA PEREZ", today);
		assertThat(card.brand()).isEqualTo("Visa");
		assertThat(card.last4()).isEqualTo("4242");

		assertThatThrownBy(() -> CardValidator.check("4000000000020000", "12/30", "123", "ANA PEREZ", today))
				.isInstanceOf(CardRejectedException.class)
				.hasMessageContaining("rechazó");
		assertThatThrownBy(() -> CardValidator.check("4242424242424242", "01/20", "123", "ANA PEREZ", today))
				.isInstanceOf(CardRejectedException.class)
				.hasMessageContaining("vencida");
	}
}
