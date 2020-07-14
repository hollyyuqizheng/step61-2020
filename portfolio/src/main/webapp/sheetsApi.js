// Client ID and API key from the Developer Console
const CLIENT_ID_SHEETS =
    '499747085593-hvi6n4kdrbbfvcuo1c9a9tu9oaf62cr2.apps.googleusercontent.com';
const API_KEY_SHEETS = '';

// Array of API discovery doc URLs for APIs used by the quickstart
const DISCOVERY_DOCS_SHEETS =
    ['https://sheets.googleapis.com/$discovery/rest?version=v4'];

// Authorization scopes required by the API; multiple scopes can be
// included, separated by spaces.
const SCOPE_READ_WRITE_SHEETS = 'https://www.googleapis.com/auth/spreadsheets';

const URL_DOCUMENT_BEGINNING = "https://docs.google.com/spreadsheets/d/";

// Error codes that the process can throw
const ERROR_CODE_SHEETS = {
  POPUP_CLOSED: 'popup_closed_by_user',
  ACCESS_DENIED: 'access_denied'
};

/**
 * On load, called to load the auth2 library and API client library.
 */
function handleClientLoadSheets() {
  gapi.load('client:auth2', initClientSheets);
}

/**
 * Initializes the API client library and sets up sign-in state
 * listeners.
 */
function initClientSheets() {
  gapi.client
      .init({
        apiKey: API_KEY_SHEETS,
        clientId: CLIENT_ID_SHEETS,
        discoveryDocs: DISCOVERY_DOCS_SHEETS,
        scope: SCOPE_READ_WRITE_SHEETS,
        consent: 'consent'
      })
      .then(
          function() {
            var exportButtonSheets =
                document.getElementById('sheets-export-button');
            var signOutButtonSheets =
                document.getElementById('sheets-sign-out-button');
            var signInButtonSheets =
                document.getElementById('sheets-sign-in-button');

            // Listen for sign-in state changes.
            gapi.auth2.getAuthInstance().isSignedIn.listen(handleUiSheets);

            // Handle the initial sign-in state.
            handleUiSheets();
            exportButtonSheets.onclick = handleExportSchedule;
            signOutButtonSheets.onclick = handleSignOut;
            signInButtonSheets.onclick = handleSignIn;
          },
          function(error) {
            handleError(error);
          });
}

/**
 * Called when the sign-in button is clicked. Handles signing in the user and
 * redirecting errors.
 */
function handleSignIn() {
  gapi.auth2.getAuthInstance().signIn().catch(function(error) {
    handleAuthorizationError(error);
  });
}

/**
 * Called when the sign-out button is clicked, handles signing out the user.
 */
function handleSignOut() {
  gapi.auth2.getAuthInstance().signOut();
}


/**
 * Called when the sign-in state changes and updates the UI appropriately.
 */
function handleUiSheets() {
  $('#sheets-export-button').addClass('d-none');
  if (gapi.auth2.getAuthInstance().isSignedIn.get()) {
    $('#sheets-export-button').removeClass('d-none');
    $('#sheets-sign-out-button').removeClass('d-none');
    $('#sheets-sign-in-button').addClass('d-none');
  } else {
    $('#sheets-export-button').addClass('d-none');
    $('#sheets-sign-out-button').addClass('d-none');
    $('#sheets-sign-in-button').removeClass('d-none');
  }
}

/**
 * Called when the sign-in process throws an error and displays the error
 * message on the UI.
 */
function handleAuthorizationError(error) {
  var $sheetsMessage = $('#sheets-message');
  $sheetsMessage.removeClass('d-none');
  if (error.error === ERROR_CODE_SHEETS.POPUP_CLOSED) {
    $sheetsMessage.text('You closed out of the popup, please log in again.');
  } else if (error.error === ERROR_CODE_SHEETS.ACCESS_DENIED) {
    $sheetsMessage.text('You did not authorize Google Sheets.');
  } else {
    $sheetsMessage.text('An error occurred try again.');
  }
  $sheetsMessage.show();
}

