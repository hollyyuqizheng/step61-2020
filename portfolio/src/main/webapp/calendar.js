class CalendarEvent{
  constructor(name, startTime, endTime) {
    this.name = name;
    this.startTime = startTime;
    this.endTime = endTime;
  }
}

/** 
 * Onclick function for "Add this event" button.
 * Retrieves the new event's name, start and end times, and creates a new 
 * CalendarEvent from this information. Adds the new event to the 
 * event list. 
 */
function createNewCalendarEvent() {
  const startTime = document.getElementById('new-event-start-time').value;
  const endTime = document.getElementById('new-event-end-time').value;
  const eventName = document.getElementById('new-event-name').value;

  // Checks that end time is later than start time. 
  // If the time order is wrong, show a warning message on the UI.
  // Otherwise, proceed to create a new event list element and 
  // send the new event to the Calendar servlet. 
  const startTimeConcat = startTime.replace(':', '');
  const endTimeConcat = endTime.replace(':', ''); 
  if (parseInt(startTimeConcat) >= parseInt(endTimeConcat)) {
    document.getElementById('event-end-time-warning').style.visibility = 'visible';
  } else {
    document.getElementById('event-end-time-warning').style.visibility = 'hidden';
    const newCalendarEvent = new CalendarEvent(eventName, startTime, endTime);

    updateCalendarEventList(newCalendarEvent); 
    document.getElementById('new-event-name').value = ''; 

    const newEventJson = JSON.stringify(newCalendarEvent);
    fetch('/calendarServlet', {method: 'POST', body: newEventJson});
  }
}

/** Creates a single element for a new calendar event. */
function updateCalendarEventList(newCalendarEvent) {
  const newEventElement = document.createElement('li');
  newEventElement.innerText = 'New Event: ' + newCalendarEvent.name 
      + ' starts at ' + newCalendarEvent.startTime
      + ' ends at ' + newCalendarEvent.endTime; 

  const eventList = document.getElementById('new-event-list');
  eventList.innterHTML = ''; 
  eventList.appendChild(newEventElement); 
}
