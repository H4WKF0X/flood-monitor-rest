#!/usr/bin/env python3
"""Command-line client for the FloodMonitorService.

Usage: python client.py [--address HOST:PORT] [--timeout SECONDS] COMMAND ...
Commands: stations, station, latest, measurements, alerts, subscribe.
"""
import argparse
import asyncio
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parent / "generated"))

import grpc
import grpc.aio
from google.protobuf.timestamp_pb2 import Timestamp

import flood_monitor_pb2 as pb2
import flood_monitor_pb2_grpc as pb2_grpc

DEFAULT_ADDRESS = "localhost:9090"
DEFAULT_TIMEOUT = 5.0
WARNING_LEVEL_CHOICES = ["normal", "warning", "critical", "unknown"]


def warning_level_value(name):
    """Converts a CLI name like `critical` to the enum value WARNING_LEVEL_CRITICAL."""
    return pb2.WarningLevel.Value(f"WARNING_LEVEL_{name.upper()}")


def parse_time(text):
    """Parses a timestamp like `2026-11-27T08:00:00Z`. Used as argparse `type`."""
    ts = Timestamp()
    try:
        ts.FromJsonString(text)
    except ValueError as e:
        raise argparse.ArgumentTypeError(
            f"invalid time '{text}', expected e.g. 2026-11-27T08:00:00Z ({e})"
        )
    return ts


def build_parser():
    """Builds the argument parser with one subparser per command."""
    parser = argparse.ArgumentParser(description="Flood monitor gRPC client")
    parser.add_argument("--address", default=DEFAULT_ADDRESS,
                        help=f"server host:port (default {DEFAULT_ADDRESS})")
    parser.add_argument("--timeout", type=float, default=DEFAULT_TIMEOUT,
                        help=f"deadline in seconds for unary calls (default {DEFAULT_TIMEOUT})")

    sub = parser.add_subparsers(dest="command", required=True)

    p = sub.add_parser("stations", help="list stations")
    p.add_argument("--river")
    p.add_argument("--active", action=argparse.BooleanOptionalAction,
                   help="--active or --no-active (omit for no filter)")
    p.add_argument("--warning-level", choices=WARNING_LEVEL_CHOICES)
    p.set_defaults(func=cmd_stations)

    p = sub.add_parser("station", help="show one station")
    p.add_argument("station_id")
    p.set_defaults(func=cmd_station)

    p = sub.add_parser("latest", help="show the latest measurement of a station")
    p.add_argument("station_id")
    p.set_defaults(func=cmd_latest)

    p = sub.add_parser("measurements", help="list measurements of a station")
    p.add_argument("station_id")
    p.add_argument("--start", type=parse_time, help="at or after, e.g. 2026-11-27T08:00:00Z")
    p.add_argument("--end", type=parse_time, help="at or before, e.g. 2026-11-27T18:00:00Z")
    p.add_argument("--limit", type=int)
    p.add_argument("--warning-level", choices=WARNING_LEVEL_CHOICES)
    p.set_defaults(func=cmd_measurements)

    p = sub.add_parser("alerts", help="list current alerts")
    p.add_argument("--river")
    p.add_argument("--warning-level", choices=WARNING_LEVEL_CHOICES)
    p.set_defaults(func=cmd_alerts)

    p = sub.add_parser("subscribe", help="stream new measurements of a station")
    p.add_argument("station_id")
    p.add_argument("--warning-level", choices=WARNING_LEVEL_CHOICES)
    p.add_argument("--deadline", type=float,
                   help="stop after this many seconds (default: run until Ctrl+C)")

    return parser


def fmt_time(ts):
    """Formats a protobuf Timestamp as UTC, like `2026-11-27 08:00:00 UTC`."""
    return ts.ToDatetime().strftime("%Y-%m-%d %H:%M:%S UTC")


def fmt_enum(enum_type, value, prefix):
    """Returns the enum name without its prefix, like `CRITICAL` for WARNING_LEVEL_CRITICAL."""
    return enum_type.Name(value).removeprefix(prefix)


def format_measurement(m):
    """Formats a measurement as one line with all its main values."""
    status = fmt_enum(pb2.StationStatus, m.station_status, "STATION_STATUS_")
    warning = fmt_enum(pb2.WarningLevel, m.warning_level, "WARNING_LEVEL_")
    return (f"{m.station_id}  {fmt_time(m.timestamp)}  "
            f"level={m.water_level:.1f} cm  discharge={m.flow_rate:.1f} m³/s  "
            f"rain={m.rainfall:.1f} mm/h  battery={m.battery_level:.0f}%  "
            f"status={status}  warning={warning}")


