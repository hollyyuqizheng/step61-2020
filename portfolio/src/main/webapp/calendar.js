/**
 * Models a calendar event that is displayed on the UI.
 * This class is useful when the event information needs to be sent
 * to the calendar servlet through a POST request.
 * This class contains all the event information that can be easily
 * converted into a JSON string.
 */
class CalendarEvent {
  /**
   * All fields are required.
   * @param name: of type String, name of event
   * @param startTime: of type Date in format "Day Month Date Year HH:MM:SS
   *    GMT-Time Zone"
   * @param endTime: of type Date in format "Day Month Date Year HH:MM:SS
   *    GMT-Time Zone"
   */
  constructor(name, startTime, endTime) {
    this.name = name;
    this.startTime = startTime;
    this.endTime = endTime;
  }
}
 
const EVENT_DEFAULT_NAME = 'New Event';
 
/**
 * Onclick function for "Add this event" button.
 * Retrieves the new event's name, start and end times, and creates a new
 * CalendarEvent from this information. Adds the new event to the
 * event list.
 */
function createNewCalendarEventUserInput() {
  // First hide the warning message.
  $('#event-warning').hide();
 
  // Constructs time objects for start and end times.
  // The objects are in format:
  // Day Month Date Year HH:MM:SS GMT-Time Zone
  const startTime =
      getTimeObject(document.getElementById('new-event-start-time').value);
  const endTime =
      getTimeObject(document.getElementById('new-event-end-time').value);
 
  // If user enters an empty event name, sets the name to a default string.
  const eventName =
      document.getElementById('new-event-name').value || EVENT_DEFAULT_NAME;
 
  // Checks that end time is later than start time.
  // If the time order is wrong, show a warning message on the UI.
  // Otherwise, proceed to create a new event list element and
  // send the new event to the Calendar servlet.
  if (endTime.getTime() <= startTime.getTime()) {
    $('#event-warning').removeClass('d-none');
    $('#event-warning').show();
    $('#event-warning').text('End time must be later than start time.');
  } else {
    $('#event-warning').hide();
    $('#event-warning')
 
    const newCalendarEvent = new CalendarEvent(eventName, startTime, endTime);
 
    // Get all the events currently displayed on the UI.
    // Look through this set of events to ensure that duplicate events
    // do not get added again.
    const allEvents = collectAllEvents();
 
    const doesEventExist = allEvents.reduce(
        (newEventExists, existingEvent) =>
            newEventExists || eventsEqual(newCalendarEvent, existingEvent),
        /* initialValue= */ false);
 
    if (!doesEventExist) {
      $('#event-warning').hide();
      updateCalendarEventList(newCalendarEvent);
    } else {
      $('#event-warning').removeClass('d-none');
      $('#event-warning').show();
      $('#event-warning').text('This event has already been already added.');
    }
    document.getElementById('new-event-name').value = EVENT_DEFAULT_NAME;
  }
}
 
/**
 * Creates a Date object based on a string that represents a time in HH:MM
 * format. This function assumes that the date is the current date when the
 * function is called.
 */
function getTimeObject(timeString) {
  const userPickedDate = getUserPickedDate();
  const currentYear = userPickedDate.getFullYear();
  const currentMonth = userPickedDate.getMonth();
  const currentDate = userPickedDate.getDate();
  const timeHour = timeString.split(':')[0];
  const timeMinute = timeString.split(':')[1];
  return new Date(currentYear, currentMonth, currentDate, timeHour, timeMinute);
}
 
/** Creates a card element for a new calendar event. */
function updateCalendarEventList(newCalendarEvent) {
  const newEventCard = document.createElement('div');
  newEventCard.classList.add('card');
 
  const cardBody = document.createElement('div');
  cardBody.classList.add('card-body');
  newEventCard.appendChild(cardBody);
 
  const cardTitle = document.createElement('h4');
  cardTitle.classList.add('card-title');
  cardTitle.innerText = newCalendarEvent.name;
  cardBody.appendChild(cardTitle);
 
  const startTimeText = document.createElement('p');
  startTimeText.classList.add('card-text');
  startTimeText.innerText = newCalendarEvent.startTime;
  cardBody.appendChild(startTimeText);
 
  const endTimeText = document.createElement('p');
  endTimeText.classList.add('card-text');
  endTimeText.innerText = newCalendarEvent.endTime;
  cardBody.appendChild(endTimeText);
 
  const deleteButton = document.createElement('button');
  deleteButton.classList.add('btn');
  deleteButton.classList.add('btn-danger');
  deleteButton.innerText = 'Delete this event';
  cardBody.appendChild(deleteButton);
 
  const eventList = document.getElementById('new-event-list');
  eventList.innterHTML = '';
  eventList.appendChild(newEventCard);
 
  // The delete button removes the event's card from the UI.
  deleteButton.onclick = function(newEventCard) {
    newEventCard.target.closest('div.card').remove();
  };
}
 
