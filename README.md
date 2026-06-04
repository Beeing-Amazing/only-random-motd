# only random motd
simple server list MOTD mod for Minecraft servers

- Simple color and formatting using [color codes](https://minecraft.wiki/w/Formatting_codes)
- Unicode "Small Caps" font support using custom symbol: `&^`
- Randomly choose a message from a list! by default! always! or only, really!
- Requires [Fabric API](https://modrinth.com/mod/fabric-api)

## config
Simple configuration using a YAML file:
```yml
do_line1_override: true
do_line2_override: false
enabled: true
motds:
  - "A Minecraft Server\n&dHere is another line"
  - "\n&^here be &ddragons"
override_line1: 'A Minecraft Server'
override_line2: ''
```

## roadmap
- Centering
