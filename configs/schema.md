#goair
begin{EntityTypes}

<>Boolean [extend]
--Yes,yeah|No,nope,nah

<>TripType [extend]
--Single,One way|Round,Round Trip

end{EntityTypes}

begin{Intents}
<>book_flight
->[source] [std.geo.city] [notRequired, notList] [] []
->[destination] [std.geo.city] [required, notList] [] [Where are you flying out of?]
->[travel_date] [std.date] [required, notList] [] [When do you want to travel?]
--Book a flight from [chennai]<source> to [Bangalore]<destination> [tomorrow]<travel_date> 
--Book a flight to [Bangalore]<destination> [tomorrow]<travel_date> 
--Show me flights to [Bangalore]<destination> [tomorrow]<travel_date> 

<>flight_details
->[trip_type] [std.integer] [required, notList] [] [How many adults are travelling?]
->[adult_count] [std.integer] [required, notList] [] [How many adults are travelling?]
->[child_count] [std.integer] [required, notList] [] [How many kids, between 3 to 12 yrs, are travelling with you?]
->[infant_count] [std.integer] [required, notList] [] [How many infants are travelling with you?]
->[child_or_infant] [Boolean] [required, notList] [] [Any children travelling with you?]
--Trigger this intent
end{Intents}
