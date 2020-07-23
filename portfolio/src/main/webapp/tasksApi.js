var importMenuVisible;

/**
 * This function toggles between displaying and clearing the "import
 * tasks" menu. The importMenuVisible boolean is toggled inside the
 * functions that are called.
 */
function toggleTasks() {
  if (importMenuVisible) {
    clearImportMenu();
  } else {
    drawImportMenu();
  }
}

/**
 * Iterate through all the user's tasklists and pass
 * them to updateTaskList().
 */
function importAllTasks() {
  if (!GoogleAuth.isSignedIn.get()) {
    return;
  }

  gapi.client.tasks.tasklists.list({maxResults: 100}).then(function(response) {
    var taskLists = response.result.items;

    // Check that the variable exists so that no error is thrown.
    if (taskLists) {
      taskLists.forEach(tasklist => {
        importTasklist(tasklist.id);
      });
    }
  });
}

/** Import a single tasklist identified by its id. */
function importTasklist(tasklistId) {
  gapi.client.tasks.tasks
      .list({tasklist: tasklistId, maxResults: 100, showCompleted: false})
      .then(function(taskResponse) {
        var tasks = taskResponse.result.items;
        if (tasks) {
          tasks.forEach(task => {
            const newTask = new Task(task.title, task.notes, 60, 3);
            updateTaskList(newTask, TIME_UNIT.MINUTES);
          });
        }
      });
}

/**
 * Populate the import-menu-wrapper div with an import menu when the user is
 * logged in.
 */
function drawImportMenu() {
  importMenuVisible = true;

  const $button = $('#connect-tasks-btn')[0];
  $button.innerText = 'Unlink Tasks';

  // Create a div element to hold the custom select.
  const customSelect = document.getElementById('import-menu-wrapper');

  // Create the select part of the custom select.
  const tasklistSelect = document.createElement('select');
  tasklistSelect.classList.add('custom-select');
  tasklistSelect.setAttribute('id', 'import-select');
  var option = tasklistSelect.appendChild(document.createElement('option'));
  option.innerText = 'All Tasklists';

  // Add all Tasklists of user to the select.
  gapi.client.tasks.tasklists.list({maxResults: 30}).then(function(response) {
    var tasklists = response.result.items;
    if (tasklists) {
      tasklists.forEach(tasklist => {
        option = tasklistSelect.appendChild(document.createElement('option'));
        option.setAttribute('value', tasklist.id);
        option.innerText = tasklist.title;
      });
    }
  });

  // Append the select to the div holding our input group.
  customSelect.appendChild(tasklistSelect);

  // Create the button part of the custom select that will be appended
  // to the input group.
  const inputGroupAppend = document.createElement('div');
  inputGroupAppend.classList.add('input-group-append');

  const importButton = document.createElement('button');
  importButton.classList.add('btn', 'btn-outline-secondary');
  importButton.setAttribute('type', 'button');
  importButton.setAttribute('onclick', 'handleImportButtonPress()');
  importButton.innerText = 'Import';

  inputGroupAppend.appendChild(importButton);
  customSelect.appendChild(inputGroupAppend);
}

function clearImportMenu() {
  importMenuVisible = false;

  const $button = $('#connect-tasks-btn')[0];
  $button.innerText = 'Link Tasks';

  const menuWrapper = document.getElementById('import-menu-wrapper');
  menuWrapper.innerHTML = '';
}

function handleImportButtonPress() {
  const choice = document.getElementById('import-select').value;
  if (choice == 'All Tasklists') {
    importAllTasks();
  } else {
    importTasklist(choice);
  }
}

function clearTasks() {
  const toClear = document.getElementById('new-task-list');
  toClear.innerHTML = '';
}
