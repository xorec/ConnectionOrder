package com.xorec.connectionorder.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class House(
    @SerializedName("house_id") val houseId: Long,
    @SerializedName("house") val house: String
): Serializable, Comparable<House> {
    /* Класс House реализует интерфейс Comparable для реализации пункта требований 5, а именно сортировки
    *  по номеру дома в списке предлагаемых номеров домов. Дополнительно реализовано требование о сортировке
    *  по номеру корпуса. Отношение порядка строится следующим образом: строка разбивается на содержащиеся в ней числа по порядку
    *  с помощью регулярного выражения, затем числа на одних и тех же позициях сравниваются. Если одна строка
    *  короче другой, то сравниваются числа на доступных позициях. Если числа на соответствующих позициях не равны,
    *  то "больше" будет та строка, где больше соответствующее число. Если все числа на доступных позициях равны,
    *  то "большей" выбирается строка с большей длиной. Т.е. между строками "23" и "23к1" "больше" будет вторая.
    *  Между строками "42" и "23к1" больше будет первая. Если же строка не содержит в себе вообще никаких номеров,
    *  то она всегда "меньше" строки, в которой есть хотя бы один номер. Поэтому вариант выбора дома
    *  со строкой "выберите дом" всегда будет на позиции 0, поскольку при сортировке он будет "меньше всех". */
    override fun compareTo(other: House): Int {
        val numberRegex = Regex("[0-9]+")
        val myNumbers = numberRegex.findAll(house, 0).toList().map {it.value.toInt()}
        val otherNumbers = numberRegex.findAll(other.house, 0).toList().map {it.value.toInt()}

        for (index in 0 until Integer.min(myNumbers.size, otherNumbers.size)) {
            if (myNumbers[index] != otherNumbers[index]) {
                return myNumbers[index] - otherNumbers[index]
            }
        }

        return myNumbers.size - otherNumbers.size
    }
}