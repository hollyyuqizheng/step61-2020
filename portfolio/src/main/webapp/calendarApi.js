/** Updates the calendar view and button visibility based on login status. */
function updateCalendarView() {
  const googleUser = gapi.auth2.getAuthInstance().currentUser.get();
  const isAuthorized = googleUser.hasGrantedScopes(SCOPE_CALENDAR_READ_ONLY);
  if (isAuthorized) {
    showCalendarView(googleUser);
    $('#import-calendar-button').removeClass('d-none');
  } else {
    $('#import-calendar-button').addClass('d-none');
  }
}

/**
 * Retrieves the logged in user's Google email and uses that email
 * to embed the user's Google Calendar on the UI.
 * The calendar is displayed in the user's Google Calendar's time zone.
 */
function showCalendarView(user) {
  const googleUser = gapi.auth2.getAuthInstance().currentUser.get();
  const isAuthorized = googleUser.hasGrantedScopes(SCOPE_CALENDAR_READ_ONLY);
  if (isAuthorized) {
    const userEmail = encodeURIComponent(user.getBasicProfile().getEmail());

    // Fetches the user's Calendar's time zone.
    gapi.client.calendar.settings.get({setting: 'timezone'})
        .then((responseTimeZone) => {
          const userCalendarTimeZone =
              encodeURIComponent(responseTimeZone.result.value);
          const calendarViewUrl =
              'https://calendar.google.com/calendar/embed?src=' + userEmail +
              '&ctz=' + userCalendarTimeZone + '&mode=WEEK';

          // Updates the UI so that calendar view appears.
          $('#calendar-view')
              .attr('src', calendarViewUrl)
              .removeClass('d-none');
        });
  }
}

/** Retrives the date that the user has picked for the scheduling. */
function getUserPickedDate() {
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

/**
 * Print the summary and start datetime/date of the events for the
 * day that user has picked in the nav bar.
 * If no events are found a message is displayed on the UI.
 */
function listUpcomingEvents() {
  // The start and end time limits for imported events are
  // start and end of day of the user's picked date.
  const timeRangeStart = getUserPickedDate();
  timeRangeStart.setHours(0, 0, 0);
  const timeRangeEnd = getUserPickedDate();
  timeRangeEnd.setHours(24, 0, 0);

  // Retrieves events on the user's calendar for the day
  // that the user has picked in the nav bar.
  gapi.client.calendar.events
      .list({
        calendarId: 'primary',
        timeMin: timeRangeStart.toISOString(),
        timeMax: timeRangeEnd.toISOString(),
        showDeleted: false,
        singleEvents: true,
        orderBy: 'startTime'
      })
      .then(function(response) {
        var events = response.result.items;
        var $emptyCalendarMessage = $('#empty-calendar-import-message');

        // Show message for no imported event if result list is empty.
        if (events.length == 0) {
          const pickedDate = $('#date-picker').val();
          $emptyCalendarMessage
              .text(
                  'There aren\'t any events scheduled on your Google Calendar for ' +
                  pickedDate)
              .removeClass('d-none');
        } else {
          $emptyCalendarMessage.addClass('d-none');

          events.forEach((event) => {
            const eventName = event.summary;

            // When retrieved from Google Calendar, these time strings
            // are in the time zone of the user's Calendar's time zone.
            // These time strings have format YYYY-MM-DDTHH:MM:SS-TimeZoneOffset
            var startTimeString = event.start.dateTime;
            if (!startTimeString) {
              startTimeString = event.start.date;
            }

            var endTimeString = event.end.dateTime;
            if (!endTimeString) {
              endTimeString = event.end.date;
            }

            const newCalendarEvent = new CalendarEvent(
                eventName, new Date(startTimeString), new Date(endTimeString));

            // Add these events to the UI under the events list.
            // On the UI, the event times will be displayed in the user's
            // current location's time zone.
            // Each will not get added more than once.
            const allEvents = collectAllEvents();
            const doesEventExist = allEvents.reduce(
                (newEventExists, existingEvent) => newEventExists ||
                    eventsEqual(newCalendarEvent, existingEvent),
                /* initialValue= */ false);

            if (!doesEventExist) {
              updateCalendarEventList(newCalendarEvent);
            }
          });
        }
      });
}

/**
 * Onclick function for 'Looks good to me, export" button.
 * Asks the user for Write access to the API scope.
 */
function addCalendarWriteScope() {
  var googleUser = GoogleAuth.currentUser.get();
  googleUser.grant({scope: SCOPE_CALENDAR_READ_WRITE})
      .then((response) => {
        $('#export-calendar-message').addClass('d-none');
        const scheduledTasks = collectAllScheduledTasks();
        addNewEventsToGoogleCalendar(scheduledTasks);
      })
      .catch(function(error) {
        handleExportAuthError(error);
      })
}

/**
 * Adds the scheduled task items back to the user's Google Calendar.
 */
function addNewEventsToGoogleCalendar() {
  const scheduledTasks = collectAllScheduledTasks();
  scheduledTasks.forEach((scheduledTask) => {
    const task = scheduledTask.task;

    const taskStartTimeMilliseconds = Date.parse(scheduledTask.date);
    const taskStartTime = new Date();

    taskStartTime.setTime(taskStartTimeMilliseconds);

    const taskEndTime = new Date();

    // This is x1000 because the functions takes milliseconds
    taskEndTime.setTime(
        taskStartTimeMilliseconds + (task.duration * 60 * 1000));

    const currentScheduledTask = {
      summary: task.name,
      description: task.description.value,
      start: {
        dateTime: taskStartTime.toISOString(),
      },
      end: {
        dateTime: taskEndTime.toISOString(),
      },
    };

    addOneEventToGoogleCalendar(currentScheduledTask);
  });
}

/** Adds an individual event to the authorized user's Google Calendar. */
function addOneEventToGoogleCalendar(event) {
  const request = gapi.client.calendar.events.insert(
      {calendarId: 'primary', resource: event});

  request.execute(function() {
    // Refreshes the calendar view so that the new event shows up on it.
    showCalendarView(gapi.auth2.getAuthInstance().currentUser.get());
  });
}

/**
 * Updates import message box based on the error during authentication process
 * for exporting
 */
function handleExportAuthError(e) {
  var $exportCalendarMessage = $('#export-calendar-message');

  var errorMessage;
  if (e.error === ERROR_CODES.popup_closed_by_user) {
    errorMessage =
        'It seems like you didn\'t complete the authorization process. ' +
        'Please click the Export button again.';
  } else if (e.error === ERROR_CODES.access_denied) {
    errorMessage =
        'You didn\'t give permission to update your Google Calendar, ' +
        'so your task schedule cannot be exported.'
  } else {
    errorMessage = 'An error occurred';
  }

  $exportCalendarMessage.text(errorMessage).removeClass('d-none');
}
