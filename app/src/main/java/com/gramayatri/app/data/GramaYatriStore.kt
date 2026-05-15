package com.gramayatri.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException

object GramaYatriStore {
    private const val PREFS = "grama_yatri_store"
    private const val DATA_KEY = "backend_json"
    private const val PASSENGER_KEY = "passenger_name"

    fun passengerName(context: Context): String = prefs(context).getString(PASSENGER_KEY, "Ravi") ?: "Ravi"

    fun savePassengerName(context: Context, name: String) {
        prefs(context).edit().putString(PASSENGER_KEY, name.ifBlank { "Passenger" }).apply()
    }

    fun routes(context: Context): List<BusRoute> {
        val items = data(context).getJSONArray("routes")
        return (0 until items.length()).map { parseRoute(items.getJSONObject(it)) }
    }

    fun route(context: Context, routeId: String): BusRoute? = routes(context).firstOrNull { it.id == routeId }

    fun pings(context: Context, routeId: String? = null): List<BusPing> {
        val items = data(context).getJSONArray("bus_pings")
        return (0 until items.length())
            .map { parsePing(items.getJSONObject(it)) }
            .filter { routeId == null || it.routeId == routeId }
            .sortedByDescending { it.createdAt }
    }

    fun alerts(context: Context, routeId: String? = null): List<RouteAlert> {
        val items = data(context).getJSONArray("notifications")
        return (0 until items.length())
            .map { parseAlert(items.getJSONObject(it)) }
            .filter { routeId == null || it.routeId == routeId }
            .sortedByDescending { it.createdAt }
    }

    fun latestPing(context: Context, routeId: String): BusPing? = pings(context, routeId).firstOrNull()

    fun addRoute(context: Context, name: String, busNumber: String, stops: List<String>) {
        val cleanStops = stops.map { it.trim() }.filter { it.isNotEmpty() }
        if (name.isBlank() || busNumber.isBlank() || cleanStops.size < 2) return

        val root = data(context)
        val routeId = "route_${System.currentTimeMillis()}"
        val stopArray = JSONArray()
        cleanStops.forEachIndexed { index, stop ->
            stopArray.put(JSONObject().apply {
                put("name", stop)
                put("averageMinutesFromPrevious", if (index == 0) 0 else 8 + (index % 3) * 3)
            })
        }
        root.getJSONArray("routes").put(JSONObject().apply {
            put("id", routeId)
            put("name", name.trim())
            put("busNumber", busNumber.trim())
            put("villageFrom", cleanStops.first())
            put("villageTo", cleanStops.last())
            put("cancelled", false)
            put("updatedAt", System.currentTimeMillis())
            put("stops", stopArray)
        })
        save(context, root)
    }

    fun addPing(context: Context, routeId: String, stopIndex: Int, type: String, reporterName: String) {
        val root = data(context)
        val route = route(context, routeId) ?: return
        val safeIndex = stopIndex.coerceIn(route.stops.indices)
        root.getJSONArray("bus_pings").put(JSONObject().apply {
            put("id", "ping_${System.currentTimeMillis()}")
            put("routeId", route.id)
            put("stopIndex", safeIndex)
            put("stopName", route.stops[safeIndex].name)
            put("type", type)
            put("reporterName", reporterName.ifBlank { "Passenger" })
            put("createdAt", System.currentTimeMillis())
        })
        setCancelled(root, routeId, false)
        save(context, root)
    }

    fun addAlert(context: Context, routeId: String, title: String, message: String, type: String, reporterName: String) {
        val root = data(context)
        val route = route(context, routeId) ?: return
        root.getJSONArray("notifications").put(JSONObject().apply {
            put("id", "notice_${System.currentTimeMillis()}")
            put("routeId", route.id)
            put("routeName", route.name)
            put("title", title)
            put("message", message)
            put("type", type)
            put("reporterName", reporterName.ifBlank { "Passenger" })
            put("createdAt", System.currentTimeMillis())
        })
        if (type == "CANCELLED") setCancelled(root, routeId, true)
        save(context, root)
    }

    fun etaMinutes(route: BusRoute, latestStopIndex: Int): Int = route.stops
        .drop(latestStopIndex + 1)
        .sumOf { it.averageMinutesFromPrevious }

    fun resetDemoData(context: Context) {
        prefs(context).edit().remove(DATA_KEY).apply()
    }

    private fun data(context: Context): JSONObject {
        val assetRaw = context.assets.open("grama_yatri_backend.json").bufferedReader().use { it.readText() }
        val raw = prefs(context).getString(DATA_KEY, null) ?: assetRaw
        return try {
            JSONObject(raw)
        } catch (_: JSONException) {
            prefs(context).edit().remove(DATA_KEY).apply()
            JSONObject(assetRaw)
        }
    }

    private fun save(context: Context, root: JSONObject) {
        prefs(context).edit().putString(DATA_KEY, root.toString()).apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun setCancelled(root: JSONObject, routeId: String, cancelled: Boolean) {
        val routes = root.getJSONArray("routes")
        for (index in 0 until routes.length()) {
            val route = routes.getJSONObject(index)
            if (route.getString("id") == routeId) {
                route.put("cancelled", cancelled)
                route.put("updatedAt", System.currentTimeMillis())
                return
            }
        }
    }

    private fun parseRoute(item: JSONObject): BusRoute {
        val stops = item.getJSONArray("stops")
        return BusRoute(
            id = item.getString("id"),
            name = item.getString("name"),
            busNumber = item.optString("busNumber", "Village Bus"),
            villageFrom = item.getString("villageFrom"),
            villageTo = item.getString("villageTo"),
            cancelled = item.optBoolean("cancelled", false),
            stops = (0 until stops.length()).map { index ->
                val stop = stops.getJSONObject(index)
                RouteStop(stop.getString("name"), stop.getInt("averageMinutesFromPrevious"))
            }
        )
    }

    private fun parsePing(item: JSONObject) = BusPing(
        id = item.getString("id"),
        routeId = item.getString("routeId"),
        stopIndex = item.getInt("stopIndex"),
        stopName = item.getString("stopName"),
        type = item.getString("type"),
        reporterName = item.getString("reporterName"),
        createdAt = item.getLong("createdAt")
    )

    private fun parseAlert(item: JSONObject) = RouteAlert(
        id = item.getString("id"),
        routeId = item.getString("routeId"),
        routeName = item.getString("routeName"),
        title = item.getString("title"),
        message = item.getString("message"),
        type = item.getString("type"),
        reporterName = item.getString("reporterName"),
        createdAt = item.getLong("createdAt")
    )
}

data class BusRoute(
    val id: String,
    val name: String,
    val busNumber: String,
    val villageFrom: String,
    val villageTo: String,
    val cancelled: Boolean,
    val stops: List<RouteStop>
)

data class RouteStop(val name: String, val averageMinutesFromPrevious: Int)

data class BusPing(
    val id: String,
    val routeId: String,
    val stopIndex: Int,
    val stopName: String,
    val type: String,
    val reporterName: String,
    val createdAt: Long
)

data class RouteAlert(
    val id: String,
    val routeId: String,
    val routeName: String,
    val title: String,
    val message: String,
    val type: String,
    val reporterName: String,
    val createdAt: Long
)