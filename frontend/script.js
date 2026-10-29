const API_URL = "";
const $ = (selector) => document.querySelector(selector);
const SLOT_TIMES = ["09:00", "10:00", "11:00", "12:00", "14:00", "15:00", "16:00"];
const DAY_NAMES = ["Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"];
const monthFormat = new Intl.DateTimeFormat("ru-RU", { month: "long", year: "numeric" });
const fullDateFormat = new Intl.DateTimeFormat("ru-RU", {
  weekday: "long",
  day: "numeric",
  month: "long"
});

async function apiRequest(path, options = {}) {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    credentials: "include",
    headers: {
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...options.headers
    },
    signal: AbortSignal.timeout(8000)
  });

  if (response.status === 204) return null;

  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message = body && typeof body === "object" && body.error
      ? body.error
      : `Ошибка сервера (${response.status})`;
    throw new Error(message);
  }
  return body;
}

function showStatus(element, message, type = "error") {
  element.textContent = message;
  element.className = `form-notice ${type}`;
}

function localDate(date) {
  return [
    date.getFullYear(),
    String(date.getMonth() + 1).padStart(2, "0"),
    String(date.getDate()).padStart(2, "0")
  ].join("-");
}

function mondayOf(date) {
  const monday = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  monday.setDate(monday.getDate() - ((monday.getDay() + 6) % 7));
  return monday;
}

function displayDate(date) {
  return fullDateFormat.format(date);
}

let currentUser = null;
let weekStart = mondayOf(new Date());
let selectedSlot = null;
let selectedAppointment = null;
let authMode = "login";

async function checkConnection() {
  const status = $("#connectionStatus");
  const dot = $("#connectionDot");

  try {
    await apiRequest("/api/services");
    status.textContent = "Сервер доступен";
    dot.classList.add("online");
  } catch {
    status.textContent = "Нет соединения";
    dot.classList.remove("online");
  }
}

function setAuthMode(mode) {
  authMode = mode;
  const isLogin = mode === "login";
  $("#accountLoginForm").hidden = !isLogin;
  $("#accountRegisterForm").hidden = isLogin;
  $("#authTitle").textContent = isLogin ? "С возвращением" : "Создать аккаунт";
  $("#authDescription").textContent = isLogin
    ? "Войдите, чтобы посмотреть расписание бухгалтера."
    : "Зарегистрируйтесь, чтобы записываться на консультации.";
  $("#authToggle").innerHTML = isLogin
    ? 'Нет аккаунта? <span>Зарегистрироваться</span>'
    : 'Уже есть аккаунт? <span>Войти</span>';
}

function showAccount(user) {
  currentUser = user;
  $("#authView").hidden = true;
  $("#scheduleView").hidden = false;
  $("#profileBar").hidden = false;
  $("#connectionIndicator").hidden = true;
  $("#accountIdentity").textContent = user.email;
  $("#accountRole").textContent = user.role === "ACCOUNTANT" ? "Бухгалтер" : "Клиент";
  $("#mineLegend").hidden = user.role === "ACCOUNTANT";
  $("#scheduleHint").textContent = user.role === "ACCOUNTANT"
    ? "Нажмите на занятую запись, чтобы посмотреть детали или удалить её."
    : "Выберите свободное время, чтобы создать запись.";
  loadSchedule();
}

function showLogin() {
  currentUser = null;
  $("#authView").hidden = false;
  $("#scheduleView").hidden = true;
  $("#profileBar").hidden = true;
  $("#connectionIndicator").hidden = false;
  weekStart = mondayOf(new Date());
}

