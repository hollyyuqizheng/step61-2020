const URL_DOCUMENT_BEGINNING = "https://docs.google.com/spreadsheets/d/"; 

/**
 * On load, called to load the auth2 library and API client library.
 */
// function handleClientLoad() {
//   gapi.load('client:auth2', initClient);
// }

/**
 * Initializes the API client library and sets up sign-in state
 * listeners.
 */
// function initClient() {
//   console.log("sheets: init client");
//   if (!gapi.auth2.getAuthInstance()) {
//     console.log("sheets: no auth, normal init"); 
//     fetchApiKey();
//     gapi.client
//       .init({
//         apiKey: API_KEY,
//         clientId: CLIENT_ID,
//         discoveryDocs: [DISCOVERY_URL_CALENDAR, DISCOVERY_DOCS_SHEETS],
//         scope: SCOPE_SHEETS_READ_WRITE
//       })
//       .then(handleSignIn(),
//             function(error) {
//               handleError(error);
//             });
//   } else {
//     console.log("sheets: auth exists, grant scope"); 
//     var googleUser = gapi.auth2.getAuthInstance().currentUser.get();
//     googleUser.grant({scope: SCOPE_SHEETS_READ_WRITE})
//       .then(handleApiButtons());
//   }  
// }

/**
 * Runs after the promise is returned from gapi.client.init.
 * Assigns onclick functions to buttons related to Sheets.
 * Assign a listener to the signed-in status of the current user,
 * which will call the handleApiButtons library function that 
 * handles the display of all API-related buttons. 
 */
// function finishSheetsInit() {
//   gapi.auth2.getAuthInstance().isSignedIn.listen(handleApiButtons);
//   handleSignIn(); 
// }

/**
 * Called when the sign-in button is clicked. Handles signing in the user and
 * redirecting errors.
 */
function handleSignIn() {
  gapi.auth2.getAuthInstance().signIn()
    .then(handleApiButtons())
    .catch(function(error) {
      handleAuthorizationError(error);
  });
}

/**
 * Called when the sign-in process throws an error and displays the error
 * message on the UI.
 */
function handleAuthorizationError(error) {
  var $sheetsMessage = $('#sheets-message');
  $sheetsMessage.removeClass('d-none');
  if (error.error === ERROR_CODE.POPUP_CLOSED) {
    $sheetsMessage.text('You closed out of the popup, please log in again.');
  } else if (error.error === ERROR_CODE.ACCESS_DENIED) {
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
  // I think this code is shorter without a forEach() and I also do not
  // know how to make this work with a forEach().
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
 * Called when the export process throws an error and displays the error
 * message on the UI.
 */
function handleExportError(reason) {
  var $sheetsMessage = $('#sheets-message');
  $sheetsMessage.removeClass('d-none')
      .text('Error: ' + reason.result.error.message)
      .show();
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
    // TODO(tomasalvarez): Include James suggestion of adding the creation time
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
    //console.log(values);

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
                  .html(
                      'Done. Here is the ' + 
                      '<a href=' + 
                      URL_DOCUMENT_BEGINNING + 
                      response.result.responses[0].spreadsheetId + 
                      ' target="_blank">link</a>' + ' to your spreadsheet!');                  
            },
            function(reason) {
              handleExportError(reason);
            });
  });

}
