package io.github.lucaargolo.extragenerators.utils

import team.reborn.energy.EnergyTier

class ModConfig {

    class Generator(
        val energyTier: String = "micro",
        val maxStoredPower: Double = 0.0
    ) {
        constructor(energyTier: EnergyTier, maxStoredPower: Double): this(energyTier.name.toLowerCase(), maxStoredPower)

        fun getEnumTier(): EnergyTier = EnergyTier.valueOf(energyTier)
    }

    val furnaceGenerator = Generator(EnergyTier.LOW, 128000.0)
    val culinaryGenerator = Generator(EnergyTier.MEDIUM, 128000.0)
    val frostyGenerator = Generator(EnergyTier.LOW, 128000.0)


}