#! /bin/bash
# Usage:
#  run.sh [--v2|--v3] input_dir output_dir
# Adapt if needed.  R v3.x is required
# Use the full path here
R3=/usr/local/test-v3/R-3.5.2/bin/R

# These 2 instruct the XMCDA Java lib to use a specific version when writing
export XMCDAv2_VERSION=2.2.2
export XMCDAv3_VERSION=3.1.0

if [ ! -x "${R3}" ]; then
  echo "Please edit '$0': R exec not found" >&2
  exit 1
fi
if [ ! "version 3" = "$(${R3} --version | head -1 | grep -o 'version [0-9]')" ]; then
  echo "Please edit '$0': $R3 is not R version 3.x" >&2
  exit 2
fi

# -- You normally do not need to change anything beyond this point --
if [ ! $# == 3 ]; then
  echo "Usage: $0 [--v2|--v3] input_dir output_dir" >&2
  exit 3
elif [ "$1" = "--v3" ]; then
  "${R3}" --slave --vanilla --file=src/dematelInfluenceCLI_XMCDAv3.R --args "$2" "$3"
  ret=$?
elif [ "$1" = "--v2" ]; then
  "${R3}" --slave --vanilla --file=src/dematelInfluenceCLI_XMCDAv2.R --args "$2" "$3"
  ret=$?
else
  echo "Usage: $0 [--v2|--v3] input_dir output_dir" >&2
  exit 4
fi
exit $ret
