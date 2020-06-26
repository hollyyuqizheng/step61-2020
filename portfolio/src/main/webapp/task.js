/**
 * Models a task that is displayed on the UI.
 * This class is useful when the task information needs to be sent
 * to the task servlet through a POST request.
 * This class contains all the task information that can be easily
 * converted into a JSON string.
 */
class Task {
  // TODO(raulcruise): could you add the type for each param please?

  /**
   * All fields are required except description, which will be null
   * when not set.
   * @param name: name of task as String
   * @param description: description of task as String, can be null
   * @param duration: duration of task as String class
   * @param priority: priority level of task, range 1-5 inclusive.
   */
  constructor(name, description, duration, taskPriority) {
    this.name = name;
    this.description = description;
    this.duration = duration;
    this.taskPriority = taskPriority;
  }
}

function createNewTask() {
  const name = document.getElementById('new-task-name').value;
  const description = document.getElementById('new-task-description').value;
  const length = document.getElementById('new-task-estimated-length').value;
  const lengthUnit =
      document.getElementById('new-task-estimated-length-unit').value;
  const priority = document.getElementById('new-task-priority').value;

  const dataArray =
      [name, description, getDuration(length, lengthUnit), priority];

  if (name == '') {
    return false;
  }

  if (parseInt(length) <= 0 || length == '') {
    return false;
  }

  const taskDataArrayJson = JSON.stringify(dataArray);
  console.log(taskDataArrayJson);

  const newTask =
      new Task(name, description, parseInt(length), parseInt(priority));
  updateTaskList(newTask, lengthUnit);
}

function updateTaskList(newTask, lengthUnit) {
  const newEventCard = document.createElement('div'); 
  newEventCard.classList.add('card');

  const cardBody = document.createElement('div');
  cardBody.classList.add('card-body');
  newEventCard.appendChild(cardBody);

  const cardTitle = document.createElement('h4');
  cardTitle.classList.add('card-title');
  cardTitle.innerText = newTask.name;
  cardBody.appendChild(cardTitle);

  const descriptionText = document.createElement('p');
  descriptionText.classList.add('card-text');
  descriptionText.innerText = newTask.description;
  cardBody.appendChild(descriptionText);

  const duration = document.createElement('p');
  duration.classList.add('card-text');
  duration.innerText = newTask.duration;
  cardBody.appendChild(duration);

  const priority = document.createElement('p');
  duration.classList.add('card-text');
  duration.innerText = newTask.taskPriority;
  cardBody.appendChild(priority);

  const deleteButton = document.createElement('button');
  deleteButton.classList.add('btn');
  deleteButton.classList.add('btn-danger');
  deleteButton.innerText = 'Delete this event';
  cardBody.appendChild(deleteButton);

  const eventList = document.getElementById('new-task-list');
  eventList.innterHTML = '';
  eventList.appendChild(newEventCard);

  // The delete button removes the event's card from the UI. 
  deleteButton.onclick = function(newEventCard) {
    newEventCard.target.closest('div.card').remove(); 
  }
}

function getDuration(duration, unit) {
  if (unit == 'minutes') {
    return String(duration);
  } else if (unit == 'hours') {
    return String(duration * 60);
  }
}

function collectAllTasks() {
  const allEventJson = new Array();

  // Get task list element to run through cards collecting data.
  const taskList = document.getElementById('new-task-list');

  taskList.childNodes.forEach((taskCard) => {
    const taskName = taskCard.childNode[0].childNode[0].innerText;
    const taskDescription = taskCard.childNode[0].childNode[1].innerText;
  })


}
