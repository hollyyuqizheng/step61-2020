
// Client ID and API key from the Developer Console
var CLIENT_ID =
    '499747085593-hvi6n4kdrbbfvcuo1c9a9tu9oaf62cr2.apps.googleusercontent.com';
var API_KEY = 'AIzaSyAcS8-F743Xy2ZTSYxxZIB0nY5kDQQ8i7E';

// Array of API discovery doc URLs for APIs used by the quickstart
var DISCOVERY_DOCS =
    ['https://sheets.googleapis.com/$discovery/rest?version=v4'];

// Authorization scopes required by the API; multiple scopes can be
// included, separated by spaces.
var SCOPES = 'https://www.googleapis.com/auth/spreadsheets';

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
  gapi.client
      .init({
        apiKey: API_KEY,
        clientId: CLIENT_ID,
        discoveryDocs: DISCOVERY_DOCS,
        scope: SCOPES
      })
      .then(
          function() {
            var exportButton = document.getElementById('sheets-export-button');
            var signOutButton =
                document.getElementById('sheets-sign-out-button');
            var signInButton = document.getElementById('sheets-sign-in-button');

            // Listen for sign-in state changes.
            gapi.auth2.getAuthInstance().isSignedIn.listen(handleButtons);

            // Handle the initial sign-in state.
            handleButtons();
            exportButton.onclick = handleExportSchedule;
            signOutButton.onclick = handleSignOut;
            signInButton.onclick = handleSignIn;
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
    handlenError(error);
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
function handleButtons() {
  $('#sheets-message').addClass('d-none');
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
 * Called when the sign-in process throws an error and displays an error
 * message on the UI.
 */
function handleError(error) {
  $('#sheets-message').removeClass('d-none');
  if (error.error === 'popup_closed_by_user') {
    $('#sheets-message')
        .text('You closed out of the popup, please log in again.');
  } else if (error.error === 'access_denied') {
    $('#sheets-message').text('You did not authorize Google Sheets.');
  } else if (typeof error === 'string') {
    $('#sheets-message').text('Error: ' + error);
  } else {
    $('#sheets-message').text('An error occurred try again.');
  }
  $('#sheets-message').show();
}

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
        // TODO: Change code below to process the `response` object:
        console.log(response.result);
        $('#sheets-url-container').removeClass('d-none');
        $('#sheets-url-container')
            .text(
                'Done. Here is the URL to your spreadsheet: ' +
                response.result.spreadsheetUrl);
      },
      function(reason) {
        handleError(reason.result.error.message);
      });
}
