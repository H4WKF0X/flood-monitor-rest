#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p generated
python -m grpc_tools.protoc \
  -I ../contract/src/main/proto \
  --python_out=generated --grpc_python_out=generated --pyi_out=generated \
  flood_monitor.proto