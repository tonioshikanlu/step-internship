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

package com.google.sps;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Arrays;

/** 
*  Function determining the avaliable times for a meeting request to take place based
*  on the event timings of all the attendees. Accepts all current events passed in
*  and the request and then returns an arrayList of suitable timeranges.
*/
public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> mandatoryAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    Collection<TimeRange> mandatoryAvaliableTimes = avaliableTimesQuery(events, request, mandatoryAttendees);
	Collection<TimeRange> optionalAvaliableTimes = avaliableTimesQuery(events,  request, optionalAttendees);
  	// Return times of only optional attendees if no mandatory attendees are indicated.
  	if (mandatoryAttendees.isEmpty()){
  		return optionalAvaliableTimes;
  	}
  	// Return times of only mandatory attendees if no optional attendees are indicated.
  	if (optionalAvaliableTimes.isEmpty()){
  		return mandatoryAvaliableTimes;
  	}
  	List<TimeRange> allAvaliableTimes = new ArrayList<TimeRange>();
  	// Add times that also work for the optional attedees and remove times that don't also 
  	// work for the optional attendees from the avaliable times collection.
  	for (TimeRange optionalRange : optionalAvaliableTimes){
  		for (TimeRange mandatoryRange : mandatoryAvaliableTimes){
  			if (optionalRange.duration() >= request.getDuration()){
  				if (mandatoryRange.contains(optionalRange) || mandatoryRange.overlaps(optionalRange)){
  					allAvaliableTimes.add(mandatoryRange);
  				}
  			}
  		}
  	}
  	// Return avaliable times for both mandatory and optional attendees.
  	return allAvaliableTimes;
  }
  /** Helper function that determines the times the attendees passed into it are avaliable.
  */
  public Collection<TimeRange> avaliableTimesQuery(Collection<Event> events, MeetingRequest request, Collection<String> attendeesRequested) {
  	HashMap<String, List<TimeRange>> attendeesTimes = new HashMap<String, List<TimeRange>>();    
    List<String> attendees = new ArrayList<String>(attendeesRequested);
    // We need to map the attendees in the request to the times this individual is not free.
	for (String attendee : attendees){
		for (Event e : events) {
			boolean contains = e.getAttendees().contains(attendee);
			if (contains) {
				// Java syntax to create map with the time ranges if attendee has an event.
				attendeesTimes.computeIfAbsent(attendee, k -> new ArrayList<>()).add(e.getWhen());			
			}
	    }
	}
	// Builds an arrayList of all times that are not avaliable for meet.
    List<TimeRange> busyTimes = new ArrayList<TimeRange>();
    for (HashMap.Entry<String, List<TimeRange>> entry : attendeesTimes.entrySet()) {
    	for (TimeRange r : entry.getValue()){
    		busyTimes.add(r);
    	}	
    }
    Collections.sort(busyTimes, new Comparator<TimeRange>() {
    	@Override
    	public int compare(TimeRange a, TimeRange b) {
      	return Long.compare(a.end(), b.end());
	}});	
	Integer startDay = TimeRange.WHOLE_DAY.start();
	Integer endDay = TimeRange.WHOLE_DAY.end();
	List<TimeRange> stack = new ArrayList<TimeRange>();
	// Using the stack logic I merge the sorted list of unavaliable times by ranges that 
	// overlap or contain one another. 
	for (TimeRange range : busyTimes){
		if (stack.isEmpty()){
			stack.add(range);
		}
			else {
				TimeRange lastRange = stack.get(stack.size() - 1);
				
				if (lastRange.overlaps(range) || lastRange.contains(range)){
					Integer newStart = Math.min(lastRange.start(), range.start());
					Integer newEnd = Math.max(lastRange.end(), range.end());
					stack.remove(stack.size() - 1);
					stack.add(TimeRange.fromStartEnd(newStart, newEnd, false));
					}
					else {
						stack.add(range);
					}
				}
			}
	List<TimeRange> avaliableTimes = new ArrayList<TimeRange>();
	Integer currentStart;
	Integer currentEnd;
	Integer nextStart;
	// Lastly, I need to build the arrayList that contains all the avaliablle times. 
	// To do this, there are three cases that have to be taken care of for us to determine the times:
	for (int i=0; i < stack.size(); i++){
			currentStart = stack.get(i).start();
			currentEnd = stack.get(i).end(); 
			// 1. If the time between the intervals in the stack are at least equal to the request duration.
			if (i+1 < stack.size()){
				nextStart = stack.get(i+1).start();
				if(nextStart - currentEnd >= request.getDuration()){
					avaliableTimes.add(TimeRange.fromStartEnd(currentEnd, nextStart, false));
				}	
			}
			// 2. We determine whether the time from the start of the day to the first interval is at least equal to the request duration.
			if (i == 0){
				if (currentStart - startDay >= request.getDuration()){
					avaliableTimes.add(TimeRange.fromStartEnd(startDay, currentStart, false));
				} 
			}
			// 3. We determine whether the time from the last interval in the stack to the end of the day is at least equal to the request duration.
			if (i == stack.size() - 1){
				if (endDay - currentEnd >= request.getDuration()){
					avaliableTimes.add(TimeRange.fromStartEnd(currentEnd, endDay, false));
				}
			}			
	}
	/** Some edge cases
	*/
	// If there are no attendes the whole day is avaliable.
	if (attendeesTimes.isEmpty()){
		avaliableTimes.add(TimeRange.fromStartEnd(startDay, endDay, false));
	}
	// If the duration request is longer than 24 hours we return an empty arrayList.
	if (request.getDuration() > TimeRange.WHOLE_DAY.duration()){
		avaliableTimes = new ArrayList<TimeRange>();
	}
	// Sort avaliable times so the ranges appear in the correct ordr.
	Collections.sort(avaliableTimes, new Comparator<TimeRange>() {
    	@Override
    	public int compare(TimeRange a, TimeRange b) {
      	return Long.compare(a.end(), b.end());
	}});
    return avaliableTimes;
  }
}