function renderCalendar(appointments) {
  const calendar = $("#calendar");
  calendar.replaceChildren();
  const appointmentMap = new Map(
    appointments.map((appointment) => [
      `${appointment.date}T${appointment.time.slice(0, 5)}`,
      appointment
    ])
  );
  const days = Array.from({ length: 7 }, (_, index) => {
    const date = new Date(weekStart);
    date.setDate(date.getDate() + index);
    return date;
  });

  const corner = document.createElement("div");
  corner.className = "calendar-corner";
  corner.textContent = "ВРЕМЯ";
  calendar.append(corner);

  days.forEach((date, index) => {
    const heading = document.createElement("div");
    heading.className = "day-heading";
    if (localDate(date) === localDate(new Date())) heading.classList.add("today");
    heading.innerHTML = `<span>${DAY_NAMES[index]}</span><strong>${String(date.getDate()).padStart(2, "0")}</strong>`;
    calendar.append(heading);
  });

  SLOT_TIMES.forEach((time) => {
    const timeLabel = document.createElement("div");
    timeLabel.className = "time-label";
    timeLabel.textContent = time;
    calendar.append(timeLabel);

    days.forEach((date) => {
      const dateValue = localDate(date);
      const appointment = appointmentMap.get(`${dateValue}T${time}`);
      const isPast = dateValue < localDate(new Date())
        || (dateValue === localDate(new Date())
          && time <= new Date().toTimeString().slice(0, 5));
      const slot = document.createElement("button");
      slot.type = "button";
      slot.className = "slot";

      if (appointment) {
        const isMine = currentUser.role !== "ACCOUNTANT"
          && appointment.id !== null && appointment.id !== undefined;
        slot.classList.add(isMine ? "slot-mine" : "slot-booked");
        slot.disabled = !isMine && currentUser.role !== "ACCOUNTANT";
        slot.textContent = currentUser.role === "ACCOUNTANT"
          ? appointment.name || "Занято"
          : isMine ? "Моя запись" : "Занято";
        slot.title = currentUser.role === "ACCOUNTANT" || isMine
          ? "Открыть подробности"
          : "Это время уже занято";
        if (currentUser.role === "ACCOUNTANT" || isMine) {
          slot.addEventListener("click", () => openAppointmentDetails(appointment));
        }
      } else {
        slot.classList.add("slot-free");
        slot.textContent = currentUser.role === "ACCOUNTANT" ? "Свободно" : "+";
        slot.disabled = isPast || currentUser.role === "ACCOUNTANT";
        slot.title = isPast ? "Время уже прошло" : "Создать запись";
        if (!slot.disabled) {
          slot.addEventListener("click", () => openBookingForm(dateValue, time));
        }
      }

      calendar.append(slot);
    });
  });

  const end = new Date(weekStart);
  end.setDate(end.getDate() + 6);
  if (weekStart.getMonth() === end.getMonth()
      && weekStart.getFullYear() === end.getFullYear()) {
    $("#weekLabel").textContent = `${monthFormat.format(weekStart)} · ${String(weekStart.getDate()).padStart(2, "0")}–${String(end.getDate()).padStart(2, "0")}`;
  } else {
    const shortDate = new Intl.DateTimeFormat("ru-RU", { day: "numeric", month: "short" });
    $("#weekLabel").textContent = `${shortDate.format(weekStart)} — ${shortDate.format(end)} ${end.getFullYear()}`;
  }
}

async function loadSchedule() {
  if (!currentUser) return;
  const start = localDate(weekStart);
  showStatus($("#dashboardStatus"), "Загружаем расписание…", "info");

  try {
    const appointments = await apiRequest(
      `/api/appointments/week?start=${encodeURIComponent(start)}`
    );
    renderCalendar(appointments);
    showStatus($("#dashboardStatus"), "", "success");
  } catch (error) {
    showStatus($("#dashboardStatus"), error.message);
  }
}

function openDialog() {
  $("#appointmentDialog").showModal();
}

function openBookingForm(date, time) {
  selectedSlot = { date, time };
  selectedAppointment = null;
  const form = $("#bookingForm");
  form.reset();
  $("#dialogEyebrow").textContent = "НОВАЯ ВСТРЕЧА";
  $("#dialogTitle").textContent = "Запись на консультацию";
  $("#dialogWhen").textContent = `${displayDate(new Date(`${date}T12:00:00`))} · ${time}`;
  $("#bookingFields").hidden = false;
  $("#appointmentDetails").hidden = true;
  $("#deleteActions").hidden = true;
  showStatus($("#bookingStatus"), "", "success");
  $("#bookingEmail").value = currentUser.email;
  openDialog();
  $("#bookingName").focus();
}

function openAppointmentDetails(appointment) {
  selectedAppointment = appointment;
  selectedSlot = null;
  $("#bookingForm").reset();
  $("#deleteAppointment").hidden = false;
  $("#deleteConfirmation").hidden = true;
  $("#dialogEyebrow").textContent = currentUser.role === "ACCOUNTANT"
    ? "ЗАПИСЬ КЛИЕНТА"
    : "ВАША ВСТРЕЧА";
  $("#dialogTitle").textContent = appointment.name || "Занято";
  $("#dialogWhen").textContent = `${displayDate(new Date(`${appointment.date}T12:00:00`))} · ${appointment.time.slice(0, 5)}`;
  $("#bookingFields").hidden = true;
  $("#deleteActions").hidden = currentUser.role !== "ACCOUNTANT";
  $("#appointmentDetails").hidden = false;
  $("#appointmentDetails").replaceChildren();

  [
    ["Email", appointment.email],
    ["Телефон", appointment.phone],
    ["Вопрос", appointment.message]
  ].forEach(([label, value]) => {
    const line = document.createElement("div");
    line.className = "detail-line";
    const name = document.createElement("span");
    name.textContent = label;
    const content = document.createElement("strong");
    content.textContent = value || "Не указано";
    line.append(name, content);
    $("#appointmentDetails").append(line);
  });

  showStatus($("#bookingStatus"), "", "success");
  openDialog();
}

