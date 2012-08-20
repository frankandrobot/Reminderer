#!/bin/sh

# renames a drawable
# changes all drawables in all drawable directories

find ../res/drawable* -type f -name "$1" -execdir mv '{}' "$2" \;