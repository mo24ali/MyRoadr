package com.example.myroadr.ViewModel

import android.app.Application
import android.location.Location
import androidx.lifecycle.*
import com.example.myroadr.DB.AppDatabase
import com.example.myroadr.models.CyclingEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CyclingEventViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).eventDao()
    private val _events = MutableLiveData<List<CyclingEvent>>()
    val events: LiveData<List<CyclingEvent>> = _events

    fun loadEvents(userLocation: Location) {
        viewModelScope.launch(Dispatchers.IO) {
            val entities = dao.getAll()
            val events = entities.map {
                CyclingEvent(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    date = it.date,
                    locationName = it.locationName,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    createdBy = it.createdBy,
                    participants = List(it.participantsCount) { "user" }
                )
            }
            _events.postValue(events)
        }
    }
}


