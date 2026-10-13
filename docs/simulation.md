# Simulation

The simulation runs once per `simulation.interval` (one tick). All models are simplified on purpose.
They aim for values that behave in a believable way, not for accurate weather or hydrology.

## One Tick

1. The rain field moves the current rain cell, or creates or removes one.
2. For every river, the average rain of its stations goes into the catchment of that river.
   The catchment drains a little, and the discharge gives the water level.
3. Every active station gets a new measurement with the water level and flow rate of its river and its own rain.
   Temperature, battery and status continue from the previous measurement of the station.
   The warning level is calculated from the water level.

## Rain

Rain comes from a single circular rain cell that moves across the map at a constant speed.
A cell appears at a random point on one of the four map borders and moves into the map,
turned by up to 22.5° to either side. It disappears when its lifetime ends or its center leaves the map.
A station gets rain while it is inside the radius of the cell.

The model is a two-state Markov chain with the states wet (a cell exists) and dry (no cell).
Instead of a small chance of a state change every tick, the length of each state is drawn once and counted down.
The dry time, the lifetime and the intensity of a cell are all drawn from an exponential distribution
around their configured average: `−average × ln(1 − random)`. This only matches a Markov chain with short ticks,
so the interval should be one minute or less.

The `rainfall` of a measurement is the rain intensity at the station during the last tick, in mm/h.

## Water Level and Flow Rate

Every river has one catchment. It stands for the land around the river and works like a reservoir:
rain is stored in it and released into the river slowly. The river itself doesn't store anything.
Because all stations of a river share one catchment, they always show the same water level and flow rate.

Per tick:

```text
S = S + rain × 0.3                    only 30 % of the rain reaches the river
S = S − S × Δt / k                    the catchment drains
Q = Q_base + S / k × A × 1000 / 3600  discharge in m³/s
h = h₀ + (Q / a)^(1/b)                water level from the rating curve
```

- **S, storage (mm):** rain that fell on the catchment but hasn't reached the river yet
- **Δt (h):** the tick length
- **k, recession time (h):** how slowly the catchment drains. Set as `0.75 × √A`, which is not a formula from hydrology.
  After k hours without rain about 37 % of the storage is left.
- **A, catchment area (km²):** Donau 100,000, Salzach 4,400, Mur 7,000, Inn 5,800, any other river 1,000
- **Q_base, base flow (m³/s):** the discharge in dry weather, from groundwater. Set as `A × 0.01`.
- **h₀ = 0.5 m:** the level at which the river would stop flowing
- **a:** describes the size of the riverbed. Set as `Q_base / 0.67`, so every river stands at about 132 cm in dry weather.
- **b = 2:** twice the height carries four times the water

The flow rate is Q. The water level h is calculated in m and stored in cm. Because h is calculated from Q, a higher
water level always comes with a higher flow rate. Without rain the storage drains exponentially, so the river
falls fast at first and then more and more slowly.

Limitations:

- A river never falls below its dry-weather level, so there are floods but no low water.
- The rain on a catchment is only the average rain at the stations of that river.
- All stations on a river react at the same time. There is no travel time downstream.

## Temperature

Each station has its own temperature. It wanders around an average and is pulled back towards it
(Ornstein–Uhlenbeck process). There is no day and night and there are no seasons.

```text
T = T + θ × (μ − T) × Δt + σ × √Δt × noise
```

- **μ = 15 °C:** the average, and the start value of every station
- **θ = 0.02 per hour:** how strongly the value is pulled back
- **σ = 0.5:** how big the random steps are
- **noise:** a normally distributed random number

The temperature stays within ±5 °C of the average about 95 % of the time.

## Battery and Status

- A station starts with a random battery level between 5 % and 100 % and the status `ONLINE`.
- While `ONLINE`, a full battery drains in `simulation.hoursToDrain` hours. At 5 % the status changes to `MAINTENANCE`.
- While in `MAINTENANCE`, the battery charges completely in `simulation.hoursToCharge` hours. At 100 % the status
  changes back to `ONLINE`. Stations in maintenance still create measurements, so the charging is visible.
- Every tick the change is multiplied by a random factor between 0.9 and 1.1.
- The battery level always stays between 0 % and 100 %.

The simulation never sets `OFFLINE` itself. Stations that are `OFFLINE` or inactive get no new measurements.

## Warning Thresholds

All stations have a `normalWaterLevel` of 150 cm. The warning and critical levels are set per river:

- **Donau:** 175 cm and 200 cm
- **Salzach:** 195 cm and 270 cm
- **Mur:** 185 cm and 255 cm
- **Inn:** 190 cm and 265 cm

These are not realistic flood levels. They were found by running the simulation for 100 years and picking levels
that give about 2.4 warning periods per week and one critical period about every two weeks for all stations
together. A real warning level is reached maybe once a year.

## Sources

- [Runoff model (reservoir), Wikipedia](https://en.wikipedia.org/wiki/Runoff_model_(reservoir)): the linear reservoir.
  It uses a reaction factor instead of k, which is the same as 1 / k.
- [Rating curve, Wikipedia](https://en.wikipedia.org/wiki/Rating_curve): the rating curve formula and typical values for b.
- [Pegel Graz/Mur, BML](https://www.bmluk.gv.at/themen/wasser/wasser-oesterreich/hydrographie/wasser-im-blick-das-messnetz/PegelGrazMur.html):
  example for the catchment area of a station. The areas of the other rivers are rounded values.
- [Ornstein–Uhlenbeck process, Wikipedia](https://en.wikipedia.org/wiki/Ornstein%E2%80%93Uhlenbeck_process)