/**
 * Collects and returns all the events currently displayed on the UI.
 * @return an array of calendar events
 */
function collectAllEvents() {
  const eventList = document.getElementById('new-event-list');
 
  // Looks at each event card and scrapes the event's name and
  // start and end times from the HTML elements.
  // Add all event information to a set of all Json strings.
  const allEventsOnPage = Array.from(eventList.childNodes).map((eventCard) => {
    const eventCardBody = eventCard.childNodes[0];
    const eventName = eventCardBody.childNodes[0].innerText;
    const startTime = new Date(eventCardBody.childNodes[1].innerText);
    const endTime = new Date(eventCardBody.childNodes[2].innerText);
    const event = new CalendarEvent(eventName, startTime, endTime);
    return event;
  });
 
  return allEventsOnPage;
}
 
/** Checks if two calendar events are identical. */
function eventsEqual(eventA, eventB) {
  return (eventA.name === eventB.name) &&
      (eventA.startTime.getTime() === eventB.startTime.getTime()) &&
      (eventA.endTime.getTime() === eventB.endTime.getTime());
}
 
/** Sets the date picker to the current date as the default. */
function useTodayAsDefault() {
  const today = new Date();
  const year = today.getFullYear();
  let month = today.getMonth() + 1;  // month is zero-indexed
 
  // Adds leading 0 as padding for month and date strings.
  if (parseInt(month) < 10) {
    month = '0' + month;
  }
  let date = today.getDate();
  if (parseInt(date) < 10) {
    date = '0' + date;
  }
  $('#date-picker').val(year + '-' + month + '-' + date);
}
 
/**
 * Sets the default calendar event start and times.
 * The default start time is set to the closet hour.
 * If the closest hour is outside working hours, use working hour
 * instead of the actual closest hour.
 * The default end time is set to the end of working hour.
 */
function setClosestEventTime() {
  const now = new Date();
  let closestHour = now.getHours() + 1;
 
  const workHourStartString = $('#working-hour-start').val();
  const workHourStart = parseInt(workHourStartString.split(':')[0]);
  const workHourEndString = $('#working-hour-end').val();
  const workHourEnd = parseInt(workHourEndString.split(':')[0]);
 
  if (closestHour < workHourStart) {
    closestHour = workHourStartString;
  } else if (closestHour > workHourEnd) {
    closestHour = workHourEndString;
  } else {
    if (closestHour < 10) {
      closestHour = '0' + closestHour;
    }
    closestHour += ':00';
  }
 
  $('#new-event-start-time').val(closestHour);
  $('#new-event-end-time').val(workHourEndString);
}
 
/** Checks the validity of the user's working hours input. */
function checkWorkHourRange() {
  const workHourStartHour =
      parseInt($('#working-hour-start').val().split(':')[0]);
  const workHourStartMinute =
      parseInt($('#working-hour-start').val().split(':')[1]);
  const workHourEndHour = parseInt($('#working-hour-end').val().split(':')[0]);
  const workHourEndMinute =
      parseInt($('#working-hour-end').val().split(':')[1]);
 
  if (workHourStartHour > workHourEndHour ||
      (workHourStartHour == workHourEndHour) &&
          (workHourStartMinute > workHourEndMinute)) {
    $('#working-hour-warning').removeClass('d-none');
    $('#working-hour-warning').text('Start time cannot be after end time.');
  } else {
    $('#working-hour-warning').addClass('d-none');
    // Only sets the default times for calendar events if
    // the inputted working hours are valid.
    setClosestEventTime();
  }
}
 
/** Checks if the date the user has picked is before the current date. */
function checkDatePicker() {
  const pickedDate = getUserPickedDate();
  const now = new Date();
 
  if (pickedDate.getTime() < now.getTime()) {
    $('#date-picker-warning').removeClass('d-none');
  } else {
    $('#date-picker-warning').addClass('d-none');
  }
}
