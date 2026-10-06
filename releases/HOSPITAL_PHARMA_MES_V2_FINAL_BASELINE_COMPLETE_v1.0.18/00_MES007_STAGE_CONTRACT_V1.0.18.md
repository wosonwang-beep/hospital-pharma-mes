# MES-007 staged definition implementation contract

DCP-MES-007-008-SEQUENCING-001; implementation mapping for baseline reconciliation, not a separate authority. All identifiers are decimal strings. All operations retain HTTP 200 and ApiResponse envelope. GET id is ebr_template_version.id, not a new template root. List is version rows, displaying templateCode. No template name exists.

Create: `{"packageVersionId":"123","templateCode":"EBR001"}` creates empty DRAFT version 1.
Save: `{"versionNo":0,"reason":"Configure approved design","definition":{"sections":[],"forms":[],"rules":[],"signatureRules":[],"reviewRules":[]}}`. Empty draft is editable but cannot pass publication lint.
Version: `{"versionNo":3,"reason":"New controlled version","copyDefinition":true}` copies the addressed version; false creates empty draft. Version family is org + templateCode; packageVersionId retained. Existing family has at most one DRAFT.
Lifecycle/lint: `{"versionNo":1,"reason":"Reviewed definition"}`.
Simulation: `{"versionNo":1,"reason":"Check tolerance","triggerPoint":"ON_SUBMIT","inputs":[{"fieldCode":"weight","values":[12.5]}]}`. Values are primitive JSON values; multiple values model repeat occurrences. Results are simulation evidence in returned/audited payload, not fabricated production rows.

All writes require Idempotency-Key; all existing-object writes require If-Match or matching body versionNo. Reason required. All reads and foreign references scope to the authenticated org; nonexistent and cross-org IDs return 404. Mutation uses transaction, template/family lock, optimistic version and platform idempotency/audit; no new infrastructure. DRAFT→SUBMITTED (lint)→APPROVED (independent creator/submitter)→EFFECTIVE (server-generated portable schema, canonical content hash). Non-DRAFT save returns 422. No new withdraw endpoint. Template approval records approvedBy/approvedAt with audit, retaining the frozen runtime-only electronic signature provider boundary.

Saved controlled rows are retained. Omitting a saved stable code or changing ownership returns 409 RETAINED_DEFINITION_REQUIRED; use a new empty version for structural replacement. Existing option/rule can be deactivated; label changes preserve codes. New rows can be added in DRAFT. Section/form/field/group/rule stable codes unique within template; option codes unique per field. Signature/review identity is objectScope/objectCode/meaning or reviewType/sequenceNo.

Server-only form schemaJson is generated from normalized definition rows. Client definition excludes row IDs, status and server schemaJson; joins use groupCode, visibilityRuleCode, formCode and fieldCode, resolved in same template. operationDefId must belong to linked process version; units and roles must exist and be usable. validationJson is the already-designed nullable JSON text, not a new business schema. Unknown DTO fields are rejected. No arbitrary JS/SQL/SpEL. DSL functions value/exists/sum/abs/round/convert/status/hasRole/now use bounded pure parsing; time and authorization come from the server, conversion from masterdata.

Errors: 400 invalid body/header/query/DSL syntax; 403 permission; 404 absent/cross-org; 409 VERSION_CONFLICT, IDEMPOTENCY_KEY_REUSED, IDEMPOTENCY_IN_PROGRESS, DUPLICATE_CODE, DRAFT_EXISTS, RETAINED_DEFINITION_REQUIRED; 422 LINT_FAILED/STATE_TRANSITION_NOT_ALLOWED/independence/reference constraints. Lint returns valid + issues for design defects, transition blocks on any issue.

Published query contract: requirePublished(org,templateVersionId) returns immutable canonical definition + definitionHash + templateVersionId/packageVersionId/version; consumers persist this exact payload at real batch release and never query latest definitions at runtime. No runtime tables, production writes, runtime API, PDF, or runtime sign provider in this stage.

## Operations

