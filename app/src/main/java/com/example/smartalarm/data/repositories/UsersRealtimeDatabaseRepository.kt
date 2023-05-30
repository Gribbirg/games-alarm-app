package com.example.smartalarm.data.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.smartalarm.data.data.AccountData
import com.example.smartalarm.data.data.AlarmData
import com.example.smartalarm.data.data.RecordInternetData
import com.example.smartalarm.data.data.arrayToString
import com.example.smartalarm.data.data.getRecordsList
import com.example.smartalarm.data.db.GameData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.log

object UsersRealtimeDatabaseRepository {
    private val usersDatabase = FirebaseDatabase
        .getInstance("https://smartalarm-ccdbb-default-rtdb.europe-west1.firebasedatabase.app/")
        .getReference("users")

    private val topRecordsDatabase = FirebaseDatabase
        .getInstance("https://smartalarm-ccdbb-default-rtdb.europe-west1.firebasedatabase.app/")
        .getReference("top_records")

    suspend fun addUser(account: AccountData?) = withContext(Dispatchers.IO) {
        if (account != null) {
            usersDatabase.child(account.uid!!).get().addOnSuccessListener {
                Log.i("firebase", "Got value")
                if (!it.exists())
                    usersDatabase.child(account.uid!!).setValue(account)
            }
        }
    }

    suspend fun getUser(uid: String, user: MutableLiveData<AccountData>) =
        withContext(Dispatchers.IO) {
            usersDatabase.child(uid).get().addOnSuccessListener {
                user.postValue(it.getValue(AccountData::class.java))
            }
        }

