# Foundation API

`GET /api/v1/foundation/status` is the only Foundation application endpoint. It is public so operators can verify the shell without implementing IAM early.

Successful responses use the shared envelope:

```json
{
  "code": "OK",
  "message": "Foundation service is ready",
  "data": null,
  "traceId": "request-trace-id"
}
```

Clients may send `X-Trace-Id` when it matches `[A-Za-z0-9._-]{1,64}`. Invalid or missing values are replaced; the effective value is echoed in the response header and body. Unexpected exceptions return a generic `INTERNAL_ERROR` without exposing exception messages or stack traces.

OpenAPI is available at `/v3/api-docs`, Swagger UI at `/swagger-ui.html`, and health at `/actuator/health`. Other API routes remain denied by default. Token issuance and login endpoints are outside Foundation scope.
