# Notes

These are just temporary notes to document various decisions to make Project documentation easier later

## Warning Levels

- double waterLevel negative, NaN or infinite return UNKNOWN
- latest measurement older than 30s return UNKNOWN (30s is currently hardcoded but should be changed later)
- an inactive station always returns UNKNOWN, even if its last measurement is younger than the 30s limit
- current level is recomputed with the stations current thresholds (matters for patches to stations) 

## Hydrodynamics

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
- **Q_base, base flow (m³/s):** the discharge in dry weather. Set as A × 0.02, which is about what the
  Danube at Vienna carries per km².
- **Q, discharge (m³/s):** the water passing the station per second.
  Q = Q_base + (S / k) × A × 1000 / 3600. The factor 1000 / 3600 converts mm per hour on km² into m³/s.
- **h, water level (m):** the result.
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
- [Danube, Wikipedia](https://en.wikipedia.org/wiki/Danube): mean discharge at Vienna (1,920 m³/s), used for the
  base flow of 0.02 m³/s per km².
- [Pegel Graz/Mur, BML](https://www.bmluk.gv.at/themen/wasser/wasser-oesterreich/hydrographie/wasser-im-blick-das-messnetz/PegelGrazMur.html):
  example for the catchment area of a station. The areas of the other rivers are rounded values.