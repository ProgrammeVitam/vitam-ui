# Introduction

Les fichiers **fr.json** et **en.json** à terme contiendront toutes les clefs de traductions des apps front.

# Aides

Pour récupérer toutes les clefs sous forme de tableau json :

```shell
jq '[ path(..) | join(".") ] | del(.[0]) | sort' fr.json > fr_key_flat.json
```

Pour récupérer toutes les clefs et leurs valeurs sous forme de tableau json :

```shell
jq '[ leaf_paths as $path | { "key": [ $path[] | tostring ] | join("."), "value": getpath($path) } ] | sort | from_entries' fr.json > fr_key_value_flat.json
```

Un script nodejs pour remettre les clefs sous forme d'objet:
commande 'node unflatten.js' avec unflatten.js :

```js
const fs = require('fs');

function unflatten(json) {
  if (Object(json) !== json || Array.isArray(json)) {
    return json;
  }
  const regex = /\.?([^.\[\]]+)|\[(\d+)\]/g;
  const resultholder = {};
  for (const p in json) {
    let cur = resultholder;
    let prop = '';
    let match;
    while ((match = regex.exec(p))) {
      cur = cur[prop] || (cur[prop] = match[2] ? [] : {});
      prop = match[2] || match[1];
    }
    cur[prop] = json[p];
  }
  return resultholder[''] || resultholder;
}

const fileData = fs.readFileSync('fr_key_value_flat2.json', 'utf8');
const newContent = unflatten(JSON.parse(fileData));
fs.writeFileSync('fr_key_value_unflat2.json', JSON.stringify(newContent, null, 2));
```
