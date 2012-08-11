#!/bin/sh

xdpi=res/drawable-xhdpi
hdpi=res/drawable-hdpi
mdpi=res/drawable-mdpi
ldpi=res/drawable-ldpi

mkdir -p $xdpi
mkdir -p $hdpi
mkdir -p $mdpi
mkdir -p $ldpi

echo "Making xdpi"
convert "$1" -resize 192x192 $xdpi/"$1"
echo "Making hdpi"
convert "$1" -resize 128x128 $hdpi/"$1"
echo "Making mdpi"
convert "$1" -resize 64x164 $mdpi/"$1"
echo "Making ldpi"
convert "$1" -resize 32x32 $ldpi/"$1"

