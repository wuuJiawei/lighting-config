#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-${1:-http://localhost:8082}}"

echo ">>> Target example-client: ${BASE_URL}"

curl_json() {
  local path=$1
  echo ""
  echo "--- GET ${path}"
  curl -sS -w "\nHTTP %{http_code}\n" "${BASE_URL}${path}"
}

curl_plain() {
  local path=$1
  echo ""
  echo "--- GET ${path}"
  curl -sS -w "\nHTTP %{http_code}\n" "${BASE_URL}${path}"
}

curl_plain "/feature/order"
curl_json "/feature/json-config"
curl_plain "/events/last"
curl_json "/examples/lighting-values"
curl_json "/examples/lighting-properties/order-routing"

echo ""
echo "Done."
