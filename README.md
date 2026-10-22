# Platea

Boletería de cine. Tiene cartelera, complejos en cuatro ciudades, funciones para los próximos días, mapa de butacas y emisión de entradas. El pago es simulado: no se cobra nada y la tarjeta no se guarda, solo los últimos cuatro números.

Los datos viven en una base en memoria y se vuelven a cargar cada vez que arranca la aplicación.

## Requisitos

- Java 21

Maven no hace falta tenerlo instalado: el proyecto trae el wrapper `./mvnw`.

## Correr

```bash
./mvnw spring-boot:run
```

Abrí [http://localhost:8741](http://localhost:8741).

## Comprar una entrada de prueba

1. Elegí una película y una función.
2. Marcá hasta 8 butacas. Quedan retenidas 8 minutos.
3. En el pago usá `4242 4242 4242 4242`, un vencimiento futuro y cualquier CVV.
4. Un número que termina en `0000` (por ejemplo `4000 0000 0002 0000`) simula un rechazo del banco.

La entrada queda con un código `PLT-……`. Se puede recuperar en **Mis entradas** con el email o con ese código.

## Tests

```bash
./mvnw test
```

## Stack

Java 21, Spring Boot, Thymeleaf, Spring Data JPA y H2.
