package com.xorec.connectionorder.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.xorec.connectionorder.R
import com.xorec.connectionorder.databinding.OrderFragmentBinding
import com.xorec.connectionorder.model.House
import com.xorec.connectionorder.ui.adapters.HousesAdapter
import com.xorec.connectionorder.ui.adapters.StreetsAdapter
import com.xorec.connectionorder.viewmodel.OrderViewModel

/* Единственный экран в приложении. Используется OrderViewModel и binding. Вся суть логики здесь сводится
*  к тому, чтобы подписаться на обновления из viewmodel (загруженные улицы/номера домов), показать/скрыть
*  различные элементы при вводе в поля и обновить введенные данные в viewmodel через updateInfo. */
class OrderFragment : Fragment() {
    private val viewModel: OrderViewModel by viewModels()
    private lateinit var binding: OrderFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.order_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Блок инициализации поля ввода названия улицы. */
        binding.orderFragmentStreetActv.let { streetActv ->

            val streetsAdapter = StreetsAdapter(requireContext())
            viewModel.streets.observe(viewLifecycleOwner) { streets ->
                streetsAdapter.submitData(streets)
            }
            streetActv.setAdapter(streetsAdapter)

            /* При нажатии на улицу из списка обновляем инфу и сбрасываем выбранный вариант у поля номера дома. */
            streetActv.onItemClickListener =
                OnItemClickListener { _, _, position, _ ->
                    viewModel.updateInfo(OrderViewModel.OrderInfoType.ORDER_INFO_TYPE_SELECTED_STREET, streetsAdapter.getItem(position))
                    binding.orderFragmentSpinner.setSelection(0)
                }

            /* Функция слежения за вводом пользователя в поле ввода названия улицы. При каждом изменении
            *  необходимо отразить соответствующее изменение во viewmodel, чтобы пересчитать возможность
            *  отправки запроса. Если пользователь ввел что-то с клавиатуры, то необходимо во viewmodel
            *  сбросить ввод из списка, если таковой был. */
            streetActv.doOnTextChanged { text, start, before, count ->
                viewModel.updateInfo(OrderViewModel.OrderInfoType.ORDER_INFO_TYPE_ENTERED_STREET, text.toString())
                if ((before == 0 && count == 1) || (before == 1 && count == 0) || start != 0) {
                    viewModel.updateInfo(OrderViewModel.OrderInfoType.ORDER_INFO_TYPE_SELECTED_STREET, null)
                }
            }
        }

