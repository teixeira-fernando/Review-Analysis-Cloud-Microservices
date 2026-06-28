#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cleanup() {
  cd "$ROOT_DIR"
  docker compose down --remove-orphans || true
}
trap cleanup EXIT

cd "$ROOT_DIR"

echo "Starting LocalStack..."
docker compose up -d localstack

echo "Waiting for LocalStack to be healthy..."
for i in $(seq 1 30); do
  if curl -sf "http://localhost:4566/_localstack/health" > /dev/null; then
    echo "LocalStack is ready"
    break
  fi

  if [[ "$i" -eq 30 ]]; then
    echo "LocalStack did not become healthy in time"
    exit 1
  fi

  sleep 5
done

echo "Provisioning resources with Terraform..."
cd "$ROOT_DIR/terraform"
terraform init
terraform apply -var-file=local.tfvars -auto-approve

cd "$ROOT_DIR"
echo "Running E2E stack..."
docker compose up --build --exit-code-from e2e-tests --abort-on-container-exit