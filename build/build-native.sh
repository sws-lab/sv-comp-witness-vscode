#!/bin/bash

# This file is part of CPA-Daemon,
# a gRPC frontend for CPAchecker:
# https://gitlab.com/sosy-lab/software/cpa-daemon/
#
# SPDX-FileCopyrightText: 2023 Dirk Beyer <https://www.sosy-lab.org>
#
# SPDX-License-Identifier: Apache-2.0

set -euo pipefail
# set -x

if [[ "$#" -lt 3 ]]; then
  echo "Usage: $0 <cpachecker-dir> <meta-inf> <output-dir> [build-args]"
  echo
  echo -e "\tcpachecker  the CPAchecker directory. Can be a release or the repository"
  echo -e "\t  meta-inf  the META-INF directory with reflection information"
  echo -e "\t            about CPAchecker, as expected by GraalVM native-image."
  echo -e "\tbuild-args  build arguments to pass to GraalVM native-image."
  echo -e "\toutput-dir  output directory"
  echo
  echo -e "See https://docs.oracle.com/en/graalvm/enterprise/20/docs/reference-manual/native-image/Reflection/#use-of-reflection-during-native-image-generation"
  echo -e "for more information about reflection information."
  exit 1
fi

SCRIPT_DIR=$(dirname $(realpath "$0"))
CPACHECKER_DIR=$(realpath "$1")
NATIVE_META_INFO_DIR=$(realpath "$2")
OUTPUT_DIR=$(realpath "$3")
NATIVE_IMAGE_ARGS=${@:4} # This captures any arguments passed from the Makefile's NATIVE_BUILD_PARAMS

if [[ "$(basename "${NATIVE_META_INFO_DIR}")" != "META-INF" ]]; then
  2>&1 echo "The META-INF directory '$NATIVE_META_INFO_DIR' must be named 'META-INF'"
  exit 1
fi

mkdir -p $OUTPUT_DIR
TMP_DIR=$(mktemp -d)
if [[ ! -e "$CPACHECKER_DIR/cpachecker.jar" ]]; then
    echo "$CPACHECKER_DIR/cpachecker.jar does not exist, trying to build it with ant"
    (
      cd "$CPACHECKER_DIR"
      ant jar
    )
fi
cp "$CPACHECKER_DIR"/cpachecker.jar "$TMP_DIR"/cpachecker.jar
(
  cd "$(dirname "$NATIVE_META_INFO_DIR")"
  zip -gr "$TMP_DIR"/cpachecker.jar ./META-INF > /dev/null
)
# We use the --gc=serial garbage collector because it leads to significantly smaller memory consumption, compared to G1.
# Example comparison:
# ```
# runexec --no-container --memlimit 10GB -- build/cpachecker/native-build/cpachecker --no-output-files --spec ../data/sv-benchmarks/c/properties/unreach-call.prp --config lib/cpachecker-native/config/components/svcomp24--configselection-restartcomponent-valueAnalysis-itp-end.properties ../data/sv-benchmarks/c/eca-rers2012/Problem03_label57.c
# ```
# With -O3 and --gc=G1:
# ```
# walltime=17.497749459027546s
# cputime=23.719593594s
# memory=2513932288B
# ```
# With -O3 and --gc=serial:
# ```
# walltime=19.730200088000856s
# cputime=19.722337101s
# memory=749862912B
# ```
#
# CPAchecker requires a large heap. We define both min and max.
# From https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/MemoryManagement/:
# The minimum Java heap size defines how much memory the GC may always assume as reserved for the Java heap, no matter how little of that memory is actually used.
# The maximum Java heap size defines the upper limit for the size of the whole Java heap. If the Java heap is full and the GC is unable reclaim sufficient memory for a Java object allocation, the allocation will fail with the OutOfMemoryError.
NATIVE_IMAGE_ARGS="$NATIVE_IMAGE_ARGS \
    --initialize-at-run-time=sun.reflect.misc.Trampoline \
    --verbose \
    --no-fallback \
    --gc=serial \
    -R:MinHeapSize=1000m \
    -R:MaxHeapSize=15000m \
    -H:+ReportExceptionStackTraces \
    -H:+AddAllCharsets \
    -g \
    -jar $TMP_DIR/cpachecker.jar \
    -cp $CPACHECKER_DIR/lib/*:$CPACHECKER_DIR/lib/java/runtime/* \
    --enable-url-protocols=http,https"
(
  cd $OUTPUT_DIR
  native-image $NATIVE_IMAGE_ARGS
    # --enable-monitoring=jfr \
    # --bundle-create=native-build.nib \
    #-H:+BuildReport \
)
rm -r $TMP_DIR