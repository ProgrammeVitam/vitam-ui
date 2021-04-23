#!/bin/sh

# Emmanuel Deviller
# Build the PDF file with Table of Contents
# sudo apt install pandoc texlive texlive-lang-french texlive-fonts-extra

[ -z "$1" ] && echo "error: markdown index source file must be supplied" && exit 1
[ ! -f "$1" ] && echo "error: $1 does not exist" && exit 1
[ -z "$2" ] && echo "error: output must be supplied" && exit 1

FILE=$(basename $1)
DIR=$(dirname $1)
[ -d "$DIR" ] && cd $DIR

TMP=/tmp/tmp_$$.md
OUT=out_$$.md
rm -f $TMP $OUT

[ -d "target" ] && rm -r target
mkdir target

echo "Generating $FILE => $2"

grep -v "^#" $FILE | grep -v "^[[:space:]]*$" | grep "^.*\. \[.*\](.*) *$" | sed "s/^[[:space:]]*//" | (
  while read -r line; do
#   echo $line

    # Tokenize line
    NUM=${line%[*}
    TIT=${line#${NUM}[}
    TIT=${TIT%]*}
    LNK=${line##*]\(}
    LNK=${LNK%%)*}
    LNK=${LNK%\#*}

    # Build scripts : replace ### TIT with ### NUM TIT
    SED="$SED -e \"s/^(#*) $TIT *$/\\\1 $NUM $TIT/g\""

    # Concatenate md files
    if [ -f $LNK -a "$OLDLNK" != "$LNK" ]; then
      cat $LNK >>$TMP
      echo " " >>$TMP
      OLDLNK=$LNK
    fi
  done

  eval sed -E $SED <$TMP >target/$OUT
  rm -f $TMP
)

echo "Generating PDF"

cd target
pandoc --atx-headers $OUT ../metadata.yaml -o "$2"
#pandoc --from markdown+pipe_tables "$1" metadata.yaml -o "$2.pdf"


echo "Generation Done"
