# High-End Cinema Design System: Editorial & Experience Guidelines

## 1. Overview & Creative North Star
The Creative North Star for this design system is **"The Digital Curator."** 

This system is not merely a utility for playing video; it is an editorial platform that treats film as art. We move away from the "template" feel of traditional streaming services by embracing a cinematic, high-contrast aesthetic. The design breaks the rigid, mechanical grid through intentional asymmetry—such as overlapping high-quality movie posters (XL radius) over immersive hero backgrounds—and an expansive typography scale that emphasizes the "Director’s Cut" feel of the interface.

**Key Principles:**
*   **Immersive Depth:** UI elements should feel like they are floating in front of the screen, never "pasted" on top.
*   **Atmospheric Tension:** Use high-contrast color shifts and tonal layering to guide the eye without the need for traditional structural lines.
*   **Curated Whitespace:** Use generous breathing room to separate metadata from the primary narrative content.

---

## 2. Colors
Our palette is rooted in the deep, ink-like tones of a darkened theater, punctuated by the "Radiant Gold" of a spotlight.

### Palette Strategy
*   **Primary Background (`#0b1326`):** A deep, midnight blue that provides more soul and depth than pure black.
*   **Radiant Accents (`primary: #ffc639`):** Reserved for high-priority calls to action (CTAs) and critical focus states.
*   **Neutral Softness (`on-surface: #dae2fd`):** A muted, cool-white for body text to reduce eye strain while maintaining a premium feel.

### The "No-Line" Rule
**Explicit Instruction:** Do not use 1px solid borders to section content. Visual boundaries must be defined solely through background color shifts. For example, a "Similar Movies" section (using `surface-container-low`) should sit directly on the `background` without a divider. Let the change in tonal value define the edge.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers. 
*   **Base:** `surface` / `background`
*   **Low-Level Sections:** `surface-container-low`
*   **Interactive Cards/Modules:** `surface-container-high` or `highest`
This nesting creates natural "depth" that mimics fine physical paper or stacked lens filters.

### Signature Textures & Glass
*   **The Glass Rule:** For floating headers or metadata overlays, use `surface-variant` with a **20px-40px backdrop-blur**. This allows the cinematic colors of the movie poster to bleed through the UI, integrating the content with the interface.
*   **CTA Gradients:** For primary buttons, transition from `primary` (#ffc639) to `primary-container` (#e1aa12) at a 135-degree angle to provide a subtle metallic sheen.

---

## 3. Typography
We utilize a pairing of **Epilogue** (Display/Headlines) and **Manrope** (Body/Labels) to create an editorial, high-fashion atmosphere.

*   **Display (Epilogue):** Large, bold, and authoritative. Used for movie titles (e.g., `display-lg` at 3.5rem). The wide apertures of Epilogue feel modern and cinematic.
*   **Headline (Epilogue):** Used for section headers like "Cast & Crew" or "Overview." These should use `headline-sm` to `md` to maintain clear hierarchy.
*   **Body & Title (Manrope):** A highly legible sans-serif. Use `body-lg` (1rem) for descriptions to ensure readability against dark backgrounds.
*   **Labels:** Use `label-sm` (0.6875rem) in all-caps with increased letter spacing (0.05em) for metadata like "YEAR," "DURATION," or "RATING" to evoke a technical, behind-the-scenes aesthetic.

---

## 4. Elevation & Depth
Depth is achieved through **Tonal Layering** and **Ambient Light**, not structural shadows.

*   **The Layering Principle:** Place a `surface-container-lowest` card on a `surface-container-low` section. This creates a soft, natural "lift" through color alone.
*   **Ambient Shadows:** For "floating" elements like the main movie poster, use extra-diffused shadows. 
    *   *Values:* 0px 20px 50px rgba(0, 0, 0, 0.4).
    *   *Color:* Always tint shadows with a hint of the background blue to avoid a "muddy" look.
*   **The "Ghost Border" Fallback:** If a border is required for accessibility, use `outline-variant` at **15% opacity**. It should be felt, not seen.
*   **Glassmorphism:** Navigation bars and detail overlays should use semi-transparent `surface` tokens with a heavy blur. This prevents the UI from feeling like a "box" sitting on a photo.

---

## 5. Components

### Buttons
*   **Primary:** Solid `primary` gradient, `round: full`. Typography: `title-sm` (Manrope), `on-primary` color.
*   **Secondary/Ghost:** `outline-variant` (100% opacity) border, transparent background. For "Add to My List" actions.
*   **Interaction:** On hover, primary buttons should scale 2% (1.02x) and increase shadow diffusion.

### Cards & Lists
*   **Movie Posters:** Use `radius-xl` (1.5rem). No borders. 
*   **Cast Avatars:** Circular (`radius: full`) with a `primary` or `outline` border (1px or 2px) to signify importance.
*   **Vertical Spacing:** Forbid divider lines. Use the Spacing Scale (32px or 48px) to separate the "Overview" from "Cast & Crew."

### Input Fields
*   **Search/Text Input:** Use `surface-container-highest` with a `radius-md`. Label should be `label-md` floating above the container.
*   **States:** Focus state should swap the subtle `outline-variant` for a `primary` 1px border.

### Chips (Genre/Tags)
*   **Style:** `surface-container-high` background, `radius-full`, `body-sm` typography. These should feel like small, tactile pills.

---

## 6. Do's and Don'ts

### Do
*   **Do** use high-quality, high-contrast imagery that fills the background.
*   **Do** allow elements to overlap (e.g., the poster overlapping the hero image) to create a 3D sense of space.
*   **Do** use `on-surface-variant` (gold-tinted) for secondary text like "Director" or "Starring" to create tonal harmony with the primary gold.
*   **Do** ensure a minimum 4.5:1 contrast ratio for all body text against the dark background.

### Don't
*   **Don't** use 100% white (#FFFFFF) for text; it is too harsh. Use the `on-surface` (#dae2fd) token.
*   **Don't** use sharp 90-degree corners. Everything must have at least a `radius-sm` to maintain the premium, soft-touch feel.
*   **Don't** use standard "drop shadows" with 20%+ opacity. They break the cinematic immersion.
*   **Don't** use dividers or lines to separate list items; use vertical space and background shifts.