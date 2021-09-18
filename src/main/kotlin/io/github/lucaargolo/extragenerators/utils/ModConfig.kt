package io.github.lucaargolo.extragenerators.utils

class ModConfig {

    class Generator(
        val output: Long = 0L,
        val storage: Long = 0L
    )

    //Tier 1 Generators
    val thermoelectricGenerator = Generator(64L, 32000L)
    val burnableGenerator = Generator(64L, 32000L)
    val icyGenerator = Generator(64L, 32000L)
    val colorfulGenerator = Generator(64L, 32000L)

    //Tier 2 Generators
    val sludgyGenerator = Generator(128L, 64000L)
    val teleportGenerator = Generator(128L, 64000L)
    val scaldingGenerator = Generator(128L, 64000L)
    val steamGenerator = Generator(128L, 64000L)

    //Tier 3 Generators
    val gluttonyGenerator = Generator(256L, 128000L)
    val brewGenerator = Generator(256L, 128000L)
    val redstoneGenerator = Generator(256L, 128000L)
    val blastGenerator = Generator(256L, 128000L)

    //Tier 4 Generators
    val enchantedGenerator = Generator(512L, 256000L)
    val demiseGenerator = Generator(512L, 256000L)

    //Tier 5 Generators
    val dragonGenerator = Generator(4096L, 2048000L)
    val witheredGenerator = Generator(4096L, 2048000L)

    //Tier ∞ Generators
    val heavenlyGenerator = Generator(204800L, 102400000L)
    val infernalGenerator = Generator(204800L, 102400000L)

}