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
| `/dba test` | Turns the block you're looking at into a test block (glass facing you) | `dba.admin` |

## License

See [LICENSE](LICENSE).
