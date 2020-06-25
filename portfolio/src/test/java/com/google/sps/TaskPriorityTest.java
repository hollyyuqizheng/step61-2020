package com.google.sps;

import com.google.sps.data.TaskPriority;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class TaskPriorityTest {
  private static final int MAX_ACCEPTABLE_VALUE = 5;
  private static final int MIN_ACCEPTABLE_VALUE = 1;

  /**
   * Expect the TaskPriority constructor to throw an IllegalArgumentException if it is passed a
   * negative number, zero, or a number that is too large( > 5).
   */
  @Test(expected = IllegalArgumentException.class)
  public void negativeValue() {
    TaskPriority taskPriority = new TaskPriority(-5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void zeroValue() {
    TaskPriority taskPriority = new TaskPriority(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void tooLargeValue() {
    TaskPriority taskPriority = new TaskPriority(100);
  }

  /**
   * Expect the normal behavior to be that TaskPriority holds and returns the same priority level
   * (int) through TaskPriority.getPriority() as the value recieved through the constructor's
   * parameter. The case is tested for the minimum value, maximum value, and center value.
   */
  @Test
  public void normalBehavior() {
    int priority = 3;
    TaskPriority taskPriority = new TaskPriority(priority);

    int expected = priority;
    int actual = taskPriority.getPriority();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void maxAcceptableValue() {
    int priority = MAX_ACCEPTABLE_VALUE;
    TaskPriority taskPriority = new TaskPriority(priority);

    int expected = MAX_ACCEPTABLE_VALUE;
    int actual = taskPriority.getPriority();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void minAcceptableValue() {
    int priority = MIN_ACCEPTABLE_VALUE;
    TaskPriority taskPriority = new TaskPriority(priority);

    int expected = MIN_ACCEPTABLE_VALUE;
    int actual = taskPriority.getPriority();

    Assert.assertEquals(expected, actual);
  }
}
