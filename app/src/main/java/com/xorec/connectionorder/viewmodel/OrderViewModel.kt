package com.xorec.connectionorder.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.xorec.connectionorder.ConnectionOrderApp
import com.xorec.connectionorder.model.House
import com.xorec.connectionorder.model.Street
import retrofit2.Call
import retrofit2.Response

/* Ключи для SavedStateHandle. */
const val STREETS_KEY = "STREETS_KEY"
const val SELECTED_STREET_KEY = "SELECTED_STREET_KEY"
const val SELECTED_STREET_HOUSES_KEY = "SELECTED_STREET_HOUSES_KEY"
const val SELECTED_HOUSE_KEY = "SELECTED_HOUSE_KEY"
const val CAN_SEND_ORDER_KEY = "CAN_SEND_ORDER_KEY"

/* ViewModel, которая привязана к жизненному циклу единственного экрана в приложении (OrderFragment).
*  Используется SavedStateHandle, чтобы предотвратить потерю состояния при выгрузке процесса приложения
*  из памяти (что вызовет onCleared() и уничтожение viewmodel). SavedStateHandle занимается сохранением
*  состояния в постоянную память и загрузкой состояния из постоянной памяти при перезапуске процесса.
*  Данное поведение можно вызвать, указав опцию "вытеснять фоновые Activity" в настройках разработчика
*  в телефоне.
*  Всего есть 6 полей:
*  1) Выбранная из списка улица: ORDER_INFO_TYPE_SELECTED_STREET, STREETS_KEY;
*  2) Выбранный из списка дом: ORDER_INFO_TYPE_SELECTED_HOUSE, SELECTED_STREET_KEY;
*  3) Введенная пользователем улица: ORDER_INFO_TYPE_ENTERED_STREET (поле ввода то же,
*  что и у ORDER_INFO_TYPE_SELECTED_STREET, но логика разная);
*  4) Введенный пользователем дом: ORDER_INFO_TYPE_ENTERED_HOUSE;
*  5) Введенный пользователем корпус: ORDER_INFO_TYPE_ENTERED_BUILDING;
*  6) Введенная пользователем квартира: ORDER_INFO_TYPE_ENTERED_APARTMENT.
*  Поля, которые содержат ENTERED в названии, сохранять в SavedStateHandle нет необходимости, поскольку
*  Fragment при загрузке обновит их состояние из SavedInstanceState соответствующих полей.
*  По заданию [пункт 11 требований] необходимо, чтобы можно было отправлять запрос при комбинации
*  заполненных полей либо 1-2-6, либо 1-3-4-5-6, либо 3-4-5-6. */
class OrderViewModel(private val state: SavedStateHandle): ViewModel() {
    private var enteredStreet: String = ""
    private var enteredHouse: String = ""
    private var enteredBuilding: String = ""
    private var enteredApartment: String = ""
    val streets: LiveData<List<Street>> = state.getLiveData(STREETS_KEY, ArrayList())
    val selectedStreetHouses: LiveData<List<House>?> = state.getLiveData(SELECTED_STREET_HOUSES_KEY, null)
    var selectedHouse: LiveData<House?> = state.getLiveData(SELECTED_HOUSE_KEY, null)
    val canSendOrder: LiveData<Boolean> = state.getLiveData(CAN_SEND_ORDER_KEY, false)
    private var selectedStreet: Street?
        get() = state[SELECTED_STREET_KEY]
        set(value) {
            state[SELECTED_STREET_KEY] = value
        }

    /* Вспомогательный enum, который перечисляет возможные поля ввода, используется в функции
    *  updateInfo(). */
    enum class OrderInfoType {
        ORDER_INFO_TYPE_SELECTED_STREET,
        ORDER_INFO_TYPE_SELECTED_HOUSE,
        ORDER_INFO_TYPE_ENTERED_STREET,
        ORDER_INFO_TYPE_ENTERED_HOUSE,
        ORDER_INFO_TYPE_ENTERED_BUILDING,
        ORDER_INFO_TYPE_ENTERED_APARTMENT
    }

    /* Вспомогательный enum, который перечисляет возможные для отправки типы запросов. */
    enum class AvailableOrderType {
        STREET_ID_HOUSE_ID_ORDER_TYPE,
        STREET_ID_ENTERED_HOUSE_ORDER_TYPE,
        ENTERED_STREET_ENTERED_HOUSE_ORDER_TYPE,
        NO_ORDER_AVAILABLE_TYPE
    }

    /* При инициализации viewmodel происходит запрос данных улиц на сервер. Если запрос успешен,
    *  полученные данные сохраняются в поле streets. */
    init {
        ConnectionOrderApp.instance.addresses.getStreets().enqueue(object: retrofit2.Callback<List<Street>> {
            override fun onResponse(call: Call<List<Street>>, response: Response<List<Street>>) {
                if (response.isSuccessful) state[STREETS_KEY] = response.body()
                else state[STREETS_KEY] = ArrayList<Street>()
            }
            override fun onFailure(call: Call<List<Street>>, t: Throwable) {
                state[STREETS_KEY] = ArrayList<Street>()
            }
        })
    }

