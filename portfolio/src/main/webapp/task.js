const TIME_UNIT = {
  MINUTES: 'minutes',
  HOURS: 'hours'
};

var ID_COUNTER = 0;

/**
 * Models a task that is displayed on the UI.
 * This class is useful when the task information needs to be sent
 * to the task servlet through a POST request.
 * This class contains all the task information that can be easily
 * converted into a JSON string.
 */
class Task {
  /**
   * All fields are required except description, which will be null
   * when not set.
   * @param name: name of task as String
   * @param description: description of task as String, can be null
   * @param duration: duration of task in minutes as an integer
   * @param taskPriority: priority level of task, range 1-5 inclusive.
   */
  constructor(name, description, duration, taskPriority) {
    this.name = name;
    this.description = description;
    this.duration = duration;
    this.taskPriority = taskPriority;
  }
}

/**
 * This method grabs the required inputs from the front end of the page that
 * will populate the Task's information. It requires that the front end fields
 * for name, and duration to be passed in. Once it checks for the required
 * inputs, a JavaScript task class is created and passed into the
 * updateTaskList function. Return statements are used only to end the function.
 */
function createNewTask() {
  const name = $('#new-task-name').val();
  const description = $('#new-task-description').val();
  const length = parseInt($('#new-task-estimated-length').val());
  const lengthUnit = $('#new-task-estimated-length-unit').val();
  const priority = parseInt($('#new-task-priority').val());

  // Show error message if an input is found to be invalid.
  // Error messages are shown by adding the class 'd-block' because the error
  // message elements have class 'invalid-feedback' which hide the element by
  // default.
  if (validateTaskName(name).isValid) {
    $('#empty-name-message').removeClass('d-block');
  } else {
    $('#empty-name-message').addClass('d-block');
    return;
  }

  if (validateTaskDuration(length).isValid) {
    $('#task-length-message').removeClass('d-block');
  } else {
    $('#task-length-message').addClass('d-block');
    return;
  }

  const newTask = new Task(
      name, description, getDurationMinutes(length, lengthUnit),
      parseInt(priority));

  updateTaskList(newTask, lengthUnit);
}

/** Display Task information from user input. */
function updateTaskList(newTask, lengthUnit) {
  const newTaskCard = document.createElement('div');
  newTaskCard.classList.add('card');

  const cardBody = document.createElement('div');
  cardBody.classList.add('card-body');
  newTaskCard.appendChild(cardBody);

  const cardTitle = document.createElement('h4');
  cardTitle.classList.add('card-title');
  cardTitle.innerText = newTask.name;
  cardBody.appendChild(cardTitle);

  const descriptionText = document.createElement('p');
  descriptionText.classList.add('card-text');
  descriptionText.innerText = newTask.description;
  cardBody.appendChild(descriptionText);

  // WORK IN HERE
  // Create row for all modifiable content
  const inputRow = document.createElement('div');
  inputRow.classList.add('form-row');

  // Create a column for duration
  const durationColumn = document.createElement('div');
  durationColumn.classList.add('col');

  const durationLabel = document.createElement('label');
  durationLabel.setAttribute('for', 'duration-input-' + ID_COUNTER);
  durationLabel.innerText = 'Duration:';

  const durationInput = document.createElement('input');
  durationInput.classList.add('form-control');
  durationInput.setAttribute('id', 'duration-input-' + ID_COUNTER);
  durationInput.setAttribute('value', newTask.duration);

  durationColumn.appendChild(durationLabel);
  durationColumn.appendChild(durationInput);
  inputRow.appendChild(durationColumn);

  // Create a column for unit
  const unitColumn = document.createElement('div');
  unitColumn.classList.add('col');

  const unitLabel = document.createElement('label');
  unitLabel.setAttribute('for', 'unit-select-' + ID_COUNTER);
  unitLabel.innerText = 'Unit:';

  const unitSelect = document.createElement('select');
  unitSelect.classList.add('form-control');
  unitSelect.setAttribute('id', 'unit-select-' + ID_COUNTER);
  unitSelect.setAttribute('selected', lengthUnit);

  var option = unitSelect.appendChild(document.createElement('option'));
  option.setAttribute('value', TIME_UNIT.MINUTES);
  option.innerText = 'minute(s)';

  var optionTwo = unitSelect.appendChild(document.createElement('option'));
  optionTwo.setAttribute('value', TIME_UNIT.HOURS);
  optionTwo.innerText = 'hour(s)';

  if (lengthUnit == TIME_UNIT.MINUTES) {
    option.setAttribute('selected', '');
  } else {
    optionTwo.setAttribute('selected', '');
  }

  unitColumn.appendChild(unitLabel);
  unitColumn.appendChild(unitSelect);
  inputRow.appendChild(unitColumn);

  // Create a column for priority
  const priorityColumn = document.createElement('div');
  priorityColumn.classList.add('col');

  const priorityLabel = document.createElement('label');
  priorityLabel.setAttribute('for', 'priority-select-' + ID_COUNTER);
  priorityLabel.innerText = 'Priority:';

  const prioritySelect = document.createElement('select');
  prioritySelect.classList.add('form-control');
  prioritySelect.setAttribute('id', 'priority-select-' + ID_COUNTER);

  for (var i = 1; i <= 5; i++) {
    option = prioritySelect.appendChild(document.createElement('option'));
    option.innerText = i;
    if (i == newTask.taskPriority) {
      option.setAttribute('selected', '');
    }
  }

  priorityColumn.appendChild(priorityLabel);
  priorityColumn.appendChild(prioritySelect);
  inputRow.appendChild(priorityColumn);

  cardBody.appendChild(inputRow);

  const deleteButton = document.createElement('button');
  deleteButton.classList.add('btn');
  deleteButton.classList.add('btn-danger');
  deleteButton.innerText = 'Delete this task';
  cardBody.appendChild(deleteButton);

  const taskList = document.getElementById('new-task-list');
  taskList.innterHTML = '';
  taskList.appendChild(newTaskCard);

  ID_COUNTER++;

  // The delete button removes the task's card from the UI.
  deleteButton.onclick = function(newTaskCard) {
    newTaskCard.target.closest('div.card').remove();
  }
}

