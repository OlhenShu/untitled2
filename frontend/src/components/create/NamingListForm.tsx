import { useRef } from 'react'
import { Upload } from 'lucide-react'
import { parseRepoNamesList } from '../../utils/nameGenerator'
import { Label } from '../ui/label'
import { Button } from '../ui/button'
import { Textarea } from '../ui/textarea'

interface Props {
  value: string
  onChange: (raw: string) => void
}

export default function NamingListForm({ value, onChange }: Props) {
  const fileRef = useRef<HTMLInputElement>(null)
  const parsed = parseRepoNamesList(value)

  const handleFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = ev => onChange(ev.target?.result as string)
    reader.readAsText(file)
    e.target.value = ''
  }

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <Label className="text-sm font-medium">
          Repository names <span className="text-red-500">*</span>
          <span className="text-slate-400 font-normal ml-1">(one per line)</span>
        </Label>
        <Button type="button" variant="ghost" size="sm" onClick={() => fileRef.current?.click()} className="gap-1.5 text-slate-500 h-8">
          <Upload className="w-3.5 h-3.5" />
          Upload .txt
        </Button>
        <input ref={fileRef} type="file" accept=".txt" className="hidden" onChange={handleFile} />
      </div>
      <Textarea
        value={value}
        onChange={e => onChange(e.target.value)}
        rows={6}
        placeholder={'repo-alice\nrepo-bob\nrepo-charlie\n# lines starting with # are ignored'}
        className="font-mono text-sm"
      />
      {parsed.length > 0 && (
        <p className="text-xs text-slate-500">{parsed.length} valid name{parsed.length !== 1 ? 's' : ''} parsed.</p>
      )}
    </div>
  )
}
