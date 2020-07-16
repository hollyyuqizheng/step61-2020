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

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;

/**
 * Class representing a span of time, enforcing properties (e.g. start comes before end) and
 * providing methods to make ranges easier to work with (e.g. {@code overlaps}).
 */
public final class TimeRange {
  private final Instant start;
  private final Duration duration;

  // Comparator for sorting time ranges by start time
  public static final Comparator<TimeRange> sortByTimeRangeStartTimeAscending =
      Comparator.comparing(TimeRange::start);

  private TimeRange(Instant start, Duration duration) {
    this.start = start;
    this.duration = duration;
  }

  /** Returns the start of the range in minutes. */
  public Instant start() {
    return start;
  }

  /** Returns the number of minutes between the start and end. */
  public Duration duration() {
    return duration;
  }

  /** Returns the end of the range. This ending value is the closing exclusive bound. */
  public Instant end() {
    return start.plus(duration);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof TimeRange && equals(this, (TimeRange) other);
  }

  @Override
  public String toString() {
    return String.format("Range: [%s, %s]", start.toString(), this.end().toString());
  }

  public static boolean equals(TimeRange a, TimeRange b) {
    return a.start.equals(b.start) && a.duration.equals(b.duration);
  }

  /** Creates a {@code TimeRange} from {@code start} to {@code end}. */
  public static TimeRange fromStartEnd(Instant start, Instant end) {
    return new TimeRange(
        start, Duration.ofSeconds(end.getEpochSecond() - start.getEpochSecond()));
  }
}
