# third_place_allocation.csv

FIFA's official allocation of the 8 best third-placed teams to Round-of-32 bracket slots for the
2026 World Cup (Annex C of the tournament regulations; all C(12,8) = 495 combinations).

## Format
One line per combination, no header:

```
<key>,<1A>,<1B>,<1D>,<1E>,<1G>,<1I>,<1K>,<1L>
```

- `key` — the 8 groups (of A–L) whose third-placed teams advance, as sorted letters, e.g. `EFGHIJKL`.
- The next 8 fields — the third-placed group that faces the **group winner** in each bracket slot,
  in the fixed column order `1A, 1B, 1D, 1E, 1G, 1I, 1K, 1L`.

Example: `EFGHIJKL,E,J,I,F,H,G,L,K` → Winner A vs 3rd-E, Winner B vs 3rd-J, Winner D vs 3rd-I, …

## Provenance / how to regenerate
Parsed from Wikipedia's `Template:2026 FIFA World Cup third-place table` (raw wikitext), which
transcribes FIFA Annex C. Each row was validated to contain exactly 8 advancing groups and 8
assignments forming a permutation of those groups. Do not hand-edit; regenerate from source.
