export interface Attribute {key:string;type:string;nullable:boolean;options?:string[];max?:number;min?:number;maxNumber?:number}
export const attributes:Record<string,Attribute[]>={
  "EbrSection": [
    {
      "key": "sectionCode",
      "type": "string",
      "nullable": false,
      "max": 64
    },
    {
      "key": "title",
      "type": "string",
      "nullable": false,
      "max": 200
    },
    {
      "key": "sequenceNo",
      "type": "integer",
      "nullable": false,
      "min": 0
    },
    {
      "key": "repeatMode",
      "type": "string",
      "nullable": false,
      "options": [
        "NONE",
        "LIST"
      ]
    },
    {
      "key": "visibilityRuleCode",
      "type": "string",
      "nullable": true,
      "max": 64
    },
    {
      "key": "pageBreakFlag",
      "type": "boolean",
      "nullable": false
    }
  ],
  "EbrGroup": [
    {
      "key": "groupCode",
      "type": "string",
      "nullable": false,
      "max": 64
    },
    {
      "key": "title",
      "type": "string",
      "nullable": true,
      "max": 200
    },
    {
      "key": "sequenceNo",
      "type": "integer",
      "nullable": false,
      "min": 0
    },
    {
      "key": "layoutColumns",
      "type": "integer",
      "nullable": false,
      "min": 1,
      "maxNumber": 12
    },
    {
      "key": "repeatMode",
      "type": "string",
      "nullable": false,
      "options": [
        "NONE",
        "LIST"
      ]
    },
    {
      "key": "minOccurs",
      "type": "integer",
      "nullable": true,
      "min": 0
    },
    {
      "key": "maxOccurs",
      "type": "integer",
      "nullable": true,
      "min": 0
    }
  ],
  "EbrForm": [
    {
      "key": "formCode",
      "type": "string",
      "nullable": false,
      "max": 64
    },
    {
      "key": "formName",
      "type": "string",
      "nullable": false,
      "max": 200
    },
    {
      "key": "operationDefId",
      "type": "string",
      "nullable": true
    },
    {
      "key": "schemaVersion",
      "type": "string",
      "nullable": false,
      "max": 20
    },
    {
      "key": "sequenceNo",
      "type": "integer",
      "nullable": false,
      "min": 0
    }
  ],
  "EbrField": [
    {
      "key": "fieldCode",
      "type": "string",
      "nullable": false,
      "max": 64
    },
    {
      "key": "groupCode",
      "type": "string",
      "nullable": true,
      "max": 64
    },
    {
      "key": "label",
      "type": "string",
      "nullable": false,
      "max": 200
    },
    {
      "key": "fieldType",
      "type": "string",
      "nullable": false,
      "options": [
        "NUMBER",
        "TEXT",
        "TEXTAREA",
        "ENUM",
        "MULTI_ENUM",
        "BOOLEAN",
        "DATE",
        "TIME",
        "DATETIME",
        "BARCODE",
        "MATERIAL_LOT",
        "CONTAINER",
        "EQUIPMENT",
        "PERSON",
        "ATTACHMENT",
        "IMAGE",
        "TIMER",
        "CALCULATED",
        "INSTRUMENT_VALUE",
        "SIGNATURE_PLACEHOLDER"
      ]
    },
    {
      "key": "sourceType",
      "type": "string",
      "nullable": false,
      "options": [
        "MANUAL",
        "BARCODE",
        "INSTRUMENT",
        "SYSTEM",
        "DERIVED"
      ]
    },
    {
      "key": "dataType",
      "type": "string",
      "nullable": false,
      "max": 30
    },
    {
      "key": "unitId",
      "type": "string",
      "nullable": true
    },
    {
      "key": "precisionScale",
      "type": "integer",
      "nullable": true,
      "min": 0,
      "maxNumber": 12
    },
    {
      "key": "requiredFlag",
      "type": "boolean",
      "nullable": false
    },
    {
      "key": "readonlyFlag",
      "type": "boolean",
      "nullable": false
    },
    {
      "key": "defaultExpr",
      "type": "string",
      "nullable": true,
      "max": 4000
    },
    {
      "key": "placeholder",
      "type": "string",
      "nullable": true,
      "max": 500
    },
    {
      "key": "helpText",
      "type": "string",
      "nullable": true,
      "max": 1000
    },
    {
      "key": "sequenceNo",
      "type": "integer",
      "nullable": false,
      "min": 0
    },
    {
      "key": "validationJson",
      "type": "string",
      "nullable": true,
      "max": 16000
    }
  ],
  "EbrOption": [
    {
      "key": "optionCode",
      "type": "string",
      "nullable": false,
      "max": 64
    },
    {
      "key": "optionLabel",
      "type": "string",
      "nullable": false,
      "max": 200
    },
    {
      "key": "optionValue",
      "type": "string",
      "nullable": false,
      "max": 500
    },
    {
      "key": "sequenceNo",
      "type": "integer",
      "nullable": false,
      "min": 0
    },
    {
      "key": "activeFlag",
      "type": "boolean",
      "nullable": false
    }
  ],
  "EbrRule": [
    {
      "key": "ruleCode",
      "type": "string",
      "nullable": false,
      "max": 64
    },
    {
      "key": "formCode",
      "type": "string",
      "nullable": true,
      "max": 64
    },
    {
      "key": "fieldCode",
      "type": "string",
      "nullable": true,
      "max": 64
    },
    {
      "key": "ruleType",
      "type": "string",
      "nullable": false,
      "options": [
        "VALIDATION",
        "CALCULATION",
        "VISIBILITY",
        "BRANCH",
        "COMPLETION",
        "SIGNATURE",
        "REVIEW",
        "DEVIATION"
      ]
    },
    {
      "key": "triggerPoint",
      "type": "string",
      "nullable": false,
      "options": [
        "ON_CHANGE",
        "ON_SAVE",
        "ON_SUBMIT",
        "ON_OPERATION_COMPLETE",
        "ON_BATCH_CLOSE"
      ]
    },
    {
      "key": "expression",
      "type": "string",
      "nullable": false,
      "max": 4000
    },
    {
      "key": "severity",
      "type": "string",
      "nullable": true,
      "options": [
        "BLOCK",
        "WARN"
      ]
    },
    {
      "key": "errorCode",
      "type": "string",
      "nullable": true,
      "max": 64
    },
    {
      "key": "messageTemplate",
      "type": "string",
      "nullable": true,
      "max": 1000
    },
    {
      "key": "deviationTrigger",
      "type": "boolean",
      "nullable": false
    },
    {
      "key": "activeFlag",
      "type": "boolean",
      "nullable": false
    }
  ],
  "EbrSignatureRule": [
    {
      "key": "objectScope",
      "type": "string",
      "nullable": false,
      "max": 30
    },
    {
      "key": "objectCode",
      "type": "string",
      "nullable": false,
      "max": 64
    },
    {
      "key": "meaning",
      "type": "string",
      "nullable": false,
      "max": 100
    },
    {
      "key": "requiredRole",
      "type": "string",
      "nullable": false,
      "max": 64
    },
    {
      "key": "reauthRequired",
      "type": "boolean",
      "nullable": false
    },
    {
      "key": "sequenceNo",
      "type": "integer",
      "nullable": false,
      "min": 0
    },
    {
      "key": "invalidateOnChange",
      "type": "boolean",
      "nullable": false
    }
  ],
  "EbrReviewRule": [
    {
      "key": "objectScope",
      "type": "string",
      "nullable": false,
      "max": 30
    },
    {
      "key": "objectCode",
      "type": "string",
      "nullable": false,
      "max": 64
    },
    {
      "key": "reviewType",
      "type": "string",
      "nullable": false,
      "options": [
        "VERIFY",
        "APPROVE"
      ]
    },
    {
      "key": "requiredRole",
      "type": "string",
      "nullable": false,
      "max": 64
    },
    {
      "key": "independentUserRequired",
      "type": "boolean",
      "nullable": false
    },
    {
      "key": "sequenceNo",
      "type": "integer",
      "nullable": false,
      "min": 0
    }
  ]
}
