# master_crafter_reward.mcfunction
# Rewards for unlocking all custom recipes

# Title message
title @s title {"text":"🎉 MASTER CRAFTER 🎉","color":"gold","bold":true}
title @s subtitle {"text":"You've unlocked all custom recipes!","color":"green"}

# Chat message
tellraw @s {"text":"\\n","extra":[{"text":"═══════════════════════════════════════\\n","color":"gold","bold":true},{"text":" CONGRATULATIONS! \\n","color":"yellow","bold":true},{"text":"You have mastered all custom crafting recipes!\\n","color":"white"},{"text":"═══════════════════════════════════════","color":"gold","bold":true}]}

# Reward items
tellraw @s {"text":"Rewards:","color":"green"}
tellraw @s {"text":" • 64 Experience Bottles","color":"white"}
tellraw @s {"text":" • 32 Gold Ingots","color":"white"}
tellraw @s {"text":" • Custom Title: Master Crafter","color":"white"}

# Give items
give @s experience_bottle 64
give @s gold_ingot 32

# Sound effects
playsound entity.player.levelup player @s ~ ~ ~ 1 1
playsound ui.toast.challenge_complete player @s ~ ~ ~ 1 1

# Particle effects
execute at @s run particle minecraft:firework ~ ~1 ~ 0 0 0 0.1 100
execute at @s run particle minecraft:totem_of_undying ~ ~1 ~ 0.5 0.5 0.5 0.5 50