/**
 * Called when the export process throws an error and displays the error
 * message on the UI.
 */
function handleExportError(reason) {
  $('#sheets-message')
      .removeClass('d-none')
      .text('Error: ' + reason.result.error.message)
      .show();
}

/**
 * Helps the SheetsApi requests easily create the values it needs for a
 * spreadsheet.
 */
function makeSheetsValuesFromScheduledTasks(scheduledTasks, values) {
  for (index = 0; index < scheduledTasks.length; index++) {
    values.push(singleScheduledTaskToSheetsArray(scheduledTasks[index]));
  }
}

/**
 * This takes in a single JSON object of a ScheduledTask (Java Class) and
 * turns it into the correct format for the Sheets API
 */
function singleScheduledTaskToSheetsArray(scheduledTask) {
  var scheduledTaskAsSheetsArray = [];

  const task = scheduledTask.task;
  const taskName = task.name;
  // Changes seconds into minutes.
  const taskDurationMinutes = task.duration.seconds / 60;
  const taskDescription = task.description.value;
  const taskPriority = task.priority.priority;
  const taskTimeSeconds = scheduledTask.startTime.seconds;
  const taskDate = new Date();
  // This is x1000 because the functions takes milliseconds
  taskDate.setTime(taskTimeSeconds * 1000);

  scheduledTaskAsSheetsArray.push(taskName);
  scheduledTaskAsSheetsArray.push(taskDate.toString());
  scheduledTaskAsSheetsArray.push(taskDurationMinutes);
  scheduledTaskAsSheetsArray.push(taskDescription);
  scheduledTaskAsSheetsArray.push(taskPriority);

  return scheduledTaskAsSheetsArray;
}

/**
 * Creates and populates a new spreadsheet with the scheduled task information.
 * Also displays the link to the spreadsheet as text on the UI.
 */
function handleExportSchedule() {
  const scheduledTaskCount = scheduledTasks.length;
  if (scheduledTaskCount == 0) {
    $('#sheets-message')
        .removeClass('d-none')
        .text('Cannot export empty schedule.')
        .show();
    return;
  }

  var spreadsheetProperties = {
    title: 'Your Scheduled Tasks'
    // TODO(tomasalvarez): Include suggestions of adding the creation time
    //     in the title of the spreadsheet.
  };

  var request = gapi.client.sheets.spreadsheets.create(
      {properties: spreadsheetProperties});
  request.then((response) => {
    var values = [[
      'Task', 'Scheduled Time', 'Duration (minutes)', 'Description', 'Priority'
    ]];
    // This is using the new scheduledTasks variable which is a local copy
    // of the latest results from scheduling.
    makeSheetsValuesFromScheduledTasks(scheduledTasks, values);
    // This is a blank row in the spreadsheet so you can see the totals easier.
    values.push([]);
    values.push([
      scheduledTaskCount.toString() + ' Tasks Total', '',
      '=SUM(C2:C' + (scheduledTaskCount + 1).toString() +
          ') & " Total Minutes Scheduled"'
    ])

    var data = {
      // This is +3 because we have the initial row with titles and the final
      // rows with some totals.
      'range': 'Sheet1!A1:E' + (scheduledTaskCount + 3).toString(),
      'majorDimension': 'ROWS',
      'values': values
    }  // Additional ranges to update.

    var body = {data: data, valueInputOption: 'USER_ENTERED'};
    gapi.client.sheets.spreadsheets.values
        .batchUpdate(
            {spreadsheetId: response.result.spreadsheetId, resource: body})
        .then(
            function(response) {
              //  This displays the link back to the user.
              $('#sheets-url-container').removeClass('d-none');
              $('#sheets-url-container')
                  .text(
                      'Here is the URL to your spreadsheet: ' +
                      URL_DOCUMENT_BEGINNING + response.result.responses[0].spreadsheetId);
            },
            function(reason) {
              handleExportError(reason);
            });
  });
}
