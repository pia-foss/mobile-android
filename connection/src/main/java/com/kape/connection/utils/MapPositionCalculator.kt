package com.kape.connection.utils

import com.kape.region_selection.server.Server
import kotlin.math.ln
import kotlin.math.tan

class MapPositionCalculator {

    // Top and bottom latitudes of map graphic
    private val topLat = 83.65
    private val bottomLat = -56.00

    // Left and right longitudes of map graphic
    private val leftLong = -168.12
    private val rightLong = -169.65

    private val locations: Map<String, Pair<Double, Double>> = mapOf(
        "UAE" to Pair(23.424076, 53.847818),
        "Albania" to Pair(41.33165, 19.8318),
        "Argentina" to Pair(-38.416096, -63.616673),
        "AU Sydney" to Pair(-33.868820, 151.209296),
        "AU Melbourne" to Pair(-37.813628, 144.963058),
        "AU Perth" to Pair(-31.950527, 115.860458),
        "Austria" to Pair(47.516231, 14.550072),
        "Bosnia and Herzegovina" to Pair(43.858181, 18.412340),
        "Belgium" to Pair(50.503887, 4.469936),
        "Bulgaria" to Pair(42.655033, 25.231817),
        "Brazil" to Pair(-14.235004, -51.92528),
        "CA Montreal" to Pair(45.501689, -73.567256),
        "CA Ontario" to Pair(51.253777, -85.232212),
        "CA Toronto" to Pair(43.653226, -79.383184),
        "CA Vancouver" to Pair(49.282729, -123.120738),
        "Czech Republic" to Pair(50.075538, 14.4378),
        "DE Berlin" to Pair(52.520007, 13.404954),
        "Denmark" to Pair(56.263920, 9.501785),
        "Estonia" to Pair(59.436962, 24.753574),
        "Finland" to Pair(61.924110, 25.748151),
        "France" to Pair(46.227638, 2.213749),
        "DE Frankfurt" to Pair(50.110922, 8.682127),
        "Greece" to Pair(37.983810, 23.727539),
        "Hong Kong" to Pair(22.396428, 114.109497),
        "Croatia" to Pair(45.815399, 15.966568),
        "Hungary" to Pair(47.162494, 19.503304),
        "India" to Pair(20.593684, 78.96288),
        "Ireland" to Pair(53.142367, -7.692054),
        "Iceland" to Pair(64.852829, -18.301501),
        "Israel" to Pair(31.046051, 34.851612),
        "Italy" to Pair(41.871940, 12.56738),
        "Japan" to Pair(36.204824, 138.252924),
        "Lithuania" to Pair(54.687157, 25.279652),
        "Luxembourg" to Pair(49.815273, 6.129583),
        "Latvia" to Pair(56.946285, 24.105078),
        "Moldova" to Pair(47.265819, 28.598334),
        "Mexico" to Pair(23.634501, -102.552784),
        "North Macedonia" to Pair(41.608635, 21.745275),
        "Malaysia" to Pair(3.140853, 101.693207),
        "Netherlands" to Pair(52.132633, 5.291266),
        "Norway" to Pair(60.472024, 8.468946),
        "New Zealand" to Pair(-40.900557, 174.885971),
        "Poland" to Pair(51.919438, 19.145136),
        "Portugal" to Pair(38.736946, -9.142685),
        "Romania" to Pair(45.943161, 24.96676),
        "Serbia" to Pair(44.016421, 21.005859),
        "Singapore" to Pair(1.352083, 103.819836),
        "Slovenia" to Pair(46.075219, 14.882733),
        "Slovakia" to Pair(48.148598, 17.107748),
        "Spain" to Pair(40.463667, -3.74922),
        "Sweden" to Pair(60.128161, 18.643501),
        "Switzerland" to Pair(46.818188, 8.227512),
        "Turkey" to Pair(38.963745, 35.243322),
        "Ukraine" to Pair(48.379433, 31.165581),
        "UK London" to Pair(51.507351, -0.127758),
        "UK Manchester" to Pair(53.480759, -2.242631),
        "UK Southampton" to Pair(50.909700, -1.404351),
        "US East" to Pair(36.414652, -77.739258),
        "US West" to Pair(40.607697, -120.805664),
        "US Atlanta" to Pair(33.748995, -84.387982),
        "US California" to Pair(36.778261, -119.417932),
        "US Chicago" to Pair(41.878114, -87.629798),
        "US Denver" to Pair(39.739236, -104.990251),
        "US Florida" to Pair(27.664827, -81.515754),
        "US Houston" to Pair(29.760427, -95.369803),
        "US Las Vegas" to Pair(36.169941, -115.13983),
        "US New York City" to Pair(40.712775, -74.005973),
        "US Seattle" to Pair(47.606209, -122.332071),
        "US Silicon Valley" to Pair(37.593392, -122.04383),
        "US Texas" to Pair(33.623962, -109.654814),
        "US Washington DC" to Pair(38.907192, -77.036871),
        "South Africa" to Pair(-30.559482, 22.937506)
    )

