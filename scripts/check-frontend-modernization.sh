#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
for app in learn_front-master learn_front_mange-master; do
  npm ci --prefix "$app"
  npm run typecheck --prefix "$app"
  npm test --prefix "$app"
  npm run build --prefix "$app"
done
npm ci
npm run test:inventory