$("#authToggle").addEventListener("click", () => {
  setAuthMode(authMode === "login" ? "register" : "login");
});

$("#accountLoginForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const button = form.querySelector('button[type="submit"]');
  button.disabled = true;
  try {
    const user = await apiRequest("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(Object.fromEntries(new FormData(form).entries()))
    });
    form.reset();
    showAccount(user);
  } catch (error) {
    showStatus($("#loginStatus"), error.message);
  } finally {
    button.disabled = false;
  }
});

$("#accountRegisterForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const button = form.querySelector('button[type="submit"]');
  const email = $("#registerEmail").value;
  button.disabled = true;
  try {
    await apiRequest("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(Object.fromEntries(new FormData(form).entries()))
    });
    form.reset();
    setAuthMode("login");
    $("#loginEmail").value = email;
    showStatus($("#loginStatus"), "Аккаунт создан. Теперь войдите.", "success");
  } catch (error) {
    showStatus($("#registerStatus"), error.message);
  } finally {
    button.disabled = false;
  }
});

$("#previousWeek").addEventListener("click", () => {
  weekStart.setDate(weekStart.getDate() - 7);
  loadSchedule();
});

$("#nextWeek").addEventListener("click", () => {
  weekStart.setDate(weekStart.getDate() + 7);
  loadSchedule();
});

$("#currentWeek").addEventListener("click", () => {
  weekStart = mondayOf(new Date());
  loadSchedule();
});

$("#refreshSchedule").addEventListener("click", loadSchedule);

$("#bookingForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!selectedSlot) return;
  const button = $("#bookingSubmit");
  button.disabled = true;
  try {
    const payload = Object.fromEntries(new FormData(event.currentTarget).entries());
    payload.date = selectedSlot.date;
    payload.time = selectedSlot.time;
    await apiRequest("/api/appointments", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    $("#appointmentDialog").close();
    await loadSchedule();
  } catch (error) {
    showStatus($("#bookingStatus"), error.message);
  } finally {
    button.disabled = false;
  }
});

$("#deleteAppointment").addEventListener("click", async () => {
  if (!selectedAppointment?.id) return;
  $("#deleteAppointment").hidden = true;
  $("#deleteConfirmation").hidden = false;
  $("#confirmDeleteAppointment").focus();
});

$("#cancelDeleteAppointment").addEventListener("click", () => {
  $("#deleteConfirmation").hidden = true;
  $("#deleteAppointment").hidden = false;
  $("#deleteAppointment").focus();
});

$("#confirmDeleteAppointment").addEventListener("click", async (event) => {
  if (!selectedAppointment?.id) return;
  const button = event.currentTarget;
  button.disabled = true;
  try {
    await apiRequest(`/api/appointments/${selectedAppointment.id}`, { method: "DELETE" });
    $("#appointmentDialog").close();
    await loadSchedule();
  } catch (error) {
    showStatus($("#bookingStatus"), error.message);
  } finally {
    button.disabled = false;
  }
});

$("#closeDialog").addEventListener("click", () => $("#appointmentDialog").close());
$("#appointmentDialog").addEventListener("click", (event) => {
  if (event.target === $("#appointmentDialog")) $("#appointmentDialog").close();
});

$("#logoutButton").addEventListener("click", async (event) => {
  const button = event.currentTarget;
  button.disabled = true;
  try {
    await apiRequest("/api/auth/logout", { method: "POST" });
    showLogin();
    setAuthMode("login");
  } catch (error) {
    showStatus($("#dashboardStatus"), error.message);
  } finally {
    button.disabled = false;
  }
});

async function restoreSession() {
  try {
    showAccount(await apiRequest("/api/auth/me"));
  } catch (error) {
    if (error.message !== "Войдите в аккаунт") {
      showStatus($("#loginStatus"), error.message);
    }
  }
}

checkConnection();
restoreSession();
