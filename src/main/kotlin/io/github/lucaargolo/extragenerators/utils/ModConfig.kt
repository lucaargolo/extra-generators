package io.github.lucaargolo.extragenerators.utils

class ModConfig {

    class Generator(
        val output: Double = 0.0,
        val storage: Double = 0.0
    )

    val burnableGenerator = Generator(40.0, 100000.0)
    val gluttonyGenerator = Generator(64.0, 100000.0)
    val icyGenerator = Generator(40.0, 100000.0)
    val sludgyGenerator = Generator(400.0, 100000.0)
    val witheredGenerator = Generator(4000.0, 400000.0)
    val teleportGenerator = Generator(80.0, 100000.0)
    val dragonGenerator = Generator(40.0, 1000000.0)
    val demiseGenerator = Generator(40.0, 100000.0)
    val blastGenerator = Generator(160.0, 100000.0)
    val enchantedGenerator = Generator(40.0, 100000.0)
    val brewGenerator = Generator(40.0, 100000.0)
    val scaldingGenerator = Generator(40.0, 100000.0)


}