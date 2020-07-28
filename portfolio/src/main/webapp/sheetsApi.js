const URL_DOCUMENT_BEGINNING = 'https://docs.google.com/spreadsheets/d/';

/**
 * Initializes the API client library and sets up sign-in state
 * listeners.
 */
function initClientSheets() {
  var sheetsExportButton = document.getElementById('sheets-export-button');
  var sheetsSignOutButton = document.getElementById('sheets-sign-out-button');
  var sheetsSignInButton = document.getElementById('sheets-sign-in-button');

  // Handle the initial sign-in state.
  handleUiSheets();
  sheetsExportButton.onclick = handleExportSchedule;
  sheetsSignOutButton.onclick = handleSignOut;
  sheetsSignInButton.onclick = handleSignIn;
}

/**
 * Called when the sign-in state changes and updates the UI appropriately.
 */
function handleUiSheets() {
  $('#sheets-export-button').addClass('d-none');
  if (GoogleAuth.isSignedIn.get()) {
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
 * Helps the SheetsApi requests easily create the values it needs for a
 * spreadsheet.
 * @param scheduledTasks: An array of JSON ScheduledTask objects
 * @param values: An empty array where the parsed data will be put
 */
function makeSheetsValuesFromScheduledTasks(scheduledTasks, values) {
  for (var index = 0; index < scheduledTasks.length; index++) {
    values.push(singleScheduledTaskToSheetsArray(scheduledTasks[index]));
  }
}

// TODO(tomasalvarez): make unit tests for this method
/**
 * This takes in a single JSON object of a ScheduledTask (Java Class) and
 * turns it into the correct format for the Sheets API
 */
function singleScheduledTaskToSheetsArray(scheduledTask) {
  var scheduledTaskAsSheetsArray = [];

  const task = scheduledTask.task;
  const taskName = task.name;
  // Changes seconds into minutes.
  const taskDurationMinutes = task.duration;
  const taskDescription = task.description;
  const taskPriority = task.taskPriority;
  const taskDate = scheduledTask.date;

  scheduledTaskAsSheetsArray.push(taskName);
  scheduledTaskAsSheetsArray.push(taskDate);
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
  const scheduledTasks = collectAllScheduledTasks();
  const scheduledTaskCount = scheduledTasks.length;
  if (scheduledTaskCount == 0) {
    $('#sheets-message')
        .text('Cannot export empty schedule.')
        .removeClass('d-none')
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
      range: 'Sheet1!A1:E' + (scheduledTaskCount + 3).toString(),
      majorDimension: 'ROWS',
      values: values
    }  // Additional ranges to update.

    var body = {data: data, valueInputOption: 'USER_ENTERED'};
    gapi.client.sheets.spreadsheets.values
        .batchUpdate(
            {spreadsheetId: response.result.spreadsheetId, resource: body})
        .then(
            function(response) {
              //  This displays the link back to the user.
              $('#sheets-url-container')
                  .attr(
                      'href',
                      URL_DOCUMENT_BEGINNING +
                          response.result.responses[0].spreadsheetId)
                  .removeClass('d-none');
            },
            function(reason) {
              handleExportError(reason);
            });
  });
}
