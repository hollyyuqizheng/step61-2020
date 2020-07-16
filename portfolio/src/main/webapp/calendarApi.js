var googleAuth;

// Scopes for API access to Google Calendar
const SCOPE_READ_ONLY = 'https://www.googleapis.com/auth/calendar.readonly';
const SCOPE_READ_WRITE = 'https://www.googleapis.com/auth/calendar';

// For the discovery document for Google Calendar API.
const DISCOVERY_URL =
    'https://www.googleapis.com/discovery/v1/apis/calendar/v3/rest';

const CLIENT_ID =
    '499747085593-hvi6n4kdrbbfvcuo1c9a9tu9oaf62cr2.apps.googleusercontent.com';

// Object for all error codes
const ERROR_CODES = {
  popup_closed_by_user: 'popup_closed_by_user',
  access_denied: 'access_denied'
}; 

/**
 * Loads the API's client and auth2 modules.
 * Calls the initClient function after the modules load.
 */
function initiateCalendarAuth() {
  gapi.load('client:auth2', initClient);
}

/** Starts authentication flow based on current user's login status. */
function initClient() {
  // Initializes the gapi.client object, which app uses to make API requests.
  // Initially, the scope is read-only to view user's Google Calendar.
  gapi.client
      .init({
        apiKey: API_KEY,
        clientId: CLIENT_ID,
        discoveryDocs: [DISCOVERY_URL],
        scope: SCOPE_READ_ONLY
      })
      .then(function() {
        googleAuth = gapi.auth2.getAuthInstance();

        // Listen for sign-in state changes.
        googleAuth.isSignedIn.listen(updateCalendarView);

        // Handle initial sign-in state. (Determine if user is already signed
        // in.)
        handleAuth();
      });
}

/** Signs user in if not logged in, and signs user out otherwise. */
function handleAuth() {
  if (googleAuth.isSignedIn.get()) {
    googleAuth.signOut();
  } else {
    // User is not signed in. Start Google auth flow.
    googleAuth.signIn()
        .then((response) => {
          var $importCalendarMessage = $('#import-calendar-message');
          $importCalendarMessage.addClass('d-none');
        })
        .catch(function(error) {
          handleImportAuthError(error);
        });
  }
}

/**
 * Updates import message box based on the error during authentication process
 * for importing.
 */
function handleImportAuthError(e) {
  var $importCalendarMessage = $('#import-calendar-message');

  var errorMessage;
  if (e.error === ERROR_CODES.popup_closed_by_user) {
    errorMessage =
        'It seems like you didn\'t complete the authorization process. ' +
        'Please click the Login button again.'
  } else if (e.error === ERROR_CODES.access_denied) {
    errorMessage =
        'You didn\'t give permission to view your Google Calendar, ' +
        'so your calendar events cannot be viewed or imported.'
  } else {
    errorMessage = 'An error occurred.';
  }
  $importCalendarMessage.text(errorMessage).removeClass('d-none');
}

/** Disconnects current user authentication. */
function revokeAccess() {
  googleAuth.disconnect();
}

/** Updates the calendar view and button visibility based on login status. */
function updateCalendarView() {
  const googleUser = googleAuth.currentUser.get();
  const isAuthorized = googleUser.hasGrantedScopes(SCOPE_READ_ONLY);
  if (isAuthorized) {
    showCalendarView(googleUser);
    $('#calendar-auth-button').attr('disabled', 'true');
    $('#calendar-auth-button').attr('aria-disabled', 'true');
    $('#import-calendar-button').removeClass('d-none');
    $('#export-calendar-button').removeClass('d-none');
  } else {
    $('#calendar-auth-button').attr('disabled', 'false');
    $('#calendar-auth-button').attr('aria-disabled', 'false');
    $('#import-calendar-button').addClass('d-none');
    $('#export-calendar-button').addClass('d-none');
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
        orderBy: 'startTime',
        showDeleted: false,
        singleEvents: true,
        status: 'confirmed', 
        timeMin: timeRangeStart.toISOString(),
        timeMax: timeRangeEnd.toISOString(),
      })
      .then(function(response) {
        var events = response.result.items;
        var $emptyCalendarMessage = $('#empty-calendar-import-message');

        // Show message for no imported event if result list is empty.
        if (events.length == 0) {
          const pickedDate = $('#date-picker').val();
          $emptyCalendarMessage.text(
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
 * Retrieves the logged in user's Google email and uses that email
 * to embed the user's Google Calendar on the UI.
 * The calendar is displayed in the user's Google Calendar's time zone.
 */
function showCalendarView(user) {
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
        $('#calendar-view').attr('src', calendarViewUrl).removeClass('d-none');
      });
}

/**
 * Onclick function for 'Looks good to me, export" button.
 * Asks the user for Write access to the API scope.
 */
function addWriteScope() {
  var googleUser = googleAuth.currentUser.get();
  googleUser.grant({scope: SCOPE_READ_WRITE})
      .then((response) => {
        $('#export-calendar-message').addClass('d-none');
        addNewEventsToGoogleCalendar();
      })
      .catch(function(error) {
        handleExportAuthError(error);
      })
}

/**
 * Adds the scheduled task items back to the user's Google Calendar.
 * TODO(hollyyuqizheng): fill in the rest of the method once
 * results can be returned from the scheduling algorithm.
 */
function addNewEventsToGoogleCalendar() {
  const event = {};
  addOneEventToGoogleCalendar(event);
}

/** Adds an individual event to the authorized user's Google Calendar. */
function addOneEventToGoogleCalendar(event) {
  const request = gapi.client.calendar.events.insert(
      {calendarId: 'primary', resource: event, visibility: 'private'});

  request.execute(function() {
    // Refreshes the calendar view so that the new event shows up on it.
    showCalendarView(googleAuth.currentUser.get());
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
