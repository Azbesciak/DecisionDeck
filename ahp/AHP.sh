#!/usr/bin/env bash
# Usage:
#  AHP.sh [--v2|--v3] -i input_dir -o output_dir
source common_settings.sh
${CMD} "$@"
exit $?