    /* Функция, которая является единой точкой входа для обновления данных. Чтобы обновить поле,
    *  нужно указать его тип и дать новое значение. Произойдет обновление значения, после чего
    *  произойдет пересчет возможности отправления запроса. */
    fun updateInfo(infoType: OrderInfoType, input: Any?) {
        when (infoType) {
            OrderInfoType.ORDER_INFO_TYPE_SELECTED_STREET -> {
                if (input is Street?) {
                    if (input == null) {
                        selectedStreet = null
                        state[SELECTED_STREET_HOUSES_KEY] = null
                        state[SELECTED_HOUSE_KEY] = null
                    } else {
                        selectedStreet = input

                        /* Запрос номеров домов по выбранной улице. Здесь важно обрабатывать случай ошибки,
                        *  поскольку, как выяснилось при описании, при запросе некоторых улиц, например,
                        *  ул. Вологдина, сервер отвечает ошибкой с сообщением "Ошибка БД". Для простоты
                        *  пусть в случае ошибки будет возвращен пустой лист. */
                        ConnectionOrderApp.instance.addresses.getHouses(selectedStreet!!)
                            .enqueue(object: retrofit2.Callback<List<House>> {
                            override fun onResponse(
                                call: Call<List<House>>,
                                response: Response<List<House>>
                            ) {
                                if (response.isSuccessful) state[SELECTED_STREET_HOUSES_KEY] = response.body()
                                else state[SELECTED_STREET_HOUSES_KEY] = ArrayList<List<House>>()
                            }

                            override fun onFailure(call: Call<List<House>>, t: Throwable) {
                                state[SELECTED_STREET_HOUSES_KEY] = ArrayList<List<House>>()
                            }
                        })
                    }
                }
            }
            OrderInfoType.ORDER_INFO_TYPE_SELECTED_HOUSE -> {
                if (input is House?) {
                    state[SELECTED_HOUSE_KEY] = input
                }
            }
            OrderInfoType.ORDER_INFO_TYPE_ENTERED_STREET -> {
                if (input is String) {
                    enteredStreet = input
                }
            }
            OrderInfoType.ORDER_INFO_TYPE_ENTERED_HOUSE -> {
                if (input is String) {
                    enteredHouse = input
                }
            }
            OrderInfoType.ORDER_INFO_TYPE_ENTERED_BUILDING -> {
                if (input is String) {
                    enteredBuilding = input
                }
            }
            OrderInfoType.ORDER_INFO_TYPE_ENTERED_APARTMENT -> {
                if (input is String) {
                    enteredApartment = input
                }
            }
        }

        /* Пересчет возможности послать запрос после обновления данных. */
        state[CAN_SEND_ORDER_KEY] = availableOrderType() != AvailableOrderType.NO_ORDER_AVAILABLE_TYPE
    }

    /* Функция вычисления возможности отправить запрос (и если да, то какой именно). */
    private fun availableOrderType(): AvailableOrderType {
        return if (selectedStreet != null) {
            if (selectedHouse.value != null) {
                if (enteredApartment.isNotEmpty()) {
                    AvailableOrderType.STREET_ID_HOUSE_ID_ORDER_TYPE
                } else AvailableOrderType.NO_ORDER_AVAILABLE_TYPE
            } else if (enteredHouse.isNotEmpty() && enteredBuilding.isNotEmpty()
                && enteredApartment.isNotEmpty()) {
                AvailableOrderType.STREET_ID_ENTERED_HOUSE_ORDER_TYPE
            } else AvailableOrderType.NO_ORDER_AVAILABLE_TYPE
        } else if (enteredStreet.isNotEmpty() && enteredHouse.isNotEmpty() && enteredBuilding.isNotEmpty()
            && enteredApartment.isNotEmpty()) {
            AvailableOrderType.ENTERED_STREET_ENTERED_HOUSE_ORDER_TYPE
        } else AvailableOrderType.NO_ORDER_AVAILABLE_TYPE
    }

    /* Функция "отправки заявки на сервер". Поскольку по заданию необходимо просто вывести тост,
    *  возвращает текст с форматом запроса и данными, которые бы отправились. */
    fun sendOrder(): String {
        return when (availableOrderType()) {
            AvailableOrderType.STREET_ID_HOUSE_ID_ORDER_TYPE -> {
                "ID улицы - ${selectedStreet!!.streetId}, " +
                        "ID дома - ${selectedHouse.value!!.houseId}, квартира - ${enteredApartment}"
            }

            AvailableOrderType.STREET_ID_ENTERED_HOUSE_ORDER_TYPE -> {
                "ID улицы - ${selectedStreet!!.streetId}, " +
                        "дом - ${enteredHouse}, корпус - ${enteredBuilding}, квартира - ${enteredApartment}"
            }

            AvailableOrderType.ENTERED_STREET_ENTERED_HOUSE_ORDER_TYPE -> {
                "улица - ${enteredStreet}, " +
                        "дом - ${enteredHouse}, корпус - ${enteredBuilding}, квартира - ${enteredApartment}"
            }
            else -> "Нет достаточных данных для заявки"
        }
    }
}