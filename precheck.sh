#!/usr/bin/env bash

sbt clean coverage test it:test  a11y:test coverageReport