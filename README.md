Burricos
========

![Donkey](http://images.wikia.com/adventuretimewithfinnandjake/images/9/95/Donkey.png)

Expand donkey inventories to the capability of a double chest, 54 slots instead of 15, without external storage. One slot is reserved for the saddle so the effective number of slots is actually 53.

## Usage

* Fabricate a chest item with `Donkey double chest` as lore, for example with [FactoryMod](https://github.com/Civcraft/FactoryMod) "Animal husbandry" factory and "Donky double chest" recipe.
* Click on a tamed, unchested donkey
* Open donkey, should now have 54 inventory slots instead of the regular 15
* It will always have a saddle in it's first slot, which cannot be removed.

## Features

* Does not use any kind of external storage, only NMS methods and NBTs
* Donkeys drop loot normally (including lore'd chest)

## Internals

The donkey's 15 slot (+ 1 saddle + 1 armor) inventory is replaced by a 54 slot inventory. When a player interacts with the donley's inventory, it is "zipped" into the NBT of the donkey's saddle item. After being saved to disk on server stop, chunk unload or player logout, later when the entity is loaded into memory again, the single item is "unzipped" into a 54 slot inventory.
