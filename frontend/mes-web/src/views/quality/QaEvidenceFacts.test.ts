import { describe, it, expect } from 'vitest'
import { render, cleanup } from '@testing-library/vue'
import IncomingFacts from './IncomingFacts.vue'
import { qaEvidenceLabel } from './qaEvidencePresentation'

describe('QA original evidence presentation', () => {
  it('uses Chinese headings recursively without altering original or revised values', () => {
    const source = {
      form: { id: '61' },
      values: [{ revisionNo: 1, value: '原始 FAIL', superseded: true },
        { revisionNo: 2, value: '批准复核记录', superseded: false }],
      reviews: [], ruleExecutions: [], signatures: [],
      futureEvidence: { anotherField: '原始补充内容' },
    }
    const before = JSON.stringify(source)
    const view = render(IncomingFacts, { props: { value: source, labelFormatter: qaEvidenceLabel } })
    for (const heading of ['基本信息', '原始记录与修订', '复核记录', '规则执行记录', '电子签名记录']) {
      expect(view.getByRole('heading', { name: heading })).toBeTruthy()
    }
    for (const original of ['原始 FAIL', '批准复核记录', '原始补充内容', '61']) {
      expect(view.getByText(original, { exact: true })).toBeTruthy()
    }
    expect(view.getAllByText('暂无记录')).toHaveLength(3)
    for (const raw of ['form', 'values', 'reviews', 'ruleExecutions', 'signatures', 'value', 'futureEvidence', 'anotherField']) {
      expect(view.queryByText(raw, { exact: true })).toBeNull()
    }
    expect(JSON.stringify(source)).toBe(before)
    cleanup()
  })

  it('preserves the existing incoming renderer when QA formatting is not supplied', () => {
    const view = render(IncomingFacts, { props: { value: { materialLotId: '81', conclusion: 'FAIL' } } })
    expect(view.getByText('物料批次', { exact: true })).toBeTruthy()
    expect(view.getByText('原始结论', { exact: true })).toBeTruthy()
    expect(view.getByText('不合格', { exact: true })).toBeTruthy()
    cleanup()
  })
})