    private val defaultCoordinate = Pair(40.463667, -3.74922)

    data class PointPadding(val start: Float, val top: Float, val end: Float, val bottom: Float)

    data class Coordinates(val latitude: Double, val longitude: Double)

    fun getCurrentLocationPadding(
        server: Server,
        widthInPx: Float,
        heightInPx: Float,
        diameterInPx: Float
    ): PointPadding {
        val current = locations[server.name]
        val coordinates = Coordinates(
            current?.first ?: defaultCoordinate.first,
            current?.second ?: defaultCoordinate.second
        )
        return PointPadding(
            (getLocationX(coordinates.longitude, widthInPx) - diameterInPx / 2).toFloat(),
            (getLocationY(coordinates.latitude, heightInPx) - diameterInPx / 2).toFloat(),
            (getLocationX(coordinates.longitude, widthInPx) + diameterInPx / 2).toFloat(),
            (getLocationY(coordinates.latitude, heightInPx) + diameterInPx / 2).toFloat()
        )
    }

    private fun getLocationX(longitude: Double?, widthInPx: Float): Double {
        if (longitude == null) {
            return -1.0
        }

        // Longitude -> range [-180, 180]
        var x: Double = longitude
        // Adjust for actual left edge of graphic -> range [-168, 192]
        if (x < leftLong) {
            x += 360.0
        }
        // Map to [0, width]
        val mapWidth: Double = widthInPx.toDouble()
        val a = x - leftLong
        val b = rightLong - leftLong
        return a / (b + 360.0) * mapWidth
    }

    private fun getLocationY(latitude: Double?, heightInPx: Float): Double {
        if (latitude == null) {
            return -1.0
        }

        // Project the latitude -> range [-2.3034..., 2.3034...]
        val millerLat: Double = millerProjectLat(latitude)

        // Map to the actual range shown by the map.  (If this point is outside
        // the map bound, it returns a negative value or a value greater than
        // height.)
        // Map to unit range -> [0, 1], where 0 is the bottom and 1 is the top
        val unitY: Double = (millerLat - bottomMiller()) / (topMiller() - bottomMiller())

        // Flip and scale to height
        return (1.0 - unitY) * heightInPx.toDouble()
    }

    // Top and bottom, Miller-projected
    private fun topMiller(): Double {
        return millerProjectLat(topLat)
    }

    private fun bottomMiller(): Double {
        return millerProjectLat(bottomLat)
    }

    private fun degToRad(degrees: Double): Double {
        return degrees * Math.PI / 180.0
    }

    // Project latitude.  The map uses this projection:
    // https://en.wikipedia.org/wiki/Miller_cylindrical_projection
    private fun millerProjectLat(latitudeDeg: Double): Double {
        return 1.25 * log2(tan(Math.PI * 0.25 + 0.4 * degToRad(latitudeDeg)))
    }

    private fun log2(`val`: Double): Double {
        return ln(`val`) / ln(2.0)
    }
}