/** Returns the number of minutes from the user's input and unit selection. */
function getDurationMinutes(duration, unit) {
  if (unit == TIME_UNIT.MINUTES) {
    return parseInt(duration);
  } else if (unit == TIME_UNIT.HOURS) {
    return parseInt(duration) * 60;
  }
}

/**
 * Returns JSON data from an array of Javascript Task classes
 * by gathering the information from the previously created
 * cards used to display input Task information.
 */
function collectAllTasks() {
  const allTaskJson = new Array();

  // Get task list element to run through cards collecting data.
  const taskList = document.getElementById('new-task-list');

  taskList.childNodes.forEach((taskCard) => {
    const taskBody = taskCard.childNodes[0];

    const taskName = taskBody.childNodes[0].innerText;
    const taskDescription = taskBody.childNodes[1].innerText;
    const taskLength = taskBody.childNodes[2].childNodes[0].childNodes[1].value;
    const taskLengthUnit =
        taskBody.childNodes[2].childNodes[1].childNodes[1].value;
    const taskPriority =
        parseInt(taskBody.childNodes[2].childNodes[2].childNodes[1].value);

    const task = new Task(
        taskName, taskDescription,
        getDurationMinutes(taskLength, taskLengthUnit), taskPriority);
    const taskJson = JSON.stringify(task);
    allTaskJson.push(taskJson);
  });
  return allTaskJson;
}

/**
 * This method checks that the name input is not an empty string.
 * In the case that an empty string is recieved, an error message is displayed.
 * The method returns an object containing a bool to declare whether the input
 * is valid and an error message.
 */
function validateTaskName(name) {
  if (name == '') {
    return {isValid: false, errorMessage: 'Name cannot be empty.'};
  } else {
    return {isValid: true, errorMessage: null};
  }
}

/**
 * This method checks that the duration input by the user is a positive integer.
 * If it is not, an error message is displayed. The method returns an object
 * containing a bool to declare whether the input is valid and an error message.
 */
function validateTaskDuration(duration) {
  if (parseInt(duration) <= 0 || !Number.isInteger(duration)) {
    return {isValid: false, errorMessage: 'Duration input is invalid.'};
  } else {
    return {isValid: true, errorMessage: null};
  }
}
