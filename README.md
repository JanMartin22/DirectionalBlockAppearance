# DirectionalBlockAppearance
Work in progress 

Make blocks look different depending on the side they're viewed from.
Perfect for one-way glass, secret doors and creative builds.

## How it works

Each player gets their own client-side "fake" blocks. The plugin checks which side of a block a player is standing on and shows them the matching block. The real block on the server stays untouched.

Example: a window that looks like **glass from the inside** and like **stone from the outside**.

No resource pack, no client mod required.

## Requirements

- Spigot or Paper **1.21+**
- Java 21+

## Commands

| Command | Description | Permission |
|---|---|---|
| `/dba wand` | Gives you the selection wand (left click: pos1, right click: pos2) | `dba.admin` |
| `/dba pos1` / `/dba pos2` | Sets a selection corner to the block you're looking at | `dba.admin` |
| `/dba set <group> <block> [side]` | Sets one side of all blocks in your selection. Without a side, the side facing you is used | `dba.admin` |
| `/dba quickset <group> <block>` | Sets the side you're looking at, only for that one block | `dba.admin` |
| `/dba clear` | Removes all DBA blocks in your selection | `dba.admin` |
| `/dba undo [amount]` | Undoes your last changes (up to 15) | `dba.admin` |
| `/dba group create <name>` | Creates a new group | `dba.admin` |
| `/dba group delete <name>` | Deletes a group and all its blocks (cannot be undone) | `dba.admin` |
| `/dba group list` | Lists all groups with their block count | `dba.admin` |

Blocks that already belong to another group are skipped by `set` and `quickset`.

## License

See [LICENSE](LICENSE).
