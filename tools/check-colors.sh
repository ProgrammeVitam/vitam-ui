#!/bin/bash

cd `dirname $0`
cd ..

hexa_color_regex='^(?!\s*(?>\*|\/\/|\/\*)).*\K(?<!&)(#[0-9a-fa-f]{6}|#[0-9a-fa-f]{3}(?![0-9a-fa-f]))(?!.*[{>])' # a hexa code of length 3 or 6, not followed by a "{" or ">" on the same line (to exclude matching of id css selector like "#fac... {}" or template reference like <ng-template #back>) and without being a comment line (starting with "//" or "/*" or " * "). Also, do not match if hexa code is preceded by a "&" to prevent matching html entities (like &#123;)
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
  -not -path "*/*.spec.ts"
)

allowed_colors=(
    "#9C31B5" # primary-(500)
    "#296EBC" # secondary-(500)
    "#C22A40" # tertiary
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
    "#FAFAFA" # grey-50
    "#F5F5F5" # grey-100
    "#EEEEEE" # grey-200
    "#E0E0E0" # grey-300
    "#BDBDBD" # grey-400
    "#9E9E9E" # grey-(500)
    "#757575" # grey-600
    "#616161" # grey-700
    "#424242" # grey-800
    "#212121" # grey-900
    "#FFFFFF" # white
    "#FCF7FD" # primary-light
    '#DFF3D8' # green-50
    '#51BC29' # green-300
    "#27740A" # green
    '#0E4403' # green-900
    '#FAE5E5' # red-50
    '#EA3E3E' # red-300
    "#C10000" # red
    '#9E0000' # red-900
    '#FBF1DF' # orange-50
    '#FFAE57' # orange-300
    "#EE7B00" # orange
    '#A85700' # orange-900

    "#0F0D2D" # background foncé
    "#000000" # black
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
