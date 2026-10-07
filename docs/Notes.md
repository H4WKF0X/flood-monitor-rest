# Notes

These are just temporary notes to document various decisions to make Project documentation easier later

## Warning Levels

- double waterLevel negative, NaN or infinite return UNKNOWN
- latest measurement older than 30s return UNKNOWN (30s is currently hardcoded but should be changed later)
- an inactive station always returns UNKNOWN, even if its last measurement is younger than the 30s limit
- current level is recomputed with the stations current thresholds (matters for patches to stations) 