/**
 * Models the requests sent to the algorithm for scheduling. This class is
 * useful when the request information needs to be sent to the scheduling
 * servlet through a POST request. This class contains all the request
 * information that can be easily converted into a JSON string.
 */
class ScheduleRequest {
  /**
   * All fields are required for this object.
   * @param events: events happening the day of the scheduling. This is an
   *     array of JavaScript CalendarEvent objects.
   * @param tasks: the tasks a user would like to schedule on that day. This is
   *     an array of JavaScript Task objects.
   * @param startTime: a JavaScript Date object representing the start time.
   * @param endTime: a JavaScript Date object representing the end time.
   * @param algorithmType: a string representation of the algorithm type that
   *     the user can select fromt the UI.
   */
  constructor(events, tasks, startTime, endTime, algorithmType) {
    this.events = events;
    this.tasks = tasks;
    this.startTime = startTime;
    this.endTime = endTime;
    this.algorithmType = algorithmType;
  }
}

// Constants for DOM information of scheduledTasks
const SCHEDULED_TIME_INITIAL_TEXT = 'Scheduled for: ';
const DURATION_INITIAL_TEXT = 'Duration (minutes): ';
const DESCRIPTION_INITIAL_TEXT = 'Description: ';
const PRIORITY_INITIAL_TEXT = 'Priority: ';

/**
 * Gets all of the scheduling information from the UI and returns a
 * ScheduleRequest with all of the data.
 */
function createScheduleRequestFromDom() {
  const startTime = document.getElementById('working-hour-start').value;
  const endTime = document.getElementById('working-hour-end').value;
  const events = collectAllEvents();
  const tasks = collectAllTasks();
  const algorithmType = document.getElementById('algorithm-type').value;
  const scheduleRequest = new ScheduleRequest(
      events,
      tasks,
      getTimeObject(startTime),
      getTimeObject(endTime),
      algorithmType);
  return scheduleRequest;
}

/**
 * Gets called when the user hits 'Start Scheduling'.
 */
function onClickStartScheduling() {
  // Create the request to send to the server using the data we collected from
  // the web form.
  fetchScheduledTasksFromServlet().then(handleScheduledTaskArray);
}

/**
 * Updates the UI to show the results of a query.
 */
function handleScheduledTaskArray(scheduledTaskArray) {
  const resultElement = document.getElementById('schedule-result-list');
  resultElement.innerHTML = '';
  scheduledTaskArray.forEach(addScheduledTaskToDom);
}

/**
 * Returns a JSON of the latest scheduled tasks pulled from the DOM.
 */
function collectAllScheduledTasks() {
  const allScheduledTasks = new Array();
  // Get scheduled task list element to run through cards collecting data.
  const scheduledTaskList = document.getElementById('schedule-result-list');

  scheduledTaskList.childNodes.forEach((scheduledTaskCard) => {
    const cardBody = scheduledTaskCard.childNodes[0];

    const scheduledTaskName =
        cardBody.childNodes[0].getAttribute('data-task-name');
    const scheduledTaskScheduledTime =
        cardBody.childNodes[1].getAttribute('data-task-time');
    const scheduledTaskDurationMinutes =
        parseInt(cardBody.childNodes[2].getAttribute('data-task-duration-minutes'));
    const scheduledTaskDescription =
        cardBody.childNodes[3].getAttribute('data-task-description');
    const scheduledTaskPriority =
        parseInt(cardBody.childNodes[4].getAttribute('data-task-priority'));

    var scheduledTask = {};
    const task = new Task(
        scheduledTaskName,
        scheduledTaskDescription,
        scheduledTaskDurationMinutes,
        scheduledTaskPriority);

    scheduledTask.task = task;
    scheduledTask.date = scheduledTaskScheduledTime;
    allScheduledTasks.push(scheduledTask);
  });
  return allScheduledTasks;
}

/**
 * Handles sending the ScheduleRequest object to the servlet and returns the
 * scheduled tasks.
 */
function fetchScheduledTasksFromServlet() {
  const scheduleRequest = createScheduleRequestFromDom();
  const json = JSON.stringify(scheduleRequest);
  return fetch('/schedule', {method: 'POST', body: json}).then((response) => {
    return response.json();
  });
}

/**
 * This takes in a single JSON object of a ScheduledTask (Java Class) and
 * displays that information on the result element using a card format.
 */
function addScheduledTaskToDom(scheduledTask) {
  const task = scheduledTask.task;
  const taskName = task.name;
  // Changes seconds into minutes.
  const taskDurationMinutes = task.duration.seconds / 60;
  const taskDescription = task.description.value;
  const taskPriority = task.priority.priority;
  const taskTimeSeconds = scheduledTask.startTime.seconds;
  const taskDate = new Date();
  // This is x1000 because the function takes milliseconds
  taskDate.setTime(taskTimeSeconds * 1000);

  const newResultCard = document.createElement('div');
  newResultCard.classList.add('card');

  const cardBody = document.createElement('div');
  cardBody.classList.add('card-body');
  newResultCard.appendChild(cardBody);

  const cardTitle = document.createElement('h4');
  cardTitle.classList.add('card-title');
  cardTitle.innerText = taskName;
  cardTitle.setAttribute('data-task-name', taskName);
  cardBody.appendChild(cardTitle);

  const timeText = document.createElement('p');
  timeText.classList.add('card-text');
  timeText.innerText = SCHEDULED_TIME_INITIAL_TEXT + taskDate;
  timeText.setAttribute('data-task-time', taskDate);
  cardBody.appendChild(timeText);


  const durationText = document.createElement('p');
  durationText.classList.add('card-text');
  durationText.innerText = DURATION_INITIAL_TEXT + taskDurationMinutes;
  durationText.setAttribute('data-task-duration-minutes', taskDurationMinutes);
  cardBody.appendChild(durationText);

  const descriptionText = document.createElement('p');
  descriptionText.classList.add('card-text');
  descriptionText.innerText = DESCRIPTION_INITIAL_TEXT + taskDescription;
  descriptionText.setAttribute('data-task-description', taskDescription);
  cardBody.appendChild(descriptionText);

  const priorityText = document.createElement('p');
  priorityText.classList.add('card-text');
  priorityText.innerText = PRIORITY_INITIAL_TEXT + taskPriority;
  priorityText.setAttribute('data-task-priority', taskPriority);
  cardBody.appendChild(priorityText);

  const scheduledTaskList = document.getElementById('schedule-result-list');
  scheduledTaskList.appendChild(newResultCard);
}