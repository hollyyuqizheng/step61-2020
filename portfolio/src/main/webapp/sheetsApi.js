// Discovery Docs for Sheets
const DISCOVERY_DOCS_SHEETS =
    'https://sheets.googleapis.com/$discovery/rest?version=v4';

// Authorization scope for read-write access to Sheets 
const SCOPE_SHEETS_READ_WRITE = 'https://www.googleapis.com/auth/spreadsheets';

// Error codes that the process can throw
const ERROR_CODE = {
  POPUP_CLOSED: 'popup_closed_by_user',
  ACCESS_DENIED: 'access_denied'
};

/**
 * On load, called to load the auth2 library and API client library.
 */
function handleClientLoad() {
  gapi.load('client:auth2', initClient);
}

/**
 * Initializes the API client library and sets up sign-in state
 * listeners.
 */
function initClient() {
  console.log("sheets: init client");
  if (!gapi.auth2.getAuthInstance()) {
    console.log("sheets: no auth, normal init"); 
    fetchApiKey();
    gapi.client
      .init({
        apiKey: API_KEY,
        clientId: CLIENT_ID,
        discoveryDocs: [DISCOVERY_URL_CALENDAR, DISCOVERY_DOCS_SHEETS],
        scope: SCOPE_SHEETS_READ_WRITE
      })
      .then(finishSheetsInit(),
            function(error) {
              handleError(error);
            });
  } else {
    console.log("sheets: auth exists, grant scope"); 
    var googleUser = gapi.auth2.getAuthInstance().currentUser.get();
    googleUser.grant({scope: SCOPE_SHEETS_READ_WRITE})
      .then(handleSheetsButtons());
  }  
}

/**
 * Runs after the promise is returned from gapi.client.init.
 * Assigns onclick functions to buttons related to Sheets.
 * Assign a listener to the signed-in status of the current user,
 * which will call the handleApiButtons library function that 
 * handles the display of all API-related buttons. 
 */
function finishSheetsInit() {
  gapi.auth2.getAuthInstance().isSignedIn.listen(handleApiButtons);
  handleSignIn(); 
}

/**
 * Called when the sign-in button is clicked. Handles signing in the user and
 * redirecting errors.
 */
function handleSignIn() {
  gapi.auth2.getAuthInstance().signIn()
    .then(handleSheetsButtons())
    .catch(function(error) {
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
function handleSheetsButtons() {
  const exportButton = document.getElementById('sheets-export-button');
  const signOutButton =
      document.getElementById('sheets-sign-out-button');
  exportButton.onclick = handleExportSchedule;
  signOutButton.onclick = handleSignOut;
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
  var spreadsheetProperties = {
    title: 'Your Scheduled Tasks'
    // TODO(tomasalvarez): Add desired creation properties.
  };

  var spreadsheetBody = {
      // TODO(tomasalvarez): Add desired properties to the request body.
  };

  var request = gapi.client.sheets.spreadsheets.create(
      {properties: spreadsheetProperties});
  request.then(
      function(response) {
        // TODO(tomasalvarez): Change code below to process the `response` 
        // object. Currently just displays the link back to the user.
        $('#sheets-url-container').removeClass('d-none');
        $('#sheets-url-container')
            .text(
                'Done. Here is the URL to your spreadsheet: ' +
                response.result.spreadsheetUrl);
      },
      function(reason) {
        handleExportError(reason);
      });
}
