# Notes

These are just temporary notes to document various decisions to make Project documentation easier later

## Warning Levels

- double waterLevel negative, NaN or infinite return UNKNOWN
- latest measurement older than 30s return UNKNOWN (30s is currently hardcoded but should be changed later)
- an inactive station always returns UNKNOWN, even if its last measurement is younger than the 30s limit
- current level is recomputed with the stations current thresholds (matters for patches to stations) 

## Hydrodynamics

### Rain Model

The model used is a very simplified system which aims to allow for realistic rain per station (with the right configuration),
instead of trying to create realistic weather.

Rain is simulated as a single circular cell representing a cloud. It can move around the map at a constant speed.
The model itself is a two-state Markov chain. The states are cell exists (wet) and cel doesn't exist (dry).
For a station to get rain two things must therefor be true: a cell must exist (state must be wet) and the station is 
inside the cell's radius. Instead of rolling a small probability every tick to see if a state change occurs, the duration 
of each state is drawn once and counted down. The distribution a Markov chain would produce (and is therefor used here)
is an exponential distribution: −average × ln(1 − random). Notable this only works at small tick lengths and the ticks 
should therefor mainly be set to one minute or less.  