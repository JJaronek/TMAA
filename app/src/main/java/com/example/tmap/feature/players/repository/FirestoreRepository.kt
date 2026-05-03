package com.example.tmap.feature.players.repository

import com.example.tmap.feature.players.data.remote.GlobalPlayerStat
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.DocumentChange
import com.example.tmap.core.notifications.TmapNotificationManager


class FirestoreRepository {
    // Získání instance databáze
    private val db = Firebase.firestore

    // Odkaz na konkrétní kolekci dokumentů
    private val leaderboardCollection = db.collection("global_leaderboard")

    suspend fun uploadPlayerStat(playerName: String, newAverage: Double) {
        try {
            // Nejdřív zkusíme stáhnout aktuální stav hráče
            val docRef = leaderboardCollection.document(playerName)
            val snapshot = docRef.get().await()

            val currentStat = snapshot.toObject(GlobalPlayerStat::class.java)

            val updatedWins = (currentStat?.wins ?: 0) + 1
            val updatedAverage = if (currentStat != null && currentStat.bestAverage > newAverage) {
                currentStat.bestAverage
            } else {
                newAverage//pokud je nový avg lepší, uložíme ten lepší
            }
            val statToSave = GlobalPlayerStat(playerName, updatedWins, updatedAverage)

            docRef.set(statToSave).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getGlobalLeaderboard(): List<GlobalPlayerStat> {
        return try {
            // Stáhneme rovnou seřazené podle výher
            val snapshot = leaderboardCollection
                .orderBy("wins", Query.Direction.DESCENDING)
                .get()
                .await()

            // Firebase umí snapshot převést přímo na naše objekty
            snapshot.toObjects(GlobalPlayerStat::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun listenForLeaderboardChanges(notificationManager: TmapNotificationManager) {
        // Tento listener se přilepí na databázi a zavolá se při každé změně
        leaderboardCollection.addSnapshotListener { snapshots, error ->
            if (error != null || snapshots == null) {
                return@addSnapshotListener
            }

            // Projdeme všechny změny, které právě nastaly
            for (dc in snapshots.documentChanges) {
                // Zajímá nás jen situace, kdy se nějaký dokument UPRAVÍ (někdo přidá výhru)
                if (dc.type == DocumentChange.Type.MODIFIED) {
                    val playerName = dc.document.id
                    notificationManager.showLeaderboardUpdateNotification("Hráč $playerName právě vylepšil své skóre!")
                }
            }
        }
    }
}