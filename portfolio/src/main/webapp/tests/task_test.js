var assert = require('assert');
var task = require('../task');

describe('Task Function Tests', function() {
  describe('#validateTaskName()', function() {
    const success = {isValid: true, errorMessage: null};
    const fail = {isValid: false, errorMessage: 'Name cannot be empty.'};

    it('should fail when empty string is passed', function() {
      assert.deepEqual(task.validateTaskName(''), fail);
    });

    it('should succeed when a word is passed', function() {
      assert.deepEqual(task.validateTaskName('hello'), success);
    });

    it('should succeed when multiple words are passed', function() {
      assert.deepEqual(task.validateTaskName('hello world'), success);
    });

    it('should succeed when special characters are in words', function() {
      assert.deepEqual(task.validateTaskName('hello#$% wor1d.!?'), success);
    });
  });

  describe('#validateTaskDuration()', function() {
    const success = {isValid: true, errorMessage: null};
    const fail = {isValid: false, errorMessage: 'Duration input is invalid.'};

    it('should fail when nothing is passed', function() {
      assert.deepEqual(task.validateTaskDuration(), fail);
    });

    it('should fail when a negative number is passed', function() {
      assert.deepEqual(task.validateTaskDuration(-3), fail);
    });

    it('should fail when zero is passed', function() {
      assert.deepEqual(task.validateTaskDuration(0), fail);
    });

    it('should succeed when a positive number is passed', function() {
      assert.deepEqual(task.validateTaskDuration(491), success);
    });
  });

  describe('#getDurationMinutes()', function() {
    const hours = 'hours';
    const minutes = 'minutes';

    it('should return the same value if units are minutes (1)', function() {
      assert.equal(task.getDurationMinutes(10, minutes), 10);
    });

    it('should return the same value if units are minutes (2)', function() {
      assert.equal(task.getDurationMinutes(30, minutes), 30);
    });

    it('should return the same value if units are minutes (3)', function() {
      assert.equal(task.getDurationMinutes(60, minutes), 60);
    });

    it('should return the value * 60 if units are hours (1)', function() {
      assert.equal(task.getDurationMinutes(1, hours), 60);
    });

    it('should return the value * 60 if units are hours (2)', function() {
      assert.equal(task.getDurationMinutes(5, hours), 300);
    });

    it('should return the value * 60 if units are hours (3)', function() {
      assert.equal(task.getDurationMinutes(10, hours), 600);
    });
  })
});