def format_station(s):
    """Formats a station as one line with its thresholds and current warning level."""
    warning = fmt_enum(pb2.WarningLevel, s.current_warning_level, "WARNING_LEVEL_")
    return (f"{s.id}  {s.name}  river={s.river}  active={'yes' if s.is_active else 'no'}  "
            f"warning={warning}  "
            f"(normal {s.normal_water_level:.0f} / warning {s.warning_water_level:.0f} / "
            f"critical {s.critical_water_level:.0f} cm)  "
            f"pos={s.location.latitude:.4f},{s.location.longitude:.4f}")


def format_alert(a):
    """Formats an alert as one line."""
    warning = fmt_enum(pb2.WarningLevel, a.warning_level, "WARNING_LEVEL_")
    return (f"{a.station_id}  {fmt_time(a.timestamp)}  {a.name} ({a.river})  "
            f"level={a.water_level:.1f} cm  warning={warning}")


def cmd_stations(stub, args):
    """Lists stations, optionally filtered by river, active state and warning level."""
    kwargs = {}
    if args.river:
        kwargs["river"] = args.river
    if args.active is not None:
        kwargs["is_active"] = args.active
    if args.warning_level:
        kwargs["warning_level"] = warning_level_value(args.warning_level)

    resp = stub.ListStations(pb2.ListStationsRequest(**kwargs), timeout=args.timeout)
    for s in resp.stations:
        print(format_station(s))
    print(f"-- {resp.total_count} station(s)")


def cmd_station(stub, args):
    """Shows one station."""
    s = stub.GetStation(pb2.GetStationRequest(station_id=args.station_id),
                        timeout=args.timeout)
    print(format_station(s))


def cmd_latest(stub, args):
    """Shows the latest measurement of a station."""
    m = stub.GetLatestMeasurement(
        pb2.GetLatestMeasurementRequest(station_id=args.station_id),
        timeout=args.timeout)
    print(format_measurement(m))


def cmd_measurements(stub, args):
    """Lists measurements of a station, optionally filtered by start, end, limit and warning level."""
    kwargs = {"station_id": args.station_id}
    if args.start:
        kwargs["start_time"] = args.start
    if args.end:
        kwargs["end_time"] = args.end
    if args.limit is not None:
        kwargs["limit"] = args.limit
    if args.warning_level:
        kwargs["warning_level"] = warning_level_value(args.warning_level)

    resp = stub.ListMeasurements(pb2.ListMeasurementsRequest(**kwargs),
                                 timeout=args.timeout)
    for m in resp.measurements:
        print(format_measurement(m))
    print(f"-- showing {len(resp.measurements)} of {resp.total_count} match(es)")


def cmd_alerts(stub, args):
    """Lists current alerts, optionally filtered by river and warning level."""
    kwargs = {}
    if args.river:
        kwargs["river"] = args.river
    if args.warning_level:
        kwargs["warning_level"] = warning_level_value(args.warning_level)

    resp = stub.ListAlerts(pb2.ListAlertsRequest(**kwargs), timeout=args.timeout)
    for a in resp.alerts:
        print(format_alert(a))
    print(f"-- {resp.total_count} alert(s)")


def run_unary(args):
    """Runs a unary command on one blocking channel, closed when the command is done."""
    with grpc.insecure_channel(args.address) as channel:
        stub = pb2_grpc.FloodMonitorServiceStub(channel)
        args.func(stub, args)


async def run_subscribe(args):
    """Prints new measurements of a station until the deadline passes or Ctrl+C is pressed.

    Without `--deadline` the call has no deadline. Cancelling the call on exit
    lets the server see that the client is gone.
    """
    kwargs = {"station_id": args.station_id}
    if args.warning_level:
        kwargs["warning_level"] = warning_level_value(args.warning_level)
    request = pb2.SubscribeMeasurementsRequest(**kwargs)

    async with grpc.aio.insecure_channel(args.address) as channel:
        stub = pb2_grpc.FloodMonitorServiceStub(channel)
        call = stub.SubscribeMeasurements(request, timeout=args.deadline)
        print(f"Subscribing to {args.station_id} on {args.address} (Ctrl+C to stop)",
              file=sys.stderr)
        try:
            async for m in call:
                print(format_measurement(m), flush=True)
        except grpc.RpcError as e:
            # The deadline is set by the user, so running out of it is a normal end.
            if e.code() != grpc.StatusCode.DEADLINE_EXCEEDED:
                raise
            print("Deadline reached, subscription ended.", file=sys.stderr)
        finally:
            call.cancel()


def main():
    """Parses the arguments, runs the command and turns gRPC errors into one readable line."""
    args = build_parser().parse_args()
    try:
        if args.command == "subscribe":
            asyncio.run(run_subscribe(args))
        else:
            run_unary(args)
    # AioRpcError is a subclass of RpcError, so this also covers the async call.
    except grpc.RpcError as e:
        print(f"gRPC error: {e.code().name}: {e.details()}", file=sys.stderr)
        sys.exit(1)
    except KeyboardInterrupt:
        print(file=sys.stderr)


if __name__ == "__main__":
    main()