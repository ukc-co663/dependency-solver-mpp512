#!/bin/bash
for f in $(ls -d tests/exampleTest/*); do
  echo "Running $f"
  ./solve $f/repository.json $f/initial.json $f/constraints.json
done
