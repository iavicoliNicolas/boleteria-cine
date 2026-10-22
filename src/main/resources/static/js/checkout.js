(function () {
	const timer = document.querySelector("[data-expira]");
	if (timer) {
		const end = Number(timer.dataset.expira);
		const label = timer.querySelector("[data-cuenta]");
		const tick = function () {
			const left = Math.max(0, end - Date.now());
			const minutes = Math.floor(left / 60000);
			const seconds = Math.floor((left % 60000) / 1000);
			if (label) {
				label.textContent = minutes + ":" + String(seconds).padStart(2, "0");
			}
			if (left === 0) {
				if (label) {
					label.textContent = "0:00";
				}
				timer.classList.add("timer-expired");
				const note = timer.querySelector("[data-vencida]");
				if (note) {
					note.hidden = false;
				}
			}
		};
		tick();
		window.setInterval(tick, 1000);
	}

	const pay = document.querySelector("[data-pagar]");
	if (pay) {
		pay.form.addEventListener("submit", function (event) {
			if (pay.dataset.sending === "1") {
				event.preventDefault();
				return;
			}
			pay.dataset.sending = "1";
			pay.textContent = "Emitiendo entradas…";
		});
	}

	const pan = document.querySelector("[data-pan]");
	if (pan) {
		pan.addEventListener("input", function () {
			const digits = pan.value.replace(/\D/g, "").slice(0, 19);
			pan.value = digits.replace(/(\d{4})(?=\d)/g, "$1 ").trim();
		});
	}

	const expiry = document.querySelector("[data-exp]");
	if (expiry) {
		expiry.addEventListener("input", function () {
			const digits = expiry.value.replace(/\D/g, "").slice(0, 4);
			expiry.value = digits.length >= 3 ? digits.slice(0, 2) + "/" + digits.slice(2) : digits;
		});
	}
})();
