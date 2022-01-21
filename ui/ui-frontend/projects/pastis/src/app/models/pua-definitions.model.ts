export class PuaDefinitions {

    "definitions": {
        "one-one": {
          "type": "string",
          "minItems": 1,
          "maxItems": 1
        },
        "one-many": {
          "type": "array",
          "minItems": 1
        },
        "zero-one": {
          "type": "string",
          "minItems": 0,
          "maxItems": 1
        },
        "zero-one2": {
          "type": "object",
          "minItems": 0,
          "maxItems": 1
        },
        "zero-many": {
          "type": "array",
          "minItems": 0
        },
        "zero-many2": {
          "type": "object",
          "minItems": 0
        },
        "non-empty-token": {
          "type": [
            "array"
          ],
          "minLength": 1
        },
        "non-empty-token-array": {
          "type": [
            "object"
          ],
          "items": {
            "type": "string",
            "minLength": 1
          }
        },
        "simple-date": {
          "type": "string",
          "pattern": "[0-9]{4}-[0-9]{2}-[0-9]{2}"
        },
        "date": {
          "type": "string",
          "pattern": "^([0-8][0-9]{3}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01]))$"
        }
    }
}
