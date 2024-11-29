#!/bin/bash

cd `dirname $0`
cd ..

hexa_color_regex='^(?!\s*(?>\*|\/\/|\/\*)).*\K(#[0-9a-fa-f]{6}|#[0-9a-fa-f]{3}(?![0-9a-fa-f]))(?!.*[{>])' # a hexa code of length 3 or 6, not followed by a "{" or ">" on the same line (to exclude matching of id css selector like "#fac... {}" or template reference like <ng-template #back>) and without being a comment line (starting with "//" or "/*" or " * ")
rgb_color_regex='rgba?\([\d, \/\.]+\)'
hsl_color_regex='(?<!new )hsla?\([\d, \/\.\%]+\)' # exclude "new HSL(...)"

find_arguments=(
  -type f \( -name "*.scss" -o -name "*.css" -o -name "*.ts" -o -name "*.html" \)
  -not -path "*/src/main/resources/templates" # excluding cas templates in which we define "--vitamui-*" css variables
  -not -path "*/dist/*"
  -not -path "*/node_modules/*"
  -not -path "*/target/*"
  -not -path "./docs/*"
  -not -path "./tools/*"
  -not -path "./deployment/*"
  -not -path "*/vitamui-icons.css" # TODO: we should remove colors from vitamui-icon-apps-colored icon instead of excluding from the check
)

allowed_colors=(
    "#9aa0ff" # additional
    "#296ebc" # secondary
    "#c22a40" # tertiary
    "#ffffff" # white
    "#604379" # header/footer
    "#0f0d2d" # background foncé
    "#FCF7FD" # background
    "#000000" # black
    "#c10000" # red
    "#bf511f" # orange
    "#27740a" # green

    "#F7EBFA" # primary-50
    "#E6C3EF" # primary-100
    "#D69BE4" # primary-200
    "#C573D9" # primary-300
    "#B54BCE" # primary-400
    "#9C31B5" # primary-(500)
    "#79268C" # primary-600
    "#561B64" # primary-700
    "#34103C" # primary-800
    "#110514" # primary-900

    "#EAF2FA" # secondary-50
    "#C0D7F1" # secondary-100
    "#96BDE8" # secondary-200
    "#6DA2DF" # secondary-300
    "#4388D6" # secondary-400
    "#296EBC" # secondary-(500)
    "#205692" # secondary-600
    "#173D69" # secondary-700
    "#0E253F" # secondary-800
    "#050C15" # secondary-900

    "#fafafa" # grey-50
    "#f5f5f5" # grey-100
    "#eeeeee" # grey-200
    "#e0e0e0" # grey-300
    "#bdbdbd" # grey-400
    "#9e9e9e" # grey-(500)
    "#757575" # grey-600
    "#616161" # grey-700
    "#424242" # grey-800
    "#212121" # grey-900

    "#E5E7FF" # additional-50
    "#D6D9FF" # additional-100
    "#C7CAFF" # additional-200
    "#B8BCFF" # additional-300
    "#A8ADFF" # additional-400
    "#9AA0FF" # additional-(500)
    "#5C65FF" # additional-600
    "#1F2CFF" # additional-700
    "#000DE0" # additional-800
    "#000AA3" # additional-900
)
files_allowed_colors=(
    "index\.html"
    "theme\.service\.ts"
    "standalone-theme\.service\.ts"
    "standalone-startup\.service\.ts"
    "design-system/.*/app\.module\.ts"
    "\.spec\.ts"
    "cas-server/.*/_colors\.scss"
    "cas-server/.*/cas\.css"
    "cas-server/.*/.*\.html"
)

allowed_colors_pattern="($(IFS="|"; echo "${allowed_colors[*]}"))"
files_allowed_colors_patter="($(IFS="|"; echo "${files_allowed_colors[*]}"))"
exclusions=".*${files_allowed_colors_patter}.*${allowed_colors_pattern}.*"

red='\033[0;31m'
green='\033[0;32m'
nc='\033[0m' # no color

has_unexpected_colors=false

function count_occurrences {
    if [[ -z "${1}" ]]; then
        echo 0
    else
        echo "${1}" | wc -l
    fi
}

function checkcolor() {
  context=$1
  regex=$2

  occurrences_with_filename=$(find . "${find_arguments[@]}" -exec grep -Pio "${regex}" {} + | grep -Eiv ${exclusions})

  filenames=$(echo "${occurrences_with_filename}" | cut -d ':' -f 1 | sort | uniq)
  canonical_occurrences=$(echo "${occurrences_with_filename}" | cut -d ':' -f 2-| tr '[:upper:]' '[:lower:]' | sed -E 's/ //g' | sort | uniq)

  nb_occurrences=$(count_occurrences "${occurrences_with_filename}")
  nb_different=$(count_occurrences "${canonical_occurrences}")
  nb_files=$(count_occurrences "${filenames}")

  if [[ $nb_occurrences -gt 0 ]]; then
    has_unexpected_colors=true
    echo -e "${red}${context}: found ${nb_occurrences} occurrences of ${nb_different} different values of unexpected colors in ${nb_files} files:${nc}"
    echo -e "${occurrences_with_filename}" | sed -E 's/^(.*):(.*)/\t\1\t\2/'
    echo -e ''
  else
    echo -e "${green}${context}: found no unexpected occurrence in the code${nc}"
  fi
}

checkcolor "hexa color code" "${hexa_color_regex}"
checkcolor "rgb(a) color code" "${rgb_color_regex}"
checkcolor "hsl(a) color code" "${hsl_color_regex}"

if [[ "$has_unexpected_colors" = true ]]; then
  exit 1;
fi
