package com.example.calculator.model

data class ScientificConstant(
    val code: Int,
    val symbol: String,
    val name: String,
    val value: Double,
    val unit: String
)

object ConstantsCatalog {
    val ALL = listOf(
        ScientificConstant(1, "mp", "Proton mass", 1.67262192369e-27, "kg"),
        ScientificConstant(2, "mn", "Neutron mass", 1.67492749804e-27, "kg"),
        ScientificConstant(3, "me", "Electron mass", 9.1093837015e-31, "kg"),
        ScientificConstant(4, "mμ", "Muon mass", 1.883531627e-28, "kg"),
        ScientificConstant(5, "a0", "Bohr radius", 5.29177210903e-11, "m"),
        ScientificConstant(6, "h", "Planck constant", 6.62607015e-34, "J·s"),
        ScientificConstant(7, "μN", "Nuclear magneton", 5.0507837461e-27, "J/T"),
        ScientificConstant(8, "μB", "Bohr magneton", 9.2740100783e-24, "J/T"),
        ScientificConstant(9, "ħ", "Reduced Planck constant", 1.054571817e-34, "J·s"),
        ScientificConstant(10, "α", "Fine-structure constant", 7.2973525693e-3, ""),
        ScientificConstant(11, "re", "Classical electron radius", 2.8179403262e-15, "m"),
        ScientificConstant(12, "λc", "Compton wavelength", 2.42631023867e-12, "m"),
        ScientificConstant(13, "γp", "Proton gyromagnetic ratio", 2.6752218744e8, "s⁻¹·T⁻¹"),
        ScientificConstant(14, "λcp", "Proton Compton wavelength", 1.32140985539e-15, "m"),
        ScientificConstant(15, "λcn", "Neutron Compton wavelength", 1.31959090581e-15, "m"),
        ScientificConstant(16, "R∞", "Rydberg constant", 10973731.568160, "m⁻¹"),
        ScientificConstant(17, "u", "Atomic mass constant", 1.66053906660e-27, "kg"),
        ScientificConstant(18, "μp", "Proton magnetic moment", 1.41060679736e-26, "J/T"),
        ScientificConstant(19, "μe", "Electron magnetic moment", -9.2847647043e-24, "J/T"),
        ScientificConstant(20, "μn", "Neutron magnetic moment", -9.6623651e-27, "J/T"),
        ScientificConstant(21, "μm", "Muon magnetic moment", -4.49044830e-26, "J/T"),
        ScientificConstant(22, "F", "Faraday constant", 96485.33212, "C/mol"),
        ScientificConstant(23, "e", "Elementary charge", 1.602176634e-19, "C"),
        ScientificConstant(24, "NA", "Avogadro constant", 6.02214076e23, "mol⁻¹"),
        ScientificConstant(25, "k", "Boltzmann constant", 1.380649e-23, "J/K"),
        ScientificConstant(26, "Vm", "Molar volume of ideal gas", 0.02271095464, "m³/mol"),
        ScientificConstant(27, "R", "Molar gas constant", 8.314462618, "J/(mol·K)"),
        ScientificConstant(28, "c0", "Speed of light in vacuum", 299792458.0, "m/s"),
        ScientificConstant(29, "c1", "First radiation constant", 3.741771852e-16, "W·m²"),
        ScientificConstant(30, "c2", "Second radiation constant", 0.01438776877, "m·K"),
        ScientificConstant(31, "σ", "Stefan-Boltzmann constant", 5.670374419e-8, "W/(m²·K⁴)"),
        ScientificConstant(32, "ε0", "Vacuum electric permittivity", 8.8541878128e-12, "F/m"),
        ScientificConstant(33, "μ0", "Vacuum magnetic permeability", 1.25663706212e-6, "N/A²"),
        ScientificConstant(34, "Φ0", "Magnetic flux quantum", 2.067833848e-15, "Wb"),
        ScientificConstant(35, "g", "Standard acceleration of gravity", 9.80665, "m/s²"),
        ScientificConstant(36, "G0", "Conductance quantum", 7.748091729e-5, "S"),
        ScientificConstant(37, "Z0", "Characteristic impedance of vacuum", 376.730313668, "Ω"),
        ScientificConstant(38, "t", "Celsius temperature offset", 273.15, "K"),
        ScientificConstant(39, "G", "Newtonian constant of gravitation", 6.67430e-11, "m³/(kg·s²)"),
        ScientificConstant(40, "atm", "Standard atmosphere", 101325.0, "Pa")
    )
}

