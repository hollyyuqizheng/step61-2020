// require(['dotenv']).config({path: './.env'});

var GoogleAuth;

// Scopes for API access to Google Calendar
const SCOPE_READ_ONLY = 'https://www.googleapis.com/auth/calendar.readonly';
const SCOPE_READ_WRITE = 'https://www.googleapis.com/auth/calendar';

// For the discovery document for Google Calendar API.
const DISCOVERY_URL =
    'https://www.googleapis.com/discovery/v1/apis/calendar/v3/rest';

const CLIENT_ID =
    '499747085593-hvi6n4kdrbbfvcuo1c9a9tu9oaf62cr2.apps.googleusercontent.com';

// Constants for error codes during OAuth process.
const ERROR_CODE_POPUP_CLOSED = 'popup_closed_by_user';
const ERROR_CODE_ACCESS_DENIED = 'access_denied';

/**
 * Loads the API's client and auth2 modules.
 * Calls the initClient function after the modules load.
 */
function initiateCalendarAuth() {
  gapi.load('client:auth2', initClient);
}

/** Starts authentication flow based on current user's login status. */
function initClient() {
  // Hide the error messages by default.
  // These messages will appear if authentication errors are caught.
  hideErrorMessages();

  // Initializes the gapi.client object, which app uses to make API requests.
  // Initially, the scope is read-only to view user's Google Calendar.
  gapi.client
      .init({
        'apiKey': API_KEY,
        'clientId': CLIENT_ID,
        'discoveryDocs': [DISCOVERY_URL],
        'scope': SCOPE_READ_ONLY
      })
      .then(function() {
        GoogleAuth = gapi.auth2.getAuthInstance();

        // Listen for sign-in state changes.
        GoogleAuth.isSignedIn.listen(updateCalendarView);

        // Handle initial sign-in state. (Determine if user is already signed
        // in.)
        handleAuth();
      });
}

/** Signs user in if not logged in, and signs user out otherwise. */
function handleAuth() {
  if (GoogleAuth.isSignedIn.get()) {
    GoogleAuth.signOut();
  } else {
    // User is not signed in. Start Google auth flow.
    GoogleAuth.signIn().catch(function(error) {
      handleImportAuthError(error);
    });
  }
}

/**
 * Updates import message box based on the error during authentication process
 * for importing
 */
function handleImportAuthError(e) {
  $('#import-calendar-message').removeClass('d-none');
  if (e.error === ERROR_CODE_POPUP_CLOSED) {
    $('#import-calendar-message')
        .text(
            'It seems like you didn\'t complete the authorization process. ' +
            'Please click the Login button again.');
  } else if (e.error === ERROR_CODE_ACCESS_DENIED) {
    $('#import-calendar-message')
        .text(
            'You didn\'t give permission to view your Google Calendar, ' +
            'so your calendar events cannot be viewed or imported.');
  }
  $('#import-calendar-message').show();
}

/** Disconnects current user authentication. */
function revokeAccess() {
  GoogleAuth.disconnect();
}

