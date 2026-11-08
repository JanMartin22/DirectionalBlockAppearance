
<h1 align="center">DirectionalBlockAppearance</h1>

<p align="center">Make blocks look different depending on the side you view them from.</p>

---

DBA shows players **fake blocks** that only exist on their client. From the side you set, players see your chosen block. From any other side, they see the real block. Great for one-way windows, spectator boxes and walls that look different from each side. No client mods or resource packs needed.
## Installation

Requires Spigot or Paper **1.21+** and **Java 21**.

Put the jar into your `plugins` folder and restart. In a BungeeCord or Velocity network, install DBA on the **backend servers**, not on the proxy.

## Quick start

```
/dba group create windows
/dba wand                      (left click = pos1, right click = pos2)
/dba set windows glass         (stand on the side that should see the glass)
```

Walk to the other side and you see the real block. Made a mistake? `/dba undo`.

## Commands

| Command | Description | Permission |
|---|---|---|
| `/dba group create <name>` | Create a group | `dba.command.group.create` |
| `/dba group delete <name>` | Delete a group and its blocks | `dba.command.group.delete` |
| `/dba group list` | List all groups | `dba.command.group.list` |
| `/dba pos1` / `/dba pos2` | Set a selection corner | `dba.command.pos` |
| `/dba wand` | Get the selection wand | `dba.command.wand` |
| `/dba set <group> <block> [side]` | Set the fake block for your whole selection | `dba.command.set` |
| `/dba quickset <group> <block>` | Set the fake block on the side you are looking at | `dba.command.quickset` |
| `/dba clear` | Remove all DBA blocks in your selection | `dba.command.clear` |
| `/dba undo [amount]` | Undo your last changes | `dba.command.undo` |

**Other permissions:**

- `dba.admin` gives all permissions (default: op).
- `dba.break` lets a player break protected DBA blocks.

## Configuration

In `config.yml` you can change the view distance, the selection limits, the number of undo steps, the wand item and all messages. You can also turn off each protection (breaking, placing, pistons, explosions, fire, liquids, entities, nature).

## Compatibility

Tested on Spigot 1.21.1, 1.21.11, 26.3 and Paper 1.21, 26.3. **Folia is not supported.**

## Known issues

**Fixed in the next update:**
- Blocks that need support, like bamboo, can leave a ghost block behind.
- The "protected" message appears with a delay when mining a DBA block.

**By design / limitations:**
- Mining time and drops are those of the real block.
- When a protected placement or piston is blocked, the real block may flash briefly.
