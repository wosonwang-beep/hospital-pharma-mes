import test from 'node:test';
import assert from 'node:assert/strict';
import { scenarios, materialRoutes, canPerform, resolveScenario } from '../prototype/model.mjs';

test('freezes fourteen uniquely routed formal scenarios', () => {
  assert.equal(scenarios.length, 14);
  assert.equal(new Set(scenarios.map((item) => item.id)).size, 14);
  assert.equal(new Set(scenarios.map((item) => item.route)).size, 14);
  assert.deepEqual(scenarios.slice(0, 2).map((item) => item.task), ['MES-001-R2', 'MES-001-R2']);
  assert.deepEqual(scenarios.slice(2).map((item) => item.task), Array.from({ length: 12 }, (_, index) => `MES-${String(index + 2).padStart(3, '0')}-R2`));
});

test('material query create view and edit use separate routes and surface modes', () => {
  assert.deepEqual(materialRoutes, {
    query: '/master/materials',
    create: '/master/materials/create',
    view: '/master/materials/MAT-00028',
    edit: '/master/materials/MAT-00028/edit',
  });
  assert.equal(resolveScenario(materialRoutes.query).mode, 'query');
  assert.equal(resolveScenario(materialRoutes.create).mode, 'create');
  assert.equal(resolveScenario(materialRoutes.view).mode, 'view');
  assert.equal(resolveScenario(materialRoutes.edit).mode, 'edit');
});

test('actions require both permission and server allowedActions membership', () => {
  const context = { permissions: ['production:batch:start'], allowedActions: ['START', 'PAUSE'] };
  assert.equal(canPerform(context, 'production:batch:start', 'START'), true);
  assert.equal(canPerform(context, 'production:batch:complete', 'COMPLETE'), false);
  assert.equal(canPerform(context, 'production:batch:start', 'RELEASE'), false);
});

test('every frozen scenario declares traceability and a png screenshot', () => {
  for (const scenario of scenarios) {
    assert.match(scenario.requirement, /[A-Z]+(?:-[A-Z]+)*-\d{3}|IAM-001/);
    assert.match(scenario.permission, /^[a-z]+:[a-z-]+(?::[a-z-]+)?$/);
    assert.match(scenario.api, /^(GET|POST|PUT) \/.+|^UNRESOLVED:/);
    assert.match(scenario.screenshot, /^UI-[A-Z0-9-]+\.png$/);
  }
});


test('v1.0.2 removes the obsolete platform route and separates formal MES-001 pages', () => {
  assert.equal(scenarios.some((item) => item.route === '/platform/operations'), false);
  assert.equal(scenarios.some((item) => item.requirement === 'PLAT-001'), false);
  assert.equal(resolveScenario('/audit').id, 'UI-AUD-Q');
  assert.equal(resolveScenario('/integration/operations').id, 'UI-INT-OPS');
});


test('all prototype routes follow formal UI V1.1 route templates', () => {
  assert.equal(resolveScenario('/wms/receipts').id, 'UI-WMS-REC-Q');
  assert.equal(resolveScenario('/mes/execution/EU-240928-03').id, 'UI-EXEC-W');
  assert.equal(resolveScenario('/mes/execution/EU-240928-03/weighing').id, 'UI-WGH-W');
  assert.equal(resolveScenario('/production/batches/MB-20260928-006/balance').id, 'UI-BAL-V');
});