    suspend fun getTopRecords(userList: MutableLiveData<List<AccountData>>) =
        withContext(Dispatchers.IO) {
            topRecordsDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.postValue(
                        snapshot.children.map { dataSnapshot ->
                            dataSnapshot.getValue(AccountData::class.java)!!
                        }
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("firebase", error.toString())
                }

            })
        }

    suspend fun getAllUsers(userList: MutableLiveData<List<AccountData>>) =
        withContext(Dispatchers.IO) {
            usersDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.postValue(
                        snapshot.children.map { dataSnapshot ->
                            dataSnapshot.getValue(AccountData::class.java)!!
                        }
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("firebase", error.toString())
                }

            })
        }

    suspend fun updateUserRecords(
        account: AccountData,
        recordInternetData: RecordInternetData,
        result: MutableLiveData<Boolean?>
    ) =
        withContext(Dispatchers.IO) {
            val recordUserDb = usersDatabase.child(account.uid!!)
            val topRecordDb = topRecordsDatabase.child(recordInternetData.gameId.toString())
            recordUserDb.get().addOnSuccessListener {
                val current = it.getValue(AccountData::class.java)

                if (current?.records == "null") {
                    current.records = recordInternetData.toString()
                } else {
                    val recordsList = getRecordsList(current?.records!!)
                    var add = false
                    for (i in recordsList.indices) {
                        if (recordsList[i]?.id == recordInternetData.id) {
                            recordsList[i] = recordInternetData
                            add = true
                            break
                        }
                    }
                    if (!add)
                        current.records += "/$recordInternetData"
                }

                recordUserDb.updateChildren(
                    mapOf(
                        "email" to current.email,
                        "name" to current.name,
                        "photo" to current.photo,
                        "records" to current.records,
                        "uid" to current.uid
                    )
                ).addOnSuccessListener {
                    topRecordDb.get().addOnSuccessListener {
                        current.records = recordInternetData.toString()
                        if (it.exists()) {
                            if (RecordInternetData(it.getValue(AccountData::class.java)?.records!!).record!! <
                                recordInternetData.record!!
                            )
                                topRecordDb.updateChildren(
                                    mapOf(
                                        "email" to current.email,
                                        "name" to current.name,
                                        "photo" to current.photo,
                                        "records" to current.records,
                                        "uid" to current.uid
                                    )
                                )
                        } else {
                            topRecordDb.setValue(current)
                        }
                        result.postValue(true)
                    }.addOnCanceledListener {
                        result.postValue(false)
                    }.addOnFailureListener {
                        Log.e("firebase", it.toString())
                        result.postValue(false)
                    }
                }.addOnCanceledListener {
                    result.postValue(false)
                }.addOnFailureListener {
                    Log.e("firebase", it.toString())
                    result.postValue(false)
                }
            }.addOnCanceledListener {
                result.postValue(false)
            }.addOnFailureListener {
                Log.e("firebase", it.toString())
                result.postValue(false)
            }
        }

    suspend fun addAlarmsToUser(
        account: AccountData,
        alarms: ArrayList<AlarmData>,
        result: MutableLiveData<Boolean?>
    ) =
        withContext(Dispatchers.IO) {
            deleteAlarmsOfUser(account, MutableLiveData())
            val userAlarms = usersDatabase.child(account.uid!!).child("alarms")
            for (alarm in alarms) {
                userAlarms.child(alarm.id.toString()).setValue(alarm).addOnSuccessListener {
                    result.postValue(true)
                }.addOnCanceledListener {
                    result.postValue(false)
                }.addOnFailureListener {
                    Log.e("firebase", it.toString())
                    result.postValue(false)
                }
            }
        }

    suspend fun deleteAlarmsOfUser(account: AccountData, result: MutableLiveData<Boolean?>) =
        withContext(Dispatchers.IO) {
            usersDatabase.child(account.uid!!).child("alarms").removeValue()
                .addOnSuccessListener {
                    result.postValue(true)
                }.addOnCanceledListener {
                    result.postValue(false)
                }.addOnFailureListener {
                    Log.e("firebase", it.toString())
                    result.postValue(false)
                }
        }

    suspend fun getAlarms(
        account: AccountData,
        alarmsList: MutableLiveData<List<AlarmData>>,
        result: MutableLiveData<Boolean?>
    ) =
        withContext(Dispatchers.IO) {
            val userAlarms = usersDatabase.child(account.uid!!).child("alarms")
            userAlarms.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    alarmsList.postValue(
                        snapshot.children.map { dataSnapshot ->
                            dataSnapshot.getValue(AlarmData::class.java)!!
                        }
                    )
                    result.postValue(true)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("firebase", error.toString())
                    result.postValue(false)
                }
            })
        }

    suspend fun deleteRecordOfUser(accountData: AccountData) =
        withContext(Dispatchers.IO) {
            val newRecord = RecordInternetData(accountData.records!!)
            val userRecords = usersDatabase.child(accountData.uid!!).child("records")
            userRecords.get().addOnSuccessListener {
                val current = it.getValue(String::class.java)
                val records = getRecordsList(current!!)
                for (i in records.indices) {
                    if (records[i]?.id!! == newRecord.id)
                        records.removeAt(i)
                }
                userRecords.setValue(arrayToString(records))
            }
            deleteTopRecordIfNeed(accountData, newRecord, MutableLiveData())
        }

    private fun deleteTopRecordIfNeed(
        account: AccountData,
        recordInternetData: RecordInternetData,
        result: MutableLiveData<Boolean?>
    ) {
        val topOfGame = topRecordsDatabase.child(recordInternetData.gameId.toString())
        topOfGame.get().addOnSuccessListener {
            val currentTop = it.getValue(AccountData::class.java)
            val currentRecord = RecordInternetData(currentTop?.records!!)

            if (currentTop.uid == account.uid && currentRecord.id == recordInternetData.id) {
                currentTop.name = "Аноним"
                currentTop.uid = ""
                currentTop.email = ""
                currentTop.photo =
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRHH_48-fhI2OTTKlHo-FagFrwi3LcF6gf8jx142YctSw&s"
            }
            topOfGame.setValue(currentTop).addOnSuccessListener {
                result.postValue(true)
            }.addOnCanceledListener {
                result.postValue(false)
            }.addOnFailureListener {
                Log.e("firebase", it.toString())
                result.postValue(false)
            }
        }
    }

    suspend fun deleteRecordsOfUser(account: AccountData, result: MutableLiveData<Boolean?>) =
        withContext(Dispatchers.IO) {
            val recordDb = usersDatabase.child(account.uid!!).child("records")
            recordDb.get()
                .addOnSuccessListener {

                    val current = it.getValue(String::class.java)

                    if (current != "null") {

                        val records = getRecordsList(current!!)
                        for (record in records)
                            deleteTopRecordIfNeed(account, record!!, result)

                        recordDb.setValue("null")
                    } else
                        result.postValue(true)

                }.addOnCanceledListener {
                    result.postValue(false)
                }.addOnFailureListener {
                    Log.e("firebase", it.toString())
                    result.postValue(false)
                }
        }

    suspend fun deleteAccount(account: AccountData, result: MutableLiveData<Boolean?>) =
        withContext(Dispatchers.IO) {
            usersDatabase.child(account.uid!!).removeValue()
                .addOnSuccessListener {
                    result.postValue(true)
                }.addOnCanceledListener {
                    result.postValue(false)
                }.addOnFailureListener {
                    Log.e("firebase", it.toString())
                    result.postValue(false)
                }
        }
}