- `GET /ebr/templates` — `ebr:template:view`; request None; response EbrPage.
- `POST /ebr/templates` — `ebr:template:create`; request EbrCreate; response EbrDetail.
- `GET /ebr/templates/{id}` — `ebr:template:view`; request None; response EbrDetail.
- `PUT /ebr/templates/{id}` — `ebr:template:update`; request EbrSave; response EbrDetail.
- `POST /ebr/templates/{id}/versions` — `ebr:designer:edit`; request EbrVersionCreate; response EbrDetail.
- `POST /ebr/versions/{id}/approve` — `ebr:template:approve`; request EbrCommand; response EbrDetail.
- `GET /ebr/versions/{id}/compare` — `ebr:template:view`; request None; response EbrComparison.
- `POST /ebr/versions/{id}/lint` — `ebr:designer:edit`; request EbrCommand; response EbrLint.
- `POST /ebr/versions/{id}/publish` — `ebr:template:publish`; request EbrCommand; response EbrDetail.
- `POST /ebr/versions/{id}/simulate` — `ebr:designer:edit`; request EbrSimulation; response EbrSimulationResult.
- `POST /ebr/versions/{id}/submit` — `ebr:template:submit`; request EbrCommand; response EbrDetail.

## Additional derived view / phase boundary

EbrDetail.operationChoices contains existing process operation id/code/name; the UI selects these references. NEW_VERSION is returned for non-DRAFT states; existing family DRAFT still blocks duplication. Device mapping is a production-stage MES-010 gate: definition lint checks INSTRUMENT source consistency and never claims future device connectivity verified.

## Complete schema tree (OpenAPI 3.0)

