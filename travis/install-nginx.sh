#!/bin/sh

mkdir -p `pwd`/tmp/nginx/sites
cp `pwd`/config/raptor.test.conf `pwd`/tmp/nginx/sites/raptor.test.conf
cp `pwd`/travis/nginx.conf `pwd`/tmp/nginx/nginx.conf
