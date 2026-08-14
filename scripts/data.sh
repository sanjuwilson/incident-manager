#!/usr/bin/env bash
set -e

TOPIC="msg_topic"
COUNT="${1:-100}"
RUN_ID="$(date +%s)"

for i in $(seq 1 "$COUNT"); do
  FAILURE_TYPE="TIMEOUT"

  if (( i % 5 == 0 )); then
    FAILURE_TYPE="DB_FAILURE"
  elif (( i % 3 == 0 )); then
    FAILURE_TYPE="SERVICE_UNAVAILABLE"
  fi

  echo "{\"sourceService\":\"order-service\",\"failureType\":\"$FAILURE_TYPE\",\"operation\":\"POST /place-order\",\"dependency\":\"product-service\",\"occurredAt\":\"$(date -u +"%Y-%m-%dT%H:%M:%SZ")\",\"correlationId\":\"load-test-$RUN_ID-$i\"}" \
  | kcat -P -b localhost:9092 -t "$TOPIC"

  echo "Sent event $i with failureType=$FAILURE_TYPE correlationId=load-test-$RUN_ID-$i"
  sleep 0.1
done