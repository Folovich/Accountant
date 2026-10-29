
const API_URL = "http://localhost:8080";

const $ = (selector) => document.querySelector(selector);

function showStatus(element, message, type = "error") {
  element.textContent = message;
  element.className = `form-notice ${type}`;
}

async function checkConnection() {
  const status = $("#connectionStatus");
  const dot = $("#connectionDot");
  const button = $("#checkConnection");

  status.textContent = "Проверяем подключение...";
  dot.style.background = "#d6a344";
  button.disabled = true;

  try {
    const response = await fetch(`${API_URL}/api/services`, {
      method: "GET",
      signal: AbortSignal.timeout(4000)
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    status.textContent = "Сервер доступен";
    dot.style.background = "#238447";
  } catch (error) {
    status.textContent = "Ошибка подключения к серверу";
    dot.style.background = "#d14343";
  } finally {
    button.disabled = false;
  }
}

$("#checkConnection").addEventListener("click", checkConnection);

function setupForm(formId, statusId, endpoint, successMessage) {
  const form = $(`#${formId}`);
  const status = $(`#${statusId}`);

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (!form.reportValidity()) return;

    const submitButton = form.querySelector('button[type="submit"]');
    const payload = Object.fromEntries(new FormData(form).entries());

    submitButton.disabled = true;
    submitButton.textContent = "Отправка...";
    showStatus(status, "", "success");

    try {
      const response = await fetch(`${API_URL}${endpoint}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
        signal: AbortSignal.timeout(6000)
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      showStatus(status, successMessage, "success");
      form.reset();
    } catch (error) {
      showStatus(
        status,
        "Не удалось отправить данные: сервер недоступен или API ещё не настроен. Ваши данные не сохранены. Попробуйте позже.",
        "error"
      );
    } finally {
      submitButton.disabled = false;
      submitButton.textContent =
        formId === "bookingForm"
          ? "Отправить заявку ↗"
          : "Отправить вопрос ↗";
    }
  });
}

// Минимальная дата встречи — сегодня.
const dateInput = $("#bookingDate");
const today = new Date();
const localToday = [
  today.getFullYear(),
  String(today.getMonth() + 1).padStart(2, "0"),
  String(today.getDate()).padStart(2, "0")
].join("-");

dateInput.min = localToday;

setupForm(
  "bookingForm",
  "bookingStatus",
  "/api/appointments",
  "Заявка успешно отправлена!"
);

setupForm(
  "contactForm",
  "contactStatus",
  "/api/contacts",
  "Сообщение успешно отправлено!"
);

// При открытии страницы сразу проверяем сервер.
checkConnection();
