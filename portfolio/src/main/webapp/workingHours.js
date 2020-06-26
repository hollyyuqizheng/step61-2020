/** 
 * Retrieves the working hours from the UI.
 * The getTimeObject funtion is defined in calendar.js.
 */
function getWorkingHours() {
  const $workHourStart = getTimeObject($("#working-hour-start").val()); 
  const $workHourEnd = getTimeObject($("#working-hour-end").val()); 
}
