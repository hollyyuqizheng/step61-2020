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


// TODO(tomasalvarez): Write tests for this class.
/**
 * Class representing a span of time, enforcing properties (e.g. start comes before end) and
 * providing methods to make ranges easier to work with (e.g. {@code overlaps}).
 */
public final class TimeRange {
  private final Instant start;
  private final Duration duration;

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
    return Instant.ofEpochSecond(start.getEpochSecond() + duration.getSeconds());
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof TimeRange && equals(this, (TimeRange) other);
  }

  @Override
  public String toString() {
    return String.format("Range: [%d, %d)", start, this.end());
  }

  public static boolean equals(TimeRange a, TimeRange b) {
    return a.start.equals(b.start) && a.duration.equals(b.duration);
  }

  /**
   * Creates a {@code TimeRange} from {@code start} to {@code end}. Whether or not {@code end} is
   * included in the range will depend on {@code inclusive}. If {@code inclusive} is {@code true},
   * then {@code end} will be in the range.
   */
  public static TimeRange fromStartEnd(Instant start, Instant end, boolean inclusive) {
    return inclusive
        ? new TimeRange(
            start, Duration.ofSeconds(end.getEpochSecond() - start.getEpochSecond() + 1))
        : new TimeRange(start, Duration.ofSeconds(end.getEpochSecond() - start.getEpochSecond()));
  }
}
