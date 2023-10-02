package com.xorec.connectionorder.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.xorec.connectionorder.R
import com.xorec.connectionorder.model.House

/* Адаптер для списка номеров домов. Всегда содержит позицию-заглушку "выберите дом", которая
*  соответствует отсутствию выбора. */
class HousesAdapter(private val context: Context): BaseAdapter() {
    private val mockHouse = House(-1, context.getString(R.string.order_fragment_choose_house))
    private val houses = ArrayList<House>(arrayListOf(mockHouse))

    override fun getCount(): Int {
        return houses.size
    }

    override fun getItem(position: Int): House {
        return houses[position]
    }

    override fun getItemId(position: Int): Long {
        return houses[position].houseId
    }

    override fun getView(position: Int, v: View?, viewGroup: ViewGroup?): View {
        val view = v ?: LayoutInflater.from(context).inflate(R.layout.order_fragment_suggestion, null)
        val tv = view.findViewById<TextView>(R.id.order_fragment_suggestion)
        tv.text = houses[position].house
        return view
    }

    /* При обновлении данных на первую позицию всегда добавляется позиция-заглушка "выберите дом".
    *  Далее происходит сортировка по номеру дома и корпусу. Позиция "выберите дом" тоже участвует в
    *  сортировке и отношение порядка класса House гарантирует, что позиция-заглушка останется на позиции 0
    *  (см. класс House для описания отношения порядка). */
    fun submitData(data: List<House>) {
        clear()
        houses.addAll(data)
        houses.sort()
        notifyDataSetChanged()
    }

    fun clear() {
        houses.clear()
        houses.add(mockHouse)
    }
}