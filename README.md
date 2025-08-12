GIVEN:

json-file with flight ticket information.



NEEDS TO BE IMPLEMENTED:

for all tickets between "Владивосток" and "Тель-Авив" it is necessary to find:

1) shortest time of flight by carriers;
2) mean value and median value(s) difference.



SOLUTION:

TaskProcessor/src/main/java/TicketsProcessor.java: program entry point. Parse json-data and pass it for processing to:

TaskProcessor/src/main/java/TicketsProcessorImplementation.java;

TaskProcessor/src/main/java/TicketsContainer.java: definitions of types used to parse json-data;

TaskProcessor/src/main/java/SummerWinterTimeHandler.java: takes care about time zone shifts in accordance with summer/winter times.
