# Project Memory Protocol

When the user corrects the assistant or asks to remember something about this project, save it as its own `.md` file inside the root `memory/` folder.

Use these filename prefixes:

- `user_` for how the user personally works.
- `project_` for project-specific memory.
- `feedback_` for corrections to assistant behavior.
- `reference_` for links, facts, or external context.

Keep `memory/MEMORY.md` updated as a one-line index of every memory rule.

Maintain:

- `memory/lessons.md` for strategic lessons and repeated correction patterns.
- `tasks/todo.md` for active sprint planning and completion tracking.

At the start of each new session, read `memory/MEMORY.md`, `memory/lessons.md`, and `tasks/todo.md` before continuing.
