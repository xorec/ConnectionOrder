package com.xorec.connectionorder.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.xorec.connectionorder.R
import com.xorec.connectionorder.model.Street

/* Адаптер для списка загруженных улиц. Согласно пункту 3 требований поиск должен осуществляться по
*  вхождению ввода в название, поэтому необходимо реализовывать интерфейс Filterable. Адаптер публикует
*  коллекцию filteredStreets для отображения, однако содержит и референс на оригинальную коллекцию для
*  последующих фильтраций. */
class StreetsAdapter(private val context: Context) : BaseAdapter(), Filterable {
    private val streets = ArrayList<Street>()
    private val filteredStreets = ArrayList<Street>()
    private val filter = object: Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            filteredStreets.clear()
            results.values = filteredStreets
            results.count = 0

            if (constraint == null) {
                return results
            }

            for (street in streets) {
                /* Фильтрация по вхождению ввода (constraint) в название. */
                if (street.toString().contains(constraint, true)) {
                    filteredStreets.add(street)
                }
            }

            results.count = filteredStreets.count()

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results == null || constraint == null) {
                notifyDataSetInvalidated()
                return
            }

            if (results.count > 0) {
                notifyDataSetChanged()
            } else notifyDataSetInvalidated()
        }
    }

    override fun getFilter(): Filter {
        return filter
    }

    override fun getCount(): Int {
        return filteredStreets.size
    }

    override fun getItem(position: Int): Any {
        return filteredStreets[position]
    }

    override fun getItemId(position: Int): Long {
        return filteredStreets[position].streetId
    }

    override fun getView(position: Int, v: View?, viewGroup: ViewGroup?): View {
        val view = v ?: LayoutInflater.from(context).inflate(R.layout.order_fragment_suggestion, null)
        val tv = view.findViewById<TextView>(R.id.order_fragment_suggestion)
        tv.text = filteredStreets[position].street
        return view
    }

    fun submitData(data: List<Street>) {
        streets.clear()
        streets.addAll(data)
    }
}