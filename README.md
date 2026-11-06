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
| `/dba wand` | Gives you the selection wand (left click: pos1, right click: pos2) | `dba.command.wand` |
| `/dba pos1` / `/dba pos2` | Sets a selection corner to the block you're looking at | `dba.command.pos` |
| `/dba set <group> <block> [side]` | Sets one side of all blocks in your selection. Without a side, the side facing you is used | `dba.command.set` |
| `/dba quickset <group> <block>` | Sets the side you're looking at, only for that one block | `dba.command.quickset` |
| `/dba clear` | Removes all DBA blocks in your selection | `dba.command.clear` |
| `/dba undo [amount]` | Undoes your last changes (up to 15) | `dba.command.undo` |
| `/dba group create <name>` | Creates a new group | `dba.command.group.create` |
| `/dba group delete <name>` | Deletes a group and all its blocks (cannot be undone) | `dba.command.group.delete` |
| `/dba group list` | Lists all groups with their block count | `dba.command.group.list` |

Blocks that already belong to another group are skipped by `set` and `quickset`.

## Permissions

| Permission | Allows | Default |
|---|---|---|
| `dba.admin` | Everything below | op |
| `dba.command.<subcommand>` | The matching command (see table above) | op |
| `dba.command.pos` | Also selecting with the wand | op |
| `dba.break` | Breaking DBA blocks | op |

Without `dba.break`, DBA blocks are protected from players. They are always protected from pistons, explosions, fire spreading and mobs.

## License

See [LICENSE](LICENSE).