data class UnitConversion(
    val code: Int,
    val fromUnit: String,
    val toUnit: String,
    val category: String,
    val convert: (Double) -> Double
)

object ConversionsCatalog {
    val ALL = listOf(
        UnitConversion(1, "in", "cm", "Length") { it * 2.54 },
        UnitConversion(2, "cm", "in", "Length") { it / 2.54 },
        UnitConversion(3, "ft", "m", "Length") { it * 0.3048 },
        UnitConversion(4, "m", "ft", "Length") { it / 0.3048 },
        UnitConversion(5, "yd", "m", "Length") { it * 0.9144 },
        UnitConversion(6, "m", "yd", "Length") { it / 0.9144 },
        UnitConversion(7, "mile", "km", "Length") { it * 1.609344 },
        UnitConversion(8, "km", "mile", "Length") { it / 1.609344 },
        UnitConversion(9, "n mile", "m", "Length") { it * 1852.0 },
        UnitConversion(10, "m", "n mile", "Length") { it / 1852.0 },
        UnitConversion(11, "acre", "m²", "Area") { it * 4046.8564224 },
        UnitConversion(12, "m²", "acre", "Area") { it / 4046.8564224 },
        UnitConversion(13, "gal (US)", "L", "Volume") { it * 3.785411784 },
        UnitConversion(14, "L", "gal (US)", "Volume") { it / 3.785411784 },
        UnitConversion(15, "gal (UK)", "L", "Volume") { it * 4.54609 },
        UnitConversion(16, "L", "gal (UK)", "Volume") { it / 4.54609 },
        UnitConversion(17, "pc", "km", "Length") { it * 3.085677581e13 },
        UnitConversion(18, "km", "pc", "Length") { it / 3.085677581e13 },
        UnitConversion(19, "km/h", "m/s", "Velocity") { it / 3.6 },
        UnitConversion(20, "m/s", "km/h", "Velocity") { it * 3.6 },
        UnitConversion(21, "oz", "g", "Mass") { it * 28.349523125 },
        UnitConversion(22, "g", "oz", "Mass") { it / 28.349523125 },
        UnitConversion(23, "lb", "kg", "Mass") { it * 0.45359237 },
        UnitConversion(24, "kg", "lb", "Mass") { it / 0.45359237 },
        UnitConversion(25, "atm", "Pa", "Pressure") { it * 101325.0 },
        UnitConversion(26, "Pa", "atm", "Pressure") { it / 101325.0 },
        UnitConversion(27, "bar", "Pa", "Pressure") { it * 100000.0 },
        UnitConversion(28, "Pa", "bar", "Pressure") { it / 100000.0 },
        UnitConversion(29, "mmHg", "Pa", "Pressure") { it * 133.322387415 },
        UnitConversion(30, "Pa", "mmHg", "Pressure") { it / 133.322387415 },
        UnitConversion(31, "hp", "kW", "Power") { it * 0.74569987158227022 },
        UnitConversion(32, "kW", "hp", "Power") { it / 0.74569987158227022 },
        UnitConversion(33, "kgf/cm²", "Pa", "Pressure") { it * 98066.5 },
        UnitConversion(34, "Pa", "kgf/cm²", "Pressure") { it / 98066.5 },
        UnitConversion(35, "kgf·m", "J", "Energy") { it * 9.80665 },
        UnitConversion(36, "J", "kgf·m", "Energy") { it / 9.80665 },
        UnitConversion(37, "lbf/in² (psi)", "kPa", "Pressure") { it * 6.894757293 },
        UnitConversion(38, "kPa", "lbf/in²", "Pressure") { it / 6.894757293 },
        UnitConversion(39, "°F", "°C", "Temperature") { (it - 32.0) * 5.0 / 9.0 },
        UnitConversion(40, "°C", "°F", "Temperature") { it * 9.0 / 5.0 + 32.0 }
    )
}
