(function () {
	const form = document.querySelector("[data-seat-form]");
	if (!form || form.dataset.closed === "true") {
		return;
	}
	const boxes = Array.from(form.querySelectorAll('input[type="checkbox"]'));
	const totalEl = document.querySelector("[data-total]");
	const countEl = document.querySelector("[data-count]");
	const listEl = document.querySelector("[data-selected]");
	const button = document.querySelector("[data-continue]");
	const max = 8;

	function pesos(amount) {
		return "$ " + String(amount).replace(/\B(?=(\d{3})+(?!\d))/g, ".");
	}

	function render() {
		const selected = boxes.filter((box) => box.checked);
		boxes.forEach((box) => {
			if (box.dataset.locked === "true") {
				return;
			}
			box.disabled = !box.checked && selected.length >= max;
		});
		if (countEl) {
			countEl.textContent = selected.length === 1 ? "1 butaca" : selected.length + " butacas";
		}
		if (totalEl) {
			const total = selected.reduce((sum, box) => sum + Number(box.dataset.price || 0), 0);
			totalEl.textContent = pesos(total);
		}
		if (listEl) {
			listEl.replaceChildren();
			selected.forEach((box) => {
				const item = document.createElement("li");
				const name = document.createElement("span");
				name.textContent = "Fila " + box.dataset.row + " · Butaca " + box.dataset.number
					+ (box.dataset.premium === "true" ? " · Preferencial" : "");
				const price = document.createElement("strong");
				price.textContent = pesos(Number(box.dataset.price || 0));
				item.append(name, price);
				listEl.append(item);
			});
		}
		if (button) {
			button.disabled = selected.length === 0;
		}
	}

	boxes.forEach((box) => box.addEventListener("change", render));
	render();
})();
