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

| Command | Description | Permission |
|---|---|---|
| `/dba group create <name>` | Creates a new group | `dba.admin` |
| `/dba group delete <name>` | Deletes a group and all its blocks | `dba.admin` |
| `/dba group list` | Lists all groups with their block count | `dba.admin` |
| `/dba test` | Turns the block you're looking at into a test block (glass facing you) | `dba.admin` |

### Planned

| Command | Description |
|---|---|
| `/dba set <group> <block>` | Sets the side you're looking at to a block |
| `/dba fill <group> <side> <block>` | Sets one side of all blocks in your selection |
| `/dba remove` | Removes the DBA block you're looking at |
| `/dba clear` | Removes all DBA blocks in your selection |
| `/dba pos1` / `/dba pos2` | Sets selection corners (or use the wand: left/right click) |
| `/dba wand` | Gives you the selection wand |
## License

See [LICENSE](LICENSE).
