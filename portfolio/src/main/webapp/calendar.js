/** 
 * Models a calendar event that is displayed on the UI.
 * This class is useful when the event information needs to be sent 
 * to the calendar servlet through a POST request. 
 * This class contains all the event information that can be easily
 * converted into a JSON string. 
 */
class CalendarEvent {
  constructor(name, startTime, endTime) {
    this.name = name;
    this.startTime = startTime;
    this.endTime = endTime;
  }
}

let allEventJson = []; 

/** 
 * Onclick function for "Add this event" button.
 * Retrieves the new event's name, start and end times, and creates a new 
 * CalendarEvent from this information. Adds the new event to the 
 * event list. 
 */
function createNewCalendarEvent() {
  // Constructs time objects for start and end times. 
  // The objects are in format: 
  // Day Month Date Year HH:MM:SS GMT-Time Zone
  const startTime = getTimeObject(document.getElementById('new-event-start-time').value);
  const endTime = getTimeObject(document.getElementById('new-event-end-time').value);

  const eventName = document.getElementById('new-event-name').value;

  // Checks that end time is later than start time. 
  // If the time order is wrong, show a warning message on the UI.
  // Otherwise, proceed to create a new event list element and 
  // send the new event to the Calendar servlet. 
  if (endTime.getTime() <= startTime.getTime()) {
    document.getElementById('event-end-time-warning').style.visibility = 'visible';
  } else {
    document.getElementById('event-end-time-warning').style.visibility = 'hidden';
    const newCalendarEvent = new CalendarEvent(eventName, startTime, endTime);

    updateCalendarEventList(newCalendarEvent); 
    document.getElementById('new-event-name').value = ''; 

    const newEventJson = JSON.stringify(newCalendarEvent);
    allEventJson.push(newEventJson);  
  }
}

/** 
 * Creates a Date object based on a string that represents a time in HH:MM format.
 * This function assumes that the date is the current date when the function is called. 
 */
function getTimeObject(timeString) {
  const today = new Date(); 
  const currentYear = today.getFullYear();
  const currentMonth = today.getMonth();
  const currentDate = today.getDate();
  const timeHour = timeString.split(":")[0];
  const timeMinute = timeString.split(":")[1];
  return new Date(
      currentYear, currentMonth, currentDate, timeHour, timeMinute); 
}

/** Creates a single element for a new calendar event. */
function updateCalendarEventList(newCalendarEvent) {
  const newEventElement = document.createElement('li');
  newEventElement.innerHTML = 'New Event: ' + newCalendarEvent.name 
      + '<br>starts at ' + newCalendarEvent.startTime
      + '<br>ends at ' + newCalendarEvent.endTime; 

  const eventList = document.getElementById('new-event-list');
  eventList.innterHTML = ''; 
  eventList.appendChild(newEventElement); 
}

/** Performs the POST request to send the event list to calendar servlet. */
function sendEventToServer() {
  fetch('/calendarServlet', {method: 'POST', body: JSON.stringify(allEventJson)});
}
