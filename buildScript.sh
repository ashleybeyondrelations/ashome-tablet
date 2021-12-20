#!/bin/sh
[ -f "target" ] && rm target -r >/dev/null || true

sh gradlew build publish || exit $?

mkdir -p target/
rm -rf target/*
mkdir -p target/packages/
mkdir -p target/packages/assets/
for f in build/libs/*sources.jar
do
       	rm $f -f 
done
for f in build/libs/*.jar
do
       	mv $f target/packages/
done
cp public target/packages/assets/ -r
return 0
