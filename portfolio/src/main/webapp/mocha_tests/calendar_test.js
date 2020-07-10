var assert = require('assert');
var calendar = require('../calendar.js'); 

/**
 * In general, these tests are for date/time string parsing and validity.
 * Because these values come from elements on the HTML file that are
 * elements of type time or date, the HTML element already checks for 
 * some invalid input. 
 * Specifically, the numbers inputted for month, date, hour, and minute
 * that come from these HTML elements are valid numbers for date and time.
 * For example, 20 is not a valid month, and 70 is not a valid minute.  
 * Invalid inputs in this aspect will not be tested in the following tests. 
 */

describe('#parseTodayString', function () {
  describe('extra zero padding', function () {
    it('should return \'2020-01-02\'', function () {
      const year = '2020';
      const month = '1';
      const date = '2'; 
      assert.equal(calendar._test.parseTodayString(year, month, date), '2020-01-02');
    });
  });

  describe('two-digit dates', function () {
    it('should return \'2020-11-22\'', function () {
      const year = '2020';
      const month = '11';
      const date = '22'; 
      assert.equal(calendar._test.parseTodayString(year, month, date), '2020-11-22');
    });
  });
});

describe('#getClosestNextHour', function() {
  describe('next hour is 5pm', function() {
    it('should return 17:00', function() {
      const workHourStartString = '09:00';
      const workHourEndString = '17:00';
      const nextHour = '17';
      assert.equal(calendar._test.getClosestNextHour(
          nextHour, workHourStartString, workHourEndString), '17:00'); 
    });
  });

  describe('next hour is 4pm', function() {
    it('should return 16:00', function() {
      const workHourStartString = '09:00';
      const workHourEndString = '17:00';
      const nextHour = '16';
      assert.equal(calendar._test.getClosestNextHour(
          nextHour, workHourStartString, workHourEndString), '16:00'); 
    });
  });

  describe('next hour is after working hour ends', function() {
    it('should return 17:00', function() {
      const workHourStartString = '09:00';
      const workHourEndString = '17:00';
      const nextHour = '20';
      assert.equal(calendar._test.getClosestNextHour(
          nextHour, workHourStartString, workHourEndString), '17:00'); 
    });
  });
  
  describe('next hour is before working hour starts', function() {
    it('should return 09:00', function() {
      const workHourStartString = '09:00';
      const workHourEndString = '17:00';
      const nextHour = '5';
      assert.equal(calendar._test.getClosestNextHour(
          nextHour, workHourStartString, workHourEndString), '09:00'); 
    });
  });
  
  describe('extra zero padding', function() {
    it('should return 08:00', function() {
      const workHourStartString = '06:00';
      const workHourEndString = '17:00';
      const nextHour = '8';
      assert.equal(calendar._test.getClosestNextHour(
          nextHour, workHourStartString, workHourEndString), '08:00'); 
    });
  });

});

describe('#isWorkHourValid', function () {
  describe('valid working hour', function () {
    it('9:30 - 17:00 is a valid working hour', function () {
      const workHourStartHour = 9;
      const workHourStartMinute = 30;
      const workHourEndHour = 17;
      const workHourEndMinute = 0;
      assert.equal(calendar._test.isWorkHourValid(
          workHourStartHour,
          workHourEndHour,
          workHourStartMinute,
          workHourEndMinute), true);
    });
  });

  describe('end time minute is earlier', function () {
    it('9:30 - 9:00 is not a valid working hour', function () {
      const workHourStartHour = 9;
      const workHourStartMinute = 30;
      const workHourEndHour = 9;
      const workHourEndMinute = 0;
      assert.equal(calendar._test.isWorkHourValid(
          workHourStartHour,
          workHourEndHour,
          workHourStartMinute,
          workHourEndMinute), false);
    });
  });

  describe('end time hour is earlier', function () {
    it('9:30 - 6:00 is not a valid working hour', function () {
      const workHourStartHour = 9;
      const workHourStartMinute = 30;
      const workHourEndHour = 6;
      const workHourEndMinute = 0;
      assert.equal(calendar._test.isWorkHourValid(
          workHourStartHour,
          workHourEndHour,
          workHourStartMinute,
          workHourEndMinute), false);
    });
  });
  
});
