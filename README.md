# Passive Mobs

This mod allows players to set their own aggression levels for mobs.

* Normal - Normal mob interaction with players.
* Passive - Players are ignored by mobs until they attack one.
* Peaceful - Players are completely ignored by mobs and cannot be damaged by
  them.

## Configuration

**passivemobs-common.toml**

| Name                   | Default | Values                    | Description                                                                                                                                 |                                        
|------------------------|---------|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| defaultAggressionLevel | passive | normal, passive, peaceful | Sets the default aggression level of a player when they join the server.                                                                    |
| deaggroTicks           | 1000    |                           | Sets the number of player ticks a player will remain aggressive to mobs after attacking one. Does not affect the "normal" aggression level. |                                                  |

## Commands

* `/aggression` - Returns your current aggression level
* `/aggression LEVEL` - Sets your aggression level.
