/** Presentation only: preserve stored decimal values without rounding or unit conversion. */
export function businessQuantity(value:unknown):string {
 const raw=value==null?'':String(value).trim()
 if(!raw)return '—'
 if(!/^-?\d+(?:\.\d+)?$/.test(raw))return raw
 const [integer='',fraction='']=raw.split('.')
 const sign=integer.startsWith('-')?'-':''
 const grouped=integer.replace(/^-/, '').replace(/\B(?=(\d{3})+(?!\d))/g, ',')
 const meaningful=fraction.replace(/0+$/, '')
 return sign+grouped+(meaningful?'.'+meaningful:'')
}
