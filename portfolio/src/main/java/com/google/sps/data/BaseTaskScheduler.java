// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import java.time.Instant;
import java.util.Collection;

// TODO: Add test coverage for scheduling algorithms
public abstract class BaseTaskScheduler {
  public enum SchedulingAlgorithmType {
    SHORTEST_TASK_FIRST
  }

  /** This is the abstract method that all algorithm classes should implement. */
  public abstract Collection<ScheduledTask> schedule(
      Collection<CalendarEvent> events,
      Collection<Task> tasks,
      Instant workHoursStartTime,
      Instant workHoursEndTime);
}