```json
{
  "EbrOption": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "optionCode": {
        "type": "string",
        "maxLength": 64
      },
      "optionLabel": {
        "type": "string",
        "maxLength": 200
      },
      "optionValue": {
        "type": "string",
        "maxLength": 500
      },
      "sequenceNo": {
        "type": "integer",
        "minimum": 0
      },
      "activeFlag": {
        "type": "boolean"
      }
    },
    "required": [
      "optionCode",
      "optionLabel",
      "optionValue",
      "sequenceNo",
      "activeFlag"
    ]
  },
  "EbrField": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "fieldCode": {
        "type": "string",
        "maxLength": 64
      },
      "groupCode": {
        "type": "string",
        "maxLength": 64,
        "nullable": true
      },
      "label": {
        "type": "string",
        "maxLength": 200
      },
      "fieldType": {
        "type": "string",
        "enum": [
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
      "sourceType": {
        "type": "string",
        "enum": [
          "MANUAL",
          "BARCODE",
          "INSTRUMENT",
          "SYSTEM",
          "DERIVED"
        ]
      },
      "dataType": {
        "type": "string",
        "maxLength": 30
      },
      "unitId": {
        "type": "string",
        "pattern": "^[1-9][0-9]*$",
        "nullable": true
      },
      "precisionScale": {
        "type": "integer",
        "minimum": 0,
        "maximum": 12,
        "nullable": true
      },
      "requiredFlag": {
        "type": "boolean"
      },
      "readonlyFlag": {
        "type": "boolean"
      },
      "defaultExpr": {
        "type": "string",
        "maxLength": 4000,
        "nullable": true
      },
      "placeholder": {
        "type": "string",
        "maxLength": 500,
        "nullable": true
      },
      "helpText": {
        "type": "string",
        "maxLength": 1000,
        "nullable": true
      },
      "sequenceNo": {
        "type": "integer",
        "minimum": 0
      },
      "validationJson": {
        "type": "string",
        "maxLength": 16000,
        "nullable": true
      },
      "options": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrOption"
        },
        "maxItems": 500
      }
    },
    "required": [
      "fieldCode",
      "groupCode",
      "label",
      "fieldType",
      "sourceType",
      "dataType",
      "unitId",
      "precisionScale",
      "requiredFlag",
      "readonlyFlag",
      "defaultExpr",
      "placeholder",
      "helpText",
      "sequenceNo",
      "validationJson",
      "options"
    ]
  },
  "EbrGroup": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "groupCode": {
        "type": "string",
        "maxLength": 64
      },
      "title": {
        "type": "string",
        "maxLength": 200,
        "nullable": true
      },
      "sequenceNo": {
        "type": "integer",
        "minimum": 0
      },
      "layoutColumns": {
        "type": "integer",
        "minimum": 1,
        "maximum": 12
      },
      "repeatMode": {
        "type": "string",
        "enum": [
          "NONE",
          "LIST"
        ]
      },
      "minOccurs": {
        "type": "integer",
        "minimum": 0,
        "nullable": true
      },
      "maxOccurs": {
        "type": "integer",
        "minimum": 0,
        "nullable": true
      }
    },
    "required": [
      "groupCode",
      "title",
      "sequenceNo",
      "layoutColumns",
      "repeatMode",
      "minOccurs",
      "maxOccurs"
    ]
  },
  "EbrSection": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "sectionCode": {
        "type": "string",
        "maxLength": 64
      },
      "title": {
        "type": "string",
        "maxLength": 200
      },
      "sequenceNo": {
        "type": "integer",
        "minimum": 0
      },
      "repeatMode": {
        "type": "string",
        "enum": [
          "NONE",
          "LIST"
        ]
      },
      "visibilityRuleCode": {
        "type": "string",
        "maxLength": 64,
        "nullable": true
      },
      "pageBreakFlag": {
        "type": "boolean"
      },
      "groups": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrGroup"
        },
        "maxItems": 500
      }
    },
    "required": [
      "sectionCode",
      "title",
      "sequenceNo",
      "repeatMode",
      "visibilityRuleCode",
      "pageBreakFlag",
      "groups"
    ]
  },
  "EbrForm": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "formCode": {
        "type": "string",
        "maxLength": 64
      },
      "formName": {
        "type": "string",
        "maxLength": 200
      },
      "operationDefId": {
        "type": "string",
        "pattern": "^[1-9][0-9]*$",
        "nullable": true
      },
      "schemaVersion": {
        "type": "string",
        "maxLength": 20
      },
      "sequenceNo": {
        "type": "integer",
        "minimum": 0
      },
      "fields": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrField"
        },
        "maxItems": 500
      }
    },
    "required": [
      "formCode",
      "formName",
      "operationDefId",
      "schemaVersion",
      "sequenceNo",
      "fields"
    ]
  },
  "EbrRule": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "ruleCode": {
        "type": "string",
        "maxLength": 64
      },
      "formCode": {
        "type": "string",
        "maxLength": 64,
        "nullable": true
      },
      "fieldCode": {
        "type": "string",
        "maxLength": 64,
        "nullable": true
      },
      "ruleType": {
        "type": "string",
        "enum": [
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
      "triggerPoint": {
        "type": "string",
        "enum": [
          "ON_CHANGE",
          "ON_SAVE",
          "ON_SUBMIT",
          "ON_OPERATION_COMPLETE",
          "ON_BATCH_CLOSE"
        ]
      },
      "expression": {
        "type": "string",
        "maxLength": 4000
      },
      "severity": {
        "type": "string",
        "enum": [
          "BLOCK",
          "WARN"
        ],
        "nullable": true
      },
      "errorCode": {
        "type": "string",
        "maxLength": 64,
        "nullable": true
      },
      "messageTemplate": {
        "type": "string",
        "maxLength": 1000,
        "nullable": true
      },
      "deviationTrigger": {
        "type": "boolean"
      },
      "activeFlag": {
        "type": "boolean"
      }
    },
    "required": [
      "ruleCode",
      "formCode",
      "fieldCode",
      "ruleType",
      "triggerPoint",
      "expression",
      "severity",
      "errorCode",
      "messageTemplate",
      "deviationTrigger",
      "activeFlag"
    ]
  },
  "EbrSignatureRule": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "objectScope": {
        "type": "string",
        "maxLength": 30
      },
      "objectCode": {
        "type": "string",
        "maxLength": 64
      },
      "meaning": {
        "type": "string",
        "maxLength": 100
      },
      "requiredRole": {
        "type": "string",
        "maxLength": 64
      },
      "reauthRequired": {
        "type": "boolean"
      },
      "sequenceNo": {
        "type": "integer",
        "minimum": 0
      },
      "invalidateOnChange": {
        "type": "boolean"
      }
    },
    "required": [
      "objectScope",
      "objectCode",
      "meaning",
      "requiredRole",
      "reauthRequired",
      "sequenceNo",
      "invalidateOnChange"
    ]
  },
  "EbrReviewRule": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "objectScope": {
        "type": "string",
        "maxLength": 30
      },
      "objectCode": {
        "type": "string",
        "maxLength": 64
      },
      "reviewType": {
        "type": "string",
        "enum": [
          "VERIFY",
          "APPROVE"
        ]
      },
      "requiredRole": {
        "type": "string",
        "maxLength": 64
      },
      "independentUserRequired": {
        "type": "boolean"
      },
      "sequenceNo": {
        "type": "integer",
        "minimum": 0
      }
    },
    "required": [
      "objectScope",
      "objectCode",
      "reviewType",
      "requiredRole",
      "independentUserRequired",
      "sequenceNo"
    ]
  },
  "EbrDefinition": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "sections": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrSection"
        },
        "maxItems": 500
      },
      "forms": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrForm"
        },
        "maxItems": 500
      },
      "rules": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrRule"
        },
        "maxItems": 500
      },
      "signatureRules": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrSignatureRule"
        },
        "maxItems": 500
      },
      "reviewRules": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrReviewRule"
        },
        "maxItems": 500
      }
    },
    "required": [
      "sections",
      "forms",
      "rules",
      "signatureRules",
      "reviewRules"
    ]
  },
  "EbrCreate": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "packageVersionId": {
        "type": "string",
        "pattern": "^[1-9][0-9]*$"
      },
      "templateCode": {
        "type": "string",
        "maxLength": 64
      }
    },
    "required": [
      "packageVersionId",
      "templateCode"
    ]
  },
  "EbrSave": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "versionNo": {
        "type": "integer",
        "minimum": 0
      },
      "reason": {
        "type": "string",
        "maxLength": 1000
      },
      "definition": {
        "$ref": "#/components/schemas/EbrDefinition"
      }
    },
    "required": [
      "versionNo",
      "reason",
      "definition"
    ]
  },
  "EbrVersionCreate": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "versionNo": {
        "type": "integer",
        "minimum": 0
      },
      "reason": {
        "type": "string",
        "maxLength": 1000
      },
      "copyDefinition": {
        "type": "boolean"
      }
    },
    "required": [
      "versionNo",
      "reason",
      "copyDefinition"
    ]
  },
  "EbrCommand": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "versionNo": {
        "type": "integer",
        "minimum": 0
      },
      "reason": {
        "type": "string",
        "maxLength": 1000
      }
    },
    "required": [
      "versionNo",
      "reason"
    ]
  },
  "EbrInput": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "fieldCode": {
        "type": "string",
        "maxLength": 64
      },
      "values": {
        "type": "array",
        "items": {
          "oneOf": [
            {
              "type": "string"
            },
            {
              "type": "number"
            },
            {
              "type": "boolean"
            }
          ]
        },
        "maxItems": 500
      }
    },
    "required": [
      "fieldCode",
      "values"
    ]
  },
  "EbrSimulation": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "versionNo": {
        "type": "integer",
        "minimum": 0
      },
      "reason": {
        "type": "string",
        "maxLength": 1000
      },
      "triggerPoint": {
        "type": "string",
        "enum": [
          "ON_CHANGE",
          "ON_SAVE",
          "ON_SUBMIT",
          "ON_OPERATION_COMPLETE",
          "ON_BATCH_CLOSE"
        ]
      },
      "inputs": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrInput"
        },
        "maxItems": 500
      }
    },
    "required": [
      "versionNo",
      "reason",
      "triggerPoint",
      "inputs"
    ]
  },
  "EbrSummary": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "id": {
        "type": "string",
        "pattern": "^[1-9][0-9]*$"
      },
      "packageVersionId": {
        "type": "string",
        "pattern": "^[1-9][0-9]*$"
      },
      "templateCode": {
        "type": "string",
        "maxLength": 64
      },
      "version": {
        "type": "integer"
      },
      "status": {
        "type": "string",
        "enum": [
          "DRAFT",
          "SUBMITTED",
          "APPROVED",
          "EFFECTIVE",
          "WITHDRAWN"
        ]
      },
      "contentHash": {
        "type": "string",
        "maxLength": 128,
        "nullable": true
      },
      "effectiveFrom": {
        "type": "string",
        "format": "date-time",
        "nullable": true
      },
      "approvedBy": {
        "type": "string",
        "pattern": "^[1-9][0-9]*$",
        "nullable": true
      },
      "approvedAt": {
        "type": "string",
        "format": "date-time",
        "nullable": true
      },
      "versionNo": {
        "type": "integer",
        "minimum": 0
      },
      "allowedActions": {
        "type": "array",
        "items": {
          "type": "string",
          "maxLength": 32
        }
      }
    },
    "required": [
      "id",
      "packageVersionId",
      "templateCode",
      "version",
      "status",
      "contentHash",
      "effectiveFrom",
      "approvedBy",
      "approvedAt",
      "versionNo",
      "allowedActions"
    ]
  },
  "EbrDetail": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "id": {
        "type": "string",
        "pattern": "^[1-9][0-9]*$"
      },
      "packageVersionId": {
        "type": "string",
        "pattern": "^[1-9][0-9]*$"
      },
      "templateCode": {
        "type": "string",
        "maxLength": 64
      },
      "version": {
        "type": "integer"
      },
      "status": {
        "type": "string",
        "enum": [
          "DRAFT",
          "SUBMITTED",
          "APPROVED",
          "EFFECTIVE",
          "WITHDRAWN"
        ]
      },
      "contentHash": {
        "type": "string",
        "maxLength": 128,
        "nullable": true
      },
      "effectiveFrom": {
        "type": "string",
        "format": "date-time",
        "nullable": true
      },
      "approvedBy": {
        "type": "string",
        "pattern": "^[1-9][0-9]*$",
        "nullable": true
      },
      "approvedAt": {
        "type": "string",
        "format": "date-time",
        "nullable": true
      },
      "versionNo": {
        "type": "integer",
        "minimum": 0
      },
      "allowedActions": {
        "type": "array",
        "items": {
          "type": "string",
          "maxLength": 32
        }
      },
      "definition": {
        "$ref": "#/components/schemas/EbrDefinition"
      },
      "versions": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrSummary"
        },
        "maxItems": 500
      },
      "operationChoices": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrOperationChoice"
        }
      }
    },
    "required": [
      "id",
      "packageVersionId",
      "templateCode",
      "version",
      "status",
      "contentHash",
      "effectiveFrom",
      "approvedBy",
      "approvedAt",
      "versionNo",
      "allowedActions",
      "definition",
      "versions",
      "operationChoices"
    ]
  },
  "EbrPage": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "items": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrSummary"
        },
        "maxItems": 500
      },
      "total": {
        "type": "integer",
        "minimum": 0
      },
      "page": {
        "type": "integer",
        "minimum": 0
      },
      "size": {
        "type": "integer",
        "minimum": 0
      }
    },
    "required": [
      "items",
      "total",
      "page",
      "size"
    ]
  },
  "EbrIssue": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "path": {
        "type": "string",
        "maxLength": 500
      },
      "code": {
        "type": "string",
        "maxLength": 64
      },
      "message": {
        "type": "string",
        "maxLength": 1000
      }
    },
    "required": [
      "path",
      "code",
      "message"
    ]
  },
  "EbrLint": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "valid": {
        "type": "boolean"
      },
      "issues": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrIssue"
        },
        "maxItems": 500
      }
    },
    "required": [
      "valid",
      "issues"
    ]
  },
  "EbrRuleResult": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "ruleCode": {
        "type": "string",
        "maxLength": 64
      },
      "passed": {
        "type": "boolean"
      },
      "severity": {
        "type": "string",
        "maxLength": 20,
        "nullable": true
      },
      "message": {
        "type": "string",
        "maxLength": 1000,
        "nullable": true
      },
      "inputSnapshot": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrInput"
        },
        "maxItems": 500
      },
      "outputValue": {
        "oneOf": [
          {
            "type": "string"
          },
          {
            "type": "number"
          },
          {
            "type": "boolean"
          }
        ],
        "nullable": true
      },
      "executedAt": {
        "type": "string",
        "format": "date-time"
      },
      "engineVersion": {
        "type": "string",
        "maxLength": 30
      },
      "ruleVersion": {
        "type": "string",
        "maxLength": 128
      }
    },
    "required": [
      "ruleCode",
      "passed",
      "severity",
      "message",
      "inputSnapshot",
      "outputValue",
      "executedAt",
      "engineVersion",
      "ruleVersion"
    ]
  },
  "EbrSimulationResult": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "allowed": {
        "type": "boolean"
      },
      "results": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrRuleResult"
        },
        "maxItems": 500
      }
    },
    "required": [
      "allowed",
      "results"
    ]
  },
  "EbrDifference": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "path": {
        "type": "string",
        "maxLength": 500
      },
      "before": {
        "type": "string",
        "maxLength": 16000,
        "nullable": true
      },
      "after": {
        "type": "string",
        "maxLength": 16000,
        "nullable": true
      }
    },
    "required": [
      "path",
      "before",
      "after"
    ]
  },
  "EbrComparison": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "leftId": {
        "type": "string",
        "pattern": "^[1-9][0-9]*$"
      },
      "rightId": {
        "type": "string",
        "pattern": "^[1-9][0-9]*$"
      },
      "differences": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/EbrDifference"
        },
        "maxItems": 500
      }
    },
    "required": [
      "leftId",
      "rightId",
      "differences"
    ]
  },
  "EbrOperationChoice": {
    "type": "object",
    "additionalProperties": false,
    "properties": {
      "id": {
        "type": "string"
      },
      "operationCode": {
        "type": "string"
      },
      "operationName": {
        "type": "string"
      }
    },
    "required": [
      "id",
      "operationCode",
      "operationName"
    ]
  }
}
```


This appendix is normative within v1.0.18; the repository development handoff is historical preparation only.


## Approved functional closure delta — v1.0.18

DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.18.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.18.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.
