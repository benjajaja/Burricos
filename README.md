Burricos
========

![Donkey](http://images.wikia.com/adventuretimewithfinnandjake/images/9/95/Donkey.png)

Expand donkey inventories to the capability of a double chest, 54 slots instead of 15, without external storage

## Usage

* Fabricate a chest item with `Donkey double chest` as lore, for example with [FactoryMod](https://github.com/Civcraft/FactoryMod) "Animal husbandry" factory and "Donky double chest" recipe.
* Click on tamed, unchested donkey
* Open donkey, should now have 54 inventory slots instead of the regular 15
* You can saddle it by placing a saddle in the first slot of inventory

## Features

* Does not use any kind of external storage, only NMS methods and NBTs
* Donkeys drop loot normally (except the lored chest)

## Internals

The donkey's 17 slot inventory is replaced by a 54 slot inventory. When a donkey-entity is saved to disk, the inventory is "zipped" into the NBT of one chest item, inventory is restablished to regular with only the "zip" item. When the entity is loaded into memory again, the single item is "unzipped" into a 54 slot inventory again.

It is necessary to also have a plugin that kicks players off vehicles (at least donkeys) on logout, such as Humbug. Expanded donkey inventories may be lost beyond slot 17 if the server crashes without kicking players properly and a player was mounting the donkey.
