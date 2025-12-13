# Passive Mobs

This mod allows players to set their own aggression levels for mobs. Allowing
different players to have their own experiences on the same server.

Want to play on hard difficulty but have your less skilled friends still play on
the same server? This mod is for you! You can play on hard while they don't have
to worry about mobs.

**NOTE** Hunger and damage effects from non-mob sources are still applied at the
server difficulty level.

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

* `/myaggression` - Returns your current aggression level.
* `/myaggression LEVEL` - Sets your aggression level.
* `/aggression PLAYER` (OP) - Returns the named player aggression level.
* `/aggression PLAYER LEVEL` (OP) - Sets the named players aggression level.
