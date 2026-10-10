# Notes

These are just temporary notes to document various decisions to make Project documentation easier later

## Warning Levels

- double waterLevel negative, NaN or infinite return UNKNOWN
- latest measurement older than 3 × simulation.interval return UNKNOWN
- an inactive station always returns UNKNOWN
- current level is recomputed with the stations current thresholds (matters for patches to stations) 

## Hydrodynamics

The following sections describe what Math/Models are used in the simulation to generate a Measurement. 

### Rain Model

The model used is a very simplified system which aims to allow for realistic/interesting rain per 
station (with the right configuration), instead of trying to create realistic weather.

Rain is simulated as a single circular cell representing a cloud. It can move around the map at a constant speed.
The model itself is a two-state Markov chain. The states are cell exists (wet) and cel doesn't exist (dry).
For a station to get rain two things must therefor be true: a cell must exist (state must be wet) and the station is 
inside the cell's radius. Instead of rolling a small probability every tick to see if a state change occurs, the duration 
of each state is drawn once and counted down. The distribution a Markov chain would produce (and is therefor used here)
is an exponential distribution: −average × ln(1 − random). Notable this only works at small tick lengths and the ticks 
should therefor mainly be set to one minute or less. 

### Water Level Model

Like the rain model, this is a very simplified system. It aims for water levels that react to rain
in a believable way, instead of trying to be accurate hydrology.

The model separates two things: the land around a river (the catchment) and the river itself.
The catchment works like a reservoir. Rain is stored in it and released into the river slowly.
The river itself doesn't store anything and only carries away what arrives. There is one catchment per river, 
shared by all stations on that river, which is why stations on the same river show the same flood at the same time.

The catchment is a "linear reservoir": the amount it releases per hour is proportional to the amount
it holds (S / k). A full catchment therefore releases a lot and the river rises quickly. While it
empties it releases less and less, so the river falls fast at first and then more and more slowly.
Without rain the flood fades exponentially.

The discharge of the river is what the catchment is currently releasing in addition to its base flow. 
The base flow stands for groundwater and never goes away, so a river never falls below its normal level.

The water level is calculated from the discharge with a rating curve, Q = a × (h − h₀)^b, solved for h:
h = h₀ + (Q / a)^(1/b). Discharge comes first, since the water level depends on how much water is flowing.
The same discharge stands at a different height depending on the riverbed.

The catchment is drained once per tick, which is only accurate while the tick is much shorter than k.

#### Variables

- **S, storage (mm):** the water that fell on the catchment but hasn't reached the river yet.
  It's measured in mm, same as rain. For scale, 1 mm on 1 km² is 1,000 m³.
- **k, recession time (hours):** controls how slowly the catchment drains. Since the storage gets divided
  by it (S / k), a larger k means a smaller part is released per hour, so floods are lower but last longer.
  After k hours without rain about 37% of the storage is still there. Set as 0.75 × √A. Not a formula from hydrology!
- **A, catchment area (km²):** the land that drains to the station. The release gets multiplied by it, since
  the same mm of water on more land is simply more m³. This is the only value set per river, k and the
  base flow are both calculated from it.
- **Runoff share (0.3):** only 30% of the rain goes into the storage. The rest either evaporates or goes
  into the groundwater, which is already covered by the base flow. Without this the rivers carry
  far too much water.
- **Q_base, base flow (m³/s):** the discharge in dry weather. It stands for the rain that goes into the
  groundwater and reaches the river slowly. Set as A × 0.01.
- **Q, discharge (m³/s):** the water passing the station per second.
  Q = Q_base + (S / k) × A × 1000 / 3600. The factor 1000 / 3600 converts mm per hour on km² into m³/s.
- **h, water level (cm):** the result calculated in m, stored in cm
- **h₀:** the level at which the river would stop flowing. Only the height above it (h − h₀) carries
  water, so it shifts every level up or down.
- **a:** the discharge when the water stands 1 m above h₀. It describes how big the riverbed is: a larger
  a means a lower level for the same discharge. Set as Q_base / 0.67, so that every river stands
  0.82 m above h₀ in dry weather regardless of its size.
- **b:** how strongly the discharge grows with the height. Deeper water also flows faster, so with b = 2
  twice the height carries four times the water. When solving for h it turns into a square root, so even
  a big increase in discharge doesn't raise the level that much.

#### Limitations

- A river never drops below its normal level, so there are floods but no low water.
- The rain on a catchment is only the average rain at the river's stations.
- All stations on a river react at the same moment. There is no travel time downstream. 
  Measurements still differ in other factors on the same river. 

#### Sources

- [Runoff model (reservoir), Wikipedia](https://en.wikipedia.org/wiki/Runoff_model_(reservoir)): the linear reservoir.
  It uses a reaction factor A instead of k, which is the same thing as 1 / k.
- [Rating curve, Wikipedia](https://en.wikipedia.org/wiki/Rating_curve): the formula for the rating curve
  and typical values for b.
- [Pegel Graz/Mur, BML](https://www.bmluk.gv.at/themen/wasser/wasser-oesterreich/hydrographie/wasser-im-blick-das-messnetz/PegelGrazMur.html):
  example for the catchment area of a station. The areas of the other rivers are rounded values.

### Temperature

The temperature just wanders around an average and gets pulled back to it. There is no day and night
and there are no seasons. The model for this is an Ornstein–Uhlenbeck process:

x_next = x + θ × (μ − x) × Δt + σ × √Δt × noise

The first part pulls the value back towards the average, the second part pushes it in a random direction.

- **μ = 15 °C:** the average
- **θ = 0.02 (per hour):** how strongly the value is pulled back. It's multiplied with the distance to
  the average (μ − x), so the further away the temperature is, the stronger the pull. 1 / θ = 50 hours
  is about how long it takes to wander off and come back.
- **σ = 0.5:** how big the random pushes are.
- **noise:** a normally distributed random number.
- **Δt:** the tick length in hours. The noise uses √Δt instead of Δt, which keeps the randomness the
  same no matter how long the tick is.

How far it wanders is σ / √(2 × θ) = 2.5 °C on average, so the temperature stays within ±5 °C of the
average about 95% of the time.

Source: [Ornstein–Uhlenbeck process, Wikipedia](https://en.wikipedia.org/wiki/Ornstein%E2%80%93Uhlenbeck_process)


### Battery and Status

The battery drains slowly while a station is online and charges quickly while it's in maintenance.
The values bellow are the ones used in application.properties

- **Online:** the battery drops from 100% to 0% in 6 days. At 5% the station switches to maintenance.
- **Maintenance:** the battery charges from 0% back to 100% in 4 hours. At 100% the station is online again.
- **Jitter:** every tick the amount is multiplied with a random factor between 0.9 and 1.1. This makes
  the curve a bit uneven but doesn't really change how long a cycle takes, since it evens out over
  thousands of ticks.

Stations in maintenance still produce measurements, otherwise the charging wouldn't be visible.
Inactive and Offline stations don't produce any.

### Warning Levels

Warning and critical are defined by the water level and are set per river, not per station

These are not realistic flood levels! They were picked so that something happens often: for all five
stations together there are about 2.4 warning periods per week and a critical one about every two weeks.
A real warning level is reached maybe once a year. The values were found by running the simulation
for 100 years and looking for the levels that are crossed that often.

Actual floods still happen in the simulation, just above these levels. One may just change the levels.