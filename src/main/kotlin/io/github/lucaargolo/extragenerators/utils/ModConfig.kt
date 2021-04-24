package io.github.lucaargolo.extragenerators.utils

class ModConfig {

    class Generator(
        val output: Double = 0.0,
        val storage: Double = 0.0
    )

    //Tier 1 Generators
    val thermoelectricGenerator = Generator(64.0, 32000.0)
    val burnableGenerator = Generator(64.0, 32000.0)
    val icyGenerator = Generator(64.0, 32000.0)
    val colorfulGenerator = Generator(64.0, 32000.0)

    //Tier 2 Generators
    val sludgyGenerator = Generator(128.0, 64000.0)
    val teleportGenerator = Generator(128.0, 64000.0)
    val scaldingGenerator = Generator(128.0, 64000.0)
    val steamGenerator = Generator(128.0, 64000.0)

    //Tier 3 Generators
    val gluttonyGenerator = Generator(256.0, 128000.0)
    val brewGenerator = Generator(256.0, 128000.0)
    val redstoneGenerator = Generator(256.0, 128000.0)
    val blastGenerator = Generator(256.0, 128000.0)

    //Tier 4 Generators
    val enchantedGenerator = Generator(512.0, 256000.0)
    val demiseGenerator = Generator(512.0, 256000.0)

    //Tier 5 Generators
    val dragonGenerator = Generator(4096.0, 2048000.0)
    val witheredGenerator = Generator(4096.0, 2048000.0)

}