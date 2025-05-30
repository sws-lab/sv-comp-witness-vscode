#!/bin/bash

# This file is part of nacpa,
# a native parallel execution of CPAchecker:
# https://gitlab.com/sosy-lab/software/nacpa
#
# An initial version of this file was copied from
# https://gitlab.com/sosy-lab/software/cpa-daemon.
#
# SPDX-FileCopyrightText: 2023-2024 Dirk Beyer <https://www.sosy-lab.org>
#
# SPDX-License-Identifier: Apache-2.0

set -euo pipefail
set -x

SCRIPT_DIR=$(dirname "$(realpath "$0")")

CPACHECKER_DIR=$SCRIPT_DIR/cpachecker
OUTPUT_DIR=$SCRIPT_DIR
CPACHECKER_OPTS="--config config/generateCFA.properties --option cfa.exportCfaAsync=false --output-path /tmp/cpachecker-output --heap 8G --option cfa.pathForExportingVariablesInScopeWithTheirType=/tmp/cpachecker-output/variablesInScope.txt"
DEFAULT_TIMELIMIT="--timelimit 20"
mkdir -p "$OUTPUT_DIR"
(
  cd $CPACHECKER_DIR
  ant
  export JAVA_VM_ARGUMENTS="-agentlib:native-image-agent=config-merge-dir=$OUTPUT_DIR/META-INF/native-image"

  for file in doc/examples/*.c; do
    bin/cpachecker $CPACHECKER_OPTS $DEFAULT_TIMELIMIT "$file"
  done
)