        /* Блок инициализации спиннера. */
        binding.orderFragmentSpinner.let { spinner ->
            val housesAdapter = HousesAdapter(requireContext())

            /* При повороте экрана необходимо вручную сделать submitData в адаптер спиннера, иначе
            *  спиннер сбросит ввод (поскольку данных нет) до того, как observe передаст соответствующие
            *  данные. */
            if (viewModel.selectedStreetHouses.value != null && (viewModel.selectedStreetHouses.value as ArrayList<House>).isNotEmpty()) {
                housesAdapter.submitData(viewModel.selectedStreetHouses.value!!)
            }
            spinner.adapter = housesAdapter

            /* Подписка на обновления данных для спиннера. Если приходит null, то спиннер скрывается,
            *  если не null, то спиннер открывается и принимает данные в адаптер. */
            viewModel.selectedStreetHouses.observe(viewLifecycleOwner) { selectedStreetHouses ->
                if (selectedStreetHouses != null && selectedStreetHouses.isEmpty()) {
                    Toast.makeText(requireContext(), "Сервер вернул \"Ошибка БД\"", LENGTH_SHORT).show()
                }
                if (selectedStreetHouses.isNullOrEmpty()) {
                    housesAdapter.clear()
                    spinner.visibility = GONE
                } else  {
                    housesAdapter.submitData(selectedStreetHouses)
                    spinner.visibility = VISIBLE
                }
            }

            /* При выборе элемента из спиннера обновляем информацию во viewmodel. Однако на позиции 0
            *  всегда находится пустой вариант ("выберите дом"), что соответствует отсутствию выбора. */
            spinner.onItemSelectedListener = object: OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    /* Если выбрана позиция 0, то необходимо сбросить ввод выбранного дома во viewmodel. */
                    if (p2 == 0) {
                        viewModel.updateInfo(OrderViewModel.OrderInfoType.ORDER_INFO_TYPE_SELECTED_HOUSE, null)
                        return
                    }

                    /* Если выбран реальный вариант, то обновляем ввод выбранного дома соответствующим
                    *  значением во viewmodel. */
                    viewModel.updateInfo(OrderViewModel.OrderInfoType.ORDER_INFO_TYPE_SELECTED_HOUSE, housesAdapter.getItem(p2))
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    viewModel.updateInfo(OrderViewModel.OrderInfoType.ORDER_INFO_TYPE_SELECTED_HOUSE, null)
                }
            }
        }

        /* Подписка на обновления состояния поля выбранного дома. Если выбран дом, то поля ввода
        *  "дом" и "корпус" необходимо скрыть, иначе показывать. */
        viewModel.selectedHouse.observe(viewLifecycleOwner) { house ->
            if (house == null) {
                binding.orderFragmentHouseTil.visibility = VISIBLE
                binding.orderFragmentSpace1.visibility = VISIBLE
                binding.orderFragmentBuildingTil.visibility = VISIBLE
                binding.orderFragmentSpace2.visibility = VISIBLE
            } else {
                binding.orderFragmentHouseTil.visibility = GONE
                binding.orderFragmentSpace1.visibility = GONE
                binding.orderFragmentBuildingTil.visibility = GONE
                binding.orderFragmentSpace2.visibility = GONE
            }
        }

        /* Если произошел ввод в поля "дом"/"корпус"/"квартира", то необходимо уведомить об этом
        *  viewmodel. Естественно, это нужно не для того, чтобы сохранить ввод при повороте экрана,
        *  поскольку у EditText есть свой SavedInstanceState, и он делает это самостоятельно, как
        *  и должна делать любая View. Но в нашем случае необходимо уведомлять viewmodel о любых
        *  изменениях в любом поле ввода, чтобы пересчитывать возможность отправки запроса, поэтому
        *  viewmodel должна знать об изменениях. */
        binding.orderFragmentHouseEt.doOnTextChanged { text, _, _, _ ->
            viewModel.updateInfo(OrderViewModel.OrderInfoType.ORDER_INFO_TYPE_ENTERED_HOUSE, text.toString())
        }
        binding.orderFragmentBuildingEt.doOnTextChanged { text, _, _, _ ->
            viewModel.updateInfo(OrderViewModel.OrderInfoType.ORDER_INFO_TYPE_ENTERED_BUILDING, text.toString())
        }
        binding.orderFragmentApartmentEt.doOnTextChanged { text, _, _, _ ->
            viewModel.updateInfo(OrderViewModel.OrderInfoType.ORDER_INFO_TYPE_ENTERED_APARTMENT, text.toString())
        }

        /* Если кнопка "Отправить" активна, происходит вызов этого clicklistener'a. Согласно заданию
        *  [пункт 11 требований] необходимо выводить тост, в котором содержится формат запроса и отправляемая
        *  информация. Вычислением возможности отправки запроса занимается viewmodel. */
        binding.orderFragmentSendBtn.setOnClickListener {
            Toast.makeText(requireContext(), viewModel.sendOrder(), LENGTH_SHORT).show()
        }

        /* Подписка на обновления возможности отправить запрос. Если запрос можно отправить, то
        *  кнопка активируется, иначе кнопка деактивирована. */
        viewModel.canSendOrder.observe(viewLifecycleOwner) { canSendOrder ->
            binding.orderFragmentSendBtn.isEnabled = canSendOrder
        }
    }

    override fun onResume() {
        super.onResume()

        /* Пункт 1 требований. При открытии приложения фокус получает поле ввода улицы, но при переворотах
        *  экрана и перезапусках процесса фокус забираться принудительно у других полей не должен. */
        if (!binding.orderFragmentHouseEt.hasFocus() && !binding.orderFragmentBuildingEt.hasFocus()
            && !binding.orderFragmentApartmentEt.hasFocus()) {
            binding.orderFragmentStreetActv.requestFocus()
        }
    }
}