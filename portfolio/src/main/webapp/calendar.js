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

/** Shows the "Event added" header. */
function showEventAddedHeader() {
  const $eventListHeader = $('#event-added-header');
  if ($eventListHeader.hasClass('d-none')) {
    $eventListHeader.removeClass('d-none');
  }
}

/**
 * Creates a Date object based on a string that represents a time in HH:MM
 * format. This function assumes that the date is the current date when the
 * function is called.
 * @param timeString: a String representation of a time, in format HH:MM.
 * @return a Date object with the current date and the time of the timeString.
 */
function getTimeObject(timeString) {
  const userPickedDate = getUserPickedDateFromDom();
  const currentYear = userPickedDate.getFullYear();
  const currentMonth = userPickedDate.getMonth();
  const currentDate = userPickedDate.getDate();
  const timeHour = timeString.split(':')[0];
  const timeMinute = timeString.split(':')[1];
  return new Date(currentYear, currentMonth, currentDate, timeHour, timeMinute);
}

/** Creates a card element for a new calendar event. */
function updateCalendarEventList(newCalendarEvent) {
  showEventAddedHeader();

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

/** Retrieves the date that the user has picked for the scheduling. */
function getUserPickedDateFromDom() {
  const userPickedDate = $('#date-picker').val().split('-');
  const year = userPickedDate[0];
  const month = userPickedDate[1];
  const date = userPickedDate[2];

  const pickedDate = new Date();
  pickedDate.setFullYear(year);
  pickedDate.setMonth(month - 1);  // month is zero-indexed.
  pickedDate.setDate(date);

  return pickedDate;
}

/** Sets the date picker to the current date as the default. */
function setDatePickerToToday() {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth() + 1;  // month is zero-indexed
  const date = today.getDate();

  const todayString = constructTodayString(year, month, date);
  $('#date-picker').val(todayString);
}

/**
 * Constructs the current day's date into a dash separated string.
 * The parameters are of type String.
 * @param year: of format YYYY
 * @param month: of format MM, starts with 1
 * @param date: of format DD
 */
function constructTodayString(year, month, date) {
  // Adds leading 0 as padding for month and date strings.
  if (parseInt(month) < 10) {
    month = '0' + month;
  }

  if (parseInt(date) < 10) {
    date = '0' + date;
  }
  const todayString = year + '-' + month + '-' + date;
  return todayString;
}

/**
 * Sets the default calendar event start and times.
 * The default start time is set to the closest hour.
 * The default end time is set to the end of working hour.
 */
function setNewEventStartAndEndTimes() {
  const workHourStartString = $('#working-hour-start').val();
  const workHourEndString = $('#working-hour-end').val();

  const userPickedDate = getUserPickedDateFromDom();
  var defaultEventStartHour;
  // Default new event is 1 hour long, which is 9 - 10 AM
  var defaultEventEndHour = "10:00"; 

  if (isToday(userPickedDate)) {
    // If user picked today to schedule for,
    // the default event starts on the next hour if it is before
    // working hour ends.
    const now = new Date();
    var nextHour = now.getHours() + 1;
    defaultEventStartHour =
        getClosestNextHour(nextHour, workHourStartString, workHourEndString);
    defaultEventEndHour = getClosestNextHour(nextHour+1, workHourStartString, workHourEndString);
  } else {
    defaultEventStartHour = workHourStartString;
  }

  $('#new-event-start-time').val(defaultEventStartHour);
  $('#new-event-end-time').val(defaultEventEndHour);
}

/** Checks if a date is today. */
function isToday(date) {
  const today = new Date();
  return (date.getFullYear() === today.getFullYear()
      && date.getMonth() === today.getMonth()
      && date.getDate() === today.getDate());
}

/**
 * Gets the closest next hour compared to the current time.
 * This hour is displayed as the placeholder for creating new event's
 * start time.
 * If the closest hour is before working hour start time, sets it to
 * working hour start time. If the closest hour is after working hour end
 * time, sets it to working hour end time.
 * This function is called if the user picks today to schedule for.
 * @return closest next hour in HH:00 format.
 */
function getClosestNextHour(nextHour, workHourStartString, workHourEndString) {
  if (nextHour == 24) {
    nextHour = 0;
  }

  const workHourStart = parseInt(workHourStartString.split(':')[0]);
  const workHourEnd = parseInt(workHourEndString.split(':')[0]);

  var closestHour = nextHour;
  if (nextHour <= workHourStart) {
    closestHour = workHourStartString;
  } else if (nextHour >= workHourEnd) {
    closestHour = workHourEndString;
  } else {
    if (nextHour < 10) {
      closestHour = '0' + nextHour;
    }
    closestHour += ':00';
  }

  return closestHour;
}

/** Checks the validity of the user's working hours input. */
function checkWorkingHourRange() {
  const $startSchedulingButton = $('#start-scheduling-button');

  const workHourStartParts = $('#working-hour-start').val().split(':');
  const workHourStartHour = parseInt(workHourStartParts[0]);
  const workHourStartMinute = parseInt(workHourStartParts[1]);

  const workHourEndParts = $('#working-hour-end').val().split(':');
  const workHourEndHour = parseInt(workHourEndParts[0]);
  const workHourEndMinute = parseInt(workHourEndParts[1]);

  const $workHourWarning = $('#working-hour-warning');
  if (!isWorkingHourValid(
          workHourStartHour, workHourEndHour, workHourStartMinute,
          workHourEndMinute)) {
    $workHourWarning.removeClass('d-none').text('Working hours are not valid.');
    $startSchedulingButton.attr('disabled', 'disabled');
  } else {
    $workHourWarning.empty().addClass('d-none');
    $startSchedulingButton.removeAttr('disabled', 'disabled');
    // Only sets the default times for calendar events if
    // the inputted working hours are valid.
    setNewEventStartAndEndTimes();
  }
}

/**
 * Checks if the working hour start time is before end time.
 * All arguments are of type integer.
 */
function isWorkingHourValid(
    workHourStartHour,
    workHourEndHour,
    workHourStartMinute,
    workHourEndMinute) {
  return workHourStartHour < workHourEndHour ||
      ((workHourStartHour == workHourEndHour) &&
       (workHourStartMinute < workHourEndMinute));
}

/** Checks if the date the user has picked is before the current date. */
function checkDatePicker() {
  const $startSchedulingButton = $('#start-scheduling-button');

  const pickedDate = getUserPickedDateFromDom();
  const now = new Date();

  if (pickedDate.getTime() < now.getTime()) {
    $('#date-picker-warning').removeClass('d-none');
    $startSchedulingButton.attr('disabled', 'disabled');
  } else {
    $('#date-picker-warning').addClass('d-none');
    $startSchedulingButton.removeAttr('disabled', 'disabled');
  }
}

module.exports._test = {
  constructTodayString: constructTodayString,
  getClosestNextHour: getClosestNextHour,
  isWorkingHourValid: isWorkingHourValid
}