/** Updates the calendar view and button visibility based on login status. */
function updateCalendarView() {
  var user = GoogleAuth.currentUser.get();
  var isAuthorized = user.hasGrantedScopes(SCOPE_READ_ONLY);
  if (isAuthorized) {
    showCalendarView(user);
    $('#calendar-auth-button').attr('disabled', 'true');
    $('#calendar-auth-button').attr('aria-disabled', 'true');
    $('#import-calendar-button').removeClass('d-none');
    $('#import-calendar-button').show();
  } else {
    $('#calendar-auth-button').attr('disabled', 'false');
    $('#calendar-auth-button').attr('aria-disabled', 'false');
    $('#import-calendar-button').hide();
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
 * Print the summary and start datetime/date of the next ten events in
 * the authorized user's calendar. If no events are found an
 * appropriate message is printed.
 */
function listUpcomingEvents() {
  const timeRangeStart = getUserPickedDate();

  const timeRangeEnd = new Date();
  timeRangeEnd.setFullYear(timeRangeStart.getFullYear());
  timeRangeEnd.setMonth(timeRangeStart.getMonth());
  timeRangeEnd.setDate(timeRangeStart.getDate() + 1);

  // Only import events for the next 24 hours.
  // const timeRangeStart = new Date();  // this is a timestamp of now
  // const timeRangeEnd = new Date();
  // timeRangeEnd.setDate(timeRangeStart.getDate() + 1);

  gapi.client.calendar.events
      .list({
        'calendarId': 'primary',
        'timeMin': timeRangeStart.toISOString(),
        'timeMax': timeRangeEnd.toISOString(),
        'showDeleted': false,
        'singleEvents': true,
        'orderBy': 'startTime'
      })
      .then(function(response) {
        var events = response.result.items;

        // Show message for no imported event if result list is empty.
        if (events.length == 0) {
          $('#empty-calendar-import-message').removeClass('d-none');
          $('#empty-calendar-import-message').show();
        } else {
          for (i = 0; i < events.length; i++) {
            const event = events[i];
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
          }
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

  gapi.client.calendar.settings.get({'setting': 'timezone'})
      .then((responseTimeZone) => {
        const userCalendarTimeZone =
            encodeURIComponent(responseTimeZone.result.value);
        const calendarViewUrl =
            'https://calendar.google.com/calendar/embed?src=' + userEmail +
            '&ctz=' + userCalendarTimeZone + '&mode=WEEK';

        // Updates the UI so that calendar view appears.
        $('#calendar-view').removeClass('d-none');
        $('#calendar-view').attr('src', calendarViewUrl);
        $('#calendar-view').show();
      });
}

/**
 * Asks the user for Write access to the API scope.
 */
function addWriteScope() {
  hideErrorMessages();
  var GoogleUser = GoogleAuth.currentUser.get();
  GoogleUser.grant({'scope': SCOPE_READ_WRITE})
      .then((response) => {
        addNewEventsToGoogleCalendar();
      })
      .catch(function(error) {
        handleExportAuthError(error);
      })
}

/**
 * Adds the scheduled task items back to the user's Google Calendar.
 * TODO(hollyyuqizheng): change the hard-coded part later.
 */
function addNewEventsToGoogleCalendar() {
  const event = {
    'summary': 'Testing',
    'description': 'test description',
    'start': {
      'dateTime': '2020-07-01T07:00:00Z',
    },
    'end': {
      'dateTime': '2020-07-01T17:00:00Z',
    },
  };

  addOneEventToGoogleCalendar(event);
}

/** Adds an individual event to the authorized user's Google Calendar. */
function addOneEventToGoogleCalendar(event) {
  const request = gapi.client.calendar.events.insert(
      {'calendarId': 'primary', 'resource': event});

  request.execute(function() {
    // Refreshes the calendar view so that the new event shows up on it.
    showCalendarView(GoogleAuth.currentUser.get());
  });
}

/**
 * Updates import message box based on the error during authentication process
 * for exporting
 */
function handleExportAuthError(e) {
  $('#export-calendar-message').removeClass('d-none');
  if (e.error === ERROR_CODE_POPUP_CLOSED) {
    $('#export-calendar-message')
        .text(
            'It seems like you didn\'t complete the authorization process. ' +
            'Please click the Export button again.');
  } else if (e.error === ERROR_CODE_ACCESS_DENIED) {
    $('#export-calendar-message')
        .text(
            'You didn\'t give permission to update your Google Calendar, ' +
            'so your calendar events cannot be exported.');
  }
  $('#export-calendar-message').show();
}

/** Hides the error messages when authentication completes succesfully. */
function hideErrorMessages() {
  $('#import-calendar-message').hide();
  $('#export-calendar-message').hide();
}
