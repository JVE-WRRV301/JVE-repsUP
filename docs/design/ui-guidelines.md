# UI Guidelines — Reps Up

## Theme
- Dark mode inspired by gaming applications
- Reduces eye strain in low-lit gym environments

## Colours
| Name              | Hex       | Usage                                      |
|-------------------|-----------|--------------------------------------------|
| dark_grey         | #393944   | Primary background                         |
| dark_grey_variant | #2D2D38   | Card backgrounds, slightly darker          |
| dark_grey_surface | #4A4A55   | Input fields, unselected buttons           |
| light_blue        | #B2D6FD   | All buttons, highlights, progress bars     |
| red               | #FF5050   | Errors, warnings, goal cards               |
| yellow            | #FAD942   | XP, coins, achievements                    |
| white             | #FFFFFF   | Primary text                               |
| muted             | #B0B0B8   | Secondary text, labels                     |

## Typography
| Style    | Font     | Usage                              |
|----------|----------|------------------------------------|
| Headings | Orbitron | Screen titles, section headers     |
| Body     | Inter    | All other text, buttons, labels    |

## Font Weights
- Regular (400) — body text
- Medium (500) — labels, secondary headings
- Bold (700) — primary headings, button text

## Buttons
- Primary: light_blue background, dark_grey text, rounded corners
- Secondary: dark_grey_surface background, white text, rounded corners
- Disabled: reduced opacity on primary button

## Cards
- Background: dark_grey_variant
- Workout cards: light_blue left border
- Goal cards: red left border
- Corner radius: 12dp
- Padding: 16dp

## Input Fields
- Background: dark_grey_surface
- Text: white
- Hint text: muted
- Border: none by default, light_blue when focused

## Progress Bars
- Segmented style (assessment screens)
- Completed segments: light_blue
- Incomplete segments: dark_grey_surface
- Height: 4dp, rounded corners

## Spacing
- Screen padding: 24dp
- Between components: 12dp
- Between sections: 24dp

## Icons
- Use Material Icons where possible
- Tint: white or light_blue depending on context

## Popups / Dialogs
- Background: dark_grey_variant
- Border: light_blue (level up), red (warnings)
- Title: Orbitron Bold
- Body: Inter Regular

## Status Bar
- Background: dark_grey (#393944)
- Light icons (white)

## Reference
See designs/ folder for wireframe screenshots of every screen.