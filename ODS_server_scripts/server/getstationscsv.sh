#!/bin/sh
WHEN="2 days ago 13:00"
HOUR=$(date '+%H')
if [ $HOUR -gt 13 ]
then
  $WHEN="yesterday 13:00"
fi
YEAR=$(date -d "$WHEN" '+%Y')
MONTH=$(date -d "$WHEN" '+%m')
DATE=$(date -d "$WHEN" '+%d')

LINK="https://dev.azure.com/tankerkoenig/362e70d1-bafa-4cf7-a346-1f3613304973/_apis/git/repositories/0d6e7286-91e4-402c-af56-fa75be1f223d/items?path=%2Fstations%2F$YEAR%2F$MONTH%2F$YEAR-$MONTH-$DATE-stations.csv&versionDescriptor%5BversionOptions%5D=0&versionDescriptor%5BversionType%5D=0&versionDescriptor%5Bversion%5D=master&resolveLfs=true&%24format=octetStream&api-version=5.0&download=true"

mkdir -p public_html

wget -O "public_html/tankstellen.csv" "$LINK"
