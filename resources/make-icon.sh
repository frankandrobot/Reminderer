#!/bin/sh

ldpiSize=32
mdpiSize=`echo "2 * $ldpiSize" | bc`
hdpiSize=`echo "3 * $ldpiSize" | bc`
xdpiSize=`echo "4 * $ldpiSize" | bc`

xdpi=res/drawable-xhdpi
hdpi=res/drawable-hdpi
mdpi=res/drawable-mdpi
ldpi=res/drawable-ldpi

mkdir -p $xdpi
mkdir -p $hdpi
mkdir -p $mdpi
mkdir -p $ldpi

echo "Making xdpi"
convert "$1" -resize ${xdpiSize}x$xdpiSize $xdpi/"$1"
echo "Making hdpi"
convert "$1" -resize ${hdpiSize}x$hdpiSize $hdpi/"$1"
echo "Making mdpi"
convert "$1" -resize ${mdpiSize}x$mdpiSize $mdpi/"$1"
echo "Making ldpi"
convert "$1" -resize ${ldpiSize}x$ldpiSize $ldpi/"$1"

