# Phone Preview Must Match Phone

The desktop phone preview should look exactly like the real phone version.

Because the desktop browser triggers Tailwind viewport breakpoints, avoid `sm:`, `md:`, `lg:`, and larger responsive classes inside the app content when the goal is phone preview parity. Keep breakpoint-specific classes only for the outer desktop preview frame itself.
