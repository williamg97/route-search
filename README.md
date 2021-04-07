# route-search
Find amenities and other useful waypoints along a route using Google Maps API and generates seperate GPX files for each type. 

I used this to find toilets, bars, restaurants, tourist attractions, supermarkets etc on a multi day bike route that I was planning to do. 

## Important

This is still work in progress. I intend to make it more usable via command line.

You need your own Google Maps API to make this work. 

Also make sure you know what you are doing - nearby Google Maps API calls are expensive. See: https://cloud.google.com/maps-platform/pricing/sheet

At the time of writing you get 5000 free API calls a month. After that it costs. $40 per 1000 calls. 

There If you have a large number of waypoints you are searching on this could get expensive. MAKE SURE YOU ADD QUOTAS first so you dont accidently go over the limit. 

You can also adjust sampling rate, radius search etc in MapSearchParser. 
