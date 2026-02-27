import dayjs from 'dayjs'

export function formatDateTime(value, fmt = 'YYYY-MM-DD HH:mm:ss') {
  if (!value) {
    return '-'
  }
  const d = dayjs(value)
  return d.isValid() ? d.format(fmt) : String(value)
}

export function toLocalDateTimeString(value) {
  if (!value) {
    return undefined
  }
  const d = dayjs(value)
  return d.isValid() ? d.format('YYYY-MM-DDTHH:mm:ss') : undefined
}

export function parseJsonSafe(value, fallback = null) {
  if (value == null) {
    return fallback
  }
  if (typeof value === 'object') {
    return value
  }
  if (typeof value !== 'string') {
    return fallback
  }
  try {
    return JSON.parse(value)
  } catch {
    return fallback
  }
}

export function prettyJson(value) {
  if (value == null || value === '') {
    return ''
  }
  if (typeof value === 'string') {
    const parsed = parseJsonSafe(value, null)
    return parsed == null ? value : JSON.stringify(parsed, null, 2)
  }
  return JSON.stringify(value, null, 2)
}

export function splitCsv(value, parser = (v) => v) {
  if (!value || typeof value !== 'string') {
    return []
  }
  return value
    .split(',')
    .map((v) => v.trim())
    .filter(Boolean)
    .map(parser)
    .filter((v) => v !== undefined && v !== null && v !== '')
}

export function joinCsv(values = []) {
  if (!Array.isArray(values)) {
    return ''
  }
  return values.join(',')
}

export function toBooleanFlag(value) {
  if (value === true || value === 1 || value === '1') {
    return 1
  }
  return 0
}
